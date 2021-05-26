(function () {
    var app = angular.module("conditionsApp",
        ['app.restfulServices', 'app.commonFunctionsFactory']);

    function ConditionsController($scope, RestfulService, CommonFunctions) {
        var self = this;
        var dialog = document.getElementById("remove-condition-dialog");
        var removeConditionMessage = document.getElementById("remove-condition-message");

        $scope.conditions = [];
        $scope.conditionToBeRemoved = null;
        $scope.tabs = ["ACTIVE", "INACTIVE"];

        // this is required inorder to initialize the RestfulService
        RestfulService.setBaseUrl('/' + OPENMRS_CONTEXT_PATH + '/ws/rest/emrapi');

        self.getConditions = self.getConditions || function (patientUuid) {
            if (typeof patientUuid === 'undefined' || patientUuid === null) {
                $scope.patientUuid = CommonFunctions.extractUrlArgs(window.location.search)['patientId'];
            } else {
                $scope.patientUuid = patientUuid;
            }

            if (typeof $scope.patientUuid !== 'undefined' &&  $scope.patientUuid !== null) {
                RestfulService.get('conditionhistory', {"patientUuid": $scope.patientUuid}, function (data) {
                    if (data && !Array.isArray(data)) {
                        console.log("Data not in expected format", data);
                        return;
                    }

                    $scope.conditions = data.flatMap(function (conditionHistory) {
                        if (conditionHistory.conditions && Array.isArray(conditionHistory.conditions)) {
                            return conditionHistory.conditions.filter(function (condition) {
                                return condition.voided === false;
                            });
                        }
                    })

                }, function (error) {
                    console.log(emr.message("coreapps.conditionui.getCondition.failure"), error);
                });
            }
        }

        self.activateCondition = self.activateCondition || function (condition) {
            condition.status = "ACTIVE";
            self.saveCondition(condition);
        }

        self.deactivateCondition = self.deactivateCondition || function (condition) {
            condition.status = "INACTIVE";
            self.saveCondition(condition);
        }

        self.removeCondition = self.removeCondition || function () {
            var condition = $scope.conditionToBeRemoved;
            condition.voided = true;
            self.saveCondition(condition);
            dialog.style.display = "none";
        }

        self.redirectToEditCondition = self.redirectToEditCondition || function (baselink, condition) {
            window.location = baselink + 'conditionUuid=' + encodeURIComponent(String(condition.uuid));
        }

        self.conditionConfirmation = self.conditionConfirmation || function (condition) {
            $scope.conditionToBeRemoved = condition;
            removeConditionMessage.innerHTML = emr.message("coreapps.conditionui.removeCondition.message").replace(
                "\{0\}", "<b>" + condition.concept.name +  "</b>");
            dialog.style.display = "block";
        }

        self.cancelDeletion = self.cancelDeletion || function () {
            $scope.conditionToBeRemoved = null;
            dialog.style.display = "none";
        }

        self.saveCondition = self.saveCondition || function (condition) {
            var idx = $scope.conditions.indexOf(condition);
            RestfulService.post('condition', [condition], function (data) {
                if (!condition.voided) {
                    emr.successAlert("coreapps.conditionui.updateCondition.success");
                } else {
                    emr.successAlert("coreapps.conditionui.removeCondition.success");
                }

                // pull out the version returned from the API
                if (data && Array.isArray(data) && !data[0].voided) {
                    $scope.conditions.splice(idx, 1, data[0]);
                } else {
                    $scope.conditions.splice(idx, 1);
                }
            }, function (error) {
                emr.errorAlert("coreapps.conditionui.updateCondition.failure");
                console.log(emr.message("coreapps.conditionui.updateCondition.failure"), error);
            });
        }

        // bind functions to scope
        $scope.removeCondition = self.removeCondition;
        $scope.cancelDeletion = self.cancelDeletion;
        $scope.activateCondition = self.activateCondition;
        $scope.deactivateCondition = self.deactivateCondition;
        $scope.formatDate = CommonFunctions.formatDate;
        $scope.getConditions = self.getConditions;
        $scope.conditionConfirmation = self.conditionConfirmation
        $scope.redirectToEditCondition = self.redirectToEditCondition;
    }

    app.controller('ConditionsController', ConditionsController);
    ConditionsController.$inject = ['$scope', 'RestfulService', 'CommonFunctions'];
})();
