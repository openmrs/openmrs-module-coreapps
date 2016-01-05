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

package org.openmrs.module.coreapps.fragment.controller.patientheader;

import org.apache.commons.lang.time.DateFormatUtils;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 
 * 
 * 
 */
public class ActiveVisitStatusFragmentController {

	public void controller(FragmentConfiguration config, @RequestParam("patientId") Patient patient,
			@InjectBeans PatientDomainWrapper wrapper, @SpringBean("adtService") AdtService adtService,
			UiSessionContext sessionContext, UiUtils uiUtils, FragmentModel model) {

		model.addAttribute("activeVisitStartDatetime", null);
		
		VisitDomainWrapper activeVisit = (VisitDomainWrapper) config.getAttribute("activeVisit");
		if (activeVisit == null) {
			try {
				Location visitLocation = adtService.getLocationThatSupportsVisits(sessionContext.getSessionLocation());
				activeVisit = adtService.getActiveVisit(wrapper.getPatient(), visitLocation);
			} catch (IllegalArgumentException ex) {
				// location does not support visits
			}
		}
		model.addAttribute("patient", patient);
		if (activeVisit != null) {
			model.addAttribute("activeVisit", activeVisit);
			model.addAttribute("activeVisitStartDatetime",
					DateFormatUtils.format(activeVisit.getStartDatetime(), "dd MMM yyyy hh:mm a", Context.getLocale()));
		}

	}

}
