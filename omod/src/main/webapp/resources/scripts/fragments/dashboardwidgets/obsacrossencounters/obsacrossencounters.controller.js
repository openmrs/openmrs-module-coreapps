//Determine current script path
var scripts = document.getElementsByTagName("script");
var obsacrossencountersPath = scripts[scripts.length - 1].src;

function ObsAcrossEncountersController(openmrsRest, $scope) {
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
                limit: ctrl.config.maxRecords,
                fromdate: getMaxAgeDate(),
                order: ctrl.order
            }).then(function (response) {
            getEncounters(response.results);
        });
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

    function getMaxAgeDate() {
        var today = new Date();
        var maxAge = ctrl.config.maxAge;
        if( maxAge.indexOf('d') !== -1 ){
            maxAge = maxAge.replace('d', '');
            today.setDate(today.getDate()-parseInt(maxAge));
        } if( maxAge.indexOf('w') !== -1 ){
            maxAge = maxAge.replace('w', '');
            today.setDate(today.getDate()-(parseInt(maxAge)*7));
        } if( maxAge.indexOf('m') !== -1 ){
            maxAge = maxAge.replace('m', '');
            today.setMonth(today.getMonth()-parseInt(maxAge));
        }
        return today;
    }

    $scope.getTemplate = function () {
        return obsacrossencountersPath.replace(".controller.js", ".html");
    };
}
