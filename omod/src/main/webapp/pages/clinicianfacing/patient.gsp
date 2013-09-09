<%
    ui.decorateWith("appui", "standardEmrPage")
%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.format(patient.patient.familyName) }, ${ ui.format(patient.patient.givenName) }" ,
        link: '${ui.pageLink("coreapps", "clinicianfacing/clinicianFacingPatientDashboard", [patientId: patient.patient.id])}'}
    ]
    var patient = { id: ${ patient.id } };
</script>

<% if(includes){
    includes.each(){ %>
${ ui.includeFragment(it.extensionParams.resource, it.extensionParams.fragment)}
<%   }
} %>

${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient.patient, activeVisit: activeVisit ]) }

<div class="clear"></div>
<div class="container">
    <div class="dashboard clear">
        <div class="info-container column">
            ${ ui.includeFragment("coreapps", "clinicianfacing/diagnosisWidget", [ patient: patient ]) }

            ${ ui.includeFragment("coreapps", "vitals/mostRecentVitals", [patientId: patient.patient.id]) }
        </div>
        <div class="info-container column">
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
            ${ ui.includeFragment("coreapps", "clinicianfacing/visitsSection", [patient: patient]) }
        </div>
        <div class="action-container column">
            <div class="action-section">
                <% if (activeVisit) {
                    def contextModel = [ patientId: patient.id, "visit.id": activeVisit.visitId, "visit.active": true, "visit.admitted": activeVisit.admitted ]
                %>
                    <ul>
                        <h3>${ ui.message("coreapps.clinicianfacing.activeVisitActions") }</h3>
                        <% visitActions.each { ext -> %>
                            <li>
                                <a href="${ ui.escapeJs(ext.url("/" + ui.contextPath(), contextModel)) }" id="${ ext.id }">
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
                        def contextModel = [ patientId: patient.id ]
                        overallActions.each { ext -> %>
                            <a href="${ ui.escapeJs(ext.url("/" + ui.contextPath(), contextModel)) }" id="${ ext.id }">
                                <li>
                                    <i class="${ ext.icon }"></i>
                                    ${ ui.message(ext.label) }
                                </li>
                            </a>
                    <% } %>
                </ul>

                <a class="button medium " href="#">
                    <i class="icon-x-ray"></i>${ ui.message("coreapps.clinicianfacing.radiology") }
                </a>
                <a class="button medium " href="#">
                    <i class="icon-paste"></i>${ ui.message("coreapps.clinicianfacing.notes") }
                </a>
                <a class="button medium " href="#">
                    <i class="icon-paper-clip"></i>${ ui.message("coreapps.clinicianfacing.surgery") }
                </a>
            </div>
        </div>
    </div>
</div>