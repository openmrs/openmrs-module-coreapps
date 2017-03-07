angular.module("openmrs-contrib-dashboardwidgets.obsacrossencounters", [ "openmrs-contrib-uicommons", "dashboardWidgetsCommons" ]).component('obsacrossencounters', {
    template: '<div ng-include="getTemplate()">',
    controller: ObsAcrossEncountersController,
    controllerAs: 'ctrl',
    bindings: {
        config: '<'
    }
});