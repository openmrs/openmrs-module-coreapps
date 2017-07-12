import angular from 'angular';
import openmrsApi from '@openmrs/angularjs-openmrs-api';
import bootstrapDatepicker from 'bootstrap-datepicker';
import 'bootstrap-datepicker/dist/css/bootstrap-datepicker.standalone.css';

import { ProgramStatusComponent } from './programstatus.component';

export default angular.module("openmrs-contrib-dashboardwidgets.programstatus", [ openmrsApi ])
	.component(ProgramStatusComponent.selector, ProgramStatusComponent).name;