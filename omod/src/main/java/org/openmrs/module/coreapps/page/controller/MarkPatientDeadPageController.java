package org.openmrs.module.coreapps.page.controller;

import org.apache.commons.lang.BooleanUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.DateUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.Patient;
import org.openmrs.Visit;
import org.openmrs.api.APIException;
import org.openmrs.api.ConceptService;
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
import java.util.Calendar;
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
                    @SpringBean("emrApiProperties") EmrApiProperties emrApiProperties,
                    @RequestParam("patientId") Patient patient,
                    @RequestParam(value = "defaultDead", required = false, defaultValue = "true") Boolean defaultDead,
                    @RequestParam(value = "defaultDeathDate", required = false) Date defaultDeathDate,
                    @RequestParam(value = "returnDashboard", required = false) String returnDashboard,
                    UiUtils ui
    ) {
        pageModel.put("birthDate", patient.getBirthdate());
        pageModel.put("patient", patient);
        pageModel.put("patientId", patient.getId());
        pageModel.put("breadcrumbOverride", breadcrumbOverride);
        pageModel.put("returnUrl", getReturnUrl(ui, patient, returnDashboard));

        boolean deadSelected = BooleanUtils.isTrue(patient.getDead()) || BooleanUtils.isTrue(defaultDead);
        Date deathDate = patient.getDeathDate() == null ? defaultDeathDate : patient.getDeathDate();
        int deathDateHour = 0;
        int deathDateMinute = 0;
        if (deathDate != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(deathDate);
            deathDateHour = cal.get(Calendar.HOUR_OF_DAY);
            deathDateMinute = cal.get(Calendar.MINUTE);
        }
        pageModel.put("deadSelected", deadSelected);
        pageModel.put("deathDate", deathDate);
        pageModel.put("deathDateHour", deathDateHour);
        pageModel.put("deathDateMinute", deathDateMinute);

        // if the getPatientDied property is configured, the ExitFromCare service will close/reopen patient programs when marking a patient dead/not dead
        pageModel.put("renderProgramWarning", emrApiProperties.getPatientDiedConcept() != null);
        pageModel.put("includesTime", Boolean.parseBoolean(getGlobalProperty(CoreAppsConstants.GP_DECEASED_DATE_USING_TIME, "false")));
        pageModel.put("timeWidget", getGlobalProperty(CoreAppsConstants.GP_DECEASED_DATE_TIME_WIDGET, ""));
        pageModel.put("minuteStep", Integer.parseInt(getGlobalProperty(CoreAppsConstants.GP_DECEASED_DATE_USING_TIME_MINUTE_STEP, "5")));
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

    public String post(@SpringBean("exitFromCareService") ExitFromCareService exitFromCareService,
                       @SpringBean("conceptService") ConceptService conceptService,
                       @SpringBean("messageSourceService") MessageSourceService messageSourceService,
                       @RequestParam(value = "causeOfDeath", required = false) String causeOfDeath,
                       @RequestParam(value = "dead", required = false) Boolean dead,
                       @RequestParam(value = "deathDate", required = false) Date deathDate,
                       @RequestParam(value = "deathDateHour", required = false) Integer deathDateHour,
                       @RequestParam(value = "deathDateMinute", required = false) Integer deathDateMinute,
                       @RequestParam("patientId") Patient patient, UiUtils ui,
                       @RequestParam(value = "returnDashboard", required = false) String returnDashboard,
                       HttpServletRequest request) {
        try {
            if (deathDate != null && deathDateHour != null && deathDateMinute != null) {
                deathDate = DateUtils.setHours(deathDate, deathDateHour);
                deathDate = DateUtils.setMinutes(deathDate, deathDateMinute);
            }
            if (BooleanUtils.isTrue(dead)) {
                if (deathDate == null) {
                    throw new APIException("Death date is required");
                }
                Date now = new Date();
                if (deathDate.after(now)) {
                    throw new APIException("Death date cannot be in the future");
                }
                if (deathDate.before(patient.getBirthdate())) {
                    throw new APIException("Death date cannot be prior to the patient's birthdate");
                }
                if (StringUtils.isBlank(causeOfDeath)) {
                    throw new APIException("Cause of death is required");
                }
                Concept causeOfDeathConcept = conceptService.getConceptByUuid(causeOfDeath);
                if (causeOfDeathConcept == null) {
                    throw new APIException("Cause of death is invalid");
                }
                exitFromCareService.markPatientDead(patient, causeOfDeathConcept, deathDate);
            }
            else {
                exitFromCareService.markPatientNotDead(patient);
            }
        }
        catch (APIException e) {
            log.error(e.getMessage(), e);
            request.getSession().setAttribute(org.openmrs.module.uicommons.UiCommonsConstants.SESSION_ATTRIBUTE_ERROR_MESSAGE,
                    messageSourceService.getMessage("Unable to mark patient as deceased: " + e.getMessage(), new Object[]{e.getMessage()}, Context.getLocale()));
        }
        return "redirect:" + getReturnUrl(ui, patient, returnDashboard);
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

    private String getReturnUrl(UiUtils ui, Patient patient, String dashboard) {
        String returnUrl = getGlobalProperty(CoreAppsConstants.GP_DASHBOARD_URL, ui.pageLink("coreapps", "clinicianfacing/patient"));
        returnUrl = ui.contextPath() + (returnUrl.startsWith("/") ? "" : "/") + returnUrl;
        return "/" + ui.urlBind(returnUrl, SimpleObject.create("patientId", patient.getId(), "dashboard", dashboard));
    }

    private String getGlobalProperty(String globalProperty, String defaultValue) {
        return Context.getAdministrationService().getGlobalProperty(globalProperty, defaultValue);
    }
}

