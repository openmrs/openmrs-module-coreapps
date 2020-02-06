/*
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

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.Map;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.PersonName;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.CoreAppsProperties;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.idgen.service.IdentifierSourceService;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class PatientHeaderFragmentControllerTest extends BaseModuleWebContextSensitiveTest{ 

    private Patient patient;
    
    private PersonAttributeType pat;
    
    private PersonAttribute pa;
    
    private FragmentModel model;
    
    private FragmentConfiguration config;
    
    private PatientDomainWrapper wrapper;
    
    private AppContextModel appContextModel;
    
	private PatientHeaderFragmentController controller;

    private UiSessionContext context;

    private UiUtils uiUtils;    
    
    private EmrApiProperties emrApiProperties;
    
    @Qualifier("coreAppsProperties")
    @Autowired
    private CoreAppsProperties coreAppsProperties;
    
    @Qualifier("baseIdentifierSourceService")
    @Autowired
    private IdentifierSourceService identifierSourceService;
    
    @Qualifier("appFrameworkService")
    @Autowired
    private AppFrameworkService appFrameworkService;
    
    @Qualifier("adtService")
    @Autowired
    private AdtService adtService;
    
    @Before
    public void setup() {
    	controller = new PatientHeaderFragmentController();  
    	appContextModel = new AppContextModel();
    	model =  new FragmentModel();
    	config = new FragmentConfiguration();
    	patient = new Patient();
    	pat = new PersonAttributeType();
    	pa = new PersonAttribute();
    	
    	uiUtils = mock(UiUtils.class);
        wrapper = mock(PatientDomainWrapper.class);
    	emrApiProperties = mock(EmrApiProperties.class);
    	context = mock(UiSessionContext.class);
    	when(emrApiProperties.getExtraPatientIdentifierTypes()).thenReturn(new ArrayList<PatientIdentifierType>());
    	when(wrapper.getPatient()).thenReturn(patient);        
    	
    	Context.getAdministrationService().setGlobalProperty("extraPersonNames.personAttributeTypes", "21cbb6c3-4ffb-4d1b-b658-332d9a58c77d");
    	
    }

    @Test
    public void controller_shouldIncludePreferredPersonNameOnly() throws Exception {
    	// Setup
    	
    	PersonName name = new PersonName();
    	name.setGivenName("GivenName");
    	name.setFamilyName("FamilyName");
    	name.setPreferred(true);
    	
    	patient.addName(name);
    	patient.addAttribute(pa);
    	patient.setGender("M");
    	PatientIdentifierType identifierType = new PatientIdentifierType(1);
    	PatientIdentifier pi = new PatientIdentifier("00001", identifierType, new Location(1));
    	patient.addIdentifier(pi);
    	Context.getPersonService().savePerson(patient);

    	// Replay
        controller.controller(config, emrApiProperties, coreAppsProperties, identifierSourceService
        		             ,appContextModel, appFrameworkService, patient, wrapper, adtService
        		             ,context, uiUtils, model);
        
        // Verify
        Map<String, String> patientNames = (Map<String, String>) config.get("patientNames");
        assertTrue(patientNames.containsKey("PersonName.givenName") && patientNames.get("PersonName.givenName").equals(patient.getGivenName()));
        assertTrue(patientNames.containsKey("PersonName.familyName") && patientNames.get("PersonName.familyName").equals(patient.getFamilyName()));
    }
    
    @Test
    public void controller_shouldIncludePersonNamesFromDefinedAttributeTypes() throws Exception {
    	// Setup
    	pat.setUuid("21cbb6c3-4ffb-4d1b-b658-332d9a58c77d");
    	pat.setName("NickName");
    	pat.setFormat("java.lang.String");
    	pa.setAttributeType(pat);
    	pa.setValue("SomeName");
    	PersonName name = new PersonName();
    	name.setGivenName("GivenName");
    	name.setFamilyName("FamilyName");
    	name.setPreferred(true);
    	
    	patient.addName(name);
    	patient.addAttribute(pa);
    	patient.setGender("M");
    	PatientIdentifierType identifierType = new PatientIdentifierType(1);
    	PatientIdentifier pi = new PatientIdentifier("00001", identifierType, new Location(1));
    	patient.addIdentifier(pi);
    	Context.getPersonService().savePersonAttributeType(pat);
    	Context.getPersonService().savePerson(patient);
        when(uiUtils.message(pat.getName())).thenReturn(pat.getName());

    	// Replay
        controller.controller(config, emrApiProperties, coreAppsProperties, identifierSourceService
        		             ,appContextModel, appFrameworkService, patient, wrapper, adtService
        		             ,context, uiUtils, model);
        
        // Verify
        Map<String, String> patientNames = (Map<String, String>) config.get("patientNames");
        assertTrue(patientNames.containsKey("PersonName.givenName") && patientNames.get("PersonName.givenName").equals(patient.getGivenName()));
        assertTrue(patientNames.containsKey("PersonName.familyName") && patientNames.get("PersonName.familyName").equals(patient.getFamilyName()));
        assertTrue(patientNames.containsKey("NickName") && patientNames.get("NickName").equals(patient.getAttribute(pat).getValue()));
    }
}