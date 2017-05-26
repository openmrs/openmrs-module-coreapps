import RelationshipsController from './relationships.controller';
import template from './relationships.html';

export let RelationshipsComponent = {
    template,
    controller: RelationshipsController,
    selector: 'relationships',
    bindings: {
        config: '<'
    }
};