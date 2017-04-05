angular.module('coreapps.fragment.stickyNote').controller('stickyNoteCtrl', ['$scope', '$window', '$log',
	function($scope, $window, $log) {

		if ($window.coreapps.stickyNote.config.concept == null)  {
			$log.error("The concept passed in the config is 'null'. Check the server logs for more details")
		}

		$scope.config = {
			patient: $window.coreapps.stickyNote.config.patient.uuid,
			concept: $window.coreapps.stickyNote.config.concept.uuid,
			locale: $window.coreapps.stickyNote.config.locale,
			defaultLocale: $window.coreapps.stickyNote.config.defaultLocale
		};

		$scope.module = {
			'getProvider': function() {
				return "coreapps";
			},
			'getPath': function(openmrsContextPath) {
				return openmrsContextPath + '/' + this.getProvider();
			},
			'getPartialsPath': function(openmrsContextPath) {
				return openmrsContextPath + '/ms/uiframework/resource/' + this.getProvider() + '/partials';
			}
		}

	}])
;