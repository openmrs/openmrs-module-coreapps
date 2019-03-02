<%
    ui.decorateWith("appui", "standardEmrPage")
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("coreapps.NoPatient.breadcrumbLabel") }" }
    ];
</script>

<div  style="background-color:red;  border-radius: 5px">
<h1 style ="color:white">${ ui.message("coreapps.patientNotFound.Description") }</h1>
</div>
