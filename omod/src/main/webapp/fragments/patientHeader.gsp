<%
    def patient = config.patient
    def patientNames = config.patientNames

    ui.includeCss("coreapps", "patientHeader.css")
    ui.includeJavascript("coreapps", "patientdashboard/patient.js")

    appContextModel.put("returnUrl", ui.thisUrl())
%>


<script type="text/javascript">
    var addMessage = "${ ui.message("coreapps.patient.identifier.add") }";
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

            var patientIdentifierId = jq(event.target).attr('data-patient-identifier-id');
            var identifierTypeId = jq(event.target).attr("data-identifier-type-id");
            var identifierTypeName = jq(event.target).attr("data-identifier-type-name");
            var patientIdentifierValue = jq(event.target).attr("data-patient-identifier-value");

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

<div class="patient-header <% if (patient.patient.dead) { %>dead<% } %>">

    <% if (patient.patient.dead) { %>
        <div class="death-header">
            <span class="death-message">
                ${ ui.message("coreapps.deadPatient", ui.format(patient.patient.deathDate), ui.format(patient.patient.causeOfDeath)) }
            </span>
            <span class="death-info-extension">
                <%= ui.includeFragment("appui", "extensionPoint", [ id: "patientHeader.deathInfo", contextModel: appContextModel ]) %>
            </span>
        </div>
    <% } %>

    <div class="demographics">
        <h1 class="name">
            <% patientNames?.each { %>
                <span><span class="${ it.key.replace('.', '-') }">${ ui.encodeHtmlContent(it.value) }</span><em>${ui.message(it.key)}</em></span>
            <% } %>
            &nbsp;
            <span class="gender-age">
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
                (<% if (patient.birthdateEstimated) { %>~<% } %>${ ui.formatDatePretty(patient.birthdate) })          
                <% } else { %>
                    ${ui.message("coreapps.unknownAge")}
                <% } %>
                </span>
                <span id="edit-patient-demographics" class="edit-info">
                    <small>
                        <%= ui.includeFragment("appui", "extensionPoint", [ id: "patientHeader.editPatientDemographics", contextModel: appContextModel ]) %>
                    </small>
                </span>
                <a href="#" id="patient-header-contactInfo" class="contact-info-label">
                    <span id="coreapps-showContactInfo" class="show">${ui.message("coreapps.patientHeader.showcontactinfo")}</span>
                    <i class="toggle-icon icon-caret-down small"></i>
                    <span class="hide">${ui.message("coreapps.patientHeader.hidecontactinfo")}</span>
                    <i class="toggle-icon icon-caret-up small"></i>
                </a>
            </span>

            <div class="firstLineFragments">
                <% firstLineFragments.each { %>
                    ${ ui.includeFragment(it.extensionParams.provider, it.extensionParams.fragment, [patient: config.patient])}
                <% } %>
            </div>

            <div class="hidden" id="contactInfoContent" class="contact-info-content">
                ${ ui.includeFragment("coreapps", "patientdashboard/contactInfoInline", [ patient: config.patient, contextModel: appContextModel ]) }
            </div>
        </h1>

    </div>

    <div class="identifiers">
        <em>${ui.message("coreapps.patientHeader.patientId")}</em>

        <% patient.primaryIdentifiers.each { %>
        <span>${it.identifier}</span>
        <% } %>
        <br/>
        <% if (config.extraPatientIdentifierTypes) { %>

            <% config.extraPatientIdentifierTypes.each { extraPatientIdentifierType -> %>

                <% def extraPatientIdentifiers = config.extraPatientIdentifiersMappedByType.get(extraPatientIdentifierType.patientIdentifierType) %>

                <% if (extraPatientIdentifiers) { %>
                    <em>${ui.format(extraPatientIdentifierType.patientIdentifierType)}</em>

                    <% if (extraPatientIdentifierType.editable) { %>
                        <% extraPatientIdentifiers.each { extraPatientIdentifier -> %>
                             <span><a class="editPatientIdentifier" data-patient-identifier-id="${extraPatientIdentifier.id}" data-identifier-type-id="${extraPatientIdentifierType.patientIdentifierType.id}"
                                data-identifier-type-name="${ui.format(extraPatientIdentifierType.patientIdentifierType)}" data-patient-identifier-value="${extraPatientIdentifier}" href="#${extraPatientIdentifierType.patientIdentifierType.id}">${extraPatientIdentifier}</a></span>
                        <% } %>
                    <% } else {%>
                        <% extraPatientIdentifiers.each { extraPatientIdentifier -> %>
                            <span>${extraPatientIdentifier}</span>
                        <% } %>
                    <% } %>

                <% } else if (extraPatientIdentifierType.editable) { %>
                    <em>${ui.format(extraPatientIdentifierType.patientIdentifierType)}</em>
                    <span class="add-id"><a class="editPatientIdentifier"  data-patient-identifier-id="" data-identifier-type-id="${extraPatientIdentifierType.patientIdentifierType.id}"
                    data-identifier-type-name="${ui.format(extraPatientIdentifierType.patientIdentifierType)}" data-patient-identifier-value=""
                    href="#${extraPatientIdentifierType.patientIdentifierType.id}">${ui.message("coreapps.patient.identifier.add")}</a></span>
                <% } %>

            <br/>
            <% } %>
        <% } %>
    </div>

    <div class="unknown-patient" style= <%=(!patient.unknownPatient) ? "display:none" : ""%>>
        ${ui.message("coreapps.patient.temporaryRecord")} <br/>

        <form action="/${contextPath}/coreapps/datamanagement/mergePatients.page" method="get">
            <input type="hidden" name="app" value="coreapps.mergePatients"/>
            <input type="hidden" name="isUnknownPatient" value="true"/>
            <input type="hidden" name="patient1" value="${patient.patient.id}"/>
            <input type="submit" id="merge-button"
                   value="${ui.message("coreapps.mergePatients.mergeIntoAnotherPatientRecord.button")}"/>
        </form>
    </div>

    <div class="secondLineFragments">
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
                <h5>${ui.escapeJs(ui.encodeHtmlContent(ui.format(patient.patient)))}</h5>
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
