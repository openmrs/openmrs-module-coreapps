<% if (config.activeVisit) { %>
    <% def visit = config.activeVisit.visit %>

    <div class="active-visit-started-at-message">
        ${ui.message("coreapps.patientHeader.activeVisit.at", config.activeVisitStartDatetime)}
    </div>
    <% if (config.activeVisit.admitted) { %>
        <div class="active-visit-message">
            ${ui.message("coreapps.patientHeader.activeVisit.inpatient", ui.format(config.activeVisit.latestAdtEncounter.location))}
        </div>
    <% } else { %>
        <div class="active-visit-message">
            ${ui.message("coreapps.patientHeader.activeVisit.outpatient")}
        </div>
    <% } %>

<% } %> 