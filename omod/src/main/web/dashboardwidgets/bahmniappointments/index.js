import angular from 'angular';
import openmrsApi from '@openmrs/angularjs-openmrs-api';
import commons from './../dashboardwidgets.services';
import openmrsTranslate from '@openmrs/angularjs-openmrs-api';

import { BahmniAppointmentsComponent } from './bahmniappointments.component';


export default angular.module("openmrs-contrib-dashboardwidgets.appointments", [ openmrsApi, commons , openmrsTranslate])
	.component(BahmniAppointmentsComponent.selector, BahmniAppointmentsComponent).name;