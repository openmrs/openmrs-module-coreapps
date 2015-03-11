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
package org.openmrs.module.coreapps.fragment.controller;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.patient.EmrPatientService;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.List;

/**
 * AJAX search methods for patients
 */

// I don't think this is used anymore and can be removed; the search widget makes REST calls directly
@Deprecated
public class FindPatientFragmentController {
	
	public List<SimpleObject> search(@RequestParam(value = "q", required = false) String query,
	                                 @RequestParam(value = "term", required = false) String term,
	                                 @RequestParam(value = "checkedInAt", required = false) Location checkedInAt,
	                                 @RequestParam(value = "maxResults", required = false) Integer maxResults,
	                                 @SpringBean EmrPatientService service, @SpringBean EmrApiProperties emrApiProperties,
	                                 UiUtils ui) {
		if (StringUtils.isBlank(query)) {
			query = term;
		}
		int resultLimit = 100;
		if (maxResults != null && maxResults.intValue() > 0) {
			resultLimit = maxResults.intValue();
		}
		List<Patient> results = service.findPatients(query, checkedInAt, 0, resultLimit);
		return simplify(ui, emrApiProperties, results);
	}
	
	public SimpleObject searchById(@RequestParam(value = "primaryId", required = false) String primaryId,
	                               @SpringBean EmrPatientService service, @SpringBean EmrApiProperties emrApiProperties,
	                               UiUtils ui) {
		
		Patient patient = service.findPatientByPrimaryId(primaryId);
		return simplify(ui, emrApiProperties, patient);
		
	}
	
	List<SimpleObject> simplify(UiUtils ui, EmrApiProperties emrApiProperties, List<Patient> results) {
		List<SimpleObject> patients = new ArrayList<SimpleObject>(results.size());
		for (Patient patient : results) {
			patients.add(simplify(ui, emrApiProperties, patient));
		}
		return patients;
	}
	
	SimpleObject simplify(UiUtils ui, EmrApiProperties emrApiProperties, Patient patient) {
		PersonName name = patient.getPersonName();
		SimpleObject preferredName = SimpleObject.fromObject(name, ui, "givenName", "middleName", "familyName",
		    "familyName2");
		preferredName.put("name", ui.format(name));
		
		PatientIdentifierType primaryIdentifierType = emrApiProperties.getPrimaryIdentifierType();
		List<PatientIdentifier> primaryIdentifiers = patient.getPatientIdentifiers(primaryIdentifierType);
		
		SimpleObject o = SimpleObject.fromObject(patient, ui, "patientId", "gender", "age", "birthdate",
		    "birthdateEstimated");
		o.put("preferredName", preferredName);
		o.put("primaryIdentifiers", SimpleObject.fromCollection(primaryIdentifiers, ui, "identifier"));
		
		return o;
	}
}
