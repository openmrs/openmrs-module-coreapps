var app = angular.module("manageConditionsApp",
    ['app.restfulServices', 'app.models', 'app.commonFunctionsFactory']);

app.controller("ManageConditionsController", ManageConditionsController);
ManageConditionsController.$inject = ['$scope', 'RestfulService', 'CommonFunctions'];

function ManageConditionsController($scope, RestfulService, CommonFunctions) {
    var self = this;
    $scope.conditions = [];
    $scope.tabs = ["ACTIVE", "INACTIVE"];

    // this is required inorder to initialize the Restangular service
    RestfulService.setBaseUrl('/' + OPENMRS_CONTEXT_PATH + '/ws/rest/emrapi');

    self.getConditions = self.getConditions || function (patientUuid) {
            if (patientUuid == null || patientUuid == undefined) {
                $scope.patientUuid = CommonFunctions.extractUrlArgs(window.location.search)['patientId'];
            } else {
                $scope.patientUuid = patientUuid;
            }

            if ($scope.patientUuid !== null && $scope.patientUuid !== undefined) {
                RestfulService.get('conditionhistory', {"patientUuid": $scope.patientUuid}, function (data) {
                    $scope.conditionHistoryList = data;
                }, function (error) {
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

    self.moveToHistoryCondition = self.moveToHistoryCondition || function (condition) {
            condition.status = "HISTORY_OF";
            self.saveCondition(condition);
        }

    self.removeCondition = self.removeCondition || function (condition) {
            condition.voided = true;
            condition.endDate = new Date();
            self.saveCondition(condition);
        }

    self.undoCondition = self.undoCondition || function (condition) {
            condition.voided = false;
            condition.endDate = null;
            self.saveCondition(condition);
        }

    self.saveCondition = self.saveCondition || function (condition) {
            var conditions = [];
            conditions.push(condition);
            RestfulService.post('condition', conditions, function (data) {
                //emr.successAlert("conditionlist.updateCondition.success");
                emr.successAlert("Condition Saved Successfully");
            }, function (error) {
                //emr.errorAlert("conditionlist.updateCondition.error");
                emr.errorAlert("Error Saving condition");
            });
        }

    // bind functions to scope
    $scope.removeCondition = self.removeCondition;
    $scope.activateCondition = self.activateCondition;
    $scope.deactivateCondition = self.deactivateCondition;
    $scope.moveToHistoryCondition = self.moveToHistoryCondition;
    $scope.undoCondition = self.undoCondition;
    $scope.formatDate = CommonFunctions.formatDate;
    $scope.strikeThrough = CommonFunctions.strikeThrough;
    $scope.getConditions = self.getConditions;
}