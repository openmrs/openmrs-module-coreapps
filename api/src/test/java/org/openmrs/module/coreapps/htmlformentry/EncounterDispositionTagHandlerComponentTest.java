package org.openmrs.module.coreapps.htmlformentry;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.Encounter;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.ConceptService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.TestUiUtils;
import org.openmrs.module.coreapps.CoreAppsActivator;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
import org.openmrs.module.emrapi.test.ContextSensitiveMetadataTestUtils;
import org.openmrs.module.emrapi.test.builder.ObsBuilder;
import org.openmrs.module.htmlformentry.HtmlFormEntryService;
import org.openmrs.module.htmlformentry.RegressionTestHelper;
import org.openmrs.module.htmlformentry.TestUtil;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class EncounterDispositionTagHandlerComponentTest extends BaseModuleContextSensitiveTest {

    @Autowired
    private ConceptService conceptService;

    @Autowired
    private EmrApiProperties emrApiProperties;

    @Autowired
    private PatientService patientService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private EncounterService encounterService;

    @Before
    public void setUp() throws Exception {
        ContextSensitiveMetadataTestUtils.setupDispositionDescriptor(conceptService, emrApiProperties);
        EncounterDispositionTagHandler tagHandler = CoreAppsActivator.setupEncounterDispositionTagHandler(emrApiProperties);
        Context.getService(HtmlFormEntryService.class).addHandler(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DISPOSITION_TAG_NAME, tagHandler);
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
            }

            @Override
            public void testResults(SubmissionResults results) {
                results.assertNoErrors();
                results.assertEncounterCreated();
                results.assertProvider(1);
                results.assertLocation(2);
                results.assertObsGroupCreatedCount(1);
                results.assertObsGroupCreated(dispositionDescriptor.getDispositionSetConcept().getConceptId(),
                        dispositionDescriptor.getDispositionConcept().getId(), randomDisposition);
            }
        }.run();
    }

    @Test
    public void testViewingExistingDisposition() throws Exception {
        final Date date = new Date();
        final DispositionDescriptor dispositionDescriptor = emrApiProperties.getDispositionDescriptor();
        final Concept randomDisposition = dispositionDescriptor.getDispositionConcept().getAnswers().iterator().next().getAnswerConcept();
        final Patient patient = patientService.getPatient(8);

        // create an encounter
        final Encounter existingEncounter = new Encounter();
        existingEncounter.setPatient(patient);
        existingEncounter.setEncounterDatetime(date);
        existingEncounter.setLocation(locationService.getLocation(2));
        Obs construct = new ObsBuilder()
                .setConcept(dispositionDescriptor.getDispositionSetConcept())
                .addMember(dispositionDescriptor.getDispositionConcept(), randomDisposition)
                .get();
        existingEncounter.addObs(construct);
        encounterService.saveEncounter(existingEncounter);


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
            public Patient getPatientToView() throws Exception {
                return patient;
            };

            @Override
            public Encounter getEncounterToView() {
                return existingEncounter;
            }

            @Override
            public void testViewingEncounter(Encounter encounter, String html) {
                TestUtil.assertContains("<p><small>emr.consult.disposition</small><span>" + randomDisposition.getName().getName() + "</span></p", html);
            }

        }.run();
    }
}
