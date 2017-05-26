export default class LatestObsForConceptListController {
    constructor(openmrsRest, widgetsCommons) {
        'ngInject';

        Object.assign(this, { openmrsRest, widgetsCommons });
    }

    $onInit() {
        this.maxConceptCount = 10;
        this.maxAgeInDays = undefined;
        this.obs = [];

        this.openmrsRest.setBaseAppPath("/coreapps");

        this.maxAgeInDays = this.widgetsCommons.maxAgeToDays(this.config.maxAge);

        // Remove whitespaces
        this.config.concepts = this.config.concepts.replace(/\s/g,'');

        let concepts = this.config.concepts.split(",");
        for (let i = 0; i < concepts.length; i++) {
            let concept = concepts[i];
            // Fetch last obs for concept
            this.openmrsRest.list('obs', {
                patient: this.config.patientUuid,
                v: 'full',
                concept: concept
            }).then((resp) => {
                // Don't add more items if max concept count is reached
                if (this.obs.length < this.maxConceptCount && resp.results.length > 0) {
                    let obs = resp.results[0];
                    // Don't add obs older than maxAge
                    if (angular.isUndefined(this.maxAgeInDays) || this.widgetsCommons.dateToDaysAgo(obs.obsDatetime) <= this.maxAgeInDays) {
                        // Add last obs for concept to list
                        this.obs.push(obs);
                    }
                }
            });
        }
    }
}