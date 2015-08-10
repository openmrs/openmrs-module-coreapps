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
						   @InjectBeans PatientDomainWrapper patientWrapper) {
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
		String visitUrl = null;
		String visitsUrl = null;
		if (app != null) {
			try {
				visitUrl = app.getConfig().get("visitUrl").getTextValue();
			} catch (Exception ex) { }
			try {
				visitsUrl = app.getConfig().get("visitsUrl").getTextValue();
			} catch (Exception ex) { }
		}
		if (visitUrl == null) {
			visitUrl = "coreapps/patientdashboard/patientDashboard.page?patientId={{patient.uuid}}&visitId={{visit.id}}#visits";
		}
		visitUrl = "/" + ui.contextPath() + "/" + visitUrl;
		if (visitsUrl == null) {
			visitsUrl = "coreapps/patientdashboard/patientDashboard.page?patientId={{patient.uuid}}#visits";
		}
		visitsUrl = "/" + ui.contextPath() + "/" + visitsUrl;
		model.addAttribute("visitsUrl", templateFactory.handlebars(visitsUrl, contextModel));

		List<VisitDomainWrapper> recentVisits = patientWrapper.getAllVisitsUsingWrappers();
		if (recentVisits.size() > 5) {
			recentVisits = recentVisits.subList(0, 5);
		}

		Map<VisitDomainWrapper, String> recentVisitsWithLinks = new LinkedHashMap<VisitDomainWrapper, String>();
		for (VisitDomainWrapper recentVisit : recentVisits) {
			contextModel.put("visit", new VisitContextModel(recentVisit));
            // since the "recentVisit" isn't part of the context module, we bind it first to the visit url, before doing the handlebars binding against the context
			recentVisitsWithLinks.put(recentVisit, templateFactory.handlebars(ui.urlBind(visitUrl, recentVisit.getVisit()), contextModel));
		}

		model.addAttribute("recentVisitsWithLinks", recentVisitsWithLinks);
	}
}
