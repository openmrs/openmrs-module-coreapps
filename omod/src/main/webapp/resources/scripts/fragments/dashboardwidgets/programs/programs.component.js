// NOTE: work-in-progress, consider this not yet an "official" release of this widgets, future changes may not be bacwards-compatible and change functionality signficantly

angular.module("openmrs-contrib-dashboardwidgets.programs", [ "openmrs-contrib-uicommons" ]).component('programs', {
    template: '<div ng-include="getTemplate()">',
    controller: ProgramsController,
    controllerAs: 'ctrl',
    bindings: {
        config: '<'
    }
});