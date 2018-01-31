/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.coreapps.fragment.controller.conditionlist;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.fragment.FragmentModel;

/**
 *  * Controller for a fragment that displays conditions for a patient
 */
public class ConditionsFragmentController {
	
	public void controller(FragmentModel model, @FragmentParam("patientId") Patient patient) {
		model.addAttribute("hasModifyConditionsPrivilege",
		    Context.getAuthenticatedUser().hasPrivilege(CoreAppsConstants.MANAGE_CONDITIONS_PRIVILEGE));
	}
}
