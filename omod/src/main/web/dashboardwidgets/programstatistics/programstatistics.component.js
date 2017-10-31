// NOTE: work-in-progress, consider this not yet an "official" release of this widgets, future changes may not be bacwards-compatible and change functionality signficantly
// TODO: document when we do an "official" release

import ProgramStatisticsController from './programstatistics.controller';
import template from './programstatistics.html';

export let ProgramStatisticsComponent = {
    template,
    controller: ProgramStatisticsController,
    selector: 'programstatistics',
    bindings: {
        config: '<'
    }
};