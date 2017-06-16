// NOTE: work-in-progress, consider this not yet an "official" release of this widgets, future changes may not be bacwards-compatible and change functionality signficantly
// TODO: document when complete


import controller from './programstatus.controller';
import template from './programstatus.html';

export let ProgramStatusComponent = {
    template,
    controller,
    selector: 'programstatus',
    bindings: {
        config: '<'
    }
};