<% if (config.activeVisit) { %>
    <% def visit = config.activeVisit.visit %>

    <div class="active-visit-started-at-message">
        ${ui.message("coreapps.patientHeader.activeVisit.at", config.activeVisitStartDatetime)}
    </div>

    <% if (config.showVisitTypeOnPatientHeaderSection == false) { %>
        <% if (config.activeVisit.admitted) { %>
        <div class="active-visit-message">
            ${ui.message("coreapps.patientHeader.activeVisit.inpatient", ui.format(config.activeVisit.latestAdtEncounter.location))}
        </div>
        <% } else { %>
        <div class="active-visit-message">
            ${ui.message("coreapps.patientHeader.activeVisit.outpatient")}
        </div>
        <% } %>
    <% } else { %>
        <script type="text/javascript">
            jq(document).ready(function () {
                if ('${visitAttributeColor}' != '') {
                    jq(".active-visit-message").css("background",'${visitAttributeColor}');
                    jq(".active-visit-message").css("background-color",'${visitAttributeColor}');
                }
            })
        </script>

        <% if (config.activeVisit.admitted) { %>
            <div class="active-visit-message">
                ${ui.message("coreapps.patientHeader.activeVisit.inpatient", ui.format(config.activeVisit.latestAdtEncounter.location))}
            </div>
        <% } else { %>
            <div class="active-visit-message">
                <% if(visit.visitType.name) { %>
                    ${visit.visitType.name}
                <% } else { %>
                    ${ui.message("coreapps.patientHeader.activeVisit.outpatient")}
                <% } %>
            </div>
        <% } %>
    <% } %>

<% } %>
