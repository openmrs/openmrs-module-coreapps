<% if (config.activeVisit) { %>
            <% def visit = activeVisit.visit %>

            <div class="active-visit-started-at-message">
                ${ui.message("coreapps.patientHeader.activeVisit.at", activeVisitStartDatetime)}
            </div>
            <% if (activeVisit.admitted) { %>
                <div class="active-visit-message">
                    ${ui.message("coreapps.patientHeader.activeVisit.inpatient", ui.format(activeVisit.latestAdtEncounter.location))}
                </div>
            <% } else { %>
                <div class="active-visit-message">
                    ${ui.message("coreapps.patientHeader.activeVisit.outpatient")}
                </div>
            <% } %>

        <% } %>
