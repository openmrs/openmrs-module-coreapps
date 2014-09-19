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
package org.openmrs.module.coreapps.page.controller.configuremetadata;

import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.stereotype.Controller;

import java.io.IOException;

@Controller
public class ConfigureMetadataPageController {
	
    public static final String CONFIGURE_MEATADATA_EXTENSION_POINT_ID = "org.openmrs.referenceapplication.configuremetadataLink";
	
	/**
	 * Process requests to show the home page
	 * 
	 * @param model
	 * @param appFrameworkService
	 * @throws IOException
	 */
	public void get(PageModel model, @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService) {
		model.addAttribute("extensions",
		    appFrameworkService.getExtensionsForCurrentUser(CONFIGURE_MEATADATA_EXTENSION_POINT_ID));
    }
	
}
