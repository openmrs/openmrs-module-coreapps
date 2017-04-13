//Determine current script path
var scripts = document.getElementsByTagName("script");
var programStatusPath = scripts[scripts.length - 1].src;

function ProgramStatusController(openmrsRest, $scope, $filter, $q) {

    // TODO change widget name to program overview?

    // TODO if the most recent state is today, change widget has no date, just allows you to change it, otherwise transition + date

    // TODO unit tests? clean up?
    // TODO localization of text
    // TODO layout
    // TODO validation elements--can't change state without selecting date, etc


    // TODO handle completion + outcome?
    // TODO fix voided issue in REST module

    var vPatientProgram = 'custom:uuid,dateEnrolled,location:(display,uuid),dateCompleted,outcome,states:(uuid,startDate,endDate,voided,state:(uuid,concept:(display)))';

    var ctrl = this;

    // TODO this should change to a new "program location" tag, and/or be configurable
    ctrl.locationTag = 'dea8febf-0bbe-4111-8152-a9cf7df622b6';

    ctrl.dateFormat = (ctrl.config.dateFormat == '' || angular.isUndefined(ctrl.config.dateFormat))
        ? 'dd-MMM-yyyy' : ctrl.config.dateFormat;

    ctrl.program = null;

    ctrl.patientProgram = null;

    ctrl.programLocations = null;

    ctrl.statesByWorkflow = {};

    ctrl.statesByUuid = {};

    ctrl.patientStateHistory = [];

    // backs the various input fields
    ctrl.input = {
        dateEnrolled: null,
        enrollmentLocation: null,
        changeToStateByWorkflow: {}
    }

    // controls the state (open/closed) of the elements to edit enrollment & state information
    ctrl.edit = {
        enrollment: false,
        workflow: {}
    }

    // controls the state and options of the various date popups
    ctrl.datePopup = {
        enrollment: {
            "opened": false,
            "options": {
                "maxDate": new Date()
            }
        },
        workflow: {}
    }

    activate();

    function activate() {
        openmrsRest.setBaseAppPath("/coreapps");
        fetchLocations();
        fetchProgram().then(function (response) {
            fetchPatientProgram();
        })

    }

    function fetchProgram() {
        return openmrsRest.get('program', {
            uuid: ctrl.config.program,
            v: 'custom:display,uuid,allWorkflows:(uuid,concept:(display),states:(uuid,concept:(display))'
        }).then(function(response) {
            // TODO handle error cases, program doesn't exist
            ctrl.program = response;

            angular.forEach(ctrl.program.allWorkflows, function(workflow) {
                ctrl.statesByWorkflow[workflow.uuid] = workflow.states;
                angular.forEach(workflow.states, function(state) {
                    ctrl.statesByUuid[state.uuid] = state;
                })
            })
        })
    }


    function fetchPatientProgram() {
        if (ctrl.config.patientProgram == '' || angular.isUndefined(ctrl.config.patientPrograms)) {
            // we haven't been been given a patient program uuid, so load the programs and pick the active program (if it exists)
            return openmrsRest.get('programenrollment', {
                program: ctrl.config.program,
                patient: ctrl.config.patientUuid,
                v: vPatientProgram
            }).then(function (response) {
                getActiveProgram(response.results);
                groupAndSortPatientStates();
                configWorkflowDatePopups();
            })
        }
        else {
            // we've been given a specific patient program uuid, load that one
            // TODO this still needs to be tested
            return openmrsRest.get('programenrollment', {
                uuid: ctrl.config.patientProgram,
                v: vPatientProgram
            }).then(function (response) {
                ctrl.patientProgram = response;
                groupAndSortPatientStates();
                configWorkflowDatePopups();
            })
        }
    }

    function fetchLocations() {
        return openmrsRest.get('location', {
            v: 'custom:display,uuid',
            tag: ctrl.locationTag,
        }).then(function(response) {
            ctrl.programLocations = response.results;
        })
    }

    function getActiveProgram(patientPrograms) {
        if (patientPrograms.length > 0) {
            patientPrograms = $filter('filter')(patientPrograms, function(program) {
                return !program.dateCompleted || angular.isUndefined(program.dateCompleted)
            })
        }
        // assumption: only one active program
        if (patientPrograms.length > 0) {
            ctrl.patientProgram = patientPrograms[0]
            ctrl.input.dateEnrolled = new Date(ctrl.patientProgram.dateEnrolled)
            ctrl.input.enrollmentLocation = ctrl.patientProgram.location ? ctrl.patientProgram.location.uuid : null
        }
    }

    function enrollInProgram() {
        openmrsRest.create('programenrollment', {
            patient: ctrl.config.patientUuid,
            program: ctrl.config.program,
            dateEnrolled: ctrl.input.dateEnrolled,
            location: ctrl.input.enrollmentLocation
        }).then(function(response) {
            fetchPatientProgram(); // refresh display
        })
    }


    function updatePatientProgram() {
        openmrsRest.create('programenrollment/' + ctrl.patientProgram.uuid, {
            dateEnrolled: ctrl.input.dateEnrolled,
            location: ctrl.input.enrollmentLocation
        }).then(function(response) {
            fetchPatientProgram(); // refresh display
        })
    }

    function createPatientState(state) {
        openmrsRest.update('programenrollment/' + ctrl.patientProgram.uuid, {
            states: [
                {
                    state: state.state,
                    startDate: state.date    // TODO is it okay that we set this date on the client-side? need to sync with
                }
            ]
        }).then(function(response) {
            // TODO: handle error cases--what if the widget rejects the change?
            fetchPatientProgram(); // refresh display
        })
    }

    function voidPatientStates(patientStateUuids) {

        var voidCalls = [];

        angular.forEach(patientStateUuids, function(patientStateUuid) {
            voidCalls.push(openmrsRest.remove('programenrollment/' + ctrl.patientProgram.uuid + "/state/" + patientStateUuid, {
                voided: "true",
                voidReason: "voided via UI"
            }));
        });

        $q.all(voidCalls).then(function(response) {
            // TODO: handle error cases--what if the widget rejects the change?
            fetchPatientProgram(); // refresh display
        })

    }

    // TODO: definitely document what this is actually doing if we keep it!
    function groupAndSortPatientStates() {
        ctrl.patientStateHistory = [];

        if (ctrl.patientProgram && ctrl.patientProgram.states) {
            // TODO remove this first filter once the bug with the REST request returning voided elements is fixed
            ctrl.patientProgram.states = $filter('filter')(ctrl.patientProgram.states, function (state) {
                return !state.voided
            }, true);
            ctrl.patientProgram.states = $filter('orderBy')(ctrl.patientProgram.states, function (state) {
                return state.startDate
            });

            angular.forEach(ctrl.patientProgram.states, function (patientState) {
                var workflow = getWorkflowForState(patientState.state);

                // TODO can't edit if today

                patientState['workflow'] = workflow;

                if (ctrl.patientStateHistory.length > 0 &&
                    ctrl.patientStateHistory[0].startDate == patientState.startDate) {
                    // assumption: only one state per workflow per day
                    ctrl.patientStateHistory[0]['patientStatesByWorkflow'][workflow.uuid] = patientState;
                }
                else {
                    var newEntry = {};
                    newEntry['startDate'] = patientState.startDate;
                    newEntry['patientStatesByWorkflow'] = {};
                    newEntry['patientStatesByWorkflow'][workflow.uuid] = patientState;
                    ctrl.patientStateHistory.unshift(newEntry);  // add to front
                }
            })

            // date enrolled can never be edited to be before the first state change

            if (ctrl.patientStateHistory.length > 0) {
                ctrl.datePopup.enrollment.options.maxDate = new Date(ctrl.patientStateHistory[ctrl.patientStateHistory.length - 1].startDate);
            }
        }
    }

    function configWorkflowDatePopups() {
        angular.forEach(ctrl.program.allWorkflows, function(workflow) {
            ctrl.datePopup.workflow[workflow.uuid] = {
                "opened": false,
                "options": {
                    "maxDate": new Date(),
                    // TODO minDate should be most recent date + 1!
                    "minDate": ctrl.getMostRecentStateForWorkflow(workflow.uuid) ? ctrl.getMostRecentStateForWorkflow(workflow.uuid).startDate
                        : ctrl.patientProgram ? ctrl.patientProgram.dateEnrolled : new Date()
                }
            }
        })
    }

    function getWorkflowForState(state) {
        var result;
        angular.forEach(ctrl.program.allWorkflows, function(workflow) {
            angular.forEach(workflow.states, function(workflowState) {
                if (state.uuid == workflowState.uuid) {
                    result = workflow;
                }
            })
        })
        return result;
    }

    ctrl.enroll = function() {
       enrollInProgram();
    }

    ctrl.updatePatientProgram = function () {
        ctrl.edit.enrollment = false;
        updatePatientProgram();
    }

    ctrl.updatePatientState = function(workflowUuid, stateUuid) {
        ctrl.edit.workflow[workflowUuid] = false;
        createPatientState(ctrl.input.changeToStateByWorkflow[workflowUuid])
    }

    ctrl.deleteMostRecentPatientStates = function() {
        if (ctrl.patientStateHistory.length > 0) {
            var stateUuids = [];
            for (var workflow in ctrl.patientStateHistory[0].patientStatesByWorkflow) {
                stateUuids.push(ctrl.patientStateHistory[0].patientStatesByWorkflow[workflow].uuid)
            }
            voidPatientStates(stateUuids);
        }
    }

    ctrl.hasHistory = function() {
        return ctrl.patientStateHistory.length > 0;
    }

    ctrl.isNotCurrentState = function(workflow) {
        return function(state) {
            var currentState = ctrl.getMostRecentStateForWorkflow(workflow.uuid);
            return !currentState || currentState.state.uuid != state.uuid;
        }

    }

    ctrl.getMostRecentStateForWorkflow = function(workflowUuid) {
        var result = null;
        angular.forEach(ctrl.patientStateHistory, function(history) {
            if (workflowUuid in history['patientStatesByWorkflow'] && result == null) {
                result = history['patientStatesByWorkflow'][workflowUuid]
            }
        })
        return result;
    }

    // functions that control showing/hiding elements for editing enrollment or states
    ctrl.toggleEdit = {}

    ctrl.toggleEdit.workflow = function(workflowUuid) {

        // make sure we edit edit mode for enrollment
        ctrl.edit.enrollment = false;

        // make sure we exit edit mode for any other workflows
        for (uuid in ctrl.edit.workflow) {
            if (uuid != workflowUuid) {
                ctrl.edit.workflow[uuid] = false
            }
        }

        // the first time we hit this, we need to initialize the workflw
        if (!workflowUuid in ctrl.edit.workflow) {
            ctrl.edit.workflow[workflowUuid] = true
        }
        else {
            ctrl.edit.workflow[workflowUuid] = !ctrl.edit.workflow[workflowUuid]
        }
    }

    ctrl.toggleEdit.enrollment = function() {

        // make sure we exit edit mode for any  workflows
        for (uuid in ctrl.edit.workflow) {
            ctrl.edit.workflow[uuid] = false
        }

        ctrl.edit.enrollment = !ctrl.edit.enrollment;
    }

    // functions that open the datepicker widgets for various input elements
    ctrl.toggleDatePopup = {}

    ctrl.toggleDatePopup.enrollment = function() {
        ctrl.datePopup.enrollment.opened = !ctrl.datePopup.enrollment.opened;
    }

    ctrl.toggleDatePopup.workflow = function(workflowUuid) {
        ctrl.datePopup.workflow[workflowUuid].opened = !ctrl.datePopup.workflow[workflowUuid].opened;
    }

    $scope.getTemplate = function () {
        return programStatusPath.replace(".controller.js", ".html");
    };



}