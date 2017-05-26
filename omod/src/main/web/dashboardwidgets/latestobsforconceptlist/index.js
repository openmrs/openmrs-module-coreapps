import angular from 'angular';
import openmrsApi from '@openmrs/angularjs-openmrs-api';
import commons from './../dashboardwidgets.services';
import { LatestObsForConceptListComponent } from './latestobsforconceptlist.component';

export default angular.module("openmrs-contrib-dashboardwidgets.latestobsforconceptlist", [ openmrsApi, commons ])
	.component(LatestObsForConceptListComponent.selector, LatestObsForConceptListComponent).name;