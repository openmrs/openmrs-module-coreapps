<%
    def careSettings = activeDrugOrders.collect{it.careSetting}.unique();
    detailsUrl = detailsUrl ? detailsUrl.replace("{{patientUuid}}", patient.uuid) : null;
    returnUrl = returnUrl ? returnUrl.replace("{{patientUuid}}", patient.uuid) : "";
%>

<style type="text/css">
    .info-body.active-drug-orders h4 {
        font-size: 1em;
    }
    .info-body.active-drug-orders h4:first-child {
        margin-top: 0px;
    }
</style>

<div class="info-section">

    <div class="info-header">
        <i class="icon-medicine"></i>
        <h3>${ ui.message("coreapps.patientdashboard.activeDrugOrders").toUpperCase() }</h3>
        <% if (detailsUrl && context.hasPrivilege("App: orderentryui.drugOrders")) { %>
        	<a href="${ detailsUrl + "&" + "returnUrl=" + ui.urlEncode(ui.escapeJs( ui.encodeHtmlAttribute(returnUrl))) }">
                <i class="icon-share-alt edit-action right" title="${ ui.message("coreapps.edit") }"></i>
            </a>
        <% } %>
    </div>

    <div class="info-body active-drug-orders">
        <% if (!activeDrugOrders) { %>
            ${ ui.message("emr.none") }
        <% } else { %>

            <% careSettings.each { careSetting -> %>
                <h4>${ ui.format(careSetting) }</h4>
                <ul>
                    <% activeDrugOrders.findAll{ it.careSetting == careSetting }.each { %>
                    <li>
                        <label>${ ui.format(it.drug ?: it.concept) }</label>
                        <small>${ it.dosingInstructionsInstance.getDosingInstructionsAsString(sessionContext.locale) }</small>
                        <% if (displayActivationDate) { %>
                            <span style='float:right; font-size: small; color:#939393' >${ ui.formatDatetimePretty(it.dateActivated) }</span>
                        <% } %>
                    </li>
                    <% } %>
                </ul>
            <% } %>
        <% } %>
    </div>

</div>
