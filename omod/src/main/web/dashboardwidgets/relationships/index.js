import angular from 'angular';
import openmrsApi from '@openmrs/angularjs-openmrs-api';
import uiBootstrap from 'angular-ui-bootstrap';

import { RelationshipsComponent } from './relationships.component';

export default angular.module("openmrs-contrib-dashboardwidgets.relationships", [ openmrsApi, 'ui.bootstrap' ])
	.component(RelationshipsComponent.selector, RelationshipsComponent).name;