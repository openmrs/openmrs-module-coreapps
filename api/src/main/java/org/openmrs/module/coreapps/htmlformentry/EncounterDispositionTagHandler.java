package org.openmrs.module.coreapps.htmlformentry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.disposition.Disposition;
import org.openmrs.module.emrapi.disposition.DispositionFactory;
import org.openmrs.module.emrapi.disposition.DispositionObs;
import org.openmrs.module.htmlformentry.BadFormDesignException;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.htmlformentry.handler.AbstractTagHandler;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 *
 */
public class EncounterDispositionTagHandler extends AbstractTagHandler {

    private final Log log = LogFactory.getLog(getClass());

    private EmrApiProperties emrApiProperties;

    private DispositionFactory dispositionFactory;

    @Override
    public boolean doStartTag(FormEntrySession session, PrintWriter out, Node parent, Node node) throws BadFormDesignException {

        List<Disposition> dispositions = null;

        try {
            dispositions = dispositionFactory.getDispositions();
        }
        catch (IOException e) {
            throw new RuntimeException("Unable to load dispositions", e);
        }

        Element dispositionObsGroup = node.getOwnerDocument().createElement("obsgroup");
        dispositionObsGroup.setAttribute("groupingConceptId", "org.openmrs.module.emr:Disposition Concept Set");

        // TODO: allow the label text to be overwritten, move the message code  to the coreapps module
        Element label = node.getOwnerDocument().createElement("label");
        Element uimessageLabel = node.getOwnerDocument().createElement("uimessage");
        uimessageLabel.setAttribute("code","emr.consult.disposition");
        label.appendChild(uimessageLabel);
        dispositionObsGroup.appendChild(label);

        // TODO: allow the id to be passed in from the form?
        Element dispositionObs = node.getOwnerDocument().createElement("obs");
        dispositionObs.setAttribute("id", "disposition-" + UUID.randomUUID().toString());
        dispositionObs.setAttribute("style", "dropdown");
        dispositionObs.setAttribute("conceptId", "org.openmrs.module.emr:Disposition");
        if (((Element) node).hasAttribute("required") && ((Element) node).getAttribute("required").equalsIgnoreCase("true")) {
            dispositionObs.setAttribute("required", "true");
        }

        // add the possible dispositions from the configured disposition retrieved from disposition factory
        List<Control> controls = new ArrayList<Control>();

        String answerConceptIds = "";
        String answerCodes = "";
        Iterator<Disposition> i = dispositions.iterator();

        while (i.hasNext()) {
            Disposition disposition = i.next();
            answerConceptIds = answerConceptIds + disposition.getConceptCode() + (i.hasNext() ? "," : "");
            answerCodes = answerCodes + disposition.getName() + (i.hasNext() ? "," : "");

            // determine if there are any additional observations we need to collect for this disposition
            if (disposition.getAdditionalObs() != null && disposition.getAdditionalObs().size() > 0) {
                for (DispositionObs additionalObs : disposition.getAdditionalObs()) {
                    controls.add(buildNewControl(disposition, additionalObs));
                }
            }
        }

        dispositionObs.setAttribute("answerConceptIds", answerConceptIds);
        dispositionObs.setAttribute("answerCodes", answerCodes);

        if (controls != null && controls.size() > 0) {
            generateControlsElement(dispositionObs, controls);
        }

        dispositionObsGroup.appendChild(dispositionObs);

        if (controls != null && controls.size() > 0) {
            generateAdditionalObsElements(dispositionObsGroup, controls);
        }

        node.appendChild(dispositionObsGroup);

        return true;
    }

    @Override
    public void doEndTag(FormEntrySession session, PrintWriter out, Node parent, Node node) throws BadFormDesignException {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    public void setEmrApiProperties(EmrApiProperties emrApiProperties) {
        this.emrApiProperties = emrApiProperties;
    }

    public void setDispositionFactory(DispositionFactory dispositionFactory) {
        this.dispositionFactory = dispositionFactory;
    }

    private Control buildNewControl(Disposition disposition, DispositionObs additionalObs) {
        Control control = new Control();
        control.setWhenValue(disposition.getConceptCode());
        control.setThenDisplay(UUID.randomUUID().toString());
        control.setLabel(additionalObs.getLabel());
        control.setConceptId(additionalObs.getConceptCode());
        control.setParams(additionalObs.getParams());
        return control;
    }

    private void generateControlsElement(Node dispositionObs, List<Control> controls) {

        Element controlsElement = dispositionObs.getOwnerDocument().createElement("controls");

        for (Control control : controls) {
            Element when = controlsElement.getOwnerDocument().createElement("when");
            when.setAttribute("value", control.getWhenValue());
            when.setAttribute("thenDisplay", "#" + control.getThenDisplay());
            controlsElement.appendChild(when);
        }

        dispositionObs.appendChild(controlsElement);
    }

    private void generateAdditionalObsElements(Node dispositionObsGroup, List<Control> controls) {

        for (Control control : controls) {
            Element p = dispositionObsGroup.getOwnerDocument().createElement("p");
            p.setAttribute("id", control.getThenDisplay());
            p.setAttribute("style", "display:none;");

            Element label = dispositionObsGroup.getOwnerDocument().createElement("label");
            Element uimessage = dispositionObsGroup.getOwnerDocument().createElement("uimessage");
            uimessage.setAttribute("code", control.getLabel());
            label.appendChild(uimessage);
            p.appendChild(label);

            Element obs = dispositionObsGroup.getOwnerDocument().createElement("obs");
            obs.setAttribute("conceptId", control.getConceptId());
            if (control.getParams() != null) {
                for (Map.Entry<String,String> param : control.getParams().entrySet()) {
                    obs.setAttribute(param.getKey(), param.getValue());
                }
            }
            p.appendChild(obs);

            dispositionObsGroup.appendChild(p);
        }
    }

    private class Control {

        private String whenValue;

        private String thenDisplay;

        private String conceptId;

        private String label;

        private Map<String, String> params;

        private String getWhenValue() {
            return whenValue;
        }

        private void setWhenValue(String whenValue) {
            this.whenValue = whenValue;
        }

        private String getThenDisplay() {
            return thenDisplay;
        }

        private void setThenDisplay(String thenDisplay) {
            this.thenDisplay = thenDisplay;
        }

        private String getConceptId() {
            return conceptId;
        }

        private void setConceptId(String conceptId) {
            this.conceptId = conceptId;
        }

        private String getLabel() {
            return label;
        }

        private void setLabel(String label) {
            this.label = label;
        }

        private Map<String, String> getParams() {
            return params;
        }

        private void setParams(Map<String, String> params) {
            this.params = params;
        }
    }
}
