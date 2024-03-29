package org.openmrs.module.coreapps.page.controller.conditionlist;

/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 * <p>
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 * <p>
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

import org.apache.commons.lang.StringUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.api.context.Context;

public class ManageConditionPageController {
	
	public void controller(PageModel model, @RequestParam(value = "conditionUuid", required = false) String conditionUuid, @RequestParam(value = "returnUrl", required = false) String returnUrl) {
		model.addAttribute("returnUrl", returnUrl);
		model.addAttribute("editingCondition", StringUtils.isNotBlank(conditionUuid));
		String conditionListClasses = Context.getAdministrationService().getGlobalProperty(CoreAppsConstants.GLOBAL_PROPERTY_CONDITIONS_CRITERIA);
		model.addAttribute("conditionListClasses", conditionListClasses);
	}
}
