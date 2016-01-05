<%
    def patient = config.patient
%>
<div class="info-section">
    <div class="info-header">
        <i class="icon-calendar"></i>
        <h3>${ ui.message("coreapps.clinicianfacing.recentVisits").toUpperCase() }</h3>
        <% if (context.hasPrivilege("App: coreapps.patientVisits")) { %>
            <a href="${visitsUrl}" class="right">
                <i class="icon-share-alt edit-action" title="${ ui.message("coreapps.edit") }"></i>
            </a>
        <% } %>
    </div>
    <div class="info-body">
        <% if (recentVisitsWithLinks.isEmpty()) { %>
        ${ui.message("coreapps.none")}
        <% } %>
        <ul>
            <% recentVisitsWithLinks.each { it, url -> %>
            <li class="clear">
                <a href="${url}" class="visit-link">
                    ${ ui.formatDatePretty(it.startDatetime) }
                    <% if(it.stopDatetime && !it.startDatetime.format("yyyy/MM/dd").equals(it.stopDatetime.format("yyyy/MM/dd"))){ %> - ${ ui.formatDatePretty(it.stopDatetime) }<% } %>
                </a>
                <div class="tag">
                    <% if (it.stopDatetime == null || new Date().before(it.stopDatetime)) { %> ${ ui.message("coreapps.clinicianfacing.active") } - <% } %>
                    ${ (it.admissionEncounter) ? ui.message("coreapps.clinicianfacing.inpatient") : ui.message("coreapps.clinicianfacing.outpatient") }
                </div>
            </li>
            <% } %>
        </ul>
    </div>
</div>