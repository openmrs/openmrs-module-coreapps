<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeCss("coreapps", "findpatient/findPatient.css")
    ui.includeCss("uicommons", "datatables/dataTables_jui.css")
    ui.includeJavascript("uicommons", "datatables/jquery.dataTables.min.js")
    ui.includeJavascript("coreapps", "findpatient/patientSearchWidget.js")
    ui.includeJavascript("uicommons", "moment.min.js")
%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message(label)}"}
    ];

    var lastViewedPatients = [];
    <%  if (showLastViewedPatients) {
            lastViewedPatients.each { it -> %>
            lastViewedPatients.push({uuid:"${ it.uuid }",fullName:"${ it.personName.fullName }",gender:"${ it.gender }",
                age:"${ it.age ?: '' }", birthdate:"${ it.birthdate ? dateFormatter.format(it.birthdate) : '' }",
                birthdateEstimated: ${ it.birthdateEstimated }, identifier:"${ it.patientIdentifier.identifier }"});
    <%      }
        }%>

    jq(function() {
        var widgetConfig = {
            initialPatients : lastViewedPatients,
            minSearchCharacters: ${minSearchCharacters},
            afterSelectedUrl: '${ ui.escapeJs(afterSelectedUrl) }',
            messages: {
                info: '${ ui.message("coreapps.search.info") }',
                first: '${ ui.message("coreapps.search.first") }',
                previous: '${ ui.message("coreapps.search.previous") }',
                next: '${ ui.message("coreapps.search.next") }',
                last: '${ ui.message("coreapps.search.last") }',
                noMatchesFound: '${ ui.message("coreapps.search.noMatchesFound") }',
                noData: '${ ui.message("coreapps.search.noData") }',
                recent: '${ ui.message("coreapps.search.label.recent") }',
                searchError: '${ ui.message("coreapps.search.error") }',
                identifierColHeader: '${ ui.message("coreapps.search.identifier") }',
                nameColHeader: '${ ui.message("coreapps.search.name") }',
                genderColHeader: '${ ui.message("coreapps.gender") }',
                ageColHeader: '${ ui.message("coreapps.age") }',
                birthdateColHeader: '${ ui.message("coreapps.birthdate") }'
            }
        };

        new PatientSearchWidget(widgetConfig);

        jq('#patient-search').focus();
    });
</script>

<h2>
	${ ui.message(heading) }
</h2>

<form method="get" id="patient-search-form" onsubmit="return false">
    <input type="text" id="patient-search" placeholder="${ ui.message("coreapps.findPatient.search.placeholder") }" autocomplete="off"/>
</form>

<div id="patient-search-results"></div>
