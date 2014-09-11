<%
    def recentVisits = config.recentVisits
    def patient = config.patient
%>
<div class="info-section">
    <div class="info-header">
        <i class="icon-calendar"></i>
        <h3>${ ui.message("coreapps.clinicianfacing.visits").toUpperCase() }</h3>
        <i class="icon-pencil edit-action right" title="${ ui.message("coreapps.edit") }" onclick="location.href='${ui.pageLink("coreapps", "patientdashboard/patientDashboard", [patientId: patient.patient.id, returnUrl: ui.thisUrl()])}#visits';"></i>
    </div>
    <div class="info-body">
        <% if (recentVisits.isEmpty()) { %>
        ${ui.message("coreapps.none")}
        <% } %>
        <ul>
            <% recentVisits.each{ %>
            <li class="clear">
                <a href="${ui.pageLink("coreapps", "patientdashboard/patientDashboard", [patientId: patient.patient.id, visitId: it.visitId])}#visits" class="visit-link">
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