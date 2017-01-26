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
            <% if (config.showVisitTypeOnPatientHeaderSection == false) { %>
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
            <% } else { %>
                <% recentVisitsWithLinks.each { it, url -> %>
                    <li class="clear">
                        <a id="${it.visit.id}" href="${url}/" class="visit-link">
                            <script type="text/javascript">
                                jq("#${it.visit.id}.visit-link").click(function () {
                                    window.location.href = "${url}";
                                });
                            </script>
                            ${ ui.formatDatePretty(it.startDatetime) }
                            <% if(it.stopDatetime && !it.startDatetime.format("yyyy/MM/dd").equals(it.stopDatetime.format("yyyy/MM/dd"))){ %> - ${ ui.formatDatePretty(it.stopDatetime) }<% } %>
                        </a>
                        <% recentVisitsWithAttr.each { visitId, attr -> %>

                            <% if (visitId == it.visit.id) { %>
                                <script type="text/javascript">
                                    jq(document).ready(function () {
                                        if ('${attr.color}' != null) {
                                            jq("#visittype-tag-${visitId}.tag").css("background",'${attr.color}');
                                        }
                                    });
                                </script>
                                <span id="visittype-tag-${visitId}" class="tag" >
                                    ${ ui.format(it.visit.visitType)}
                                </span>
                            <% } %>
                        <% } %>
                        <span id="active-tag-${it.visit.id}" class="tag active">
                            <% if (it.stopDatetime == null || new Date().before(it.stopDatetime)) { %> ${ ui.message("coreapps.clinicianfacing.active") } <% } else { %>
                                <script type="text/javascript">
                                    jq(document).ready(function () {
                                        jq("#active-tag-${it.visit.id}.tag").css("display","none");
                                    });
                                </script>
                            <%  } %>
                        </span>
                    <% } %>
            <% } %>
        </ul>
    </div>
</div>