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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.hamcrest.CoreMatchers;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapperFactory;
import org.openmrs.module.emrapi.visit.VisitDomainWrapperRepository;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;

public class QuickVisitFragmentControllerTest {
	
	private QuickVisitFragmentController controller;
	
	private VisitDomainWrapperFactory visitWrapperFactory;
	
	private VisitDomainWrapperRepository visitWrapperRepository;
	
	private UiUtils uiUtils;
	
	@Before
	public void setUp() {
		controller = new QuickVisitFragmentController();
		visitWrapperFactory = mock(VisitDomainWrapperFactory.class);
		visitWrapperRepository = mock(VisitDomainWrapperRepository.class);
		uiUtils = mock(UiUtils.class);
	}
	
	@Test
	public void shouldCreateNewVisit() throws Exception {
		Patient patient = new Patient();
		Location location = new Location();
		
		Visit visit = new Visit();
		VisitDomainWrapper visitWrapper = new VisitDomainWrapper(visit);
		when(visitWrapperFactory.createNewVisit(eq(patient), eq(location), any(Date.class))).thenReturn(visitWrapper);
		
		String successMessage = "Success message";
		String formattedPatient = "Patient name";
		when(uiUtils.format(patient)).thenReturn(formattedPatient);
		when(uiUtils.message("emr.visit.createQuickVisit.successMessage", formattedPatient)).thenReturn(successMessage);
		
		HttpSession session = mock(HttpSession.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getSession()).thenReturn(session);
		
		FragmentActionResult result = controller.create(visitWrapperFactory, visitWrapperRepository, patient, location,
		    uiUtils, request);
		
		verify(visitWrapperRepository).persist(visitWrapper);
		verify(session).setAttribute(EmrApiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE, successMessage);
		verify(session).setAttribute(EmrApiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");
		assertThat(result, is(CoreMatchers.any(FragmentActionResult.class)));
	}
}
