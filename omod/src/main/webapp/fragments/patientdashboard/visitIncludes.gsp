<%
    ui.includeJavascript("coreapps", "custom/visits.js")
    def editDateFormat = new java.text.SimpleDateFormat("dd-MM-yyyy")
%>

<script type="text/javascript">
    jq(function() {

        // initialize the dialogs used when creating a retrospective visit
        visit.createRetrospectiveVisitDialog(${patient.id});
        visit.createRetrospectiveVisitExistingVisitsDialog();

		visit.contextPath = "/${contextPath}";

        // override returnUrl if specified
        <% if (config?.patientVisitsPage) { %>
            visit.returnUrl = "${config.patientVisitsPage}";
        <% } %>

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

<div id="end-visit-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <h3>${ ui.message("coreapps.task.endVisit.label") }</h3>
    </div>
    <div class="dialog-content">
        <input type="hidden" id="visitId" value=""/>
        <ul>
            <li class="info">
                <span>${ ui.message("coreapps.task.endVisit.message") }</span>
            </li>
        </ul>

        <button class="confirm right">${ ui.message("coreapps.yes") }</button>
        <button class="cancel">${ ui.message("coreapps.no") }</button>
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
            <%  def visitEndTime = new Date()
                if (patient.patient.deathDate) {
                  visitEndTime = patient.patient.deathDate
                }
            %>

            ${ ui.includeFragment("uicommons", "field/datetimepicker", [
                    id: "retrospectiveVisitStartDate",
                    formFieldName: "retrospectiveVisitStartDate",
                    label:"",
                    defaultDate: visitEndTime,
                    endDate: editDateFormat.format(visitEndTime),
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
                    defaultDate: visitEndTime,
                    endDate: editDateFormat.format(visitEndTime),
                    useTime: false,
            ])}
        </p>

        <br><br>

        <button class="cancel">${ ui.message("coreapps.cancel") }</button>
        <button class="confirm right">${ ui.message("coreapps.confirm") }<i class="icon-spinner icon-spin icon-2x" style="display: none; margin-left: 10px;"></i></button>
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

        <button class="cancel">${ ui.message("coreapps.cancel") }</button>
        <button class="confirm no-color">${ ui.message("coreapps.retrospectiveVisit.changeDate.label") }</button>
    </div>
</div>


<div id="quick-visit-creation-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <i class="icon-check-in"></i>
        <h3>
            ${ ui.message("coreapps.visit.createQuickVisit.title") }
        </h3>
    </div>
    <div class="dialog-content">
        <p class="dialog-instructions">${ ui.message("coreapps.task.startVisit.message", ui.format(patient.patient)) }</p>

        <button class="confirm right">${ ui.message("coreapps.confirm") }<i class="icon-spinner icon-spin icon-2x" style="display: none; margin-left: 10px;"></i></button>
        <button class="cancel">${ ui.message("coreapps.cancel") }</button>
    </div>
</div>

<div id="delete-patient-creation-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <i class="icon-remove"></i>
        <h3>
            ${ ui.message("coreapps.deletePatient.title", ui.format(patient.patient)) }
        </h3>
    </div>
    <div class="dialog-content">
        <p class="dialog-instructions">${ ui.message("coreapps.task.deletePatient.message", ui.format(patient.patient)) }</p>

        <label for="delete-reason">${ ui.message("coreapps.retrospectiveCheckin.paymentReason.label") }: </label>
        <input type="text" id="delete-reason">
        
        <br>
        <h6 id="delete-reason-empty">${ ui.message("coreapps.task.deletePatient.deleteMessageEmpty") }</h6>
        <br>
        <button class="confirm right">${ ui.message("coreapps.confirm") }<i class="icon-spinner icon-spin icon-2x" style="display: none; margin-left: 10px;"></i></button>
        <button class="cancel">${ ui.message("coreapps.cancel") }</button>
    </div>
</div>
