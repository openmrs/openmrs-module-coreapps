import angular from 'angular';
import openmrsApi from '@openmrs/angularjs-openmrs-api';
import openmrsTranslate from '@openmrs/angularjs-openmrs-api';
import chartjs from 'angular-chart.js';
import commons from './../dashboardwidgets.services';

import { ObsGraphComponent } from './obsgraph.component';

export default angular.module("openmrs-contrib-dashboardwidgets.obsgraph", [openmrsApi, openmrsTranslate, commons, chartjs])
	.component(ObsGraphComponent.selector, ObsGraphComponent)
	.config(['ChartJsProvider', function (ChartJsProvider) {
		ChartJsProvider.setOptions({
			chartColors: ['#00463f', '#bf4f44', "#567fad"],
          	spanGaps: true,
			elements: {
				line: {
					tension: 0
				}
			}
		});
	}]).name;
