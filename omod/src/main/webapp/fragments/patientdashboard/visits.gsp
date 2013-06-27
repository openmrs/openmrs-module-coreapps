<%
    def dateFormat = new java.text.SimpleDateFormat("dd MMM yyyy")
    def timeFormat = new java.text.SimpleDateFormat("hh:mm a")
    def editDateFormat = new java.text.SimpleDateFormat("dd-MM-yyyy")

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

        // initialize the dialogs used when creating a retrospective visit
        visit.createRetrospectiveVisitDialog(${patient.id});
        visit.createRetrospectiveVisitExistingVisitsDialog();

        jq(function(){
            // TODO: for retrospective visits dialog, hack to set the end date when selecting a start date
            // TODO: move this!
            jq('#retrospectiveVisitStartDate').change(function() {
                jq('#retrospectiveVisitStopDate-display').val(jq('#retrospectiveVisitStartDate-display').val());
                jq('#retrospectiveVisitStopDate-field').val(jq('#retrospectiveVisitStartDate-field').val());
            });
        });

    });
</script>
<% encounterTemplateExtensions.each { extension -> %>
    ${ui.includeFragment(extension.extensionParams.templateFragmentProviderName, extension.extensionParams.templateFragmentId)}
<% } %>
<!-- End of encounter templates -->

<script type="text/template" id="visitDetailsTemplate">
    ${ ui.includeFragment("coreapps", "patientdashboard/visitDetailsTemplate") }
</script>

<script type="text/javascript">
    jq(function(){
        var visitId;
        <% if (param.visitId != null) { %>
            visitId = ${ param.visitId };
        <% } else if (activeVisit != null) { %>
            visitId = ${ activeVisit.visit.id };
        <% } %>
        loadTemplates(visitId);
    });
</script>

<ul id="visits-list" class="left-menu">

    <%  def visits = patient.allVisitsUsingWrappers
        visits.eachWithIndex { wrapper, idx ->
            def primaryDiagnoses = wrapper.primaryDiagnoses
    %>
    <li class="menu-item viewVisitDetails" data-visit-id="${wrapper.visit.visitId}">
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


    ${ ui.includeFragment("coreapps", "patientdashboard/editVisitDatesDialog", [
            visitId: wrapper.visit.visitId,
            endDateUpperLimit: idx == 0 ? null : editDateFormat.format(org.apache.commons.lang.time.DateUtils.addDays(visits[idx - 1].startDatetime, -1)),
            endDateLowerLimit: editDateFormat.format(wrapper.lastEncounter == null ? wrapper.startDatetime : wrapper.lastEncounter.encounterDatetime),
            startDateLowerLimit: idx + 1 == visits.size ? null : editDateFormat.format(org.apache.commons.lang.time.DateUtils.addDays(visits[idx + 1].stopDatetime, 1)),
            startDateUpperLimit: wrapper.oldestEncounter == null && wrapper.stopDatetime == null ? null : editDateFormat.format(wrapper.oldestEncounter == null ? wrapper.stopDatetime : wrapper.oldestEncounter.encounterDatetime),
            defaultStartDate: wrapper.startDatetime,
            defaultEndDate: wrapper.stopDatetime
    ]) }
    <% } %>

    <% if(patient.allVisitsUsingWrappers.size == 0) { %>
        ${ ui.message("emr.patientDashBoard.noVisits")}
    <% } %>
</ul>

<div id="visit-details" class="main-content">
    <% if (patient.patient.dead) { %>
        <h4>${ ui.message('emr.noActiveVisit') }</h4>
        <p class="spaced">${ ui.message('emr.deadPatient.description') }</p>
    <% } else if (!activeVisit) { %>
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

<div id="retrospective-visit-creation-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <i class="icon-plus"></i>
        <h3>${ ui.message("coreapps.task.createRetrospectiveVisit.label") }</h3>
    </div>
    <div class="dialog-content form">
        <p>
            <label for="startDate" class="required">
                ${ ui.message("coreapps.startDate.label") }
            </label>

            ${ ui.includeFragment("uicommons", "field/datetimepicker", [
                    id: "retrospectiveVisitStartDate",
                    formFieldName: "retrospectiveVisitStartDate",
                    label:"",
                    endDate: editDateFormat.format(new Date()),
                    useTime: false,
            ])}
        </p>

        <p>
            <label for="stopDate" class="required">
                ${ ui.message("coreapps.stopDate.label") }
            </label>

            ${ ui.includeFragment("uicommons", "field/datetimepicker", [
                    id: "retrospectiveVisitStopDate",
                    formFieldName: "retrospectiveVisitStopDate",
                    label:"",
                    endDate: editDateFormat.format(new Date()),
                    useTime: false,
            ])}
        </p>

        <br><br>

        <button class="cancel">${ ui.message("emr.cancel") }</button>
        <button class="confirm right">${ ui.message("emr.confirm") }</button>
    </div>
</div>

<div id="retrospective-visit-existing-visits-dialog" class="dialog" style="display: none">

    <div class="dialog-header">
        <i class="icon-plus"></i>
        <h3>${ ui.message("coreapps.task.createRetrospectiveVisit.label") }</h3>
    </div>

    <div class="dialog-content form">

        <ul>
            <li class="error">
                <span>${ ui.message("coreapps.retrospectiveVisit.conflictingVisitMessage") }</span>
            </li>
        </ul>

        <ul class="select" id="past-visit-dates">

        </ul>

        <br><br>

        <button class="cancel">${ ui.message("emr.cancel") }</button>
        <button class="confirm no-color">${ ui.message("coreapps.retrospectiveVisit.changeDate.label") }</button>
    </div>
</div>
