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
package org.openmrs.module.coreapps.page.controller.patientdashboard;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.feature.FeatureToggleProperties;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.module.coreapps.contextmodel.VisitContextModel;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.event.ApplicationEventService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.Redirect;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

public class PatientDashboardPageController {

    public Object controller(@RequestParam("patientId") Patient patient,
	                       @RequestParam(value = "tab", defaultValue = "visits") String selectedTab,
						   @RequestParam(value = "returnUrl", required = false) String returnUrl, PageModel model,
	                       @InjectBeans PatientDomainWrapper patientDomainWrapper,
	                       @SpringBean("orderService") OrderService orderService,
	                       @SpringBean("adtService") AdtService adtService,
	                       @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
                           @SpringBean("featureToggles") FeatureToggleProperties featureToggleProperties,
                           @SpringBean("applicationEventService") ApplicationEventService applicationEventService,
                           UiSessionContext sessionContext) {
		
        if (patient.isVoided() || patient.isPersonVoided()) {
            return new Redirect("coreapps", "patientdashboard/deletedPatient", "patientId=" + patient.getId());
        }

		patientDomainWrapper.setPatient(patient);
		model.addAttribute("returnUrl", returnUrl);
		model.addAttribute("patient", patientDomainWrapper);
		model.addAttribute("selectedTab", selectedTab);

        Location visitLocation = null;
        try {
            visitLocation = adtService.getLocationThatSupportsVisits(sessionContext.getSessionLocation());
        } catch (IllegalArgumentException ex) {
            // location does not support visits
        }

		VisitDomainWrapper activeVisit = null;
        if (visitLocation != null) {
            activeVisit = adtService.getActiveVisit(patient, visitLocation);
        }
		model.addAttribute("activeVisit", activeVisit);

		List<Extension> encounterTemplateExtensions = appFrameworkService
		        .getExtensionsForCurrentUser(CoreAppsConstants.ENCOUNTER_TEMPLATE_EXTENSION);
		model.addAttribute("encounterTemplateExtensions", encounterTemplateExtensions);


        AppContextModel contextModel = sessionContext.generateAppContextModel();
        contextModel.put("patientId", patient.getPatientId());
        contextModel.put("patientDead", patient.isDead());
        contextModel.put("visit", activeVisit == null ? null : new VisitContextModel(activeVisit));
		model.addAttribute("actionBindings", contextModel);

		List<Extension> overallActions = appFrameworkService.getExtensionsForCurrentUser("patientDashboard.overallActions", contextModel);
		Collections.sort(overallActions);
		model.addAttribute("overallActions", overallActions);

        List<Extension> includeFragments = appFrameworkService.getExtensionsForCurrentUser("patientDashboard.includeFragments");
        Collections.sort(includeFragments);
        model.addAttribute("includeFragments", includeFragments);
		
		List<Extension> visitActions = appFrameworkService.getExtensionsForCurrentUser("patientDashboard.visitActions");

		Collections.sort(visitActions);
		model.addAttribute("visitActions", visitActions);
		model.addAttribute("patientTabs", appFrameworkService.getExtensionsForCurrentUser("patientDashboard.tabs"));
        model.addAttribute("isNewPatientHeaderEnabled",
                featureToggleProperties.isFeatureEnabled("enableNewPatientHeader"));

        applicationEventService.patientViewed(patient, sessionContext.getCurrentUser());

        return null;
	}
}
