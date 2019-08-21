var app = angular.module("manageConditionsApp",
    ['app.restfulServices', 'app.models', 'app.commonFunctionsFactory']);

app.controller("ManageConditionsController", ManageConditionsController);
ManageConditionsController.$inject = ['$scope', 'RestfulService', 'CommonFunctions'];

function ManageConditionsController($scope, RestfulService, CommonFunctions) {
    var self = this;
    $scope.conditions = [];
    $scope.conditionToBeRemoved =null;
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
            condition.endDate = new Date();
            self.saveCondition(condition);
        }

    self.removeCondition = self.removeCondition || function () {
            $scope.conditionToBeRemoved.voided = true;
            $scope.conditionToBeRemoved.endDate = new Date();
            self.saveCondition($scope.conditionToBeRemoved); 
            var dialog = document.getElementById("remove-condition-dialog");
            dialog.style.display = "none";           
        }

    self.redirectToEditCondition = self.redirectToEditCondition || function(baselink,condition) {
        window.location= baselink+'conditionUuid=' + JSON.stringify(condition.uuid)+'&';
    }
     
    self.conditionConfirmation = self.conditionConfirmation || function(condition) {
        var dialog = document.getElementById("remove-condition-dialog");
        dialog.style.display = "block";
        $scope.conditionToBeRemoved = condition;
        var setText = document.getElementById("removeConditionMessage");
        setText.innerHTML = "Are you sure you want to remove condition: <b>"+$scope.conditionToBeRemoved.concept.name+"</b> for this patient";
    }    
   
    self.cancelDeletion = self.cancelDeletion || function(){
        var dialog = document.getElementById("remove-condition-dialog");
        dialog.style.display = "none";
    }

    self.saveCondition = self.saveCondition || function (condition) {
            var conditions = [];
            conditions.push(condition);
            RestfulService.post('condition', conditions, function (data) {
                if(condition.voided == false){
                //emr.successAlert("conditionlist.updateCondition.success");
                    emr.successAlert("Condition Saved Successfully");
                }
                else{
                    emr.successAlert("Condition Deleted Successfully");
                }
            }, function (error) {
                //emr.errorAlert("conditionlist.updateCondition.error");
                emr.errorAlert("Error Saving condition");
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