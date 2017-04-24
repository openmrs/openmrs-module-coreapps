
package org.openmrs.module.coreapps.page.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.Patient;
import org.openmrs.api.APIException;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collection;
import java.util.Date;


/**
 * Marking a patient as dead
 */

public class MarkPatientDeadPageController {
    protected final Log log = LogFactory.getLog(this.getClass());

    public void get(@SpringBean PageModel pageModel, @RequestParam(value = "breadcrumbOverride", required = false) String breadcrumbOverride, @SpringBean("patientService") PatientService patientService, @RequestParam("patientId") String patientId) {
        String conceptId = Context.getAdministrationService().getGlobalProperty("concept.causeOfDeath");

        Patient patient = patientService.getPatientByUuid(patientId);
        pageModel.put("birthDate", patient.getBirthdate());
        pageModel.put("patient", patient);
        pageModel.put("patientId", patientId);
        pageModel.put("breadcrumbOverride", breadcrumbOverride);
        if (conceptId != null && !conceptId.contains("[a-zA-Z]+")) {
            pageModel.put("conceptAnswers", getConceptAnswers(Integer.parseInt(conceptId)));
        }
    }

    public String post(@SpringBean("patientService") PatientService patientService, @RequestParam(value = "causeOfDeath", required = false) String causeOfDeath, @RequestParam(value = "dead", required = false) Boolean dead, @RequestParam(value = "deathDate", required = false) Date deathDate, @RequestParam("patientId") String patientId, UiUtils ui, @RequestParam(value = "returnUrl", required = false) String returnUrl) {
        Patient patient = patientService.getPatientByUuid(patientId);
        try {
            Date date = new Date();
            if (dead != null && StringUtils.isNotBlank(causeOfDeath) && deathDate != null && !deathDate.before(patient.getBirthdate()) && !deathDate.after(date)) {
                patient.setDead(dead);
                patient.setCauseOfDeath(Context.getConceptService().getConceptByUuid(causeOfDeath));
                patient.setDeathDate(deathDate);
            } else {
                patient.setDeathDate(null);
                patient.setDead(false);
                patient.setCauseOfDeath(null);
            }

            patientService.savePatient(patient);
            return "redirect:" + ui.pageLink("coreapps", "clinicianfacing/patient", SimpleObject.create("patientId", patient.getId(), "returnUrl", returnUrl));
        } catch (APIException e) {
            log.error(e.getMessage(), e);
            return "redirect:" + ui.pageLink("coreapps", "markPatientDead", SimpleObject.create("patientId", patient.getId(), "returnUrl", returnUrl));
        }
    }

    private Collection<ConceptAnswer> getConceptAnswers(Integer conceptId) {
        Collection<ConceptAnswer> conceptAnswers = null;
        Concept concept = Context.getConceptService().getConcept(conceptId);
        if (concept != null) {
            conceptAnswers = concept.getAnswers();
        }
        return conceptAnswers;
    }
}

