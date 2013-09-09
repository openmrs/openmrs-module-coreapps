<%
    def recentDiagnoses = config.patient.recentDiagnoses
%>
<div class="info-section">
    <div class="info-header">
        <i class="icon-diagnosis"></i>
        <h3>${ ui.message("coreapps.clinicianfacing.diagnosis").toUpperCase() }</h3>
    </div>
    <div class="info-body">
        <ul>
            <% recentDiagnoses.each { %>
            <li>
            <% if(it.diagnosis.nonCodedAnswer) { %>
                "${it.diagnosis.nonCodedAnswer}"
            <% } else { %>
                ${it.diagnosis.codedAnswer.getName(context.locale)}
            <% } %>
            </li>
            <% } %>
        </ul>
        <!-- <a class="view-more">${ ui.message("coreapps.clinicianfacing.showMoreInfo") } ></a> //-->
    </div>
</div>