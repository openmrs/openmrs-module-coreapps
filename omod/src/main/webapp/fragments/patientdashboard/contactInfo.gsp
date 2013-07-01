<h3>${ui.message("emr.patientDashBoard.contactinfo")}</h3>

<% if(!config.hideEditContactInfoButton){ %>
<div class="right">
    <input type="button" value="${ui.message("general.edit")}" onclick='javascript:emr.navigateTo({url:"/${contextPath}/registrationapp/editPatientContactInfo.page?patientId=${patient.patient.id}&appId=referenceapplication.registrationapp.registerPatient"})' />
</div>
<% } %>

<div class="contact-info">
    <ul>
        <li><strong>${ ui.message("emr.person.address")}: </strong>
        <% addressHierarchyLevels.each { addressLevel -> %>
           <% if(patient.personAddress && patient.personAddress[addressLevel]) { %>
                 ${patient.personAddress[addressLevel]}<% if(addressLevel != addressHierarchyLevels.last()){%>,<%}%>
            <% }%>
        <% } %>
        </li>
        <li><strong>${ ui.message("emr.person.telephoneNumber")}:</strong> ${patient.telephoneNumber ?: ''}</li>
    </ul>
</div>