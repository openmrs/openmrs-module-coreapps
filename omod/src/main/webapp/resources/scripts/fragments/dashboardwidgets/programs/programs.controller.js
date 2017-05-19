// NOTE: work-in-progress, consider this not yet an "official" release of this widgets, future changes may not be bacwards-compatible and change functionality signficantly


//Determine current script path
var scripts = document.getElementsByTagName("script");
var programsPath = scripts[scripts.length - 1].src;

function ProgramsController(openmrsRest, $scope, $filter) {

    var ctrl = this;

    // the default patient page is the clinician dashboard
    ctrl.patientPage = "/coreapps/clinicianfacing/patient.page?patientId={{patientUuid}}&dashboard={{dashboard}}";

    ctrl.programs = [];

    ctrl.unenrolledPrograms = [];

    ctrl.patientPrograms= [];

    ctrl.showAddProgram = false;

    ctrl.input = {
        program: ""
    }

    // TODO did this work?
    ctrl.dateFormat = (ctrl.config.dateFormat == '' || angular.isUndefined(ctrl.config.dateFormat))
        ? 'yyyy-MM-dd' : ctrl.config.dateFormat;

    activate();

    function activate() {
        openmrsRest.setBaseAppPath("/coreapps");

        if( ctrl.config.patientPage ) {
            ctrl.patientPage = ctrl.config.patientPage;
        }

        fetchPrograms()
            .then(fetchPatientPrograms)
            .then(determineUnenrolledPrograms)
    }

    function fetchPrograms() {
        return openmrsRest.get('program', {
            v: 'custom:display,uuid'
        }).then(function(response) {
            getPrograms(response.results)
        })
    }

    function fetchPatientPrograms() {
        return openmrsRest.get('programenrollment', {
            patient: ctrl.config.patientUuid,
            voided: false,
            limit: getMaxRecords(),
            v: 'custom:program:(uuid,display),dateEnrolled,dateCompleted'
        }).then(function (response) {
            getPatientPrograms(response.results);
        })
    }

    function getPrograms(programs) {
        angular.forEach(programs, function(program) {
            ctrl.programs.push(program)
        })
    }

    function getPatientPrograms(patientPrograms) {
        ctrl.patientPrograms = [];
        angular.forEach(patientPrograms, function(patentProgram) {
            ctrl.patientPrograms.push(patentProgram)
        })
    }

    function getMaxRecords() {
        if (ctrl.config.maxRecords == '' || angular.isUndefined(ctrl.config.maxRecords)) {
            return 10;
        } else {
            return ctrl.config.maxRecords;
        }
    }

    function determineUnenrolledPrograms() {

        var activeProgramsUuids = [];
        angular.forEach(ctrl.patientPrograms, function(patientProgram) {
            if (!patientProgram.dateCompleted) {
                activeProgramsUuids.push(patientProgram.program.uuid)
            }
        })

        ctrl.unenrolledPrograms = $filter('filter')(ctrl.programs, function(program) {
            return activeProgramsUuids.indexOf(program.uuid) == -1
        })

    }

    ctrl.addProgram = function() {
        ctrl.showAddProgram = true
    }

    ctrl.cancelAddProgram = function() {
        ctrl.showAddProgram = false
        ctrl.input.program = ""
    }

    ctrl.gotoProgramDashboard = function() {
        if (ctrl.input.program && ctrl.config.enableProgramDashboards) {
            var destinationPage = "";
            destinationPage = Handlebars.compile(ctrl.patientPage)({
                patientUuid: ctrl.config.patientUuid,
                dashboard: ctrl.input.program
            });
            openmrsRest.getServerUrl().then(function (url) {
                window.location.href = url + destinationPage;
            });
        }
    }

    $scope.getTemplate = function () {
        return programsPath.replace(".controller.js", ".html");
    };

}