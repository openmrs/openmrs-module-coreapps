//Determine current script path
var scripts = document.getElementsByTagName("script");
var programStatusPath = scripts[scripts.length - 1].src;

function ProgramStatusController(openmrsRest, $scope, $filter, $q) {

    // TODO change widget name to program overview?

    // TODO make the state change need to take a date

    // TODO if the most recent state is today, change widget has no date, just allows you to change it, otherwise transition + date

    // TODO localization of states works?
    // TODO location (on enrollment?)
    // TODO date (on enrollment)
    // TODO handle completion + outcome?

    var vPatientProgram = 'custom:uuid,dateEnrolled,location:(display),dateCompleted,outcome,states:(uuid,startDate,endDate,voided,state:(uuid,concept:(display)))';

    var ctrl = this;

    ctrl.program = null;

    ctrl.patientProgram = null;

    ctrl.statesByWorkflow = {};

    ctrl.statesByUuid = {};

    ctrl.patientStateHistory = [];

    ctrl.changeToStateByWorkflow = {};

    ctrl.editModeByWorkflow = {};

    ctrl.editableByWorkflow = {};

    ctrl.dateFormat = (ctrl.config.dateFormat == '' || angular.isUndefined(ctrl.config.dateFormat))
        ? 'yyyy-MM-dd' : ctrl.config.dateFormat;

    activate();

    function activate() {
        openmrsRest.setBaseAppPath("/coreapps");
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
                ctrl.editModeByWorkflow[workflow.uuid] = false;
                ctrl.editableByWorkflow[workflow.uuid] = true;
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
            })
        }
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
        }
    }

    function enrollInProgram() {
        openmrsRest.create('programenrollment', {
            patient: ctrl.config.patientUuid,
            program: ctrl.config.program,
            dateEnrolled: new Date()
        }).then(function(response) {
            fetchPatientProgram(); // refresh display
        })
    }

    function createPatientState(stateUuid) {
        // TODO don't allow switching to a state that already exists?

        openmrsRest.update('programenrollment/' + ctrl.patientProgram.uuid, {
            states: [
                {
                    state: stateUuid,
                    startDate: new Date()    // TODO is it okay that we set this date on the client-side? need to sync with
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
        }

        return; // TODO remove

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

    ctrl.updatePatientState = function(workflowUuid, stateUuid) {
        ctrl.editModeByWorkflow[workflowUuid] = false;
        createPatientState(ctrl.changeToStateByWorkflow[workflowUuid])
    }

    ctrl.enterEditMode = function(workflowUuid) {
        // exit edit mode for any other workflows
        for (uuid in ctrl.editModeByWorkflow) {
            ctrl.editModeByWorkflow[uuid] = false
        }

        ctrl.editModeByWorkflow[workflowUuid] = true;
    }

    ctrl.exitEditMode = function(workflowUuid) {
        ctrl.editModeByWorkflow[workflowUuid] = false;
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
            return ctrl.patientStateHistory.length == 0
                || !ctrl.patientStateHistory[0].patientStatesByWorkflow.hasOwnProperty(workflow.uuid)
                ||  ctrl.patientStateHistory[0].patientStatesByWorkflow[workflow.uuid].state.uuid != state.uuid;
        }

    }

    $scope.getTemplate = function () {
        return programStatusPath.replace(".controller.js", ".html");
    };



}