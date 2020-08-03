<%
    ui.decorateWith("appui", "standardEmrPage", [ title: ui.message("coreapps.app.dataManagement.label"), includeBootstrap: false ])
    ui.includeCss("coreapps", "datamanagement/dataManagement.css")

    def htmlSafeId = { extension ->
        "${ extension.id.replace(".", "-") }-app"
    }
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("coreapps.app.dataManagement.label")}"}
    ];
</script>

<div id="tasks" class="row">
    <% extensions.each { extension -> %>
    <div  class="col-6 col-sm-2 col-md-3 col-lg-2 homeList schedulingList">
    <a id="${ htmlSafeId(extension) }" href="/${ contextPath }/${ extension.url }" class="btn btn-default btn-lg button app big align-self-center" type="button">
        <% if (extension.icon) { %>
        <i class="${ extension.icon }"></i>
        <% } %>
        ${ ui.message(extension.label) }
    </a>
    </div>

    <% } %>
</div>
