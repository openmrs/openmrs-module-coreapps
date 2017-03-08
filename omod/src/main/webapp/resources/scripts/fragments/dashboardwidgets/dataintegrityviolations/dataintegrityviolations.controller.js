//Determine current script path
var scripts = document.getElementsByTagName("script");
var dataintegrityviolationstPath = scripts[scripts.length - 1].src;

function DataIntegrityViolationsController($scope, openmrsRest) {
    $scope.getTemplate = function () {
        return dataintegrityviolationstPath.replace(".controller.js", ".html");
    };

    var ctrl = this;

    ctrl.dataViolations = [];

    ctrl.initialize = function () {
        // Set default maxResults if not defined
        if (angular.isUndefined(ctrl.config.maxResults)) {
            ctrl.config.maxResults = 6;
        }
        openmrsRest.list('dataintegrity/integrityresults',{patient: ctrl.config.patientUuid, v: 'full', limit: ctrl.config.maxResults}).then(function (resp) {
            for (let dataViolation of resp.results) {
                ctrl.dataViolations.push(dataViolation);
            }
        })
    };
    ctrl.initialize()

}