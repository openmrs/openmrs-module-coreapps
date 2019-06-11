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
                emr.successAlert("Condition edited successfully")
                window.location = '/' + OPENMRS_CONTEXT_PATH + '/coreapps/conditionlist/manageConditions.page?patientId=' + $scope.patientUuid + '&';
            }, function (error) {
                //emr.errorAlert("conditionlist.updateCondition.error");
                emr.errorAlert("Error Saving condition");
            });
        }

    self.initCondition = self.initCondition || function () {
            $scope.patientUuid = CommonFunctions.extractUrlArgs(window.location.search)['patientId'];
            $scope.condition = new ConditionModel($scope.patientUuid)
            $scope.condition = CommonFunctions.extractUrlArgs(window.location.search)['condition'];
            $scope.condition = JSON.parse($scope.condition.split("%22").join("\"").split("%20").join(" "));

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
            onsetDatePicker.value = self.convertDate1($scope.condition.onSetDate);
          
            if($scope.condition.status=='INACTIVE'){
                var endDatePicker = angular.element(document.getElementsByName('conditionEndDate'))[0];      
                endDatePicker.value = self.convertDate1($scope.condition.endDate);
            } 

            var inputs = document.getElementsByTagName('input');
            inputs[1].value = self.convertDate2($scope.condition.onSetDate);  
            self.showEndDate();
        }

    self.showEndDate = self.showEndDate || function() {
        var inputs = document.getElementsByTagName('input');
        var groups = document.getElementsByClassName("group");
        if($scope.condition.status=='INACTIVE'){
            groups[2].style.visibility = "visible";
            inputs[3].value = self.convertDate2($scope.condition.endDate);
        }
        else{
            groups[2].style.visibility = "hidden";    
        }
    }

    self.validateCondition = self.validateCondition || function () {
            var concept ;
            if($scope.concept.concept.uuid)
            {
                concept=$scope.concept.concept;
            }
            else{
                concept=$scope.condition.concept;
            }
            if (concept !== null) {
                $scope.condition.concept = new ConceptModel(concept.uuid, concept.display);
            } else {
                // noncoded diagnosis
                $scope.condition.conditionNonCoded = "NON_CODED:" + $scope.concept.word;
            }

            $scope.condition.onSetDate = self.getSelectedDate();
            if($scope.condition.status=='INACTIVE')
            {
                $scope.condition.endDate = self.getEndDate();
            }
            self.saveCondition();
        }

    self.unselectStatus = self.unselectStatus || function () {
            $scope.condition.status = null;
        }

    self.getSelectedDate = self.getSelectedDate || function () {
            var datePicker = angular.element(document.getElementsByName('conditionStartDate'))[0];
            return datePicker.value;
        }
        
    self.getEndDate = self.getEndDate || function() {
           var endDatePicker = angular.element(document.getElementsByName('conditionEndDate'))[0];
           return endDatePicker.value;
    }    

    self.convertDate1 = self.convertDate1 || function(date_in_ms) {
            var date = new Date(date_in_ms);
            var y = date.getFullYear();
            var m = date.getMonth() + 1;    
            var d = date.getDate();    
            m = (m < 10) ? '0' + m : m; 
            d = (d < 10) ? '0' + d : d;
            return [y, m, d].join('-');
    }
        
    self.convertDate2 = self.convertDate2 || function(date_in_ms) {
           var d = new Date(date_in_ms);
           d = d.toUTCString();
           d = d.split(" ").slice(1,4).join(" ");
           return d; 
    }
    // init page
    self.initCondition();

    // bind functions to scope
    $scope.validateCondition = self.validateCondition;
    $scope.unselectStatus = self.unselectStatus;
    $scope.convertDate1 = self.convertDate1;
    $scope.convertDate2 = self.convertDate2;
    $scope.showEndDate = self.showEndDate;
    $scope.getEndDate = self.getEndDate;
}
