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
import org.openmrs.*;
import org.openmrs.api.VisitService;
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

	private VisitService visitService;

    private String successMessage;

    @Before
	public void setUp() {
		controller = new QuickVisitFragmentController();
		uiUtils = mock(UiUtils.class);
		emrContext = mock(UiSessionContext.class);
		adtService = mock(AdtService.class);
		visitService = mock(VisitService.class);
        successMessage = uiUtils.encodeHtml("Success message");
	}
	
	@Test
	public void shouldCreateNewVisit() throws Exception {
		Patient patient = new Patient();
        Location visitLocation = new Location();

		String formattedPatient = "Patient name";
		when(uiUtils.format(patient)).thenReturn(formattedPatient);
		when(uiUtils.message("coreapps.visit.createQuickVisit.successMessage", formattedPatient)).thenReturn(successMessage);
		
		HttpSession session = mock(HttpSession.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getSession()).thenReturn(session);

        Visit visit = createVisit();

        when(adtService.ensureVisit(eq(patient), any(Date.class), eq(visitLocation))).thenReturn(visit);

		FragmentActionResult result = controller.create(adtService, visitService, patient, visitLocation, uiUtils, null, emrContext, request);

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

        FragmentActionResult result = controller.create(adtService, visitService, patient, visitLocation, uiUtils, null, emrContext, request);

        assertThat(result, is(instanceOf(FailureResult.class)));
        verify(adtService, never()).ensureVisit(eq(patient), any(Date.class), eq(visitLocation));
    }

    @Test
    public void shouldCreateNewVisitWithVisitType() throws Exception {
	    Patient patient = new Patient();
	    Location visitLocation = new Location();

	    String formattedPatient = "Patient name";
	    when(uiUtils.format(patient)).thenReturn(formattedPatient);
	    when(uiUtils.message("coreapps.visit.createQuickVisit.successMessage", formattedPatient)).thenReturn(successMessage);

	    HttpSession session = mock(HttpSession.class);
	    HttpServletRequest request = mock(HttpServletRequest.class);
	    when(request.getSession()).thenReturn(session);

	    Visit visit = createVisit();

	    VisitType visitType = new VisitType();
	    visitType.setId(1);
	    visitType.setName("Outpatient");
	    when(visitService.saveVisitType(visitType)).thenReturn(visitType);

	    when(adtService.ensureVisit(eq(patient), any(Date.class), eq(visitLocation))).thenReturn(visit);

	    FragmentActionResult result = controller.create(adtService, visitService, patient, visitLocation, uiUtils, visitType, emrContext, request);

	    verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE, successMessage);
	    verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");
	    assertThat(result, is(CoreMatchers.any(FragmentActionResult.class)));

    }

	@Test
	public void shouldCreateNewVisitWithVisitTypeAndSingleVisitAttribute() throws Exception {
		Patient patient = new Patient();
		Location visitLocation = new Location();

		String formattedPatient = "Patient name";
		when(uiUtils.format(patient)).thenReturn(formattedPatient);
		when(uiUtils.message("coreapps.visit.createQuickVisit.successMessage", formattedPatient)).thenReturn(successMessage);

		HttpSession session = mock(HttpSession.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getSession()).thenReturn(session);

		Visit visit = createVisit();

		VisitType visitType = new VisitType();
		visitType.setId(1);
		visitType.setName("Outpatient");
		when(visitService.saveVisitType(visitType)).thenReturn(visitType);

		// create a visit attribute type
		VisitAttributeType visitAttributeType = new VisitAttributeType();
		visitAttributeType.setVisitAttributeTypeId(1);
		visitAttributeType.setName("First Visit");
		visitAttributeType.setDatatypeClassname("org.openmrs.customdatatype.datatype.BooleanDatatype");
		visitAttributeType.setMinOccurs(0);
		when(visitService.saveVisitAttributeType(visitAttributeType)).thenReturn(visitAttributeType);

		request.getParameterMap().put("attribute.1.new[0]", "false");

		when(adtService.ensureVisit(eq(patient), any(Date.class), eq(visitLocation))).thenReturn(visit);

		FragmentActionResult result = controller.create(adtService, visitService, patient, visitLocation, uiUtils, visitType, emrContext, request);

		verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE, successMessage);
		verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");
		assertThat(result, is(CoreMatchers.any(FragmentActionResult.class)));
	}

	@Test
	public void shouldCreateNewVisitWithVisitTypeAndMultipleVisitAttributes() throws Exception{
		Patient patient = new Patient();
		Location visitLocation = new Location();

		String formattedPatient = "Patient name";
		when(uiUtils.format(patient)).thenReturn(formattedPatient);
		when(uiUtils.message("coreapps.visit.createQuickVisit.successMessage", formattedPatient)).thenReturn(successMessage);

		HttpSession session = mock(HttpSession.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getSession()).thenReturn(session);

		Visit visit = createVisit();

		VisitType visitType = new VisitType();
		visitType.setId(1);
		visitType.setName("Outpatient");
		when(visitService.saveVisitType(visitType)).thenReturn(visitType);

		// create visit attribute types
		VisitAttributeType visitAttributeType = new VisitAttributeType();
		visitAttributeType.setVisitAttributeTypeId(1);
		visitAttributeType.setName("First Visit");
		visitAttributeType.setDatatypeClassname("org.openmrs.customdatatype.datatype.BooleanDatatype");
		visitAttributeType.setMinOccurs(0);
		when(visitService.saveVisitAttributeType(visitAttributeType)).thenReturn(visitAttributeType);

		VisitAttributeType visitAttributeType2 = new VisitAttributeType();
		visitAttributeType2.setVisitAttributeTypeId(2);
		visitAttributeType2.setName("Admission Date");
		visitAttributeType2.setDatatypeClassname("org.openmrs.customdatatype.datatype.DateDatatype");
		visitAttributeType2.setMinOccurs(0);
		when(visitService.saveVisitAttributeType(visitAttributeType2)).thenReturn(visitAttributeType2);

		request.getParameterMap().put("attribute.1.new[0]", "false");
		request.getParameterMap().put("attribute.2.new[0]", "2016-12-08");

		when(adtService.ensureVisit(eq(patient), any(Date.class), eq(visitLocation))).thenReturn(visit);

		FragmentActionResult result = controller.create(adtService, visitService, patient, visitLocation, uiUtils, visitType, emrContext, request);

		verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE, successMessage);
		verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");
		assertThat(result, is(CoreMatchers.any(FragmentActionResult.class)));
	}

	@Test
	public void shouldUpdateVisitWithDifferentVisitType() throws Exception{
		Patient patient = new Patient();
		patient.setId(1);

		Location visitLocation = new Location();

		String formattedPatient = "Patient name";
		when(uiUtils.format(patient)).thenReturn(formattedPatient);
		when(uiUtils.message("coreapps.visit.createQuickVisit.successMessage", formattedPatient)).thenReturn(successMessage);

		String updateSuccessMessage = "Update message";
		when(uiUtils.message("coreapps.visit.updateVisit.successMessage", formattedPatient)).thenReturn(updateSuccessMessage);

		HttpSession session = mock(HttpSession.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getSession()).thenReturn(session);

		Visit visit = createVisit();
		visit.setPatient(patient);

		VisitType visitType = new VisitType();
		visitType.setId(1);
		visitType.setName("Inpatient");
		when(visitService.saveVisitType(visitType)).thenReturn(visitType);

		VisitType visitType2 = new VisitType();
		visitType2.setId(2);
		visitType2.setName("Outpatient");
		when(visitService.saveVisitType(visitType2)).thenReturn(visitType2);

		when(adtService.ensureVisit(eq(patient), any(Date.class), eq(visitLocation))).thenReturn(visit);

		FragmentActionResult result = controller.create(adtService, visitService, patient,
				visitLocation, uiUtils, visitType, emrContext, request);

		verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE, successMessage);
		verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");
		assertThat(result, is(CoreMatchers.any(FragmentActionResult.class)));

		FragmentActionResult update = controller.update(visitService, patient, visit, visitType2, uiUtils, request);

		verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE, updateSuccessMessage);
		assertThat(update, is(CoreMatchers.any(FragmentActionResult.class)));
	}

	@Test
	public void shouldUpdateVisitWithNewVisitAttributes() throws Exception{
		Patient patient = new Patient();
		patient.setId(1);

		Location visitLocation = new Location();

		String formattedPatient = "Patient name";
		when(uiUtils.format(patient)).thenReturn(formattedPatient);
		when(uiUtils.message("coreapps.visit.createQuickVisit.successMessage", formattedPatient)).thenReturn(successMessage);

		String updateSuccessMessage = "Update message";
		when(uiUtils.message("coreapps.visit.updateVisit.successMessage", formattedPatient)).thenReturn(updateSuccessMessage);

		HttpSession session = mock(HttpSession.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getSession()).thenReturn(session);

		Visit visit = createVisit();
		visit.setPatient(patient);

		VisitType visitType = new VisitType();
		visitType.setId(1);
		visitType.setName("Outpatient");
		when(visitService.saveVisitType(visitType)).thenReturn(visitType);

		// create visit attribute types
		VisitAttributeType visitAttributeType = new VisitAttributeType();
		visitAttributeType.setVisitAttributeTypeId(1);
		visitAttributeType.setName("First Visit");
		visitAttributeType.setDatatypeClassname("org.openmrs.customdatatype.datatype.BooleanDatatype");
		visitAttributeType.setMinOccurs(0);
		when(visitService.saveVisitAttributeType(visitAttributeType)).thenReturn(visitAttributeType);

		VisitAttributeType visitAttributeType2 = new VisitAttributeType();
		visitAttributeType2.setVisitAttributeTypeId(2);
		visitAttributeType2.setName("Admission Date");
		visitAttributeType2.setDatatypeClassname("org.openmrs.customdatatype.datatype.DateDatatype");
		visitAttributeType2.setMinOccurs(0);
		when(visitService.saveVisitAttributeType(visitAttributeType2)).thenReturn(visitAttributeType2);

		request.getParameterMap().put("attribute.1.new[0]", "false");
		request.getParameterMap().put("attribute.2.new[0]", "2016-12-08");

		when(adtService.ensureVisit(eq(patient), any(Date.class), eq(visitLocation))).thenReturn(visit);

		FragmentActionResult result = controller.create(adtService, visitService, patient, visitLocation, uiUtils, visitType, emrContext, request);

		verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE, successMessage);
		verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");
		assertThat(result, is(CoreMatchers.any(FragmentActionResult.class)));

		request.getParameterMap().put("attribute.1.existing[1]", "true");
		request.getParameterMap().put("attribute.2.existing[2]", "2016-12-07");

		FragmentActionResult update = controller.update(visitService, patient, visit, null, uiUtils, request);

		verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE, updateSuccessMessage);
		assertThat(update, is(CoreMatchers.any(FragmentActionResult.class)));
	}

	@Test
	public void shouldUpdateVisitWithExistingVisitAttributes() throws Exception{
		Patient patient = new Patient();
		patient.setId(1);

		Location visitLocation = new Location();

		String formattedPatient = "Patient name";
		when(uiUtils.format(patient)).thenReturn(formattedPatient);
		when(uiUtils.message("coreapps.visit.createQuickVisit.successMessage", formattedPatient)).thenReturn(successMessage);

		String updateSuccessMessage = "Update message";
		when(uiUtils.message("coreapps.visit.updateVisit.successMessage", formattedPatient)).thenReturn(updateSuccessMessage);

		HttpSession session = mock(HttpSession.class);
		HttpServletRequest request = mock(HttpServletRequest.class);
		when(request.getSession()).thenReturn(session);

		Visit visit = createVisit();
		visit.setPatient(patient);

		VisitType visitType = new VisitType();
		visitType.setId(1);
		visitType.setName("Outpatient");
		when(visitService.saveVisitType(visitType)).thenReturn(visitType);

		// create visit attribute types
		VisitAttributeType visitAttributeType = new VisitAttributeType();
		visitAttributeType.setVisitAttributeTypeId(1);
		visitAttributeType.setName("First Visit");
		visitAttributeType.setDatatypeClassname("org.openmrs.customdatatype.datatype.BooleanDatatype");
		visitAttributeType.setMinOccurs(0);
		when(visitService.saveVisitAttributeType(visitAttributeType)).thenReturn(visitAttributeType);

		VisitAttributeType visitAttributeType2 = new VisitAttributeType();
		visitAttributeType2.setVisitAttributeTypeId(2);
		visitAttributeType2.setName("Admission Date");
		visitAttributeType2.setDatatypeClassname("org.openmrs.customdatatype.datatype.DateDatatype");
		visitAttributeType2.setMinOccurs(0);
		when(visitService.saveVisitAttributeType(visitAttributeType2)).thenReturn(visitAttributeType2);

		VisitAttributeType visitAttributeType3 = new VisitAttributeType();
		visitAttributeType3.setVisitAttributeTypeId(3);
		visitAttributeType3.setName("Bed");
		visitAttributeType3.setDatatypeClassname("org.openmrs.customdatatype.datatype.LongFreeTextDatatype");
		visitAttributeType3.setMinOccurs(0);
		when(visitService.saveVisitAttributeType(visitAttributeType3)).thenReturn(visitAttributeType3);

		VisitAttributeType visitAttributeType4 = new VisitAttributeType();
		visitAttributeType4.setVisitAttributeTypeId(4);
		visitAttributeType4.setName("Ward");
		visitAttributeType4.setDatatypeClassname("org.openmrs.customdatatype.datatype.LongFreeTextDatatype");
		visitAttributeType4.setMinOccurs(0);
		when(visitService.saveVisitAttributeType(visitAttributeType4)).thenReturn(visitAttributeType4);

		request.getParameterMap().put("attribute.1.new[0]", "false");
		request.getParameterMap().put("attribute.2.new[0]", "2016-12-08");

		when(adtService.ensureVisit(eq(patient), any(Date.class), eq(visitLocation))).thenReturn(visit);

		FragmentActionResult result = controller.create(adtService, visitService, patient, visitLocation, uiUtils, visitType, emrContext, request);

		verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE, successMessage);
		verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");
		assertThat(result, is(CoreMatchers.any(FragmentActionResult.class)));

		request.getParameterMap().put("attribute.3.new[0]", "Bed No.2");
		request.getParameterMap().put("attribute.4.new[0]", "Male");

		FragmentActionResult update = controller.update(visitService, patient, visit, null, uiUtils, request);

		verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE, updateSuccessMessage);
		assertThat(update, is(CoreMatchers.any(FragmentActionResult.class)));

	}

    private Visit createVisit() {
        Visit visit = new Visit();
        visit.setId(1);
        return visit;
    }

}
