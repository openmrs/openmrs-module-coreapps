<div class="contact-info-inline">
    <span>
        ${ ui.format(config.patient.personAddress).replace("\n", ", ")}
        <em>${ ui.message("coreapps.person.address")}</em>
    </span>
    <span class="left-margin">
        ${config.patient.telephoneNumber ?: ''}
        <em>${ ui.message("coreapps.person.telephoneNumber")}</em>
    </span>
    <% if(!config.hideEditDemographicsButton) { %>
    <small class="edit-info" class="left-margin">
        <a href="/${contextPath}/registrationapp/editPatientContactInfo.page?patientId=${config.patient.patient.id}&appId=referenceapplication.registrationapp.registerPatient&returnUrl=${ui.urlEncode(ui.thisUrl())}">${ui.message("general.edit")}</a>
    </small>
    <% } %>
</div>