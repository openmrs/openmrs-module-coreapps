package org.openmrs.module.coreapps.fragment.controller.visit;

import org.joda.time.DateTime;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
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

    static final String ALLOW_OVERLAPPING_VISIT_AT_ANOTHER_LOCATION_GP =
            "coreapps.addPastVisitsAllowOverlappingVisitAtAnotherLocation";

    private final Logger log = LoggerFactory.getLogger(getClass());

    public Object create(@SpringBean("adtService") AdtService adtService,
                                       @SpringBean("adminService") AdministrationService administrationService,
                                       @RequestParam("patientId") Patient patient,
                                       @RequestParam("locationId") Location location,
                                       @RequestParam("startDate") Date startDate,
                                       @RequestParam(value = "stopDate", required = false) Date stopDate,
                                       HttpServletRequest request, UiUtils ui) {

        if (ui.convertTimezones()) {
            //If stopDate is today, it cannot be today at 23:59:59, because that would be in the future.
            stopDate = stopDate != null && stopDate.after(new Date()) ? new Date() : stopDate;
        } else {
            // set the startDate time component to the start of day
            startDate = new DateTime(startDate).withTime(0,0,0,0).toDate();

            // if stopDate is today, set stopDate to current datetime, otherwise set time component to end of date
            if (stopDate != null) {
                if (new DateTime().withTime(0, 0, 0, 0).equals(new DateTime(stopDate).withTime(0, 0, 0, 0))) {
                    stopDate = new Date();
                } else {
                    stopDate = new DateTime(stopDate).withTime(23, 59, 59, 999).toDate();
                }
            }
        }

        String propValue = administrationService.getGlobalProperty(ALLOW_OVERLAPPING_VISIT_AT_ANOTHER_LOCATION_GP);
        boolean allowOverlappingAtAnotherLocation = !"false".equalsIgnoreCase(propValue);

        if (!allowOverlappingAtAnotherLocation) {
            List<SimpleObject> conflictingVisits = getConflictsAcrossAllLocations(adtService, patient, startDate, stopDate, ui);
            if (!conflictingVisits.isEmpty()) {
                return conflictingVisits;
            }
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
            List<SimpleObject> conflictingVisits = new ArrayList<SimpleObject>();
            List<VisitDomainWrapper> visits = adtService.getVisits(patient, location, startDate, stopDate);

            if (visits != null) {
                for (VisitDomainWrapper visit : visits) {
                    conflictingVisits.add(formatVisit(visit, ui));
                }
            }

            return conflictingVisits;
        }
        catch (Exception e) {
            log.error("Unable to add retrospective visit", e);
            return new FailureResult(ui.message(e.getMessage()));
        }
    }

    private List<SimpleObject> getConflictsAcrossAllLocations(AdtService adtService, Patient patient,
                                                               Date startDate, Date stopDate, UiUtils ui) {
        List<SimpleObject> conflictingVisits = new ArrayList<SimpleObject>();
        List<Location> allLocations = adtService.getAllLocationsThatSupportVisits();
        if (allLocations != null) {
            for (Location loc : allLocations) {
                List<VisitDomainWrapper> visits = adtService.getVisits(patient, loc, startDate, stopDate);
                if (visits != null) {
                    for (VisitDomainWrapper visit : visits) {
                        conflictingVisits.add(formatVisit(visit, ui));
                    }
                }
            }
        }
        return conflictingVisits;
    }

    private SimpleObject formatVisit(VisitDomainWrapper visit, UiUtils ui) {
        if (ui.convertTimezones()) {
            return SimpleObject.create(
                    "startDate", ui.formatDateWithClientTimezone(new DateTime(visit.getVisit().getStartDatetime()).toDate()),
                    "stopDate", ui.formatDateWithClientTimezone(new DateTime(visit.getVisit().getStopDatetime()).toDate()),
                    "id", visit.getVisit().getId(), "uuid", visit.getVisit().getUuid());
        } else {
            return SimpleObject.create(
                    "startDate", ui.format(new DateTime(visit.getVisit().getStartDatetime()).toDateMidnight().toDate()),
                    "stopDate", ui.format(new DateTime(visit.getVisit().getStopDatetime()).toDateMidnight().toDate()),
                    "id", visit.getVisit().getId(), "uuid", visit.getVisit().getUuid());
        }
    }

}
