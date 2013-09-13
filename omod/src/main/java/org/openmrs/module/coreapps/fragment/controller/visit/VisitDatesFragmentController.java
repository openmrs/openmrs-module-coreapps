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

import org.joda.time.DateTime;
import org.openmrs.Visit;
import org.openmrs.api.VisitService;
import org.openmrs.module.appui.AppUiConstants;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

import static org.apache.commons.lang.time.DateUtils.isSameDay;

@Transactional
public class VisitDatesFragmentController {

    public SimpleObject setDuration(@SpringBean("visitService") VisitService visitService,
                                            @RequestParam("visitId") Visit visit,
                                            @RequestParam("startDate") Date startDate,
                                            @RequestParam(value="stopDate", required = false) Date stopDate,
                                            HttpServletRequest request, UiUtils ui) {


        if (!isSameDay(startDate, visit.getStartDatetime())) {
            visit.setStartDatetime(new DateTime(startDate).toDateMidnight().toDate());
        }

        if ( (stopDate!=null) && !isSameDay(stopDate, visit.getStopDatetime())) {
            Date now = new DateTime().toDate();
            if (isSameDay(stopDate, now)) {
                visit.setStopDatetime(now);
            } else {
                visit.setStopDatetime(new DateTime(stopDate)
                        .withHourOfDay(23)
                        .withMinuteOfHour(59)
                        .withSecondOfMinute(59)
                        .withMillisOfSecond(999)
                        .toDate());
            }
        }

        visitService.saveVisit(visit);

        request.getSession().setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE, ui.message("coreapps.editVisitDate.visitSavedMessage"));
        request.getSession().setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");

        return SimpleObject.create("success", true, "search", "?patientId=" + visit.getPatient().getId() + "&visitId=" + visit.getId());

    }

}
