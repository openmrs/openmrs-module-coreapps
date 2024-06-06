package org.openmrs.module.coreapps.htmlformentry;

import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.api.ConceptNameType;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.htmlformentry.FormEntryContext;
import org.openmrs.module.htmlformentry.Translator;
import org.openmrs.ui.framework.UiUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class CodedOrFreeTextAnswerListWidgetTest {

    private CodedOrFreeTextAnswerListWidget widget;
    private FormEntryContext context;
    private UiUtils uiUtils;

    @Before
    public void setUp() throws Exception {
        widget = new CodedOrFreeTextAnswerListWidget();
        widget.setTitleCode("Title");
        widget.setPlaceholderCode("Instructions");
        widget.setBetweenElementsCode("(between)");
        widget.setContainerClasses("required");

        uiUtils = mock(UiUtils.class);
        widget.setUiUtils(uiUtils);
    }

    public void setUpView() throws Exception {
        context = new FormEntryContext(FormEntryContext.Mode.VIEW) {
            @Override
            public Translator getTranslator() {
                return new Translator() {
                    @Override
                    public String translate(String localeStr, String key) {
                        return key;
                    }
                };
            }
        };
    }

    public void setUpEnter() throws Exception {
        context = new FormEntryContext(FormEntryContext.Mode.ENTER) {
            @Override
            public Translator getTranslator() {
                return new Translator() {
                    @Override
                    public String translate(String localeStr, String key) {
                        return key;
                    }
                };
            }
        };
        context.registerWidget(widget);
    }

    @Test
    public void testViewEmptyList() throws Exception {
        setUpView();
        String html = widget.generateHtml(context);
        assertThat(html, matches("<div class=\"coded-or-free-text-list-widget\">\\s*" +
                "<span class=\"coded-or-free-text-title\">" +
                "Title" +
                "</span>\\s*" +
                "</div>"));
    }

    @Test
    public void testViewNonEmptyList() throws Exception {
        setUpView();

        ConceptName conceptName = new ConceptName();
        conceptName.setLocale(Context.getLocale());
        conceptName.setConceptNameType(ConceptNameType.FULLY_SPECIFIED);
        conceptName.setLocalePreferred(true);
        conceptName.setName("Coded");
        Concept concept = new Concept();
        concept.addName(conceptName);

        List<CodedOrFreeTextAnswer> list = new ArrayList<CodedOrFreeTextAnswer>();
        list.add(new CodedOrFreeTextAnswer("Free text"));
        list.add(new CodedOrFreeTextAnswer(concept));
        widget.setInitialValue(list);

        String html = widget.generateHtml(context);
        assertThat(html, matches("<div class=\"coded-or-free-text-list-widget\">\\s*" +
                "<span class=\"coded-or-free-text-title\">\\s*" +
                "Title\\s*" +
                "</span>\\s*" +
                "<br/>\\s*" +
                "<span class=\"value\">\\s*" +
                "\"Free text\"\\s*" +
                "</span>\\s*" +
                "<br/>\\s*" +
                "<span class=\"coded-or-free-text-between\">\\s*" +
                "\\(between\\)\\s*" +
                "</span>\\s*" +
                "<br/>\\s*" +
                "<span class=\"value\">\\s*" +
                "Coded\\s*" +
                "</span>\\s*" +
                "</div>"));
    }

    @Test
    public void testEnter() throws Exception {
        setUpEnter();
        String html = widget.generateHtml(context);

        Map<String, Object> expectedConfig = new HashMap<String, Object>();
        expectedConfig.put("formFieldName", context.getFieldName(widget));
        expectedConfig.put("title", "Title");
        expectedConfig.put("placeholder", "Instructions");
        expectedConfig.put("initialValue", null);
        expectedConfig.put("betweenElements", "(between)");
        expectedConfig.put("containerClasses", "required");

        verify(uiUtils).includeFragment("coreapps", "htmlformentry/codedOrFreeTextAnswerList", expectedConfig);
    }

    private Matcher<? super String> matches(final String regex) {
        return new BaseMatcher<String>() {

            @Override
            public boolean matches(Object o) {
                String actual = (String) o;
                return actual.matches(regex);
            }

            @Override
            public void describeTo(Description description) {
                description.appendText("text matching regex: " + regex);
            }
        };
    }

}