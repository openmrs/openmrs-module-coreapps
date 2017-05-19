// NOTE: work-in-progress, consider this not yet an "official" release of this widgets, future changes may not be backwards-compatible and change functionality signficantly


//Determine current script path
var scripts = document.getElementsByTagName("script");
var programStatusPath = scripts[scripts.length - 1].src;

function ProgramStatusController(openmrsRest, $scope, $filter, $q) {

    // TODO fix giant date widget--figure out how to bundle in bootstrap.css?
    // TODO if the most recent state is today, change widget has no date, just allows you to change it, otherwise transition + date (need moment)
    // TODO need way to fully delete program enrollment

    // TODO localization of text
    // TODO validation elements--can't change state without selecting date, etc
    // TODO handle completion + outcome? when there is outcome, then you can't change the states? deleting completion and outcome together
    // TODO ability to "void" enrollment

    // TODO unit tests? clean up?

    // TODO fix voided issue in REST module

    var vPatientProgram = 'custom:uuid,program:(uuid),dateEnrolled,dateCompleted,outcome:(display),location:(display,uuid),dateCompleted,outcome,states:(uuid,startDate,endDate,voided,state:(uuid,concept:(display)))';

    var ctrl = this;

    ctrl.dateFormat = (ctrl.config.dateFormat == '' || angular.isUndefined(ctrl.config.dateFormat))
        ? 'dd-MMM-yyyy' : ctrl.config.dateFormat;

    ctrl.program = null;

    ctrl.patientProgram = null;

    ctrl.programLocations = null;

    ctrl.programOutcomes = null;

    ctrl.statesByWorkflow = {};

    ctrl.statesByUuid = {};

    ctrl.patientStateHistory = [];

    // backs the various input fields
    ctrl.input = {
        dateEnrolled: null,
        enrollmentLocation: null,
        dateCompleted: null,
        outcome: null,
        changeToStateByWorkflow: {}
    }

    // controls the state (open/closed) of the elements to edit enrollment & state information
    ctrl.edit = {
        enrollment: false,
     //   completion: false,
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
        completion: {
            "opened": false,
            "options": {
                "maxDate": new Date()
            }
        },
        workflow: {}
    }

    ctrl.history = {
        expanded: false
    }

    ctrl.expanded = false;

    activate();

    function activate() {
        openmrsRest.setBaseAppPath("/coreapps");
        fetchLocations();
        fetchProgram().then(function (response) {
            fetchOutcomes();
            fetchPatientProgram(ctrl.config.patientProgram);
        })

    }

    function fetchProgram() {
        return openmrsRest.get('program', {
            uuid: ctrl.config.program,
            v: 'custom:display,uuid,outcomesConcept:(uuid),allWorkflows:(uuid,concept:(display),states:(uuid,concept:(display))'
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


    function fetchPatientProgram(patientProgramUuid) {
        if (!patientProgramUuid) {
            // we haven't been been given a patient program uuid, so load the programs and pick the active program (if it exists)
            return openmrsRest.get('programenrollment', {
                patient: ctrl.config.patientUuid,
                v: vPatientProgram
            }).then(function (response) {
                getActiveProgram(response.results);
                groupAndSortPatientStates();
                configDatePopups();
            })
        }
        else {
            // we've been given a specific patient program uuid, load that one
            // TODO this still needs to be tested
            return openmrsRest.get('programenrollment', {
                uuid: patientProgramUuid,
                v: vPatientProgram
            }).then(function (response) {
                ctrl.patientProgram = response;
                groupAndSortPatientStates();
                configDatePopups();
            })
        }
    }

    function fetchLocations() {
        openmrsRest.get('location', {
            v: 'custom:display,uuid',
            tag: ctrl.config.locationTag,
        }).then(function(response) {
            ctrl.programLocations = response.results;
        })
    }


    function fetchOutcomes() {
        if (ctrl.program.outcomesConcept) {
            openmrsRest.get('concept', {
                v: 'custom:answers:(display,uuid)',
                uuid: ctrl.program.outcomesConcept.uuid,
            }).then(function (response) {
                ctrl.programOutcomes = response.answers;
            })
        }
    }


    function getActiveProgram(patientPrograms) {

        // only patient programs of the specified type
        patientPrograms = $filter('filter') (patientPrograms, function (patientProgram) {
            return (patientProgram.program.uuid == ctrl.config.program
                && patientProgram.dateCompleted == null);
        })

        if (patientPrograms.length > 0) {

            patientPrograms = $filter('orderBy')(patientPrograms, function (patientProgram) {
                return -patientPrograms.startDate
            });

            ctrl.patientProgram = patientPrograms[0];
            setInputsToStartingValues();
        }
    }

    function setInputsToStartingValues() {
        ctrl.input.dateEnrolled = new Date(ctrl.patientProgram.dateEnrolled);
        ctrl.input.dateCompleted = ctrl.patientProgram.dateCompleted ? new Date(ctrl.patientProgram.dateCompleted) : null;
        ctrl.input.enrollmentLocation = ctrl.patientProgram.location ? ctrl.patientProgram.location.uuid : null;
        ctrl.input.outcome = ctrl.patientProgram.outcome ? ctrl.patientProgram.outcome.uuid : null;
    }

    function enrollInProgram() {
        if (ctrl.input.dateEnrolled && ctrl.input.enrollmentLocation) {
            openmrsRest.create('programenrollment', {
                patient: ctrl.config.patientUuid,
                program: ctrl.config.program,
                dateEnrolled: ctrl.input.dateEnrolled,
                location: ctrl.input.enrollmentLocation
            }).then(function (response) {
                fetchPatientProgram(); // refresh display
            })
        }
    }


    function updatePatientProgram() {
        openmrsRest.create('programenrollment/' + ctrl.patientProgram.uuid, {
            dateEnrolled: ctrl.input.dateEnrolled,
            dateCompleted: ctrl.input.dateCompleted,
            location: ctrl.input.enrollmentLocation,
            outcome: ctrl.input.outcome
        }).then(function(response) {
            fetchPatientProgram(ctrl.patientProgram.uuid); // refresh display
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
            fetchPatientProgram(ctrl.patientProgram.uuid); // refresh display
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
            fetchPatientProgram(ctrl.patientProgram.uuid); // refresh display
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
    }

    function configDatePopups() {

        // date enrolled can never be before the first state change
        if (ctrl.patientStateHistory.length > 0) {
            ctrl.datePopup.enrollment.options.maxDate = new Date(ctrl.patientStateHistory[ctrl.patientStateHistory.length - 1].startDate);
        }

     /*   ctrl.datePopup.completion.options.minDate = ctrl.patientStateHistory.length > 0 ? ctrl.patientStateHistory[0].startDate
            : ctrl.patientProgram ? ctrl.patientProgram.dateEnrolled : new Date()*/

        // date completed can never be before date enrollment
        if (ctrl.patientProgram) {
            ctrl.datePopup.completion.options.minDate = ctrl.patientProgram.dateEnrolled;
        }

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

    function cancelAllEditModes() {
        ctrl.edit.enrollment = false;
        for (uuid in ctrl.edit.workflow) {
            ctrl.edit.workflow[uuid] = false
        }
    }

    ctrl.enroll = function() {
       enrollInProgram();
    }

    ctrl.updatePatientProgram = function () {
        cancelAllEditModes();
        updatePatientProgram();
        setInputsToStartingValues();
    }

    ctrl.cancelEdit = function() {
        cancelAllEditModes();
        setInputsToStartingValues();
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

    ctrl.toggleEdit.enrollment = function() {
        var currentStatus = ctrl.edit.enrollment;
        cancelAllEditModes()
        ctrl.edit.enrollment = !currentStatus;
    }

  /*  ctrl.toggleEdit.completion = function() {
        var currentStatus = ctrl.edit.completion;
        cancelAllEditModes();
        ctrl.edit.completion = !currentStatus;
    }*/

    ctrl.toggleEdit.workflow = function(workflowUuid) {
        var currentStatus = ctrl.edit.workflow[workflowUuid];
        cancelAllEditModes();

        // the first time we hit this, we need to initialize the workflw
        if (!workflowUuid in ctrl.edit.workflow) {
            ctrl.edit.workflow[workflowUuid] = true
        }
        else {
            ctrl.edit.workflow[workflowUuid] = !currentStatus;
        }
    }

    // functions that open the datepicker widgets for various input elements
    ctrl.toggleDatePopup = {}

    ctrl.toggleDatePopup.enrollment = function() {
        ctrl.datePopup.enrollment.opened = !ctrl.datePopup.enrollment.opened;
    }

    ctrl.toggleDatePopup.completion = function() {
        ctrl.datePopup.completion.opened = !ctrl.datePopup.completion.enrollment;
    }

    ctrl.toggleDatePopup.workflow = function(workflowUuid) {
        ctrl.datePopup.workflow[workflowUuid].opened = !ctrl.datePopup.workflow[workflowUuid].opened;
    }

    ctrl.toggleHistory =  function() {
        ctrl.history.expanded = !ctrl.history.expanded;
    }

    ctrl.toggleExpanded = function() {

        if (!ctrl.expanded) {
            angular.element(document.body).prepend('<div id="overlay"></div>');
            ctrl.expanded = true;
            ctrl.history.expanded = true;
        }
        else if (ctrl.expanded) {
            cancelAllEditModes();
            ctrl.expanded = false;
            ctrl.history.expanded = false;
            angular.element('#overlay').remove();
        }
    }

    $scope.getTemplate = function () {
        return programStatusPath.replace(".controller.js", ".html");
    };



}