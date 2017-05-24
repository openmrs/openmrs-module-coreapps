<%
    ui.includeJavascript("coreapps", "web/coreapps.vendor.js")
    ui.includeJavascript("coreapps", "web/coreapps.dashboardwidgets.js")
    ui.includeJavascript("uicommons", "handlebars/handlebars.js")
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