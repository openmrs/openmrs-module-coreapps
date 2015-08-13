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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.servlet.http.HttpServletRequest;

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

        // set the startDate time component to the start of day
        startDate = new DateTime(startDate).withTime(0,0,0,0).toDate();

        // if stopDate is today, set stopDate to current datetime, otherwise set time component to end of date
        if (new DateTime().withTime(0,0,0,0).equals(new DateTime(stopDate).withTime(0,0,0,0))) {
            stopDate = new Date();
        }
        else {
            stopDate = new DateTime(stopDate).withTime(23, 59, 59, 999).toDate();
        }

        try {
            VisitDomainWrapper createdVisit = adtService.createRetrospectiveVisit(patient, location, startDate, stopDate);

            request.getSession().setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE,
                    ui.message("coreapps.retrospectiveVisit.addedVisitMessage"));
            request.getSession().setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");

            return SimpleObject.create("success", true, "id", createdVisit.getVisit().getId().toString(), "uuid", createdVisit.getVisit().getUuid());
        }
        catch (ExistingVisitDuringTimePeriodException e) {

            // if there are existing visit(s), return these existing visits
            List<SimpleObject> simpleVisits = new ArrayList<SimpleObject>();
            List<VisitDomainWrapper> visits = adtService.getVisits(patient, location, startDate, stopDate);

            if (visits != null) {
                for (VisitDomainWrapper visit : visits) {
                    simpleVisits.add(SimpleObject.create("startDate", ui.format(new DateTime(visit.getVisit().getStartDatetime()).toDateMidnight().toDate()),
                            "stopDate", ui.format(new DateTime(visit.getVisit().getStopDatetime()).toDateMidnight().toDate()),
                            "id", visit.getVisit().getId(), "uuid", visit.getVisit().getUuid()));
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
