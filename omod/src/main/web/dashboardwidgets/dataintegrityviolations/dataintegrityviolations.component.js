import DataIntegrityViolationsController from './dataintegrityviolations.controller';
import template from './dataintegrityviolations.html';

export let DatatIntegrityViolationsComponent = {
    template,
    controller: DataIntegrityViolationsController,
    selector: 'dataintegrityviolations',
    bindings: {
        config: '<'
    }
};