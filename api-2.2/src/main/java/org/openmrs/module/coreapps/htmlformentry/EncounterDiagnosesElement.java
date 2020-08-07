
/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */

package org.openmrs.module.coreapps.htmlformentry;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.StringTokenizer;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonProcessingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.CodedOrFreeText;
import org.openmrs.Concept;
import org.openmrs.ConceptSource;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.disposition.DispositionType;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.module.htmlformentry.CustomFormSubmissionAction;
import org.openmrs.module.htmlformentry.FormEntryContext;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.htmlformentry.FormSubmissionError;
import org.openmrs.module.htmlformentry.HtmlFormEntryUtil;
import org.openmrs.module.htmlformentry.action.FormSubmissionControllerAction;
import org.openmrs.module.htmlformentry.element.HtmlGeneratorElement;
import org.openmrs.module.htmlformentry.widget.ErrorWidget;
import org.openmrs.module.htmlformentry.widget.HiddenFieldWidget;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageAction;

public class EncounterDiagnosesElement implements HtmlGeneratorElement, FormSubmissionControllerAction, CustomFormSubmissionAction {

    private boolean required = false;
    private UiUtils uiUtils;
    private String selectedDiagnosesTarget;

    private String diagnosisSets;

    private String diagnosisConceptSources;

    private String preferredCodingSource;
    
    private String diagnosisConceptClasses;

    private EmrApiProperties emrApiProperties;

    private ConceptService conceptService;

    private AdtService adtService;

    private DispositionType dispositionTypeForPriorDiagnoses = null;

    // we do not actually use the hiddenDiagnoses widget (the form field name is hardcoded) but we need it to register errorWidget
    private HiddenFieldWidget hiddenDiagnoses;
    private ErrorWidget errorWidget;

    private static final Integer DIAGNOSIS_RANK_PRIMARY = 1;
    private static final Integer DIAGNOSIS_RANK_SECONDARY = 2;

    public EncounterDiagnosesElement() {
    }

    /**
     * Method to convert the core diagnosis object into a list of diagnoses compatible with the diagnosis object in the emrapi module
     * @return diagnoses
     * */
    private List<Diagnosis> convert(List<org.openmrs.Diagnosis> coreDiagnoses) {
        List<Diagnosis> diagnoses = new ArrayList<Diagnosis>();
        for (org.openmrs.Diagnosis coreDiagnosis : coreDiagnoses) {
            Diagnosis diagnosis = new Diagnosis();
            CodedOrFreeText coded = coreDiagnosis.getDiagnosis();
            diagnosis.setDiagnosis(new CodedOrFreeTextAnswer(coded.getCoded(), coded.getSpecificName(), coded.getNonCoded()));
            diagnosis.setCertainty(coreDiagnosis.getCertainty() == ConditionVerificationStatus.CONFIRMED ? Diagnosis.Certainty.CONFIRMED : Diagnosis.Certainty.PRESUMED);
            diagnosis.setOrder(coreDiagnosis.getRank() == DIAGNOSIS_RANK_PRIMARY ? Diagnosis.Order.PRIMARY : Diagnosis.Order.SECONDARY);
            diagnosis.setExistingDiagnosis(coreDiagnosis.getDiagnosisId());
            diagnoses.add(diagnosis);
        }
        return diagnoses;
    }

    @Override
    public String generateHtml(FormEntryContext context) {
        List<Diagnosis> existingDiagnoses = convert(new ArrayList<org.openmrs.Diagnosis>(getExistingDiagnoses(context)));
        
        if (FormEntryContext.Mode.VIEW == context.getMode()) {
            StringBuilder sb = new StringBuilder();
            if (existingDiagnoses != null) {
            	List<ConceptSource> conceptSourcesForDiagnosisSearch;
            	if (preferredCodingSource != null) {
            		ConceptSource preferredSource = Context.getConceptService().getConceptSourceByName(preferredCodingSource);
            		conceptSourcesForDiagnosisSearch = Arrays.asList(preferredSource);
            	} 
            	else {
            		conceptSourcesForDiagnosisSearch = emrApiProperties.getConceptSourcesForDiagnosisSearch();
            	}
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
                // Parse '0' to config attribute if specified such that null value can be used during the search in 'DiagnosesFragmentController' class  
                fragmentConfig.put("diagnosisSets", "0".equals(diagnosisSets) ? "0" : validateAndFormat(diagnosisSets));
                
                fragmentConfig.put("preferredCodingSource", preferredCodingSource);
                fragmentConfig.put("diagnosisConceptSources", StringUtils.deleteWhitespace(diagnosisConceptSources));
                fragmentConfig.put("diagnosisConceptClasses", StringUtils.deleteWhitespace(diagnosisConceptClasses));

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
    
    /**
     * This method receives a comma-separated list of diagnosis sets 'identifiers'
     * and returns as comma-separated list of their uuids.
     *
     * @param diagnosisSetIds Either concepts UUIDS, mappings or internal IDs.
     * @return The list of diagnoses sets as a list of their concept UUIDs.
     * @throws IllegalArgumentException if one or more sets cannot be fetched from the database.
     */
    private String validateAndFormat(String diagnosisSetIds) {
        if (diagnosisSetIds == null) {
            return null;
        }
        List<Concept> concepts = new ArrayList<Concept>();
        for (StringTokenizer st = new StringTokenizer(diagnosisSetIds, ","); st.hasMoreTokens();) {
            String id = st.nextToken().trim();
            Concept concept = HtmlFormEntryUtil.getConcept(id);
            if (concept == null) {
                throw new IllegalArgumentException("Cannot find diagnosis set for value '" + id
                        + "' in diagnosisSets attribute value. Parameters: " + diagnosisSetIds);
            }
            concepts.add(concept);
        }
        StringBuilder sb = new StringBuilder("");
        if (CollectionUtils.isNotEmpty(concepts)) {
            for (Concept con : concepts) {
                sb.append(con.getUuid() + ",");
            }
        }
        String uuids = sb.toString();

        return StringUtils.removeEnd(uuids, ",");
    }

    @Override
    public Collection<FormSubmissionError> validateSubmission(FormEntryContext context, HttpServletRequest request) {
        String submitted = request.getParameter("encounterDiagnoses");
        if (StringUtils.isEmpty(submitted) && required) {
            return Collections.singleton(new FormSubmissionError(hiddenDiagnoses, "Required"));
        }

        try {

            JsonNode submittedList = new ObjectMapper().readTree(submitted);

            List<Diagnosis> diagnoses = parseDiagnoses(submittedList, null);
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

    private List<Diagnosis> parseDiagnoses(JsonNode list, Map<Integer, Obs> existingDiagnosisObs) throws IOException {
        // low-priority: refactor this so that a Diagnosis can parse itself via jackson.
        // requires changing org.openmrs.module.emrapi.diagnosis.ConceptCodeDeserializer to also handle parse by id.
        List<Diagnosis> parsed = new ArrayList<Diagnosis>();

        for (JsonNode node : list) {
            CodedOrFreeTextAnswer answer = new CodedOrFreeTextAnswer(node.get("diagnosis").getTextValue(), conceptService);
            Diagnosis.Order diagnosisOrder = Diagnosis.Order.valueOf(node.get("order").getTextValue());
            Diagnosis.Certainty certainty = Diagnosis.Certainty.valueOf(node.get("certainty").getTextValue());
            Obs existingObs = null;
            Integer existingDiagnosis = null;

            if (node.path("existingDiagnosis").getNumberValue() != null) {
                existingDiagnosis = node.get("existingDiagnosis").getIntValue();
            }
            if (existingDiagnosisObs != null && node.path("existingObs").getNumberValue() != null) {
                existingObs = existingDiagnosisObs.get(node.get("existingObs").getNumberValue());
            }

            Diagnosis diagnosis = new Diagnosis(answer, diagnosisOrder);
            diagnosis.setCertainty(certainty);
            diagnosis.setExistingObs(existingObs);
            diagnosis.setExistingDiagnosis(existingDiagnosis);
            parsed.add(diagnosis);
        }
        return parsed;
    }

    @Override
    public void handleSubmission(FormEntrySession formEntrySession, HttpServletRequest request) {
    	// Register this as CustomFormSubmissionAction handler, to be used at the post submission 
    	// because of https://issues.openmrs.org/browse/RA-1705
    	// refer to EncounterDiagnosesElement#applyAction(FormEntrySession formEntrySession)
    	formEntrySession.getSubmissionActions().addCustomFormSubmissionAction(this);
    }
    
    
    /**
     * Since 1.27.0
     * 
     * @param formEntrySession provides the saved encounter and submitted parameters details
     */
    @Override
	public void applyAction(FormEntrySession formEntrySession) {
    	
    	HttpServletRequest request = formEntrySession.getSubmissionController().getLastSubmission();
    	try {
            String jsonList = request.getParameter("encounterDiagnoses");

            JsonNode list = new ObjectMapper().readTree(jsonList);

            Set<org.openmrs.Diagnosis> existingDiagnoses = getExistingDiagnoses(formEntrySession.getContext());
            Set<org.openmrs.Diagnosis> resubmittedDiagnoses = new HashSet<org.openmrs.Diagnosis>();

            for (JsonNode node : list) {
                CodedOrFreeTextAnswer answer = new CodedOrFreeTextAnswer(node.get("diagnosis").getTextValue(), conceptService);
                Diagnosis.Order diagnosisOrder = Diagnosis.Order.valueOf(node.get("order").getTextValue());
                Diagnosis.Certainty certainty = Diagnosis.Certainty.valueOf(node.get("certainty").getTextValue());
                Integer existingDiagnosis = null;

                if (node.path("existingObs").getNumberValue() != null) {
                    JsonNode nd = node.get("existingObs");
                    System.out.println(nd);
                }

                if (node.path("existingDiagnosis").getNumberValue() != null) {
                    existingDiagnosis = node.get("existingDiagnosis").getIntValue();
                }

                org.openmrs.Diagnosis diagnosis;

                Integer rank = diagnosisOrder == Diagnosis.Order.PRIMARY ? DIAGNOSIS_RANK_PRIMARY : DIAGNOSIS_RANK_SECONDARY;
                ConditionVerificationStatus certaintyStatus = certainty == Diagnosis.Certainty.CONFIRMED ? ConditionVerificationStatus.CONFIRMED : ConditionVerificationStatus.PROVISIONAL;

                if(existingDiagnosis !=null){

                    diagnosis = Context.getDiagnosisService().getDiagnosis(existingDiagnosis);
                    resubmittedDiagnoses.add(diagnosis);

                    if(!diagnosis.getRank().equals(rank) || !diagnosis.getCertainty().equals(certaintyStatus)){
                        diagnosis.setRank(rank);
                        diagnosis.setCertainty(certaintyStatus);
                        diagnosis.setDateChanged(new Date());
                        diagnosis.setChangedBy(Context.getAuthenticatedUser());
                        Context.getDiagnosisService().save(diagnosis);
                    }

                }else{
                    diagnosis = new org.openmrs.Diagnosis();
                    diagnosis.setDiagnosis(new CodedOrFreeText(answer.getCodedAnswer(), answer.getSpecificCodedAnswer(), answer.getNonCodedAnswer()));
                    diagnosis.setEncounter(formEntrySession.getEncounter());
                    diagnosis.setCertainty(certaintyStatus);
                    diagnosis.setRank(rank);
                    diagnosis.setPatient(formEntrySession.getPatient());

                    if (diagnosis.getEncounter().getEncounterType() == null) {
                        diagnosis.getEncounter().setEncounterType(formEntrySession.getForm().getEncounterType());
                    }
                    Context.getDiagnosisService().save(diagnosis);
                }

            }

            // Remove Diagnoses that were not resubmitted
            Collection<org.openmrs.Diagnosis> diagnosesToVoid = CollectionUtils
                    .subtract(existingDiagnoses, resubmittedDiagnoses);

            for (org.openmrs.Diagnosis diagnosisToVoid : diagnosesToVoid) {
                Context.getDiagnosisService().voidDiagnosis(diagnosisToVoid, "Deleted Diagnosis");
            }
        }
        catch (JsonProcessingException ex) {
            ex.printStackTrace();
        }
        catch (IOException ex) {
            ex.printStackTrace();
        }
	}

    /**
     * Gets existing Diagnoses within the current Encounter
     * 
     * @param context
     * @return
     */
    public Set<org.openmrs.Diagnosis> getExistingDiagnoses(FormEntryContext context) {
        FormEntryContext.Mode mode = context.getMode();
        if (mode == FormEntryContext.Mode.EDIT || mode == FormEntryContext.Mode.VIEW) {
            Encounter existingEncounter = context.getExistingEncounter();
            if (existingEncounter != null) {
                Set<org.openmrs.Diagnosis> nonVoidedDiagnoses = new HashSet<org.openmrs.Diagnosis>();
                for (org.openmrs.Diagnosis diagnosis : existingEncounter.getDiagnoses()) {
                    if (!diagnosis.getVoided()) {
                        nonVoidedDiagnoses.add(diagnosis);
                    }
                }
                return nonVoidedDiagnoses;
            }
        }

        return new HashSet<org.openmrs.Diagnosis>();
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

    public void setRequired(boolean required) {
        this.required = required;
    }

    public boolean getRequired() {
        return required;
    }

    public void setDiagnosisSets(String diagnosisSets) {
        this.diagnosisSets = diagnosisSets;
    }

    public String getDiagnosisSets() {
        return diagnosisSets;
    }

    public void setDiagnosisConceptSources(String diagnosisConceptSources) {
        this.diagnosisConceptSources = diagnosisConceptSources;
    }

    public String getDiagnosisConceptSources() {
        return diagnosisConceptSources;
    }

    public void setPreferredCodingSource(String preferredCodingSource) {
        this.preferredCodingSource = preferredCodingSource;
    }

    public String getPreferredCodingSource() {
        return preferredCodingSource;
    }
    
    public void setDiagnosisConceptClasses(String diagnosisConceptClasses) {
        this.diagnosisConceptClasses = diagnosisConceptClasses;
    }

    public String getDiagnosisConceptClasses() {
        return diagnosisConceptClasses;
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
     * In case you are viewing a form with this elementon it from the legacy UI, don't use UiUtils to format
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
