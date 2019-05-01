export default class LatestObsForConceptListController {
    constructor($filter, openmrsRest, widgetsCommons) {
        'ngInject';

        Object.assign(this, {$filter, openmrsRest, widgetsCommons });
    }

    $onInit() {
        this.maxConceptCount = 10;
        this.maxAgeInDays = undefined;
        this.obs = [];

        this.openmrsRest.setBaseAppPath("/coreapps");

        this.maxAgeInDays = this.widgetsCommons.maxAgeToDays(this.config.maxAge);

        // Remove whitespaces
        let concept_list = this.config.concepts.replace(/\s/g,'');
        // remove all concepts over the maximum number of concepts
        let concepts = this.config.concepts.split(",");
        // from the number of elements specified by maxConceptCount remove the remaining elements
        let elements_to_remove = concepts.length - this.maxConceptCount;
        if (elements_to_remove > 0) {
            concepts.splice(this.maxConceptCount, elements_to_remove);
        }

        concept_list = concepts.join(",");

        // Fetch last obs for the list of concepts
        this.openmrsRest.list('latestobs', {
            patient: this.config.patientUuid,
            v: 'full',
            concept: concept_list
        }).then((resp) => {
            // Process the results from the list of concepts as not all of them may have data
            for (let i = 0; i < resp.results.length; i++) {
                let obs = resp.results[i];
                // Don't add obs older than maxAge
                if (angular.isUndefined(this.maxAgeInDays) || this.widgetsCommons.dateToDaysAgo(obs.obsDatetime) <= this.maxAgeInDays) {
                    // Add last obs for concept to list

                    if (['8d4a505e-c2cc-11de-8d13-0010c6dffd0f',
                        '8d4a591e-c2cc-11de-8d13-0010c6dffd0f',
                        '8d4a5af4-c2cc-11de-8d13-0010c6dffd0f'].indexOf(obs.concept.datatype.uuid) > -1) {
                        //If value is date, time or datetime
                        var date = this.$filter('date')(new Date(obs.value), this.config.dateFormat);
                        obs.value = date;
                    } else if (angular.isDefined(obs.value.display)) {
                        //If value is a concept
                        obs.value = obs.value.display;
                    }

                    this.obs.push(obs);
                }
            }
        });
    }
}