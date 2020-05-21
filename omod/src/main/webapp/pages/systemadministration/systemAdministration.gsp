<%
    ui.decorateWith("appui", "standardEmrPage", [ title: ui.message("coreapps.app.systemAdministration.label") ])
    ui.includeCss("coreapps", "systemadministration/systemadministration.css")


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

<div id="tasks" class="row">
    <div  class="col-12 col-sm-12 col-md-12 col-lg-12 homeList">
        <% if (context.hasPrivilege("App: coreapps.systemAdministration")) { %>
            <% extensions.each { extension -> %>
                <a id="${ htmlSafeId(extension) }"
                   href="/${ contextPath }/${ extension.url }"
                   class="btn btn-default btn-lg button app big align-self-center" type="button">
                    <% if (extension.icon) { %>
                        <i class="${ extension.icon }"></i>
                    <% } %>
                    ${ ui.message(extension.label) }
                </a>
            <% } %>
        <% } %>
    <div>
</div>
