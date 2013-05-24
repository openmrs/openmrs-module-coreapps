<h3>${ui.message("emr.patientDashBoard.contactinfo")}</h3>

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