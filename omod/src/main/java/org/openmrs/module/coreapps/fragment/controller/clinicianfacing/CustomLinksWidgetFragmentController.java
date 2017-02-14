package org.openmrs.module.coreapps.fragment.controller.clinicianfacing;

import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

public class CustomLinksWidgetFragmentController {

    public void controller(FragmentConfiguration config,
                           @FragmentParam("app") AppDescriptor app,
                           @SpringBean("adtService") AdtService adtService,
                           @RequestParam("patientId") Patient patient,
                           UiSessionContext sessionContext) {

        Location visitLocation = adtService.getLocationThatSupportsVisits(sessionContext.getSessionLocation());
        VisitDomainWrapper activeVisit = adtService.getActiveVisit(patient, visitLocation);

        ObjectMapper mapper = new ObjectMapper();
        Map<String, String> links = mapper.convertValue(app.getConfig().get("links"), Map.class);

        String patientUuid = patient.getUuid();
        String patientId = patient.getId().toString();
        replacePatientVariables(links, patientUuid, patientId);

        if (activeVisit != null) {
            String visitUuid = activeVisit.getVisit().getUuid();
            String visitId = activeVisit.getVisit().getId().toString();
            replaceVisitVariables(links, visitUuid, visitId);
        }


        config.addAttribute("icon", app.getIcon());
        config.addAttribute("label", app.getLabel());
        config.addAttribute("links", links);
    }

    private void replacePatientVariables(Map<String, String> links, String patientUuid, String patientId) {
        for(Map.Entry<String, String> entry: links.entrySet()){
            entry.setValue(entry.getValue().replace("{{patientUuid}}", patientUuid));
            entry.setValue(entry.getValue().replace("{{patientId}}", patientId));
        }
    }

    private void replaceVisitVariables(Map<String, String> links, String visitUuid, String visitId) {
        for(Map.Entry<String, String> entry: links.entrySet()){
            entry.setValue(entry.getValue().replace("{{visitUuid}}", visitUuid));
            entry.setValue(entry.getValue().replace("{{visitId}}", visitId));
        }
    }

}
