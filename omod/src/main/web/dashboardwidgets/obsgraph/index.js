import angular from 'angular';
import openmrsApi from '@openmrs/angularjs-openmrs-api';
import chartjs from 'angular-chart.js';
import commons from './../dashboardwidgets.services';

import { ObsGraphComponent } from './obsgraph.component';

export default angular.module("openmrs-contrib-dashboardwidgets.obsgraph", [openmrsApi, commons, chartjs])
	.component(ObsGraphComponent.selector, ObsGraphComponent)
	.config(['ChartJsProvider', function (ChartJsProvider) {
		ChartJsProvider.setOptions({
			chartColors: ['#00463f'],
			elements: {
				line: {
					tension: 0
				}
			}
		});
	}]).name;
	