<%
    config.contextModel.put("returnUrl", ui.thisUrl())
%>

<h3>${ui.message("coreapps.patientDashBoard.contactinfo")}</h3>

<div class="right">
    <small>
       <%= ui.includeFragment("appui", "extensionPoint", [ id: "patientHeader.editPatientContactInfo", contextModel: config.contextModel ]) %>
    </small>
</div>

<div class="contact-info">
    <div>
        <strong>${ ui.message("coreapps.person.address")}: </strong><br />
        <div class="ml-2">${ ui.format(config.patient.personAddress).replace("\n", "<br />") }</div>
    </div>
    <br />
    <div>
        <strong>${ ui.message("coreapps.person.telephoneNumber")}: </strong>
        <span class="ml-2">${config.patient.telephoneNumber ?: ''}</span>
    </div>
</div>
