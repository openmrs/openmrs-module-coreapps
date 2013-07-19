<%
    ui.decorateWith("appui", "standardEmrPage")
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("coreapps.deletedPatient.breadcrumbLabel") }" }
    ];
</script>

<h1>${ ui.message("coreapps.deletedPatient.title") }</h1>

<p>${ ui.message("coreapps.deletedPatient.description") }</p>