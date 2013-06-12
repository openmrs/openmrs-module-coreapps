<%
    def dateFormat = new java.text.SimpleDateFormat("dd MMM yyyy")
    def timeFormat = new java.text.SimpleDateFormat("hh:mm a")
    def formatDiagnoses = {
        it.collect{ ui.escapeHtml(it.diagnosis.formatWithoutSpecificAnswer(context.locale)) } .join(", ")
    }
    ui.includeJavascript("coreapps", "fragments/visitDetails.js")
%>

<script type="text/javascript">
    breadcrumbs.push({ label: "${ui.message("emr.patientDashBoard.visits")}" , link:'${ui.pageLink("coreapps", "patientdashboard/patientDashboard", [patientId: patient.id])}'});

    jq(".collapse").collapse();
</script>

<!-- Encounter templates -->
<%
	ui.includeJavascript("coreapps", "fragments/encounterTemplates.js")
%>
<script type="text/javascript">
    jq(function() {
        <% encounterTemplateExtensions.each { extension ->
			extension.extensionParams.supportedEncounterTypes?.each { encounterType -> %>
            encounterTemplates.setTemplate('${encounterType.key}', '${extension.extensionParams.templateId}');
            <% encounterType.value.each { parameter -> %>
                encounterTemplates.setParameter('${encounterType.key}', '${parameter.key}', '${parameter.value}');
            <% }
			}
		} %>
        encounterTemplates.setDefaultTemplate('defaultEncounterTemplate');
    });
</script>
<% encounterTemplateExtensions.each { extension -> %>
    ${ui.includeFragment(extension.extensionParams.templateFragmentProviderName, extension.extensionParams.templateFragmentId)}
<% } %>
<!-- End of encounter templates -->

<script type="text/template" id="visitDetailsTemplate">
    <div class="status-container">
        [[ if (stopDatetime) { ]]
            <i class="icon-time small"></i> ${ ui.message("emr.visitDetails", '[[- startDatetime ]]', '[[- stopDatetime ]]') }
        [[ } else { ]]
            <span class="status active"></span> ${ ui.message("emr.activeVisit") }
            <i class="icon-time small"></i>
            ${ ui.message("emr.activeVisit.time", '[[- startDatetime ]]') }
        [[ } ]]
    </div>

    <div class="visit-actions [[- stopDatetime ? 'past-visit' : 'active-visit' ]]">
        [[ if (stopDatetime) { ]]
            <p class="label"><i class="icon-warning-sign small"></i> ${ ui.message("coreapps.patientDashboard.actionsForInactiveVisit") }</p>
        [[ } ]]
        <% visitActions.each { task ->
            def url = task.url
            if (task.type != "script") {
                url = "/" + contextPath + "/" + url
        %>
            <% if (task.require) { %>
                [[ if ((function() { var patientId = ${ patient.id }; var visit = { id: id, active: stopDatetime == null }; return (${ task.require }); })()) { ]]
            <% } %>
            <a href="[[= emr.applyContextModel('${ ui.escapeJs(url) }', { patientId: ${ patient.id }, 'visit.id': id, 'visit.active': stopDatetime == null }) ]]" class="button task">
        <%
            } else { // script
                url = "javascript:" + task.script
        %>
            <a href="${ url }" class="button task">
        <%
            }
        %>
                <i class="${task.icon}"></i> ${ ui.message(task.label) }
            </a>
            <% if (task.require) { %>
                [[ } ]]
            <% } %>
        <% } %>
    </div>

    <h4>${ ui.message("emr.patientDashBoard.encounters")} </h4>
    <ul id="encountersList">
        [[ _.each(encounters, function(encounter) { ]]
            [[ if (!encounter.voided) { ]]
                [[= encounterTemplates.displayEncounter(encounter, patient) ]]
            [[  } ]]
        [[ }); ]]
    </ul>
</script>

<script type="text/javascript">
    jq(function(){
        loadTemplates(${ activeVisit != null });
    });
</script>

<ul id="visits-list" class="left-menu">
    <% patient.allVisitsUsingWrappers.each { wrapper ->
        def primaryDiagnoses = wrapper.primaryDiagnoses
    %>
    <li class="menu-item viewVisitDetails" visitId="${wrapper.visit.visitId}">
        <span class="menu-date">
            <i class="icon-time"></i>
            ${dateFormat.format(wrapper.visit.startDatetime)}
            <% if(wrapper.visit.stopDatetime != null) { %>
            - ${dateFormat.format(wrapper.visit.stopDatetime)}
            <% } else { %>
            (${ ui.message("emr.patientDashBoard.activeSince")} ${timeFormat.format(wrapper.visit.startDatetime)})
            <% } %>
        </span>
        <span class="menu-title">
            <i class="icon-stethoscope"></i>
            <% if (primaryDiagnoses) { %>
            ${ formatDiagnoses(primaryDiagnoses) }
            <% } else { %>
            ${ ui.message("emr.patientDashBoard.noDiagnosis")}
            <% } %>
        </span>
        <span class="arrow-border"></span>
        <span class="arrow"></span>
    </li>
    <% } %>
    <% if(patient.allVisitsUsingWrappers.size == 0) { %>
        ${ ui.message("emr.patientDashBoard.noVisits")}
    <% } %>
</ul>

<div id="visit-details" class="main-content">
    <% if (!activeVisit) { %>
        <h4>${ ui.message('emr.noActiveVisit') }</h4>
        <p class="spaced">${ ui.message('emr.noActiveVisit.description') }</p>
        <p class="spaced">
            <a id="noVisitShowVisitCreationDialog" href="javascript:visit.showQuickVisitCreationDialog()" class="button task">
                <i class="icon-check-in small"></i>${ ui.message("emr.task.startVisit.label") }
            </a>
        </p>
    <% } %>
</div>

<div id="delete-encounter-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <h3>${ ui.message("emr.patientDashBoard.deleteEncounter.title") }</h3>
    </div>
    <div class="dialog-content">
        <input type="hidden" id="encounterId" value=""/>
        <ul>
            <li class="info">
                <span>${ ui.message("emr.patientDashBoard.deleteEncounter.message") }</span>
            </li>
        </ul>

        <button class="confirm right">${ ui.message("emr.yes") }</button>
        <button class="cancel">${ ui.message("emr.no") }</button>
    </div>
</div>
