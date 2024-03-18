package org.openmrs.module.coreapps.page.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.exitfromcare.ExitFromCareService;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
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
                    @SpringBean("emrApiProperties") EmrApiProperties emrApiProperties,
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
        // if the getPatientDied property is configured, the ExitFromCare service will close/reopen patient programs when marking a patient dead/not dead
        pageModel.put("renderProgramWarning", emrApiProperties.getPatientDiedConcept() != null);
        pageModel.put("includesTime", Context.getAdministrationService().getGlobalProperty(CoreAppsConstants.GP_DECEASED_DATE_USING_TIME , "false"));
        pageModel.put("minuteStep", Context.getAdministrationService().getGlobalProperty(CoreAppsConstants.GP_DECEASED_DATE_USING_TIME_MINUTE_STEP, "5"));
        String conceptId = Context.getAdministrationService().getGlobalProperty("concept.causeOfDeath");

        Collection<ConceptAnswer> conceptAnswers = null; 
        if (conceptId != null) {
        	conceptAnswers = getConceptAnswers(conceptId);
        }
        pageModel.put("conceptAnswers", conceptAnswers);
        
        List<Visit> visits = Context.getVisitService().getVisitsByPatient(patient);

        if (!visits.isEmpty()) {
            //Current order of visits returned by getVisitsByPatient() of VisitService has first in list as last visit
            pageModel.put("lastVisitDate", visits.get(0).getStartDatetime());
        } else {
            pageModel.put("lastVisitDate", null);
        }
    }

    public String post(@SpringBean("patientService")
                       PatientService patientService,
                       @SpringBean("exitFromCareService")
                       ExitFromCareService exitFromCareService,
                       @SpringBean("conceptService") ConceptService conceptService,
                       @SpringBean("messageSourceService") MessageSourceService messageSourceService,
                       @RequestParam(value = "causeOfDeath", required = false) String causeOfDeath,
                       @RequestParam(value = "dead", required = false) Boolean dead,
                       @RequestParam(value = "deathDate", required = false) Date deathDate,
                       @RequestParam("patientId") Patient patient, UiUtils ui,
                       @RequestParam(value = "returnUrl", required = false) String returnUrl,  // does this even work, and, if so, how?
                       @RequestParam(value = "returnDashboard", required = false) String returnDashboard,
                       HttpServletRequest request) {
        try {
            Date date = new Date();
            if (dead != null && StringUtils.isNotBlank(causeOfDeath) && deathDate != null && !deathDate.before(patient.getBirthdate()) && !deathDate.after(date)) {
                // TODO should more gracefully handle bad cause of death concept
                exitFromCareService.markPatientDead(patient, conceptService.getConceptByUuid(causeOfDeath), deathDate);
            } else {
                exitFromCareService.markPatientNotDead(patient);
            }

            return "redirect:" + ui.pageLink("coreapps", "clinicianfacing/patient", SimpleObject.create("patientId", patient.getId(), "dashboard", returnDashboard, "returnUrl", returnUrl));
        } catch (APIException e) {
            log.error(e.getMessage(), e);
            request.getSession().setAttribute(org.openmrs.module.uicommons.UiCommonsConstants.SESSION_ATTRIBUTE_ERROR_MESSAGE,
                    messageSourceService.getMessage("Unable to mark patient as deceased: " + e.getMessage(), new Object[]{e.getMessage()}, Context.getLocale()));
            return "redirect:" + ui.pageLink("coreapps", "clinicianfacing/patient", SimpleObject.create("patientId", patient.getId(), "dashboard", returnDashboard, "returnUrl", returnUrl));
        }
    }

    private Collection<ConceptAnswer> getConceptAnswers(String conceptIdOrNameOrUuid) {
        Collection<ConceptAnswer> conceptAnswers = null;
        Concept concept;
        concept = Context.getConceptService().getConcept(conceptIdOrNameOrUuid);
        if (concept == null) {
            concept = Context.getConceptService().getConceptByUuid(conceptIdOrNameOrUuid);
        }
        if (concept != null) {
            conceptAnswers = concept.getAnswers();
        }
        return conceptAnswers;
    }
}

