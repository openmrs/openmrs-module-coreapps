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
package org.openmrs.module.coreapps.fragment.controller.clinicianfacing;

import org.openmrs.Patient;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.template.TemplateFactory;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.CoreAppsProperties;
import org.openmrs.module.coreapps.contextmodel.PatientContextModel;
import org.openmrs.module.coreapps.contextmodel.VisitContextModel;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.ui.framework.page.PageModel;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.openmrs.Location;
import org.openmrs.module.emrapi.adt.AdtService;

/**
 * Supports the containing PageModel having an "app" property whose config defines a "visitUrl" property
 */
public class VisitsSectionFragmentController {

	public void controller(FragmentConfiguration config,
						   PageModel pageModel,
						   FragmentModel model,
						   UiUtils ui,
						   UiSessionContext sessionContext,
						   @SpringBean("appframeworkTemplateFactory") TemplateFactory templateFactory,
                           @SpringBean("coreAppsProperties") CoreAppsProperties coreAppsProperties,
						   @InjectBeans PatientDomainWrapper patientWrapper, @SpringBean("adtService") AdtService adtService) {
		config.require("patient");
		Object patient = config.get("patient");

		if (patient instanceof Patient) {
			patientWrapper.setPatient((Patient) patient);
			config.addAttribute("patient", patientWrapper);
		} else if (patient instanceof PatientDomainWrapper) {
			patientWrapper = (PatientDomainWrapper) patient;
		}

		AppContextModel contextModel = sessionContext.generateAppContextModel();
		contextModel.put("patient", new PatientContextModel(patientWrapper.getPatient()));

		AppDescriptor app = (AppDescriptor) pageModel.get("app");
		String visitsPageWithSpecificVisitUrl = null;
		String visitsPageUrl = null;
		if (app != null) {
			try {
				visitsPageWithSpecificVisitUrl = app.getConfig().get("visitUrl").getTextValue();
			} catch (Exception ex) { }
			try {
				visitsPageUrl = app.getConfig().get("visitsUrl").getTextValue();
			} catch (Exception ex) { }
		}
        if (visitsPageWithSpecificVisitUrl == null) {
            visitsPageWithSpecificVisitUrl = coreAppsProperties.getVisitsPageWithSpecificVisitUrl();
        }
        if (visitsPageUrl == null) {
			visitsPageUrl = "coreapps/patientdashboard/patientDashboard.page?patientId="+patientWrapper.getPatient().getUuid();
			Location visitLocation = adtService.getLocationThatSupportsVisits(sessionContext.getSessionLocation());
                        VisitDomainWrapper activeVisit = adtService.getActiveVisit(patientWrapper.getPatient(), visitLocation);
			visitsPageUrl += (activeVisit != null) ? "&visitId="+activeVisit.getVisit().getId()+"#visits" : "#visits";
		}
		visitsPageWithSpecificVisitUrl = "/" + ui.contextPath() + "/" + visitsPageWithSpecificVisitUrl;
        if (visitsPageUrl == null) {
            visitsPageUrl = coreAppsProperties.getVisitsPageUrl();
        }
        if (visitsPageUrl == null) {
			visitsPageUrl = "coreapps/patientdashboard/patientDashboard.page?patientId={{patient.uuid}}#visits";
		}
		visitsPageUrl = "/" + ui.contextPath() + "/" + visitsPageUrl;
		model.addAttribute("visitsUrl", templateFactory.handlebars(visitsPageUrl, contextModel));

		List<VisitDomainWrapper> recentVisits = patientWrapper.getAllVisitsUsingWrappers();
		if (recentVisits.size() > 5) {
			recentVisits = recentVisits.subList(0, 5);
		}

		Map<VisitDomainWrapper, String> recentVisitsWithLinks = new LinkedHashMap<VisitDomainWrapper, String>();
		for (VisitDomainWrapper recentVisit : recentVisits) {
			contextModel.put("visit", new VisitContextModel(recentVisit));
            // since the "recentVisit" isn't part of the context module, we bind it first to the visit url, before doing the handlebars binding against the context
			recentVisitsWithLinks.put(recentVisit, templateFactory.handlebars(ui.urlBind(visitsPageWithSpecificVisitUrl, recentVisit.getVisit()), contextModel));
		}

		model.addAttribute("recentVisitsWithLinks", recentVisitsWithLinks);
	}
}