package org.openmrs.module.coreapps.page.controller;

import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.api.LocationService;
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
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

public class MergeVisitsPageController {
    public Object controller(@RequestParam("patientId") Patient patient,
                    @InjectBeans PatientDomainWrapper patientDomainWrapper,
                    UiSessionContext sessionContext,
                    PageModel model, @SpringBean AdtService service,
                    @SpringBean("locationService") LocationService locationService) {

        if (patient.isVoided() || patient.isPersonVoided()) {
            return new Redirect("coreapps", "patientdashboard/deletedPatient", "patientId=" + patient.getId());
        }

        patientDomainWrapper.setPatient(patient);
        model.addAttribute("patient", patientDomainWrapper);

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
                       UiUtils ui,
                       HttpServletRequest request){

        if (patient.isVoided() || patient.isPersonVoided()) {
            return "redirect:" + ui.pageLink("coreapps", "patientdashboard/deletedPatient",
                    SimpleObject.create("patientId", patient.getId().toString() ));
        }

        if (mergeVisits!=null && mergeVisits.size() > 0 ){
            //are the visits consecutives ?
            //merge visits
        }
        return "redirect:" + ui.pageLink("coreapps", "mergeVisits", SimpleObject.create("patientId", patient.getId().toString() ));
    }
}
