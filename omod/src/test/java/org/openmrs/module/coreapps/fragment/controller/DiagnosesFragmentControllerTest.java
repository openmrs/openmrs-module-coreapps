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

import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.GlobalProperty;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.BasicUiUtils;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.FormatterImpl;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class DiagnosesFragmentControllerTest extends BaseModuleWebContextSensitiveTest { 

    private String DIAGNOSIS_SET_OF_SETS_UUID = "63b378a5-5065-11de-80cb-0018ea23ee37";

    private String CONCEPT_SOURCE_UUID = "75f5b378-5065-11de-80cb-001e378eb67e";
    
    private String LUNG_DISEASE_DIAGNOSIS_SET_UUID = "d1010973-803e-8659-4415-c01707c01dec";

    private String CANCEROUS_DISEASE_DIAGNOSIS_SET_UUID = "d710b7h4-40b7-d333-b449-6e0e15d0739d";

    @Mock
    private UiSessionContext context;

    private DiagnosesFragmentController controller;
    
    private Locale locale;

    private UiUtils uiUtils;    

    @Autowired
    private EmrConceptService emrConceptService;

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private EmrApiProperties emrApiProperties;

    @Autowired @Qualifier("adminService")
    private AdministrationService administrationService;

    @Before
    public void setUp() throws Exception {

        locale = Context.getLocale();
        uiUtils = new BasicUiUtils() {
            @Override
            public String format(Object o) {
                return new FormatterImpl(null, administrationService).format(o, getLocale());
            }
        };
        controller = new DiagnosesFragmentController();

        when(context.getLocale()).thenReturn(locale);

        ConceptSource source = conceptService.getConceptSourceByUuid(CONCEPT_SOURCE_UUID);
        source.setName("ICD-10-WHO");
        
        source = conceptService.saveConceptSource(source);
        ConceptMapType conceptMapType = conceptService.getConceptMapTypeByUuid("35543629-7d8c-11e1-909d-c80aa9edcf4e");

        administrationService.saveGlobalProperty(new GlobalProperty(EmrApiConstants.GP_DIAGNOSIS_SET_OF_SETS,
                DIAGNOSIS_SET_OF_SETS_UUID));
        administrationService.saveGlobalProperty(new GlobalProperty(EmrApiConstants.EMR_CONCEPT_SOURCES_FOR_DIAGNOSIS_SEARCH,
                "ICD-10-WHO"));

        // Set up 'Lung diseases diagnoses' set
        {
            Concept diagnosis1 = new Concept();
            diagnosis1.setFullySpecifiedName(new ConceptName("Asthma", locale));
            diagnosis1.setConceptClass(conceptService.getConceptClassByName("Diagnosis"));
            diagnosis1.setDatatype(conceptService.getConceptDatatypeByName("N/A"));
            diagnosis1.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(source,"J45.9","Test"), conceptMapType));
            diagnosis1 = conceptService.saveConcept(diagnosis1);
            Concept diagnosis2 = new Concept();
            diagnosis2.setFullySpecifiedName(new ConceptName("Tuberculosis infection", locale));
            diagnosis2.setConceptClass(conceptService.getConceptClassByName("Diagnosis"));
            diagnosis2.setDatatype(conceptService.getConceptDatatypeByName("N/A"));
            diagnosis2.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(source,"A15.0","Test"), conceptMapType));
            diagnosis2 = conceptService.saveConcept(diagnosis2);
            
            Concept diagnosisSet = new Concept();
            diagnosisSet.setUuid(LUNG_DISEASE_DIAGNOSIS_SET_UUID);
            diagnosisSet.setFullySpecifiedName(new ConceptName("Lung disease diagnoses", locale));
            diagnosisSet.setConceptClass(conceptService.getConceptClassByName("ConvSet"));
            diagnosisSet.setDatatype(conceptService.getConceptDatatypeByName("N/A"));
            diagnosisSet.addSetMember(diagnosis1);
            diagnosisSet.addSetMember(diagnosis2);
            conceptService.saveConcept(diagnosisSet);
        }

        // Set up 'Cancerous diseases diagnoses' set
        {
            Concept diagnosis3 = new Concept();
            diagnosis3.setFullySpecifiedName(new ConceptName("Liver cancer", locale));
            diagnosis3.setConceptClass(conceptService.getConceptClassByName("Diagnosis"));
            diagnosis3.setDatatype(conceptService.getConceptDatatypeByName("N/A"));
            diagnosis3.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(source,"C22.9","Test"), conceptMapType));
            diagnosis3 = conceptService.saveConcept(diagnosis3);
            Concept diagnosis4 = new Concept();
            diagnosis4.setFullySpecifiedName(new ConceptName("Lung cancer infection", locale));
            diagnosis4.setConceptClass(conceptService.getConceptClassByName("Diagnosis"));
            diagnosis4.setDatatype(conceptService.getConceptDatatypeByName("N/A"));
            diagnosis4.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(source,"J85.2","Test"), conceptMapType));
            diagnosis4 = conceptService.saveConcept(diagnosis4);
            
            Concept diagnosisSet = new Concept();
            diagnosisSet.setUuid(CANCEROUS_DISEASE_DIAGNOSIS_SET_UUID);
            diagnosisSet.setFullySpecifiedName(new ConceptName("Cancerous diseases diagnoses", locale));
            diagnosisSet.setConceptClass(conceptService.getConceptClassByName("ConvSet"));
            diagnosisSet.setDatatype(conceptService.getConceptDatatypeByName("N/A"));
            diagnosisSet.addSetMember(diagnosis3);
            diagnosisSet.addSetMember(diagnosis4);
            conceptService.saveConcept(diagnosisSet);
        }

        // Set up of Diagnosis super set
        {            
            Concept diagnosisSuperSet = new Concept();
            diagnosisSuperSet.setUuid(DIAGNOSIS_SET_OF_SETS_UUID);
            diagnosisSuperSet.setFullySpecifiedName(new ConceptName("Diagnosis Categories", new Locale("en_GB")));
            diagnosisSuperSet.setConceptClass(conceptService.getConceptClassByName("ConvSet"));
            diagnosisSuperSet.setDatatype(conceptService.getConceptDatatypeByName("N/A"));
            diagnosisSuperSet.addSetMember(conceptService.getConceptByUuid(LUNG_DISEASE_DIAGNOSIS_SET_UUID));
            diagnosisSuperSet.addSetMember(conceptService.getConceptByUuid(CANCEROUS_DISEASE_DIAGNOSIS_SET_UUID));
            conceptService.saveConcept(diagnosisSuperSet);
        }

    }

    @Test
    public void search_shouldSearchForDiagnosisConceptsFromSpecifiedSets() throws Exception {
        // setup
        String queryString = "infection";
        String diagnosisSetUuid = LUNG_DISEASE_DIAGNOSIS_SET_UUID;

        // replay
        List<SimpleObject> conceptSearchResults = controller.search(context, uiUtils, emrApiProperties, emrConceptService, conceptService, queryString, diagnosisSetUuid, null, null);
        
        // verify
        assertEquals(1, conceptSearchResults.size());
        assertThat((String) ((SimpleObject) conceptSearchResults.get(0).get("conceptName")).get("name"), is("Tuberculosis infection"));

    }

    @Test
    public void search_shouldSearchForDiagnosisConceptsUsingGloballyDefinedSuperSetGivenEmptyDiagnosisSetUuids() throws Exception {
        // setup
        String queryString = "infection";

        // replay
        List<SimpleObject> conceptSearchResults = controller.search(context, uiUtils, emrApiProperties, emrConceptService, conceptService, queryString, "", null, null);

        // verify
        assertEquals(2, conceptSearchResults.size());
        assertThat((String) ((SimpleObject) conceptSearchResults.get(0).get("conceptName")).get("name"), is("Lung cancer infection"));
        assertThat((String) ((SimpleObject) conceptSearchResults.get(1).get("conceptName")).get("name"), is("Tuberculosis infection"));

    }

    @Test
    public void search_shouldSearchForDiagnosisConceptsUsingGloballyDefinedSuperSetGivenNullDiagnosisSetUuids() throws Exception {
        // setup
        String queryString = "infection";

        // replay
        List<SimpleObject> conceptSearchResults = controller.search(context, uiUtils, emrApiProperties, emrConceptService, conceptService, queryString, null, null, null);

        // verify
        assertEquals(2, conceptSearchResults.size());
        assertThat((String) ((SimpleObject) conceptSearchResults.get(0).get("conceptName")).get("name"), is("Lung cancer infection"));
        assertThat((String) ((SimpleObject) conceptSearchResults.get(1).get("conceptName")).get("name"), is("Tuberculosis infection"));

    }

    @Test
    public void search_shouldSearchForDiagnosisConceptsUsingGloballyDefinedSuperSetGivenUndefinedDiagnosisSetUuids() throws Exception {
        // setup
        String queryString = "infection";

        // replay
        List<SimpleObject> conceptSearchResults = controller.search(context, uiUtils, emrApiProperties, emrConceptService, conceptService, queryString, "undefined", null, null);
     
        // verify
        assertEquals(2, conceptSearchResults.size());
        assertThat((String) ((SimpleObject) conceptSearchResults.get(0).get("conceptName")).get("name"), is("Lung cancer infection"));
        assertThat((String) ((SimpleObject) conceptSearchResults.get(1).get("conceptName")).get("name"), is("Tuberculosis infection"));

    }
}