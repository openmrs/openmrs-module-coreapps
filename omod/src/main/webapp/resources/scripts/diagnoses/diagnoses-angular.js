var app = angular.module('diagnoses', [])
    .directive('autocomplete', function($compile) {
        return function(scope, element, attrs) {
            // I don't know how to use an angular template programmatically, so use an underscore template instead. :-(
            var itemFormatter = _.template($('#' + attrs.itemformatter).html());
            element.autocomplete({
                source: emr.fragmentActionLink("coreapps", "diagnoses", "search"),
                response: function(event, ui) {
                    var query = event.target.value.toLowerCase();
                    var items = ui.content;
                    // remove any already-selected concepts, and look for exact matches by name/code
                    var exactMatch = false;
                    for (var i = items.length - 1; i >= 0; --i) {
                        items[i] = diagnoses.CodedOrFreeTextConceptAnswer(items[i]);
                        if (!exactMatch && items[i].exactlyMatchesQuery(query)) {
                            exactMatch = true;
                        }
                        if (scope.encounterDiagnoses.diagnosisWithConceptId(items[i].conceptId)) {
                            items.splice(i, 1);
                        }
                    }
                    if (!exactMatch) {
                        items.push(diagnoses.CodedOrFreeTextConceptAnswer(element.val()))
                    }
                },
                focus: function( event, ui ) {
                    element.val(ui.item.matchedName);
                    return false;
                },
                select: function( event, ui ) {
                    scope.$apply(function() {
                        scope.encounterDiagnoses.addDiagnosis(diagnoses.Diagnosis(ui.item));
                        element.val('');
                    });
                    return false;
                }
            })
            .data( "autocomplete" )._renderItem = function( ul, item ) {
                var formatted = itemFormatter({item: item});
                return jq('<li>').append('<a>' + formatted + '</a>').appendTo(ul);
            };
        }
    })

    .controller('DiagnosesController', [ '$scope',
        function DiagnosesController($scope) {

            $scope.encounterDiagnoses = diagnoses.EncounterDiagnoses();
            $scope.priorDiagnoses = diagnoses.EncounterDiagnoses();

            $scope.addPriorDiagnoses = function() {
                $scope.encounterDiagnoses.addDiagnoses(angular.copy($scope.priorDiagnoses.getDiagnoses()));
            }

            $scope.removeDiagnosis = function(diagnosis) {
                $scope.encounterDiagnoses.removeDiagnosis(diagnosis);
            };

            $scope.valueToSubmit = function() {
                return "[" + _.map($scope.encounterDiagnoses.diagnoses, function(d) {
                    return d.valueToSubmit();
                }).join(", ") + "]";
            };
        }
    ]);

