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
package org.openmrs.module.coreapps.contextmodel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;

import java.util.Collections;
import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class AppContextModelGeneratorTest {

    @InjectMocks
    private AppContextModelGenerator generator;

    @Mock
    private AdtService adtService;

    @Mock
    private EncounterService encounterService;

    @Mock
    private ProgramWorkflowService programWorkflowService;

    private UiSessionContext sessionContext;
    private Patient patient;
    private Location sessionLocation;
    private Location visitSupportingLocation;

    @Before
    public void setUp() {
        sessionContext = mock(UiSessionContext.class);
        patient = new Patient();
        patient.setUuid("patient-uuid");

        sessionLocation = new Location();
        visitSupportingLocation = new Location();

        when(sessionContext.generateAppContextModel()).thenReturn(new AppContextModel());
        when(sessionContext.getSessionLocation()).thenReturn(sessionLocation);
        when(encounterService.getEncountersByPatient(patient)).thenReturn(Collections.emptyList());
        when(programWorkflowService.getPatientPrograms(
                any(Patient.class), any(), any(), any(), any(), any(), anyBoolean()
        )).thenReturn(Collections.emptyList());
    }

    @Test
    public void generateAppContextModel_shouldSetActiveVisitWhenOneExists() {
        VisitDomainWrapper activeVisit = mockVisitDomainWrapper("visit-uuid");
        when(adtService.getLocationThatSupportsVisits(sessionLocation)).thenReturn(visitSupportingLocation);
        when(adtService.getActiveVisit(patient, visitSupportingLocation)).thenReturn(activeVisit);

        AppContextModel contextModel = generator.generateAppContextModel(sessionContext, patient);

        VisitContextModel visit = (VisitContextModel) contextModel.get("visit");
        assertThat(visit, notNullValue());
        assertThat(visit.getUuid(), is("visit-uuid"));
        assertThat(visit.isActive(), is(true));
    }

    @Test
    public void generateAppContextModel_shouldSetVisitToNullWhenNoActiveVisitExists() {
        when(adtService.getLocationThatSupportsVisits(sessionLocation)).thenReturn(visitSupportingLocation);
        when(adtService.getActiveVisit(patient, visitSupportingLocation)).thenReturn(null);

        AppContextModel contextModel = generator.generateAppContextModel(sessionContext, patient);

        assertThat(contextModel.get("visit"), nullValue());
    }

    @Test
    public void generateAppContextModel_shouldSetVisitToNullWhenLocationDoesNotSupportVisits() {
        when(adtService.getLocationThatSupportsVisits(sessionLocation)).thenThrow(new IllegalArgumentException("location does not support visits"));

        AppContextModel contextModel = generator.generateAppContextModel(sessionContext, patient);

        assertThat(contextModel.get("visit"), nullValue());
        verify(adtService, never()).getActiveVisit(any(Patient.class), any(Location.class));
    }

    @Test
    public void generateAppContextModel_shouldUseProvidedVisitWithoutLookingUpActiveVisit() {
        Visit visit = new Visit();
        visit.setStartDatetime(new Date());
        visit.setPatient(patient);

        VisitDomainWrapper wrappedVisit = mockVisitDomainWrapper("explicit-visit-uuid");
        when(adtService.wrap(visit)).thenReturn(wrappedVisit);

        AppContextModel contextModel = generator.generateAppContextModel(sessionContext, patient, visit);

        VisitContextModel visitModel = (VisitContextModel) contextModel.get("visit");
        assertThat(visitModel, notNullValue());
        assertThat(visitModel.getUuid(), is("explicit-visit-uuid"));
        verify(adtService, never()).getLocationThatSupportsVisits(any(Location.class));
        verify(adtService, never()).getActiveVisit(any(Patient.class), any(Location.class));
    }

    @Test(expected = IllegalArgumentException.class)
    public void generateAppContextModel_shouldThrowIfPatientAndVisitPatientMismatch() {
        Patient otherPatient = new Patient();
        otherPatient.setUuid("other-patient-uuid");

        Visit visit = new Visit();
        visit.setPatient(otherPatient);

        generator.generateAppContextModel(sessionContext, patient, visit);
    }

    @Test
    public void generateAppContextModel_shouldSetPatientAndPatientIdOnContextModel() {
        when(adtService.getLocationThatSupportsVisits(sessionLocation)).thenReturn(visitSupportingLocation);
        when(adtService.getActiveVisit(patient, visitSupportingLocation)).thenReturn(null);

        AppContextModel contextModel = generator.generateAppContextModel(sessionContext, patient);

        assertThat(contextModel.get("patient"), notNullValue());
        assertThat(contextModel.get("patientId"), is("patient-uuid"));
    }

    private VisitDomainWrapper mockVisitDomainWrapper(String uuid) {
        VisitDomainWrapper wrapper = mock(VisitDomainWrapper.class);
        Visit visit = new Visit();
        visit.setUuid(uuid);
        visit.setStartDatetime(new Date());
        when(wrapper.getVisit()).thenReturn(visit);
        when(wrapper.getVisitId()).thenReturn(1);
        when(wrapper.isOpen()).thenReturn(true);
        when(wrapper.isAdmitted()).thenReturn(false);
        when(wrapper.getStartDatetime()).thenReturn(new Date());
        when(wrapper.getStopDatetime()).thenReturn(null);
        when(wrapper.getSortedEncounters()).thenReturn(Collections.emptyList());
        return wrapper;
    }
}