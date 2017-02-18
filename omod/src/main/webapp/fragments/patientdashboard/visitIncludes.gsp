<%
    ui.includeJavascript("coreapps", "custom/visits.js")
    def editDateFormat = new java.text.SimpleDateFormat("dd-MM-yyyy")
    ui.includeCss("coreapps", "visit/visits.css")
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
        <% if (activeVisits) { %>
            <script type="text/javascript">
                jq("#start-visit-with-visittype-confirm").remove();
            </script>
            <p class="dialog-instructions">
                <i class="icon-sign-warning">&#xf071;</i>
                ${ui.message("coreapps.task.visitType.start.warning", ui.encodeHtmlContent(ui.format(patient.patient)))}
            </p>
            <ul class="list" style="margin-bottom:0px">
                <% activeVisits.each { activeVisit -> %>
                    <li>
                            ${ui.format(activeVisit)}
                        </li>
                    <% } %>
                </ul>
                <p class="dialog-instructions">
                    ${ ui.message("coreapps.task.endVisit.warningMessage") }
                </p>

                <% } else { %>
                <script type="text/javascript">
                    jq("#start-visit-with-visittype-confirm").removeClass("disabled");
                </script>

                <% if (visitTypes.size > 1) { %>
	                <table class="left-aligned-th">
	                    <tr>
	                        <td><label>${ ui.message("coreapps.task.visitType.label") }:</label></td>
	                        <td>
	                            <select id="visit-visittype-drop-down">
	                                <% visitTypes.each { type -> %>
	                                <% if (currentVisitType == type) { %>
	                                    <script type="text/javascript">
	                                        jq("#visit-visittype-drop-down").val(${type.id});
	                                    </script>
	                                <% } %>
	                                <option class="dialog-drop-down small" value ="${type.id}">${ ui.format(type) }</option>
	                                <% } %>
	                            </select>
	                        </td>
	                    </tr>

	                    <!-- visit attributes -->
	                    <% visitAttributeTypes.each { type -> %>
	                        <% if(type.retired == false){ %>
	                            <tr>
	                                <td class="info">${type.name}: </td>
	                                <td>
	                                    <% if(type.datatypeClassname == 'org.openmrs.customdatatype.datatype.BooleanDatatype'){ %>
	                                        <input type="radio" value="false" name="attribute.${type.id}.new[0]" /> ${ui.message("False")}
	                                        <input type="radio" value="true" name="attribute.${type.id}.new[0]" />  ${ui.message("True")}
	                                    <% } else if(type.datatypeClassname == 'org.openmrs.customdatatype.datatype.DateDatatype'){ %>

	                                    ${ ui.includeFragment("uicommons", "field/datetimepicker", [
	                                                        formFieldName: "attribute." + type.id + ".new[0].date",
	                                                        label:"",
	                                                        useTime: false,
	                                                ])}
	                                    <% } else if(type.datatypeClassname == 'org.openmrs.module.coreapps.customdatatype.CodedConceptDatatype'){ %>
	                                            <select id="coded-data-types" name="attribute.${type.id}.new[0]"></select>
	                                            <script>
	                                                    var conceptId = '${type.datatypeConfig}';
	                                                    visit.getCodedConcepts(conceptId, 'attribute.${type.id}.new[0]');
	                                                </script>
	                                        <% } else { %>
	                                            <input type="text" size="17" name="attribute.${type.id}.new[0]"/>
	                                            <% } %>
	                                        </td>
	                                </tr>
	                        <% } %>
	                    <% } %>
	                </table>
                <% } %>
                <input type="hidden" id="dateFormat" value='<%= org.openmrs.api.context.Context.getDateFormat().toPattern().toLowerCase() %>' />
                <p class="dialog-instructions">${ ui.message("coreapps.task.startVisit.message", ui.encodeHtmlContent(ui.format(patient.patient))) }</p>
                <% } %>

        <button id="start-visit-with-visittype-confirm" class="confirm right">${ ui.message("coreapps.confirm") }<i class="icon-spinner icon-spin icon-2x" style="display: none; margin-left: 10px;"></i></button>
        <button class="cancel">${ ui.message("coreapps.cancel") }</button>
    </div>
</div>

<div id="delete-patient-creation-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <i class="icon-remove"></i>
        <h3>
            ${ ui.message("coreapps.deletePatient.title", ui.encodeHtmlContent(ui.format(patient.patient))) }
        </h3>
    </div>
    <div class="dialog-content">
        <p class="dialog-instructions">${ ui.message("coreapps.task.deletePatient.message", ui.encodeHtmlContent(ui.format(patient.patient))) }</p>

        <label for="delete-reason">${ ui.message("coreapps.retrospectiveCheckin.paymentReason.label") }: </label>
        <input type="text" id="delete-reason">

        <br>
        <h6 id="delete-reason-empty">${ ui.message("coreapps.task.deletePatient.deleteMessageEmpty") }</h6>
        <br>
        <button class="confirm right">${ ui.message("coreapps.confirm") }<i class="icon-spinner icon-spin icon-2x" style="display: none; margin-left: 10px;"></i></button>
        <button class="cancel">${ ui.message("coreapps.cancel") }</button>
    </div>
</div>
