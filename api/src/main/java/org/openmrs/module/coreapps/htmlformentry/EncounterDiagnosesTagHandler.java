package org.openmrs.module.coreapps.htmlformentry;

import java.util.Map;

import org.openmrs.api.ConceptService;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.disposition.DispositionType;
import org.openmrs.module.htmlformentry.BadFormDesignException;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.htmlformentry.FormSubmissionController;
import org.openmrs.module.htmlformentry.handler.SubstitutionTagHandler;
import org.openmrs.ui.framework.UiUtils;

/**
 * In enter/edit mode, includes the diagnosis/encounterDiagnoses fragment.
 * Currently this is hardcoded to use specific ids and names, and can only be included once on a form
 */
public class EncounterDiagnosesTagHandler extends SubstitutionTagHandler {

    private EmrApiProperties emrApiProperties;

    private ConceptService conceptService;

    private AdtService adtService;


    public EmrApiProperties getEmrApiProperties() {
        return emrApiProperties;
    }

    public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }

    public ConceptService getConceptService() {
        return conceptService;
    }

    public void setConceptService(ConceptService conceptService) {
        this.conceptService = conceptService;
    }

    public AdtService getAdtService() {
        return adtService;
    }

    public void setAdtService(AdtService adtService) {
        this.adtService = adtService;
    }

    @Override
    protected String getSubstitution(FormEntrySession session, FormSubmissionController controller, Map<String, String> attributes) throws BadFormDesignException {
        UiUtils uiUtils = (UiUtils) session.getAttribute("uiUtils");

        EncounterDiagnosesElement element = new EncounterDiagnosesElement();
        element.setUiUtils(uiUtils);
        element.setRequired("true".equals(attributes.get("required")));
        element.setSelectedDiagnosesTarget(attributes.get("selectedDiagnosesTarget"));
        element.setEmrApiProperties(emrApiProperties);
        element.setConceptService(conceptService);
        element.setAdtService(adtService);

        // handle the attribute to specify loading a prior diagnoses
        if (attributes.containsKey(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_INCLUDE_PRIOR_DIAGNOSES_ATTRIBUTE_NAME)) {
            try {
                element.setDispositionTypeForPriorDiagnoses(DispositionType.valueOf(
                        attributes.get(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_INCLUDE_PRIOR_DIAGNOSES_ATTRIBUTE_NAME).toUpperCase()));
            }
            catch (IllegalArgumentException ex) {
                throw new BadFormDesignException("Encounter Diagnoses Tag: invalid value of "
                        + attributes.get(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_INCLUDE_PRIOR_DIAGNOSES_ATTRIBUTE_NAME)
                        + " for attribute " + CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_INCLUDE_PRIOR_DIAGNOSES_ATTRIBUTE_NAME);
            }
        }

        controller.addAction(element);
        return element.generateHtml(session.getContext());
    }

}
