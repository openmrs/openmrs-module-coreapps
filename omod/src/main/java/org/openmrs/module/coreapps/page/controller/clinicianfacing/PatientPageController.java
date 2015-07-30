/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.coreapps.page.controller.clinicianfacing;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.module.coreapps.CoreAppsProperties;
import org.openmrs.module.coreapps.contextmodel.PatientContextModel;
import org.openmrs.module.coreapps.contextmodel.VisitContextModel;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.event.ApplicationEventService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.Redirect;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PatientPageController {
	
	public Object controller(@RequestParam("patientId") Patient patient, PageModel model,
	                         @RequestParam(required = false, value = "app") AppDescriptor app,
	                         @InjectBeans PatientDomainWrapper patientDomainWrapper,
	                         @SpringBean("adtService") AdtService adtService,
	                         @SpringBean("visitService") VisitService visitService,
	                         @SpringBean("encounterService") EncounterService encounterService,
	                         @SpringBean("emrApiProperties") EmrApiProperties emrApiProperties,
                             @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
                             @SpringBean("applicationEventService") ApplicationEventService applicationEventService,
                             @SpringBean("coreAppsProperties") CoreAppsProperties coreAppsProperties,
	                         UiSessionContext sessionContext) {

        if (!Context.hasPrivilege(CoreAppsConstants.PRIVILEGE_PATIENT_DASHBOARD)) {
            return new Redirect("coreapps", "noAccess", "");
        }
        else if (patient.isVoided() || patient.isPersonVoided()) {
			return new Redirect("coreapps", "patientdashboard/deletedPatient", "patientId=" + patient.getId());
		}
		
		patientDomainWrapper.setPatient(patient);
		model.addAttribute("patient", patientDomainWrapper);
		model.addAttribute("app", app);

		Location visitLocation = null;
		try {
			visitLocation = adtService.getLocationThatSupportsVisits(sessionContext.getSessionLocation());
		}
		catch (IllegalArgumentException ex) {
			// location does not support visits
		}
		
		VisitDomainWrapper activeVisit = null;
		if (visitLocation != null) {
			activeVisit = adtService.getActiveVisit(patient, visitLocation);
		}
		model.addAttribute("activeVisit", activeVisit);

        AppContextModel contextModel = sessionContext.generateAppContextModel();
        contextModel.put("patient", new PatientContextModel(patient));
        contextModel.put("visit", activeVisit == null ? null : new VisitContextModel(activeVisit));
        model.addAttribute("appContextModel", contextModel);

        List<Extension> overallActions = appFrameworkService.getExtensionsForCurrentUser("patientDashboard.overallActions", contextModel);
        Collections.sort(overallActions);
        model.addAttribute("overallActions", overallActions);

        List<Extension> visitActions;
        if (activeVisit == null) {
            visitActions = new ArrayList<Extension>();
        } else {
            visitActions = appFrameworkService.getExtensionsForCurrentUser("patientDashboard.visitActions", contextModel);
            Collections.sort(visitActions);
        }
        model.addAttribute("visitActions", visitActions);

        List<Extension> includeFragments = appFrameworkService.getExtensionsForCurrentUser("patientDashboard.includeFragments");
        Collections.sort(includeFragments);
        model.addAttribute("includeFragments", includeFragments);
        
        List<Extension> firstColumnFragments = appFrameworkService.getExtensionsForCurrentUser("patientDashboard.firstColumnFragments");
        Collections.sort(firstColumnFragments);
        model.addAttribute("firstColumnFragments", firstColumnFragments);
        
        List<Extension> secondColumnFragments = appFrameworkService.getExtensionsForCurrentUser("patientDashboard.secondColumnFragments");
        Collections.sort(secondColumnFragments);
        model.addAttribute("secondColumnFragments", secondColumnFragments);

        List<Extension> otherActions = appFrameworkService.getExtensionsForCurrentUser(
                "clinicianFacingPatientDashboard.otherActions", contextModel);
        Collections.sort(otherActions);
        model.addAttribute("otherActions", otherActions);

        model.addAttribute("dashboardUrl", coreAppsProperties.getDashboardUrl());

        applicationEventService.patientViewed(patient, sessionContext.getCurrentUser());

		return null;
	}

}
