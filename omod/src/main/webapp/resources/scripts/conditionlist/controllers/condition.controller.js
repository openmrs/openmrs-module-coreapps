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

        // this is required inorder to initialize the Restangular service
        RestfulService.setBaseUrl('/' + OPENMRS_CONTEXT_PATH + '/ws/rest/emrapi');

        /**
         * Perform POST save
         * PS: Make sure the 'Non Coded Uuid' setting (under Settings->Condition List) is set inorder to work with noncoded concepts,
         * the LOCALE needs to be set (most concepts have 'en')
         * @type {Function}
         */
        self.saveCondition = self.saveCondition || function () {
            if ($scope.condition.endDate && $scope.condition.onSetDate > $scope.condition.endDate) {
                emr.errorMessage("coreapps.conditionui.updateCondition.onSetDate.error");
            } else {
                RestfulService.post('condition', [$scope.condition], function (data) {
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
            $scope.condition = {patientUuid: $scope.patientUuid, voided: false};
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
                RestfulService.get('condition', {"conditionUuid": $scope.conditionUuid}, function (data) {
                    if (data && Array.isArray(data)) {
                        $scope.condition = data[0];
                        self.displayCondition();
                    }
                }, function (error) {
                    console.log(emr.message("coreapps.conditionui.getCondition.failure"), error);
                });
            }
        };

        self.displayCondition = self.displayCondition || function () {
            if ($scope.condition.conditionNonCoded) {
                $scope.concept = {
                    word: $scope.condition.conditionNonCoded
                };
            }
            else {
                $scope.concept = {
                    display: $scope.condition.concept.name,
                    concept: {
                        uuid: $scope.condition.concept.uuid,
                        display: $scope.condition.concept.name,
                    },
                    conceptName: {
                        display: $scope.condition.concept.name,
                    }
                }
            };

            if ($scope.condition.onSetDate) {
                onsetDatePicker.datetimepicker('setUTCDate', new Date($scope.condition.onSetDate));
            }

            if ($scope.condition.endDate) {
                endDatePicker.datetimepicker('setUTCDate', new Date($scope.condition.endDate));
            }

            document.getElementById('conceptId-input').disabled = true;
            self.showEndDate();
        };

        self.showEndDate = self.showEndDate || function () {
            var groups = document.getElementsByClassName("group");
            if ($scope.condition.status === INACTIVE_STATUS) {
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
            var concept;
            if ($scope.concept.concept && $scope.concept.concept.uuid) {
                concept = $scope.concept.concept;
            } else {
                concept = $scope.condition.concept;
            }

            if (concept) {
                $scope.condition.concept = {uuid: concept.uuid, name: concept.display};
            } else {
                $scope.condition.conditionNonCoded = $scope.concept.word;
            }

            $scope.condition.onSetDate = self.getSelectedDate();
            if ($scope.condition.status === INACTIVE_STATUS) {
                $scope.condition.endDate = self.getEndDate();
            }

            self.saveCondition();
        };

        self.getSelectedDate = self.getSelectedDate || function () {
            return startDateElement.value ? self.toUTCDate(startDateElement.value) : null;
        };

        self.getEndDate = self.getEndDate || function () {
            return endDateElement.value ? self.toUTCDate(endDateElement.value) : null;
        };

        self.toUTCDate = function(dateString) {
            var utcDate = null;
            if (dateString) {
                utcDate = new Date(dateString);
                utcDate.setTime(utcDate.getTime() + utcDate.getTimezoneOffset() * 60 * 1000);
            }
            return utcDate;
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
