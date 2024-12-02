(function(emr) {
    'use strict';
    var app = angular.module("conditionApp",
        ['uicommons.widget.coded-or-free-text-answer', 'app.restfulServices', 'app.commonFunctionsFactory']);

    function ConditionController($scope, RestfulService, CommonFunctions) {
        var self = this;
        var onsetDatePicker;
        var startDateElement;
        var endDatePicker;
        var endDateElement;
        {
            startDateElement = document.querySelector('[name = "conditionStartDate"]');
            if (startDateElement) {
                onsetDatePicker = jQuery(startDateElement.parentNode.querySelector('[id$="-wrapper"]'));
            }

            if (!onsetDatePicker) {
                onsetDatePicker = {};
            }

            endDateElement = document.querySelector('[name = "conditionEndDate"]');
            if (endDateElement) {
                endDatePicker = jQuery(endDateElement.parentNode.querySelector('[id$="-wrapper"]'));
            }

            if (!endDatePicker) {
                endDatePicker = {};
            }
        }

        $scope.patientUuid = null;
        $scope.condition = null;
        $scope.conditionUuid = null;
        $scope.title = '';


        const INACTIVE_STATUS = 'INACTIVE';
        const CONDITION_CUSTOM_REP = "custom:(uuid,display,clinicalStatus,voided,onsetDate,endDate,patient:(uuid,display),condition)";

        // this is required inorder to initialize the Restangular service
        RestfulService.setBaseUrl('/' + OPENMRS_CONTEXT_PATH + '/ws/rest/v1');

        /**
         * Perform POST save
         * PS: Make sure the 'Non Coded Uuid' setting (under Settings->Condition List) is set inorder to work with noncoded concepts,
         * the LOCALE needs to be set (most concepts have 'en')
         * @type {Function}
         */
        self.saveCondition = self.saveCondition || function () {
            if ($scope.condition.endDate && $scope.condition.onsetDate > $scope.condition.endDate) {
                emr.errorMessage("coreapps.conditionui.updateCondition.onSetDate.error");
            } else {
                var resource = "condition";
                if ( $scope.condition && $scope.condition.uuid ) {
                    resource += "/" + $scope.condition.uuid;
                }
                RestfulService.post(resource, $scope.condition, function (data) {
                    if ($scope.conditionUuid) {
                        emr.successAlert("coreapps.conditionui.updateCondition.edited");
                    } else {
                        emr.successAlert("coreapps.conditionui.updateCondition.added");
                    }

                    window.location = '/' + OPENMRS_CONTEXT_PATH + '/coreapps/conditionlist/manageConditions.page?patientId=' +
                        encodeURIComponent($scope.patientUuid);
                }, function (error) {
                    emr.errorAlert("coreapps.conditionui.updateCondition.error");
                    console.log(emr.message("coreapps.conditionui.updateCondition.error"), error);
                });
            }
        };

        self.initCondition = self.initCondition || function () {
            var urlArguments = CommonFunctions.extractUrlArgs(window.location.search);
            $scope.patientUuid = urlArguments['patientId'];
            $scope.condition = {
                patient: $scope.patientUuid,
                voided: false
            };
            $scope.conditionUuid = urlArguments['conditionUuid'];

            // if we have a conditionUuid, we're editing an existing condition; otherwise, we're creating a new one
            if ($scope.conditionUuid) {
                self.getCondition($scope.conditionUuid);
                $scope.title = emr.message("coreapps.conditionui.editCondition");
            } else {
                $scope.title = emr.message("coreapps.conditionui.addNewCondition");
            }

            // toggle whether the end date should show based on initial status
            self.showEndDate();
        };

        self.getCondition = self.getCondition || function (conditionUuid) {
            if (conditionUuid === null || conditionUuid === undefined) {
                $scope.conditionUuid = CommonFunctions.extractUrlArgs(window.location.search)['conditionUUID'];
            } else {
                $scope.conditionUuid = conditionUuid;
            }

            if ($scope.conditionUuid !== null && $scope.conditionUuid !== undefined) {
                RestfulService.get('condition' + "/" + $scope.conditionUuid,
                  {
                      "v" : CONDITION_CUSTOM_REP
                  },
                  function (data) {
                    if ( data ) {
                        $scope.condition = data;
                        self.displayCondition();
                    }
                }, function (error) {
                    console.log(emr.message("coreapps.conditionui.getCondition.failure"), error);
                });
            }
        };

        self.displayCondition = self.displayCondition || function () {
            if ($scope.condition && $scope.condition.condition && $scope.condition.condition.nonCoded) {
                $scope.concept = {
                    word: $scope.condition.condition.nonCoded
                };
            }
            else {
                $scope.concept = {
                    display: $scope.condition.condition.coded.name.name,
                    concept: {
                        uuid: $scope.condition.condition.coded.uuid,
                        display: $scope.condition.condition.coded.display,
                    },
                    conceptName: {
                        display: $scope.condition.condition.coded.name.name,
                    }
                }
            };

            if ($scope.condition.onsetDate) {
                onsetDatePicker.datetimepicker('setUTCDate', new Date($scope.condition.onsetDate));
            }

            if ($scope.condition.endDate) {
                endDatePicker.datetimepicker('setUTCDate', new Date($scope.condition.endDate));
            }

            document.getElementById('conceptId-input').disabled = true;
            self.showEndDate();
        };

        self.showEndDate = self.showEndDate || function () {
            var groups = document.getElementsByClassName("group");
            if ($scope.condition.clinicalStatus === INACTIVE_STATUS) {
                groups[2].style.visibility = "visible";
                if ($scope.condition.endDate) {
                    endDatePicker.datetimepicker('setUTCDate', new Date($scope.condition.endDate));
                }
            } else {
                $scope.condition.endDate = null;
                groups[2].style.visibility = "hidden";
            }
        };

        self.validateCondition = self.validateCondition || function () {

            if ($scope.concept && $scope.concept.concept && $scope.concept.concept.uuid) {
                $scope.condition.condition = {
                    coded: $scope.concept.concept.uuid
                };
            } else {
                $scope.condition.condition = {
                    nonCoded: $scope.concept.word
                };
            }

            $scope.condition.onsetDate = self.getSelectedDate();
            if ($scope.condition.clinicalStatus === INACTIVE_STATUS) {
                $scope.condition.endDate = self.getEndDate();
            }

            self.saveCondition();
        };

        self.getSelectedDate = self.getSelectedDate || function () {
            return startDateElement.value;
        };

        self.getEndDate = self.getEndDate || function () {
            return endDateElement.value;
        };

        self.initCondition();

        $scope.validateCondition = self.validateCondition;
        $scope.showEndDate = self.showEndDate;
        $scope.getEndDate = self.getEndDate;
        $scope.getCondition = self.getCondition;
        $scope.displayCondition = self.displayCondition;
    }

    ConditionController.$inject = ['$scope', 'RestfulService', 'CommonFunctions'];

    app.controller('ConditionController', ConditionController);
})(window.emr);
