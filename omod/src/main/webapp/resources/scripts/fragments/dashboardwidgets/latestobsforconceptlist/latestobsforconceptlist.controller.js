//Determine current script path
var scripts = document.getElementsByTagName("script");
var latestobsforconceptlistPath = scripts[scripts.length - 1].src;

function LatestObsForConceptListController($scope, openmrsRest, widgetCommons) {
    $scope.getTemplate = function () {
        return latestobsforconceptlistPath.replace(".controller.js", ".html");
    };

    var ctrl = this;

    ctrl.widgetCommons = widgetCommons;

    ctrl.maxConceptCount = 10;
    ctrl.maxAgeInDays = 'undefined';
    ctrl.obs = [];

    // Init method
    ctrl.initialize = function () {
        if (ctrl.config.maxAge !== 'undefined') {
            ctrl.maxAgeInDays = ctrl.widgetCommons.maxAgeToDays(ctrl.config.maxAge);
        }
        // Remove whitespaces
        ctrl.config.concepts = ctrl.config.concepts.replace(/\s/g,'');
        const concepts = ctrl.config.concepts.split(",");
        for (let concept of concepts) {
            // Fetch last obs for concept
            openmrsRest.list('obs',{patient: ctrl.config.patientUuid, v: 'full', concept: concept}).then(function (resp) {
                // Don't add more items if max concept count is reached
                if (ctrl.obs.length < ctrl.maxConceptCount && resp.results.length > 0) {
                    const obs = resp.results[0];
                    // Don't add obs older than maxAge
                    if (ctrl.maxAgeInDays === 'undefined' || ctrl.widgetCommons.dateToDaysAgo(obs.obsDatetime) <= ctrl.maxAgeInDays) {
                        // Add last obs for concept to list
                        ctrl.obs.push(obs);
                    }
                }
            })
        }
    };
    ctrl.initialize();

}