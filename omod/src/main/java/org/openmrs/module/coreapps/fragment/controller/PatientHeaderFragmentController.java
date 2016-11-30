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

import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonName;
import org.openmrs.api.APIException;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.CoreAppsProperties;
import org.openmrs.module.coreapps.NameSupportCompatibility;
import org.openmrs.module.coreapps.contextmodel.PatientContextModel;
import org.openmrs.module.coreapps.contextmodel.VisitContextModel;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.module.idgen.AutoGenerationOption;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Ideally you pass in a PatientDomainWrapper as the "patient" config parameter. But if you pass in
 * a Patient, then this controller will wrap that for you.
 */
public class PatientHeaderFragmentController {
	
	public void controller(FragmentConfiguration config, @SpringBean("emrApiProperties") EmrApiProperties emrApiProperties,
                           @SpringBean("coreAppsProperties") CoreAppsProperties coreAppsProperties,
	                       @SpringBean("baseIdentifierSourceService") IdentifierSourceService identifierSourceService,
                           @FragmentParam(required = false, value="appContextModel") AppContextModel appContextModel,
                           @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
	                       @FragmentParam("patient") Object patient, @InjectBeans PatientDomainWrapper wrapper,
	                       @SpringBean("adtService") AdtService adtService, UiSessionContext sessionContext,
                           UiUtils uiUtils,
                           FragmentModel model) {

		if (patient instanceof Patient) {
			wrapper.setPatient((Patient) patient);
		} else {
            wrapper = (PatientDomainWrapper) patient;
        }
        config.addAttribute("patient", wrapper);
        config.addAttribute("patientNames", getNames(wrapper.getPersonName()));

        if (appContextModel == null) {
            AppContextModel contextModel = sessionContext.generateAppContextModel();
            contextModel.put("patient", new PatientContextModel(wrapper.getPatient()));
            model.addAttribute("appContextModel", contextModel);
        }

        List<Extension> firstLineFragments = appFrameworkService.getExtensionsForCurrentUser("patientHeader.firstLineFragments");
        Collections.sort(firstLineFragments);
        model.addAttribute("firstLineFragments", firstLineFragments);
		
        List<Extension> secondLineFragments = appFrameworkService.getExtensionsForCurrentUser("patientHeader.secondLineFragments");
        Collections.sort(secondLineFragments);
        model.addAttribute("secondLineFragments", secondLineFragments);

        List<ExtraPatientIdentifierType> extraPatientIdentifierTypes = new ArrayList<ExtraPatientIdentifierType>();

		for (PatientIdentifierType type : emrApiProperties.getExtraPatientIdentifierTypes()) {
			List<AutoGenerationOption> options = identifierSourceService.getAutoGenerationOptions(type);
            // TODO note that this may allow use to edit a identifier that should not be editable, or vice versa, in the rare case where there are multiple autogeneration
            // TODO options for a single identifier type (which is possible if you have multiple locations) and the manual entry boolean is different between those two generators
			extraPatientIdentifierTypes.add(new ExtraPatientIdentifierType(type,
                    options.size() > 0 ? options.get(0).isManualEntryEnabled() : true));
		}

		config.addAttribute("extraPatientIdentifierTypes", extraPatientIdentifierTypes);
        config.addAttribute("extraPatientIdentifiersMappedByType", wrapper.getExtraIdentifiersMappedByType(sessionContext.getSessionLocation()));
        config.addAttribute("dashboardUrl", coreAppsProperties.getDashboardUrl());
    }

    private Map<String,String> getNames(PersonName personName) {

    	NameSupportCompatibility nameSupport = Context.getRegisteredComponent("coreapps.NameSupportCompatibility", NameSupportCompatibility.class);
    	
        Map<String, String> nameFields = new LinkedHashMap<String, String>();
        List<List<Map<String, String>>> lines = nameSupport.getLines();
        String layoutToken = nameSupport.getLayoutToken();

        // note that the assumption is one one field per "line", otherwise the labels that appear under each field may not render properly
        try {
            for (List<Map<String, String>> line : lines) {
                String nameLabel = "";
                String nameLine = "";
                Boolean hasToken = false;
                for (Map<String, String> lineToken : line) {
                    if (lineToken.get("isToken").equals(layoutToken)) {
                        String tokenValue = BeanUtils.getProperty(personName, lineToken.get("codeName"));
                        nameLabel = nameSupport.getNameMappings().get(lineToken.get("codeName"));
                        if (StringUtils.isNotBlank(tokenValue)) {
                            hasToken = true;
                            nameLine += tokenValue;
                        }
                    }
                    else {
                        nameLine += lineToken.get("displayText");
                    }
                }
                // only display a line if there's a token within it we've been able to resolve
                if (StringUtils.isNotBlank(nameLine) && hasToken) {
                    nameFields.put(nameLabel, nameLine);
                }
            }
            return nameFields;
        }
        catch (Exception e) {
            throw new APIException("Unable to generate name fields for patient header", e);
        }

    }
	
	public class ExtraPatientIdentifierType {
		
		private PatientIdentifierType patientIdentifierType;
		
		private boolean editable = false;
		
		public ExtraPatientIdentifierType(PatientIdentifierType type, boolean editable) {
			this.patientIdentifierType = type;
			this.editable = editable;
		}
		
		public PatientIdentifierType getPatientIdentifierType() {
			return patientIdentifierType;
		}
		
		public void setPatientIdentifierType(PatientIdentifierType patientIdentifierType) {
			this.patientIdentifierType = patientIdentifierType;
		}
		
		public boolean isEditable() {
			return editable;
		}
		
		public void setEditable(boolean editable) {
			this.editable = editable;
		}
	}
	
}
