import angular from 'angular';
import openmrsApi from '@openmrs/angularjs-openmrs-api';
import commons from './../dashboardwidgets.services';

import { ObsAcrossEncountersComponent } from './obsacrossencounters.component';

export default angular.module("openmrs-contrib-dashboardwidgets.obsacrossencounters", [ openmrsApi, commons ])
	.component(ObsAcrossEncountersComponent.selector, ObsAcrossEncountersComponent).name;