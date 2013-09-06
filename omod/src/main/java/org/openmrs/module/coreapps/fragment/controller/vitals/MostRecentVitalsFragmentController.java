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
package org.openmrs.module.coreapps.fragment.controller.vitals;

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.util.ArrayList;
import java.util.List;

public class MostRecentVitalsFragmentController {
	
	public void controller(FragmentModel model, @FragmentParam("patientId") Patient patient,
	                       @SpringBean("encounterService") EncounterService encounterService) {
		
		Integer encounterId = null;
		List<EncounterType> encounterTypes = new ArrayList<EncounterType>();
		encounterTypes.add(encounterService.getEncounterTypeByUuid(CoreAppsConstants.VITALS_ENCOUNTER_TYPE_UUID));
		List<Encounter> encounters = encounterService.getEncounters(patient, null, null, null, null, encounterTypes, null,
		    null, null, false);
		
		if (encounters.size() > 0) {
            model.addAttribute("encounter", encounters.get(encounters.size() - 1));
		} else {
            model.addAttribute("encounter", null);
        }
	}
}
