<%
    def patient = config.patient

    def dateFormat = new java.text.SimpleDateFormat("dd MMM yyyy hh:mm a")

    ui.includeCss("coreapps", "patientHeader.css")
    ui.includeJavascript("coreapps", "patientdashboard/patient.js")
%>


<script type="text/javascript">
    var addMessage = "${ ui.message("emr.patient.identifier.add") }";
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
    })
</script>

<div class="patient-header">

    <div class="demographics">
        <h1 class="name">
            <span>${ui.format(patient.patient.familyName)},<em>${ui.message("emr.patientHeader.surname")}</em></span>
            <span>${ui.format(patient.patient.givenName)}<em>${ui.message("emr.patientHeader.name")}</em></span>

        </h1>

        <div class="gender-age">
            <span>${ui.message("emr.gender." + patient.gender)}</span>
            <% if (patient.birthdate) { %>
            <% if (patient.age > 0) { %>
            <span>${ui.message("emr.ageYears", patient.age)}</span>
            <% } else if (patient.ageInMonths > 0) { %>
            <span>${ui.message("emr.ageMonths", patient.ageInMonths)}</span>
            <% } else { %>
            <span>${ui.message("emr.ageDays", patient.ageInDays)}</span>
            <% } %>
            <% } else { %>
            <span>${ui.message("emr.unknownAge")}</span>
            <% } %>
        </div>

        <% if(!config.hideEditDemographicsButton){ %>
        <span>
            <input type="button" value="${ui.message("general.edit")}" onclick='javascript:emr.navigateTo({url:"/${contextPath}/registrationapp/editPatientDemographics.page?patientId=${patient.patient.id}"})' />
        </span>
        <% } %>

        <% if (patient.patient.dead) { %>
        <br>
        <br>
        <div class="death-message">
            ${ui.message("emr.deadPatient")} ${ ui.format(patient.patient.deathDate) }
        </div>
        <% } %>
        <% if (config.activeVisit) { %>
        <div class="status-container">
            <% def visit = config.activeVisit.visit %>
            <span class="status active"></span>
            ${ui.message("emr.activeVisit")}
        </div>
        <% } %>
    </div>



    <div class="identifiers">
        <em>${ui.message("emr.patientHeader.patientId")}</em>
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
                                    href="#${it.patientIdentifierType.id}">${ui.message("emr.patient.identifier.add")}</a></span>
        <% } %>

        <br/>
        <% } %>
        <% } %>
    </div>

    <div class="unknown-patient" style= <%=(!patient.unknownPatient) ? "display:none" : ""%>>
        ${ui.message("emr.patient.temporaryRecord")} <br/>

        <form action="/${contextPath}/coreapps/mergePatients.page" method="get">
            <input type="hidden" name="isUnknownPatient" value="true"/>
            <input type="hidden" name="patient1" value="${patient.patient.id}"/>
            <input type="submit" id="merge-button"
                   value="${ui.message("emr.mergePatients.mergeIntoAnotherPatientRecord.button")}"/>
        </form>
    </div>

    <div class="close"></div>
</div>

<div id="edit-patient-identifier-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <h3>${ui.message("emr.patientDashBoard.editPatientIdentifier.title")}</h3>
    </div>

    <div class="dialog-content">
        <input type="hidden" id="hiddenIdentifierTypeId" value=""/>
        <input type="hidden" id="hiddenInitialIdentifierValue" value=""/>
        <ul>
            <li class="info">
                <span>${ui.message("emr.patient")}</span>
                <h5>${ui.format(patient.patient)}</h5>
            </li>
            <li class="info">
                <span id="identifierTypeNameSpan"></span>
            </li>
            <li class="info">
                <input id="patientIdentifierValue" value=""/>
            </li>
        </ul>

        <button id="confirmIdentifierId" class="confirm right">${ui.message("emr.confirm")}</button>
        <button class="cancel">${ui.message("emr.cancel")}</button>
    </div>
</div>
