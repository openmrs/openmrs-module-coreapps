var app = angular.module("conditionApp",
    ['uicommons.widget.coded-or-free-text-answer', 'app.restfulServices', 'app.models', 'app.commonFunctionsFactory']);

app.controller("ConditionController", ConditionController);
ConditionController.$inject = ['$scope', 'RestfulService', 'ConditionModel', 'ConceptModel', 'CommonFunctions'];

function ConditionController($scope, RestfulService, ConditionModel, ConceptModel, CommonFunctions) {
    var self = this;

    $scope.patientUuid = null;
    $scope.condition = null;
    $scope.conditionUuid=null;
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
        if($scope.condition.status == INACTIVE_STATUS && $scope.condition.onSetDate > $scope.condition.endDate){
                emr.errorAlert("coreapps.conditionui.updateCondition.onSetDate.error");
        }
        else{
            var conditions = [];
            conditions.push($scope.condition);
            RestfulService.post('condition', conditions, function (data) {
                emr.successAlert("coreapps.conditionui.updateCondition.edited");
                window.location = '/' + OPENMRS_CONTEXT_PATH + '/coreapps/conditionlist/manageConditions.page?patientId=' + $scope.patientUuid + '&';
            }, function (error) {
                emr.errorAlert("coreapps.conditionui.updateCondition.error");
            });
        }
    }

    self.initCondition = self.initCondition || function () {
            $scope.patientUuid = CommonFunctions.extractUrlArgs(window.location.search)['patientId'];
            $scope.condition = new ConditionModel($scope.patientUuid)
            $scope.conditionUuid = CommonFunctions.extractUrlArgs(window.location.search)['conditionUuid'];
            self.getCondition($scope.conditionUuid);
        }

        self.getCondition = self.getCondition || function (conditionUuid) {
            if (conditionUuid == null || conditionUuid == undefined) {
                $scope.conditionUuid = CommonFunctions.extractUrlArgs(window.location.search)['conditionUUID'];
            } else {
                $scope.conditionUuid = conditionUuid;
            }
            $scope.conditionUuid =  $scope.conditionUuid.substring(3,$scope.conditionUuid.length-3);
            if ($scope.conditionUuid !== null && $scope.conditionUuid !== undefined) {
                RestfulService.get('condition', {"conditionUuid": $scope.conditionUuid}, function (data) {
                    $scope.condition = data[0];
                    self.displayCondition();
                }, function (error) {
                });
            }
        }

        self.displayCondition = self.displayCondition || function()
        {
            $scope.concept = {
                display: $scope.condition.concept.name,
                concept: {
                    display: $scope.condition.concept.name,
                },
                conceptName: {
                    display: $scope.condition.concept.name,
                }
            }

            var onsetDatePicker = angular.element(document.getElementsByName('conditionStartDate'))[0];     
            onsetDatePicker.value =  CommonFunctions.formatDate($scope.condition.onSetDate);

            var endDatePicker = angular.element(document.getElementsByName('conditionEndDate'))[0];      
            endDatePicker.value = CommonFunctions.formatDate(new Date()); 

            if($scope.condition.status == INACTIVE_STATUS){
                var endDatePicker = angular.element(document.getElementsByName('conditionEndDate'))[0];      
                endDatePicker.value = CommonFunctions.formatDate($scope.condition.endDate); 
            } 

            document.getElementById('conceptId-input').disabled = true;
            var groups = document.getElementsByClassName("group");
            groups[1].getElementsByTagName("input")[0].value = CommonFunctions.formatDate(new Date());  
            self.showEndDate();
        }


    self.showEndDate = self.showEndDate || function() {
        var groups = document.getElementsByClassName("group");
        if($scope.condition.status == INACTIVE_STATUS){
            groups[2].style.visibility = "visible";
            if ($scope.condition.endDate == null) {
                groups[2].getElementsByTagName("input")[0].value = CommonFunctions.formatDate(new Date()); 
            } else {
                groups[2].getElementsByTagName("input")[0].value = CommonFunctions.formatDate($scope.condition.endDate); 
            }
        }
        else{
            groups[2].style.visibility = "hidden";    
        }
    }

    self.validateCondition = self.validateCondition || function () {
            var concept = null ;
            if($scope.concept.concept.uuid)
            {
                concept = $scope.concept.concept;
            }
            else{
                concept = $scope.condition.concept;
            }
            if (concept !== null) {
                $scope.condition.concept = new ConceptModel(concept.uuid, concept.display);
            } else {
                $scope.condition.conditionNonCoded = "NON_CODED:" + $scope.concept.word;
            }

            $scope.condition.onSetDate = self.getSelectedDate();
            if($scope.condition.status == INACTIVE_STATUS)
            {
                $scope.condition.endDate = self.getEndDate();
            }
            self.saveCondition();
        }

    self.getSelectedDate = self.getSelectedDate || function () {
            var datePicker = angular.element(document.getElementsByName('conditionStartDate'))[0];
            return datePicker.value;
        }

    self.getEndDate = self.getEndDate || function() {
           var endDatePicker = angular.element(document.getElementsByName('conditionEndDate'))[0];
           return endDatePicker.value;
    }    

    self.initCondition();

    $scope.validateCondition = self.validateCondition;
    $scope.showEndDate = self.showEndDate;
    $scope.getEndDate = self.getEndDate;
    $scope.formatDate = CommonFunctions.formatDate;
    $scope.getCondition = self.getCondition;
    $scope.displayCondition = self.displayCondition;
}
