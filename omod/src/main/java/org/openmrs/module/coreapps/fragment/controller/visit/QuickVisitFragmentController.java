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

import org.openmrs.*;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.AppUiConstants;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.WebConstants;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.ObjectResult;
import org.openmrs.web.attribute.WebAttributeUtil;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.*;
import javax.servlet.http.HttpServletRequest;

public class QuickVisitFragmentController {
	
	@Transactional
	public FragmentActionResult create(
			@SpringBean("adtService") AdtService adtService,
			@RequestParam("patientId") Patient patient,
			@RequestParam("locationId") Location location,
			@RequestParam(value = "selectedTypeId", required = false) VisitType selectedType,
			UiUtils uiUtils, UiSessionContext emrContext, HttpServletRequest request) {

		VisitDomainWrapper activeVisit = adtService.getActiveVisit(patient, location);
		BindingResult bindingResult = null;
		if (activeVisit != null) {
			return new FailureResult(uiUtils.message("coreapps.activeVisits.alreadyExists"));
		}

        Visit visit = adtService.ensureVisit(patient, new Date(), location);

		if(selectedType != null){
			//set visit type
			visit.setVisitType(selectedType);

			// manually handle the attribute parameters
			List<VisitAttributeType> attributeTypes = Context.getVisitService().getAllVisitAttributeTypes();
			WebAttributeUtil
					.handleSubmittedAttributesForType(visit, bindingResult, VisitAttribute.class, request, attributeTypes);
			try {
				Context.getVisitService().saveVisit(visit);
			}
			catch (APIException e) {
				request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Visit.save.error");
			}
		}

		request.getSession().setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE,
		    uiUtils.message("coreapps.visit.createQuickVisit.successMessage", uiUtils.format(patient)));
		request.getSession().setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");

        SimpleObject result = SimpleObject.create("id", visit.getId().toString(), "uuid", visit.getUuid());
		return new ObjectResult(result);
	}

	public FragmentActionResult update(@RequestParam("patientId") Patient patient,
			@RequestParam("visitId") Visit visit,
			@RequestParam(value = "selectedTypeId", required = false) VisitType selectedType,
			UiUtils uiUtils,
			HttpServletRequest request) {

		BindingResult bindingResult = null;
		if(selectedType != null) {
			visit.setVisitType(selectedType);
		}

		if(request.getParameterNames().hasMoreElements()) {
			List<VisitAttributeType> attributeTypes = Context.getVisitService().getAllVisitAttributeTypes();
			WebAttributeUtil
					.handleSubmittedAttributesForType(visit, bindingResult, VisitAttribute.class, request, attributeTypes);
		}

		try {
			Context.getVisitService().saveVisit(visit);
		} catch (APIException e) {
			request.getSession().setAttribute(WebConstants.OPENMRS_ERROR_ATTR, "Visit.save.error");
		}

		request.getSession().setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE,
				uiUtils.message("coreapps.visit.updateVisit.successMessage", uiUtils.format(patient)));
		request.getSession().setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");

		SimpleObject result = SimpleObject.create("success", true, "search", "?patientId=" + visit.getPatient().getId() + "&visitId=" + visit.getId());
		return new ObjectResult(result);
	}

	public SimpleObject getConceptAnswers(@RequestParam("conceptId") Concept concept) {
		SimpleObject results = new SimpleObject();
		List<SimpleObject> values = new LinkedList<SimpleObject>();
		for(ConceptAnswer conceptAnswer : concept.getAnswers()){
			SimpleObject conceptAnswerMap = new SimpleObject();
			conceptAnswerMap.put("uuid", conceptAnswer.getUuid());
			conceptAnswerMap.put("name", conceptAnswer.getAnswerConcept().getName().getName());
			values.add(conceptAnswerMap);
		}

		results.put("results", values);

		return results;
	}
}
