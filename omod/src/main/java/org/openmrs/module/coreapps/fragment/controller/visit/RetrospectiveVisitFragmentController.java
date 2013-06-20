package org.openmrs.module.coreapps.fragment.controller.visit;

import org.joda.time.DateTime;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.module.appui.AppUiConstants;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.adt.exception.ExistingVisitDuringTimePeriodException;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RetrospectiveVisitFragmentController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public Object create(@SpringBean("adtService") AdtService adtService,
                                       @RequestParam("patientId") Patient patient,
                                       @RequestParam("locationId") Location location,
                                       @RequestParam("startDate") Date startDate,
                                       @RequestParam(value = "stopDate", required = false) Date stopDate,
                                       HttpServletRequest request, UiUtils ui) {

        // if no stop date, set it to start date
        if (stopDate == null) {
            stopDate = startDate;
        }

        // set the startDate and stopDate time components to the start and end of the day, respectively
        startDate = new DateTime(startDate).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0).toDate();
        stopDate = new DateTime(stopDate).withHourOfDay(23).withMinuteOfHour(59).withSecondOfMinute(59).toDate();

        try {
            VisitDomainWrapper createdVisit = adtService.createRetrospectiveVisit(patient, location, startDate, stopDate);

            request.getSession().setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE,
                    ui.message("coreapps.retrospectiveVisit.addedVisitMessage"));
            request.getSession().setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");

            return SimpleObject.create("success", true, "url", ui.pageLink("coreapps", "patientdashboard/patientDashboard",
                    SimpleObject.create("patientId", patient.getId().toString(), "visitId", createdVisit.getVisit().getId().toString())));
        }
        catch (ExistingVisitDuringTimePeriodException e) {

            // if there are existing visit(s), return these existing visits
            List<SimpleObject> simpleVisits = new ArrayList<SimpleObject>();
            List<VisitDomainWrapper> visits = adtService.getVisits(patient, location, startDate, stopDate);

            if (visits != null) {
                for (VisitDomainWrapper visit : visits) {
                    simpleVisits.add(SimpleObject.create("startDate", ui.format(new DateTime(visit.getVisit().getStartDatetime()).toDateMidnight().toDate()),
                            "stopDate", ui.format(new DateTime(visit.getVisit().getStopDatetime()).toDateMidnight().toDate()),
                            "id", visit.getVisit().getId()));
                }
            }

            return simpleVisits;
        }
        catch (Exception e) {
            log.error("Unable to add retrospective visit", e);
            return new FailureResult(ui.message(e.getMessage()));
        }
    }

}
