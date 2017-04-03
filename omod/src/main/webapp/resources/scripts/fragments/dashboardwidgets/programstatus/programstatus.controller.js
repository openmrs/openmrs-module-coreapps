//Determine current script path
var scripts = document.getElementsByTagName("script");
var programStatusPath = scripts[scripts.length - 1].src;

function ProgramStatusController(openmrsRest, $scope, $filter) {

    // TODO change widget name to program overview?

    // TODO change the history to be row-per-day
    // TODO only allow deletion of most recent
    // TODO make the state change need to take a date

    // TODO state
    // TODO commit a branch?
    // TODO localization
    // TODO location (on enrollment?)
    // TODO date (on enrollment)
    // TODO voided patient states and regular states bug
    // TODO handle completion + outcome?

    var vPatientProgram = 'custom:uuid,dateEnrolled,location:(display),dateCompleted,outcome,states:(uuid,startDate,endDate,voided,state:(uuid,concept:(display)))';

    var ctrl = this;

    ctrl.editMode = false;

    ctrl.program = null;

    ctrl.patientProgram = null;

    ctrl.statesByWorkflow = {};

    ctrl.statesByUuid = {};

    ctrl.stateUuidForCurrentPatientStateByWorkflow = {};

    ctrl.patientStateHistory = [];

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

    function voidPatientState(patientStateUuid) {
        openmrsRest.remove('programenrollment/' + ctrl.patientProgram.uuid + "/state/" + patientStateUuid, {
            voided: "true",
            voidReason: "voided via UI"
        }).then(function(response) {
            // TODO: handle error cases--what if the widget rejects the change?
            fetchPatientProgram(); // refresh display
        })
    }

    function groupAndSortPatientStates() {
        ctrl.patientStateHistory = [];
        ctrl.stateUuidForCurrentPatientStateByWorkflow = {};

        // TODO remove this first filter once the bug with the REST request returning voided elements is fixed
        ctrl.patientProgram.states = $filter('filter')(ctrl.patientProgram.states, function(state) { return !state.voided }, true);
        ctrl.patientProgram.states = $filter('orderBy')(ctrl.patientProgram.states, function(state) { return state.startDate }, true);

        angular.forEach(ctrl.patientProgram.states, function(patientState) {
            var workflow = getWorkflowForState(patientState.state);
            if (!(workflow.uuid in ctrl.stateUuidForCurrentPatientStateByWorkflow)) {
                ctrl.stateUuidForCurrentPatientStateByWorkflow[workflow.uuid] = patientState.state.uuid;
            }

            patientState['workflow'] = workflow;
            ctrl.patientStateHistory.push(patientState);
        })

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

    ctrl.updatePatientState = function(workflowUuid) {
        createPatientState(ctrl.stateUuidForCurrentPatientStateByWorkflow[workflowUuid])
    }

    ctrl.deletePatientState = function(patientStateUuid) {
        voidPatientState(patientStateUuid)
    }

    ctrl.enterEditMode = function() {
        ctrl.editMode = true;
    }

    ctrl.exitEditMode = function() {
        ctrl.editMode = false;
    }

    $scope.getTemplate = function () {
        return programStatusPath.replace(".controller.js", ".html");
    };



}