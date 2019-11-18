import AppointmentsController from './appointments.controller';
import template from './appointments.html';

export let AppointmentsComponent = {
    template,
    controller: AppointmentsController,
    selector: 'appointments',
    bindings: {
        config: '<'
    }
};