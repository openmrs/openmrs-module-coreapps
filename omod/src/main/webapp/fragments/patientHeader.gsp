<%
    def patient = config.patient
    def patientNames = config.patientNames

    ui.includeCss("coreapps", "patientHeader.css")
    ui.includeJavascript("coreapps", "patientdashboard/patient.js")

    appContextModel.put("returnUrl", ui.thisUrl())
%>

<script type="text/javascript">
    jq(document).ready(function () {
        createEditPatientIdentifierDialog(${patient.id});
        jq("#patientIdentifierValue").keyup(function(event){
            var oldValue = jq("#patientIdentifierValue").val();
            var newValue = jq("#hiddenInitialIdentifierValue").val();
            if(oldValue==newValue){
                jq('.confirm').attr("disabled", "disabled");
                jq('.confirm').addClass("disabled");
            }else{
                jq('.confirm').removeAttr("disabled");
                jq('.confirm').removeClass("disabled");
                if(event.keyCode == 13){
                    //ENTER key has been pressed
                    jq('#confirmIdentifierId').click();
                }
            }

        });

        jq(".editPatientIdentifier").click(function (event) {

            var patientIdentifierId = jq(event.target).closest('.editPatientIdentifier').attr('data-patient-identifier-id');
            var identifierTypeId = jq(event.target).closest('.editPatientIdentifier').attr("data-identifier-type-id");
            var identifierTypeName = jq(event.target).closest('.editPatientIdentifier').attr("data-identifier-type-name");
            var patientIdentifierValue = jq(event.target).closest('.editPatientIdentifier').attr("data-patient-identifier-value");

            jq("#hiddenIdentifierTypeId").val(identifierTypeId);
            jq("#hiddenInitialIdentifierValue").val(patientIdentifierValue);
            jq("#hiddenPatientIdentifierId").val(patientIdentifierId);
            jq("#identifierTypeNameSpan").text(identifierTypeName);
            jq("#patientIdentifierValue").val(patientIdentifierValue);

            showEditPatientIdentifierDialog();

            jq('.confirm').attr("disabled", "disabled");
            jq('.confirm').addClass("disabled");

        });

        jq(".demographics .name").click(function () {
            emr.navigateTo({
                url: "${ ui.urlBind("/" + contextPath + config.dashboardUrl, [ patientId: patient.patient.id ] ) }"
            });
        })

        jq("#patient-header-contactInfo").click(function (){
            var contactInfoDialogDiv = jq("#contactInfoContent");

            if (contactInfoDialogDiv.hasClass('hidden')) {
                contactInfoDialogDiv.removeClass('hidden');
                jq(this).addClass('expanded');
            } else {
                contactInfoDialogDiv.addClass('hidden');
                jq(this).removeClass('expanded');
            }

            return false;
        });
    })
</script>

<div class="patient-header row <% if (patient.patient.dead) { %>dead<% } %>">

    <% if (patient.patient.dead) { %>
        <div class="death-header col-12">
            <span class="death-message">
                    ${ui.message("coreapps.deadPatient", ui.format(patient.patient.deathDate), ui.format(patient.patient.causeOfDeath))}
            </span>
            <span class="death-info-extension">
                <%= ui.includeFragment("appui", "extensionPoint", [ id: "patientHeader.deathInfo", contextModel: appContextModel ]) %>
                <% if (context.hasPrivilege("Task: coreapps.markPatientDead")) { %>
                    <a href="${ ui.pageLink("coreapps", "markPatientDead",[patientId: patient.id]) }"><i class="icon-pencil edit-action" title="${ ui.message("coreapps.edit") }"></i></a>
                <% } %>
            </span>
        </div>
    <% } %>

    <div class="demographics col-12 col-sm-7 col-md-7">
        <div class="row align-items-center">
            <div class="col-12 col-sm-auto">
                <h1 class="mt-0 mb-2 name">
                    <% patientNames?.each { %>
                        <span class="labeled">
                            <span class="${ it.key.replace('.', '-') }">
                                ${ ui.encodeHtmlContent(it.value) }
                            </span>
                            <em>${ui.message(it.key)}</em>
                        </span>
                    <% } %>
                </h1>
            </div>

            <div class="gender-age col-auto">
                <span>${ui.message("coreapps.gender." + ui.encodeHtml(patient.gender))}&nbsp;</span>
                <span>
                    <% if (patient.birthdate) { %>
                    <% if (patient.age > 0) { %>
                        ${ui.message("coreapps.ageYears", patient.age)}
                    <% } else if (patient.ageInMonths > 0) { %>
                        ${ui.message("coreapps.ageMonths", patient.ageInMonths)}
                    <% } else { %>
                        ${ui.message("coreapps.ageDays", patient.ageInDays)}
                    <% } %>
                    (<% if (patient.birthdateEstimated) { %>~<% } %>
                        <% if(ui.convertTimezones()) { %>
                            ${ ui.formatDateWithoutTimezoneConversion(patient.birthdate) })
                        <% } else { %>
                            ${ ui.formatDatePretty(patient.birthdate) })
                        <% } %>
                    <% } else { %>
                        ${ui.message("coreapps.unknownAge")}
                    <% } %>
                </span>
                <span id="edit-patient-demographics" class="edit-info ml-2">
                    <%= ui.includeFragment("appui", "extensionPoint", [ id: "patientHeader.editPatientDemographics", contextModel: appContextModel ]) %>
                </span>
            </div>

            <div class="col-6 mt-2 col-sm-8 col-md-6">
                <a href="#" id="patient-header-contactInfo" class="contact-info-label">
                    <span id="coreapps-showContactInfo" class="show">${ui.message("coreapps.patientHeader.showcontactinfo")}</span>
                    <i class="toggle-icon icon-caret-down small"></i>
                    <span class="hide">${ui.message("coreapps.patientHeader.hidecontactinfo")}</span>
                    <i class="toggle-icon icon-caret-up small"></i>
                </a>
            </div>

            <% if (firstLineFragments) { %>
                <div class="firstLineFragments col-12 mt-2">
                    <% firstLineFragments.each { %>
                        ${ ui.includeFragment(it.extensionParams.provider, it.extensionParams.fragment, [patient: config.patient])}
                    <% } %>
                </div>
            <% } %>

            <div class="hidden col-12 mt-2 ml-3" id="contactInfoContent">
                ${ ui.includeFragment("coreapps", "patientdashboard/contactInfoInline", [ patient: config.patient, contextModel: appContextModel ]) }
            </div>
        </div>
    </div>

    <div class="identifiers mt-2 col-12 col-sm-5 col-md-5 d-flex flex-column align-items-sm-end">
        <% if (config.patientIdentifierTypesToDisplay) { %>
            <% config.patientIdentifierTypesToDisplay.each { patientIdentifierType -> %>

                <% def patientIdentifiers = config.patientIdentifiersMappedByType.get(patientIdentifierType.patientIdentifierType) %>
                <div>
                    <em>${ui.format(patientIdentifierType.patientIdentifierType)}</em>
                    <% patientIdentifiers?.each { patientIdentifier -> %>
                        <%  def identifierLink = config.identifierLinks.get(patientIdentifierType.patientIdentifierType)
                        if (identifierLink) {
                            def url = identifierLink.url.replace("{{identifier}}", patientIdentifier.identifier) %>
                            <a href="${url}" target="_blank">
                                <i class="${ identifierLink.icon ?: 'icon-external-link' }" title="${ ui.message(identifierLink.label) }"></i>
                            </a>
                        <% } %>
                        <span>
                            <% if (patientIdentifierType.editable && !patientIdentifier.preferred) { %>
                                <a class="editPatientIdentifier"
                                   data-patient-identifier-id="${patientIdentifier.id}"
                                   data-identifier-type-id="${patientIdentifierType.patientIdentifierType.id}"
                                   data-identifier-type-name="${ui.format(patientIdentifierType.patientIdentifierType)}"
                                   data-patient-identifier-value="${patientIdentifier}"
                                   href="#${patientIdentifierType.patientIdentifierType.id}"
                                >
                                    ${patientIdentifier}
                                </a>
                            <% } else { %>
                                ${patientIdentifier}
                            <% } %>
                        </span>
                    <% } %>
                    <% if (patientIdentifierType.editable) { %>
                      <span class="add-link">
                         <a class="editPatientIdentifier"
                           data-patient-identifier-id=""
                           data-identifier-type-id="${patientIdentifierType.patientIdentifierType.id}"
                           data-identifier-type-name="${ui.format(patientIdentifierType.patientIdentifierType)}"
                           data-patient-identifier-value=""
                           href="#${patientIdentifierType.patientIdentifierType.id}"

                         >
                             <i class="icon-plus-sign"

                             ></i>
                         </a>
                     </span>
                    <% } %>
                </div>
            <% } %>
        <% } %>
    </div>

    <div class="unknown-patient col-12 mt-2" style= <%=(!patient.unknownPatient) ? "display:none" : ""%>>
        ${ui.message("coreapps.patient.temporaryRecord")} <br/>

        <form action="/${contextPath}/coreapps/datamanagement/mergePatients.page" method="get">
            <input type="hidden" name="app" value="coreapps.mergePatients"/>
            <input type="hidden" name="isUnknownPatient" value="true"/>
            <input type="hidden" name="patient1" value="${patient.patient.id}"/>
            <input type="submit" id="merge-button"
                   value="${ui.message("coreapps.mergePatients.mergeIntoAnotherPatientRecord.button")}"/>
        </form>
    </div>

    <div class="secondLineFragments col-12 mt-2 mb-1">
        <% secondLineFragments.each { %>
            ${ ui.includeFragment(it.extensionParams.provider, it.extensionParams.fragment, [patient: config.patient])}
        <% } %>
    </div>

    <div class="close"></div>
</div>

<div id="edit-patient-identifier-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <h3>${ui.message("coreapps.patientDashBoard.editPatientIdentifier.title")}</h3>
    </div>

    <div class="dialog-content">
        <input type="hidden" id="hiddenPatientIdentifierId" value=""/>
        <input type="hidden" id="hiddenIdentifierTypeId" value=""/>
        <input type="hidden" id="hiddenInitialIdentifierValue" value=""/>
        <ul>
            <li class="info">
                <span>${ui.message("coreapps.patient")}</span>
                <h5>${ui.encodeHtml(ui.format(patient.patient))}</h5>
            </li>
            <li class="info">
                <span id="identifierTypeNameSpan"></span>
            </li>
            <li class="info">
                <input id="patientIdentifierValue" value=""/>
            </li>
        </ul>

        <button id="confirmIdentifierId" class="confirm right">${ui.message("coreapps.confirm")}</button>
        <button class="cancel">${ui.message("coreapps.cancel")}</button>
    </div>
</div>
