import angular from 'angular';
import openmrsApi from '@openmrs/angularjs-openmrs-api';
import openmrsTranslate from '@openmrs/angularjs-openmrs-api';
import commons from './../dashboardwidgets.services';

import { ProgramsComponent } from './programs.component';

export default angular.module("openmrs-contrib-dashboardwidgets.programs", [ openmrsApi, openmrsTranslate, commons ])
	.component(ProgramsComponent.selector, ProgramsComponent).name;