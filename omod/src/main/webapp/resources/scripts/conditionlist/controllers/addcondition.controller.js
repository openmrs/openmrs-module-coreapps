var app = angular.module("conditionApp",
    ['uicommons.widget.coded-or-free-text-answer', 'app.restfulServices', 'app.models', 'app.commonFunctionsFactory']);

app.controller("ConditionController", ConditionController);
ConditionController.$inject = ['$scope', 'RestfulService', 'ConditionModel', 'ConceptModel', 'CommonFunctions'];

function ConditionController($scope, RestfulService, ConditionModel, ConceptModel, CommonFunctions) {
    var self = this;

    $scope.patientUuid = null;
    $scope.condition = null;
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
            emr.errorAlert("coreapps.conditionui.updateCondition.error");
        }
        else{
            var conditions = [];
            conditions.push($scope.condition);
            RestfulService.post('condition', conditions, function (data) {
                emr.successAlert("coreapps.conditionui.updateCondition.added");
                window.location = '/' + OPENMRS_CONTEXT_PATH + '/coreapps/conditionlist/manageConditions.page?patientId=' + $scope.patientUuid + '&';
            }, function (error) {
                emr.errorAlert("coreapps.conditionui.updateCondition.error");
            });
        }
    }

    self.initCondition = self.initCondition || function () {
            $scope.patientUuid = CommonFunctions.extractUrlArgs(window.location.search)['patientId'];
            $scope.condition = new ConditionModel($scope.patientUuid);
            self.showEndDate();
        }

    self.validateCondition = self.validateCondition || function () {
        var concept ;
        if($scope.concept.concept.uuid) {
            concept = $scope.concept.concept;
        } else {
            concept = $scope.condition.concept;
        }

        if (concept !== null) {
            $scope.condition.concept = new ConceptModel(concept.uuid, concept.display);
        } else {
            $scope.condition.conditionNonCoded = "NON_CODED:" + $scope.concept.word;
        }

        $scope.condition.onSetDate = self.getSelectedDate();

        if($scope.condition.status == INACTIVE_STATUS) {
            $scope.condition.endDate = self.getEndDate();
        }
        self.saveCondition();
    }

    self.showEndDate = self.showEndDate || function() {
        var groups = document.getElementsByClassName("group");
        if($scope.condition.status == INACTIVE_STATUS){
            groups[2].style.visibility = "visible";
        }
        else{
            groups[2].style.visibility = "hidden";    
        }
     }        

    self.getSelectedDate = self.getSelectedDate || function () {
            var datePicker = angular.element(document.getElementsByName('conditionStartDate'))[0];
            return datePicker.value;
        }

    self.getEndDate = self.getEndDate || function() {
            var endDatePicker = angular.element(document.getElementsByName('conditionEndDate'))[0];
            return endDatePicker.value;
        } 

    // init page
    self.initCondition();

    // bind functions to scope
    $scope.validateCondition = self.validateCondition;
    $scope.showEndDate = self.showEndDate;
    $scope.getEndDate = self.getEndDate;
}
