angular.module('att.fragment.dashboardWidget').controller('DashboardWidgetCtrl',
        [ '$scope', '$window', function($scope, $window) {
            $scope.obsQuery = {
                limit : $window.config.thumbnailCount,
                patient : $window.config.patient.uuid,
                concepts : $window.config.conceptComplexUuidList.toString()
            // http://stackoverflow.com/a/202247/321797
            };
        } ]);