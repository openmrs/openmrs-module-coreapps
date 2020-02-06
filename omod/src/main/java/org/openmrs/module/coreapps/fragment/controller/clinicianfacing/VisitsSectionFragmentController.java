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

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.VisitType;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.template.TemplateFactory;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.CoreAppsProperties;
import org.openmrs.module.coreapps.contextmodel.PatientContextModel;
import org.openmrs.module.coreapps.contextmodel.VisitContextModel;
import org.openmrs.module.coreapps.utils.VisitTypeHelper;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.FragmentParam;
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

	protected final Log log = LogFactory.getLog(VisitsSectionFragmentController.class);

	public void controller(FragmentConfiguration config,
						   PageModel pageModel,
						   FragmentModel model,
						   UiUtils ui,
						   UiSessionContext sessionContext,
						   @FragmentParam("app") AppDescriptor appDescriptor, // this is the app descriptor of the Visits Sections fragment
						   @SpringBean("appframeworkTemplateFactory") TemplateFactory templateFactory,
                           @SpringBean("coreAppsProperties") CoreAppsProperties coreAppsProperties,
						   @InjectBeans PatientDomainWrapper patientWrapper, @SpringBean("adtService") AdtService adtService,
			               @SpringBean("visitTypeHelper") VisitTypeHelper visitTypeHelper) {
		config.require("patient");
		Object patient = config.get("patient");
		VisitType visitType = null;
		JsonNode visitTypeNode = appDescriptor.getConfig().path("visitType");
		if (visitTypeNode != null) {
			visitType = Context.getVisitService().getVisitTypeByUuid(visitTypeNode.getTextValue());
			if (visitType == null) {
				log.warn("Visit type with uuid: " + visitTypeNode.getTextValue() + " not found.");
			}
		}

		if (patient instanceof Patient) {
			patientWrapper.setPatient((Patient) patient);
			config.addAttribute("patient", patientWrapper);
		} else if (patient instanceof PatientDomainWrapper) {
			patientWrapper = (PatientDomainWrapper) patient;
		}

		AppContextModel contextModel = sessionContext.generateAppContextModel();
		contextModel.put("patient", new PatientContextModel(patientWrapper.getPatient()));
		contextModel.put("patientId", patientWrapper.getPatient().getUuid());  // backwards-compatible for links that still specify patient uuid substitution with "{{patientId}}"

		AppDescriptor app = (AppDescriptor) pageModel.get("app"); // this is the config of the host page e.g. CLINICIAN_DASHBOARD

		String visitsPageWithSpecificVisitUrl = null;
		String visitsPageUrl = null;

		// see if the app specifies urls to use
		if (app != null) {
			try {
				visitsPageWithSpecificVisitUrl = app.getConfig().get("visitUrl").getTextValue();
			} catch (Exception ex) { }
			try {
				visitsPageUrl = app.getConfig().get("visitsUrl").getTextValue();
			} catch (Exception ex) { }
			try {
				//is the CLINICIAN_DASHBOARD visitsPageUrl config param overwritten via the VisitsSection widget config ?
				JsonNode visitsUrlNode = appDescriptor.getConfig().path("visitsUrl");
				if (visitsUrlNode != null && StringUtils.isNotBlank(visitsUrlNode.getTextValue())) {
					visitsPageUrl = visitsUrlNode.getTextValue();
				}
			} catch (Exception ex) { }
			try {
				//is the CLINICIAN_DASHBOARD visitsPageWithSpecificVisitUrl config param ovewritten via the VisitsSection widget config ?
				JsonNode visitUrlNode = appDescriptor.getConfig().path("visitUrl");
				if (visitUrlNode != null && StringUtils.isNotBlank(visitUrlNode.getTextValue())) {
					visitsPageWithSpecificVisitUrl = visitUrlNode.getTextValue();
				}
			} catch (Exception ex) { }
		}


		if (visitsPageWithSpecificVisitUrl == null) {
            visitsPageWithSpecificVisitUrl = coreAppsProperties.getVisitsPageWithSpecificVisitUrl();
        }
		visitsPageWithSpecificVisitUrl = "/" + ui.contextPath() + "/" + visitsPageWithSpecificVisitUrl;

        if (visitsPageUrl == null) {
			visitsPageUrl = coreAppsProperties.getVisitsPageUrl();
        }

        // hack fix for RA-1002--if there is an active visit, and we are using the "regular" visit dashboard we actually want to link to the specific visit
		Location visitLocation = adtService.getLocationThatSupportsVisits(sessionContext.getSessionLocation());
		VisitDomainWrapper activeVisit = adtService.getActiveVisit(patientWrapper.getPatient(), visitLocation);
		if (visitsPageUrl.contains("/coreapps/patientdashboard/patientDashboard.page?") &&activeVisit != null) {
			visitsPageUrl = coreAppsProperties.getVisitsPageWithSpecificVisitUrl();
			contextModel.put("visit", activeVisit.getVisit());
		}

		visitsPageUrl = "/" + ui.contextPath() + "/" + visitsPageUrl;
		model.addAttribute("visitsUrl", templateFactory.handlebars(visitsPageUrl, contextModel));

		List<VisitDomainWrapper> recentVisits = visitType != null ? patientWrapper.getVisitsByTypeUsingWrappers(visitType) : patientWrapper.getAllVisitsUsingWrappers();
		if (recentVisits.size() > 5) {
			recentVisits = recentVisits.subList(0, 5);
		}

		Map<VisitDomainWrapper, String> recentVisitsWithLinks = new LinkedHashMap<VisitDomainWrapper, String>();
		for (VisitDomainWrapper recentVisit : recentVisits) {
			contextModel.put("visit", new VisitContextModel(recentVisit));
            // since the "recentVisit" isn't part of the context module, we bind it first to the visit url, before doing the handlebars binding against the context
			recentVisitsWithLinks.put(recentVisit, templateFactory.handlebars(ui.urlBind(visitsPageWithSpecificVisitUrl, recentVisit.getVisit()), contextModel));
		}

		Map<Integer, Map<String, Object>> recentVisitsWithAttr = visitTypeHelper.getVisitColorAndShortName(recentVisits);
		model.addAttribute("recentVisitsWithAttr", recentVisitsWithAttr);
		model.addAttribute("recentVisitsWithLinks", recentVisitsWithLinks);

		// this allows to overwrite the default showVisitTypeOnPatientHeaderSection GP setting via the  Visits Section widget's config param
		JsonNode showVisitType = appDescriptor.getConfig().path("showVisitTypeOnPatientHeaderSection");
		if (showVisitType != null && showVisitType.getBooleanValue()) {
			config.addAttribute("showVisitTypeOnPatientHeaderSection", showVisitType.getTextValue());
		} else {
			config.addAttribute("showVisitTypeOnPatientHeaderSection", visitTypeHelper.showVisitTypeOnPatientHeaderSection());
		}
		// this allows to overwrite the default CLINICIAN_DASHBOARD app label via the Visits Section widget's config label
		JsonNode widgetLabel = appDescriptor.getConfig().path("label");
		if (widgetLabel != null && StringUtils.isNotBlank(widgetLabel.getTextValue())) {
			config.addAttribute("label", widgetLabel.getTextValue());
		}
	}
}
