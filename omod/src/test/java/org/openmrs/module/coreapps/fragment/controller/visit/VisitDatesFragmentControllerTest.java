package org.openmrs.module.coreapps.fragment.controller.visit;

import org.joda.time.DateTime;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.openmrs.module.appui.AppUiConstants;
import org.openmrs.ui.framework.UiUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

import static org.mockito.Mockito.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class VisitDatesFragmentControllerTest {
    @Test
    public void shouldSetToastMessageOnSetDuration() throws Exception {
        VisitDatesFragmentController controller = new VisitDatesFragmentController();

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);

        UiUtils ui = mock(UiUtils.class);
        when(ui.message("coreapps.editVisitDate.visitSavedMessage")).thenReturn("message");

        controller.setDuration(mock(VisitService.class), new Visit(), new Date(), new Date(), request, ui);

        verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE,
                "message");
        verify(session).setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");
    }

    @Test
    public void shouldSetVisitStartAndStopDates() throws Exception {
        VisitDatesFragmentController controller = new VisitDatesFragmentController();

        VisitService visitService = mock(VisitService.class);

        Date startDate = (new DateTime(2013, 6, 24, 13, 1, 7)).toDate();
        Date stopDate = (new DateTime(2013, 6, 26, 17, 12, 32)).toDate();

        final Date expectedStartDate = (new DateTime(2013, 6, 24, 0, 0, 0)).toDate();
        final Date expectedStopDate = (new DateTime(2013, 6, 26, 23, 59, 59, 999)).toDate();

        HttpServletRequest request = mock(HttpServletRequest.class);
        HttpSession session = mock(HttpSession.class);
        when(request.getSession()).thenReturn(session);

        controller.setDuration(visitService, new Visit(), startDate, stopDate, request, mock(UiUtils.class));

        verify(visitService).saveVisit(argThat(new ArgumentMatcher<Visit>() {
            @Override
            public boolean matches(Object o) {
                Visit actual = (Visit) o;
                return actual.getStartDatetime().equals(expectedStartDate) &&
                        actual.getStopDatetime().equals(expectedStopDate);
            }
        }));
    }
}
