
import angular from 'angular';

export default class ProgramsController {

    constructor($filter, openmrsRest, openmrsTranslate) {
        'ngInject';

        Object.assign(this, {$filter, openmrsRest, openmrsTranslate});
    }

    $onInit() {
        // the default patient page is the clinician dashboard
        this.dashboardPage = "/coreapps/clinicianfacing/patient.page?patientId={{patientUuid}}&dashboard={{dashboard}}";

        this.programs = [];

        this.unenrolledPrograms = [];

        this.patientPrograms= [];

        this.showAddProgram = false;

        this.canEnrollInProgram = false;

        this.input = {
            program: ""
        };

        // TODO did this work?
        this.dateFormat = (this.config.dateFormat == '' || angular.isUndefined(this.config.dateFormat))
            ? 'yyyy-MM-dd' : this.config.dateFormat;

        this.supportedPrograms = [];

        if (this.config.supportedPrograms) {
            this.supportedPrograms = this.config.supportedPrograms.split(',');
        }

        this.activate();
    }

    activate() {
        this.openmrsRest.setBaseAppPath("/coreapps");

        if(this.config.dashboardPage) {
            this.dashboardPage = this.config.dashboardPage;
        }

        this.fetchPrivileges();

        this.fetchPrograms()
            .then(this.fetchPatientPrograms.bind(this))
            .then(this.determineUnenrolledPrograms.bind(this));
    }

    fetchPrivileges() {
        this.openmrsRest.get('session', {
            v: 'custom:(privileges)'
        }).then((response) => {
            if (response && response.user && angular.isArray(response.user.privileges)) {
                if (response.user.privileges.some( (p) => { return p.name === 'Task: coreapps.enrollInProgram'; })) {
                    this.canEnrollInProgram = true;
                };
            }
        });
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
            // filter out any unsupported programs
            if (this.supportedPrograms.length == 0 || this.supportedPrograms.indexOf(program.uuid) != -1) {
                this.programs.push(program)
            }
        });
    }

    getPatientPrograms(patientPrograms) {
        this.patientPrograms = [];
        angular.forEach(patientPrograms, (patientProgram) => {
            // filter out any unsupported programs
            if (this.supportedPrograms.length == 0 || this.supportedPrograms.indexOf(patientProgram.program.uuid) != -1) {
                this.patientPrograms.push(patientProgram);
            }
        });
    }

    getMaxRecords() {
        if (this.config.maxRecords == '' || angular.isUndefined(this.config.maxRecords)) {
            return 20;
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
            destinationPage = Handlebars.compile(this.dashboardPage)({
                patientUuid: this.config.patientUuid,
                dashboard: programUuid
            });
            this.openmrsRest.getServerUrl().then((url) => {
                window.location.href = url + destinationPage;
            });
        }
    }
}