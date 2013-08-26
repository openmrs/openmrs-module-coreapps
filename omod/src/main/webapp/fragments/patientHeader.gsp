<%
    def patient = config.patient

    def dateFormat = new java.text.SimpleDateFormat("dd MMM yyyy hh:mm a")

    ui.includeCss("coreapps", "patientHeader.css")
    ui.includeJavascript("coreapps", "patientdashboard/patient.js")
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
            var identifierTypeId = jq(event.target).attr("data-identifier-type-id");
            var identifierTypeName = jq(event.target).attr("data-identifier-type-name");
            var patientIdentifierValue = jq(event.target).attr("data-patient-identifier-value");

            jq("#hiddenIdentifierTypeId").val(identifierTypeId);
            jq("#hiddenInitialIdentifierValue").val(patientIdentifierValue);
            jq("#identifierTypeNameSpan").text(identifierTypeName);
            jq("#patientIdentifierValue").val(patientIdentifierValue);

            showEditPatientIdentifierDialog();

            jq('.confirm').attr("disabled", "disabled");
            jq('.confirm').addClass("disabled");

        });

        jq(".demographics .name").click(function () {
            emr.navigateTo({
                provider: 'coreapps',
                page: 'patientdashboard/patientDashboard',
                query: { patientId: ${patient.patient.id} }
            });
        })

        <% if (config.isNewPatientHeaderEnabled) { %>
        jq("div#contactInfoContent").dialog({
            autoOpen: false,
            width: '700',
            height: '250',
            show: 'slideDown',
            hide: 'slideUp',
            position: { my: "${ (!config.activeVisit) ? "left " : "" }top", at: "${ (!config.activeVisit) ? "left " : "" }bottom", of: jq("div#patient-header-contactInfo") }
        });

        jq("div#patient-header-contactInfo").click(function(){
            var contactInfoDialogDiv = jq("#contactInfoContent");
            //hide the title bar
            contactInfoDialogDiv.siblings('.ui-dialog-titlebar').first().hide();
            if(contactInfoDialogDiv.dialog('isOpen')){
                contactInfoDialogDiv.dialog('close');
                jq(this).children('i.toggle-icon').removeClass('icon-caret-up');
                jq(this).children('i.toggle-icon').addClass('icon-caret-down');
                return
            }

            contactInfoDialogDiv.dialog('open');
            jq(this).children('i.toggle-icon').removeClass('icon-caret-down');
            jq(this).children('i.toggle-icon').addClass('icon-caret-up');
        });
        <% } %>
    })
</script>

<div class="patient-header">

    <div class="demographics">
        <h1 class="name">
            <span>${ui.format(patient.patient.familyName)},<em>${ui.message("coreapps.patientHeader.surname")}</em></span>
            <span>${ui.format(patient.patient.givenName)}<em>${ui.message("coreapps.patientHeader.name")}</em></span>

        </h1>

        <div class="gender-age">
            <span>${ui.message("coreapps.gender." + patient.gender)}</span>
            <% if (patient.birthdate) { %>
            <% if (patient.age > 0) { %>
            <span>${ui.message("coreapps.ageYears", patient.age)}</span>
            <% } else if (patient.ageInMonths > 0) { %>
            <span>${ui.message("coreapps.ageMonths", patient.ageInMonths)}</span>
            <% } else { %>
            <span>${ui.message("coreapps.ageDays", patient.ageInDays)}</span>
            <% } %>
            <% } else { %>
            <span>${ui.message("coreapps.unknownAge")}</span>
            <% } %>
        </div>

        <% if(!config.hideEditDemographicsButton){ %>
        <span>
            <input type="button" value="${ui.message("general.edit")}" onclick='javascript:emr.navigateTo({url:"/${contextPath}/registrationapp/editPatientDemographics.page?patientId=${patient.patient.id}"})' />
        </span>
        <% } %>
        <% def skipLineBreak = false %>
        <% if (patient.patient.dead) { %>
        <br /><% skipLineBreak = true %>
        <div class="death-message">
            ${ui.message("coreapps.deadPatient", ui.format(patient.patient.deathDate))}
        </div>
        <% } %>
        <% if (config.activeVisit) { %>
        <% def visit = config.activeVisit.visit %>
        <% if (config.isNewPatientHeaderEnabled) { %>
        <% if(!skipLineBreak) { %><br /><% skipLineBreak = true } %>
        <div class="active-visit-started-at-message">
            ${ui.message("coreapps.patientHeader.activeVisit.at", config.activeVisitStartDatetime)}
        </div>
        <% if (config.activeVisit.admitted) { %>
        <div class="active-visit-message">
            ${ui.message("coreapps.patientHeader.activeVisit.inpatient", ui.format(config.activeVisit.latestAdtEncounter.location))}
        </div>
        <% } else { %>
        <div class="active-visit-message">
            ${ui.message("coreapps.patientHeader.activeVisit.outpatient")}
        </div>
        <% } %>
        <% } else {%>
        <div class="status-container">
            <span class="status active"></span>
            ${ui.message("coreapps.activeVisit")}
        </div>
        <% } %>
        <% } %>
        <% if (config.isNewPatientHeaderEnabled) { %>
        <% if(!skipLineBreak) { %><br /><% } %>
        <div id="patient-header-contactInfo" class="white-bordered-message">
            ${ui.message("coreapps.patientHeader.contactinfo")} <i class="toggle-icon icon-caret-down small"></i>
        </div>
        <div id="contactInfoContent">
            ${ui.includeFragment("coreapps", "patientdashboard/contactInfo", [ patient: config.patient ])}
        </div>
        <% } %>
    </div>



    <div class="identifiers">
        <em>${ui.message("coreapps.patientHeader.patientId")}</em>
        <% patient.primaryIdentifiers.each { %>
        <span>${it.identifier}</span>
        <% } %>
        <br/>
        <% if (config.extraPatientIdentifierTypes) { %>
        <% config.extraPatientIdentifierTypes.each { %>
        <% def extraPatientIdentifier = patient.patient.getPatientIdentifier(it.patientIdentifierType) %>
        <% if (extraPatientIdentifier) { %>
            <em>${ui.format(it.patientIdentifierType)}</em>
            <% if (it.editable) { %>
                 <span><a class="editPatientIdentifier" data-identifier-type-id="${it.patientIdentifierType.id}" data-identifier-type-name="${ui.format(it.patientIdentifierType)}"
                    data-patient-identifier-value="${extraPatientIdentifier}" href="#${it.patientIdentifierType.id}">${extraPatientIdentifier}</a></span>
            <% } else {%>
                <span>${extraPatientIdentifier}</span>
            <% } %>
        <% } else if (it.editable) { %>
            <em>${ui.format(it.patientIdentifierType)}</em>
            <span class="add-id"><a class="editPatientIdentifier" data-identifier-type-id="${it.patientIdentifierType.id}"
                                    data-identifier-type-name="${ui.format(it.patientIdentifierType)}" data-patient-identifier-value=""
                                    href="#${it.patientIdentifierType.id}">${ui.message("coreapps.patient.identifier.add")}</a></span>
        <% } %>

        <br/>
        <% } %>
        <% } %>
    </div>

    <div class="unknown-patient" style= <%=(!patient.unknownPatient) ? "display:none" : ""%>>
        ${ui.message("coreapps.patient.temporaryRecord")} <br/>

        <form action="/${contextPath}/emr/mergePatients.page" method="get">
            <input type="hidden" name="isUnknownPatient" value="true"/>
            <input type="hidden" name="patient1" value="${patient.patient.id}"/>
            <input type="submit" id="merge-button"
                   value="${ui.message("coreapps.mergePatients.mergeIntoAnotherPatientRecord.button")}"/>
        </form>
    </div>

    <div class="close"></div>
</div>

<div id="edit-patient-identifier-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <h3>${ui.message("coreapps.patientDashBoard.editPatientIdentifier.title")}</h3>
    </div>

    <div class="dialog-content">
        <input type="hidden" id="hiddenIdentifierTypeId" value=""/>
        <input type="hidden" id="hiddenInitialIdentifierValue" value=""/>
        <ul>
            <li class="info">
                <span>${ui.message("coreapps.patient")}</span>
                <h5>${ui.format(patient.patient)}</h5>
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
