import BahmniAppointmentsController from './bahmniappointments.controller';
import template from './bahmniappointments.html';

export let BahmniAppointmentsComponent = {
    template,
    controller: BahmniAppointmentsController,
    selector: 'bahmniappointments',
    bindings: {
        config: '<'
    }
};