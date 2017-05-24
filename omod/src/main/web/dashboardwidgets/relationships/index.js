import angular from 'angular';
import openmrsApi from '@openmrs/angularjs-openmrs-api';
import { RelationshipsComponent } from './relationships.component';

export default angular.module("openmrs-contrib-dashboardwidgets.relationships", [ openmrsApi ])
	.component(RelationshipsComponent.selector, RelationshipsComponent).name;