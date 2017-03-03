angular.module("openmrs-contrib-dashboardwidgets", [ "openmrs-contrib-uicommons" ]).component('latestobsforconceptlist', {
    template: '<div ng-include="getTemplate()">',
    controller: LatestObsForConceptListController,
    bindings: {
        config: '<'
    }
});