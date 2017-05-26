import ObsAcrossEncountersController from './obsacrossencounters.controller';
import template from './obsacrossencounters.html';

export let ObsAcrossEncountersComponent = {
    template,
    controller: ObsAcrossEncountersController,
    selector: 'obsacrossencounters',
    bindings: {
        config: '<'
    }
};