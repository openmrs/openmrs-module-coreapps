import angular from 'angular';
import openmrsApi from '@openmrs/angularjs-openmrs-api';
import openmrsTranslate from '@openmrs/angularjs-openmrs-api';

import { ProgramsComponent } from './programs.component';

export default angular.module("openmrs-contrib-dashboardwidgets.programs", [ openmrsApi, openmrsTranslate ])
	.component(ProgramsComponent.selector, ProgramsComponent).name;