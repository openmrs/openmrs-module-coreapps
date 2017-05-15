<%

    def timeFormat = new java.text.SimpleDateFormat("hh:mm a", org.openmrs.api.context.Context.getLocale() )
    def editDateFormat = new java.text.SimpleDateFormat("dd-MM-yyyy")

    def formatDiagnoses = {
        it.collect{ ui.escapeHtml(it.diagnosis.formatWithoutSpecificAnswer(context.locale)) } .join(", ")
    }
    ui.includeJavascript("coreapps", "fragments/visitDetails.js")
    ui.includeJavascript("coreapps", "visit/encounterToggle.js")

    ui.includeCss("coreapps", "encounterToggle.css")
%>

<script type="text/javascript">
    breadcrumbs.push({ label: "${ui.message("coreapps.patientDashBoard.visits")}" , link:'${ui.pageLink("coreapps", "patientdashboard/patientDashboard", [patientId: patient.id])}'});

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

<i id="i-toggle" title="${ui.message('coreapps.showAllEncounterDetails')}"
   class="toggle-icon icon-arrow-down small caret-color"
   onclick="toggle()"></i>

<script type="text/template" id="visitDetailsTemplate">
    ${ ui.includeFragment("coreapps", "patientdashboard/visitDetailsTemplate") }
</script>

<script type="text/javascript">
    jq(function(){
        var visitId;
        <% if (selectedVisit != null) { %>
            visitId = ${ selectedVisit.id };
        <% } else if (activeVisit != null) { %>
            visitId = ${ activeVisit.visit.id };
        <% } %>
        var fromEncounter = 0;
        <% if (param.fromEncounter != null) { %>
            fromEncounter = ${ param.fromEncounter };
        <% } %>
        <% if (param.encounterCount != null) { %>
            encounterCount = ${ param.encounterCount }; // This variable is defined in patientdashboard/patientDashboard.gsp
        <% } %>
        loadTemplates(visitId, ${ patient.id }, fromEncounter, encounterCount);

        <%
            if ( activeVisit != null){
                if(activeVisit.hasEncounters() && activeVisit.getVisit().getEncounters().size() < 20){ %>
                    jq('#i-toggle').show();
                <%}
            }
        %>
    });
</script>

<ul id="visits-list" class="left-menu">

    <%
        def visits = patient.allVisitsUsingWrappers;
        visits.eachWithIndex { wrapper, idx ->
            def primaryDiagnoses = wrapper.getUniqueDiagnoses(true, false)
    %>
    <li class="menu-item viewVisitDetails" data-visit-id="${wrapper.visit.visitId}">
        <span class="menu-date">
            <i class="icon-time"></i>
            ${ui.format(wrapper.startDate)}
            <% if(wrapper.stopDate != null) { %>
                - ${ui.format(wrapper.stopDate)}
            <% } else { %>
                (${ ui.message("coreapps.patientDashBoard.activeSince")} ${timeFormat.format(wrapper.visit.startDatetime)})
            <% } %>
        </span>

        <% if (primaryDiagnoses != null) { %>  <!-- if primary diagnosis is null, don't display box at all, if empty, display "no diagnosis" message -->
            <span class="menu-title">
                <i class="icon-stethoscope"></i>
                <% if (!primaryDiagnoses.empty) { %>
                    ${ formatDiagnoses(primaryDiagnoses) }
                <% }  else { %>
                    ${ ui.message("coreapps.patientDashBoard.noDiagnosis")}
                <% } %>
            </span>
        <% } %>
        <span class="arrow-border"></span>
        <span class="arrow"></span>
    </li>


    ${ ui.includeFragment("coreapps", "patientdashboard/editVisitDatesDialog", [
            visitId: wrapper.visit.visitId,
            endDateUpperLimit: idx == 0 ? editDateFormat.format(new Date()) : editDateFormat.format(org.apache.commons.lang.time.DateUtils.addDays(visits[idx - 1].startDatetime, -1)),
            endDateLowerLimit: editDateFormat.format(wrapper.mostRecentEncounter == null ? wrapper.startDatetime : wrapper.mostRecentEncounter.encounterDatetime),
            startDateLowerLimit: (idx + 1 == visits.size || visits[idx + 1].stopDatetime == null) ? null : editDateFormat.format(org.apache.commons.lang.time.DateUtils.addDays(visits[idx + 1].stopDatetime, 1)),
            startDateUpperLimit: wrapper.oldestEncounter == null && wrapper.stopDatetime == null ? editDateFormat.format(new Date()) : editDateFormat.format(wrapper.oldestEncounter == null ? wrapper.stopDatetime : wrapper.oldestEncounter.encounterDatetime),
            defaultStartDate: wrapper.startDatetime,
            defaultEndDate: wrapper.stopDatetime
    ]) }

    ${ ui.includeFragment("coreapps", "patientdashboard/editVisit", [
            visit: wrapper.visit,
            patient: patient
    ]) }

    <% } %>

    <% if(patient.allVisitsUsingWrappers.size == 0) { %>
        <div class="no-results">
            ${ ui.message("coreapps.patientDashBoard.noVisits")}
        </div>
    <% } %>
</ul>

<div class="main-content">
    <div id="visit-details">
        <% if (patient.patient.dead) { %>
            <h4>${ ui.message('coreapps.noActiveVisit') }</h4>
            <p class="spaced">${ ui.message('coreapps.deadPatient.description') }</p>
        <% } else if (!activeVisit) { %>
            <h4>${ ui.message('coreapps.noActiveVisit') }</h4>
            <p class="spaced">${ ui.message('coreapps.noActiveVisit.description') }</p>
            <% if (sessionContext.userContext.hasPrivilege("Task: coreapps.createVisit")) { %>
                <p class="spaced">
                    <a id="noVisitShowVisitCreationDialog" href="javascript:visit.showQuickVisitCreationDialog(${patient.id})" class="button task">
                        <i class="icon-check-in small"></i>${ ui.message("coreapps.task.startVisit.label") }
                    </a>
                </p>
            <% } %>
        <% } %>
    </div>

    <div id="visit-paging-buttons" style="width: 75%; visibility: hidden;">
        <button id="visit-paging-button-prev" class="left" style="visibility: hidden">
            <i class=" icon-arrow-left icon-1x"></i>${ ui.message("coreapps.search.previous") }
        </button>
        <button id="visit-paging-button-next" class="right" style="visibility: hidden">
            ${ ui.message("coreapps.search.next") }<i class=" icon-arrow-right icon-1x"></i>
        </button>
    </div>
</div>

<div id="delete-encounter-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <h3>${ ui.message("coreapps.patientDashBoard.deleteEncounter.title") }</h3>
    </div>
    <div class="dialog-content">
        <input type="hidden" id="encounterId" value=""/>
        <ul>
            <li class="info">
                <span>${ ui.message("coreapps.patientDashBoard.deleteEncounter.message") }</span>
            </li>
        </ul>

        <button class="confirm right">${ ui.message("coreapps.yes") }<i class="icon-spinner icon-spin icon-2x" style="display: none; margin-left: 10px;"></i></button>
        <button class="cancel">${ ui.message("coreapps.no") }</button>
    </div>
</div>

<div id="delete-visit-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <i class="icon-check-in"></i>
        <h3>${ ui.message("coreapps.task.deleteVisit.label") }</h3>
    </div>
    <div class="dialog-content">
        <input type="hidden" id="visitId" value=""/>
        <ul>
            <li class="info">
                <span>${ ui.message("coreapps.task.deleteVisit.message") }</span>
            </li>
        </ul>

        <button class="confirm right">${ ui.message("coreapps.yes") }<i class="icon-spinner icon-spin icon-2x" style="display: none; margin-left: 10px;"></i></button>
        <button class="cancel">${ ui.message("coreapps.no") }</button>
    </div>
</div>
