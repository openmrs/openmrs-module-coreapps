angular.module("openmrs-contrib-dashboardwidgets.obsgraph", [ "openmrs-contrib-uicommons", "dashboardWidgetsCommons", "chart.js"])
    .component('obsgraph', {
    template: '<div ng-include="getTemplate()">',
    controller: ObsGraphController,
    controllerAs: 'ctrl',
    bindings: {
        config: '<'
    }
}).config(['ChartJsProvider', function (ChartJsProvider) {
    // Configure all charts
    ChartJsProvider.setOptions({
        chartColors: ['#00463f'],
        elements: {
            line: {
                tension: 0
            }
        }
    });
}]);