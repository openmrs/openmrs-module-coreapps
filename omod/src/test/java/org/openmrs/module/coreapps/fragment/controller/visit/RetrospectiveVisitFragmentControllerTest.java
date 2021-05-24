package org.openmrs.module.coreapps.fragment.controller.visit;

import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.appui.AppUiConstants;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.adt.exception.ExistingVisitDuringTimePeriodException;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Matchers.argThat;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.eq;
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
    public void test_shouldCreateNewRetrospectiveVisit() throws Exception {

        when(ui.message("coreapps.retrospectiveVisit.addedVisitMessage")).thenReturn("success message");

        Patient patient = createPatient();

        Location location = new Location();
        Date startDate = new DateTime(2012, 1, 1, 12, 12, 12).toDate();
        Date stopDate = new DateTime(2012, 1, 2, 13,13, 13).toDate();

        Visit visit = createVisit();

        when(adtService.createRetrospectiveVisit(patient, location, startDate, stopDate)).thenReturn(new VisitDomainWrapper(visit));
        SimpleObject result = (SimpleObject) controller.create(adtService, patient, location, startDate, stopDate, request, ui);

        verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE,
                ui.message("coreapps.retrospectiveVisit.addedVisitMessage"));
        verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");

        assertTrue((Boolean) result.get("success"));
        assertThat((String) result.get("id"), is(visit.getId().toString()));
        assertThat((String) result.get("uuid"), is(visit.getUuid()));
    }

    @Test
    public void test_shouldCreateNewRetrospectiveVisit_whenNoStopDateSpecified() throws Exception {

        when(ui.message("coreapps.retrospectiveVisit.addedVisitMessage")).thenReturn("success message");

        Patient patient = createPatient();
        Location location = new Location();
        Date startDate = new DateTime(2012, 1, 1, 12, 12, 12).toDate();
        Date auxStopDate= startDate;

        Visit visit = createVisit();

        when(adtService.createRetrospectiveVisit(patient, location, startDate, auxStopDate)).thenReturn(new VisitDomainWrapper(visit));

        SimpleObject result = (SimpleObject) controller.create(adtService, patient, location, startDate, null, request, ui);

        verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE,
                ui.message("coreapps.retrospectiveVisit.addedVisitMessage"));
        verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");

        assertTrue((Boolean) result.get("success"));
        assertThat((String) result.get("id"), is(visit.getId().toString()));
        assertThat((String) result.get("uuid"), is(visit.getUuid()));
    }

    @Test
    public void test_shouldReturnConflictingVisits() throws Exception {

        Patient patient = new Patient();
        Location location = new Location();
        Date startDate = new DateTime(2012, 1, 1, 12, 12, 12).toDate();
        Date auxStopDate = startDate;


        Visit conflictingVisit = new Visit();
        conflictingVisit.setStartDatetime(new DateTime(2012, 1, 1, 0, 0, 0,0).toDate());
        conflictingVisit.setStopDatetime(new DateTime(2012, 1, 3, 0, 0, 0, 999).toDate());

        when(adtService.createRetrospectiveVisit(patient, location, startDate, auxStopDate))
                .thenThrow(ExistingVisitDuringTimePeriodException.class);

        when(adtService.getVisits(patient, location, startDate, auxStopDate))
                .thenReturn(Collections.singletonList(new VisitDomainWrapper(conflictingVisit)));

        when(ui.format(any())).thenReturn("someDate");

        List<SimpleObject> result = (List<SimpleObject>) controller.create(adtService, patient, location, startDate, null, request, ui);

        assertThat(result.size(), is(1));
        assertThat(result.get(0).toJson(), is("{\"startDate\":\"someDate\",\"stopDate\":\"someDate\",\"id\":null,\"uuid\":\"" + conflictingVisit.getUuid() + "\"}"));
    }


    @Test
    public void test_shouldSetEndDateToCurrentTimeIfEndDateIsCurrentDay() throws Exception {

        when(ui.message("coreapps.retrospectiveVisit.addedVisitMessage")).thenReturn("success message");

        Patient patient = createPatient();
        Location location = new Location();
        Visit mockVisit = new Visit();
        mockVisit.setId(1);

        Date startDate = new DateTime().withTime(0,0,0,0).toDate();
        Date endDate = new DateTime().withTime(23,59,59,59).toDate();

        when(adtService.createRetrospectiveVisit(eq(patient), eq(location), eq(startDate), any(Date.class))).thenReturn(new VisitDomainWrapper(mockVisit));  // to prevent against NPE when generating success message

        controller.create(adtService, patient, location, startDate, endDate, request, ui);

        verify(adtService).createRetrospectiveVisit(eq(patient), eq(location), eq(startDate), argThat(new IsWithinDateRange(startDate, endDate)));

    }



    private Visit createVisit() {
        Visit visit = new Visit();
        visit.setId(1);
        return visit;
    }

    private Patient createPatient() {
        Patient patient = new Patient();
        patient.setId(1);
        return patient;
    }

    private class IsWithinDateRange extends ArgumentMatcher<Date> {

        private Date expectedMinDateValue;

        private Date expectedMaxDateValue;

        public IsWithinDateRange(Date expectedMinDateValue, Date expectedMaxDateValue) {
            this.expectedMinDateValue = expectedMinDateValue;
            this.expectedMaxDateValue = expectedMaxDateValue;
        }

        @Override
        public boolean matches(Object o) {
            Date date = (Date) o;
            return (date.after(expectedMinDateValue) || date.equals(expectedMinDateValue))
                    && (date.before(expectedMaxDateValue) || date.equals(expectedMaxDateValue));
        }

    }

}
