import angular from 'angular';
import openmrsApi from '@openmrs/angularjs-openmrs-api';
import datepickerPopup from 'angular-ui-bootstrap/src/datepickerPopup';
import 'bootstrap/dist/css/bootstrap.css'

import { ProgramStatusComponent } from './programstatus.component';

export default angular.module("openmrs-contrib-dashboardwidgets.programstatus", [ openmrsApi, datepickerPopup ])
	.component(ProgramStatusComponent.selector, ProgramStatusComponent).name;