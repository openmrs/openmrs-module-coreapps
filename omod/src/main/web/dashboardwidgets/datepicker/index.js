import angular from 'angular';
import 'bootstrap-datepicker';
import 'bootstrap-datepicker/dist/css/bootstrap-datepicker.standalone.css';

import { DatepickerComponent } from './datepicker.component';

export default angular.module("openmrs-contrib-dashboardwidgets.datepicker", [])
	.component(DatepickerComponent.selector, DatepickerComponent).name;