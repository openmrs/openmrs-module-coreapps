// NOTE: work-in-progress, consider this not yet an "official" release of this widgets, future changes may not be bacwards-compatible and change functionality signficantly

import ProgramsController from './programs.controller';
import template from './programs.html';

export let ProgramsComponent = {
    template,
    controller: ProgramsController,
    selector: 'programs',
    bindings: {
        config: '<'
    }
};