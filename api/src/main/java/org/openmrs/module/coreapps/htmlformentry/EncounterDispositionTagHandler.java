package org.openmrs.module.coreapps.htmlformentry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.Transformer;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Visit;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.disposition.Disposition;
import org.openmrs.module.emrapi.disposition.DispositionObs;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.module.htmlformentry.BadFormDesignException;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.htmlformentry.HtmlFormEntryUtil;
import org.openmrs.module.htmlformentry.handler.AbstractTagHandler;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 *
 */
public class EncounterDispositionTagHandler extends AbstractTagHandler {

    private final Log log = LogFactory.getLog(getClass());

    private static final Transformer PREFIX_WITH_HASH = new Transformer() {

        @Override
        public Object transform(Object o) {
            return "#" + (String) o;
        }

    };

    private EmrApiProperties emrApiProperties;

    private DispositionService dispositionService;

    private AdtService adtService;

    @Override
    public boolean doStartTag(FormEntrySession session, PrintWriter out, Node parent, Node node) throws BadFormDesignException {

        Set<Obs> exisitingDispositionObsGroup = null;
        if (session.getContext().getExistingEncounter() != null && session.getContext().getExistingObsInGroups() != null) {
            exisitingDispositionObsGroup = getObsGroupByGroupingConcept(session.getContext().getExistingObsInGroups(),
                    HtmlFormEntryUtil.getConcept(EmrApiConstants.EMR_CONCEPT_SOURCE_NAME + ": " + EmrApiConstants.CONCEPT_CODE_DISPOSITION_CONCEPT_SET));
        }

        List<Disposition> dispositions = null;

        VisitDomainWrapper visitDomainWrapper = session.getContext().getVisit() != null
                ? adtService.wrap((Visit) session.getContext().getVisit()) : null;

        if (visitDomainWrapper == null) {
            dispositions = dispositionService.getDispositions();
        }
        else {
            dispositions = dispositionService.getValidDispositions(visitDomainWrapper);
        }

        Element dispositionObsGroup = node.getOwnerDocument().createElement("obsgroup");
        dispositionObsGroup.setAttribute("groupingConceptId", emrApiProperties.getEmrApiConceptSource().getName() + ":"
                + EmrApiConstants.CONCEPT_CODE_DISPOSITION_CONCEPT_SET);

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
        dispositionObs.setAttribute("conceptId",  emrApiProperties.getEmrApiConceptSource().getName() + ":"
                + EmrApiConstants.CONCEPT_CODE_DISPOSITION);
        if (((Element) node).hasAttribute("required") && ((Element) node).getAttribute("required").equalsIgnoreCase("true")) {
            dispositionObs.setAttribute("required", "true");
        }

        // add the possible dispositions from the configured disposition retrieved from disposition factory
        List<Control> controls = new ArrayList<Control>();

        String answerConceptIds = "";
        StringBuilder answerCodes = new StringBuilder();
        Iterator<Disposition> i = dispositions.iterator();

        while (i.hasNext()) {
            Disposition disposition = i.next();

            answerConceptIds = answerConceptIds + disposition.getConceptCode() + (i.hasNext() ? "," : "");
            answerCodes.append(disposition.getName());
            if (i.hasNext()) {
                answerCodes.append(",");
            }

            // determine if there are any additional observations we need to collect for this disposition
            if (disposition.getAdditionalObs() != null && disposition.getAdditionalObs().size() > 0) {
                controls.add(buildNewControl(disposition, disposition.getAdditionalObs()));
            }
        }

        dispositionObs.setAttribute("answerConceptIds", answerConceptIds);
        dispositionObs.setAttribute("answerCodes", answerCodes.toString());

        if (controls != null && controls.size() > 0) {
            generateControlsElement(dispositionObs, controls);
        }

        dispositionObsGroup.appendChild(dispositionObs);

        if (controls != null && controls.size() > 0) {
            generateAdditionalObsElements(dispositionObsGroup, controls, exisitingDispositionObsGroup);
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

    public void setDispositionService(DispositionService dispositionService) {
        this.dispositionService = dispositionService;
    }

    public void setAdtService(AdtService adtService) {
        this.adtService = adtService;
    }

    private Control buildNewControl(Disposition disposition, List<DispositionObs> additionalObs) {
        Control control = new Control();
        control.setWhenValue(disposition.getConceptCode());

        for (DispositionObs obs : additionalObs) {
            control.getThenDisplay().add(UUID.randomUUID().toString());
            control.getLabel().add(obs.getLabel());
            control.getConceptId().add(obs.getConceptCode());
            control.getParams().add(obs.getParams());
        }

        return control;
    }

    private void generateControlsElement(Node dispositionObs, List<Control> controls) {

        Element controlsElement = dispositionObs.getOwnerDocument().createElement("controls");

        for (Control control : controls) {
            Element when = controlsElement.getOwnerDocument().createElement("when");
            when.setAttribute("value", control.getWhenValue());
            List<String> thenDisplay = new ArrayList<String>(control.getThenDisplay());
            CollectionUtils.transform(thenDisplay, PREFIX_WITH_HASH); // prefix with the hash, since we are referencing by id
            when.setAttribute("thenDisplay", StringUtils.join(thenDisplay, ","));
            controlsElement.appendChild(when);
        }

        dispositionObs.appendChild(controlsElement);
    }

    private void generateAdditionalObsElements(Node dispositionObsGroup, List<Control> controls, Set<Obs> existingDispositionObsGroup) {

        for (Control control : controls) {

            for (int i = 0; i < control.getThenDisplay().size(); i++) {

                Element p = dispositionObsGroup.getOwnerDocument().createElement("p");
                p.setAttribute("id", control.getThenDisplay().get(i));

                // hide or show depending on whether there is an existing obs associated with the current "thenDisplay" element
                // this is to make sure that "additional obs" are shown when in view mode
                // would be better if the HFE control element could handle this explicitly
                if (existingDispositionObsGroup == null || !hasMemberWithConcept(existingDispositionObsGroup, HtmlFormEntryUtil.getConcept(control.getConceptId().get(i)))) {
                    p.setAttribute("style", "display:none;");
                }

                Element label = dispositionObsGroup.getOwnerDocument().createElement("label");
                Element uimessage = dispositionObsGroup.getOwnerDocument().createElement("uimessage");
                uimessage.setAttribute("code", control.getLabel().get(i));
                label.appendChild(uimessage);
                p.appendChild(label);

                Element obs = dispositionObsGroup.getOwnerDocument().createElement("obs");
                obs.setAttribute("conceptId", control.getConceptId().get(i));
                if (control.getParams() != null) {
                    for (Map.Entry<String,String> param : control.getParams().get(i).entrySet()) {
                        obs.setAttribute(param.getKey(), param.getValue());
                    }
                }

                p.appendChild(obs);
                dispositionObsGroup.appendChild(p);
            }
        }
    }

    private boolean hasMemberWithConcept(Set<Obs> obsGroup, Concept concept) {
        for (Obs obs : obsGroup) {
            if (obs.getConcept().equals(concept)) {
                return true;
            }
        }
        return false;
    }

    private Set<Obs> getObsGroupByGroupingConcept(Map<Obs,Set<Obs>> existingObsInGroups, Concept groupingConcept) {
      for (Obs obs : existingObsInGroups.keySet()) {
          if (obs.getConcept().equals(groupingConcept)) {
              return existingObsInGroups.get(obs);
          }
      }
      return null;
    }

    private class Control {

        private String whenValue;

        private List<String> thenDisplay = new ArrayList<String>();

        private List<String> conceptId = new ArrayList<String>();

        private List<String> label = new ArrayList<String>();

        private List<Map<String, String>> params = new ArrayList<Map<String, String>>();

        private String getWhenValue() {
            return whenValue;
        }

        private void setWhenValue(String whenValue) {
            this.whenValue = whenValue;
        }

        private List<String> getThenDisplay() {
            return thenDisplay;
        }

        private void setThenDisplay(List<String> thenDisplay) {
            this.thenDisplay = thenDisplay;
        }

        private List<String> getConceptId() {
            return conceptId;
        }

        private void setConceptId(List<String> conceptId) {
            this.conceptId = conceptId;
        }

        private List<String> getLabel() {
            return label;
        }

        private void setLabel(List<String> label) {
            this.label = label;
        }

        private List<Map<String, String>> getParams() {
            return params;
        }

        private void setParams(List<Map<String, String>> params) {
            this.params = params;
        }
    }

}
