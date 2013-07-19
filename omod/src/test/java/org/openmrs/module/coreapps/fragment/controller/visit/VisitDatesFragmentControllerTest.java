package org.openmrs.module.coreapps.fragment.controller.visit;

import org.joda.time.DateTime;
import org.joda.time.DateTimeUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.openmrs.module.appui.AppUiConstants;
import org.openmrs.ui.framework.UiUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.co.it.modular.hamcrest.date.DateMatchers.sameInstant;

public class VisitDatesFragmentControllerTest {
    private VisitDatesFragmentController controller;
    private VisitService visitService;
    private HttpServletRequest request;
    private HttpSession session;

    @Before
    public void setUp() throws Exception {
        //stop time so tests don't fail running overnight
        DateTimeUtils.setCurrentMillisFixed(new DateTime(2013, 6, 26, 13, 1, 7).getMillis());

        controller = new VisitDatesFragmentController();
        visitService = mock(VisitService.class);

        request = mock(HttpServletRequest.class);
        session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);
    }

    @Test
    public void shouldSetToastMessageOnSetDuration() throws Exception {
        UiUtils ui = mock(UiUtils.class);
        when(ui.message("coreapps.editVisitDate.visitSavedMessage")).thenReturn("message");

        Visit visit = new Visit(1);
        visit.setPatient(new Patient(1));
        visit.setStartDatetime(new Date());
        visit.setStopDatetime(new Date());
        controller.setDuration(visitService, visit, new Date(), new Date(), request, ui);

        verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE, "message");
        verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");
    }

    @Test
    public void shouldSetVisitStartAndStopDates() throws Exception {
        Date startDate = (new DateTime(2013, 6, 24, 13, 1, 7)).toDate();
        Date stopDate = (new DateTime(2013, 6, 25, 17, 12, 32)).toDate();

        Date expectedStartDate = (new DateTime(2013, 6, 24, 0, 0, 0)).toDate();
        Date expectedStopDate = (new DateTime(2013, 6, 25, 23, 59, 59, 999)).toDate();

        Visit visit = new Visit(1);
        visit.setStartDatetime(new Date());
        visit.setStopDatetime(new Date());
        visit.setPatient(new Patient(1));

        controller.setDuration(visitService, visit, startDate, stopDate, request, mock(UiUtils.class));

        Visit actualVisit = savedVisit();
        assertThat(actualVisit.getStartDatetime(), is(expectedStartDate));
        assertThat(actualVisit.getStopDatetime(), is(expectedStopDate));
    }

    @Test
    public void shouldNotChangeStartOrStopDatetimeIfSettingToSameDay() throws Exception {
        Visit visit = new Visit(1);
        visit.setPatient(new Patient(1));

        Date startDate = (new DateTime(2013, 6, 24, 13, 1, 7)).toDate();
        visit.setStartDatetime(startDate);

        Date stopDate = (new DateTime(2013, 6, 26, 17, 12, 32)).toDate();
        visit.setStopDatetime(stopDate);

        controller.setDuration(visitService, visit, (new DateTime(2013, 6, 24, 0, 0, 0)).toDate(), (new DateTime(2013, 6, 26, 0, 0, 0)).toDate(), request, mock(UiUtils.class));

        Visit actualVisit = savedVisit();
        assertThat(actualVisit.getStartDatetime(), is(startDate));
        assertThat(actualVisit.getStopDatetime(), is(stopDate));
    }

    @Test
    public void shouldSetTimeToNowIfChangingVisitEndToToday() throws Exception {
        Visit visit = new Visit(1);
        visit.setPatient(new Patient(1));

        Date startDate = (new DateTime(2013, 6, 24, 13, 1, 7)).toDate();
        visit.setStartDatetime(startDate);

        Date stopDate = (new DateTime(2013, 6, 25, 17, 12, 32)).toDate();
        visit.setStopDatetime(stopDate);

        Date today = new DateTime().toDate();
        controller.setDuration(visitService, visit, startDate, today, request, mock(UiUtils.class));

        assertThat(savedVisit().getStopDatetime(), sameInstant(today));
    }

    @Test
    public void shouldNotChangeStopDatetimeIfSettingToSameDayAndToday() throws Exception {
        Visit visit = new Visit(1);
        visit.setPatient(new Patient(1));

        Date startDate = (new DateTime(2013, 6, 24, 13, 1, 7)).toDate();
        visit.setStartDatetime(startDate);

        Date stopDate = new DateTime().plusHours(-1).toDate();
        visit.setStopDatetime(stopDate);

        controller.setDuration(visitService, visit, startDate, new DateTime().toDate(), request, mock(UiUtils.class));

        assertThat(savedVisit().getStopDatetime(), is(stopDate));
    }

    private Visit savedVisit() {
        ArgumentCaptor<Visit> captor = ArgumentCaptor.forClass(Visit.class);
        verify(visitService).saveVisit(captor.capture());
        return captor.getValue();
    }
}
