package org.openmrs.module.coreapps.htmlformentry;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptAnswer;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.Encounter;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.TestUiUtils;
import org.openmrs.module.coreapps.CoreAppsActivator;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.emrapi.test.ContextSensitiveMetadataTestUtils;
import org.openmrs.module.emrapi.test.builder.ConceptBuilder;
import org.openmrs.module.htmlformentry.HtmlFormEntryService;
import org.openmrs.module.htmlformentry.HtmlFormEntryUtil;
import org.openmrs.module.htmlformentry.RegressionTestHelper;
import org.openmrs.module.htmlformentry.TestUtil;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
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
public class EncounterDispositionTagHandlerComponentTest extends BaseModuleWebContextSensitiveTest {

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private EmrApiProperties emrApiProperties;

    @Autowired
    private DispositionService dispositionService;

    @Autowired
    private AdtService adtService;

    @Before
    public void setUp() throws Exception {
        ContextSensitiveMetadataTestUtils.setupDispositionDescriptor(conceptService, dispositionService);
        EncounterDispositionTagHandler tagHandler = CoreAppsActivator.setupEncounterDispositionTagHandler(emrApiProperties, dispositionService, adtService);
        Context.getService(HtmlFormEntryService.class).addHandler(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DISPOSITION_TAG_NAME, tagHandler);
        dispositionService.setDispositionConfig("coreappsTestDispositionConfig.json");  // we use a custom name so as to not clash with the test one in the emr-api module
    }

    @Test
    public void testFormOnlyShowsDispositionsInDescriptor() throws Exception {

        ConceptClass misc = conceptService.getConceptClassByName("Misc");
        ConceptDatatype naDatatype = conceptService.getConceptDatatypeByName("N/A");
        Concept dispositionConcept = dispositionService.getDispositionDescriptor().getDispositionConcept();
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
                return new String[] { "Date:", "Location:", "Provider:", "Disposition:", "Encounter Type:" };
            }


        }.run();
    }


    @Test
    public void testEnteringForm() throws Exception {

        final Date date = new Date();
        final DispositionDescriptor dispositionDescriptor = dispositionService.getDispositionDescriptor();
        final Concept admissionDisposition = HtmlFormEntryUtil.getConcept("org.openmrs.module.emrapi: Admit to hospital");

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
                return new String[] { "Date:", "Location:", "Provider:", "Disposition:", "Encounter Type:" };
            }

            @Override
            public void setupRequest(MockHttpServletRequest request, Map<String, String> widgets) {
                request.setParameter(widgets.get("Date:"), dateAsString(date));
                request.setParameter(widgets.get("Location:"), "2");
                request.setParameter(widgets.get("Provider:"), "1");
                request.setParameter(widgets.get("Encounter Type:"), "1");
                request.setParameter(widgets.get("Disposition:"), admissionDisposition.toString());
                request.setParameter("w10", "1");    // hack, manually reference the widget the location
            }

            @Override
            public void testResults(SubmissionResults results) {
                results.assertNoErrors();
                results.assertEncounterCreated();
                results.assertProvider(1);
                results.assertLocation(2);
                results.assertEncounterType(1);
                results.assertObsGroupCreatedCount(1);
                results.assertObsGroupCreated(dispositionDescriptor.getDispositionSetConcept().getConceptId(),
                        dispositionDescriptor.getDispositionConcept().getId(), admissionDisposition,
                        dispositionDescriptor.getAdmissionLocationConcept().getId(), "1");
            }

        }.run();
    }

    @Test
    public void testShouldDisplayAdmissionLocationAfterEnteringAdmissionDisposition() throws Exception {


        final Date date = new Date();
        final DispositionDescriptor dispositionDescriptor = dispositionService.getDispositionDescriptor();
        final Concept admissionDisposition = HtmlFormEntryUtil.getConcept("org.openmrs.module.emrapi: Admit to hospital");

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
                return new String[] { "Date:", "Location:", "Provider:", "Disposition:", "Encounter Type:" };
            }

            @Override
            public void setupRequest(MockHttpServletRequest request, Map<String, String> widgets) {
                request.setParameter(widgets.get("Date:"), dateAsString(date));
                request.setParameter(widgets.get("Location:"), "2");
                request.setParameter(widgets.get("Provider:"), "1");
                request.setParameter(widgets.get("Encounter Type:"), "1");
                request.setParameter(widgets.get("Disposition:"), admissionDisposition.toString());
                request.setParameter("w10", "1");    // hack, manually reference the widget the location
            }

            @Override
            public boolean doViewEncounter() {
                return true;
            }

            @Override
            public void testViewingEncounter(Encounter encounter, String html) {
                // make sure the admission location is displayed (ie, does not have style="display:none;")
                TestUtil.assertFuzzyContains("p id=\"[^\"]+\"><label><uimessage code=\"Admission Location\">", html);
            }


        }.run();
    }

    @Test
    public void testShouldNotDisplayAdmissionLocationAfterEnteringDischargeDisposition() throws Exception {

        final Date date = new Date();
        final DispositionDescriptor dispositionDescriptor = dispositionService.getDispositionDescriptor();
        final Concept dischargeDisposition = HtmlFormEntryUtil.getConcept("org.openmrs.module.emrapi: Discharged");

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
                return new String[] { "Date:", "Location:", "Provider:", "Disposition:", "Encounter Type:" };
            }

            @Override
            public void setupRequest(MockHttpServletRequest request, Map<String, String> widgets) {
                request.setParameter(widgets.get("Date:"), dateAsString(date));
                request.setParameter(widgets.get("Location:"), "2");
                request.setParameter(widgets.get("Provider:"), "1");
                request.setParameter(widgets.get("Encounter Type:"), "1");
                request.setParameter(widgets.get("Disposition:"), dischargeDisposition.toString());
            }

            @Override
            public boolean doViewEncounter() {
                return true;
            }

            @Override
            public void testViewingEncounter(Encounter encounter, String html) {
                // make sure the admission location is not displayed (ie, has style="display:none;")
                TestUtil.assertFuzzyContains("p id=\"[^\"]+\" style=\"display:none;\"><label><uimessage code=\"Admission Location\">", html);
            }


        }.run();
    }
}
