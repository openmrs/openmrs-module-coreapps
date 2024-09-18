<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeCss("coreapps", "clinicianfacing/patient.css")
%>

<script type="text/javascript">
	var breadcrumbs = [
		{ icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
		{ label: "${ ui.message(app.label)}"}
	];

	jq(function(){
		jq(".tabs").tabs();
		// make sure we reload the page if the location is changes; this custom event is emitted by the location selector in the header
		jq(document).on('sessionLocationChanged', function() {
			window.location.reload();
		});
	});
</script>

<div class="clear"></div>
<div class="container">
	<div class="dashboard clear">
		<!-- only show the title div if a title has been defined in the messages.properties -->
		<% if (ui.message(app.id + ".custom.title") != app.id + ".custom.title") { %>
		<div class="title">
			<h3>${ ui.message(app.id + ".custom.title") }</h3>
		</div>
		<% } %>
		<div class="info-container column">
			<% if (firstColumnFragments) {
				firstColumnFragments.each {
					// create a base map from the fragmentConfig if it exists, otherwise just create an empty map
					def configs = [:];
					if(it.extensionParams.fragmentConfig != null){
						configs = it.extensionParams.fragmentConfig;
					}
					configs << [  app: it.appId ]
			%>
				${ ui.includeFragment(it.extensionParams.provider, it.extensionParams.fragment, configs)}
			<%  }
			} %>

		</div>
		<div class="info-container column">
		</div>
	</div>
</div>
<br/>
<div class="dashboard info-header clear">
	<h3>${ ui.message("coreapps.currently.enrolled") }:</h3>
</div>
<div>
	<table id="enrollmentsTable" style="width: 100%">
		<thead>
			<tr>
				<th>${ ui.message("Patient.name") }</th>
				<th>${ ui.message("coreapps.emr.id") }</th>
				<th>${ ui.message("coreapps.enrollment.startDate") }</th>
				<th>${ ui.message("coreapps.treatment.state") }</th>
			</tr>
		</thead>
		<tbody>
			<% programEnrollments.each { e ->
			%>
			<tr>
			<td class="name-link"><a href="${ ui.urlBind("/" + contextPath + dashboardUrl, [ patientId: e.patient.patientId ]) }">${ ui.format(e.patient.personName)}</a></td>
				<td>${ e.patient.getPatientIdentifier(primaryIdentifierType.id) }</td>
				<td>${ ui.format(e.dateEnrolled)}</td>
				<td>
					<% e.currentStates.eachWithIndex { state, index -> %>
						${ ui.format(state.state.concept) }${ e.currentStates.size() - index > 1 ? ", " : ""}
					<% } %>
				</td>
			</tr>
			<% } %>
		</tbody>
	</table>
</div>

${ ui.includeFragment("uicommons", "widget/dataTable", [ object: "#enrollmentsTable",
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