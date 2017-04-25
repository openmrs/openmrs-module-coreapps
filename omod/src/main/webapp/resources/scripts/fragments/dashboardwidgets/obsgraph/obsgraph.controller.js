//Determine current script path
var scripts = document.getElementsByTagName("script");
var obsgraphPath = scripts[scripts.length - 1].src;

function ObsGraphController($scope, openmrsRest, widgetCommons) {
    $scope.getTemplate = function () {
        return obsgraphPath.replace(".controller.js", ".html");
    };

    var ctrl = this;

    ctrl.widgetCommons = widgetCommons;

    // Max age of obs to display
    ctrl.maxAgeInDays = undefined;

    // Concept info
    ctrl.concept = {};

    // Chart data
    ctrl.series = [];
    ctrl.labels = [];
    ctrl.data = [[]];

    // Init method
    ctrl.initialize = function () {
        openmrsRest.setBaseAppPath("/coreapps");
        // Set default maxResults if not defined
        if (angular.isUndefined(ctrl.config.maxResults)) {
            ctrl.config.maxResults = 4;
        }
        // Parse maxAge to day count
        ctrl.maxAgeInDays = ctrl.widgetCommons.maxAgeToDays(ctrl.config.maxAge);
        openmrsRest.list('obs',{patient: ctrl.config.patientUuid, v: 'full', limit: ctrl.config.maxResults, concept: ctrl.config.conceptId}).then(function (resp) {
            const obss = resp.results;
            if (obss.length > 0) {
                // Set concept to display
                ctrl.concept = obss[0].concept;
                ctrl.series.push(ctrl.concept.display);
                for (var i = 0; i < obss.length; i++) {
                    var obs = obss[i];
                    // Show numeric concepts only
                    if (obs.concept.datatype.display == 'Numeric') {
                        // Don't add obs older than maxAge
                        if (angular.isUndefined(ctrl.maxAgeInDays) || ctrl.widgetCommons.dateToDaysAgo(obs.obsDatetime) <= ctrl.maxAgeInDays) {
                            // Add obs data for chart display
                            var date = new Date(obs.obsDatetime);
                            ctrl.labels.unshift(date.getDate() + '/' + date.getMonth() + '/' + date.getFullYear());
                            ctrl.data[0].unshift(obs.value);
                        }
                    }
                }
            }
        })

    };
    ctrl.initialize();
}