/**
 This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.coreapps.page.controller.conditionlist;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;

/**
 * Controller for a fragment that displays conditions for a patient
 * 
 * @author owais.hussain@ihsinformatics.com
 */
public class ManageConditionsPageController {
	
	public void controller(PageModel model, @RequestParam("patientId") Patient patient,
	        @RequestParam(value = "returnUrl", required = false) String returnUrl, UiUtils ui) {
		if (StringUtils.isBlank(returnUrl)) {
			returnUrl = ui.pageLink("coreapps", "clinicianfacing/patient",
			    Collections.singletonMap("patientId", (Object) patient.getId()));
		}
		
		model.addAttribute("patient", patient);
		model.addAttribute("returnUrl", returnUrl);
		model.addAttribute("hasModifyConditionsPrivilege",
		    Context.getAuthenticatedUser().hasPrivilege(CoreAppsConstants.MANAGE_CONDITIONS_PRIVILEGE));
	}
}
