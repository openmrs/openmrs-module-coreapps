// NOTE: work-in-progress, consider this not yet an "official" release of this widgets, future changes may not be backwards-compatible and change functionality signficantly

import angular from 'angular';

export default class ProgramStatusController {

    // TODO fix giant date widget--figure out how to bundle in bootstrap.css?
    // TODO handle completion + outcome? when there is outcome, then you can't change the states? deleting completion and outcome together
    // TODO ability to delete enrollment--add are you sure?

    // TODO if the most recent state is today, change widget has no date, just allows you to change it, otherwise transition + date (need moment)
    // TODO localization of text
    // TODO validation elements--can't change state without selecting date, etc

    // TODO unit tests? clean up?

    constructor(openmrsRest, $filter, $q) {
        'ngInject';

        Object.assign(this, {$filter, openmrsRest, $q});
    }

    $onInit() {
        this.vPatientProgram = 'custom:uuid,program:(uuid),dateEnrolled,dateCompleted,outcome:(display),location:(display,uuid),dateCompleted,outcome,states:(uuid,startDate,endDate,voided,state:(uuid,concept:(display)))';

        this.dateFormat = (this.config.dateFormat == '' || angular.isUndefined(this.config.dateFormat))
            ? 'dd-MMM-yyyy' : this.config.dateFormat;

        this.program = null;

        this.patientProgram = null;

        this.programLocations = null;

        this.programOutcomes = null;

        this.statesByWorkflow = {};

        this.statesByUuid = {};

        this.patientStateHistory = [];

        // backs the various input fields
        this.input = {
            dateEnrolled: null,
            enrollmentLocation: null,
            dateCompleted: null,
            outcome: null,
            changeToStateByWorkflow: {}
        }

        // controls the state (open/closed) of the elements to edit enrollment & state information
        this.edit = {
            enrollment: false,
            //   completion: false,
            workflow: {}
        }

        // controls the state and options of the various date popups
        this.datePopup = {
            enrollment: {
                "opened": false,
                "options": {
                    "maxDate": new Date()
                }
            },
            completion: {
                "opened": false,
                "options": {
                    "maxDate": new Date()
                }
            },
            workflow: {}
        }

        this.history = {
            expanded: false
        }

        this.expanded = false;
        
        this.activate();

        let ctrl = this;

        //TODO: Migrate to new format-maybe just forget about this nested function idea?
        // functions that control showing/hiding elements for editing enrollment or states
        this.toggleEdit = {}

        this.toggleEdit.enrollment = function() {
            var currentStatus = ctrl.edit.enrollment;
            ctrl.cancelAllEditModes()
            ctrl.edit.enrollment = !currentStatus;
        }

        /*  this.toggleEdit.completion = function() {
         var currentStatus = this.edit.completion;
         cancelAllEditModes();
         this.edit.completion = !currentStatus;
         }*/

        this.toggleEdit.workflow = function(workflowUuid) {
            var currentStatus = ctrl.edit.workflow[workflowUuid];
            ctrl.cancelAllEditModes();

            // the first time we hit this, we need to initialize the workflw
            if (!workflowUuid in ctrl.edit.workflow) {
                ctrl.edit.workflow[workflowUuid] = true
            }
            else {
                ctrl.edit.workflow[workflowUuid] = !currentStatus;
            }
        }

        // functions that open the datepicker widgets for various input elements
        this.toggleDatePopup = {};

        this.toggleDatePopup.enrollment = function() {
            ctrl.datePopup.enrollment.opened = !ctrl.datePopup.enrollment.opened;
        }

        this.toggleDatePopup.completion = function() {
            ctrl.datePopup.completion.opened = !ctrl.datePopup.completion.enrollment;
        }

        this.toggleDatePopup.workflow = function(workflowUuid) {
            ctrl.datePopup.workflow[workflowUuid].opened = !ctrl.datePopup.workflow[workflowUuid].opened;
        }
    }
    
    activate() {
        this.openmrsRest.setBaseAppPath("/coreapps");
        this.fetchLocations();
        this.fetchProgram().then((response) => {
            this.fetchOutcomes();
            this.fetchPatientProgram(this.config.patientProgram);
        });

    }

    setInputsToStartingValues() {
        this.input.dateEnrolled = new Date(this.patientProgram.dateEnrolled);
        this.input.dateCompleted = this.patientProgram.dateCompleted ? new Date(this.patientProgram.dateCompleted) : null;
        this.input.enrollmentLocation = this.patientProgram.location ? this.patientProgram.location.uuid : null;
        this.input.outcome = this.patientProgram.outcome ? this.patientProgram.outcome.uuid : null;
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
                this.configDatePopups();
            });
        }
        else {
            // we've been given a specific patient program uuid, load that one
            // TODO this still needs to be tested
            return this.openmrsRest.get('programenrollment', {
                uuid: patientProgramUuid,
                v: this.vPatientProgram
            }).then((response) => {
                this.patientProgram = response;
                this.groupAndSortPatientStates();
                this.configDatePopups();
            })
        }
    }

    fetchLocations() {
        this.openmrsRest.get('location', {
            v: 'custom:display,uuid',
            tag: this.config.locationTag,
        }).then((response) => {
            this.programLocations = response.results;
        })
    }


    fetchOutcomes() {
        if (this.program.outcomesConcept) {
            this.openmrsRest.get('concept', {
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
            this.setInputsToStartingValues();
        }
    }

    enrollInProgram() {
        if (this.input.dateEnrolled && this.input.enrollmentLocation) {
            this.openmrsRest.create('programenrollment', {
                patient: this.config.patientUuid,
                program: this.config.program,
                dateEnrolled: this.input.dateEnrolled,
                location: this.input.enrollmentLocation
            }).then((response) => {
                this.fetchPatientProgram(); // refresh display
            })
        }
    }

    updatePatientProgram() {
        this.openmrsRest.update('programenrollment/' + this.patientProgram.uuid, {
            dateEnrolled: this.input.dateEnrolled,
            dateCompleted: this.input.dateCompleted,
            location: this.input.enrollmentLocation,
            outcome: this.input.outcome
        }).then((response) => {
            this.fetchPatientProgram(this.patientProgram.uuid); // refresh display
        })
    }

    deletePatientProgram() {
        this.openmrsRest.remove('programenrollment/', {
            uuid: this.patientProgram.uuid
        }).then((response) => {
            this.patientProgram = null;
            this.fetchPatientProgram(); // refresh display
        })
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
            // TODO: handle error cases--what if the widget rejects the change?
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

    voidPatientStates(patientStateUuids) {

        let voidCalls = [];

        angular.forEach(patientStateUuids, (patientStateUuid) => {
            voidCalls.push(this.openmrsRest.remove('programenrollment/' + this.patientProgram.uuid + "/state/" + patientStateUuid, {
                voided: "true",
                voidReason: "voided via UI"
            }));
        });

        this.$q.all(voidCalls).then((response) => {
            // TODO: handle error cases--what if the widget rejects the change?
            this.fetchPatientProgram(this.patientProgram.uuid); // refresh display
        });

    }

    updatePatientState(workflowUuid, stateUuid) {
        this.edit.workflow[workflowUuid] = false;
        this.createPatientState(this.input.changeToStateByWorkflow[workflowUuid])
    }

    deleteMostRecentPatientStates() {
        if (this.patientStateHistory.length > 0) {
            var stateUuids = [];
            for (var workflow in this.patientStateHistory[0].patientStatesByWorkflow) {
                stateUuids.push(this.patientStateHistory[0].patientStatesByWorkflow[workflow].uuid)
            }
            this.voidPatientStates(stateUuids);
        }
    }

    getMostRecentStateForWorkflow(workflowUuid) {
        var result = null;
        angular.forEach(this.patientStateHistory, (history) => {
            if (workflowUuid in history['patientStatesByWorkflow'] && result == null) {
                result = history['patientStatesByWorkflow'][workflowUuid]
            }
        });
        return result;
    }

    isNotCurrentState(workflow) {
        return (state) => {
            var currentState = this.getMostRecentStateForWorkflow(workflow.uuid);
            return !currentState || currentState.state.uuid != state.uuid;
        }

    }

    // TODO: definitely document what this is actually doing if we keep it!
    groupAndSortPatientStates() {
        this.patientStateHistory = [];

        if (this.patientProgram && this.patientProgram.states) {
            // TODO remove this first filter once the bug with the REST request returning voided elements is fixed
            this.patientProgram.states = this.$filter('filter')(this.patientProgram.states, (state) => {
                return !state.voided
            }, true);
            this.patientProgram.states = this.$filter('orderBy')(this.patientProgram.states, (state) => {
                return state.startDate
            });

            angular.forEach(this.patientProgram.states, (patientState) => {
                let workflow = getWorkflowForState(patientState.state);

                // TODO can't edit if today

                patientState['workflow'] = workflow;

                if (this.patientStateHistory.length > 0 &&
                    this.patientStateHistory[0].startDate == patientState.startDate) {
                    // assumption: only one state per workflow per day
                    this.patientStateHistory[0]['patientStatesByWorkflow'][workflow.uuid] = patientState;
                }
                else {
                    var newEntry = {};
                    newEntry['startDate'] = patientState.startDate;
                    newEntry['patientStatesByWorkflow'] = {};
                    newEntry['patientStatesByWorkflow'][workflow.uuid] = patientState;
                    this.patientStateHistory.unshift(newEntry);  // add to front
                }
            })
        }
    }

    configDatePopups() {

        // date enrolled can never be before the first state change
        if (this.patientStateHistory.length > 0) {
            this.datePopup.enrollment.options.maxDate = new Date(this.patientStateHistory[this.patientStateHistory.length - 1].startDate);
        }

     /*   this.datePopup.completion.options.minDate = this.patientStateHistory.length > 0 ? this.patientStateHistory[0].startDate
            : this.patientProgram ? this.patientProgram.dateEnrolled : new Date()*/

        // date completed can never be before date enrollment
        if (this.patientProgram) {
            this.datePopup.completion.options.minDate = this.patientProgram.dateEnrolled;
        }

        angular.forEach(this.program.allWorkflows, (workflow) => {
            this.datePopup.workflow[workflow.uuid] = {
                "opened": false,
                "options": {
                    "maxDate": new Date(),
                    // TODO minDate should be most recent date + 1!
                    "minDate": this.getMostRecentStateForWorkflow(workflow.uuid) ? this.getMostRecentStateForWorkflow(workflow.uuid).startDate
                        : this.patientProgram ? this.patientProgram.dateEnrolled : new Date()
                }
            }
        })
    }

    update() {
        this.cancelAllEditModes();
        this.updatePatientProgram();
        this.setInputsToStartingValues();
    }

    cancelAllEditModes() {
        this.edit.enrollment = false;
        for (uuid in this.edit.workflow) {
            this.edit.workflow[uuid] = false
        }
    }

    cancelEdit() {
        this.cancelAllEditModes();
        this.setInputsToStartingValues();
    }

    hasHistory() {
        return this.patientStateHistory.length > 0;
    }

    toggleHistory() {
        this.history.expanded = !this.history.expanded;
    }

    toggleExpanded() {

        if (!this.expanded) {
            angular.element(document.body).prepend('<div id="overlay"></div>');
            this.expanded = true;
            this.history.expanded = true;
        }
        else if (this.expanded) {
            this.cancelAllEditModes();
            this.expanded = false;
            this.history.expanded = false;
            angular.element('#overlay').remove();
        }
    }
}