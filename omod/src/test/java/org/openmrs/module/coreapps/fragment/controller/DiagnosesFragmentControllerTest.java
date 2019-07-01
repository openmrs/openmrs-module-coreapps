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

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.junit.Before;
import org.junit.Test;

import org.openmrs.api.AdministrationService;
import org.openmrs.api.ConceptService;
import org.openmrs.Concept;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSource;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.ui.framework.UiUtils;

import static org.mockito.Matchers.anyCollection;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class DiagnosesFragmentControllerTest { 

    private String queryString = "infection";

    private DiagnosesFragmentController controller;

    private UiSessionContext context;

    private UiUtils uiUtils;    

    private EmrConceptService emrConceptService;

    private ConceptService conceptService;

    private EmrApiProperties emrApiProperties;

    private AdministrationService administrationService;

    @Before
    public void setUp() throws Exception {
        controller = new DiagnosesFragmentController();
        context = mock(UiSessionContext.class);
        uiUtils = mock(UiUtils.class);
        emrConceptService = mock(EmrConceptService.class);
        conceptService = mock(ConceptService.class);
        emrApiProperties = mock(EmrApiProperties.class);
        administrationService = mock(AdministrationService.class);
    }

    @Test
    public void search_shouldSearchForDiagnosisConceptsFromSpecifiedSets() throws Exception {
        // replay
        controller.search(context, uiUtils, emrApiProperties, emrConceptService, conceptService, queryString, "1st_diagnosis_set_uuid,2nd_diagnosis_set_uuid", null, null);
        
        // verify
        verify(emrApiProperties, never()).getDiagnosisSets();
    }

    @Test
    public void search_shouldSearchForDiagnosisConceptsUsingGloballyDefinedSuperSetGivenEmptyDiagnosisSets() throws Exception {
        // replay
        controller.search(context, uiUtils, emrApiProperties, emrConceptService, conceptService, queryString, "", null, null);

        // verify
        verify(emrApiProperties, times(1)).getDiagnosisSets();
    }

    @Test
    public void search_shouldSearchForDiagnosisConceptsUsingGloballyDefinedSuperSetGivenNullDiagnosisSets() throws Exception {
        // replay
        controller.search(context, uiUtils, emrApiProperties, emrConceptService, conceptService, queryString, null, null, null);

        // verify
        verify(emrApiProperties, times(1)).getDiagnosisSets();

    }

}