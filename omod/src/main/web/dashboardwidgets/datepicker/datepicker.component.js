import controller from './datepicker.controller';
import template from './datepicker.html';

export let DatepickerComponent = {
    template,
    controller,
    selector: 'openmrsDatepicker',
    bindings: {
        ngModel: '=',
        format: '@',
        language: '@',
        startDate: '<',
        endDate: '<',
        clearBtn: '<'
    }
};