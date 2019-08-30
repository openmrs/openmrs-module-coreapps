import angular from 'angular';
import openmrsApi from '@openmrs/angularjs-openmrs-api';
import commons from './../dashboardwidgets.services';
import openmrsTranslate from '@openmrs/angularjs-openmrs-api';

import { VisitByEncounterTypeComponent } from './visitbyencountertype.component';


export default angular.module("openmrs-contrib-dashboardwidgets.visitbyencountertype", [ openmrsApi, commons , openmrsTranslate])
	.component(VisitByEncounterTypeComponent.selector, VisitByEncounterTypeComponent).name;