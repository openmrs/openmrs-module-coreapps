<%
    ui.decorateWith("appui", "standardEmrPage")
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("referenceapplication.app.capturevitals.title") }", link: "${ ui.pageLink("coreapps", "findpatient/findPatient", [app: "referenceapplication.vitals"]) }" },
        { label: "${ ui.escapeJs(ui.encodeHtmlContent(ui.format(patient.patient))) }" , link: '${ui.pageLink("coreapps", "patientdashboard/patientDashboard", [patientId: patient.id])}'},
    ];
</script>

${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient.patient ]) }

<script type="text/javascript">
    jq(function() {
        jq('#actions .cancel').click(function() {
            emr.navigateTo({
                provider: "coreapps",
                page: "findpatient/findPatient",
                query: {
                    app: "referenceapplication.vitals"
                }
            });
        });
        jq('#actions .confirm').click(function() {
            emr.navigateTo({
                provider: "htmlformentryui",
                page: "htmlform/enterHtmlFormWithSimpleUi",
                query: {
                    patientId: "${ patient.id }",
                    visitId: "${ visit?.id }",
                    definitionUiResource: "referenceapplication:htmlforms/vitals.xml",
                    returnUrl: "${ ui.escapeJs(ui.pageLink("coreapps", "findpatient/findPatient?app=referenceapplication.vitals")) }",
                    breadcrumbOverride: "${ ui.escapeJs(breadcrumbOverride) }"
                }
            });
        });
        jq('#actions button').first().focus();
    });
</script>
<style>
    #existing-encounters {
        margin-top: 2em;
    }
</style>

<% if (visit) { %>

    <div class="container half-width">

        <h1>${ ui.message("coreapps.vitals.confirmPatientQuestion") }</h1>

        <div id="actions">
            <button id="coreapps-vitals-confirm" class="confirm big right">
                <i class="icon-arrow-right"></i>
                ${ ui.message("coreapps.vitals.confirm.yes") }
            </button>

            <button class="cancel big">
                <i class="icon-arrow-left"></i>
                ${ ui.message("coreapps.vitals.confirm.no") }
            </button>
        </div>

        <div id="existing-encounters">
            <h3>${ ui.message("coreapps.vitals.vitalsThisVisit") }</h3>
            <table>
                <thead>
                    <tr>
                        <th>${ ui.message("coreapps.vitals.when") }</th>
                        <th>${ ui.message("coreapps.vitals.where") }</th>
                        <th>${ ui.message("coreapps.vitals.enteredBy") }</th>
                    </tr>
                </thead>
                <tbody>
                    <% if (existingEncounters.size() == 0) { %>
                        <tr>
                            <td colspan="3">${ ui.message("emr.none") }</td>
                        </tr>
                    <% } %>
                    <% existingEncounters.each { enc ->
                        def minutesAgo = (long) ((System.currentTimeMillis() - enc.encounterDatetime.time) / 1000 / 60)
                    %>
                        <tr>
                            <td>${ ui.message("coreapps.vitals.minutesAgo", minutesAgo) }</td>
                            <td>${ ui.format(enc.location) }</td>
                            <td>${ ui.format(enc.creator) }</td>
                        </tr>
                    <% } %>
                </tbody>
            </table>
        </div>
    </div>

<% } else { %>

    <h1>
        ${ ui.message("coreapps.vitals.noVisit") }
    </h1>

    <div id="actions">
        <button class="cancel big">
            <i class="icon-arrow-left"></i>
            ${ ui.message("coreapps.vitals.noVisit.findAnotherPatient") }
        </button>
    </div>

<% } %>