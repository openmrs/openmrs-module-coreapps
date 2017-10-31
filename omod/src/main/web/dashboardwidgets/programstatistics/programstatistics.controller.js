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
        return this.openmrsRest.update('reportingrest/cohort', {
            uuid: 'reporting.library.cohortDefinition.builtIn.patientsWithEnrollment',
            programs: [this.config.program]
        }).then((response) => {
            this.everEnrolled = (response && response.members ? response.members.length : 0);
        });
    }

    getCurrentlyEnrolledInProgram() {
        return this.openmrsRest.update('reportingrest/cohort', {
            uuid: 'reporting.library.cohortDefinition.builtIn.patientsInProgram',
            programs: [this.config.program],
            onDate: new Date
        }).then((response) => {
            this.currentlyEnrolled = (response && response.members ? response.members.length : 0);
    });
    }

}