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
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.appui.AppUiConstants;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;

import java.util.Date;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class QuickVisitFragmentControllerTest {
	
	private QuickVisitFragmentController controller;

	private UiUtils uiUtils;
	
	private UiSessionContext emrContext;
	
	private AdtService adtService;
	
	@Before
	public void setUp() {
		controller = new QuickVisitFragmentController();
		uiUtils = mock(UiUtils.class);
		emrContext = mock(UiSessionContext.class);
		adtService = mock(AdtService.class);
	}
	
	@Test
	public void shouldCreateNewVisit() throws Exception {
		Patient patient = new Patient();
        Location visitLocation = new Location();

		String successMessage = "Success message";
		String formattedPatient = "Patient name";
		when(uiUtils.format(patient)).thenReturn(formattedPatient);
		when(uiUtils.message("coreapps.visit.createQuickVisit.successMessage", formattedPatient)).thenReturn(successMessage);
		
		HttpSession session = mock(HttpSession.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getSession()).thenReturn(session);

        Visit visit = createVisit();

        when(adtService.ensureVisit(eq(patient), any(Date.class), eq(visitLocation))).thenReturn(visit);

		FragmentActionResult result = controller.create(adtService, patient, visitLocation,
		    uiUtils, emrContext, request);

		verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE, successMessage);
		verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");
		assertThat(result, is(CoreMatchers.any(FragmentActionResult.class)));

	}

    @Test
    public void shouldFailIfActiveVisitAlreadyExists() throws Exception {

        Patient patient = new Patient();
        Location visitLocation = new Location();

        when(adtService.getActiveVisit(patient, visitLocation)).thenReturn(new VisitDomainWrapper(null, null));

        HttpSession session = mock(HttpSession.class);
        HttpServletRequest request = mock(HttpServletRequest.class);
        when(request.getSession()).thenReturn(session);

        FragmentActionResult result = controller.create(adtService, patient, visitLocation,
                uiUtils, emrContext, request);

        assertThat(result, is(instanceOf(FailureResult.class)));
        verify(adtService, never()).ensureVisit(eq(patient), any(Date.class), eq(visitLocation));
    }


    private Visit createVisit() {
        Visit visit = new Visit();
        visit.setId(1);
        return visit;
    }

}
