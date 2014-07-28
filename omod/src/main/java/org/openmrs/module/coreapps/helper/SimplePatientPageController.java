package org.openmrs.module.coreapps.helper;

import org.openmrs.Patient;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.Redirect;
import org.springframework.web.bind.annotation.RequestParam;

public class SimplePatientPageController {

    /**
     * Simple controller, sufficient for patient pages that will do all their work client-side and/or do not need
     * data beyond what is directly on PatientDomainWrapper.
     *
     * Exposes a model with:
     *  * "patient" as a PatientDomainWrapper, based on the "patientId" parameter
     *  * "app" as an AppDescriptor, based on the optional "app" parameter
     *
     * @param patient
     * @param patientDomainWrapper
     * @param app
     * @param model
     * @return
     */
    public Redirect get(@RequestParam("patientId") Patient patient,
                        @InjectBeans PatientDomainWrapper patientDomainWrapper,
                        @RequestParam(required = false, value = "app") AppDescriptor app,
                        PageModel model) {

        if (patient.isVoided() || patient.isPersonVoided()) {
            return new Redirect("coreapps", "patientdashboard/deletedPatient", "patientId=" + patient.getId());
        }

        patientDomainWrapper.setPatient(patient);
        model.addAttribute("app", app);
        model.addAttribute("patient", patientDomainWrapper);

        return null;
    }

}
