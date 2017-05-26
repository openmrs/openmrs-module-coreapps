<%
    ui.includeJavascript("coreapps", "web/coreapps.vendor.js")
    ui.includeJavascript("coreapps", "web/coreapps.dashboardwidgets.js")
    ui.includeJavascript("uicommons", "handlebars/handlebars.js")
%>

<% config.programJson.eachWithIndex { json, idx -> %>

    <div id="coreapps-${config.id}-${idx}" class="info-section">
        <div class="info-header">
            <i class="${config.icon}"></i>
            <h3>${ ui.message(config.label) }</h3>
        </div>
        <div class="info-body">
            <programstatus config="${json}"></programstatus>
        </div>
    </div>

    <script>
        angular.bootstrap("#coreapps-${config.id}-${idx}", ["openmrs-contrib-dashboardwidgets.programstatus"])
    </script>
<% } %>

