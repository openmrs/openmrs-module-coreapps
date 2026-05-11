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
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.test.ContextSensitiveMetadataTestUtils;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.Date;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.hamcrest.core.IsNull.nullValue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AppContextModelGeneratorComponentTest extends BaseModuleWebContextSensitiveTest {

    @Autowired
    private AppContextModelGenerator generator;

    @Autowired
    private LocationService locationService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private VisitService visitService;

    private Location visitSupportingLocation;
    private Patient patient;

    @Before
    public void setUp() throws Exception {
        ContextSensitiveMetadataTestUtils.setupSupportsVisitLocationTag(locationService);

        // location_id=2 ("Xanadu") from the standard test dataset; we add the Supports Visits tag to it
        visitSupportingLocation = locationService.getLocation(2);
        visitSupportingLocation.addTag(locationService.getLocationTagByName("Visit Location"));
        locationService.saveLocation(visitSupportingLocation);

        // patient 6 has no encounters or programs — keeps tests focused on visit/patient behavior
        patient = patientService.getPatient(6);
    }

    @Test
    public void generateAppContextModel_shouldPopulateActiveVisitWhenOneExists() {
        Visit activeVisit = createActiveVisit(patient, visitSupportingLocation);

        UiSessionContext sessionContext = mockSessionContext(visitSupportingLocation);
        AppContextModel contextModel = generator.generateAppContextModel(sessionContext, patient);

        VisitContextModel visitModel = (VisitContextModel) contextModel.get("visit");
        assertThat(visitModel, notNullValue());
        assertThat(visitModel.getUuid(), is(activeVisit.getUuid()));
        assertThat(visitModel.isActive(), is(true));
    }

    @Test
    public void generateAppContextModel_shouldSetVisitNullWhenNoActiveVisitExists() {
        // patient 7 has only a closed visit in baseTestDataset
        UiSessionContext sessionContext = mockSessionContext(visitSupportingLocation);
        AppContextModel contextModel = generator.generateAppContextModel(sessionContext, patient);

        assertThat(contextModel.get("visit"), nullValue());
    }

    @Test
    public void generateAppContextModel_shouldSetVisitNullWhenSessionLocationDoesNotSupportVisits() {
        Location nonVisitLocation = locationService.getLocation(1); // "Unknown Location" — no Supports Visits tag

        UiSessionContext sessionContext = mockSessionContext(nonVisitLocation);
        AppContextModel contextModel = generator.generateAppContextModel(sessionContext, patient);

        assertThat(contextModel.get("visit"), nullValue());
    }

    @Test
    public void generateAppContextModel_shouldUseExplicitVisitInsteadOfLookingUpActiveVisit() {
        // Create both an active visit and a closed visit; pass the closed one explicitly
        createActiveVisit(patient, visitSupportingLocation);
        Visit closedVisit = createClosedVisit(patient, visitSupportingLocation);

        UiSessionContext sessionContext = mockSessionContext(visitSupportingLocation);
        AppContextModel contextModel = generator.generateAppContextModel(sessionContext, patient, closedVisit);

        VisitContextModel visitModel = (VisitContextModel) contextModel.get("visit");
        assertThat(visitModel, notNullValue());
        assertThat(visitModel.getUuid(), is(closedVisit.getUuid()));
        assertThat(visitModel.isActive(), is(false));
    }

    @Test
    public void generateAppContextModel_shouldSetPatientAndPatientIdOnContextModel() {
        UiSessionContext sessionContext = mockSessionContext(visitSupportingLocation);
        AppContextModel contextModel = generator.generateAppContextModel(sessionContext, patient);

        assertThat(contextModel.get("patient"), notNullValue());
        assertThat(contextModel.get("patientId"), is(patient.getUuid()));
    }

    @Test
    public void generateAppContextModel_shouldPopulateEncountersAndProgramsOnContextModel() {
        UiSessionContext sessionContext = mockSessionContext(visitSupportingLocation);
        AppContextModel contextModel = generator.generateAppContextModel(sessionContext, patient);

        assertThat(contextModel.get("encounters"), notNullValue());
        assertThat(contextModel.get("programs"), notNullValue());
        assertThat(contextModel.get("activePrograms"), notNullValue());
    }

    private Visit createActiveVisit(Patient patient, Location location) {
        VisitType visitType = visitService.getAllVisitTypes().get(0);
        Visit visit = new Visit(patient, visitType, new Date());
        visit.setLocation(location);
        return visitService.saveVisit(visit);
    }

    private Visit createClosedVisit(Patient patient, Location location) {
        VisitType visitType = visitService.getAllVisitTypes().get(0);
        Visit visit = new Visit(patient, visitType, new Date());
        visit.setLocation(location);
        visit.setStopDatetime(new Date());
        return visitService.saveVisit(visit);
    }

    private UiSessionContext mockSessionContext(Location sessionLocation) {
        UiSessionContext sessionContext = mock(UiSessionContext.class);
        when(sessionContext.generateAppContextModel()).thenReturn(new AppContextModel());
        when(sessionContext.getSessionLocation()).thenReturn(sessionLocation);
        return sessionContext;
    }
}