<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeJavascript("coreapps", "visit/jquery.dataTables.js")
    ui.includeJavascript("coreapps", "visit/filterTable.js")
    ui.includeCss("coreapps", "visit/visits.css")
%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("coreapps.app.activeVisits.label")}"}
    ];
</script>

<h3>${ ui.message("coreapps.activeVisits.title") }</h3>
<p id="filter-tags" class="filters">
    ${ui.message("Filters")}
    <% visitTypesWithAttr.each { type, attr -> %>
    <span class="filter disabled" value="${type}"/>
        <script type="text/javascript">
            jq(document).ready(function () {
                if ('${attr.color}' != null) {
                    jq("#visittype-tag-${type}.tag").css("background",'${attr.color}');
                }
            })
            </script>
        <span id="visittype-tag-${type}" class="tag ${attr.shortName}" style="cursor:pointer;" >
                ${ui.format(attr.name)}
        </span>
    </span>
    <% } %>
</p>

<table id="active-visits" width="100%" border="1" cellspacing="0" cellpadding="2">
	<thead>
		<tr>
			<th>${ ui.message("coreapps.patient.identifier") }</th>
			<th>${ ui.message("coreapps.person.name") }</th>
			<th>${ ui.message("coreapps.activeVisits.checkIn") }</th>
			<th>${ ui.message("coreapps.activeVisits.lastSeen") }</th>
            <th>${ ui.message("coreapps.retrospectiveCheckin.visitType.label") }</th>
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
				<td>${ ui.encodeHtmlContent(ui.format(v.visit.patient.patientIdentifier)) }</td>
				<td>

                    <% if (canViewVisits) { %>
                    <!-- only add link to patient dashboard if user has appropriate privilege -->
                        <a href="${ ui.urlBind("/" + contextPath + patientPageUrl, v.visit) }">
                    <% } %>

                    ${ ui.encodeHtmlContent(ui.format(v.visit.patient)) }

                    <% if (canViewVisits) { %>
                        </a>
                    <% } %>
                </td>
				<td>
                    <% if (checkIn) { %>
                        <small>
                            ${ ui.encodeHtmlContent(ui.format(checkIn.location)) } @ ${ ui.format(checkIn.encounterDatetime) }
                        </small>
                    <% } %>
				</td>
				<td>
                    <% if (latest) { %>
                        ${ ui.encodeHtmlContent(ui.format(latest.encounterType)) }
                        <br/>
                        <small>
                            ${ ui.encodeHtmlContent(ui.format(latest.location)) } @ ${ ui.format(latest.encounterDatetime) }
                        </small>

                    <% } %>
				</td>
                <td>
                    <% if (v.visit.visitType) { %>
                        <span style="display:none">${v.visit.visitType.id}</span>
                        <span id="visittype-tag-${ visitTypesWithAttr[v.visit.visitType.id].shortName }" style="background: ${ visitTypesWithAttr[v.visit.visitType.id].color }" class="tag" >
                            ${ui.format(v.visit.visitType)}
                        </span>
                        <br/>
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
