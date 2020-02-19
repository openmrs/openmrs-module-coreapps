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

package org.openmrs.module.coreapps.fragment.controller.clinicianfacing;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.EncounterService;
import org.openmrs.api.VisitService;
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
import org.openmrs.module.coreapps.page.controller.clinicianfacing.PatientPageController;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.event.ApplicationEventService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

public class PatientPageControllerTest extends BaseModuleWebContextSensitiveTest{ 

    private Patient patient;
    
    private PersonAttributeType pat;
    
    private PersonAttribute pa;
    
    private PageModel model;
    
    private PatientDomainWrapper wrapper;
    
	private PatientPageController controller;

    private UiSessionContext context;

    private UiUtils uiUtils;    
    
    private EmrApiProperties emrApiProperties;
    
    @Qualifier("coreAppsProperties")
    @Autowired
    private CoreAppsProperties coreAppsProperties;
    
    @Qualifier("appFrameworkService")
    @Autowired
    private AppFrameworkService appFrameworkService;
    
    @Qualifier("adtService")
    @Autowired
    private AdtService adtService;
    
    @Qualifier("visitService")
    @Autowired
    private VisitService visitService;
    
    @Qualifier("encounterService")
    @Autowired
    private EncounterService encounterService;
    
    @Qualifier("applicationEventService")
    @Autowired 
    private ApplicationEventService applicationEventService;
    
    @Before
    public void setup() {
    	controller = new PatientPageController();  
    	patient = new Patient();
    	pat = new PersonAttributeType();
    	pa = new PersonAttribute();
    	model = new PageModel();
    	
    	wrapper = mock(PatientDomainWrapper.class);
    	emrApiProperties = mock(EmrApiProperties.class);
    	context = mock(UiSessionContext.class);
    	when(emrApiProperties.getExtraPatientIdentifierTypes()).thenReturn(new ArrayList<PatientIdentifierType>());
    	when(wrapper.getPatient()).thenReturn(patient);
    	when(context.generateAppContextModel()).thenReturn(new AppContextModel());
    }

    @Test
    public void controller_shouldAddBreadcrumbPersonAttributeDetailsAndFormattersToModel() throws Exception {
    	// Setup
    	PersonName name = new PersonName();
    	name.setGivenName("GivenName");
    	name.setFamilyName("FamilyName");
    	name.setPreferred(true);
    	
    	pat.setUuid("21cbb6c3-4ffb-4d1b-b658-332d9a58c77d");
    	pat.setName("NickName");
    	pat.setFormat("java.lang.String");
    	pa.setAttributeType(pat);
    	pa.setValue("SomeName");
    	
    	patient.addName(name);
    	patient.addAttribute(pa);
    	patient.setGender("M");
    	PatientIdentifierType identifierType = new PatientIdentifierType(1);
    	PatientIdentifier pi = new PatientIdentifier("00001", identifierType, new Location(1));
    	patient.addIdentifier(pi);
    	Context.getPersonService().savePersonAttributeType(pat);
    	Context.getPersonService().savePerson(patient);
    	Context.getAdministrationService().setGlobalProperty("breadCrumbs.details.personAttr.uuids", "21cbb6c3-4ffb-4d1b-b658-332d9a58c77d");
    	Context.getAdministrationService().setGlobalProperty("breadCrumbs.formatters", "(;, ;)");

    	// Replay
        controller.controller(patient, model, null, null, wrapper
                             ,adtService, visitService, encounterService
                             ,emrApiProperties, appFrameworkService, applicationEventService
                             ,coreAppsProperties, context);
        
        // Verify
        List<PersonAttribute> personNameAttrs = (List<PersonAttribute>) model.getAttribute("breadCrumbsDetails");
        String [] breadcrumbFormatters = (String []) model.getAttribute("breadCrumbsFormatters");
        
        assertTrue(personNameAttrs.get(0).getAttributeType().getUuid().equals(pat.getUuid()));
        assertTrue(breadcrumbFormatters[0].equals("("));
    	assertTrue(breadcrumbFormatters[1].equals(", "));
    	assertTrue(breadcrumbFormatters[2].equals(")"));
    }
}