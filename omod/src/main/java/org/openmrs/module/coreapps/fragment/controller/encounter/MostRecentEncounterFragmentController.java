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
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Patient;
import org.openmrs.api.EncounterService;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.parameter.EncounterSearchCriteria;
import org.openmrs.parameter.EncounterSearchCriteriaBuilder;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class MostRecentEncounterFragmentController {

	public void controller(FragmentConfiguration config, FragmentModel model, UiUtils ui,
						   @FragmentParam("patientId") Patient patient,
						   @FragmentParam("app") AppDescriptor app,
	                       @SpringBean("encounterService") EncounterService encounterService) {

		// The widget requires at least one of the config parameters, encounterTypeUuid or encounterTypes to have a value
		if (app.getConfig().path("encounterTypeUuid").isMissingNode() && app.getConfig().path("encounterTypes").isMissingNode()) {
			throw new IllegalStateException("encounterTypeUuid or encounterTypes app config parameter are required");
		}

		if (app.getConfig().path("encounterDateLabel").isMissingNode()) {
			throw new IllegalStateException("encounterDateLabel app config parameter required");
		}

		List<EncounterType> encounterTypes = new ArrayList<EncounterType>();
		Map<String, String> encounterTypeToForms = new HashMap<String, String>();
		String uuid = null;
		if (app.getConfig().get("encounterTypeUuid") != null) {
			uuid = app.getConfig().get("encounterTypeUuid").getTextValue();
		}
		if (StringUtils.isNotBlank(uuid)) {
			// first try to find the EncounterType specified via the encounterTypeUuid config parameter
			EncounterType encounterType = encounterService.getEncounterTypeByUuid(uuid);
			if (encounterType == null) {
				throw new IllegalStateException("No encounter type with uuid " + uuid);
			}
			encounterTypes.add(encounterType);
		} else {
			// if the encounterTypeUuid config parameter was not specified or empty, then parse the encounterTypes config parameter
			JsonNode node = app.getConfig().get("encounterTypes");
			if ( node == null){
				throw new IllegalStateException("Missing configuration encounterTypes on widget");
			}
			if (node instanceof ArrayNode) {
				ArrayNode arrayNode = (ArrayNode) node;
				for (Iterator<JsonNode> iter = arrayNode.getElements(); iter.hasNext();) {
					JsonNode encTypeNode = iter.next();
					if (encTypeNode == null ) {
						throw new IllegalStateException("Missing configuration encounterTypes on widget");
					}
					for (Iterator<Map.Entry<String, JsonNode>> field = encTypeNode.getFields(); field.hasNext();){
						Map.Entry<String, JsonNode> encTypeField = field.next();
						String encTypeUuid = encTypeField.getKey();
						String formName = encTypeField.getValue().getTextValue();
						if (StringUtils.isNotBlank(encTypeUuid) && StringUtils.isNotBlank(formName)) {
							EncounterType encounterType = encounterService.getEncounterTypeByUuid(encTypeUuid);
							if (encounterType == null) {
								throw new IllegalStateException("No encounter type with uuid " + encTypeUuid);
							}
							encounterTypes.add(encounterType);
							encounterTypeToForms.put(encTypeUuid, formName);
						}
					}
				}
			}
		}
		if ( encounterTypes.isEmpty()) {
			throw new IllegalStateException("No valid EncounterType was found");
		}

		EncounterSearchCriteria encounterSearchCriteria = new EncounterSearchCriteriaBuilder()
				.setPatient(patient)
				.setEncounterTypes(encounterTypes)
				.setIncludeVoided(false)
				.createEncounterSearchCriteria();
		List<Encounter> encounters = encounterService.getEncounters(encounterSearchCriteria);
		Encounter mostRecentEncounter = null;
		if (!encounters.isEmpty()) {
			mostRecentEncounter = encounters.get(encounters.size() - 1);
			model.addAttribute("encounter", mostRecentEncounter);
		} else {
			model.addAttribute("encounter", null);
		}

		model.addAttribute("app", app);
        model.addAttribute("patient", patient);

        String definitionUiResource = "";
        if (!app.getConfig().path("definitionUiResource").isMissingNode()) {
            definitionUiResource = app.getConfig().get("definitionUiResource").getTextValue();
        } else if (mostRecentEncounter != null && !encounterTypeToForms.isEmpty()){
			definitionUiResource = encounterTypeToForms.get(mostRecentEncounter.getEncounterType().getUuid());
		}
        model.addAttribute("definitionUiResource", definitionUiResource);

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
		
		if (StringUtils.isNotBlank(returnUrl)) {
			returnUrl = ui.urlBind(returnUrl, patient);
		}
		
		model.addAttribute("returnUrl", returnUrl);
	}

	private String getNodeValue(ObjectNode node, String attribute, String defaultValue) {
		return node.get(attribute) != null ? node.get(attribute).getTextValue() : defaultValue;
	}
}
