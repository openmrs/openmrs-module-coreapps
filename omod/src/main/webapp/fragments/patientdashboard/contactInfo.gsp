<h3>${ui.message("coreapps.patientDashBoard.contactinfo")}</h3>

<% if(!config.hideEditContactInfoButton){ %>
    <div class="right">
        <input type="button" value="${ui.message("general.edit")}" onclick='javascript:emr.navigateTo({url:"/${contextPath}/registrationapp/editPatientContactInfo.page?patientId=${config.patient.patient.id}&appId=referenceapplication.registrationapp.registerPatient"})' />
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