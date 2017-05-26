import angular from 'angular';
import openmrsApi from '@openmrs/angularjs-openmrs-api';
import uiBootstrap from 'angular-ui-bootstrap';

import { ProgramStatusComponent } from './programstatus.component';

export default angular.module("openmrs-contrib-dashboardwidgets.programstatus", [ openmrsApi, "ui.bootstrap" ])
	.component(ProgramStatusComponent.selector, ProgramStatusComponent).name;