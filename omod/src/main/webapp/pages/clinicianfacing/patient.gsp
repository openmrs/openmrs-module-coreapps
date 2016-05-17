<%
    ui.decorateWith("appui", "standardEmrPage")

    ui.includeJavascript("coreapps", "custom/deletePatient.js")
%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.escapeJs(ui.format(patient.patient)) }" ,
            link: '${ ui.urlBind("/" + contextPath + dashboardUrl, [ patientId: patient.patient.id ] ) }'}
    ];

    jq(function(){
        jq(".tabs").tabs();

        // make sure we reload the page if the location is changes; this custom event is emitted by by the location selector in the header
        jq(document).on('sessionLocationChanged', function() {
            window.location.reload();
        });
    });

    var patient = { id: ${ patient.id } };
</script>

<% if(includeFragments){
    includeFragments.each{ %>
        ${ ui.includeFragment(it.extensionParams.provider, it.extensionParams.fragment, it.extensionParams.fragmentConfig)}
<%   }
} %>

${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient.patient, activeVisit: activeVisit, appContextModel: appContextModel ]) }

<div class="clear"></div>
<div class="container">
    <div class="dashboard clear">
        <div class="info-container column">
            <% if (firstColumnFragments) {
			    firstColumnFragments.each { %>
			        ${ ui.includeFragment(it.extensionParams.provider, it.extensionParams.fragment, [ patient: patient, patientId: patient.patient.id, app: it.appId ])}
			<%  }
			} %>

        </div>
        <div class="info-container column">
            <%/*
            <div class="info-section">
                <div class="info-header">
                    <i class="icon-medicine"></i>
                    <h3>${ ui.message("coreapps.clinicianfacing.prescribedMedication").toUpperCase() }</h3>
                </div>
                <div class="info-body">
                    <ul>
                        <li></li>
                    </ul>
                    <a class="view-more">${ ui.message("coreapps.clinicianfacing.showMoreInfo") } ></a>
                </div>
            </div>
            <div class="info-section">
                <div class="info-header">
                    <i class="icon-medical"></i>
                    <h3>${ ui.message("coreapps.clinicianfacing.allergies").toUpperCase() }</h3>
                </div>
                <div class="info-body">
                    <ul>
                        <li></li>
                    </ul>
                </div>
            </div>
            */%>
            
            <% if (secondColumnFragments) {
			    secondColumnFragments.each { %>
			        ${ ui.includeFragment(it.extensionParams.provider, it.extensionParams.fragment, [patient: patient, patientId: patient.patient.id, app: it.appId ])}
			<%   }
			} %>
			
        </div>
        <div class="action-container column">
            <div class="action-section">
                <% if (activeVisit) { %>
                    <ul>
                        <h3>${ ui.message("coreapps.clinicianfacing.activeVisitActions") }</h3>
                        <% visitActions.each { ext -> %>
                            <li>
                                <a href="${ ui.escapeJs(ext.url("/" + ui.contextPath(), appContextModel, ui.thisUrl())) }" id="${ ext.id }">
                                    <i class="${ ext.icon }"></i>
                                    ${ ui.message(ext.label) }
                                </a>
                            </li>
                        <% } %>
                    </ul>
                <% } %>
                <ul>
                    <h3>${ ui.message("coreapps.clinicianfacing.overallActions") }</h3>
                    <%
                        overallActions.each { ext -> %>
                            <a href="${ ui.escapeJs(ext.url("/" + ui.contextPath(), appContextModel, ui.thisUrl())) }" id="${ ext.id }">
                                <li>
                                    <i class="${ ext.icon }"></i>
                                    ${ ui.message(ext.label) }
                                </li>
                            </a>
                    <% } %>
                </ul>
                <%
                 def cxtModel = [ patientId: patient.id, activeVisitId: activeVisit ? activeVisit.visit.id : null]
                 otherActions.each { action -> %>
                <a id="${ action.id }" class="button medium" href="${ ui.escapeJs(action.url("/" + ui.contextPath(), cxtModel)) }">
                    <i class="${ action.icon }"></i>${ ui.message(action.label) }
                </a>
                <% } %>
            </div>
        </div>
    </div>
</div>