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
package org.openmrs.module.coreapps.fragment.controller.visit;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.VisitService;
import org.openmrs.module.appui.AppUiConstants;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class QuickVisitFragmentControllerTest {
	
	private QuickVisitFragmentController controller;
	
	private VisitService visitService;
	
	private UiUtils uiUtils;
	
	private UiSessionContext emrContext;
	
	private AdtService adtService;

    private EmrApiProperties emrApiProperties;
	
	@Before
	public void setUp() {
		controller = new QuickVisitFragmentController();
        visitService = mock(VisitService.class);
		uiUtils = mock(UiUtils.class);
		emrContext = mock(UiSessionContext.class);
		adtService = mock(AdtService.class);
        emrApiProperties = mock(EmrApiProperties.class);
	}
	
	@Test
	public void shouldCreateNewVisit() throws Exception {
		Patient patient = new Patient();

        Location visitLocation = new Location();
        when(adtService.getLocationThatSupportsVisits(visitLocation)).thenReturn(visitLocation);

        VisitType visitType = new VisitType();
        when(emrApiProperties.getAtFacilityVisitType()).thenReturn(visitType);

		String successMessage = "Success message";
		String formattedPatient = "Patient name";
		when(uiUtils.format(patient)).thenReturn(formattedPatient);
		when(uiUtils.message("coreapps.visit.createQuickVisit.successMessage", formattedPatient)).thenReturn(successMessage);
		
		HttpSession session = mock(HttpSession.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getSession()).thenReturn(session);
		
		FragmentActionResult result = controller.create(visitService, adtService, emrApiProperties, patient, visitLocation,
		    uiUtils, emrContext, request);
		
		verify(visitService).saveVisit(argThat(new IsExpectedVisit(patient, visitLocation, visitType)));

		verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE, successMessage);
		verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");
		assertThat(result, is(CoreMatchers.any(FragmentActionResult.class)));
	}

    @Test
    public void shouldFailIfActiveVisitAlreadyExists() throws Exception {

        Patient patient = new Patient();

        Location visitLocation = new Location();
        when(adtService.getLocationThatSupportsVisits(visitLocation)).thenReturn(visitLocation);

        VisitType visitType = new VisitType();
        when(emrApiProperties.getAtFacilityVisitType()).thenReturn(visitType);

        when(adtService.getActiveVisit(patient, visitLocation)).thenReturn(new VisitDomainWrapper(null, null));

        HttpSession session = mock(HttpSession.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(session);

        FragmentActionResult result = controller.create(visitService, adtService, emrApiProperties, patient, visitLocation,
                uiUtils, emrContext, request);

        assertThat(result, is(instanceOf(FailureResult.class)));
    }


    private class IsExpectedVisit extends ArgumentMatcher<Visit> {

        private Patient expectedPatient;

        private Location expectedLocation;

        private VisitType expectedVisitType;

        public IsExpectedVisit(Patient patient, Location location, VisitType visitType) {
            this.expectedPatient = patient;
            this.expectedLocation = location;
            this.expectedVisitType = visitType;
        }

        @Override
        public boolean matches(Object o) {

            Visit actualVisit = (Visit) o;

            assertThat(actualVisit.getPatient(), is(expectedPatient));
            assertThat(actualVisit.getLocation(), is (expectedLocation));
            assertThat(actualVisit.getVisitType(), is(expectedVisitType));

            return true;
        }
    }
}
