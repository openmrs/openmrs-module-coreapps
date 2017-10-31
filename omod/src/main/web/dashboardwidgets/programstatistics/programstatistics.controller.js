import angular from 'angular';

export default class ProgramStatisticsController {

    constructor($filter, openmrsRest, openmrsTranslate) {
        'ngInject';

        Object.assign(this, {$filter, openmrsRest, openmrsTranslate});
    }

    $onInit() {

        this.everEnrolled;

        this.activate();
        let ctrl = this;
    }

    activate() {
        this.openmrsRest.setBaseAppPath("/coreapps");
        this.getEnrolledInProgram();
    }

    getEnrolledInProgram() {
        return this.openmrsRest.update('reportingrest/cohort', {
            uuid: 'reporting.library.cohortDefinition.builtIn.patientsWithEnrollment',
            programs: [this.config.program]
        }).then((response) => {
            this.everEnrolled = (response && response.members ? response.members.length : 0);
    });
    }

}