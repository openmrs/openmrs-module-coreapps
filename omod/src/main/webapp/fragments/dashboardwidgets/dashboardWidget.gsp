<%
    ui.includeJavascript("coreapps", "fragments/openmrs-contrib-uicommons.bundle.js")
    ui.includeJavascript("coreapps", "fragments/dashboardwidgets/" + config.widget + "/" + config.widget + ".controller.js")
    ui.includeJavascript("coreapps", "fragments/dashboardwidgets/" + config.widget + "/" + config.widget + ".component.js")
%>

<div id="coreapps-${config.id}" class="info-section">
    <div class="info-header">
        <i class="${config.icon}"></i>
        <h3>${config.label}</h3>
    </div>
    <div class="info-body">
        <${config.widget} config="${config.json}"></${config.widget}>
    </div>
</div>

<script>
    angular.bootstrap("#coreapps-${config.id}", ["openmrs-contrib-dashboardwidgets.${config.widget}"])
</script>