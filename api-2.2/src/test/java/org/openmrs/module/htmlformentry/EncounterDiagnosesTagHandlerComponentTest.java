package org.openmrs.module.htmlformentry;

import static org.junit.Assert.assertThat;
import static org.hamcrest.Matchers.is;

import java.util.Date;
import java.util.Map;

import org.junit.Before;
import org.junit.Test;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.coreapps.CoreAppsActivator;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.module.coreapps.htmlformentry.EncounterDiagnosesTagHandler;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.test.BaseModuleContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

public class EncounterDiagnosesTagHandlerComponentTest extends BaseModuleContextSensitiveTest { 
	
    private EncounterDiagnosesTagHandler encounterDiagnosesTagHandler;
	
	@Autowired
    ConceptService conceptService;

    @Autowired
    AdtService adtService;

    @Autowired
    EmrApiProperties emrApiProperties;
	
	@Before
	public void setup() throws Exception {
        encounterDiagnosesTagHandler = CoreAppsActivator.setupEncounterDiagnosesTagHandler(conceptService, adtService, Context.getRegisteredComponent("emrApiProperties", EmrApiProperties.class), null);
        Context.getService(HtmlFormEntryService.class).addHandler(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_NAME, encounterDiagnosesTagHandler);
	}

	@Test
	public void testSingleObsGroupAndEncounterDiagnosesTagsShouldPassGivenNonEmptyObsGroup() throws Exception {
		final Date date = new Date();
		new RegressionTestHelper() {

			@Override
			public String getFormName() {
				return "obsGroupAndEncounterForm";
			}

			@Override
			public String[] widgetLabels() {
				return new String[] { "Date:", "Location:", "Provider:", "Dose:" };
			}

			@Override
			public void setupRequest(MockHttpServletRequest request, Map<String, String> widgets) {
				request.addParameter(widgets.get("Date:"), dateAsString(date));
				request.addParameter(widgets.get("Location:"), "2");
				request.addParameter(widgets.get("Provider:"), "1");
				request.addParameter(widgets.get("Dose:"), "1x3 daily");
				request.addParameter("encounterDiagnoses", "[{\"certainty\":\"PRESUMED\",\"order\":\"PRIMARY\",\"diagnosis\":\"Concept:22\",\"existingObs\":null,\"existingDiagnosis\":null}]");
			}

			@Override
			public void testResults(SubmissionResults results) {
				results.assertNoErrors();
				results.assertEncounterCreated();
				results.assertProvider(1);
				results.assertLocation(2);
				results.assertObsCreated(51, "1x3 daily");
				assertThat(Context.getDiagnosisService().getDiagnoses(getPatient(), date).get(0).getDiagnosis().getCoded().getId(), is(22));
			}
		}.run();
	}
	
	@Test
	public void testSingleObsGroupAndEncounterDiagnosesTagsShouldPassGivenEmptyObsGroup() throws Exception {
		final Date date = new Date();
		new RegressionTestHelper() {

			@Override
			public String getFormName() {
				return "obsGroupAndEncounterForm";
			}

			@Override
			public String[] widgetLabels() {
				return new String[] { "Date:", "Location:", "Provider:", "Dose:" };
			}

			@Override
			public void setupRequest(MockHttpServletRequest request, Map<String, String> widgets) {
				request.addParameter(widgets.get("Date:"), dateAsString(date));
				request.addParameter(widgets.get("Location:"), "2");
				request.addParameter(widgets.get("Provider:"), "1");
				request.addParameter(widgets.get("Dose:"), "");
				request.addParameter("encounterDiagnoses", "[{\"certainty\":\"PRESUMED\",\"order\":\"PRIMARY\",\"diagnosis\":\"Concept:22\",\"existingObs\":null,\"existingDiagnosis\":null}]");
			}

			@Override
			public void testResults(SubmissionResults results) {
				results.assertNoErrors();
				results.assertEncounterCreated();
				results.assertProvider(1);
				results.assertLocation(2);
				assertThat(Context.getDiagnosisService().getDiagnoses(getPatient(), date).get(0).getDiagnosis().getCoded().getId(), is(22));
			}
		}.run();
	}
	

}
