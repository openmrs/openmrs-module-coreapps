package org.openmrs.module.coreapps.page.controller;

import org.openmrs.Location;
import org.openmrs.api.LocationService;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * Created by cosmin on 1/17/14.
 */
public class AwaitingAdmissionPageController {

    public String get(UiSessionContext sessionContext, PageModel model, @SpringBean AdtService service,
                      @SpringBean("locationService") LocationService locationService, @RequestParam("app") AppDescriptor app) {

        Location sessionLocation = sessionContext.getSessionLocation();
        if (sessionLocation == null) {
            return "redirect:login.htm";
        }
        Location visitLocation = null;
        if (sessionLocation != null) {
            visitLocation = service.getLocationThatSupportsVisits(sessionLocation);
        }
        if (visitLocation == null) {
            throw new IllegalStateException("Configuration required: no visit location found based on session location");
        }

        model.addAttribute("visitSummaries", null);

        String patientPageUrl = app.getConfig().get("patientPageUrl").getTextValue();
        model.addAttribute("patientPageUrl", patientPageUrl);

        return null;
    }
}
