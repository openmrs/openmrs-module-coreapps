/**
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
package org.openmrs.module.coreapps.fragment.controller.visit;

import org.apache.commons.beanutils.PropertyUtils;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptDatatype;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.LocationService;
import org.openmrs.module.appui.TestUiUtils;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisMetadata;
import org.openmrs.module.emrapi.disposition.Disposition;
import org.openmrs.module.emrapi.disposition.DispositionDescriptor;
import org.openmrs.module.emrapi.disposition.DispositionObs;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.emrapi.test.MockMetadataTestUtil;
import org.openmrs.module.emrapi.test.builder.ConceptBuilder;
import org.openmrs.module.emrapi.test.builder.ObsBuilder;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 *
 */
public class ParserEncounterIntoSimpleObjectsTest {
	
	private DiagnosisMetadata diagnosisMetadata;
	
	private DispositionDescriptor dispositionDescriptor;
	
	private EmrApiProperties emrApiProperties;
	
	private EmrConceptService emrConceptService;
	
	private ConceptService conceptService;

    private LocationService locationService;

    private DispositionService dispositionService;
	
	private UiUtils uiUtils;
	
	private Encounter encounter;
	
	private ParserEncounterIntoSimpleObjects parser;
	
	@Before
	public void setUp() throws Exception {
		emrApiProperties = mock(EmrApiProperties.class);
		conceptService = mock(ConceptService.class);
        locationService = mock(LocationService.class);
        dispositionService = mock(DispositionService.class);
		
		MockMetadataTestUtil.setupMockConceptService(conceptService, emrApiProperties);
		
		emrConceptService = mock(EmrConceptService.class);
		
		diagnosisMetadata = MockMetadataTestUtil.setupDiagnosisMetadata(emrApiProperties, conceptService);
		dispositionDescriptor = MockMetadataTestUtil.setupDispositionDescriptor(conceptService);
		when(dispositionService.getDispositionDescriptor()).thenReturn(dispositionDescriptor);

		TestUiUtils testUiUtils = new TestUiUtils();
		testUiUtils.setMockFormattingConcepts(true);
		uiUtils = testUiUtils;
		
		encounter = new Encounter();
		parser = new ParserEncounterIntoSimpleObjects(encounter, uiUtils, emrApiProperties, locationService, dispositionService);
	}
	
	@Test
	public void testParsingDiagnoses() throws Exception {
		encounter.addObs(diagnosisMetadata.buildDiagnosisObsGroup(new Diagnosis(new CodedOrFreeTextAnswer("Random Disease"),
		        Diagnosis.Order.PRIMARY)));
		ParsedObs parsed = parser.parseObservations(Locale.ENGLISH);
		
		assertThat(parsed.getDiagnoses().size(), is(1));
		assertThat(parsed.getDispositions().size(), is(0));
		assertThat(parsed.getObs().size(), is(0));
		assertThat(path(parsed.getDiagnoses(), 0, "question"), is((Object) "coreapps.patientDashBoard.diagnosisQuestion.PRIMARY"));
		assertThat(path(parsed.getDiagnoses(), 0, "answer"),
		    is((Object) "(coreapps.Diagnosis.Certainty.PRESUMED) \"Random Disease\""));
		assertThat(path(parsed.getDiagnoses(), 0, "order"), is((Object) 0));
	}
	
	@Test
	public void testParsingDispositions() throws Exception {
		Concept admit = new ConceptBuilder(null, conceptService.getConceptDatatypeByName("N/A"),
		        conceptService.getConceptClassByName("Misc")).addName("Admit").get();
		when(emrConceptService.getConcept("test:admit")).thenReturn(admit);
		Obs dispositionObs = dispositionDescriptor.buildObsGroup(new Disposition("emrapi.admit", "Admit", "test:admit",
		        Collections.<String> emptyList(), Collections.<DispositionObs> emptyList()), emrConceptService);
		encounter.addObs(doNotGoToServiceToFormatMembers(dispositionObs));
		ParsedObs parsed = parser.parseObservations(Locale.ENGLISH);
		
		assertThat(parsed.getDiagnoses().size(), is(0));
		assertThat(parsed.getDispositions().size(), is(1));
		assertThat(parsed.getObs().size(), is(0));
		assertThat(path(parsed.getDispositions(), 0, "disposition"), is((Object) "Admit"));
		assertThat(path(parsed.getDispositions(), 0, "additionalObs"), is((Object) Collections.emptyList()));
	}

    @Test
    public void testParsingDispositionWithAdmissionLocation() throws Exception {
        Concept admit = new ConceptBuilder(null, conceptService.getConceptDatatypeByName("N/A"),
                conceptService.getConceptClassByName("Misc")).addName("Admit").get();
        when(emrConceptService.getConcept("test:admit")).thenReturn(admit);

        Obs dispositionObs = dispositionDescriptor.buildObsGroup(new Disposition("emrapi.admit", "Admit", "test:admit",
                Collections.<String> emptyList(), Collections.<DispositionObs> emptyList()), emrConceptService);

        Obs admissionLocationObs = new Obs();
        admissionLocationObs.setObsId(100);
        admissionLocationObs.setConcept(dispositionDescriptor.getAdmissionLocationConcept());
        admissionLocationObs.setValueText("3");
        dispositionObs.addGroupMember(admissionLocationObs);

        Location admissionLocation = new Location();
        admissionLocation.setName("Outpatient clinic");
        when(locationService.getLocation(3)).thenReturn(admissionLocation);

        encounter.addObs(doNotGoToServiceToFormatMembers(dispositionObs));
        ParsedObs parsed = parser.parseObservations(Locale.ENGLISH);

        SimpleObject expectedAdmissionLocationObject = SimpleObject.create("obsId", admissionLocationObs.getObsId());
        expectedAdmissionLocationObject.put("question","Admission location");
        expectedAdmissionLocationObject.put("answer","Outpatient clinic");

        List<SimpleObject> expectedAdditionalObsList = new ArrayList<SimpleObject>();
        expectedAdditionalObsList.add(expectedAdmissionLocationObject);

        assertThat(parsed.getDiagnoses().size(), is(0));
        assertThat(parsed.getDispositions().size(), is(1));
        assertThat(parsed.getObs().size(), is(0));
        assertThat(path(parsed.getDispositions(), 0, "disposition"), is((Object) "Admit"));
        assertThat(path(parsed.getDispositions(), 0, "additionalObs"), is((Object) expectedAdditionalObsList));
    }

    @Test
    public void testParsingDispositionWithTransferLocation() throws Exception {
        Concept admit = new ConceptBuilder(null, conceptService.getConceptDatatypeByName("N/A"),
                conceptService.getConceptClassByName("Misc")).addName("Transfer").get();
        when(emrConceptService.getConcept("test:transfer")).thenReturn(admit);

        Obs dispositionObs = dispositionDescriptor.buildObsGroup(new Disposition("emrapi.transfer", "Transfer", "test:transfer",
                Collections.<String> emptyList(), Collections.<DispositionObs> emptyList()), emrConceptService);

        Obs transferLocationObs = new Obs();
        transferLocationObs.setObsId(100);
        transferLocationObs.setConcept(dispositionDescriptor.getInternalTransferLocationConcept());
        transferLocationObs.setValueText("3");
        dispositionObs.addGroupMember(transferLocationObs);

        Location transferLocation = new Location();
        transferLocation.setName("Outpatient clinic");
        when(locationService.getLocation(3)).thenReturn(transferLocation);

        encounter.addObs(doNotGoToServiceToFormatMembers(dispositionObs));
        ParsedObs parsed = parser.parseObservations(Locale.ENGLISH);

        SimpleObject expectedTransferLocationObject = SimpleObject.create("obsId", transferLocationObs.getObsId());
        expectedTransferLocationObject.put("question", "Transfer location");
        expectedTransferLocationObject.put("answer", "Outpatient clinic");

        List<SimpleObject> expectedAdditionalObsList = new ArrayList<SimpleObject>();
        expectedAdditionalObsList.add(expectedTransferLocationObject);

        assertThat(parsed.getDiagnoses().size(), is(0));
        assertThat(parsed.getDispositions().size(), is(1));
        assertThat(parsed.getObs().size(), is(0));
        assertThat(path(parsed.getDispositions(), 0, "disposition"), is((Object) "Transfer"));
        assertThat(path(parsed.getDispositions(), 0, "additionalObs"), is((Object) expectedAdditionalObsList));
    }

    @Test
    public void testParsingDispositionWithDateOfDeath() throws Exception {
        Concept admit = new ConceptBuilder(null, conceptService.getConceptDatatypeByName("N/A"),
                conceptService.getConceptClassByName("Misc")).addName("Death").get();
        when(emrConceptService.getConcept("test:death")).thenReturn(admit);

        Obs dispositionObs = dispositionDescriptor.buildObsGroup(new Disposition("emrapi.death", "Death", "test:death",
                Collections.<String> emptyList(), Collections.<DispositionObs> emptyList()), emrConceptService);

        Date dateOfDeath = new DateTime(2012,2,20,10,10,10).toDate();
        Obs dateOfDeathObs = new Obs();
        dateOfDeathObs.setObsId(100);
        dateOfDeathObs.setConcept(dispositionDescriptor.getDateOfDeathConcept());
        dateOfDeathObs.setValueDate(dateOfDeath);
        dispositionObs.addGroupMember(dateOfDeathObs);

        encounter.addObs(doNotGoToServiceToFormatMembers(dispositionObs));
        ParsedObs parsed = parser.parseObservations(Locale.ENGLISH);

        SimpleObject expectedAdmissionLocationObject = SimpleObject.create("obsId", dateOfDeathObs.getObsId());
        expectedAdmissionLocationObject.put("question","Date of death");
        expectedAdmissionLocationObject.put("answer", "20 Feb 2012 10:10 AM");

        List<SimpleObject> expectedAdditionalObsList = new ArrayList<SimpleObject>();
        expectedAdditionalObsList.add(expectedAdmissionLocationObject);

        assertThat(parsed.getDiagnoses().size(), is(0));
        assertThat(parsed.getDispositions().size(), is(1));
        assertThat(parsed.getObs().size(), is(0));
        assertThat(path(parsed.getDispositions(), 0, "disposition"), is((Object) "Death"));
        assertThat(path(parsed.getDispositions(), 0, "additionalObs"), is((Object) expectedAdditionalObsList));
    }

	private Obs doNotGoToServiceToFormatMembers(Obs obsGroup) {
		Set<Obs> replacements = new HashSet<Obs>();
		for (Obs member : obsGroup.getGroupMembers()) {
			replacements.add(new DoNotGoToServiceWhenFormatting(member));
		}
		
		obsGroup.setGroupMembers(replacements);
		return obsGroup;
	}
	
	@Test
	public void testParsingSimpleObs() throws Exception {
		ConceptDatatype textDatatype = conceptService.getConceptDatatypeByName("Text");
		ConceptClass misc = conceptService.getConceptClassByName("Misc");
		
		String consultNote = "Consult note"; // intentionally the same as what will result from capitalizeFirstLetter(consultNote)
		String valueText = "Patient is here for blah blah blah.";
		
		Concept consultComments = new ConceptBuilder(conceptService, textDatatype, misc).addName(consultNote).get();
		
		encounter.addObs(new ObsBuilder().setConcept(consultComments).setValue(valueText).get());
		ParsedObs parsed = parser.parseObservations(Locale.ENGLISH);
		
		assertThat(parsed.getDiagnoses().size(), is(0));
		assertThat(parsed.getDispositions().size(), is(0));
		assertThat(parsed.getObs().size(), is(1));
		assertThat(path(parsed.getObs(), 0, "question"), is((Object) consultNote));
		assertThat(path(parsed.getObs(), 0, "answer"), is((Object) valueText));
	}

    @Test
    public void testParsingObsWithLocationAnswer() throws Exception {
        ConceptDatatype textDatatype = conceptService.getConceptDatatypeByName("Text");
        ConceptClass misc = conceptService.getConceptClassByName("Misc");

        Location xanadu = new Location();
        xanadu.setName("Xanadu");
        when(locationService.getLocation(2)).thenReturn(xanadu);

        Concept someLocation = new ConceptBuilder(conceptService, textDatatype, misc).addName("Some location").get();

        encounter.addObs(new ObsBuilder().setConcept(someLocation).setValue("2").setComment("org.openmrs.Location").get());
        ParsedObs parsed = parser.parseObservations(Locale.ENGLISH);
;
        assertThat(parsed.getObs().size(), is(1));
        assertThat(path(parsed.getObs(), 0, "question"), is((Object) "Some location"));
        assertThat(path(parsed.getObs(), 0, "answer"), is((Object) "Xanadu"));
    }

	private Object path(Object simpleObjectOrList, Object... paths) {
		Object current = simpleObjectOrList;
		for (Object path : paths) {
			if (path instanceof Integer) {
				current = ((List) current).get((Integer) path);
			} else {
				try {
					current = PropertyUtils.getProperty(current, (String) path);
				}
				catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		}
		return current;
	}
	
	private class DoNotGoToServiceWhenFormatting extends Obs {
		
		private Obs obs;
		
		public DoNotGoToServiceWhenFormatting(Obs obs) {
			this.obs = obs;
		}

        @Override
        public Integer getId() {
            return obs.getId();
        }

        @Override
        public Integer getObsId() {
            return obs.getObsId();
        }

        @Override
        public String getValueText() {
            return obs.getValueText();
        }

		@Override
		public Concept getConcept() {
			return obs.getConcept();
		}

		@Override
		public String getValueAsString(Locale locale) {
			if (obs.getValueCoded() != null) {
				return obs.getValueCoded().getNames(false).iterator().next().getName();
			} else if (obs.getValueDate() != null) {
                return new SimpleDateFormat("dd MMM yyyy hh:mm a", locale).format(obs.getValueDate());
            }
            else {
				return obs.getValueAsString(locale);
			}
		}
	}
}
