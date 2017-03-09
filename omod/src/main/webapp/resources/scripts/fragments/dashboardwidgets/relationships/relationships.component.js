angular.module("openmrs-contrib-dashboardwidgets.relationships", [ "openmrs-contrib-uicommons" ]).component('relationships', {
    template: '<div ng-include="getTemplate()">',
    controller: RelationshipsController,
    controllerAs: 'ctrl',
    bindings: {
        config: '<'
    }
});