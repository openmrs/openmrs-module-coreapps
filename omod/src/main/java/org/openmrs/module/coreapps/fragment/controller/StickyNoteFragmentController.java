package org.openmrs.module.coreapps.fragment.controller;
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


import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.util.OpenmrsConstants;

public class StickyNoteFragmentController {

	protected final Log log = LogFactory.getLog(getClass());

	public void controller(
			FragmentModel model,
			@FragmentParam("patient") PatientDomainWrapper patientWrapper,
			@SpringBean("conceptService") ConceptService conceptService,
			@SpringBean("adminService") AdministrationService adminService,
			UiUtils ui
			)

	{
		Map<String, Object> jsonConfig = new HashMap<String, Object>();
		Patient patient = patientWrapper.getPatient();
		String conceptMapping = adminService.getGlobalProperty(CoreAppsConstants.CONCEPT_STICKY_NOTE_PROPERTY_NAME);
		String[] splitMapping = conceptMapping.split(":");
		
		Concept concept = null;
	
		if (splitMapping.length == 2) {
			concept = conceptService.getConceptByMapping(splitMapping[1], splitMapping[0]);
		}
		if (concept == null) {
			log.error("Could not load the Sticky Note concept. The most probable cause is that the concept mapping provided in the \""+ 
					CoreAppsConstants.CONCEPT_STICKY_NOTE_PROPERTY_NAME +"\" global property" +
					" does not match with any existing concept. Verify that this value is correct." +
					" (Mapping value: " + conceptMapping +")");
		}
		
		String locale = Context.getLocale().getLanguage();
		String defaultLocale = new Locale(adminService.getGlobalProperty((OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE), "en")).getLanguage();
		
		jsonConfig.put("patient", ConversionUtil.convertToRepresentation(patient, Representation.REF));
		jsonConfig.put("concept", ConversionUtil.convertToRepresentation(concept, Representation.REF));
		jsonConfig.put("locale", locale);
		jsonConfig.put("defaultLocale", defaultLocale);
		
		model.addAttribute("patient", patient);
		model.addAttribute("jsonConfig", ui.toJson(jsonConfig));
	}
}