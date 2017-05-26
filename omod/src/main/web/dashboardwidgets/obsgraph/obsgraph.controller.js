export default class ObsGraphController {
    constructor($filter, openmrsRest, widgetsCommons) {
        'ngInject';

        Object.assign(this, { $filter, openmrsRest, widgetsCommons });
    }
    
    $onInit() {
        // Max age of obs to display
        this.maxAgeInDays = undefined;

        // Concept info
        this.concept = {};

        // Chart data
        this.series = [];
        this.labels = [];
        this.data = [[]];

        this.openmrsRest.setBaseAppPath("/coreapps");

        // Set default maxResults if not defined
        if (angular.isUndefined(this.config.maxResults)) {
            this.config.maxResults = 4;
        }
        // Parse maxAge to day count
        this.maxAgeInDays = this.widgetsCommons.maxAgeToDays(this.config.maxAge);

        this.openmrsRest.list('obs',{ patient: this.config.patientUuid, v: 'full', limit: this.config.maxResults, concept: this.config.conceptId }).then((resp) => {
            const obss = resp.results;
            if (obss.length > 0) {
                // Set concept to display
                this.concept = obss[0].concept;
                this.series.push(this.concept.display);
                for (let i = 0; i < obss.length; i++) {
                    let obs = obss[i];
                    // Show numeric concepts only
                    if (obs.concept.datatype.display == 'Numeric') {
                        // Don't add obs older than maxAge
                        if (angular.isUndefined(this.maxAgeInDays) || this.widgetsCommons.dateToDaysAgo(obs.obsDatetime) <= this.maxAgeInDays) {
                            // Add obs data for chart display
                            var date = this.$filter('date')(new Date(obs.obsDatetime), this.config.dateFormat);
                            this.labels.unshift(date);
                            this.data[0].unshift(obs.value);
                        }
                    }
                }
            }
        })
    }
}