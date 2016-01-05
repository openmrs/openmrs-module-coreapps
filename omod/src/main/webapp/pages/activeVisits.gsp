<%
    ui.decorateWith("appui", "standardEmrPage")
%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("coreapps.app.activeVisits.label")}"}
    ];
</script>

<h3>${ ui.message("coreapps.activeVisits.title") }</h3>

<table id="active-visits" width="100%" border="1" cellspacing="0" cellpadding="2">
	<thead>
		<tr>
			<th>${ ui.message("coreapps.patient.identifier") }</th>
			<th>${ ui.message("coreapps.person.name") }</th>
			<th>${ ui.message("coreapps.activeVisits.checkIn") }</th>
			<th>${ ui.message("coreapps.activeVisits.lastSeen") }</th>
		</tr>
	</thead>
	<tbody>
        <% if (visitSummaries == null || (visitSummaries !=null && visitSummaries.size() == 0) ) { %>
            <tr>
                <td colspan="4">${ ui.message("coreapps.none") }</td>
            </tr>
        <% } %>
		<% visitSummaries.each { v ->
			def checkIn = v.checkInEncounter
			def latest = v.lastEncounter
		%>
			<tr id="visit-${ v.visit.id }">
				<td>${ ui.format(v.visit.patient.patientIdentifier) }</td>
				<td>

                    <% if (canViewVisits) { %>
                    <!-- only add link to patient dashboard if user has appropriate privilege -->
                        <a href="${ ui.urlBind("/" + contextPath + patientPageUrl, v.visit) }">
                    <% } %>

                    ${ ui.format(v.visit.patient) }

                    <% if (canViewVisits) { %>
                        </a>
                    <% } %>
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

<% if (visitSummaries !=null && visitSummaries.size() > 0) { %>
${ ui.includeFragment("uicommons", "widget/dataTable", [ object: "#active-visits",
                                                         options: [
                                                                     bFilter: true,
                                                                     bJQueryUI: true,
                                                                     bLengthChange: false,
                                                                     iDisplayLength: 10,
                                                                     sPaginationType: '\"full_numbers\"',
                                                                     bSort: false,
                                                                     sDom: '\'ft<\"fg-toolbar ui-toolbar ui-corner-bl ui-corner-br ui-helper-clearfix datatables-info-and-pg \"ip>\''
                                                                  ]
                                                        ]) }
<% } %>