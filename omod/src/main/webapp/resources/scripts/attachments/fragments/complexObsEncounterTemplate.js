angular.module('att.fragment.encounterTemplate').controller(
        'EncounterTemplateCtrl',
        [
                '$scope',
                '$compile',
                function($scope, $compile) {

                    $scope.init = function() {

                        $(document).on(
                                'click',
                                '.att_view-details.collapsed',
                                function(event) {
                                    var jqTarget = $(event.currentTarget);
                                    var uuid = jqTarget.data("encounter-uuid");
                                    var displayWithHtmlForm = jqTarget.data("encounter-form")
                                            && jqTarget.data("display-with-html-form");
                                    var dataTarget = jqTarget.data("target");
                                    var encounterTypeUuid = jqTarget.data("encounter-type-uuid");
                                    getEncounterDetails(uuid, dataTarget, "complexObsEncounterTemplate");
                                });

                        //We cannot assign it here due to Jasmine failure: 
                        //net.sourceforge.htmlunit.corejs.javascript.EcmaError: TypeError: Cannot call method "replace" of undefined
                        var detailsTemplates = {};

                        function getEncounterDetails(uuid, dataTarget, displayTemplateId) {
                            if (!detailsTemplates[displayTemplateId]) {
                                detailsTemplates[displayTemplateId] = _.template($('#' + displayTemplateId).html());
                            }
                            var displayTemplate = detailsTemplates[displayTemplateId];
                            var encounterDetailsSection = $(dataTarget + ' .encounter-summary-container');

                            var encounter = {};
                            encounter.uuid = uuid;
                            encounterStringified = JSON.stringify(encounter);
                            encounterStringified = encounterStringified.replace(/\"([^(\")"]+)\":/g, "$1:");

                            // Delegating the rest to an Angular directive.
                            var htmlContent = "<att-complex-obs-encounter encounter='" + encounterStringified
                                    + "'></att-complex-obs-encounter>";
                            encounterDetailsSection.html($compile(htmlContent)($scope));
                            $scope.$apply();
                        }

                    } // init()
                } ]);