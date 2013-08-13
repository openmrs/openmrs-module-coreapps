<%
    ui.decorateWith("appui", "standardEmrPage")
%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.format(patient.patient.familyName) }, ${ ui.format(patient.patient.givenName) }" ,
        link: '${ui.pageLink("coreapps", "clinicianfacing/clinicianFacingPatientDashboard", [patientId: patient.patient.id])}'}
    ]
</script>

${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient.patient, activeVisit: activeVisit ]) }

<div class="clear"></div>
<div class="container">
    <div class="dashboard clear">
        <div class="info-container column">
            <div class="info-section">
                <div class="info-header">
                    <i class="icon-diagnosis"></i>
                    <h3>${ ui.message("coreapps.clinicianfacing.diagnosis").toUpperCase() }</h3>
                </div>
                <div class="info-body">
                    <ul>
                        <li></li>
                    </ul>
                    <a class="view-more">${ ui.message("coreapps.clinicianfacing.showMoreInfo") } ></a>
                </div>
            </div>
            <div class="info-section vitals">
                <div class="info-header">
                    <i class="icon-vitals"></i>
                    <h3>${ ui.message("coreapps.clinicianfacing.vitals").toUpperCase() }</h3>
                </div>
                <div class="info-body">
                    <ul>
                        <li></li>
                    </ul>
                    <a class="view-more">${ ui.message("coreapps.clinicianfacing.showMoreInfo") } ></a>
                </div>
            </div>
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
            <div class="info-section">
                <div class="info-header">
                    <i class="icon-calendar"></i>
                    <h3>${ ui.message("coreapps.clinicianfacing.visits").toUpperCase() }</h3>
                </div>
                <div class="info-body">
                    <ul>
                        <% patientVisits.each{ %>
                        <li class="clear">
                            <a class="visit-link">${ ui.format(it.startDatetime) } - ${ ui.format(it.stopDatetime) }</a>
                        <div class="tag">${ (it.admissionEncounter) ? ui.message("coreapps.clinicianfacing.inpatient") : ui.message("coreapps.clinicianfacing.outpatient") }</div>
                        </li>
                        <% } %>
                    </ul>
                    <a class="view-more" href="${ui.pageLink("coreapps", "patientdashboard/patientDashboard", [patientId: patient.patient.id])}">
                        ${ ui.message("coreapps.clinicianfacing.showMoreInfo") } >
                    </a>
                </div>
            </div>
        </div>
        <div class="action-container column">
            <div class="action-section">
                <ul>
                    <h3>${ ui.message("general.patient").toUpperCase() }</h3>
                    <li><i class="icon-vitals"></i>${ ui.message("coreapps.clinicianfacing.recordVitals") }</li>
                    <li><i class="icon-stethoscope"></i>${ ui.message("coreapps.clinicianfacing.writeConsultNote") }</li>
                    <li><i class="icon-stethoscope"></i>${ ui.message("coreapps.clinicianfacing.writeEdNote") }</li>
                    <li><i class="icon-x-ray"></i>${ ui.message("coreapps.clinicianfacing.orderXray") }</li>
                    <li><i class="icon-tomo"></i>${ ui.message("coreapps.clinicianfacing.orderCTScan") }</li>
                    <li><i class="icon-paste"></i>${ ui.message("coreapps.clinicianfacing.writeSurgeryNote") }</li>
                    <h3>${ ui.message("coreapps.general").toUpperCase() }</h3>
                    <li><i class="icon-folder-open"></i>${ ui.message("coreapps.clinicianfacing.requestPaperRecord") }</li>
                    <li><i class="icon-print"></i>${ ui.message("coreapps.clinicianfacing.printCardLabel") }</li>
                    <li><i class="icon-print"></i>${ ui.message("coreapps.clinicianfacing.printChartLabel") }</li>
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