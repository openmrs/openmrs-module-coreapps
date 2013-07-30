package org.openmrs.module.coreapps.htmlformentry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
import org.openmrs.module.htmlformentry.BadFormDesignException;
import org.openmrs.module.htmlformentry.FormEntryContext;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.htmlformentry.FormSubmissionController;
import org.openmrs.module.htmlformentry.action.ObsGroupAction;
import org.openmrs.module.htmlformentry.element.ObsSubmissionElement;
import org.openmrs.module.htmlformentry.handler.SubstitutionTagHandler;
import org.openmrs.ui.framework.UiUtils;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class EncounterDispositionTagHandler extends SubstitutionTagHandler {

    private final Log log = LogFactory.getLog(getClass());

    private EmrApiProperties emrApiProperties;

    @Override
    protected String getSubstitution(FormEntrySession session, FormSubmissionController controller, Map<String, String> attrs) throws BadFormDesignException {
        DispositionDescriptor dispositionDescriptor = emrApiProperties.getDispositionDescriptor();
        UiUtils uiUtils = (UiUtils) session.getAttribute("uiUtils");

        FormEntryContext.Mode mode = session.getContext().getMode();
        if (mode == FormEntryContext.Mode.VIEW) {
            Encounter encounter = session.getEncounter();
            return toDisplayHtml(dispositionDescriptor, uiUtils, encounter);
        }
        else if (mode == FormEntryContext.Mode.ENTER) {
            // implemented so far: allow entry of a disposition in its obs group, but without any actions

            Concept dispositionConstruct = dispositionDescriptor.getDispositionSetConcept();

            session.getContext().beginObsGroup(dispositionConstruct, null, null);
            session.getSubmissionController().addAction(ObsGroupAction.start(dispositionConstruct, null, null));

            Map<String, String> obsAttrs = new HashMap<String, String>();
            obsAttrs.put("conceptId", "org.openmrs.module.emr:Disposition");
            ObsSubmissionElement element = new ObsSubmissionElement(session.getContext(), obsAttrs);
            session.getSubmissionController().addAction(element);
            String html = element.generateHtml(session.getContext());

            session.getContext().endObsGroup();
            session.getSubmissionController().addAction(ObsGroupAction.end());

            return "<p>\n<label>" + uiUtils.message("emr.consult.disposition") + "</label>\n" + html + "\n</p>";
        }
        else { // EDIT
            // for now, just display
            Encounter encounter = session.getEncounter();
            return toDisplayHtml(dispositionDescriptor, uiUtils, encounter);
        }
    }

    private String toDisplayHtml(DispositionDescriptor dispositionDescriptor, UiUtils uiUtils, Encounter encounter) {
        if (encounter == null) {
            return "";
        }

        Obs group = null;
        for (Obs candidate : encounter.getObsAtTopLevel(false)) {
            if (dispositionDescriptor.isDisposition(candidate)) {
                group = candidate;
                break;
            }
        }
        StringBuilder html = new StringBuilder();
        if (group != null) {
            Obs disposition = dispositionDescriptor.getDispositionObs(group);
            if (disposition == null) {
                log.warn("Obs group " + disposition.getId() + " is a disposition obs group but has no disposition specified");
                return "";
            }
            html.append("<p><small>")
                    .append(uiUtils.message("emr.consult.disposition"))
                    .append("</small><span>")
                    .append(uiUtils.format(disposition.getValueCoded()))
                    .append("</span></p>");

            for (Obs obs : dispositionDescriptor.getAdditionalObs(group)) {
                html.append("\n<p><small>")
                        .append(uiUtils.format(obs.getConcept()))
                        .append("</small><span>")
                        .append(obs.getValueAsString(Context.getLocale()))
                        .append("</span></p>");
            }
        }
        return html.toString();
    }

    public EmrApiProperties getEmrApiProperties() {
        return emrApiProperties;
    }

    public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }
}
