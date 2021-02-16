import angular from 'angular';
import openmrsApi from '@openmrs/angularjs-openmrs-api';
import openmrsTranslate from '@openmrs/angularjs-openmrs-api';
import datepicker from './../datepicker';
import commons from './../dashboardwidgets.services';

import { ProgramStatusComponent } from './programstatus.component';

export default angular.module("openmrs-contrib-dashboardwidgets.programstatus", [ openmrsApi, datepicker, openmrsTranslate, commons ])
	.component(ProgramStatusComponent.selector, ProgramStatusComponent).name;