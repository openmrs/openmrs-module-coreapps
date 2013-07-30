package org.openmrs.module.coreapps.htmlformentry;

import org.openmrs.api.ConceptService;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.htmlformentry.BadFormDesignException;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.htmlformentry.FormSubmissionController;
import org.openmrs.module.htmlformentry.handler.SubstitutionTagHandler;
import org.openmrs.ui.framework.UiUtils;

import java.util.Map;

/**
 * In enter/edit mode, includes the diagnosis/encounterDiagnoses fragment.
 * Currently this is hardcoded to use specific ids and names, and can only be included once on a form
 */
public class EncounterDiagnosesTagHandler extends SubstitutionTagHandler {

    private EmrApiProperties emrApiProperties;

    private ConceptService conceptService;

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

    @Override
    protected String getSubstitution(FormEntrySession session, FormSubmissionController controller, Map<String, String> attributes) throws BadFormDesignException {
        UiUtils uiUtils = (UiUtils) session.getAttribute("uiUtils");

        EncounterDiagnosesElement element = new EncounterDiagnosesElement();
        element.setUiUtils(uiUtils);
        element.setRequired("true".equals(attributes.get("required")));
        element.setEmrApiProperties(emrApiProperties);
        element.setConceptService(conceptService);
        controller.addAction(element);
        return element.generateHtml(session.getContext());
    }

}
