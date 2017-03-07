//Determine current script path
var scripts = document.getElementsByTagName("script");
var obsacrossencountersPath = scripts[scripts.length - 1].src;

function ObsAcrossEncountersController(openmrsRest, $scope, widgetCommons) {
    var ctrl = this;

    ctrl.order = 'desc';
    ctrl.concepts = [];
    ctrl.encounters = [];

    activate();

    function activate() {
        fetchConcepts();
        fetchEncounters();
    }

    function fetchConcepts() {
        ctrl.concepts = getConfigConceptsAsArray(ctrl.config.concepts);
        for(i = 0; i < ctrl.concepts.length; i++) {
            openmrsRest.getFull("concept/" + ctrl.concepts[i], {v: 'custom:(uuid,display,names:(display,conceptNameType)'}).then(function (concept) {
                var index = ctrl.concepts.indexOf(concept.uuid);
                ctrl.concepts[index] = getConceptWithShortName(concept);
            })
        }
    }

    function fetchEncounters() {
        openmrsRest.get("encounter", {
                patient: ctrl.config.patientUuid,
                v: 'custom:(uuid,encounterDatetime,obs:(uuid,value,concept:(uuid))',
                limit: getMaxRecords(),
                fromdate: widgetCommons.dateFromMaxAge(ctrl.config.maxAge),
                order: ctrl.order
            }).then(function (response) {
            getEncounters(response.results);
        });
    }

    function getMaxRecords() {
        if(ctrl.config.maxRecords == '' || angular.isUndefined(ctrl.config.maxRecords)){
            return 4;
        } else {
            return ctrl.config.maxRecords;
        }
    }

    function getConfigConceptsAsArray(commaDelimitedConcepts) {
        return commaDelimitedConcepts.replace(" ", "").split(",");
    }

    function getConceptWithShortName(concept) {
        angular.forEach(concept.names, function (name) {
            if(name.conceptNameType == 'SHORT'){
                concept.display = name.display;
            }
        });
        return concept;
    }

    function getEncounters(encounters) {
        angular.forEach(encounters, function (encounter) {
            var enc = {
                encounterDatetime: encounter.encounterDatetime,
                obs: []
            };
            angular.forEach(getConfigConceptsAsArray(ctrl.config.concepts), function (concept) {
                enc.obs.push(getObservationForConcept(encounter.obs, concept));
            });

            ctrl.encounters.push(enc);
        });
    }

    function getObservationForConcept(observations, conceptUuid) {
        for(i = 0; i < observations.length; i++){
            if(observations[i].concept.uuid === conceptUuid){
                return observations[i];
            }
        }
        return {value: '-'};
    }

    $scope.getTemplate = function () {
        return obsacrossencountersPath.replace(".controller.js", ".html");
    };
}
