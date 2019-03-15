<%
    ui.includeJavascript("coreapps", "web/coreapps.vendor.js")
    ui.includeJavascript("coreapps", "web/coreapps.dashboardwidgets.js")
    ui.includeJavascript("uicommons", "handlebars/handlebars.js")

    def editIcon = config.editIcon ?: "icon-share-alt"
%>
<div id="coreapps-${config.id}" class="info-section openmrs-contrib-dashboardwidgets">
    <div class="info-header">
        <i class="${config.icon}"></i>
        <h3>${ ui.message(config.label) }</h3>
        <% if (editIcon && config.detailsUrl) { %>
            <i class="${editIcon} edit-action right" title="${ ui.message("coreapps.edit") }"
               onclick="location.href='${ ui.urlBind("/" + contextPath + "/" + config.detailsUrl, [ "patient.uuid": config.patientUuid ]) }';"></i>
        <% } %>
    </div>
    <div class="info-body">
        <${config.widget} config="${config.json}"></${config.widget}>
    </div>
</div>
