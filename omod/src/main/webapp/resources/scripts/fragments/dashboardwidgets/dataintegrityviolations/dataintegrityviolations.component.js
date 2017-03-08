angular.module("openmrs-contrib-dashboardwidgets.dataintegrityviolations", [ "openmrs-contrib-uicommons" ]).component('dataintegrityviolations', {
    template: '<div ng-include="getTemplate()">',
    controller: DataIntegrityViolationsController,
    controllerAs: 'ctrl',
    bindings: {
        config: '<'
    }
});