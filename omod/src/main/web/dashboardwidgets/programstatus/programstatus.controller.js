
import angular from 'angular';
import moment from 'moment';

export default class ProgramStatusController {

    // TODO add support for special logic around "initial" and "terminal?"

    constructor($filter, $window, $q, openmrsRest, openmrsTranslate) {
        'ngInject';

        Object.assign(this, {$filter, $window, $q, openmrsRest, openmrsTranslate});
    }

    $onInit() {
        this.vPatientProgram = 'custom:uuid,program:(uuid),dateEnrolled,dateCompleted,outcome:(display),location:(display,uuid),dateCompleted,outcome,states:(uuid,startDate,endDate,voided,state:(uuid,concept:(display)))';

        this.dateFormat = (this.config.dateFormat == '' || angular.isUndefined(this.config.dateFormat))
            ? 'dd-MMM-yyyy' : this.config.dateFormat;
        this.today = new Date();

        this.loaded = false;
        this.deleted = false;

        this.program = null;
        this.patientProgram = null;
        this.patientPrograms = null;
        this.programLocations = null;
        this.programOutcomes = null;

        // we calculate these two values based on the completion date of the previous program and the enrollment date of any subsequent program
        // we populate these on initial load
        this.minEnrollmentDate = null;
        this.maxCompletionDate = null;

        this.canEnrollInProgram = false;
        this.canEditProgram = false;
        this.canDeleteProgram = false;

        this.statesByWorkflow = {};
        this.statesByUuid = {};
        this.sortedStatesByWorkflow = {};

        // backs the various input fields
        this.input = {
            dateEnrolled: null,
            enrollmentLocation: null,
            dateCompleted: null,
            outcome: null,
            initialWorkflowStateByWorkflow: {},
            changeToStateByWorkflow: {}
        }

        this.resetWindowStates();

        this.activate();

        let ctrl = this;

    }

    activate() {
        this.openmrsRest.setBaseAppPath("/coreapps");

        this.fetchPrivileges();

        this.fetchLocations().then((response) => {
            this.fetchProgram().then((response) => {
                this.fetchOutcomes();
                this.fetchPatientProgram()
            });
        });
    }

    fetchPrivileges() {
        this.openmrsRest.get('session', {
            v: 'custom:(privileges)'
        }).then((response) => {
            if (response && response.user && angular.isArray(response.user.privileges)) {
                if (response.user.privileges.some( (p) => { return p.name === 'Task: coreapps.enrollInProgram'; })) {
                    this.canEnrollInProgram = true;
                };
                if (response.user.privileges.some( (p) => { return p.name === 'Task: coreapps.editPatientProgram'; })) {
                    this.canEditProgram = true;
                };
                if (response.user.privileges.some( (p) => { return p.name === 'Task: coreapps.deletePatientProgram'; })) {
                    this.canDeleteProgram = true;
                };
            }
        });
    }

    setInputsToStartingValues() {
        this.input.dateEnrolled = this.patientProgram ? new Date(this.patientProgram.dateEnrolled) : null;
        this.input.dateCompleted = this.patientProgram && this.patientProgram.dateCompleted ? new Date(this.patientProgram.dateCompleted) : null;
        this.input.outcome = this.patientProgram && this.patientProgram.outcome ? this.patientProgram.outcome.uuid : null;

        if (this.patientProgram && this.patientProgram.location) {
            this.input.enrollmentLocation = this.patientProgram.location.uuid;
        }
        // if there is only possible location, set it as the default (this is why loading locations (in activate) needs to happen before patient programs)
        else if (this.programLocations && this.programLocations.length == 1) {
            this.input.enrollmentLocation = this.programLocations[0].uuid;
        }

        this.input.initialWorkflowStateByWorkflow = {};
        this.input.changeToStateByWorkflow = {};
    }

    convertDateEnrolledAndDateCompletedStringsToDates() {
        if (this.patientProgram) {
            this.patientProgram.dateEnrolled = this.patientProgram ? new Date(this.patientProgram.dateEnrolled) : null;
            this.patientProgram.dateCompleted = this.patientProgram && this.patientProgram.dateCompleted ? new Date(this.patientProgram.dateCompleted) : null;
        }
    }

    toggleEditEnrollment() {
        let currentStatus = this.edit.enrollment;
        this.cancelAllEditModes();
        this.setInputsToStartingValues();

        this.edit.enrollment = !currentStatus;
    }

    toggleEditWorkflow(workflowUuid) {
        let currentStatus = this.edit.workflow[workflowUuid];
        this.cancelAllEditModes();
        this.setInputsToStartingValues();

        // the first time we hit this, we need to initialize the workflow
        if (!workflowUuid in this.edit.workflow) {
            this.edit.workflow[workflowUuid] = true
        }
        else {
            this.edit.workflow[workflowUuid] = !currentStatus;
        }
    }

    toggleExpandedWorkflow(workflowUuid) {
        // the first time we hit this, we need to initialize the workflow
        if (!workflowUuid in this.expanded.workflow) {
            this.expanded.workflow[workflowUuid] = true;
        }
        else {
            this.expanded.workflow[workflowUuid] = !this.expanded.workflow[workflowUuid];
        }
    }

    fetchProgram() {
        return this.openmrsRest.get('program', {
            uuid: this.config.program,
            v: 'custom:display,uuid,outcomesConcept:(uuid),allWorkflows:(uuid,concept:(display),states:(uuid,initial,terminal,concept:(display))'
        }).then((response) => {
            // TODO handle error cases, program doesn't exist
            this.program = response;

            angular.forEach(this.program.allWorkflows, (workflow) => {
                this.statesByWorkflow[workflow.uuid] = workflow.states;
                angular.forEach(workflow.states, (state) => {
                    this.statesByUuid[state.uuid] = state;
                });
            });
        });
    }

    fetchPatientProgram() {

        this.loaded = false;
        this.patientProgram = null;
        this.resetWindowStates();

        return this.openmrsRest.get('programenrollment', {
            patient: this.config.patientUuid,
            v: this.vPatientProgram
        }).then((response) => {
            this.patientPrograms = response.results;
            this.getPatientProgramFromPatientProgramList();
            this.groupAndSortPatientStates();
            this.setInputsToStartingValues();
            this.convertDateEnrolledAndDateCompletedStringsToDates();
            this.loaded = true;
        });
    }

    fetchLocations() {
        return this.openmrsRest.get('location', {
            v: 'custom:display,uuid',
            tag: this.config.locationTag,
        }).then((response) => {
            this.programLocations = response.results;
        })
    }


    fetchOutcomes() {
        if (this.program.outcomesConcept) {
            return this.openmrsRest.get('concept', {
                v: 'custom:answers:(display,uuid)',
                uuid: this.program.outcomesConcept.uuid,
            }).then((response) => {
                this.programOutcomes = response.answers;
            })
        }
    }

    // get the patient program that this widget will be displaying/manipulating
    getPatientProgramFromPatientProgramList() {

        // first, filter to only patient programs of the specified type
        this.patientPrograms = this.$filter('filter') (this.patientPrograms, (patientProgram) => {
            return (patientProgram.program.uuid == this.config.program);
        });


        if (this.patientPrograms.length > 0) {

            // sort programs in order
            this.patientPrograms = this.$filter('orderBy')(this.patientPrograms, (patientProgram) => {
                return -this.patientPrograms.startDate;
            });

            // find the matching program, and set the min/max for enroll/complete based on the surrounding programs
            if (!this.displayActiveProgram()) {

                angular.forEach(this.patientPrograms, (patientProgram, i) => {
                    if (patientProgram.uuid == this.config.patientProgram) {
                        this.patientProgram = patientProgram;
                        if (i > 0) {
                            this.maxCompletionDate = this.getPreviousDay(new Date(this.patientPrograms[i-1].dateEnrolled));
                        }
                        if (i + 1 < this.patientPrograms.length) {
                            this.minEnrollmentDate = this.getNextDay(new Date(this.patientPrograms[i+1].dateCompleted));
                        }
                    }
                })

                // TODO error case: no match found
            }
            // this widget is meant to show the active program, or if no active program, will render a widget for enrolling in the program
            else {
                // there's an active program
                if (!this.patientPrograms[0].dateCompleted) {
                    this.patientProgram = this.patientPrograms[0];
                    if (this.patientPrograms.length > 1) {
                        // enrollment date cannot be shifted to before completion date of previous program
                        this.minEnrollmentDate = this.getNextDay(new Date(this.patientPrograms[1].dateCompleted))

                    }
                }
                // no active program
                else {
                    // enrollment date for a new program can't be set before the completion date of any previous program

                    this.minEnrollmentDate = this.getNextDay(new Date(this.patientPrograms[0].dateCompleted))
                }

            }

        }
    }

    enrollInProgram() {
        if (this.input.dateEnrolled && this.input.enrollmentLocation) {

            var states = [];
            angular.forEach(this.input.initialWorkflowStateByWorkflow, (state) => {
                state.startDate = this.input.dateEnrolled;
                states.push(state);
            });

            this.openmrsRest.create('programenrollment', {
                patient: this.config.patientUuid,
                program: this.config.program,
                dateEnrolled: this.input.dateEnrolled,
                location: this.input.enrollmentLocation,
                states: states
            }).then((response) => {
                if (this.config.disableReloadPage) {
                    this.fetchPatientProgram(this.patientProgram.uuid); // refresh display
                }
                else {
                    this.reloadPage();  // closing a program affects other widgets, so we need to reload the entire page
                }
            });

        }
    }

    updatePatientProgram() {

        // we need to reload the entire page if date of enrollment or completion has changed, because may affect other program widget on the same page
        // (reloading can be disabled by using the "disable reload page" config parameter
        var needToReloadPage =
            !this.config.disableReloadPage &&
            (this.input.dateEnrolled.getTime() != this.patientProgram.dateEnrolled.getTime()) ||
            (this.input.dateCompleted && !this.patientProgram.dateCompleted) ||
            (!this.input.dateCompleted && this.patientProgram.dateCompleted) ||
            (this.input.dateCompleted && this.patientProgram.dateCompleted && this.input.dateCompleted.getTime() != this.patientProgram.dateCompleted.getTime());

        // (disabling this... keeping most recent state with no end date for consistency with current workflow)
        // we need to make sure that the most recent state for each workflow has an end date = completion date
        // (this should really be handled by the api?)
      /*  let states = [];
        angular.forEach(this.sortedStatesByWorkflow, (workflow) => {
            if (workflow.length > 0) {
                states.push({
                    uuid: workflow[0].uuid,
                    endDate: this.input.dateCompleted
                })
            }
        })*/

        this.openmrsRest.update('programenrollment/' + this.patientProgram.uuid, {
            dateEnrolled: this.input.dateEnrolled,
            dateCompleted: this.input.dateCompleted,
            location: this.input.enrollmentLocation,
            outcome: this.input.outcome//,
            //states: states
        }).then((response) => {
            if (!needToReloadPage) {
                this.fetchPatientProgram(this.patientProgram.uuid); // refresh display
            }
            else {
                this.reloadPage();  // closing a program affects other widgets, so we need to reload the entire page
            }
        });
    }

    deletePatientProgram() {
        if (!this.confirmDelete.enrollment) {
            this.confirmDelete.enrollment= true;
        }
        else {
            this.confirmDelete.enrollment = false;
            this.openmrsRest.remove('programenrollment/', {
                uuid: this.patientProgram.uuid
            }).then((response) => {
                this.patientProgram = null;

                // if this widget was set to display the "active program" (ie, no patient program uuid passed in)
                // then reload, otherwise render a "patient program deleted" message and reload entire page

                if (this.displayActiveProgram()) {
                    this.fetchPatientProgram(); // refresh display
                }
                else {
                    this.deleted = true;
                    if (!this.config.disableReloadPage) {
                        this.reloadPage();
                    }
                }
            })
        }
    }

    deleteMostRecentPatientState(workflowUuid) {
        if (!this.confirmDelete.workflow[workflowUuid]) {
            this.confirmDelete.workflow[workflowUuid] = true;
        }
        else {
            this.confirmDelete.workflow[workflowUuid] = false;
            if (workflowUuid in this.sortedStatesByWorkflow && this.sortedStatesByWorkflow[workflowUuid].length > 0) {
                this.voidPatientState(this.sortedStatesByWorkflow[workflowUuid][0].uuid);
            }
        }
    }

    createPatientState(state) {
        this.openmrsRest.update('programenrollment/' + this.patientProgram.uuid, {
            states: [
                {
                    state: state.state,
                    startDate: state.date    // TODO is it okay that we set this date on the client-side? need to sync with
                }
            ]
        }).then((response) => {
            // TODO: handle error cases
            if (this.statesByUuid[state.state].terminal) {
                this.reloadPage()
            }
            else {
                this.fetchPatientProgram(this.patientProgram.uuid); // refresh display
            }
        })
    }

    getWorkflowForState(state) {
        let result;
        angular.forEach(this.program.allWorkflows, (workflow) => {
            angular.forEach(workflow.states, (workflowState) => {
                if (state.uuid == workflowState.uuid) {
                    result = workflow;
                }
            })
        })
        return result;
    }

    voidPatientState(patientStateUuid) {

        return this.openmrsRest.remove('programenrollment/' + this.patientProgram.uuid + "/state/" + patientStateUuid, {
                voided: "true",
                voidReason: "voided via UI"
            })
            .then((response) => {
                // TODO: handle error cases
                this.fetchPatientProgram(this.patientProgram.uuid); // refresh display
            });

    }

    updatePatientState(workflowUuid, stateUuid) {
        this.edit.workflow[workflowUuid] = false;
        this.createPatientState(this.input.changeToStateByWorkflow[workflowUuid])
    }

    isNotCurrentState(workflow) {
        return (state) => {
            var currentState = (workflow.uuid in this.sortedStatesByWorkflow) ? this.sortedStatesByWorkflow[workflow.uuid][0] : null;
            return !currentState || currentState.state.uuid != state.uuid;
        }

    }

    isInitialState() {
        return (state) => { return state.initial };
    }

    groupAndSortPatientStates() {

        this.sortedStatesByWorkflow = {};

        if (this.patientProgram && this.patientProgram.states) {
            // TODO remove this first filter once the bug with the REST request returning voided elements is fixed
            this.patientProgram.states = this.$filter('filter')(this.patientProgram.states, (state) => {
                return !state.voided
            }, true);
            this.patientProgram.states = this.$filter('orderBy')(this.patientProgram.states, (state) => {
                return state.startDate
            });

            angular.forEach(this.patientProgram.states, (patientState) => {
                let workflow = this.getWorkflowForState(patientState.state);

                if (!(workflow.uuid in this.sortedStatesByWorkflow)) {
                    this.sortedStatesByWorkflow[workflow.uuid] = [];
                }

                var newEntry = {
                    startDate:  new Date(patientState.startDate),
                    dayAfterStartDate: this.getNextDay(patientState.startDate),
                    endDate: patientState.endDate ? new Date(patientState.endDate) : null,
                    state: patientState.state,
                    uuid: patientState.uuid
                }

                this.sortedStatesByWorkflow[workflow.uuid].unshift(newEntry);  // add to front
            })
        }
    }

    getMostRecentStateChangeDate() {
        let mostRecentStateChangeDate = null

        angular.forEach(this.sortedStatesByWorkflow, (workflow) => {
            if (workflow.length > 0) {
                if (!mostRecentStateChangeDate || workflow[0].startDate > mostRecentStateChangeDate){
                    mostRecentStateChangeDate = workflow[0].startDate
                }
            }
        })

        return mostRecentStateChangeDate;
    }

    update() {
        this.cancelAllEditModes();
        this.updatePatientProgram();
    }

    cancelAllEditModes() {
        this.edit.enrollment = false;
        for (let uuid in this.edit.workflow) {
            this.edit.workflow[uuid] = false
        }
    }

    cancelEdit() {
        this.cancelAllEditModes();
        this.setInputsToStartingValues();
    }

    cancelEnrollment() {
        this.expanded.enrollment = false;
        this.setInputsToStartingValues();
    }

    cancelDelete() {
        this.confirmDelete = {
            enrollment: false,
            workflow:{}
        }
    }

    inConfirmDelete() {
        if (this.confirmDelete.enrollment === true) {
            return true;
        }
        var result = false;
        angular.forEach(this.confirmDelete.workflow, function (value, key) {
            if (value === true) {
                result = true;
            }
        })
        return result;
    }

    inEditMode() {
        if (this.edit.enrollment === true) {
            return true;
        }
        var result = false;
        angular.forEach(this.edit.workflow, function (value, key) {
            if (value === true) {
                result = true;
            }
        })
        return result;
    }

    getNextDay(date) {
        return moment(date).add(1, 'days').toDate();
    }

    getPreviousDay(date) {
        return moment(date).add(-1, 'days').toDate();
    }

    isToday(date) {
        if (!date) {
            return false;
        }
        else {
           return moment(date).isSame(moment(), 'day');
        }
    }

    isMostRecentProgram() {
        return !this.patientPrograms || this.patientPrograms.length == 0 || !this.patientProgram || this.patientPrograms[0].uuid === this.patientProgram.uuid;
    }

    // if no program uuid is passed in, this widget will display the active program, or an "enroll in program" option if no active program
    displayActiveProgram() {
        return this.config.patientProgram ? false : true;
    }

    programIsCompleted() {
        return (this.patientProgram && this.patientProgram.dateCompleted ? true : false);
    }

    enrollmentValid() {
        return this.input.enrollmentLocation && this.input.dateEnrolled &&  // must have enrollmentLocation and date enrolled
            (!this.input.dateCompleted || this.input.dateCompleted >= this.input.dateEnrolled) &&  // date completed must be after date enrolled
            ((!this.input.dateCompleted && !this.input.outcome) || (this.input.dateCompleted && this.input.outcome)) &&  // if there's a completion date, must specific an outcome (and vice versa)
            (this.isMostRecentProgram() || this.input.dateCompleted);  // must be the most recent program or have a date completed
    }

    workflowTransitionValid(workflowUuid) {
        return this.input.changeToStateByWorkflow[workflowUuid] && this.input.changeToStateByWorkflow[workflowUuid].date && this.input.changeToStateByWorkflow[workflowUuid].state;
    }

    resetWindowStates() {
        // controls the state (open/closed) of the elements to edit enrollment & state information
        this.edit = {
            enrollment: false,
            workflow: {}
        }

        // controls the state (opened/closed) of the expanded view of enrollment & workflows
        this.expanded = {
            enrollment: false,
            workflow: {}
        }

        // controls whether the "confirm delete" message is displayed
        this.confirmDelete = {
            enrollment: false,
            workflow: {}
        };
    }

    reloadPage() {
        this.$window.location.reload();
    }
}