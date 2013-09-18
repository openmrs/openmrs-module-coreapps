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

import org.apache.http.HttpRequest;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.OrderService;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.feature.FeatureToggleProperties;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.contextmodel.VisitContextModel;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.Redirect;
import org.springframework.web.bind.annotation.RequestParam;

import javax.script.Bindings;
import javax.script.SimpleBindings;
import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.List;

public class PatientDashboardPageController {
	
	private static final String ENCOUNTER_TEMPLATE_EXTENSION = "org.openmrs.referenceapplication.encounterTemplate";
	
	public Object controller(@RequestParam("patientId") Patient patient,
	                       @RequestParam(value = "tab", defaultValue = "visits") String selectedTab,
						   @RequestParam(value = "returnUrl", required = false) String returnUrl, PageModel model,
	                       @InjectBeans PatientDomainWrapper patientDomainWrapper,
	                       @SpringBean("orderService") OrderService orderService,
	                       @SpringBean("adtService") AdtService adtService,
	                       @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
                           @SpringBean("featureToggles") FeatureToggleProperties featureToggleProperties,
                           UiSessionContext sessionContext) {
		
        if (patient.isVoided() || patient.isPersonVoided()) {
            return new Redirect("coreapps", "patientdashboard/deletedPatient", "patientId=" + patient.getId());
        }

		patientDomainWrapper.setPatient(patient);
		model.addAttribute("returnUrl", returnUrl);
		model.addAttribute("patient", patientDomainWrapper);
		model.addAttribute("orders", orderService.getOrdersByPatient(patient));
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
		        .getExtensionsForCurrentUser(ENCOUNTER_TEMPLATE_EXTENSION);
		model.addAttribute("encounterTemplateExtensions", encounterTemplateExtensions);


		Bindings bindings = new SimpleBindings();
		bindings.put("patientId", patient.getPatientId());
		bindings.put("patientDead", patient.isDead());
		bindings.put("visit", activeVisit == null ? null : new VisitContextModel(activeVisit));
		model.addAttribute("actionBindings", bindings);

		List<Extension> overallActions = appFrameworkService.getExtensionsForCurrentUser("patientDashboard.overallActions", bindings);
		Collections.sort(overallActions);
		model.addAttribute("overallActions", overallActions);

        List<Extension> includeFragments = appFrameworkService.getExtensionsForCurrentUser("patientDashboard.includeFragments");
        Collections.sort(includeFragments);
        model.addAttribute("includeFragments", includeFragments);
		
		List<Extension> visitActions = appFrameworkService.getExtensionsForCurrentUser("patientDashboard.visitActions");

        // TODO this is a whole block can be removed once this feature is enabled
        if (!featureToggleProperties.isFeatureEnabled("orderRadiologyRetrospective"))  {
            for (Extension visitAction : visitActions) {
                if (visitAction.getId().equals("org.openmrs.module.radiologyapp.orderXray") ||
                        visitAction.getId().equals("org.openmrs.module.radiologyapp.orderCT")) {
                       visitAction.setRequire("visit.active");
                }
            }
        }

		Collections.sort(visitActions);
		model.addAttribute("visitActions", visitActions);
		model.addAttribute("patientTabs", appFrameworkService.getExtensionsForCurrentUser("patientDashboard.tabs"));
        model.addAttribute("isNewPatientHeaderEnabled",
                featureToggleProperties.isFeatureEnabled("enableNewPatientHeader"));
        return null;
	}
}
