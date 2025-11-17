angular.module('att.widget.complexObsEncounter').directive('attComplexObsEncounter', function() {
    return {
        restrict : 'E',
        scope : {
            encounter : '='
        },
        template : '<att-gallery obs-query="obsQuery"></att-gallery>',
        controller : function($scope) {
            $scope.obsQuery = {
                encounter : $scope.encounter.uuid
            };
        }
    };
});