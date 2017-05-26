import angular from 'angular';
import openmrsApi from '@openmrs/angularjs-openmrs-api';
import { DatatIntegrityViolationsComponent } from './dataintegrityviolations.component';

export default angular.module("openmrs-contrib-dashboardwidgets.dataintegrityviolations", [ openmrsApi ])
	.component(DatatIntegrityViolationsComponent.selector, DatatIntegrityViolationsComponent).name;