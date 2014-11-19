package org.openmrs.module.coreapps.htmlformentry;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.htmlformentry.BadFormDesignException;
import org.openmrs.module.htmlformentry.FormEntryContext;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.htmlformentry.FormSubmissionController;
import org.openmrs.module.htmlformentry.FormSubmissionError;
import org.openmrs.module.htmlformentry.HtmlFormEntryUtil;
import org.openmrs.module.htmlformentry.action.FormSubmissionControllerAction;
import org.openmrs.module.htmlformentry.element.HtmlGeneratorElement;
import org.openmrs.module.htmlformentry.handler.AttributeDescriptor;
import org.openmrs.module.htmlformentry.handler.SubstitutionTagHandler;
import org.openmrs.ui.framework.UiUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class CodedOrFreeTextObsTagHandler extends SubstitutionTagHandler {

    @Override
    protected List<AttributeDescriptor> createAttributeDescriptors() {
        List<AttributeDescriptor> attributeDescriptors = new ArrayList<AttributeDescriptor>();
        attributeDescriptors.add(new AttributeDescriptor("codedConceptId", Concept.class));
        attributeDescriptors.add(new AttributeDescriptor("nonCodedConceptId", Concept.class));
        return Collections.unmodifiableList(attributeDescriptors);
    }

    @Override
    protected String getSubstitution(FormEntrySession formEntrySession, FormSubmissionController formSubmissionController, Map<String, String> attributes) throws BadFormDesignException {
        Element element = new Element(formEntrySession, attributes);
        formSubmissionController.addAction(element);

        return element.generateHtml(formEntrySession.getContext());
    }

    class Element implements FormSubmissionControllerAction, HtmlGeneratorElement {

        private Concept codedConcept;
        private Concept nonCodedConcept;
        private CodedOrFreeTextAnswerWidget widget;
        private Obs existingObs;

        public Element(FormEntrySession session, Map<String, String> attributes) {
            FormEntryContext context = session.getContext();
            widget = new CodedOrFreeTextAnswerWidget();
            if (attributes.get("locale") != null) {
                widget.setLocale(attributes.get("locale"));
            }
            widget.setUiUtils((UiUtils) session.getAttribute("uiUtils"));
            context.registerWidget(widget);
            widget.setTitleCode(attributes.get("titleCode"));
            widget.setContainerClasses(attributes.get("containerClasses"));

            this.codedConcept = HtmlFormEntryUtil.getConcept(attributes.get("codedConceptId"));
            this.nonCodedConcept = HtmlFormEntryUtil.getConcept(attributes.get("nonCodedConceptId"));

            // TODO: this doesn't work in obs groups yet

            CodedOrFreeTextAnswer initialValue = null;
            List<Obs> existingCoded = context.removeExistingObs(codedConcept);
            if (existingCoded != null) {
                if (existingCoded.size() > 1) {
                    throw new IllegalStateException("Cannot handle scenario with >1 existing obs");
                }
                if (existingCoded.size() == 1) {
                    existingObs = existingCoded.get(0);
                }
            } else {
                List<Obs> existingNonCoded = context.removeExistingObs(nonCodedConcept);
                if (existingNonCoded != null) {
                    if (existingNonCoded.size() > 1) {
                        throw new IllegalStateException("Cannot handle scenario with >1 existing obs");
                    }
                    if (existingNonCoded.size() == 1) {
                        existingObs = existingNonCoded.get(0);
                    }
                }
            }
            if (existingObs != null) {
                widget.setInitialValue(new CodedOrFreeTextAnswer(existingObs));
            }
        }

        @Override
        public String generateHtml(FormEntryContext context) {
            return widget.generateHtml(context);
        }

        @Override
        public Collection<FormSubmissionError> validateSubmission(FormEntryContext context, HttpServletRequest request) {
            Object value = widget.getValue(context, request);
            return null;
        }

        @Override
        public void handleSubmission(FormEntrySession formEntrySession, HttpServletRequest request) {
            CodedOrFreeTextAnswer value = (CodedOrFreeTextAnswer) widget.getValue(formEntrySession.getContext(), request);
            Concept newConcept = value == null ? null : (value.getCodedAnswer() != null ? codedConcept : nonCodedConcept);
            Object newValue = value == null ? null : value.getValue();
            if (existingObs != null) {
                formEntrySession.getSubmissionActions().modifyObs(existingObs, newConcept, newValue, null, null);
            }
            else if (value != null) {
                formEntrySession.getSubmissionActions().createObs(newConcept, value.getValue(), null, null);
            }
        }

    }

}
