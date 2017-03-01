function LatestObsForConceptListController($scope, $element, $attrs) {
    var ctrl = this;

    // This would be loaded by $http etc.
    ctrl.list = [
            {
                name: 'Superman',
            },
            {
                name: 'Batman'
            }
    ];
}
