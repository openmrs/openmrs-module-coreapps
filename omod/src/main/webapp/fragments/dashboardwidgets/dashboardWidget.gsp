<%
    ui.includeJavascript("coreapps", "fragments/openmrs-contrib-uicommons.bundle.js")
    ui.includeJavascript("coreapps", "fragments/dashboardwidgets/" + config.widget + "/" + config.widget + ".component.js")
    ui.includeJavascript("coreapps", "fragments/dashboardwidgets/" + config.widget + "/" + config.widget + ".controller.js")
%>

<div id="coreapps-customLinks" class="info-section">
    <div class="info-header">
        <i class="${config.icon}"></i>
        <h3>${config.label}</h3>
    </div>
    <div class="info-body">

    </div>
</div>

<script>
    angular.bootstrap("#coreapps-customLinks", ["openmrs-contrib-uicommons"])
</script>