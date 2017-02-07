<%
    def breadcrumbOverride = config.breadcrumbOverride ?: ""

    ui.includeCss("uicommons", "datatables/dataTables_jui.css")
    ui.includeCss("coreapps", "patientsearch/patientSearchWidget.css")
    ui.includeJavascript("uicommons", "datatables/jquery.dataTables.min.js")
    ui.includeJavascript("coreapps", "patientsearch/patientSearchWidget.js")
    ui.includeJavascript("uicommons", "moment-with-locales.min.js")
%>
<script type="text/javascript">
    var lastViewedPatients = [];
    <%  if (showLastViewedPatients) {
            lastViewedPatients.each { it -> %>
    lastViewedPatients.push({
        uuid:"${ it.uuid }",
        name:"${ it.personName ? ui.escapeJs(ui.encodeHtmlContent(ui.format(it.personName))) : '' }",
        gender:"${ ui.escapeJs(ui.encodeHtmlContent(it.gender))}",
        age:"${ it.age ?: '' }", birthdate:"${ it.birthdate ? dateFormatter.format(it.birthdate) : '' }",
        birthdateEstimated: ${ it.birthdateEstimated },
        identifier:"${ it.patientIdentifier ? ui.escapeJs(ui.encodeHtmlContent(it.patientIdentifier.identifier)) : '' }"});
    <%      }
        }%>

    var handlePatientRowSelection =  new handlePatientRowSelection(completePatientIdField, checkConfirmButton);
    jq(function() {
        var widgetConfig = {
            initialPatients : lastViewedPatients,
            minSearchCharacters: ${ config.minSearchCharacters ?: 3 },
            afterSelectedUrl: '${ ui.escapeJs(config.afterSelectedUrl) }',
            breadcrumbOverride: '${ ui.escapeJs(breadcrumbOverride) }',
            searchDelayShort: ${ searchDelayShort },
            searchDelayLong: ${ searchDelayLong },
            handleRowSelection: handlePatientRowSelection,
            dateFormat: '${ dateFormatJS }',
            locale: '${ locale }',
            defaultLocale: '${ defaultLocale }',
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
                birthdateColHeader: '${ ui.message("coreapps.birthdate") }',
                ageInMonths: '${ ui.message("coreapps.age.months") }',
                ageInDays: '${ ui.message("coreapps.age.days") }'
            }
        };

        new PatientSearchWidget(widgetConfig);
    });
</script>

<form method="get" id="patient-search-form" onsubmit="return false">
    <input type="text" id="patient-search" placeholder="${ ui.message("coreapps.findPatient.search.placeholder") }" autocomplete="off"/><i id="patient-search-clear-button" class="small icon-remove-sign"></i>
</form>

<div id="patient-search-results"></div>
