export default class ObsAcrossEncountersController {
    constructor($filter, openmrsRest, widgetsCommons) {
        'ngInject';

        Object.assign(this, { $filter, openmrsRest, widgetsCommons });
    }

    $onInit() {
        this.order = 'desc';
        this.concepts = [];
        this.encounters = [];

        this.openmrsRest.setBaseAppPath("/coreapps");

        this.fetchConcepts();
        this.fetchEncounters();
    }

    fetchConcepts() {
        this.concepts = this.getConfigConceptsAsArray(this.config.concepts);
        for(let i = 0; i < this.concepts.length; i++) {
            this.openmrsRest.getFull("concept/" + this.concepts[i], {v: 'custom:(uuid,display,names:(display,conceptNameType)'}).then((concept) => {
                let index = this.concepts.indexOf(concept.uuid);
                this.concepts[index] = this.getConceptWithShortName(concept);
            });
        }
    }

    fetchEncounters() {
        this.openmrsRest.get("encounter", {
            patient: this.config.patientUuid,
            v: 'custom:(uuid,encounterDatetime,obs:(uuid,value,concept:(uuid,datatype),groupMembers)',
            limit: this.getMaxRecords(),
            fromdate: this.widgetsCommons.maxAgeToDate(this.config.maxAge),
            order: this.order
        }).then((response) => {
            this.getEncounters(response.results);
        });
    }

    getMaxRecords() {
        if(this.config.maxRecords == '' || angular.isUndefined(this.config.maxRecords)){
            return 4;
        } else {
            return this.config.maxRecords;
        }
    }

    getConfigConceptsAsArray(commaDelimitedConcepts) {
        return commaDelimitedConcepts.replace(" ", "").split(",");
    }

    getConceptWithShortName(concept) {
        angular.forEach(concept.names, (name) => {
            if(name.conceptNameType == 'SHORT'){
                concept.display = name.display;
            }
        });
        return concept;
    }

    getEncounters(encounters) {
        angular.forEach(encounters, (encounter) => {
            let enc = {
                encounterDatetime: encounter.encounterDatetime,
                obs: []
            };
            var nullRow = true;
            angular.forEach(this.getConfigConceptsAsArray(this.config.concepts), (concept) => {
                let obsValue = this.getObservationForConcept(encounter.obs, concept);
                enc.obs.push(obsValue);
                if(obsValue.value !== '-') {
                    nullRow = false;
                }
            });

            if(!nullRow) {
                this.encounters.push(enc);
            }
        });
    }

    getObservationForConcept(observations, conceptUuid) {
        for(var i = 0; i < observations.length; i++){
            var obs = observations[i];
            if (obs.groupMembers != null) {
                if (obs.groupMembers.length !== 0) {
                    //it is a group obs
                    for (var j = 0; j < obs.groupMembers.length; j++) {
                        var groupMember = obs.groupMembers[j];
                        if (groupMember.concept.uuid === conceptUuid) {
                            obs = groupMember;
                            break;
                        }
                    }
                }
            }

            if (obs.value != null && obs.concept.uuid === conceptUuid) {
                if (angular.isDefined(obs.value.display)) {
                    //If value is a concept
                    obs.value = obs.value.display;
                }
                else if (['8d4a505e-c2cc-11de-8d13-0010c6dffd0f',
                        '8d4a591e-c2cc-11de-8d13-0010c6dffd0f',
                        '8d4a5af4-c2cc-11de-8d13-0010c6dffd0f'].indexOf(obs.concept.datatype.uuid) > -1) {
                    //If value is date, time or datetime
                    var date = this.$filter('date')(new Date(obs.value), this.config.dateFormat);
                    obs.value = date;
                }

                return obs;
            }
        }
        return {value: '-'};
    }
}
