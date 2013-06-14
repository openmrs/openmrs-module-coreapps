package org.openmrs.module.coreapps.fragment.controller.visit;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.module.appui.AppUiConstants;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RetrospectiveVisitFragmentControllerTest {

    RetrospectiveVisitFragmentController controller;

    AdtService adtService;

    UiUtils ui;

    HttpServletRequest request;

    HttpSession session;

    @Before
    public void setup() {
        controller = new RetrospectiveVisitFragmentController();
        adtService = mock(AdtService.class);
        ui = mock(UiUtils.class);
        request = mock(HttpServletRequest.class);
        session = mock(HttpSession.class);

        when(request.getSession()).thenReturn(session);
    }

    @Test
    public void test_shouldCreateNewRetrospectiveVisit() {

        when(ui.message("coreapps.retrospectiveVisit.addedVisitMessage")).thenReturn("success message");

        Patient patient = new Patient();
        Location location = new Location();
        Date startDate = new DateTime(2012, 1, 1, 0, 0, 0).toDate();
        Date stopDate = new DateTime(2012, 1, 2, 0, 0, 0).toDate();

        FragmentActionResult result = controller.create(adtService, patient, location, startDate, stopDate, request, ui);

        verify(adtService).createRetrospectiveVisit(patient, location, startDate, stopDate);
        verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE,
                ui.message("coreapps.retrospectiveVisit.addedVisitMessage"));
        verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");

        assertTrue(result instanceof SuccessResult);
    }
}
