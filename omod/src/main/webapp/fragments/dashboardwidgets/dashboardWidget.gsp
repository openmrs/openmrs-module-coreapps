<%
    ui.includeJavascript("coreapps", "fragments/openmrs-contrib-uicommons.bundle.min.js")
    ui.includeJavascript("coreapps", "fragments/dashboardWidgetsCommons.service.js")
    ui.includeJavascript("uicommons", "handlebars/handlebars.js")

    // Load chart library only when obsGraph widget is present
    if (config.widget.equals('obsgraph')) {
        ui.includeJavascript("coreapps", "fragments/Chart.min.js")
        ui.includeJavascript("coreapps", "fragments/angular-chart.min.js")
    }

    ui.includeJavascript("coreapps", "fragments/dashboardwidgets/" + config.widget + "/" + config.widget + ".controller.js")
    ui.includeJavascript("coreapps", "fragments/dashboardwidgets/" + config.widget + "/" + config.widget + ".component.js")
%>
<div id="coreapps-${config.id}" class="info-section">
    <div class="info-header">
        <i class="${config.icon}"></i>
        <h3>${ ui.message(config.label) }</h3>
    </div>
    <div class="info-body">
        <${config.widget} config="${config.json}"></${config.widget}>
    </div>
</div>

<script>
    angular.bootstrap("#coreapps-${config.id}", ["openmrs-contrib-dashboardwidgets.${config.widget}"])
</script>