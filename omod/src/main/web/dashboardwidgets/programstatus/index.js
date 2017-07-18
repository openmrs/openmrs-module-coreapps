import angular from 'angular';
import openmrsApi from '@openmrs/angularjs-openmrs-api';
import datepicker from './../datepicker';

import { ProgramStatusComponent } from './programstatus.component';

export default angular.module("openmrs-contrib-dashboardwidgets.programstatus", [ openmrsApi, datepicker ])
	.component(ProgramStatusComponent.selector, ProgramStatusComponent).name;