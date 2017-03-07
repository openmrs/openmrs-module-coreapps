angular.module("openmrs-contrib-dashboardwidgets.visitbyencountertype", [ "openmrs-contrib-uicommons", "dashboardWidgetsCommons" ]).component('visitbyencountertype', {
    template: '<div ng-include="getTemplate()">',
    controller: VisitByEncounterTypeController,
    controllerAs: 'ctrl',
    bindings: {
        config: '<'
    }
});