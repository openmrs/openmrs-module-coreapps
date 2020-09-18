var app = angular.module("manageConditionsApp",
    ['app.restfulServices', 'app.models', 'app.commonFunctionsFactory']);

app.controller("ManageConditionsController", ManageConditionsController);
ManageConditionsController.$inject = ['$scope', 'RestfulService', 'CommonFunctions'];

function ManageConditionsController($scope, RestfulService, CommonFunctions) {
    var self = this;
    $scope.conditions = [];
    $scope.conditionListToBeRemoved =null;
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
            var conditions = $scope.conditionListToBeRemoved;
            for(i=0;i<conditions.length;i++){
                conditions[i].voided = true;
                conditions[i].endDate = new Date();
                self.saveCondition(conditions[i]);
                var dialog = document.getElementById("remove-condition-dialog");
                dialog.style.display = "none";  
            }         
        }

    self.redirectToEditCondition = self.redirectToEditCondition || function(baselink,condition) {
        window.location= baselink+'conditionUuid=' + JSON.stringify(condition.uuid)+'&';
    }
     
    self.conditionConfirmation = self.conditionConfirmation || function(conditionList) {
        var dialog = document.getElementById("remove-condition-dialog");
        dialog.style.display = "block";
        $scope.conditionListToBeRemoved = conditionList;
        var setText = document.getElementById("removeConditionMessage");
        setText.innerHTML = "Are you sure you want to remove condition: <b>"+$scope.conditionListToBeRemoved.concept.name+"</b> for this patient";
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
                    emr.successAlert("coreapps.conditionui.updateCondition.success");
                }
                else{
                    emr.successAlert("coreapps.conditionui.updateCondition.delete");
                }
            }, function (error) {
                emr.errorAlert("coreapps.conditionui.updateCondition.error");
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