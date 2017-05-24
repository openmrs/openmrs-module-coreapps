import angular from 'angular';
import openmrsApi from '@openmrs/angularjs-openmrs-api';
import commons from './../dashboardwidgets.services';

import { VisitByEncounterTypeComponent } from './visitbyencountertype.component';


export default angular.module("openmrs-contrib-dashboardwidgets.visitbyencountertype", [ openmrsApi, commons ])
	.component(VisitByEncounterTypeComponent.selector, VisitByEncounterTypeComponent).name;