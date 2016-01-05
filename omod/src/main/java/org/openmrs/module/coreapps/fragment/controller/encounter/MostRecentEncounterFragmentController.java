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
package org.openmrs.module.coreapps.fragment.controller.encounter;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.coreapps.EncounterServiceCompatibility;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.util.ArrayList;
import java.util.List;

public class MostRecentEncounterFragmentController {
	
	public void controller(FragmentConfiguration config, FragmentModel model, UiUtils ui,
						   @FragmentParam("patientId") Patient patient,
						   @FragmentParam("app") AppDescriptor app,
	                       @SpringBean("encounterService") EncounterService encounterService) {

		if (app.getConfig().path("encounterTypeUuid").isMissingNode()) {
			throw new IllegalStateException("encounterTypeUuid app config parameter required");
		}

		if (app.getConfig().path("encounterDateLabel").isMissingNode()) {
			throw new IllegalStateException("encounterDateLabel app config parameter required");
		}

		List<EncounterType> encounterTypes = new ArrayList<EncounterType>();
		EncounterType encounterType = encounterService.getEncounterTypeByUuid((app.getConfig().get("encounterTypeUuid").getTextValue()));

		if (encounterType == null) {
			throw new IllegalStateException("No encounter type with uuid " + app.getConfig().get("encounterTypeUuid").getTextValue());
		}

		encounterTypes.add(encounterType);

		EncounterServiceCompatibility service = Context.getRegisteredComponent("coreapps.EncounterServiceCompatibility", EncounterServiceCompatibility.class);
		List<Encounter> encounters = service.getEncounters(Context.getEncounterService(), patient, null, null, null, null, encounterTypes, null,
		    null, null, false);

		model.addAttribute("app", app);
        model.addAttribute("patient", patient);

        String definitionUiResource = "";
        if (!app.getConfig().path("definitionUiResource").isMissingNode()) {
            definitionUiResource = app.getConfig().get("definitionUiResource").getTextValue();
        }

        model.addAttribute("definitionUiResource", definitionUiResource);

		if (encounters.size() > 0) {
            model.addAttribute("encounter", encounters.get(encounters.size() - 1));
		} else {
			model.addAttribute("encounter", null);
		}

        model.addAttribute("creatable", app.getConfig().get("creatable") != null ? app.getConfig().get("creatable").getBooleanValue() : false);
        model.addAttribute("createIcon", app.getConfig().get("create-icon") != null ? app.getConfig().get("create-icon").getTextValue() : null);
        model.addAttribute("createProvider", app.getConfig().get("create-provider") != null ? app.getConfig().get("create-provider").getTextValue() : null);
        model.addAttribute("createFragment", app.getConfig().get("create-fragment") != null ? app.getConfig().get("create-fragment").getTextValue() : null);

        model.addAttribute("editable", app.getConfig().get("editable") != null ? app.getConfig().get("editable").getBooleanValue() : false);
        model.addAttribute("editIcon", app.getConfig().get("edit-icon") != null ? app.getConfig().get("edit-icon").getTextValue() : null);
        model.addAttribute("editProvider", app.getConfig().get("edit-provider") != null ? app.getConfig().get("edit-provider").getTextValue() : null);
        model.addAttribute("editFragment", app.getConfig().get("edit-fragment") != null ? app.getConfig().get("edit-fragment").getTextValue() : null);

		String returnUrl = config.getAttribute("returnUrl") != null ? config.getAttribute("returnUrl").toString() : null;
		if (StringUtils.isBlank(returnUrl)) {
			returnUrl = getNodeValue(app.getConfig(), "returnUrl", null);
		}
		if (StringUtils.isBlank(returnUrl)) {
			String returnProvider = getNodeValue(app.getConfig(), "returnProvider", null);
			String returnPage = getNodeValue(app.getConfig(), "returnPage", null);
			if (StringUtils.isNotBlank(returnProvider) && StringUtils.isNotBlank(returnPage)) {
				returnUrl = ui.pageLink(returnProvider, returnPage, SimpleObject.create("patientId", patient.getId()));
			}
		}
		model.addAttribute("returnUrl", returnUrl);
	}

	private String getNodeValue(ObjectNode node, String attribute, String defaultValue) {
		return node.get(attribute) != null ? node.get(attribute).getTextValue() : defaultValue;
	}
}
