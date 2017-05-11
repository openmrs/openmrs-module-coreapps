// NOTE: work-in-progress, consider this not yet an "official" release of this widgets, future changes may not be bacwards-compatible and change functionality signficantly


//Determine current script path
var scripts = document.getElementsByTagName("script");
var programsPath = scripts[scripts.length - 1].src;

function ProgramsController(openmrsRest, $scope) {

    var ctrl = this;

    // the default patient page is the clinician dashboard
    ctrl.patientPage = "/coreapps/clinicianfacing/patient.page?patientId={{patientUuid}}&dashboard={{dashboard}}";

    ctrl.programs = [];

    ctrl.patientPrograms= [];

    ctrl.dateFormat = (ctrl.config.dateFormat == '' || angular.isUndefined(ctrl.config.dateFormat))
        ? 'yyyy-MM-dd' : ctrl.config.dateFormat;

    activate();

    function activate() {
        openmrsRest.setBaseAppPath("/coreapps");

        if( ctrl.config.patientPage ) {
            ctrl.patientPage = ctrl.config.patientPage;
        }

        fetchPrograms();
        fetchPatientPrograms();
    }

    function fetchPrograms() {
        openmrsRest.get('program', {
            v: 'custom:display,uuid'
        }).then(function(response) {
            getPrograms(response.results)
        })
    }

    function fetchPatientPrograms() {
        openmrsRest.get('programenrollment', {
            patient: ctrl.config.patientUuid,
            voided: false,
            limit: getMaxRecords(),
            v: 'custom:program:(uuid,display),dateEnrolled'
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
        // TODO restrict to active programs?
        ctrl.patientPrograms= [];
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

    ctrl.gotoProgramDashboard = function(programUuid) {
        if (programUuid && ctrl.config.enableProgramDashboards) {
            var destinationPage = "";
            destinationPage = Handlebars.compile(ctrl.patientPage)({
                patientUuid: ctrl.config.patientUuid,
                dashboard: programUuid
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