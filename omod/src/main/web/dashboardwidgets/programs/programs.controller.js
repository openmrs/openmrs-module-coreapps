
export default class ProgramsController {

    constructor(openmrsRest, $filter) {
        'ngInject';

        Object.assign(this, {$filter, openmrsRest});
    }

    $onInit() {
        // the default patient page is the clinician dashboard
        this.patientPage = "/coreapps/clinicianfacing/patient.page?patientId={{patientUuid}}&dashboard={{dashboard}}";

        this.programs = [];

        this.unenrolledPrograms = [];

        this.patientPrograms= [];

        this.showAddProgram = false;

        this.input = {
            program: ""
        }

        // TODO did this work?
        this.dateFormat = (this.config.dateFormat == '' || angular.isUndefined(this.config.dateFormat))
            ? 'yyyy-MM-dd' : this.config.dateFormat;

        this.activate();
    }

    activate() {
        this.openmrsRest.setBaseAppPath("/coreapps");

        if(this.config.patientPage) {
            this.patientPage = this.config.patientPage;
        }

        this.fetchPrograms()
            .then(this.fetchPatientPrograms.bind(this))
            .then(this.determineUnenrolledPrograms.bind(this))
    }

    fetchPrograms() {
        return this.openmrsRest.get('program', {
            v: 'custom:display,uuid'
        }).then((response) => {
            this.getPrograms(response.results)
        });
    }

    fetchPatientPrograms() {
        return this.openmrsRest.get('programenrollment', {
            patient: this.config.patientUuid,
            voided: false,
            limit: this.getMaxRecords(),
            v: 'custom:program:(uuid,display),dateEnrolled,dateCompleted'
        }).then((response) => {
            this.getPatientPrograms(response.results);
        });
    }

    getPrograms(programs) {
        angular.forEach(programs, (program) => {
            this.programs.push(program)
        });
    }

    getPatientPrograms(patientPrograms) {
        this.patientPrograms = [];
        angular.forEach(patientPrograms, (patentProgram) => {
            this.patientPrograms.push(patentProgram);
        });
    }

    getMaxRecords() {
        if (this.config.maxRecords == '' || angular.isUndefined(this.config.maxRecords)) {
            return 10;
        } else {
            return this.config.maxRecords;
        }
    }

    determineUnenrolledPrograms() {
        let activeProgramsUuids = [];
        angular.forEach(this.patientPrograms, (patientProgram) => {
            if (!patientProgram.dateCompleted) {
                activeProgramsUuids.push(patientProgram.program.uuid)
            }
        });

        this.unenrolledPrograms = this.$filter('filter')(this.programs, (program) => {
            return activeProgramsUuids.indexOf(program.uuid) == -1
        });
    }

    addProgram() {
        this.showAddProgram = true;
    }

    cancelAddProgram() {
        this.showAddProgram = false;
        this.input.program = "";
    }

   gotoProgramDashboard(programUuid) {

        if (!programUuid) {
            programUuid = this.input.program
        }

        if (programUuid && this.config.enableProgramDashboards) {
            var destinationPage = "";
            destinationPage = Handlebars.compile(this.patientPage)({
                patientUuid: this.config.patientUuid,
                dashboard: programUuid
            });
            this.openmrsRest.getServerUrl().then((url) => {
                window.location.href = url + destinationPage;
            });
        }
    }
}