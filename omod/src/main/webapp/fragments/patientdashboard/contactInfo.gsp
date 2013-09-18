<h3>${ui.message("coreapps.patientDashBoard.contactinfo")}</h3>

<% if(!config.hideEditContactInfoButton){ %>
    <div class="right">
        <small><a href="/${contextPath}/registrationapp/editPatientContactInfo.page?patientId=${config.patient.patient.id}&appId=referenceapplication.registrationapp.registerPatient&returnUrl=${ui.urlEncode(ui.thisUrl())}">${ui.message("general.edit")}</a></small>
    </div>
<% } %>

<div class="contact-info">
    <div>
        <strong>${ ui.message("coreapps.person.address")}: </strong><br />
        <div class="left-margin">${ ui.format(config.patient.personAddress).replace("\n", "<br />") }</div>
    </div>
    <br />
    <div>
        <strong>${ ui.message("coreapps.person.telephoneNumber")}: </strong>
        <span class="left-margin">${config.patient.telephoneNumber ?: ''}</span>
    </div>
</div>