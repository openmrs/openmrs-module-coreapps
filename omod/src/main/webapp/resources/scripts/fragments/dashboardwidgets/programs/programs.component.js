angular.module("openmrs-contrib-dashboardwidgets.programs", [ "openmrs-contrib-uicommons" ]).component('programs', {
    template: '<div ng-include="getTemplate()">',
    controller: ProgramsController,
    controllerAs: 'ctrl',
    bindings: {
        config: '<'
    }
});