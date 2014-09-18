angular.module('codedOrFreeTextAnswerList', [ 'ui.bootstrap' ]).

    controller('ListCtrl', ['$scope', '$http', '$timeout', '$element',
    function($scope, $http, $timeout, $element) {

        var codingSystemToUse = 'ICD-10-WHO';
        var mapTypeOrder = [ "SAME-AS", "NARROWER-THAN" ];

        function findConceptMapping(concept, sourceName) {
            var matches = _.filter(concept.conceptMappings, function(item) {
                return item.conceptReferenceTerm.conceptSource.name == sourceName
            });
            if (!matches || matches.length == 0) {
                return "";
            }
            return _.sortBy(matches, function(item) {
                var temp = _.indexOf(mapTypeOrder, item.conceptMapType);
                return temp < 0 ? 9999 : temp;
            })[0].conceptReferenceTerm.code;
        };

        function swapElements(arr, oldInd, newInd) {
            var temp = arr[newInd];
            arr[newInd] = arr[oldInd];
            arr[oldInd] = temp;
        }

        function alreadySelected(answers, item) {
            return _.some(answers, function(candidate) {
                if (typeof item == 'string') {
                    return candidate.nonCodedValue === item;
                } else {
                    return candidate.concept && (candidate.concept.uuid === item.concept.uuid);
                }
            });
        }

        function isExactMatch(candidate, query) {
            query = emr.stripAccents(query.toLowerCase());
            return (candidate.code && emr.stripAccents(candidate.code.toLowerCase()) === query) ||
                (candidate.matchedName && emr.stripAccents(candidate.matchedName.toLowerCase()) === query);
        }

        function convertSearchResult(item) {
            if (item.nonCodedValue) {
                return item;
            }
            else {
                var converted = {
                    concept: item.concept,
                    conceptName: item.conceptName,
                    code: findConceptMapping(item.concept, codingSystemToUse),
                    matchedName: item.conceptName ? item.conceptName.name : item.concept.preferredName
                };
                converted.nameIsPreferred = converted.conceptName && (converted.matchedName == converted.concept.preferredName);
                return converted;
            }
        }

        $scope.selection = null;
        $scope.answerList = [ ];

        $scope.init = function(varName) {
            var initialValue = window[varName];
            if (initialValue) {
                _.each(initialValue, function(item) {
                    $scope.answerList.push(convertSearchResult(item));
                });
            }
        }

        $scope.searchDiagnoses = function(query) {
            console.log("Search for: " + query);
            if (query.length < 2) {
                return [];
            }
            return $http.get('/' + OPENMRS_CONTEXT_PATH + '/coreapps/diagnoses/search.action?term=' + query).
                then(function(results) {
                    var list = [];
                    var exactMatch = false;
                    angular.forEach(results.data, function(item) {
                        if (alreadySelected($scope.answerList, item)) {
                            return;
                        }
                        var converted = convertSearchResult(item);
                        list.push(converted);
                        if (!exactMatch && isExactMatch(converted, query)) {
                            exactMatch = true;
                        }
                    });
                    if (!exactMatch && !alreadySelected($scope.answerList, query)) {
                        list.push({
                            matchedName: query,
                            nonCodedValue: query
                        });
                    }
                    return list;
                });
        }

        $scope.selected = function(item) {
            $scope.answerList.push(item);
            $scope.selection = null;
            $element.find('.manual-exit').addClass('do-not-exit-once');
        }

        $scope.remove = function(index) {
            $scope.answerList.splice(index, 1);
        }

        $scope.moveUp = function(index) {
            swapElements($scope.answerList, index, index - 1);
        }

        $scope.moveDown = function(index) {
            swapElements($scope.answerList, index, index + 1);
        }

        $scope.displayValue = function() {
            return _.map($scope.answerList, function(item) {
                return item.matchedName;
            }).join(' &larr; ');
        }

        $scope.toSubmit = function() {
            var list = [];
            angular.forEach($scope.answerList, function(item) {
                var obj = {};
                if (item.conceptName) {
                    obj["ConceptName"] = item.conceptName.uuid;
                } else if (item.concept) {
                    obj["Concept"] = item.concept.uuid;
                } else {
                    obj["NonCodedValue"] = item.nonCodedValue;
                }
                list.push(obj);
            });
            return list.length == 0 ? "" : angular.toJson(list);
        }

    }]);