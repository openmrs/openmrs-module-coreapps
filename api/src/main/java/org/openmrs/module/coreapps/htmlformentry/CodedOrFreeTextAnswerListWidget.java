package org.openmrs.module.coreapps.htmlformentry;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.openmrs.ConceptSearchResult;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.htmlformentry.FormEntryContext;
import org.openmrs.module.htmlformentry.Translator;
import org.openmrs.module.htmlformentry.widget.Widget;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageAction;
import org.openmrs.util.LocaleUtility;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Uses a nice AngularJS widget to capture a List<CodedOrFreeTextAnswer>.
 */
public class CodedOrFreeTextAnswerListWidget implements Widget {

    private String titleCode;

    private String placeholderCode;

    private String betweenElementsCode;

    private String containerClasses;

    private List<CodedOrFreeTextAnswer> initialValue;

    private UiUtils uiUtils;

    private Locale locale = Context.getLocale();

    @Override
    public void setInitialValue(Object initialValue) {
        this.initialValue = (List<CodedOrFreeTextAnswer>) initialValue;
    }

    @Override
    public String generateHtml(FormEntryContext context) {
        Translator translator = context.getTranslator();
        String localeStr = locale.toString();

        String title = translator.translate(localeStr, titleCode);
        String betweenElements = translator.translate(localeStr, betweenElementsCode);
        String placeholder = translator.translate(localeStr, placeholderCode);

        StringBuilder ret = new StringBuilder();
        ret.append("<div class=\"coded-or-free-text-list-widget\">");
        if (context.getMode().equals(FormEntryContext.Mode.VIEW)) {
            ret.append("<span class=\"coded-or-free-text-title\">" + title + "</span>\n");
            if (initialValue != null) {
                boolean first = true;
                for (CodedOrFreeTextAnswer answer : initialValue) {
                    if (!first && StringUtils.isNotEmpty(betweenElements)) {
                        ret.append("<br/>");
                        ret.append("<span class=\"coded-or-free-text-between\">").append(betweenElements).append("</span>");
                    }
                    ret.append("<br/>");
                    ret.append("<span class=\"value\">").append(answer.format(locale)).append("</span>");
                    first = false;
                }
            }

        } else {
            String hiddenInputName = context.getFieldName(this);
            Map<String, Object> fragmentConfig = new HashMap<String, Object>();
            fragmentConfig.put("formFieldName", hiddenInputName);
            fragmentConfig.put("title", title);
            fragmentConfig.put("placeholder", placeholder);
            fragmentConfig.put("betweenElements", betweenElements);
            fragmentConfig.put("containerClasses", containerClasses);
            fragmentConfig.put("initialValue", initialValueAsJson(initialValue));
            try {
                ret.append(uiUtils.includeFragment("coreapps", "htmlformentry/codedOrFreeTextAnswerList", fragmentConfig));
            } catch (PageAction pageAction) {
                throw new IllegalStateException("fragment threw PageAction", pageAction);
            } catch (NullPointerException ex) {
                // if we are validating/submitting the form, then this method is being called from a fragment action method
                // and the UiUtils we have access to doesn't have a FragmentIncluder. That's okay, because we don't actually
                // need to generate the HTML, so we can pass on this exception.
                // (This is hacky, but I don't see a better way to do it.)
                return "<input type=\"hidden\" name=\"" + context.getFieldName(this) + "\"/> (Submitting the form, so we don't generate HTML)";
            }
        }
        ret.append("</div>");
        return ret.toString();
    }

    private String initialValueAsJson(List<CodedOrFreeTextAnswer> initialValue) {
        if (initialValue == null || initialValue.size() == 0) {
            return null;
        }

        // TODO large scale refactoring to have a proper REST-compatible representation of CodedOrFreeTextAnswer, and have diagnosis search use this
        try {
            // the controller is in the web layer, so we handle it in a very hacky way here.
            Object controller = Context.loadClass("org.openmrs.module.coreapps.fragment.controller.DiagnosesFragmentController").newInstance();
            Method simplify = controller.getClass().getMethod("simplify", ConceptSearchResult.class, UiUtils.class, Locale.class);

            List<Object> simplified = new ArrayList<Object>();
            for (CodedOrFreeTextAnswer answer : initialValue) {
                if (answer.getNonCodedAnswer() != null) {
                    simplified.add(SimpleObject.create("matchedName", answer.getNonCodedAnswer(), "nonCodedValue", answer.getNonCodedAnswer()));
                } else {
                    ConceptSearchResult result = new ConceptSearchResult(null, answer.getCodedAnswer(), answer.getSpecificCodedAnswer());
                    simplified.add(simplify.invoke(controller, result, uiUtils, Context.getLocale()));
                }
            }
            return uiUtils.toJson(simplified);
        } catch (Exception ex) {
            // In an API-layer unit test this will fail due to not being able to load the class
            return null;
        }
    }

    @Override
    public Object getValue(FormEntryContext context, HttpServletRequest request) {
        String fieldName = context.getFieldName(this);
        String[] submitted = request.getParameterValues(fieldName);
        if (submitted != null && submitted.length > 1) {
            throw new IllegalArgumentException("Expected one submitted parameter value for " + fieldName + " but got " + submitted.length);
        }
        try {
            List<CodedOrFreeTextAnswer> results = new ArrayList<CodedOrFreeTextAnswer>();
            if (submitted !=null && StringUtils.isNotEmpty(submitted[0])) {
                ArrayNode array = new ObjectMapper().readValue(submitted[0], ArrayNode.class);
                ConceptService conceptService = Context.getConceptService();
                for (JsonNode node : array) {
                    String conceptNameUuid = node.path("ConceptName").getTextValue();
                    String conceptUuid = node.path("Concept").getTextValue();
                    String nonCodedValue = node.path("NonCodedValue").getTextValue();
                    if (conceptNameUuid != null) {
                        results.add(new CodedOrFreeTextAnswer(conceptService.getConceptNameByUuid(conceptNameUuid)));
                    } else if (conceptUuid != null) {
                        results.add(new CodedOrFreeTextAnswer(conceptService.getConceptByUuid(conceptUuid)));
                    } else {
                        results.add(new CodedOrFreeTextAnswer(nonCodedValue));
                    }
                }
            }
            return results;
        } catch (IOException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public String getTitleCode() {
        return titleCode;
    }

    public void setTitleCode(String titleCode) {
        this.titleCode = titleCode;
    }

    public String getPlaceholderCode() {
        return placeholderCode;
    }

    public void setPlaceholderCode(String placeholderCode) {
        this.placeholderCode = placeholderCode;
    }

    public String getBetweenElementsCode() {
        return betweenElementsCode;
    }

    public void setBetweenElementsCode(String betweenElementsCode) {
        this.betweenElementsCode = betweenElementsCode;
    }

    public String getContainerClasses() {
        return containerClasses;
    }

    public void setContainerClasses(String containerClasses) {
        this.containerClasses = containerClasses;
    }

    public void setUiUtils(UiUtils uiUtils) {
        this.uiUtils = uiUtils;
    }

    public void setLocale(String locale) {
        this.locale = LocaleUtility.fromSpecification(locale);
    }
}
