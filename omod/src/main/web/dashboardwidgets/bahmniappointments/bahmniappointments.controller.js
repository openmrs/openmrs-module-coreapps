export default class BahmniAppointmentsController {
    constructor(openmrsRest, widgetsCommons, openmrsTranslate) {
        'ngInject';

        Object.assign(this, { openmrsRest, widgetsCommons, openmrsTranslate });
    }

    $onInit() {
        //innitialise
        this.appointments = [];
        this.serverUrl = "";
        this.startDate = new Date();
        this.openmrsRest.setBaseAppPath("/coreapps");
        this.openmrsRest.getServerUrl().then((result) => {
            this.serverUrl = result;
        });
        //overiding default 'POST method' configurations
        let methodConfig = {
            url: '/appointments/search',
            actions: { save: { method: 'POST', isArray: true } }
        }

        this.openmrsRest.create(methodConfig, {
            patientUuid: this.config.patientUuid,
            startDate: this.startDate,
            limit: this.config.maxRecords
        }).then((response) => {
            this.getAppointments(response);
        })
    }

    getAppointments(appointments) {
        if (appointments.length > 0) {
            angular.forEach(appointments, (appointment) => {
                    let appointmentDetails = {
                        date: appointment.startDateTime,
                        startTime: appointment.startDateTime,
                        endTime: appointment.endDateTime,
                        ServiceType: appointment.service.name
                    };
                    this.appointments.push(appointmentDetails);
            })
        }
    }
}