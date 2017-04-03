angular.module("openmrs-contrib-dashboardwidgets.programstatus", [ "openmrs-contrib-uicommons" ]).component('programstatus', {
    template: '<div ng-include="getTemplate()">',
    controller: ProgramStatusController,
    controllerAs: 'ctrl',
    bindings: {
        config: '<'
    }
});