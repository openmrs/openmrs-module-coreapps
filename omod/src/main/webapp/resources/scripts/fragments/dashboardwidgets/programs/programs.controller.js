//Determine current script path
var scripts = document.getElementsByTagName("script");
var programsPath = scripts[scripts.length - 1].src;

function ProgramsController(openmrsRest, $scope) {

    // TODO don't allow multiple enrollments in same program

    var ctrl = this;

    ctrl.programs = [];

    ctrl.patientPrograms= [];

    ctrl.dateFormat = (ctrl.config.dateFormat == '' || angular.isUndefined(ctrl.config.dateFormat))
        ? 'yyyy-MM-dd' : ctrl.config.dateFormat;

    activate();

    function activate() {
        openmrsRest.setBaseAppPath("/coreapps");
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
            v: 'custom:program:(display),dateEnrolled'
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
        if(ctrl.config.maxRecords == '' || angular.isUndefined(ctrl.config.maxRecords)){
            return 10;
        } else {
            return ctrl.config.maxRecords;
        }
    }

    $scope.getTemplate = function () {
        return programsPath.replace(".controller.js", ".html");
    };



}