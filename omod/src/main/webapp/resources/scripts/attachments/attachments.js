angular.module('att.page.main').controller('FileUploadCtrl', [ '$scope', '$window', function($scope, $window) {
    $scope.config = $window.att.config;
} ]);

angular.module('att.page.main').controller('GalleryCtrl', [ '$scope', '$window', function($scope, $window) {
    $scope.obsQuery = {
        patient : $window.att.config.patient.uuid,
        concepts: $window.att.config.conceptComplexUuidList.toString()
    // http://stackoverflow.com/a/202247/321797
    };
} ]);