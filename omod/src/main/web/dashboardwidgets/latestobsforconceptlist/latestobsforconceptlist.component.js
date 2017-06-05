import LatestObsForConceptListController from './latestobsforconceptlist.controller';
import template from './latestobsforconceptlist.html';

export let LatestObsForConceptListComponent = {
    template,
    controller: LatestObsForConceptListController,
    selector: 'latestobsforconceptlist',
	scope: {},
    bindings: {
        config: '<'
    }
};