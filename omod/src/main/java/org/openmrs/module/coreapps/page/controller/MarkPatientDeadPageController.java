package org.openmrs.module.coreapps.page.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.Patient;
import org.openmrs.Visit;
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
import java.util.List;


/**
 * Marking a patient as dead
 */

public class MarkPatientDeadPageController {
    protected final Log log = LogFactory.getLog(this.getClass());

    public void get(@SpringBean PageModel pageModel,
                    @RequestParam(value = "breadcrumbOverride", required = false) String breadcrumbOverride,
                    @SpringBean("patientService") PatientService patientService,
                    @RequestParam("patientId") Patient patient,
                    @RequestParam(value = "defaultDead", required = false) Boolean defaultDead,
                    @RequestParam(value = "defaultDeathDate", required = false) Date defaultDeathDate
    ) {

        pageModel.put("birthDate", patient.getBirthdate());
        pageModel.put("patient", patient);
        pageModel.put("patientId", patient.getId());
        pageModel.put("breadcrumbOverride", breadcrumbOverride);
        pageModel.put("defaultDead", defaultDead);
        pageModel.put("defaultDeathDate", defaultDeathDate);

        String conceptId = Context.getAdministrationService().getGlobalProperty("concept.causeOfDeath");

        if (conceptId != null) {
            pageModel.put("conceptAnswers", getConceptAnswers(conceptId));
        }
        List<Visit> visits = Context.getVisitService().getVisitsByPatient(patient);

        if (visits.size() > 0) {
            //Current order of visits returned by getVisitsByPatient() of VisitService has first in list as last visit
            pageModel.put("lastVisitDate", visits.get(0).getStartDatetime());
        } else {
            pageModel.put("lastVisitDate", null);
        }
    }

    public String post(@SpringBean("patientService")
                       PatientService patientService,
                       @RequestParam(value = "causeOfDeath", required = false) String causeOfDeath,
                       @RequestParam(value = "dead", required = false) Boolean dead,
                       @RequestParam(value = "deathDate", required = false) Date deathDate,
                       @RequestParam("patientId") Patient patient, UiUtils ui,
                       @RequestParam(value = "returnUrl", required = false) String returnUrl,  // does this even work, and, if so, how?
                       @RequestParam(value = "returnDashboard", required = false) String returnDashboard) {
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

            if (patient.isDead() && patient.getDeathDate() != null) {
                closeActiveVisitsAfterDeath(patient);
            }
            return "redirect:" + ui.pageLink("coreapps", "clinicianfacing/patient", SimpleObject.create("patientId", patient.getId(), "dashboard", returnDashboard, "returnUrl", returnUrl));
        } catch (APIException e) {
            log.error(e.getMessage(), e);
            return "redirect:" + ui.pageLink("coreapps", "clinicianfacing/patient", SimpleObject.create("patientId", patient.getId(), "dashboard", returnDashboard, "returnUrl", returnUrl));
        }
    }

    private Collection<ConceptAnswer> getConceptAnswers(String conceptId) {
        Collection<ConceptAnswer> conceptAnswers = null;
        Concept concept;
        try {
            concept = Context.getConceptService().getConcept(Integer.parseInt(conceptId));
        } catch (NumberFormatException exception) {
            concept = Context.getConceptService().getConceptByUuid(conceptId);
        }
        if (concept != null) {
            conceptAnswers = concept.getAnswers();
        }
        return conceptAnswers;
    }

    /**
     * The Method closeActiveVisitsAfterDeath closes all active visits when a patient dies.
     * @param patient
     */
    private void closeActiveVisitsAfterDeath(Patient patient) {
        List<Visit> visitList = Context.getVisitService().getActiveVisitsByPatient(patient);
        if (visitList.size() <= 0) {
            for (Visit visit : visitList) {
                Context.getVisitService().endVisit(visit, patient.getDeathDate());
            }
        }
    }
}

