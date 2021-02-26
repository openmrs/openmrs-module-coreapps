<%
    ui.includeJavascript("coreapps", "custom/utilsTimezone.js")
    ui.includeJavascript("uicommons", "moment-with-locales.min.js")
%>

<div class="status-container">
    [[ if (stopDatetime) { ]]
    <i class="icon-time small"></i> <span class="visit-date"> ${ ui.message("coreapps.visitDetails", '[[- startDatetime ]]', '[[- stopDatetime ]]') } </span>
[[ } else { ]]
    <span class="status active"></span> ${ ui.message("coreapps.activeVisit") }
    <i class="icon-time small"></i>
    <span class="visit-date" > ${ ui.message("coreapps.activeVisit.time", '[[- startDatetime ]]') } </span>
    [[ } ]]
    [[ if (canDeleteVisit) { ]]
        <a class="right" id="deleteVisitLink" href="#" data-visit-id="[[= id]]">${ ui.message("coreapps.task.deleteVisit.label")}</a>
        <span class="right"> | </span>
    [[ } ]]
    <a class="right" id="editVisitDatesLink" href="#" data-visit-id="[[= id]]">${ ui.message("coreapps.task.editVisitDate.label")}</a>
    [[ if (canEditVisit) { ]]
    	<span class="right"> | </span>
        <a class="right" id="editVisitLink" href="#" data-visit-id="[[= id]]">${ ui.message("coreapps.task.editVisit.label")}</a>
    [[ } ]]    
        
</div>

<div class="visit-actions [[- stopDatetime ? 'past-visit' : 'active-visit' ]]">
    [[ if (stopDatetime) { ]]
        <p class="label"><i class="icon-warning-sign small"></i> ${ ui.message("coreapps.patientDashboard.actionsForInactiveVisit") }</p>
    [[ } ]]
    <% visitActions.each { task ->  %>

        [[ if (_.contains(availableVisitActions, '${task.id}')) { ]]

            <%  def returnUrl = ui.thisUrl().replaceAll("visitId=\\d+","") + "visitId={{visit.id}}"; // make sure the returnUrl includes a visit parameter so that it can be dynamically populated based on current selected visit
                def url = task.url(contextPath, appContextModel.with("visit", [id: "{{visit.id}}", uuid: "{{visit.uuid}}", active: "{{visit.active}}"]), returnUrl);   // strip out any hard-coded id, uuid, or active with the appropriate template  so that it can be dynamically populated based on current selected visit
                if (task.type != "script") {
                %>
                <a href="[[= emr.applyContextModel('${ ui.escapeJs(url) }', { 'visit.id': id, 'visit.uuid': uuid, 'visit.active': stopDatetime == null }) ]]" id="${task.id}" class="button task activelist">
            <% } else { // script
                %>
                <a href="[[= emr.applyContextModel('${ url }', {'visit.id': id, 'visit.uuid': uuid, 'visit.active': stopDatetime == null })]]" class="button task activelist">
            <% } %>
                <i class="${task.icon}"></i> ${ ui.message(task.label) }</a>

        [[ } ]]
    <% } %>
</div>
[[ if (encounters.length > 0) { ]]
<h4>${ ui.message("coreapps.patientDashBoard.encounters")} </h4>
[[ } ]]
<ul id="encountersList">
    [[ _.each(encounters, function(encounter) { ]]
        [[= encounterTemplates.displayEncounter(encounter, patient) ]]
    [[ }); ]]
</ul>

<script type="text/javascript">
    jq(document).ready(function () {

        //Convert dates to client timezone
        if (${ui.handleTimeZones()}) {
            if ('[[- startDatetime ]]' && '[[- stopDatetime ]]'){
                jq(".visit-date").text(function () {
                    return jq(this).text().replace('[[- startDatetime ]]', formatDate(new Date('[[- startDatetime ]]'), "${ui.getJSDateFormat()}", "${ui.getLocale()}"));
                });
                    jq(".visit-date").text(function () {
                        return jq(this).text().replace('[[- stopDatetime ]]', formatDatetime(new Date('[[- stopDatetime ]]'), "${ui.getJSDatetimeFormat()}",  "${ui.getLocale()}"));
                    });
            }else if ('[[- startDatetime ]]') {
                jq(".visit-date").text(function () {
                   return jq(this).text().replace('[[- startDatetime ]]', formatDatetime(new Date('[[- startDatetime ]]'), "${ui.getJSDatetimeFormat()}", "${ui.getLocale()}"));
                });
            }
        }
    })
</script>