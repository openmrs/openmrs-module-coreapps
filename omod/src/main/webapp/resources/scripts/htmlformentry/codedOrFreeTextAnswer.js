angular.module('codedOrFreeTextAnswer', [ 'uicommons.widget.coded-or-free-text-answer' ]).

    controller('AnswerCtrl', ['$scope', '$http', '$timeout', '$element',
        function($scope, $http, $timeout, $element) {

            $scope.answer = null;

            $scope.init = function(varName) {
                var initialValue = window[varName];
                if (initialValue) {
                    $scope.answer = initialValue;
                }
            }

            $scope.toSubmit = function() {
                if (!$scope.answer) {
                    return "";
                } else if ($scope.answer.conceptName) {
                    return "ConceptName:" + $scope.answer.conceptName.uuid;
                } else if ($scope.answer.concept) {
                    return "Concept:" + $scope.answer.concept.uuid;
                } else if ($scope.answer.word) {
                    return "NonCoded:" + $scope.answer.word;
                } else {
                    return "";
                }
            }
        }]);