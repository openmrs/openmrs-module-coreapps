(function () {
    var app = angular.module("conditionsApp",
        ['app.restfulServices', 'app.commonFunctionsFactory']);

    function ConditionsController($scope, RestfulService, CommonFunctions) {
        var self = this;
        var dialog = document.getElementById("remove-condition-dialog");
        var removeConditionMessage = document.getElementById("remove-condition-message");
        let CONDITION_CUSTOM_REP = "custom:(uuid,display,clinicalStatus,voided,onsetDate,endDate,patient:(uuid,display),condition)";

        $scope.conditions = [];
        $scope.conditionToBeRemoved = null;
        $scope.tabs = ["ACTIVE", "INACTIVE"];
        $scope.config = {};


        // this is required inorder to initialize the RestfulService
        RestfulService.setBaseUrl('/' + OPENMRS_CONTEXT_PATH + '/ws/rest/v1');

        self.getConditions = self.getConditions || function (patientUuid) {
            if (typeof patientUuid === 'undefined' || patientUuid === null) {
                $scope.patientUuid = CommonFunctions.extractUrlArgs(window.location.search)['patientId'];
            } else {
                $scope.patientUuid = patientUuid;
            }

            if (typeof $scope.patientUuid !== 'undefined' &&  $scope.patientUuid !== null) {
                RestfulService.get('condition', {
                    "patientUuid": $scope.patientUuid,
                    "includeInactive": "true",
                     "v" : CONDITION_CUSTOM_REP
                  },
                  function (data) {
                    if (data && !Array.isArray(data.results)) {
                        console.log("Data not in expected format", data);
                        return;
                    }
                    $scope.conditions = data?.results.filter((condition) => condition.voided === false);
                }, function (error) {
                    console.log(emr.message("coreapps.conditionui.getCondition.failure"), error);
                });
            }
        };

        self.activateCondition = self.activateCondition || function (condition) {
            condition.clinicalStatus = "ACTIVE";
            condition.endDate = null;
            self.saveCondition(condition);
        };

        self.deactivateCondition = self.deactivateCondition || function (condition) {
            condition.clinicalStatus = "INACTIVE";
            self.saveCondition(condition);
        };

        self.removeCondition = self.removeCondition || function () {
            var condition = $scope.conditionToBeRemoved;
            condition.voided = true;
            self.deleteCondition(condition);
            dialog.style.display = "none";
        };

        self.redirectToEditCondition = self.redirectToEditCondition || function (baselink, condition) {
            window.location = baselink + 'conditionUuid=' + encodeURIComponent(String(condition.uuid));
        };

        self.conditionConfirmation = self.conditionConfirmation || function (condition) {
            $scope.conditionToBeRemoved = condition;
            removeConditionMessage.innerHTML = emr.message("coreapps.conditionui.removeCondition.message").replace(
                "\{0\}", "<b>" + condition.display +  "</b>");
            dialog.style.display = "block";
        };

        self.cancelDeletion = self.cancelDeletion || function () {
            $scope.conditionToBeRemoved = null;
            dialog.style.display = "none";
        };

        function formatRestConditionForPost(condition) {
            let restCondition = null;
            if (condition) {
                restCondition = structuredClone(condition);
                delete restCondition.patient;
                delete restCondition.condition;

                if (condition?.patient?.uuid) {
                    restCondition.patient = {
                        uuid: condition.patient.uuid
                    };
                }
                if (condition?.condition?.coded?.uuid) {
                    restCondition.condition = {
                        coded: condition.condition.coded.uuid
                    };
                }
            }
            return restCondition;
        }
        self.saveCondition = self.saveCondition || function (condition) {
            var idx = $scope.conditions.indexOf(condition);
            let resource = "condition";
            if ( condition.uuid ) {
                resource += "/" + condition.uuid;
            }
            let restObj = formatRestConditionForPost(condition);
            RestfulService.post(resource, restObj, function (data) {
                if (!condition.voided) {
                    emr.successAlert("coreapps.conditionui.updateCondition.success");
                } else {
                    emr.successAlert("coreapps.conditionui.removeCondition.success");
                }
                self.getConditions();
            }, function (error) {
                emr.errorAlert("coreapps.conditionui.updateCondition.failure");
                console.log(emr.message("coreapps.conditionui.updateCondition.failure"), error);
            });
        };

        self.deleteCondition = self.deleteCondition || function (condition) {
            let resource = "condition";
            if ( condition.uuid ) {
                resource += "/" + condition.uuid;
            }
            RestfulService.delete(resource, function (response) {
                if (response?.status === 204) {
                    emr.successAlert("coreapps.conditionui.removeCondition.success");
                } else {
                    emr.errorAlert("coreapps.conditionui.updateCondition.failure");
                }
                self.getConditions();
            }, function (error) {
                emr.errorAlert("coreapps.conditionui.updateCondition.failure");
                console.log(emr.message("coreapps.conditionui.updateCondition.failure"), error);
            });
        };

        self.formatDate = self.formatDate || function(date) {
            if (typeof date !== 'undefined' && date !== null) {
                var locale = $scope.config.locale ? $scope.config.locale : 'en';
                var format = $scope.config.format ? $scope.config.format : 'DD MMM YYYY';
                return moment(date.substring(0, 10)).locale(locale).format(format);
            }
        };

        self.formatCondition = self.formatCondition || function(condition) {
            if (condition.condition?.nonCoded) {
                return '"' + condition.condition.nonCoded + '"';
            }
            return condition.condition?.coded?.name?.name;
        };

        // bind functions to scope
        $scope.removeCondition = self.removeCondition;
        $scope.cancelDeletion = self.cancelDeletion;
        $scope.activateCondition = self.activateCondition;
        $scope.deactivateCondition = self.deactivateCondition;
        $scope.formatDate = self.formatDate;
        $scope.formatCondition = self.formatCondition;
        $scope.getConditions = self.getConditions;
        $scope.conditionConfirmation = self.conditionConfirmation;
        $scope.redirectToEditCondition = self.redirectToEditCondition;
    }

    app.controller('ConditionsController', ConditionsController);
    ConditionsController.$inject = ['$scope', 'RestfulService', 'CommonFunctions'];
})();
