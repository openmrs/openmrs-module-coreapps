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
                    	<li>
                    		<% if( lastVitalsDate ){ %> 
                    			${ ui.message("coreapps.clinicianfacing.lastVitalsDateLabel") } ${ lastVitalsDate }
                    		<% } %>
                    	</li> 
                    	
                        <% vitals.each{ %>
	                        <li>
	                        	<% if(it.answer){ %> 
	                        		<small> ${ it.question } </small> ${ it.answer }
	                        	<% } %>
	                        </li>
                        <% } %>
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
                    <% if (patientVisits.isEmpty()) { %>
                    ${ui.message("coreapps.none")}
                    <% } %>
                    <ul>
                        <% patientVisits.each{ %>
                        <li class="clear">
                            <a href="${ui.pageLink("coreapps", "patientdashboard/patientDashboard", [patientId: patient.patient.id, visitId: it.visitId])}#visits" class="visit-link">
                                ${ ui.formatDatePretty(it.startDatetime) }
                            <% if(it.stopDatetime && !it.startDatetime.format("yyyy/MM/dd").equals(it.stopDatetime.format("yyyy/MM/dd"))){ %> - ${ ui.formatDatePretty(it.stopDatetime) }<% } %>
                            </a>
                        <div class="tag">
                            <% if (it.stopDatetime == null || new Date().before(it.stopDatetime)) { %> ${ ui.message("coreapps.clinicianfacing.active") } - <% } %>
                            ${ (it.admissionEncounter) ? ui.message("coreapps.clinicianfacing.inpatient") : ui.message("coreapps.clinicianfacing.outpatient") }
                        </div>
                        </li>
                        <% } %>
                    </ul>
                    <a class="view-more" href="${ui.pageLink("coreapps", "patientdashboard/patientDashboard", [patientId: patient.patient.id])}#visits">
                        ${ ui.message("coreapps.clinicianfacing.showMoreInfo") } >
                    </a>
                </div>
            </div>
        </div>
        <div class="action-container column">
            <div class="action-section">
                <ul>
                    <h3>${ ui.message("general.patient").toUpperCase() }</h3>
                    <li>
                        <i class="icon-vitals"></i>${ ui.message("coreapps.clinicianfacing.recordVitals") }
                    </li>
                    <% if(activeVisit){ %>
                    <li>
                        <a href="/${contextPath}/htmlformentryui/htmlform/enterHtmlFormWithSimpleUi.page?patientId=${patient.id}&visitId=${activeVisit.visit.id}&definitionUiResource=referenceapplication:htmlforms/consultNote.xml">
                            <i class="icon-stethoscope"></i>${ ui.message("coreapps.clinicianfacing.writeConsultNote") }
                        </a>
                    </li>
                    <% } %>
                    <li>
                        <i class="icon-stethoscope"></i>${ ui.message("coreapps.clinicianfacing.writeEdNote") }
                    </li>
                    <li>
                        <i class="icon-x-ray"></i>${ ui.message("coreapps.clinicianfacing.orderXray") }
                    </li>
                    <li>
                        <i class="icon-tomo"></i>${ ui.message("coreapps.clinicianfacing.orderCTScan") }
                    </li>
                    <li>
                        <i class="icon-paste"></i>${ ui.message("coreapps.clinicianfacing.writeSurgeryNote") }
                    </li>
                    <h3>${ ui.message("coreapps.general").toUpperCase() }</h3>
                    <li>
                        <i class="icon-folder-open"></i>${ ui.message("coreapps.clinicianfacing.requestPaperRecord") }
                    </li>
                    <li>
                        <i class="icon-print"></i>${ ui.message("coreapps.clinicianfacing.printCardLabel") }
                    </li>
                    <li>
                        <i class="icon-print"></i>${ ui.message("coreapps.clinicianfacing.printChartLabel") }
                    </li>
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