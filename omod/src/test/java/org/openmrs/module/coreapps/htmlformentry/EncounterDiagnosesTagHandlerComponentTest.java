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

import static org.hamcrest.Matchers.is;
import static org.hamcrest.collection.IsIterableContainingInAnyOrder.containsInAnyOrder;
import static org.junit.Assert.assertThat;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.hamcrest.Matcher;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
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
import org.openmrs.module.htmlformentry.HtmlFormEntryService;
import org.openmrs.module.htmlformentry.RegressionTestHelper;
import org.openmrs.ui.framework.page.PageAction;
import org.openmrs.web.test.BaseModuleWebContextSensitiveTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;

/**
 *
 */
public class EncounterDiagnosesTagHandlerComponentTest extends BaseModuleWebContextSensitiveTest {

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
        ContextSensitiveMetadataTestUtils.setupDiagnosisMetadata(conceptService, emrApiProperties);
        EncounterDiagnosesTagHandler encounterDiagnosesTagHandler = CoreAppsActivator.setupEncounterDiagnosesTagHandler(conceptService, adtService, Context.getRegisteredComponent("emrApiProperties", EmrApiProperties.class), null);
        Context.getService(HtmlFormEntryService.class).addHandler(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_NAME, encounterDiagnosesTagHandler);
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

}
