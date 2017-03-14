//Determine current script path
var scripts = document.getElementsByTagName("script");
var visitbyencountertypePath = scripts[scripts.length - 1].src;

function VisitByEncounterTypeController(openmrsRest, $scope, widgetCommons) {
    var ctrl = this;

    ctrl.visits = [];

    activate();

    function activate() {
        openmrsRest.setBaseAppPath("/coreapps");
        fetchVisits();
    }

    function fetchVisits() {
        openmrsRest.get('visit', {
            patient: ctrl.config.patientUuid,
            limit: getMaxRecords(),
            fromStartDate: widgetCommons.dateFromMaxAge(ctrl.config.maxAge),
            v: 'custom:(uuid,startDatetime,stopDatetime,encounters:(uuid,encounterType:(uuid,display)))'
        }).then(function (response) {
                getVisits(response.results);
        })
    }

    function getVisits(visits) {
        angular.forEach(visits, function (visit) {
            var encounterTypes = [];
            if(getCombineEncounterTypes()){
                var vis = {startDatetime: visit.startDatetime, encounterType: '', uuid: visit.uuid};
                angular.forEach(visit.encounters, function (encounter) {
                    if (encounterTypes.indexOf(encounter.encounterType.display) == -1) {
                        if (vis.encounterType === '') {
                            vis.encounterType = encounter.encounterType.display;
                        } else {
                            vis.encounterType += ', ' + encounter.encounterType.display;
                        }
                        encounterTypes.push(encounter.encounterType.display);
                    }
                });
                ctrl.visits.push(vis);
            } else {
                angular.forEach(visit.encounters, function (encounter) {
                    if (encounterTypes.indexOf(encounter.encounterType.display) == -1) {
                        var vis = {startDatetime: visit.startDatetime};
                        vis.encounterType = encounter.encounterType.display;
                        encounterTypes.push(encounter.encounterType.display);
                        ctrl.visits.push(vis);
                    }
                })
            }
        })
    }

    function getCombineEncounterTypes() {
        if(ctrl.config.combineEncounterTypes === 'false'){
            return false;
        } else {
            return true;
        }
    }

    function getMaxRecords() {
        if(ctrl.config.maxRecords == '' || angular.isUndefined(ctrl.config.maxRecords)){
            return 6;
        } else {
            return ctrl.config.maxRecords;
        }
    }

    $scope.getTemplate = function () {
        return visitbyencountertypePath.replace(".controller.js", ".html");
    };
}
