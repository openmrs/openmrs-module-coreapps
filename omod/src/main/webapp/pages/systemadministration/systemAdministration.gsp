<%
    ui.decorateWith("appui", "standardEmrPage", [ title: ui.message("coreapps.app.systemAdministration.label") ])

    def htmlSafeId = { extension ->
        "${ extension.id.replace(".", "-") }-app"
    }
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("coreapps.app.systemAdministration.label")}"}
    ];
</script>

${ ui.includeFragment("coreapps", "administrativenotification/notifications") }

<div id="tasks">
    <% extensions.each { extension -> %>

    <a id="${ htmlSafeId(extension) }" href="/${ contextPath }/${ extension.url }" class="button app big">
        <% if (extension.icon) { %>
        <i class="${ extension.icon }"></i>
        <% } %>
        ${ ui.message(extension.label) }
    </a>

    <% } %>
</div>
