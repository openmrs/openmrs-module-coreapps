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

import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.api.ConceptService;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptSource;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.ui.framework.UiUtils;

import java.util.List;
import java.util.ArrayList;

@RunWith(MockitoJUnitRunner.class)
public class DiagnosesFragmentControllerTest { 

    private String queryString = "infection";

    private DiagnosesFragmentController controller = new DiagnosesFragmentController();

    @Mock
    private UiSessionContext context;

    @Mock
    private UiUtils uiUtils;    

    @Mock
    private EmrConceptService emrConceptService;

    @Mock
    private ConceptService conceptService;

    @Mock
    private EmrApiProperties emrApiProperties;

    @Test
    public void search_shouldSearchForDiagnosisConceptsFromSpecifiedSets() throws Exception {
        // replay
        controller.search(context, uiUtils, emrApiProperties, emrConceptService, conceptService, queryString, "1st_diagnosis_set_uuid,2nd_diagnosis_set_uuid", null, null, null, null);
        
        // verify
        verify(emrApiProperties, never()).getDiagnosisSets();
    }

    @Test
    public void search_shouldSearchForDiagnosisConceptsUsingGloballyDefinedSuperSetGivenNullDiagnosisSetsParam() throws Exception {
        // replay
        controller.search(context, uiUtils, emrApiProperties, emrConceptService, conceptService, queryString, null, null, null, null, null);

        // verify
        verify(emrApiProperties, times(1)).getDiagnosisSets();

    }

    @Test
    public void search_shouldSearchForDiagnosesWithNullDiagnosisSetsGivenDiagnosisSetsParamWithZeroChar() throws Exception {
        // replay
        controller.search(context, uiUtils, emrApiProperties, emrConceptService, conceptService, queryString, "0", null, null, null, null);
        
        // verify
        verify(emrApiProperties, never()).getDiagnosisSets();
    }

    @Test
    public void search_shouldSearchForDiagnosesFromSpecifiedDiagnosisConceptSources() throws Exception {
        // replay
        controller.search(context, uiUtils, emrApiProperties, emrConceptService, conceptService, queryString, null, "CIEL,ICD-10-WHO", null, null, null);
        
        // verify
        verify(emrApiProperties, never()).getConceptSourcesForDiagnosisSearch();
    }

    @Test
    public void search_shouldSearchForDiagnosesUsingGloballyDefinedConceptSourcesGivenNullDiagnosisConceptSourcesParam() throws Exception {
        // replay
        controller.search(context, uiUtils, emrApiProperties, emrConceptService, conceptService, queryString, null, null, null, null, null);

        // verify
        verify(emrApiProperties, times(1)).getConceptSourcesForDiagnosisSearch();
    }

    @Test
    public void search_shouldSearchForDiagnosesWithNullDiagnosisConceptSourcesGivenDiagnosisConceptSourcesParamWithZeroChar() throws Exception {
        // replay
        controller.search(context, uiUtils, emrApiProperties, emrConceptService, conceptService, queryString, null, "0", null, null, null);
        
        // verify
        verify(emrApiProperties, never()).getConceptSourcesForDiagnosisSearch();
    }
    
    @Test
    public void search_shouldSearchForDiagnosesUsingSpecifiedDiagnosisConceptClasses() throws Exception {
    	// setup
    	ConceptClass conceptClass = new ConceptClass();
    	List<ConceptClass> classList = new ArrayList<ConceptClass> ();
    	classList.add(conceptClass);
    	
    	ConceptSource source = new ConceptSource();
    	List<ConceptSource> sourceList = new ArrayList<ConceptSource> ();
    	sourceList.add(source);
    	when(conceptService.getConceptClassByName(eq("Diagnosis"))).thenReturn(conceptClass);
    	when(conceptService.getConceptSourceByName(eq("ICD-10-WHO"))).thenReturn(source);
    	when(emrApiProperties.getDiagnosisSets()).thenReturn(null);
    	
    	// replay
        controller.search(context, uiUtils, emrApiProperties, emrConceptService, conceptService, queryString, null, "ICD-10-WHO", "Diagnosis", null, null);
        
        // verify
        verify(emrConceptService, times(1)).conceptSearch(queryString, null, classList, null, sourceList, null);
    }
}