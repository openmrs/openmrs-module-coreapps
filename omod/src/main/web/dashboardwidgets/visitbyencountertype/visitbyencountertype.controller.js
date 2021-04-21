export default class VisitByEncounterTypeController {
    constructor(openmrsRest, widgetsCommons , openmrsTranslate) {
        'ngInject';

        Object.assign(this, {openmrsRest, widgetsCommons , openmrsTranslate});
    }

    $onInit() {
        this.visits = [];
        this.serverUrl = "";

        this.openmrsRest.setBaseAppPath("/coreapps");
        this.openmrsRest.getServerUrl().then((result) => {
            this.serverUrl = result;
        });

        //fetchVisits
        this.openmrsRest.get('visit', {
            patient: this.config.patientUuid,
            limit: this.getMaxRecords(),
            fromStartDate: this.widgetsCommons.maxAgeToDate(this.config.maxAge),
            v: 'custom:(uuid,startDatetime,stopDatetime,encounters:(uuid,encounterDatetime,encounterType:(uuid,display)))'
        }).then((response) => {
            this.getVisits(response.results);
        })
    }

    getVisits(visits) {
        angular.forEach(visits, (visit) => {
            let encounterTypes = [];
            if(this.getCombineEncounterTypes()){
                let vis = {startDatetime: visit.startDatetime, encounterType: '', uuid: visit.uuid};
                angular.forEach(visit.encounters, (encounter) => {
                    if (encounterTypes.indexOf(encounter.encounterType.display) == -1) {
                        if (vis.encounterType === '') {
                            vis.encounterType = encounter.encounterType.display;
                        } else {
                            vis.encounterType += ', ' + encounter.encounterType.display;
                        }
                        encounterTypes.push(encounter.encounterType.display);
                    }
                });
                this.visits.push(vis);
            } else {
                angular.forEach(visit.encounters, (encounter) => {
                    if (encounterTypes.indexOf(encounter.encounterType.display) == -1) {
                        let vis = {startDatetime: this.config.showEncounterDate === 'true' ? encounter.encounterDatetime : visit.startDatetime };
                        vis.encounterType = encounter.encounterType.display;
                        encounterTypes.push(encounter.encounterType.display);
                        this.visits.push(vis);
                    }
                })
            }
        })
    }

    getCombineEncounterTypes() {
        if(this.config.combineEncounterTypes === 'false'){
            return false;
        } else {
            return true;
        }
    }

    getMaxRecords() {
        if(this.config.maxRecords == '' || angular.isUndefined(this.config.maxRecords)){
            return 6;
        } else {
            return this.config.maxRecords;
        }
    }
}
