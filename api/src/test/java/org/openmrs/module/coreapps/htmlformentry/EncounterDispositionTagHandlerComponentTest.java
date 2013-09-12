package org.openmrs.module.coreapps.htmlformentry;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.TestUiUtils;
import org.openmrs.module.coreapps.CoreAppsActivator;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
import org.openmrs.module.emrapi.disposition.DispositionFactory;
import org.openmrs.module.emrapi.test.ContextSensitiveMetadataTestUtils;
import org.openmrs.module.emrapi.test.builder.ConceptBuilder;
import org.openmrs.module.htmlformentry.HtmlFormEntryService;
import org.openmrs.module.htmlformentry.RegressionTestHelper;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;


/**
 *
 */
public class EncounterDispositionTagHandlerComponentTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private EmrApiProperties emrApiProperties;

   @Autowired
   private DispositionFactory dispositionFactory;

    @Before
    public void setUp() throws Exception {
        ContextSensitiveMetadataTestUtils.setupDispositionDescriptor(conceptService, emrApiProperties);
        EncounterDispositionTagHandler tagHandler = CoreAppsActivator.setupEncounterDispositionTagHandler(emrApiProperties, dispositionFactory);
        Context.getService(HtmlFormEntryService.class).addHandler(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DISPOSITION_TAG_NAME, tagHandler);
        dispositionFactory.setDispositionConfig("coreappsTestDispositionConfig.json");  // we use a custom name so as to not clash with the test one in the emr-api module
    }

    @Test
    public void testFormOnlyShowsDispositionsInDescriptor() throws Exception {

        ConceptClass misc = conceptService.getConceptClassByName("Misc");
        ConceptDatatype naDatatype = conceptService.getConceptDatatypeByName("N/A");
        Concept dispositionConcept = emrApiProperties.getDispositionDescriptor().getDispositionConcept();
        dispositionConcept.addAnswer(new ConceptAnswer(new ConceptBuilder(conceptService, naDatatype, misc).addName("Should not be in option list").saveAndGet()));

        new RegressionTestHelper() {

            @Override
            public void testBlankFormHtml(String html) {
                assertThat(html, containsString("disposition.admission"));
                assertThat(html, containsString("disposition.transfer"));
                assertThat(html, containsString("disposition.discharge"));
                assertThat(html, not(containsString("Should not be in option list")));
            }

            @Override
            public String getXmlDatasetPath() {
                return "";
            }

            @Override
            public String getFormName() {
                return "encounterDispositionSimpleForm";
            }

            @Override
            public Map<String, Object> getFormEntrySessionAttributes() {
                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("uiUtils", new TestUiUtils());
                return attributes;
            }

            @Override
            public String[] widgetLabels() {
                return new String[] { "Date:", "Location:", "Provider:", "Disposition:" };
            }


        }.run();
    }


    @Test
    public void testEnteringForm() throws Exception {
        final Date date = new Date();

        final DispositionDescriptor dispositionDescriptor = emrApiProperties.getDispositionDescriptor();
        final Concept randomDisposition = dispositionDescriptor.getDispositionConcept().getAnswers().iterator().next().getAnswerConcept();

        new RegressionTestHelper() {

            @Override
            public String getXmlDatasetPath() {
                return "";
            }

            @Override
            public String getFormName() {
                return "encounterDispositionSimpleForm";
            }

            @Override
            public Map<String, Object> getFormEntrySessionAttributes() {
                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("uiUtils", new TestUiUtils());
                return attributes;
            }

            @Override
            public String[] widgetLabels() {
                return new String[] { "Date:", "Location:", "Provider:", "Disposition:" };
            }

            @Override
            public void setupRequest(MockHttpServletRequest request, Map<String, String> widgets) {
                request.setParameter(widgets.get("Date:"), dateAsString(date));
                request.setParameter(widgets.get("Location:"), "2");
                request.setParameter(widgets.get("Provider:"), "1");
                request.setParameter(widgets.get("Disposition:"), randomDisposition.getConceptId().toString());
                request.setParameter("w10", "1");    // hack, manually reference the widget the location
            }

            @Override
            public void testResults(SubmissionResults results) {
                results.assertNoErrors();
                results.assertEncounterCreated();
                results.assertProvider(1);
                results.assertLocation(2);
                results.assertObsGroupCreatedCount(1);
                results.assertObsGroupCreated(dispositionDescriptor.getDispositionSetConcept().getConceptId(),
                        dispositionDescriptor.getDispositionConcept().getId(), randomDisposition,
                        dispositionDescriptor.getAdmissionLocationConcept().getId(), "1");
            }
        }.run();
    }

}
