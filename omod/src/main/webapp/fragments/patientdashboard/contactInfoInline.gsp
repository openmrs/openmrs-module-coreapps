<%
    config.contextModel.put("returnUrl", ui.thisUrl())
%>

<div class="contact-info-inline">
    <span>
        ${ ui.encodeHtmlContent(ui.format(config.patient.personAddress).replace("\n", ", "))}
        <em>${ ui.message("coreapps.person.address")}</em>
    </span>
    <span class="left-margin">
        <span id="coreapps-telephoneNumber">
            ${ ui.encodeHtmlContent(config.patient.telephoneNumber ?: '') }
        </span>
        <em>${ ui.message("coreapps.person.telephoneNumber")}</em>
    </span>
    <% if(!config.hideEditDemographicsButton) { %>
    <small id="contact-info-inline-edit" class="edit-info" class="left-margin">
        <%= ui.includeFragment("appui", "extensionPoint", [ id: "patientHeader.editPatientContactInfo", contextModel: config.contextModel ]) %>
    </small>
    <% } %>
</div>
