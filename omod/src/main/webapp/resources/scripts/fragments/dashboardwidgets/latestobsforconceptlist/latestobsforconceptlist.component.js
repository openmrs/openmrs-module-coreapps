angular.module("openmrs-contrib-dashboardwidgets.latestobsforconceptlist", [ "openmrs-contrib-uicommons", "dashboardWidgetsCommons" ]).component('latestobsforconceptlist', {
    template: '<div ng-include="getTemplate()">',
    controller: LatestObsForConceptListController,
    controllerAs: 'ctrl',
    bindings: {
        config: '<'
    }
});