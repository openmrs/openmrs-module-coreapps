export default class DataIntegrityViolationsController {
    constructor (openmrsRest) {
        'ngInject';

        Object.assign(this, { openmrsRest });
    }

    $onInit() {
        this.dataViolations = [];

        this.openmrsRest.setBaseAppPath("/coreapps");
        this.openmrsRest.getServerUrl().then((result) => {
            this.serverUrl = result;
        });

        // Set default maxResults if not defined
        if (angular.isUndefined(this.config.maxResults)) {
            this.config.maxResults = 6;
        }

        this.openmrsRest.list('dataintegrity/integrityresults', { patient: this.config.patientUuid, v: 'full', limit: this.config.maxResults }).then((resp) => {
            for (let index = 0; index < resp.results.length; index++) {
                this.dataViolations.push(resp.results[index]);
            }
        });
    }
}