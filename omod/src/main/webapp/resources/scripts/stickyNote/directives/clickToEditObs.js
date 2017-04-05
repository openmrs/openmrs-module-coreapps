angular.module('coreapps.fragment.stickyNote')

.directive('coreappsClickToEditObs', ['ObsService', 'Obs', 'ngDialog', function(obsService, Obs, ngDialog){
  return {
    scope: {
      module: '=',
      config: "="
    },
    restrict: 'E',
    // The 'templateUrl:' field can not be set using a variable from the $scope (eg: $scope.module.getPath()) because the field is not yet available in the $scope,
    // therefore we use 'template:' with 'ng-include' to display the 'templateUrl' as soon as it is available
    template: "<div ng-include='templateUrl'></div>",
    controller: function($scope) {

      $scope.templateUrl = '/' + $scope.module.getPartialsPath(OPENMRS_CONTEXT_PATH) + '/clickToEditObs.html'

      // Loading i18n messages
      var msgCodes = [
      $scope.module.getProvider() + ".clickToEditObs.empty",
      $scope.module.getProvider() + ".clickToEditObs.placeholder",
      $scope.module.getProvider() + ".clickToEditObs.saveError",
      $scope.module.getProvider() + ".clickToEditObs.loadError",
      $scope.module.getProvider() + ".clickToEditObs.deleteError",
      $scope.module.getProvider() + ".clickToEditObs.saveSuccess",
      $scope.module.getProvider() + ".clickToEditObs.deleteSuccess",
      $scope.module.getProvider() + ".clickToEditObs.delete.title",
      $scope.module.getProvider() + ".clickToEditObs.delete.confirm",
      "coreapps.yes",
      "coreapps.no"
      ]

      emr.loadMessages(msgCodes.toString(), function(msgs) {
        $scope.msgs = msgs;
      });

      // Initialize vars
      $scope.note = {
        text: "",
      }
      $scope.isEditing = false;
      $scope.isEmpty = true;
      $scope.showActions = false;
      var draftObs = {};
      var obsArray = [];
      moment.locale($scope.config.locale);
    
      var loading = function (isLoading) {
        $scope.isLoading = isLoading;
      }

      var loadNonVoidedObs = function() {
        loading(true);
        obsService.getObs({
          v: 'full',
          patient: $scope.config.patient,
          concept: $scope.config.concept
        }).then(function(results) {
          loading(false);
          obsArray = results
          if (obsArray.length !=0) {
            $scope.isEmpty = false;
            // Taking the first value if there is more than one obs (although this shouldn't happen)
            $scope.note.text = obsArray[0].value;
            $scope.note.createdBy = obsArray[0].auditInfo.creator.display;
            $scope.note.creationDate = getPrettyDate(obsArray[0].obsDatetime);
          } else {
            $scope.isEmpty = true;
          }
          initDraftObs();
        }, function(error) {
          loading(false);
          emr.errorMessage($scope.module.getProvider() + ".clickToEditObs.loadError");
        })
      }

      var getPrettyDate = function (obsDate) {
        var timeFormat = "DD MMM YY [at] h:mm A";
        var now = moment();
        var obsDate = moment(obsDate);

        if ( (obsDate.year() === now.year() ) && ( now.dayOfYear() - obsDate.dayOfYear() ) < 7 ) {
            return obsDate.calendar()
        }
        return obsDate.format(timeFormat);
      }

      // Configure the new obs to be saved
      var initDraftObs = function() {
        draftObs = {};
        draftObs.person = $scope.config.patient;
        draftObs.concept = $scope.config.concept;
        draftObs.value = $scope.note.text;
        if (obsArray.length != 0) {
          draftObs.uuid = obsArray[0].uuid;
        }
      }

      $scope.toggleEditMode = function() {
        if(obsArray.length != 0) {
          $scope.isEditing = !$scope.isEditing;
          if ($scope.showActions == true) {
            $scope.showActions = !$scope.showActions	
          }
        }
      }

      $scope.updateObs = function() {
        loading(true);
        // Do not update the obs if the previous text value (draftObs.value) is the exact same as the newly entered text
        if (draftObs.value == $scope.note.text) {
          loadNonVoidedObs();
          return;
        }
        var obsArray = [draftObs]
        draftObs.value = $scope.note.text;
        // Empty text value is considered as deleting the note
        if (draftObs.value == "") {
          $scope.confirmDelete();
          loadNonVoidedObs();
        } else {
          $scope.isEmpty = false;
          draftObs.obsDatetime = moment().format("YYYY-MM-DD[T]HH:mm:ss.SSSZZ"); // This is the format to send a timezoned datetime to the REST service (ex: "2016-12-25T19:02:34.232+0700")
          Obs.save(draftObs).$promise.then(function (results) {
            loadNonVoidedObs();
            emr.successMessage($scope.module.getProvider() + ".clickToEditObs.saveSuccess");
          }, function(error) {
            loading(false);
            emr.errorMessage($scope.module.getProvider() + ".clickToEditObs.saveError");
          });				
        }
      } 		

      $scope.confirmDelete = function() {
        // https://github.com/likeastore/ngDialog/blob/master/README.md
        ngDialog.open({
          template: '/' + $scope.module.getPartialsPath(OPENMRS_CONTEXT_PATH) + '/deleteDialog.html',
          scope: $scope,
          controller: ['$scope', function(dialogScope) {
            dialogScope.confirm = function() {
              $scope.deleteObs(dialogScope);
            }
          }]
        });
      }

      $scope.deleteObs = function(dialogScope) {
        loading(true);
        $scope.note.text = "";
        $scope.isEmpty = true;
        dialogScope.closeThisDialog();
        Obs.delete(draftObs).$promise.then(function (results) {
          loadNonVoidedObs();
          dialogScope.closeThisDialog();
          emr.successMessage($scope.module.getProvider() + ".clickToEditObs.deleteSuccess");
        }, function(error) {
          dialogScope.closeThisDialog();
          emr.errorMessage($scope.module.getProvider() + ".clickToEditObs.deleteError");
        });
      }

      loadNonVoidedObs();
      initDraftObs();
    }
  };
}])
.run(function(editableOptions) {
  // configure the 'xeditable' theme
  editableOptions.theme = 'bs2';
})
;