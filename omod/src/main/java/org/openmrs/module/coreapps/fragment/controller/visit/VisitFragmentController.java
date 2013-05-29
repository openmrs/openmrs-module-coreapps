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

import java.util.Date;

import javax.servlet.http.HttpServletRequest;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.appui.AppUiConstants;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapperFactory;
import org.openmrs.module.emrapi.visit.VisitDomainWrapperRepository;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

public class VisitFragmentController {
	
	@Transactional
	public FragmentActionResult start(@SpringBean("visitDomainWrapperFactory") VisitDomainWrapperFactory visitWrapperFactory,
	                                  @SpringBean("visitDomainWrapperRepository") VisitDomainWrapperRepository visitWrapperRepository,
	                                  @SpringBean("adtService") AdtService adtService,
	                                  @RequestParam("patientId") Patient patient,
	                                  @RequestParam("locationId") Location location,
	                                  @RequestParam(value = "stopActiveVisit", required = false) Boolean stopActive,
	                                  VisitDomainWrapper activeVisit, UiUtils uiUtils, HttpServletRequest request) {
		
		// if patient has an active visit close it
		if (patient != null && (activeVisit != null)) {
			Visit visit = activeVisit.getVisit();
			if (visit != null && stopActive) {
				adtService.closeAndSaveVisit(visit);
			}
		}
		// create new visit and save it
		VisitDomainWrapper visitWrapper = visitWrapperFactory.createNewVisit(patient, location, new Date());
		visitWrapperRepository.persist(visitWrapper);
		
		request.getSession().setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE,
		    uiUtils.message("emr.visit.createQuickVisit.successMessage", uiUtils.format(patient)));
		request.getSession().setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");
		return new SuccessResult();
	}
	
}
