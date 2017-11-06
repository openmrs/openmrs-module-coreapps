<%
	ui.includeJavascript("uicommons", "angular.min.js")
	ui.includeJavascript("uicommons", "angular-resource.min.js")
	ui.includeJavascript("uicommons", "angular-common.js")
	ui.includeJavascript("uicommons", "angular-app.js")
	
	ui.includeJavascript("uicommons", "services/obsService.js")
	ui.includeJavascript("uicommons", "services/encounterService.js")
	
	ui.includeJavascript("uicommons", "ngDialog/ngDialog.js")
	ui.includeCss("uicommons", "ngDialog/ngDialog.min.css")
	
	ui.includeJavascript("uicommons", "moment.min.js")
	
%>

<%
	ui.includeJavascript("coreapps", "stickyNote/resources/xeditable.min.js")

	ui.includeJavascript("coreapps", "stickyNote/app.js")
	ui.includeJavascript("coreapps", "stickyNote/controllers/stickyNoteCtrl.js")
	
	ui.includeCss("coreapps", "stickynote/stickyNoteIcon.css")
	ui.includeJavascript("coreapps", "stickyNote/directives/clickToEditObs.js")
%>
<script type="text/javascript">
	var strConfig = '${ui.encodeJavaScript(jsonConfig)}';
	var jsonConfig = JSON.parse(strConfig);
	// check if 'coreapps' global variable already exists (possibly set somewhere else in the module)
	if ("coreapps" in window) {
		window.coreapps.stickyNote = {
			config: jsonConfig
		}
	} else {
		window.coreapps = {
			stickyNote: {
				config: jsonConfig
			}
		}
	}

	angular.element(document).ready(function() {
		angular.bootstrap('#coreapps-fragment-stickyNote', [ 'coreapps.fragment.stickyNote' ]);
	});
</script>

<style type="text/css">
	.stickyNote {
		display: inline-block;
		margin-top: 4px;
		max-width: 60%;
		min-width: 50%;
	}
	.active-visit-started-at-message {
		vertical-align: top !important;
	}

	.active-visit-message {
		vertical-align: top !important;
	}

</style>

<div class="stickyNote">
	<div id="coreapps-fragment-stickyNote" ng-controller="stickyNoteCtrl">
		<coreapps-click-to-edit-obs config="config" module="module"></coreapps-click-to-edit-obs>
	</div>
</div>
