<%
  ui.includeJavascript("uicommons", "angular.min.js")
  ui.includeJavascript("uicommons", "angular-resource.min.js")
  ui.includeJavascript("uicommons", "angular-common.js")
  ui.includeJavascript("uicommons", "angular-app.js")

  ui.includeJavascript("coreapps", "attachments/app.js")
  ui.includeJavascript("coreapps", "attachments/dashboardWidget.js")
%>

<!-- Angular widgets -->
<%
  ui.includeFragment("coreapps", "attachments/dependenciesThumbnail")
  ui.includeFragment("coreapps", "attachments/dependenciesGallery")
%>

<script type="text/javascript">

  window.config = ${jsonConfig}; // Getting the config from the Spring Java controller.

</script>

<style>
</style>

<div class="info-section">
	<div class="info-header">
		<i class="icon-paper-clip"></i>
		<h3>${ ui.message("coreapps.attachments.visitactions.label").toUpperCase() }</h3>

<% if (context.hasPrivilege("App: attachments.attachments.page")) { %>

    <a href="${ ui.pageLink("coreapps", "attachments/attachments", [patient: patient.patient.uuid, patientId: patient.id, returnUrl: ui.thisUrl()]) }">
      <i class="icon-share-alt edit-action right" title="${ ui.message("coreapps.edit") }"></i>
    </a>

	</div>
	<div class="info-body">

		<div id="att-fragment-dashboard-widget" ng-controller="DashboardWidgetCtrl">
			<att-gallery obs-query="obsQuery"></att-gallery>
		</div>

	</div>

<% } else { %>

  </div>

<% } %>

</div>

<script type="text/javascript">
  // manually bootstrap angular app, in case there are multiple angular apps on a page
  angular.bootstrap('#att-fragment-dashboard-widget', ['att.fragment.dashboardWidget']);
</script>