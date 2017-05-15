package org.openmrs.module.coreapps.htmlformentry;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.TestUiUtils;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.module.htmlformentry.HtmlFormEntryService;
import org.openmrs.module.htmlformentry.RegressionTestHelper;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public class CodedOrFreeTextObsTagHandlerComponentTest extends BaseModuleWebContextSensitiveTest {

    @Before
    public void setUp() throws Exception {
        CodedOrFreeTextObsTagHandler tagHandler = new CodedOrFreeTextObsTagHandler();
        Context.getService(HtmlFormEntryService.class).addHandler(CoreAppsConstants.HTMLFORMENTRY_CODED_OR_FREE_TEXT_OBS_TAG_NAME, tagHandler);
    }

    @Test
    public void testBlank() throws Exception {
        new RegressionTestHelper() {

            @Override
            public String getFormName() {
                return "codedOrFreeTextObsForm";
            }

            @Override
            public String getXmlDatasetPath() {
                return "";
            }

            @Override
            public Map<String, Object> getFormEntrySessionAttributes() {
                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("uiUtils", new TestUiUtils());
                return attributes;
            }

            @Override
            public String[] widgetLabels() {
                return new String[] { "Date:", "Location:", "Provider:", "Test:", "Encounter Type:" };
            }

            @Override
            public void setupRequest(MockHttpServletRequest request, Map<String, String> widgets) {
                request.addParameter(widgets.get("Date:"), dateAsString(new Date()));
                request.addParameter(widgets.get("Location:"), "2");
                request.addParameter(widgets.get("Provider:"), "1");
                request.addParameter(widgets.get("Test:"), "");
                request.addParameter(widgets.get("Encounter Type:"), "1");
            }

            @Override
            public void testResults(SubmissionResults results) {
                assertThat(results.getEncounterCreated().getAllObs().size(), is(0));
            }
        }.run();
    }

    @Test
    public void testEditingToBlankVoidsObs() throws Exception {
        new RegressionTestHelper() {

            @Override
            public String getFormName() {
                return "codedOrFreeTextObsForm";
            }

            @Override
            public String getXmlDatasetPath() {
                return "";
            }

            @Override
            public Map<String, Object> getFormEntrySessionAttributes() {
                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("uiUtils", new TestUiUtils());
                return attributes;
            }

            @Override
            public String[] widgetLabels() {
                return new String[] { "Date:", "Location:", "Provider:", "Test:", "Encounter Type:" };
            }

            @Override
            public void setupRequest(MockHttpServletRequest request, Map<String, String> widgets) {
                request.addParameter(widgets.get("Date:"), dateAsString(new Date()));
                request.addParameter(widgets.get("Location:"), "2");
                request.addParameter(widgets.get("Provider:"), "1");
                request.addParameter(widgets.get("Test:"), "ConceptName:f74e3673-983b-41e3-b872-56f0e6f7696e");
                request.addParameter(widgets.get("Encounter Type:"), "1");
            }

            @Override
            public void testResults(SubmissionResults results) {
                assertThat(results.getEncounterCreated().getAllObs().size(), is(1));
            }

            @Override
            public boolean doEditEncounter() {
                return true;
            }

            @Override
            public String[] widgetLabelsForEdit() {
                return widgetLabels();
            }

            @Override
            public void setupEditRequest(MockHttpServletRequest request, Map<String, String> widgets) {
                request.addParameter(widgets.get("Date:"), dateAsString(new Date()));
                request.addParameter(widgets.get("Location:"), "2");
                request.addParameter(widgets.get("Provider:"), "1");
                request.addParameter(widgets.get("Test:"), "");
                request.addParameter(widgets.get("Encounter Type:"), "1");
            }

            @Override
            public void testEditedResults(SubmissionResults results) {
                assertThat(results.getEncounterCreated().getAllObs(true).size(), is(1));
                assertThat(results.getEncounterCreated().getAllObs(false).size(), is(0));
            }
        }.run();
    }

    @Test
    public void testCodedEditedToCoded() throws Exception {
        new RegressionTestHelper() {

            @Override
            public String getFormName() {
                return "codedOrFreeTextObsForm";
            }

            @Override
            public String getXmlDatasetPath() {
                return "";
            }

            @Override
            public Map<String, Object> getFormEntrySessionAttributes() {
                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("uiUtils", new TestUiUtils());
                return attributes;
            }

            @Override
            public String[] widgetLabels() {
                return new String[] { "Date:", "Location:", "Provider:", "Test:", "Encounter Type:"};
            }

            @Override
            public void setupRequest(MockHttpServletRequest request, Map<String, String> widgets) {
                request.addParameter(widgets.get("Date:"), dateAsString(new Date()));
                request.addParameter(widgets.get("Location:"), "2");
                request.addParameter(widgets.get("Provider:"), "1");
                request.addParameter(widgets.get("Test:"), "ConceptName:f74e3673-983b-41e3-b872-56f0e6f7696e");
                request.addParameter(widgets.get("Encounter Type:"), "1");
            }

            @Override
            public void testResults(SubmissionResults results) {
                assertThat(results.getEncounterCreated().getAllObs().size(), is(1));
                results.assertObsCreated(4, Context.getConceptService().getConceptByName("MARRIED"));
            }

            @Override
            public boolean doEditEncounter() {
                return true;
            }

            @Override
            public String[] widgetLabelsForEdit() {
                return widgetLabels();
            }

            @Override
            public void setupEditRequest(MockHttpServletRequest request, Map<String, String> widgets) {
                request.addParameter(widgets.get("Date:"), dateAsString(new Date()));
                request.addParameter(widgets.get("Location:"), "2");
                request.addParameter(widgets.get("Provider:"), "1");
                request.addParameter(widgets.get("Test:"), "Concept:32d3611a-6699-4d52-823f-b4b788bac3e3");
                request.addParameter(widgets.get("Encounter Type:"), "1");
            }

            @Override
            public void testEditedResults(SubmissionResults results) {
                assertThat(results.getEncounterCreated().getAllObs(true).size(), is(2));
                assertThat(results.getEncounterCreated().getAllObs(false).size(), is(1));
                results.assertObsVoided(4, Context.getConceptService().getConceptByName("MARRIED"));
                results.assertObsCreated(4, Context.getConceptService().getConceptByUuid("32d3611a-6699-4d52-823f-b4b788bac3e3"));
            }
        }.run();
    }

    @Test
    public void testNonCodedEditedToCoded() throws Exception {
        new RegressionTestHelper() {

            @Override
            public String getFormName() {
                return "codedOrFreeTextObsForm";
            }

            @Override
            public String getXmlDatasetPath() {
                return "";
            }

            @Override
            public Map<String, Object> getFormEntrySessionAttributes() {
                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("uiUtils", new TestUiUtils());
                return attributes;
            }

            @Override
            public String[] widgetLabels() {
                return new String[] { "Date:", "Location:", "Provider:", "Test:", "Encounter Type:"};
            }

            @Override
            public void setupRequest(MockHttpServletRequest request, Map<String, String> widgets) {
                request.addParameter(widgets.get("Date:"), dateAsString(new Date()));
                request.addParameter(widgets.get("Location:"), "2");
                request.addParameter(widgets.get("Provider:"), "1");
                request.addParameter(widgets.get("Test:"), "NonCoded:This is a test");
                request.addParameter(widgets.get("Encounter Type:"), "1");
            }

            @Override
            public void testResults(SubmissionResults results) {
                assertThat(results.getEncounterCreated().getAllObs().size(), is(1));
                results.assertObsCreated(19, "This is a test");
            }

            @Override
            public boolean doEditEncounter() {
                return true;
            }

            @Override
            public String[] widgetLabelsForEdit() {
                return widgetLabels();
            }

            @Override
            public void setupEditRequest(MockHttpServletRequest request, Map<String, String> widgets) {
                request.addParameter(widgets.get("Date:"), dateAsString(new Date()));
                request.addParameter(widgets.get("Location:"), "2");
                request.addParameter(widgets.get("Provider:"), "1");
                request.addParameter(widgets.get("Test:"), "Concept:32d3611a-6699-4d52-823f-b4b788bac3e3");
                request.addParameter(widgets.get("Encounter Type:"), "1");
            }

            @Override
            public void testEditedResults(SubmissionResults results) {
                assertThat(results.getEncounterCreated().getAllObs(true).size(), is(2));
                assertThat(results.getEncounterCreated().getAllObs(false).size(), is(1));
                results.assertObsVoided(19, "This is a test");
                results.assertObsCreated(4, Context.getConceptService().getConceptByUuid("32d3611a-6699-4d52-823f-b4b788bac3e3"));
            }
        }.run();
    }

}