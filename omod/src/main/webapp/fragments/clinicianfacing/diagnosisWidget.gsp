<div id="coreapps-diagnosesList" class="info-section">
    <div class="info-header">
        <i class="icon-diagnosis"></i>
        <h3>${ ui.message("coreapps.clinicianfacing.diagnoses").toUpperCase() }</h3>
    </div>
    <div class="info-body">
		<% if (!config.recentDiagnoses) { %>
		${ ui.message("coreapps.none.inLastPeriod", ui.encodeHtmlContent(ui.format(config.recentPeriodIndays))) }
		<% } else { %>
        <ul>
            <% config.recentDiagnoses.each { %>
            <li>
            <% if(it.diagnosis.nonCodedAnswer) { %>
                "${ui.escapeHtml(it.diagnosis.nonCodedAnswer)}"
            <% } else { %>
                ${ui.format(it.diagnosis.codedAnswer)}
            <% } %>
            </li>
            <% } %>
        </ul>
		<% } %>
        <!-- <a class="view-more">${ ui.message("coreapps.clinicianfacing.showMoreInfo") } ></a> //-->
    </div>
</div>
