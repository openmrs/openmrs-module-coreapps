<%
    ui.decorateWith("appui", "standardEmrPage")
%>
<script type="text/javascript">
<% if (breadcrumbs) { %>
    var breadcrumbs = ${ breadcrumbs };
<% } else { %>
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message(label)}"}
    ];
<% } %>

    jq(function() {
        jq('#patient-search').focus();
    });

</script>

<h2>
	${ ui.message(heading) }
</h2>
<% if (breadcrumbs) { %>
${ ui.includeFragment("coreapps", "patientsearch/patientSearchWidget",
        [ afterSelectedUrl: afterSelectedUrl,
          showLastViewedPatients: showLastViewedPatients,
          breadcrumbOverride: breadcrumbs,
          registrationAppLink: registrationAppLink])}
<% } else { %>
${ ui.includeFragment("coreapps", "patientsearch/patientSearchWidget",
        [ afterSelectedUrl: afterSelectedUrl,
          showLastViewedPatients: showLastViewedPatients,
          registrationAppLink: registrationAppLink ])}
<% } %>