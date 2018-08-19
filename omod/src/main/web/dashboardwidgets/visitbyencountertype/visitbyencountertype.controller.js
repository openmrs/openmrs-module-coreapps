export default class VisitByEncounterTypeController {
    constructor(openmrsRest, widgetsCommons) {
        'ngInject';

        Object.assign(this, {openmrsRest, widgetsCommons});
    }

    $onInit() {
        this.visits = [];
        this.serverUrl = "";
        this.noVisitsMessage = void 0;
        this.maxAgeInDays = void 0;

        this.openmrsRest.setBaseAppPath("/coreapps");
        this.openmrsRest.getServerUrl().then((result) => {
            this.serverUrl = result;
        });

        // Parse maxAge to day count
        this.maxAgeInDays = this.widgetsCommons.maxAgeToDays(this.config.maxAge);

        //fetchVisits
        this.openmrsRest.get('visit', {
            patient: this.config.patientUuid,
            limit: this.getMaxRecords(),
            fromStartDate: this.widgetsCommons.maxAgeToDate(this.config.maxAge),
            v: 'custom:(uuid,startDatetime,stopDatetime,encounters:(uuid,encounterType:(uuid,display)))'
        }).then((response) => {
            this.getVisits(response.results);
        })
        
        if (angular.isDefined(this.maxAgeInDays)) {
            this.noDataMessage = this.visits.length > 0 ? '' : 'None in the past ' + this.maxAgeInDays + ' days', '';
        } else {
	        this.noDataMessage = 'None';
	    }
        
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
                        let vis = {startDatetime: visit.startDatetime};
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
