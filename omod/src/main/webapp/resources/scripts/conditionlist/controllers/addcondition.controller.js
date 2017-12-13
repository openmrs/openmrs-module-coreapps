var app = angular.module("conditionApp",
    ['uicommons.widget.coded-or-free-text-answer', 'app.restfulServices', 'app.models', 'app.commonFunctionsFactory']);

app.controller("ConditionController", ConditionController);
ConditionController.$inject = ['$scope', 'RestfulService', 'ConditionModel', 'ConceptModel', 'CommonFunctions'];

function ConditionController($scope, RestfulService, ConditionModel, ConceptModel, CommonFunctions) {
    var self = this;

    $scope.patientUuid = null;
    $scope.condition = null;

    // this is required inorder to initialize the Restangular service
    RestfulService.setBaseUrl('/' + OPENMRS_CONTEXT_PATH + '/ws/rest/emrapi');

    /**
     * Perform POST save
     * PS: Make sure the 'Non Coded Uuid' setting (under Settings->Condition List) is set inorder to work with noncoded concepts,
     * the LOCALE needs to be set (most concepts have 'en')
     * @type {Function}
     */
    self.saveCondition = self.saveCondition || function () {
            var conditions = [];
            conditions.push($scope.condition);
            RestfulService.post('condition', conditions, function (data) {
                //emr.successAlert("conditionlist.updateCondition.success"); Messages not being resolved
                emr.successAlert("Condition added successfully")
                window.location = '/' + OPENMRS_CONTEXT_PATH + '/coreapps/conditionlist/manageConditions.page?patientId=' + $scope.patientUuid + '&';
            }, function (error) {
                //emr.errorAlert("conditionlist.updateCondition.error");
                emr.errorAlert("Error Saving condition");
            });
        }

    self.initCondition = self.initCondition || function () {
            $scope.patientUuid = CommonFunctions.extractUrlArgs(window.location.search)['patientId'];
            $scope.condition = new ConditionModel($scope.patientUuid);
        }

    self.validateCondition = self.validateCondition || function () {
            var concept = $scope.concept.concept;
            if (concept !== null) {
                $scope.condition.concept = new ConceptModel(concept.uuid, concept.display);
            } else {
                // noncoded diagnosis
                $scope.condition.conditionNonCoded = "NON_CODED:" + $scope.concept.word;
            }

            $scope.condition.onSetDate = self.getSelectedDate();

            self.saveCondition();
        }

    self.unselectStatus = self.unselectStatus || function () {
            $scope.condition.status = null;
        }

    self.getSelectedDate = self.getSelectedDate || function () {
            var datePicker = angular.element(document.getElementsByName('conditionStartDate'))[0];
            return datePicker.value;
        }

    // init page
    self.initCondition();

    // bind functions to scope
    $scope.validateCondition = self.validateCondition;
    $scope.unselectStatus = self.unselectStatus;
}
