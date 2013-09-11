<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeCss("coreapps", "findpatient/findPatient.css")
    ui.includeCss("uicommons", "datatables/dataTables_jui.css")
    ui.includeJavascript("uicommons", "datatables/jquery.dataTables.min.js")
    ui.includeJavascript("coreapps", "findpatient/findPatient.js")
    ui.includeJavascript("coreapps", "moment.min.js")
%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message(label)}"}
    ];

    jq(function() {
        new PatientSearchWidget(getConfig());
        jq('#patient-search').focus();
    });

    function getConfig(){
        var config = {
            searchInputId: 'patient-search',
            searchButtonId: 'patient-search-button',
            searchResultsTableId: 'patient-search-results-table',
            searchResultsDivId: 'patient-search-results',
            afterSelectedUrl: '${ ui.escapeJs(afterSelectedUrl) }',
            messages: {
                info: '${ ui.message("coreapps.search.info") }',
                first: '${ ui.message("coreapps.search.first") }',
                previous: '${ ui.message("coreapps.search.previous") }',
                next: '${ ui.message("coreapps.search.next") }',
                last: '${ ui.message("coreapps.search.last") }',
                noMatchesFound: '${ ui.message("coreapps.search.noMatchesFound") }'
            }
        };

        return config;
    }
</script>

<h2>
	${ ui.message('coreapps.findPatient.app.label') }
</h2>

<form method="get" id="patient-search-form" onsubmit="return false">
    <input type="text" id="patient-search" placeholder="${ ui.message("coreapps.findPatient.search.placeholder") }" autocomplete="off"/>
    <input type="button" id="patient-search-button" value="${ ui.message("coreapps.findPatient.search.button") }" />
</form>

<div id="patient-search-results">
    <table id="patient-search-results-table">
        <thead>
            <tr>
                <th>${ ui.message("coreapps.search.identifier") }</th>
                <th>${ ui.message("coreapps.search.name") }</th>
                <th>${ ui.message("coreapps.age") }</th>
                <th>${ ui.message("coreapps.gender") }</th>
                <th>${ ui.message("coreapps.birthdate") }</th>
            </tr>
        </thead>
        <tbody></tbody>
    </table>
</div>
