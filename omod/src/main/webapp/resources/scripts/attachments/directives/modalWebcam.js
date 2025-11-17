angular.module('att.widget.modalWebcam')

.directive('attEscapeKeyDown', function() {
    return function(scope, element, attrs) {
        element.bind("keydown keypress", function(event) {
            if (event.which === 27) {
                scope.$apply(function() {
                    scope.$eval(attrs.attEscapeKeyDown, {
                        'event' : event
                    });
                });
                event.preventDefault();
            }
        });
    };
})

.directive('attModalWebcam', [ 'ModuleUtils', function(module) {
    return {
        restrict : 'E',
        scope : {
            visible : '=',
            disabled : '=?'
        },
        templateUrl : '/' + module.getPartialsPath(OPENMRS_CONTEXT_PATH) + '/modalWebcam.html',
        controller : function($scope, $rootScope, $window) {
            $scope.imgWidth = 640;
            $scope.imgHeight = 480;

            $scope.setWebcam = function() {
                Webcam.set({
                    width : $scope.imgWidth,
                    height : $scope.imgHeight,
                    dest_width : $scope.imgWidth,
                    dest_height : $scope.imgHeight,
                });
                Webcam.attach('#att_webcam-id');
            }

            $scope.$watch("visible", function() {
                if ($scope.disabled === true)
                    return;

                if ($scope.visible) {
                    $scope.setWebcam();
                } else {
                    Webcam.reset();
                }
            });

            $scope.close = function() {
                $scope.visible = false;
                $scope.dataUri = null;
            }

            $scope.snap = function() {
                Webcam.snap(function(dataUri) {
                    $scope.dataUri = dataUri;

                });
            }

            $scope.repeat = function() {
                $scope.dataUri = null;
            }

            $scope.finalise = function() {
                var file = module.dataUritoBlob($scope.dataUri);
                file.name = "wc_pic_" + moment(new Date()).format("YYYYMMDD_HHmmss") + ".png";
                $rootScope.$emit(module.webcamCaptureForUpload, file);
                $scope.close();
            }
        }
    };
} ]);