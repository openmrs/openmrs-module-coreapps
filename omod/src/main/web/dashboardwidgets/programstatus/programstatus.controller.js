
import angular from 'angular';
import moment from 'moment';

export default class ProgramStatusController {

    // TODO add support for special logic around "initial" and "terminal?"

    constructor($filter, $q, openmrsRest, openmrsTranslate) {
        'ngInject';

        Object.assign(this, {$filter, $q, openmrsRest, openmrsTranslate});
    }

    $onInit() {
        this.vPatientProgram = 'custom:uuid,program:(uuid),dateEnrolled,dateCompleted,outcome:(display),location:(display,uuid),dateCompleted,outcome,states:(uuid,startDate,endDate,voided,state:(uuid,concept:(display)))';

        this.dateFormat = (this.config.dateFormat == '' || angular.isUndefined(this.config.dateFormat))
            ? 'dd-MMM-yyyy' : this.config.dateFormat;
        this.today = new Date();

        this.loaded = false;

        this.program = null;
        this.patientProgram = null;
        this.programLocations = null;
        this.programOutcomes = null;

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

        this.activate();

        let ctrl = this;

    }

    activate() {
        this.openmrsRest.setBaseAppPath("/coreapps");

        this.fetchPrivileges();

        this.fetchLocations().then((response) => {
            this.fetchProgram().then((response) => {
                this.fetchOutcomes();
                this.fetchPatientProgram(this.config.patientProgram).then((response) => {
                    this.loaded = true;
                })
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
            v: 'custom:display,uuid,outcomesConcept:(uuid),allWorkflows:(uuid,concept:(display),states:(uuid,concept:(display))'
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

    fetchPatientProgram(patientProgramUuid) {
        if (!patientProgramUuid) {
            // we haven't been been given a patient program uuid, so load the programs and pick the active program (if it exists)
            return this.openmrsRest.get('programenrollment', {
                patient: this.config.patientUuid,
                v: this.vPatientProgram
            }).then((response) => {
                this.getActiveProgram(response.results);
                this.groupAndSortPatientStates();
                this.setInputsToStartingValues();
                this.convertDateEnrolledAndDateCompletedStringsToDates();
            });
        }
        else {
            // we've been given a specific patient program uuid, load that one
            return this.openmrsRest.get('programenrollment', {
                uuid: patientProgramUuid,
                v: this.vPatientProgram
            }).then((response) => {
                this.patientProgram = response;
                this.groupAndSortPatientStates();
                this.setInputsToStartingValues();
                this.convertDateEnrolledAndDateCompletedStringsToDates();;
            })
        }
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

    getActiveProgram(patientPrograms) {

        // only patient programs of the specified type
        patientPrograms = this.$filter('filter') (patientPrograms, (patientProgram) => {
            return (patientProgram.program.uuid == this.config.program
                && patientProgram.dateCompleted == null);
        });

        if (patientPrograms.length > 0) {
            patientPrograms = this.$filter('orderBy')(patientPrograms, (patientProgram) => {
                return -patientPrograms.startDate;
            });

            this.patientProgram = patientPrograms[0];
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
                this.fetchPatientProgram(); // refresh display

            });

        }
    }

    updatePatientProgram() {
        // we need to make sure that the most recent state for each workflow has an end date = completion date
        // (this should really be handled by the api?)
        let states = [];
        angular.forEach(this.sortedStatesByWorkflow, (workflow) => {
            if (workflow.length > 0) {
                states.push({
                    uuid: workflow[0].uuid,
                    endDate: this.input.dateCompleted
                })
            }
        })

        this.openmrsRest.update('programenrollment/' + this.patientProgram.uuid, {
            dateEnrolled: this.input.dateEnrolled,
            dateCompleted: this.input.dateCompleted,
            location: this.input.enrollmentLocation,
            outcome: this.input.outcome,
            states: states
        }).then((response) => {
            this.fetchPatientProgram(this.patientProgram.uuid); // refresh display
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
                this.fetchPatientProgram(); // refresh display
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
            this.fetchPatientProgram(this.patientProgram.uuid); // refresh display
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

    isToday(date) {
        if (!date) {
            return false;
        }
        else {
           return moment(date).isSame(moment(), 'day');
        }
    }

    programIsCompleted() {
        return this.patientProgram && this.patientProgram.dateCompleted ? true : false;
    }

    enrollmentValid() {
        return this.input.enrollmentLocation && this.input.dateEnrolled &&  // must have enrollmentLocation and date enrolled
            (!this.input.dateCompleted || this.input.dateCompleted >= this.input.dateEnrolled) &&  // date completed must be after date enrolled
            ((!this.input.dateCompleted && !this.input.outcome) || (this.input.dateCompleted && this.input.outcome));  // if there's a completion date, must specific an outcome (and vice versa)
    }

    workflowTransitionValid(workflowUuid) {
        return this.input.changeToStateByWorkflow[workflowUuid] && this.input.changeToStateByWorkflow[workflowUuid].date && this.input.changeToStateByWorkflow[workflowUuid].state;
    }

}