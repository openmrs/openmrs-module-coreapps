
// NOTE: work-in-progress, consider this not yet an "official" release of this widgets, future changes may not be backwards-compatible and change functionality signficantly

angular.module("openmrs-contrib-dashboardwidgets.programstatus", [ "openmrs-contrib-uicommons", "ui.bootstrap"]).component('programstatus', {
    template: '<div ng-include="getTemplate()">',
    controller: ProgramStatusController,
    controllerAs: 'ctrl',
    bindings: {
        config: '<'
    }
});