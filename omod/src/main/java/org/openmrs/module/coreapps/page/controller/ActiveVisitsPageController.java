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
package org.openmrs.module.coreapps.page.controller;

import org.openmrs.Location;
import org.openmrs.VisitType;
import org.openmrs.api.LocationService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;
import org.openmrs.module.coreapps.utils.VisitTypeHelper;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class ActiveVisitsPageController {
	
	public String get(UiSessionContext sessionContext, PageModel model, @SpringBean AdtService service,
	                @SpringBean("visitService") VisitService visitService, @RequestParam("app") AppDescriptor app,
				    @SpringBean("visitTypeHelper") VisitTypeHelper visitTypeHelper) {
		
		Location sessionLocation = sessionContext.getSessionLocation();
        if (sessionLocation == null) {
            return "redirect:login.htm";
        }
		Location visitLocation = null;
		if (sessionLocation != null) {
			visitLocation = service.getLocationThatSupportsVisits(sessionLocation);
		}
		if (visitLocation == null) {
			throw new IllegalStateException("Configuration required: no visit location found based on session location");
		}
		List<VisitDomainWrapper> activeVisits = service.getActiveVisits(visitLocation);
		model.addAttribute("visitSummaries", activeVisits);

		String patientPageUrl = app.getConfig().get("patientPageUrl").getTextValue();
		model.addAttribute("patientPageUrl", patientPageUrl);

        // used to determine whether or not we display a link to the patient in the results list
        model.addAttribute("canViewVisits", Context.hasPrivilege(CoreAppsConstants.PRIVILEGE_PATIENT_VISITS));

		// retrieve all different visit types, with their color and short name
		Map <Integer, Object> visitTypesWithAttr = new HashMap<Integer, Object>();

		List<VisitType> allVisitTypes = visitService.getAllVisitTypes();
		for (VisitType type : allVisitTypes) {
			Map<String, Object> typeAttr = visitTypeHelper.getVisitTypeColorAndShortName(type);
			visitTypesWithAttr.put(type.getVisitTypeId(), typeAttr);
		}

		model.addAttribute("visitTypesWithAttr", visitTypesWithAttr);

        return null;
	}
	
}
