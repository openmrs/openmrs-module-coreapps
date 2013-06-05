<%
    ui.decorateWith("appui", "standardEmrPage")
%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("emr.app.activeVisits.label")}"}
    ];
</script>

<h3>${ ui.message("emr.activeVisits.title") }</h3>

<table id="active-visits" width="100%" border="1" cellspacing="0" cellpadding="2">
	<thead>
		<tr>
			<th>${ ui.message("emr.patient.identifier") }</th>
			<th>${ ui.message("emr.person.name") }</th>
			<th>${ ui.message("emr.activeVisits.checkIn") }</th>
			<th>${ ui.message("emr.activeVisits.lastSeen") }</th>
		</tr>
	</thead>
	<tbody>
        <% if (visitSummaries.size() == 0) { %>
            <tr>
                <td colspan="4">${ ui.message("emr.none") }</td>
            </tr>
        <% } %>
		<% visitSummaries.each { v ->
			def checkIn = v.checkInEncounter
			def latest = v.lastEncounter
		%>
			<tr id="visit-${ v.visit.id }">
				<td>${ ui.format(v.visit.patient.patientIdentifier) }</td>
				<td>
                    <a href="${ ui.pageLink("coreapps", "patientdashboard/patientDashboard", [ patientId: v.visit.patient.id ]) }">
                        ${ ui.format(v.visit.patient) }
                    </a>
                </td>
				<td>
                    <% if (checkIn) { %>
                        <small>
                            ${ ui.format(checkIn.location) } @ ${ ui.format(checkIn.encounterDatetime) }
                        </small>
                    <% } %>
				</td>
				<td>
                    <% if (latest) { %>
                        ${ ui.format(latest.encounterType) }
                        <br/>
                        <small>
                            ${ ui.format(latest.location) } @ ${ ui.format(latest.encounterDatetime) }
                        </small>

                    <% } %>
				</td>
			</tr>
		<% } %>
	</tbody>
</table>