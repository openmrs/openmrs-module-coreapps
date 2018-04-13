/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.coreapps.htmlformentry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.ConceptSource;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.disposition.DispositionType;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.module.htmlformentry.FormEntryContext;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.htmlformentry.FormSubmissionActions;
import org.openmrs.module.htmlformentry.FormSubmissionError;
import org.openmrs.module.htmlformentry.InvalidActionException;
import org.openmrs.module.htmlformentry.action.FormSubmissionControllerAction;
import org.openmrs.module.htmlformentry.element.HtmlGeneratorElement;
import org.openmrs.module.htmlformentry.widget.ErrorWidget;
import org.openmrs.module.htmlformentry.widget.HiddenFieldWidget;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageAction;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;

/**
 *
 */
public class EncounterDiagnosesElement implements HtmlGeneratorElement, FormSubmissionControllerAction {

    private boolean required = false;
    private UiUtils uiUtils;
    private String selectedDiagnosesTarget;

    private EmrApiProperties emrApiProperties;

    private ConceptService conceptService;

    private AdtService adtService;

    private DispositionType dispositionTypeForPriorDiagnoses = null;

    // we do not actually use the hiddenDiagnoses widget (the form field name is hardcoded) but we need it to register errorWidget
    private HiddenFieldWidget hiddenDiagnoses;
    private ErrorWidget errorWidget;

    public EncounterDiagnosesElement() {
    }

    @Override
    public String generateHtml(FormEntryContext context) {
        List<Diagnosis> existingDiagnoses = getExistingDiagnoses(context, emrApiProperties.getDiagnosisMetadata());

        if (FormEntryContext.Mode.VIEW == context.getMode()) {
            StringBuilder sb = new StringBuilder();
            if (existingDiagnoses != null) {
                List<ConceptSource> conceptSourcesForDiagnosisSearch = emrApiProperties.getConceptSourcesForDiagnosisSearch();
                for (Diagnosis diagnosis : existingDiagnoses) {
                    sb.append("<p><small>");
                    // question (e.g. "Primary diagnosis")
                    sb.append(message("coreapps.patientDashBoard.diagnosisQuestion." + diagnosis.getOrder()));
                    sb.append("</small><span>");
                    // answer (e.g. "(Confirmed) Malaria [code]")
                    sb.append("(" + message("coreapps.Diagnosis.Certainty." + diagnosis.getCertainty()) + ") ");
                    sb.append(diagnosis.getDiagnosis().formatWithCode(getLocale(), conceptSourcesForDiagnosisSearch));
                    sb.append("</span></p>");
                }
            }
            return sb.toString();

        }
        else {

            hiddenDiagnoses = new HiddenFieldWidget();
            errorWidget = new ErrorWidget();
            context.registerWidget(hiddenDiagnoses);
            context.registerErrorWidget(hiddenDiagnoses, errorWidget);

            try {
                Map<String, Object> fragmentConfig = new HashMap<String, Object>();
                fragmentConfig.put("formFieldName", "encounterDiagnoses");
                fragmentConfig.put("existingDiagnoses", existingDiagnoses);

                // add the prior diagnoses if requested
                if (FormEntryContext.Mode.ENTER == context.getMode() && dispositionTypeForPriorDiagnoses != null) {
                    fragmentConfig.put("priorDiagnoses", getPriorDiagnoses(context, dispositionTypeForPriorDiagnoses));
                }

                try {
                    StringBuilder output = new StringBuilder();
                    output.append(errorWidget.generateHtml(context));
                    output.append(uiUtils.includeFragment("coreapps", "diagnosis/encounterDiagnoses", fragmentConfig));
                    if (selectedDiagnosesTarget != null) {
                        output.append("\n <script type=\"text/javascript\"> \n $(function() { $('#display-encounter-diagnoses-container').appendTo('" + selectedDiagnosesTarget + "'); }); \n </script>");
                    }
                    return output.toString();
                } catch (NullPointerException ex) {
                    // if we are validating/submitting the form, then this method is being called from a fragment action method
                    // and the UiUtils we have access to doesn't have a FragmentIncluder. That's okay, because we don't actually
                    // need to generate the HTML, so we can pass on this exception.
                    // (This is hacky, but I don't see a better way to do it.)
                    return "Submitting the form, so we don't generate HTML";
                }
            }
            catch (PageAction pageAction) {
                throw new IllegalStateException("Included fragment threw a PageAction", pageAction);
            }
        }
    }

    @Override
    public Collection<FormSubmissionError> validateSubmission(FormEntryContext context, HttpServletRequest request) {
        String submitted = request.getParameter("encounterDiagnoses");
        if (StringUtils.isEmpty(submitted) && required) {
            return Collections.singleton(new FormSubmissionError(hiddenDiagnoses, "Required"));
        }

        try {
            List<Diagnosis> diagnoses = parseDiagnoses(submitted, null);
            if (diagnoses.size() == 0 && required) {
                return Collections.singleton(new FormSubmissionError(hiddenDiagnoses, "Required"));
            }
            if (diagnoses.size() > 0) {
                // at least one diagnosis must be primary
                boolean foundPrimary = false;
                for (Diagnosis diagnosis : diagnoses) {
                    if (diagnosis.getOrder().equals(Diagnosis.Order.PRIMARY)) {
                        foundPrimary = true;
                        break;
                    }
                }
                if (!foundPrimary) {
                    return Collections.singleton(new FormSubmissionError(hiddenDiagnoses, message("coreapps.encounterDiagnoses.error.primaryRequired")));
                }
            }
        } catch (IOException e) {
            return Collections.singleton(new FormSubmissionError(hiddenDiagnoses, "Programming Error: invalid json list submitted"));
        }
        return null;
    }

    private List<Diagnosis> parseDiagnoses(String jsonList, Map<Integer, Obs> existingDiagnosisObs) throws IOException {
        // low-priority: refactor this so that a Diagnosis can parse itself via jackson.
        // requires changing org.openmrs.module.emrapi.diagnosis.ConceptCodeDeserializer to also handle parse by id.
        List<Diagnosis> parsed = new ArrayList<Diagnosis>();
        JsonNode list = new ObjectMapper().readTree(jsonList);
        for (JsonNode node : list) {
            CodedOrFreeTextAnswer answer = new CodedOrFreeTextAnswer(node.get("diagnosis").getTextValue(), conceptService);
            Diagnosis.Order diagnosisOrder = Diagnosis.Order.valueOf(node.get("order").getTextValue());
            Diagnosis.Certainty certainty = Diagnosis.Certainty.valueOf(node.get("certainty").getTextValue());
            Obs existingObs = null;
            if (existingDiagnosisObs != null && node.path("existingObs").getNumberValue() != null) {
                existingObs = existingDiagnosisObs.get(node.get("existingObs").getNumberValue());
            }

            Diagnosis diagnosis = new Diagnosis(answer, diagnosisOrder);
            diagnosis.setCertainty(certainty);
            diagnosis.setExistingObs(existingObs);
            parsed.add(diagnosis);
        }
        return parsed;
    }

    @Override
    public void handleSubmission(FormEntrySession formEntrySession, HttpServletRequest request) {
        DiagnosisMetadata diagnosisMetadata = emrApiProperties.getDiagnosisMetadata();
        String submitted = request.getParameter("encounterDiagnoses");

        // if we are in edit mode, we need to map the submitted diagnoses to their existing obs
        Map<Integer, Obs> existingDiagnosisObs = getExistingDiagnosisObs(formEntrySession.getContext(), diagnosisMetadata);

        FormSubmissionActions submissionActions = formEntrySession.getSubmissionActions();
        try {
            Set<Integer> resubmittedObs = new HashSet<Integer>(); // we need to void any existing that isn't resubmitted

            List<Diagnosis> diagnoses = parseDiagnoses(submitted, existingDiagnosisObs);
            for (Diagnosis diagnosis : diagnoses) {
                if (diagnosis.getExistingObs() != null) {
                    resubmittedObs.add(diagnosis.getExistingObs().getId());
                }
                Obs obsGroup = diagnosisMetadata.buildDiagnosisObsGroup(diagnosis);
                createObsGroup(submissionActions, obsGroup);
            }

            if (formEntrySession.getContext().getMode().equals(FormEntryContext.Mode.EDIT)) {
                // void any diagnosis that wasn't resubmitted
                Collection<Integer> obsToVoid = CollectionUtils.subtract(existingDiagnosisObs.keySet(), resubmittedObs);
                for (Integer obsId : obsToVoid) {
                    submissionActions.modifyObs(existingDiagnosisObs.get(obsId), null, null, null, null, null);
                }
            }
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
        catch (InvalidActionException e) {
            throw new IllegalStateException(e);
        }
    }

    private Map<Integer, Obs> getExistingDiagnosisObs(FormEntryContext context, DiagnosisMetadata diagnosisMetadata) {
        Map<Integer, Obs> existingDiagnosisObs = null;
        FormEntryContext.Mode mode = context.getMode();
        if (mode == FormEntryContext.Mode.EDIT || mode == FormEntryContext.Mode.VIEW) {
            existingDiagnosisObs = new HashMap<Integer, Obs>();
            Encounter encounter = context.getExistingEncounter();
            if (encounter == null) {
                // this situation happens during unit tests when viewing the form with a Person. (I don't know if this a
                // real use case though.
                return null;
            }
            for (Obs candidate : encounter.getObsAtTopLevel(false)) {
                if (diagnosisMetadata.isDiagnosis(candidate)) {
                    existingDiagnosisObs.put(candidate.getObsId(), candidate);
                }
            }

            // remove any diagnoses found from existingObsInGroups
            // TODO do we need to remove from existingObs as well?
            for (Obs existingDiagnosis : existingDiagnosisObs.values()) {
                context.getExistingObsInGroups().remove(existingDiagnosis);
            }

        }
        return existingDiagnosisObs;
    }

    /**
     * only visible for testing
     * @param context
     * @param diagnosisMetadata
     * @return
     */
    List<Diagnosis> getExistingDiagnoses(FormEntryContext context, DiagnosisMetadata diagnosisMetadata) {
        List<Diagnosis> diagnoses = new ArrayList<Diagnosis>();
        Map<Integer, Obs> existing = getExistingDiagnosisObs(context, diagnosisMetadata);
        if (existing != null) {
            for (Obs group : existing.values()) {
                diagnoses.add(diagnosisMetadata.toDiagnosis(group));
            }
        }
        Collections.sort(diagnoses, new Comparator<Diagnosis>() {
            @Override
            public int compare(Diagnosis left, Diagnosis right) {
                return left.getOrder().compareTo(right.getOrder());
            }
        });
        return diagnoses;
    }

    private List<Diagnosis> getPriorDiagnoses(FormEntryContext context, DispositionType dispositionType) {
        List<Diagnosis> diagnoses = new ArrayList<Diagnosis>();
        if (context.getVisit() != null) {

            VisitDomainWrapper visitDomainWrapper;

            if (context.getVisit() instanceof Visit) {
                visitDomainWrapper = adtService.wrap((Visit) context.getVisit());
            }
            else {
                visitDomainWrapper = (VisitDomainWrapper) context.getVisit();
            }

            diagnoses = visitDomainWrapper.getDiagnosesFromMostRecentDispositionByType(dispositionType);
        }

        return diagnoses;
    }

    private void createObsGroup(FormSubmissionActions actions, Obs obsGroup) throws InvalidActionException {
        actions.beginObsGroup(obsGroup);
        actions.endObsGroup();
    }

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean getRequired() {
        return required;
    }

    public void setUiUtils(UiUtils uiUtils) {
        this.uiUtils = uiUtils;
    }

    public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }

    public void setConceptService(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    public void setAdtService(AdtService adtService) {
        this.adtService = adtService;
    }

    public void setDispositionTypeForPriorDiagnoses(DispositionType dispositionTypeForPriorDiagnoses) {
        this.dispositionTypeForPriorDiagnoses = dispositionTypeForPriorDiagnoses;
    }

    public DispositionType getDispositionTypeForPriorDiagnoses() {
        return dispositionTypeForPriorDiagnoses;
    }

    public void setSelectedDiagnosesTarget(String selectedDiagnosesTarget) {
        this.selectedDiagnosesTarget = selectedDiagnosesTarget;
    }

    public String getSelectedDiagnosesTarget() {
        return selectedDiagnosesTarget;
    }

    /**
     * In case you are viewing a form with this element on it from the legacy UI, don't use UiUtils to format
     * @param code
     * @return
     */
    String message(String code) {
        if (uiUtils != null) {
            return uiUtils.message(code);
        }
        else {
            return Context.getMessageSourceService().getMessage(code);
        }
    }

    private Locale getLocale() {
        if (uiUtils != null) {
            return uiUtils.getLocale();
        }
        else {
            return Context.getLocale();
        }
    }
}
