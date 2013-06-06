<%
    ui.decorateWith("appui", "standardEmrPage")
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("emr.deletedPatient.breadcrumbLabel") }" }
    ];
</script>

<h1>${ ui.message("emr.deletedPatient.title") }</h1>

<p>${ ui.message("emr.deletedPatient.description") }</p>