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
package org.openmrs.module.coreapps.fragment.controller.visit;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.appui.AppUiConstants;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.ObjectResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import javax.servlet.http.HttpServletRequest;

public class QuickVisitFragmentController {
	
	@Transactional
	public FragmentActionResult create(@SpringBean("adtService") AdtService adtService,
	                                   @RequestParam("patientId") Patient patient,
	                                   @RequestParam("locationId") Location location, UiUtils uiUtils,
	                                   UiSessionContext emrContext,
	                                   HttpServletRequest request) {

		VisitDomainWrapper activeVisit = adtService.getActiveVisit(patient, location);
		if (activeVisit != null) {
			return new FailureResult(uiUtils.message("coreapps.activeVisits.alreadyExists"));
		}

        Visit visit = adtService.ensureVisit(patient, new Date(), location);

		request.getSession().setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE,
		    uiUtils.message("coreapps.visit.createQuickVisit.successMessage", uiUtils.format(patient)));
		request.getSession().setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");

        SimpleObject result = SimpleObject.create("id", visit.getId().toString(), "uuid", visit.getUuid());
		return new ObjectResult(result);
	}

}
