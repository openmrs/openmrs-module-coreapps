package org.openmrs.module.coreapps.fragment.controller.dashboardwidgets;

import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

public class CustomLinksWidgetFragmentController {

    public void controller(FragmentConfiguration config,
                           UiUtils uiUtils,
                           @FragmentParam("app") AppDescriptor app,
                           @SpringBean("adtService") AdtService adtService,
                           @RequestParam("patientId") Patient patient,
                           UiSessionContext sessionContext) {

        Location visitLocation = adtService.getLocationThatSupportsVisits(sessionContext.getSessionLocation());
        VisitDomainWrapper activeVisit = adtService.getActiveVisit(patient, visitLocation);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> links = mapper.convertValue(app.getConfig().get("links"), Map.class);

        replacePatientVariables(links, patient, uiUtils);

        if (activeVisit != null) {
            replaceVisitVariables(links, activeVisit.getVisit(), uiUtils);
        }

        config.addAttribute("icon", app.getIcon());
        config.addAttribute("label", app.getLabel());
        config.addAttribute("links", links);
    }

    private void replacePatientVariables(Map<String, String> links, Patient patient, UiUtils uiUtils) {
        for(Map.Entry<String, String> entry: links.entrySet()){
            entry.setValue(uiUtils.urlBind(entry.getValue(), patient));
        }
    }

    private void replaceVisitVariables(Map<String, String> links, Visit visit, UiUtils uiUtils) {
        for(Map.Entry<String, String> entry: links.entrySet()){
            entry.setValue(uiUtils.urlBind(entry.getValue(), visit));
        }
    }

}
