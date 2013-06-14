package org.openmrs.module.coreapps.fragment.controller.visit;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.module.appui.AppUiConstants;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

public class RetrospectiveVisitFragmentController {

    private final Logger log = LoggerFactory.getLogger(getClass());

    public FragmentActionResult create(@SpringBean("adtService") AdtService adtService,
                                       @RequestParam("patientId") Patient patient,
                                       @RequestParam("locationId") Location location,
                                       @RequestParam("startDate") Date startDate,
                                       @RequestParam(value = "stopDate", required = false) Date stopDate,
                                       HttpServletRequest request, UiUtils ui) {

        try {
            adtService.createRetrospectiveVisit(patient, location, startDate, stopDate);
        }
        catch (Exception e) {
            log.error("Unable to add retrospective visit", e);
            return new FailureResult(ui.message(e.getMessage()));
        }

        request.getSession().setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE,
                ui.message("coreapps.retrospectiveVisit.addedVisitMessage"));
        request.getSession().setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");

        return new SuccessResult();

    }

}
