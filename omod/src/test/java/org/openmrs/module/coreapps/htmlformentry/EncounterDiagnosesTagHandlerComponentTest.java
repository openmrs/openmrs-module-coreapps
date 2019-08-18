/*
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */

package org.openmrs.module.coreapps.htmlformentry;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.when;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
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
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.matcher.ObsGroupMatcher;
import org.openmrs.module.emrapi.test.ContextSensitiveMetadataTestUtils;
import org.openmrs.module.htmlformentry.FormEntryContext;
import org.openmrs.module.htmlformentry.FormEntrySession;
import org.openmrs.module.htmlformentry.FormSubmissionController;
import org.openmrs.module.htmlformentry.HtmlFormEntryService;
import org.openmrs.module.htmlformentry.RegressionTestHelper;
import org.openmrs.ui.framework.Model;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.page.PageAction;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

import groovy.text.SimpleTemplateEngine;
import groovy.text.Template;

/**
 *
 */
public class EncounterDiagnosesTagHandlerComponentTest extends BaseModuleWebContextSensitiveTest {

    private EncounterDiagnosesTagHandler encounterDiagnosesTagHandler;

    private String GENERAL_AND_SPECIFIED_DIAGNOSIS_SET_UUID = "d1010973-803e-8659-4415-c01707c01dec";

    private String HIV_OPPORTUNISTIC_INFECTION_DIAGNOSIS_SET_UUID = "d710b7h4-40b7-d333-b449-6e0e15d0739d";

    private String CONCEPT_SOURCE_UUID = "75f5b378-5065-11de-80cb-001e378eb67e";
    
    private String USE_NULL_VALUE = "0";
    
    @Mock
    private FormEntrySession formEntrySession;

    @Mock
    private FormEntryContext formEntryContext;
    
    @Mock
    private FormSubmissionController formSubmissionController;

    @Mock
    private UiUtils uiUtils;

    @Autowired
    ConceptService conceptService;

    @Autowired
    PatientService patientService;

    @Autowired
    EncounterService encounterService;

    @Autowired
    LocationService locationService;

    @Autowired
    AdtService adtService;

    @Autowired
    EmrApiProperties emrApiProperties;

    @Before
    public void setUp() throws Exception {
        when(formEntryContext.getMode()).thenReturn(FormEntryContext.Mode.ENTER);
        when(formEntrySession.getContext()).thenReturn(formEntryContext);
        when(uiUtils.message(anyString())).thenReturn("message");
        // this ensures that our mock of UiUtils renders the fragment it's been given (the third argument)
        when(uiUtils.includeFragment(eq("coreapps"), eq("diagnosis/encounterDiagnoses"), any(Map.class))).thenAnswer(new Answer<String>() {
			@Override
			public String answer(InvocationOnMock invocation) throws Throwable {
				Map<String, Object> fragmentConfig = (Map<String, Object>) invocation.getArguments()[2];
				return renderFragmentHtml(fragmentConfig);
			}
        });

        ContextSensitiveMetadataTestUtils.setupDiagnosisMetadata(conceptService, emrApiProperties);
        encounterDiagnosesTagHandler = CoreAppsActivator.setupEncounterDiagnosesTagHandler(conceptService, adtService, Context.getRegisteredComponent("emrApiProperties", EmrApiProperties.class), null);
        Context.getService(HtmlFormEntryService.class).addHandler(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_NAME, encounterDiagnosesTagHandler);
        encounterDiagnosesTagHandler.setUiUtils(uiUtils);

        // Setting up diagnosis sets
        {
            ConceptSource source = conceptService.getConceptSourceByUuid(CONCEPT_SOURCE_UUID);
            source.setName("CIEL");
            source = conceptService.saveConceptSource(source);
            ConceptMapType conceptMapType = conceptService.getConceptMapTypeByUuid("35543629-7d8c-11e1-909d-c80aa9edcf4e");

            Concept diagnosisSet1 = new Concept();
            diagnosisSet1.setUuid(GENERAL_AND_SPECIFIED_DIAGNOSIS_SET_UUID);
            diagnosisSet1.setFullySpecifiedName(new ConceptName("General and unspecified diagnoses", Context.getLocale()));
            diagnosisSet1.setConceptClass(conceptService.getConceptClassByName("ConvSet"));
            diagnosisSet1.setDatatype(conceptService.getConceptDatatypeByName("N/A"));
            diagnosisSet1.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(source,"160168","Test"), conceptMapType));
            conceptService.saveConcept(diagnosisSet1);

            Concept diagnosisSet2 = new Concept();
            diagnosisSet2.setUuid(HIV_OPPORTUNISTIC_INFECTION_DIAGNOSIS_SET_UUID);
            diagnosisSet2.setFullySpecifiedName(new ConceptName("HIV and opportunistic infection diagnoses", Context.getLocale()));
            diagnosisSet2.setConceptClass(conceptService.getConceptClassByName("ConvSet"));
            diagnosisSet2.setDatatype(conceptService.getConceptDatatypeByName("N/A"));
            diagnosisSet2.addConceptMapping(new ConceptMap(new ConceptReferenceTerm(source,"160170","Test"), conceptMapType));
            conceptService.saveConcept(diagnosisSet2);
        }
    }

    @Test
    public void testHandleSubmissionHandlesValidSubmissionEnteringForm() throws Exception {
        final int codedConceptId = 11;

        ObjectMapper jackson = new ObjectMapper();
        ArrayNode json = jackson.createArrayNode();
        ObjectNode diagnosisNode = json.addObject();
        diagnosisNode.put("certainty", "PRESUMED");
        diagnosisNode.put("order", "PRIMARY");
        diagnosisNode.put("diagnosis", CodedOrFreeTextAnswer.CONCEPT_PREFIX + codedConceptId);
        final String jsonToSubmit = jackson.writeValueAsString(json);

        final DiagnosisMetadata dmd = emrApiProperties.getDiagnosisMetadata();

        final Date date = new Date();

        new RegressionTestHelper() {
            @Override
            public String getXmlDatasetPath() {
                return "";
            }

            @Override
            public String getFormName() {
                return "encounterDiagnosesSimpleForm";
            }

            @Override
            public Map<String, Object> getFormEntrySessionAttributes() {
                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("uiUtils", new TestUiUtils() {
                    @Override
                    public String includeFragment(String providerName, String fragmentId, Map<String, Object> config) throws PageAction {
                        return "[[ included fragment " + providerName + "." + fragmentId + " here ]]";
                    }
                });
                return attributes;
            }

            @Override
            public String[] widgetLabels() {
                return new String[] { "Date:", "Location:", "Provider:", "Encounter Type:" };
            }

            @Override
            public void setupRequest(MockHttpServletRequest request, Map<String, String> widgets) {
                request.setParameter(widgets.get("Date:"), dateAsString(date));
                request.setParameter(widgets.get("Location:"), "2");
                request.setParameter(widgets.get("Provider:"), "1");
                request.setParameter(widgets.get("Encounter Type:"), "1");
                request.setParameter("encounterDiagnoses", jsonToSubmit);
            }

            @Override
            public void testResults(SubmissionResults results) {
                results.assertNoErrors();
                results.assertEncounterCreated();
                results.assertProvider(1);
                results.assertLocation(2);
                results.assertEncounterType(1);
                results.assertObsCreatedCount(1);
                results.assertObsGroupCreated(dmd.getDiagnosisSetConcept().getConceptId(),
                        dmd.getDiagnosisCertaintyConcept().getId(), dmd.getConceptFor(Diagnosis.Certainty.PRESUMED),
                        dmd.getDiagnosisOrderConcept().getId(), dmd.getConceptFor(Diagnosis.Order.PRIMARY),
                        dmd.getCodedDiagnosisConcept().getId(), Context.getConceptService().getConcept(codedConceptId));
            }
        }.run();
    }

    @Test
    public void testHandleSubmissionHandlesValidSubmissionEditingForm() throws Exception {
        final DiagnosisMetadata dmd = emrApiProperties.getDiagnosisMetadata();
        final Date date = new Date();

        // first, create an encounter
        final Concept malaria = conceptService.getConcept(11);
        final Patient patient = patientService.getPatient(8);
        final Encounter existingEncounter = new Encounter();
        existingEncounter.setEncounterType(new EncounterType(1));
        existingEncounter.setPatient(patient);
        existingEncounter.setEncounterDatetime(date);
        existingEncounter.setLocation(locationService.getLocation(2));
        Obs primaryDiagnosis = dmd.buildDiagnosisObsGroup(new Diagnosis(new CodedOrFreeTextAnswer(malaria), Diagnosis.Order.PRIMARY));
        Obs secondaryDiagnosis = dmd.buildDiagnosisObsGroup(new Diagnosis(new CodedOrFreeTextAnswer("Unknown disease"), Diagnosis.Order.SECONDARY));
        existingEncounter.addObs(primaryDiagnosis);
        existingEncounter.addObs(secondaryDiagnosis);
        encounterService.saveEncounter(existingEncounter);

        ObjectMapper jackson = new ObjectMapper();
        ArrayNode json = jackson.createArrayNode();
        { // change the existing primary diagnosis from PRESUMED to CONFIRMED
            ObjectNode diagnosisNode = json.addObject();
            diagnosisNode.put("certainty", "CONFIRMED");
            diagnosisNode.put("order", "PRIMARY");
            diagnosisNode.put("diagnosis", CodedOrFreeTextAnswer.CONCEPT_PREFIX + "11");
            diagnosisNode.put("existingObs", primaryDiagnosis.getObsId());
        }
        { // this is a new diagnosis
            ObjectNode diagnosisNode = json.addObject();
            diagnosisNode.put("certainty", "PRESUMED");
            diagnosisNode.put("order", "SECONDARY");
            diagnosisNode.put("diagnosis", CodedOrFreeTextAnswer.NON_CODED_PREFIX + "I had spelled it wrong before");
        }
        final String jsonToSubmit = jackson.writeValueAsString(json);

        new RegressionTestHelper() {
            @Override
            public String getXmlDatasetPath() {
                return "";
            }

            @Override
            public String getFormName() {
                return "encounterDiagnosesSimpleForm";
            }

            @Override
            public Map<String, Object> getFormEntrySessionAttributes() {
                Map<String, Object> attributes = new HashMap<String, Object>();
                attributes.put("uiUtils", new TestUiUtils() {
                    @Override
                    public String includeFragment(String providerName, String fragmentId, Map<String, Object> config) throws PageAction {
                        return "[[ included fragment " + providerName + "." + fragmentId + " here ]]";
                    }
                });
                return attributes;
            }

            @Override
            public Patient getPatientToView() throws Exception {
                return patient;
            };

            @Override
            public Encounter getEncounterToEdit() {
                return existingEncounter;
            }

            @Override
            public String[] widgetLabelsForEdit() {
                return new String[] { "Date:", "Location:", "Provider:", "Encounter Type:" };
            }

            @Override
            public void setupEditRequest(MockHttpServletRequest request, Map<String, String> widgets) {
                request.setParameter(widgets.get("Date:"), dateAsString(date));
                request.setParameter(widgets.get("Location:"), "2");
                request.setParameter(widgets.get("Provider:"), "1");
                request.setParameter(widgets.get("Encounter Type:"), "1");
                request.setParameter("encounterDiagnoses", jsonToSubmit);
            };

            @Override
            public void testEditFormHtml(String html) {
                System.out.println(html);
            }

            @Override
            public void testEditedResults(SubmissionResults results) {
                results.assertNoErrors();
                results.assertEncounterEdited();

                List<Obs> diagnoses = new ArrayList<Obs>();
                for (Obs obs : results.getEncounterCreated().getObsAtTopLevel(true)) {
                    if (dmd.isDiagnosis(obs)) {
                        diagnoses.add(obs);
                    }
                }

                Matcher<Obs> newPrimary = new ObsGroupMatcher()
                        .withGroupingConcept(dmd.getDiagnosisSetConcept())
                        .withNonVoidedObs(dmd.getDiagnosisCertaintyConcept(), dmd.getConceptFor(Diagnosis.Certainty.CONFIRMED))
                        .withVoidedObs(dmd.getDiagnosisCertaintyConcept(), dmd.getConceptFor(Diagnosis.Certainty.PRESUMED))
                        .withNonVoidedObs(dmd.getDiagnosisOrderConcept(), dmd.getConceptFor(Diagnosis.Order.PRIMARY))
                        .withNonVoidedObs(dmd.getCodedDiagnosisConcept(), malaria);

                Matcher<Obs> newSecondary = new ObsGroupMatcher()
                        .withGroupingConcept(dmd.getDiagnosisSetConcept())
                        .withNonVoidedObs(dmd.getDiagnosisCertaintyConcept(), dmd.getConceptFor(Diagnosis.Certainty.PRESUMED))
                        .withNonVoidedObs(dmd.getDiagnosisOrderConcept(), dmd.getConceptFor(Diagnosis.Order.SECONDARY))
                        .withNonVoidedObs(dmd.getNonCodedDiagnosisConcept(), "I had spelled it wrong before");

                Matcher<Obs> oldSecondary = new ObsGroupMatcher()
                        .withGroupingConcept(dmd.getDiagnosisSetConcept())
                        .withVoidedObs(dmd.getDiagnosisCertaintyConcept(), dmd.getConceptFor(Diagnosis.Certainty.PRESUMED))
                        .withVoidedObs(dmd.getDiagnosisOrderConcept(), dmd.getConceptFor(Diagnosis.Order.SECONDARY))
                        .withVoidedObs(dmd.getNonCodedDiagnosisConcept(), "Unknown disease")
                        .thatIsVoided();

                assertThat(diagnoses.size(), is(3));
                assertThat(diagnoses, containsInAnyOrder(newPrimary, newSecondary, oldSecondary));
            };
        }.run();
    }

    @Test
    public void getSubstitution_shouldAddPreferredCodingSourceWithPreferredValueAttributeOnDiagnosisSearchField() throws Exception {
        // Setup
        String preferredCodingSource = "ICPC2";

        Map<String,String> attributes = new HashMap<String, String>();
        attributes.put("required", "true");
        attributes.put(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_INCLUDE_PRIOR_DIAGNOSES_ATTRIBUTE_NAME, "admit");
        attributes.put("selectedDiagnosesTarget", "example-target");

        // Replay
        attributes.put("preferredCodingSource", preferredCodingSource);
        String generatedHtml = encounterDiagnosesTagHandler.getSubstitution(formEntrySession, formSubmissionController, attributes);

        // Verify
        assertTrue(StringUtils.contains(generatedHtml, "preferredCodingSource=\"" + preferredCodingSource + "\""));

    }

    @Test
    public void getSubstitution_shouldAddPreferredCodingSourceWithDefaultValueAttributeOnDiagnosisSearchField() throws Exception {
        // Setup
        Map<String,String> attributes = new HashMap<String, String>();
        attributes.put("required", "true");
        attributes.put(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_INCLUDE_PRIOR_DIAGNOSES_ATTRIBUTE_NAME, "admit");
        attributes.put("selectedDiagnosesTarget", "example-target");

        // Replay
        attributes.put("preferredCodingSource", null);
        String generatedHtml = encounterDiagnosesTagHandler.getSubstitution(formEntrySession, formSubmissionController, attributes);

        // Verify
        assertTrue(StringUtils.contains(generatedHtml, "preferredCodingSource=\"" + CoreAppsConstants.DEFAULT_CODING_SOURCE + "\""));

    }

    @Test
    public void getSubstitution_shouldAddUuidsToDiagnosisSetsAttributeOnDiagnosisSearchFieldGivenDiagnosisUuids() throws Exception {
        // setup
        String diagnosisSetsUuids = GENERAL_AND_SPECIFIED_DIAGNOSIS_SET_UUID + "," + HIV_OPPORTUNISTIC_INFECTION_DIAGNOSIS_SET_UUID;

        Map<String,String> attributes = new HashMap<String, String>();
        attributes.put("required", "true");
        attributes.put(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_INCLUDE_PRIOR_DIAGNOSES_ATTRIBUTE_NAME, "admit");
        attributes.put("selectedDiagnosesTarget", "example-target");
        
        // replay
        attributes.put("diagnosisSets", diagnosisSetsUuids);
        String generatedHtml = encounterDiagnosesTagHandler.getSubstitution(formEntrySession, formSubmissionController, attributes);

        // verify
        assertTrue(StringUtils.contains(generatedHtml, "diagnosisSets=\"" + diagnosisSetsUuids + "\""));
    
    }

    @Test
    public void getSubstitution_shouldAddUuidsToDiagnosisSetsAttributeOnDiagnosisSearchFieldGivenDiagnosisMappings() throws Exception {
        // setup
        String diagnosisSetsUuids = GENERAL_AND_SPECIFIED_DIAGNOSIS_SET_UUID + "," + HIV_OPPORTUNISTIC_INFECTION_DIAGNOSIS_SET_UUID;

        Map<String,String> attributes = new HashMap<String, String>();
        attributes.put("required", "true");
        attributes.put(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_INCLUDE_PRIOR_DIAGNOSES_ATTRIBUTE_NAME, "admit");
        attributes.put("selectedDiagnosesTarget", "example-target");

        // replay
        attributes.put("diagnosisSets", "CIEL:160168,CIEL:160170");
        String generatedHtml = encounterDiagnosesTagHandler.getSubstitution(formEntrySession, formSubmissionController, attributes);

        // verify
        assertTrue(StringUtils.contains(generatedHtml, "diagnosisSets=\"" + diagnosisSetsUuids + "\""));
    }
    
    @Test
    public void getSubstitution_shouldNotAddDiagnosisSetsAttributeOnDiagnosisSearchFieldGivenNullAttribute() throws Exception {
        // setup
    	Map<String,String> attributes = new HashMap<String, String>();
        attributes.put("required", "true");
        attributes.put(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_INCLUDE_PRIOR_DIAGNOSES_ATTRIBUTE_NAME, "admit");
        attributes.put("selectedDiagnosesTarget", "example-target");
    	
        // replay
        attributes.put("diagnosisSets", null);
        String generatedHtml = encounterDiagnosesTagHandler.getSubstitution(formEntrySession, formSubmissionController, attributes);

        // verify
        assertFalse(StringUtils.contains(generatedHtml, "diagnosisSets"));
    }

    @Test(expected = IllegalArgumentException.class)
    public void getSubstitution_shouldThrowWhenDiagnosisSetsAttributeContainsInvalidSetsIds() throws Exception {
    	// setup
    	Map<String,String> attributes = new HashMap<String, String>();
        attributes.put("required", "true");
        attributes.put(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_INCLUDE_PRIOR_DIAGNOSES_ATTRIBUTE_NAME, "admit");
        attributes.put("selectedDiagnosesTarget", "example-target");
        
        // replay
        attributes.put("diagnosisSets", "NON-EXISTING:MAPPING,CIEL:160170");
        encounterDiagnosesTagHandler.getSubstitution(formEntrySession, formSubmissionController, attributes);
    }

    @Test
    public void getSubstitution_shouldAddNullValueCharToDiagnosisSetsAttributeOnDiagnosisSearchField() throws Exception {
        // setup
        String diagnosisSetsUuids = USE_NULL_VALUE;

        Map<String,String> attributes = new HashMap<String, String>();
        attributes.put("required", "true");
        attributes.put(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_INCLUDE_PRIOR_DIAGNOSES_ATTRIBUTE_NAME, "admit");
        attributes.put("selectedDiagnosesTarget", "example-target");

        // replay
        attributes.put("diagnosisSets", "0");
        String generatedHtml = encounterDiagnosesTagHandler.getSubstitution(formEntrySession, formSubmissionController, attributes);

        // verify
        assertTrue(StringUtils.contains(generatedHtml, "diagnosisSets=\"" + diagnosisSetsUuids + "\""));
    }
    
    @Test
    public void getSubstitution_shouldAddDiagnosisConceptSourcesAttributeOnDiagnosisSearchField() throws Exception {
        // setup
        String diagnosisConceptSources = "ICPC,CIEL";
        
        Map<String,String> attributes = new HashMap<String, String>();
        attributes.put("required", "true");
        attributes.put(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_INCLUDE_PRIOR_DIAGNOSES_ATTRIBUTE_NAME, "admit");
        attributes.put("selectedDiagnosesTarget", "example-target");

        // replay
        attributes.put("diagnosisConceptSources", "ICPC, CIEL");
        String generatedHtml = encounterDiagnosesTagHandler.getSubstitution(formEntrySession, formSubmissionController, attributes);

        // verify
        assertTrue(StringUtils.contains(generatedHtml, "diagnosisConceptSources=\"" + diagnosisConceptSources + "\""));
    }

    @Test
    public void getSubstitution_shouldNotAddDiagnosisConceptSourcesAttributeOnDiagnosisSearchFieldGivenNullAttribute() throws Exception {
        // setup
        Map<String,String> attributes = new HashMap<String, String>();
        attributes.put("required", "true");
        attributes.put(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_INCLUDE_PRIOR_DIAGNOSES_ATTRIBUTE_NAME, "admit");
        attributes.put("selectedDiagnosesTarget", "example-target");

        // replay
        attributes.put("diagnosisConceptSources", null);
        String generatedHtml = encounterDiagnosesTagHandler.getSubstitution(formEntrySession, formSubmissionController, attributes);

        // verify
        assertFalse(StringUtils.contains(generatedHtml, "diagnosisConceptSources"));
    }
        
    @Test
    public void getSubstitution_shouldAddDiagnosisConceptClassesAttributeOnDiagnosisSearchField() throws Exception {
        // setup
        String diagnosisConceptClasses = "Diagnosis,Findig";
        
        Map<String,String> attributes = new HashMap<String, String>();
        attributes.put("required", "true");
        attributes.put(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_INCLUDE_PRIOR_DIAGNOSES_ATTRIBUTE_NAME, "admit");
        attributes.put("selectedDiagnosesTarget", "example-target");

        // replay
        attributes.put("diagnosisConceptClasses", "Diagnosis,Findig");
        String generatedHtml = encounterDiagnosesTagHandler.getSubstitution(formEntrySession, formSubmissionController, attributes);
        
        // verify
        assertTrue(StringUtils.contains(generatedHtml, "diagnosisConceptClasses=\"" + diagnosisConceptClasses + "\""));
    }
    
    @Test
    public void getSubstitution_shouldNotAddDiagnosisConceptClassesAttributeOnDiagnosisSearchFieldGivenNullAttribute() throws Exception {
        // setup        
        Map<String,String> attributes = new HashMap<String, String>();
        attributes.put("required", "true");
        attributes.put(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_INCLUDE_PRIOR_DIAGNOSES_ATTRIBUTE_NAME, "admit");
        attributes.put("selectedDiagnosesTarget", "example-target");

        // replay
        attributes.put("diagnosisConceptClasses", null);
        String generatedHtml = encounterDiagnosesTagHandler.getSubstitution(formEntrySession, formSubmissionController, attributes);
        
        // verify
        assertFalse(StringUtils.contains(generatedHtml, "diagnosisConceptClasses"));
    }

    private String renderFragmentHtml(Map<String, Object> fragmentConfig) throws Exception {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("web/module/fragments/diagnosis/encounterDiagnoses.gsp");
        String string = IOUtils.toString(inputStream, UTF_8.toString());

        Template template = new SimpleTemplateEngine(getClass().getClassLoader()).createTemplate(string);
        Map<String, Object> model = new LinkedHashMap<String, Object>();
        Model cfgAsModel = new Model();
        for (String prop : fragmentConfig.keySet()) {
            cfgAsModel.addAttribute(prop, fragmentConfig.get(prop));
        }
        model.put("config", cfgAsModel);
        model.put("ui", uiUtils);
        model.put("jsForExisting", new ArrayList<String>());
        model.put("jsForPrior", new ArrayList<String>());

        return template.make(model).toString();
    }
}
