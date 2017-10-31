import angular from 'angular';
import openmrsApi from '@openmrs/angularjs-openmrs-api';
import openmrsTranslate from '@openmrs/angularjs-openmrs-api';

import { ProgramStatisticsComponent } from './programstatistics.component';

export default angular.module("openmrs-contrib-dashboardwidgets.programstatistics", [ openmrsApi, openmrsTranslate ])
    .component(ProgramStatisticsComponent.selector, ProgramStatisticsComponent).name;