<%
ui.decorateWith("appui", "standardEmrPage")

ui.includeJavascript("uicommons", "angular.min.js")
ui.includeJavascript("uicommons", "angular-resource.min.js")
ui.includeJavascript("uicommons", "angular-common.js")
ui.includeJavascript("uicommons", "angular-app.js")

ui.includeJavascript("coreapps", "attachments/app.js")
ui.includeJavascript("coreapps", "attachments/attachments.js")
%>

<!-- Angular widgets -->
<%
ui.includeFragment("coreapps", "attachments/dependenciesFileUpload")
ui.includeFragment("coreapps", "attachments/dependenciesThumbnail")
ui.includeFragment("coreapps", "attachments/dependenciesGallery")
%>

<script type="text/javascript">

  var breadcrumbs = [
  { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
  { label: "${ ui.escapeJs(ui.format(patient)) }" ,
  link: '${ ui.pageLink( "coreapps", "clinicianfacing/patient", [patientId: patient.id] ) }'},
  { label: "${ ui.message("coreapps.attachments.breadcrumbs.label") }"}
  ];

  // Getting the config from the Spring Java controller.
  if ("att" in window) {
    window.att.config = ${jsonConfig}
  } else {
    window.att = {
      config: ${jsonConfig}
    }
  }

</script>

<style>

  .att_main-section {
    border: 1px solid #EEE;
    background-color: #F9F9F9;
    margin: 15px 0 10px 0;
  }

</style>

${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient ]) }

<% if (context.hasPrivilege("App: attachments.attachments.page")) { %>

<div id="att-page-main">

  <div ng-controller="FileUploadCtrl" class="att_main-section">
    <att-file-upload config="config"></att-file-upload>
  </div>

  <div ng-controller="GalleryCtrl" class="att_main-section">
    <att-gallery obs-query="obsQuery" options="{canEdit:true}"></att-gallery>
  </div>

</div>


<script type="text/javascript">

  // manually bootstrap angular app, in case there are multiple angular apps on a page
  angular.bootstrap('#att-page-main', ['att.page.main']);
</script>

<% } %>
