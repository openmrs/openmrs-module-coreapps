package org.openmrs.module.coreapps.page.controller;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.LocationService;
import org.openmrs.module.appui.AppUiConstants;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.Redirect;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MergeVisitsPageController {
    public Object controller(@RequestParam("patientId") Patient patient,
                             @InjectBeans PatientDomainWrapper patientDomainWrapper,
                             UiSessionContext sessionContext,
                             PageModel model, @SpringBean AdtService service,
                             @SpringBean("locationService") LocationService locationService,
                             @RequestParam(value = "returnUrl", required = false) String returnUrl,
                             @RequestParam(value = "mergedVisitIds", required = false) List<String> mergedVisitIds) {


        if (patient.isVoided() || patient.isPersonVoided()) {
            return new Redirect("coreapps", "patientdashboard/deletedPatient", "patientId=" + patient.getId());
        }

        patientDomainWrapper.setPatient(patient);
        model.addAttribute("patient", patientDomainWrapper);

        //Remove the visit ID from URL, in case we merge that visit, it should not be possible to return to it.
        if (!StringUtils.isEmpty(returnUrl) && returnUrl.contains("visitId=") && mergedVisitIds.size() > 0) {
            returnUrl = updateVisitIdUrlParameter(returnUrl, mergedVisitIds);
        }

        model.addAttribute("returnUrl", returnUrl);

        Location sessionLocation = sessionContext.getSessionLocation();
        Location visitLocation = null;
        if (sessionLocation != null) {
            visitLocation = service.getLocationThatSupportsVisits(sessionLocation);
        }
        if (visitLocation != null) {
            VisitDomainWrapper activeVisit = service.getActiveVisit(patient, visitLocation);
            model.addAttribute("activeVisit", activeVisit);
        } else {
            throw new IllegalStateException("Configuration required: no visit location found based on session location");
        }
        return null;
    }

    public String post(@RequestParam("patientId") Patient patient,
                       @RequestParam("mergeVisits") List<Integer> mergeVisits,
                       @RequestParam(value = "returnUrl", required = false) String returnUrl,
                       @SpringBean AdtService service,
                       UiUtils ui,
                       HttpServletRequest request, PageModel model) {

        List<String> mergedVisitIds = new ArrayList<String>();
        if (patient.isVoided() || patient.isPersonVoided()) {
            return "redirect:" + ui.pageLink("coreapps", "patientdashboard/deletedPatient",
                    SimpleObject.create("patientId", patient.getId().toString()));
        }

        if (mergeVisits != null && mergeVisits.size() > 0) {
            Visit mergedVisit = service.mergeConsecutiveVisits(mergeVisits, patient);
            if (mergedVisit != null) {
                request.getSession().setAttribute(AppUiConstants.SESSION_ATTRIBUTE_INFO_MESSAGE, ui.message("coreapps.task.mergeVisits.success"));
                request.getSession().setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");
            } else {
                request.getSession().setAttribute("emr.errorMessage", ui.message("coreapps.task.mergeVisits.error"));
                request.getSession().setAttribute(AppUiConstants.SESSION_ATTRIBUTE_TOAST_MESSAGE, "true");
            }

           for (int i = 1; i < mergeVisits.size(); i++) {
                mergedVisitIds.add(mergeVisits.get(i).toString());
            }
        }
        return "redirect:" + ui.pageLink("coreapps", "mergeVisits", SimpleObject.create("patientId", patient.getId(), "returnUrl", returnUrl, "mergedVisitIds", mergedVisitIds));
    }


    /**
     * If some of the merged visit IDs are on the return url,  it scraps the visitId param altogether off the UR
     * so it doesn't return to a visit that no longer exists.
     */
    public String updateVisitIdUrlParameter(String returnUrl, List<String> mergedVisitsIDs) {
        //Pattern to get the visitID from url
        Pattern pat = Pattern.compile("(?<=visitId=)[^&]+");
        Matcher urlVisitId = pat.matcher(returnUrl);
        //If find some visitID on url.
        if (urlVisitId.find()) {
            for (String s : mergedVisitsIDs.get(0).split(",")) {
                if (urlVisitId.group(0).equals(s)) {
                    //remove the visitID from url
                    returnUrl = returnUrl.replaceAll("(&visitId[^&]+)", "");
                    break;
                }
            }
        }
        return returnUrl;
    }
}
