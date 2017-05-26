import ObsGraphController from './obsgraph.controller';
import template from './obsgraph.html';

export let ObsGraphComponent = {
    template,
    controller: ObsGraphController,
    selector: 'obsgraph',
    bindings: {
        config: '<'
    }
};