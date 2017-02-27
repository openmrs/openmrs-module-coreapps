package org.openmrs.module.coreapps.htmlformentry;

import org.openmrs.ConceptSearchResult;
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
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CodedOrFreeTextAnswerWidget implements Widget {

    private String titleCode;

    private String placeholderCode;

    private String containerClasses;

    private CodedOrFreeTextAnswer initialValue;

    private UiUtils uiUtils;

    private Locale locale = Context.getLocale();

    @Override
    public String generateHtml(FormEntryContext context) {
        Translator translator = context.getTranslator();
        String localeStr = locale.toString();

        String title = translator.translate(localeStr, titleCode);
        String placeholder = translator.translate(localeStr, placeholderCode);

        StringBuilder ret = new StringBuilder();
        if (context.getMode().equals(FormEntryContext.Mode.VIEW)) {
            if (initialValue != null) {
                ret.append("<span class=\"value\">" + initialValue.format(locale) + "</span>");
            }
        } else {
            String hiddenInputName = context.getFieldName(this);
            Map<String, Object> fragmentConfig = new HashMap<String, Object>();
            fragmentConfig.put("formFieldName", hiddenInputName);
            fragmentConfig.put("title", title);
            fragmentConfig.put("placeholder", placeholder);
            fragmentConfig.put("containerClasses", containerClasses);
            fragmentConfig.put("initialValue", initialValueAsJson(initialValue));
            try {
                ret.append(uiUtils.includeFragment("coreapps", "htmlformentry/codedOrFreeTextAnswer", fragmentConfig));
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
        return ret.toString();
    }

    private String initialValueAsJson(CodedOrFreeTextAnswer initialValue) {
        if (initialValue == null) {
            return null;
        }

        ConceptSearchResult initial = new ConceptSearchResult(initialValue.getNonCodedAnswer(), initialValue.getCodedAnswer(), initialValue.getSpecificCodedAnswer());
        if (initial.getConcept() == null) {
            return uiUtils.toJson(SimpleObject.create("word", initial.getWord()));
        }
        try {
            // the code we are calling is in the web layer, so we handle it in a very hacky way here.
            Class<?> representationClass = Context.loadClass("org.openmrs.module.webservices.rest.web.representation.Representation");
            Object defaultRepresentation = representationClass.getField("DEFAULT").get(null);
            Class<?> conversionUtil = Context.loadClass("org.openmrs.module.webservices.rest.web.ConversionUtil");
            Method convert = conversionUtil.getMethod("convertToRepresentation", Object.class, representationClass);
            return uiUtils.toJson(convert.invoke(null, initial, defaultRepresentation));
        } catch (Exception ex) {
            // In an API-layer unit test this will fail due to not being able to load the class
            return null;
        }
    }

    @Override
    public Object getValue(FormEntryContext context, HttpServletRequest request) {
        String fieldName = context.getFieldName(this);
        String[] submitted = request.getParameterValues(fieldName);
        if (submitted == null || submitted.length == 0) {
            return null;
        }
        if (submitted.length != 1) {
            throw new IllegalArgumentException("Expected one submitted parameter value for " + fieldName + " but got " + submitted.length);
        }
        if (submitted[0].equals("")) {
            return null;
        }
        String prefix = submitted[0].substring(0, submitted[0].indexOf(':'));
        String value = submitted[0].substring(submitted[0].indexOf(':') + 1);
        if (prefix.equals("ConceptName")) {
            return new CodedOrFreeTextAnswer(Context.getConceptService().getConceptNameByUuid(value));
        } else if (prefix.equals("Concept")) {
            return new CodedOrFreeTextAnswer(Context.getConceptService().getConceptByUuid(value));
        } else if (prefix.equals("NonCoded")) {
            return new CodedOrFreeTextAnswer(value);
        } else {
            throw new IllegalArgumentException("Unexpected submission: " + submitted[0]);
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

    public String getContainerClasses() {
        return containerClasses;
    }

    public void setContainerClasses(String containerClasses) {
        this.containerClasses = containerClasses;
    }

    public CodedOrFreeTextAnswer getInitialValue() {
        return initialValue;
    }

    @Override
    public void setInitialValue(Object initialValue) {
        this.initialValue = (CodedOrFreeTextAnswer) initialValue;
    }

    public UiUtils getUiUtils() {
        return uiUtils;
    }

    public void setUiUtils(UiUtils uiUtils) {
        this.uiUtils = uiUtils;
    }

    public void setLocale(String locale) {
        this.locale = LocaleUtility.fromSpecification(locale);
    }
}
