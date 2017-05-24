import VisitByEncounterTypeController from './visitbyencountertype.controller';
import template from './visitbyencountertype.html';

export let VisitByEncounterTypeComponent = {
    template,
    controller: VisitByEncounterTypeController,
    selector: 'visitbyencountertype',
    bindings: {
        config: '<'
    }
};