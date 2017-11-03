import angular from 'angular';

export default class ProgramStatisticsController {

    constructor($filter, openmrsRest, openmrsTranslate) {
        'ngInject';

        Object.assign(this, {$filter, openmrsRest, openmrsTranslate});
    }

    $onInit() {
        this.activate();
        let ctrl = this;
    }

    activate() {
        this.openmrsRest.setBaseAppPath("/coreapps");
        this.getEverEnrolledInProgram();
        this.getCurrentlyEnrolledInProgram();
    }

    getEverEnrolledInProgram() {
        return this.openmrsRest.get('reportingrest/cohort', {
            v: "ref",
            uuid: 'reporting.library.cohortDefinition.builtIn.patientsWithEnrollment',
            programs: [this.config.program]
        }).then((response) => {
            this.everEnrolled = (response && 'count' in response ? response.count : this.$filter('translate')('coreapps.dashboardwidgets.programstatistics.error'));
        });
    }

    getCurrentlyEnrolledInProgram() {
        return this.openmrsRest.get('reportingrest/cohort', {
            v: "ref",
            uuid: 'reporting.library.cohortDefinition.builtIn.patientsInProgram',
            programs: [this.config.program],
            onDate: new Date
        }).then((response) => {
            this.currentlyEnrolled = (response && 'count' in response  ? response.count : this.$filter('translate')('coreapps.dashboardwidgets.programstatistics.error'));
    });
    }

}