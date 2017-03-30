//Determine current script path
var scripts = document.getElementsByTagName("script");
var programsPath = scripts[scripts.length - 1].src;

function ProgramsController(openmrsRest, $scope) {

    var ctrl = this;

    ctrl.patientPrograms= [];

    activate();

    function activate() {
        openmrsRest.setBaseAppPath("/coreapps");
        fetchPatientPrograms();
    }

    function fetchPatientPrograms() {
        openmrsRest.get('programenrollment', {
            patient: ctrl.config.patientUuid,
            limit: getMaxRecords(),
            v: 'custom:program:(display),dateEnrolled'
        }).then(function (response) {
            getPatientPrograms(response.results);
        })
    }

    function getPatientPrograms(patientPrograms) {
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

    $scope.dateFormat = (ctrl.config.dateFormat == '' || angular.isUndefined(ctrl.config.dateFormat))
                            ? 'yyyy-MM-dd' : ctrl.config.dateFormat;


    $scope.getTemplate = function () {
        return programsPath.replace(".controller.js", ".html");
    };

}