package org.openmrs.module.htmlformentry;

import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.openmrs.CodedOrFreeText;
import org.openmrs.Concept;
import org.openmrs.ConceptMap;
import org.openmrs.ConceptMapType;
import org.openmrs.ConceptName;
import org.openmrs.ConceptReferenceTerm;
import org.openmrs.ConceptSource;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.coreapps.htmlformentry.EncounterDiagnosesElement;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.htmlformentry.FormEntryContext.Mode;
import org.openmrs.ui.framework.UiUtils;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

@RunWith(PowerMockRunner.class)
@PrepareForTest(Context.class)
public class EncounterDiagnosesElementTest {
	
	private Concept concept;
	
	private ConceptSource source;
	
	private String sourceName;

    @Mock
	FormEntryContext context;
    
    @Mock
    Encounter existingEncounter;
    
    @Mock
    ConceptService conceptService;
    
    @Mock
    private EmrApiProperties emrApiProperties;
    
    @Before
	public void setup() {
    	PowerMockito.mockStatic(Context.class);
    	when(context.getExistingEncounter()).thenReturn(existingEncounter);
    	when(Context.getConceptService()).thenReturn(conceptService);
	}

	@Test
	public void testGetExistingDiagnoses() {
		// Setup
		Diagnosis asthmaDiagnosis = new Diagnosis(existingEncounter, new CodedOrFreeText(null, null, "Asthma"), null, null, null);
		Diagnosis malariaDiagnosis = new Diagnosis(existingEncounter, new CodedOrFreeText(null, null, "Malaria"), null, null, null);
		Diagnosis voidedEpilepsyDiagnosis = new Diagnosis(existingEncounter, new CodedOrFreeText(null, null, "Epilepsy"), null, null, null);
		voidedEpilepsyDiagnosis.setVoided(true);
		when(context.getMode()).thenReturn(Mode.EDIT);
		when(existingEncounter.getDiagnoses()).thenReturn(
				new LinkedHashSet<Diagnosis>(Arrays.asList(asthmaDiagnosis, malariaDiagnosis, voidedEpilepsyDiagnosis)));

		// Replay
		EncounterDiagnosesElement element = new EncounterDiagnosesElement();
		Set<Diagnosis> existingDiagnoses = element.getExistingDiagnoses(context);

		// Verify
		Assert.assertEquals(2, existingDiagnoses.size());

		int asthmaDiagnosisCount = 0;
		int malariaDiagnosisCount = 0;
		int voidedEpilepsyDiagnosisCount = 0;
		for (Diagnosis diagnosis : existingDiagnoses) {
			if ("Asthma".equals(diagnosis.getDiagnosis().getNonCoded())) {
				asthmaDiagnosisCount++;
			}
			if ("Malaria".equals(diagnosis.getDiagnosis().getNonCoded())) {
				malariaDiagnosisCount++;
			}
			if ("Epilepsy".equals(diagnosis.getDiagnosis().getNonCoded())) {
				voidedEpilepsyDiagnosisCount++;
			}
		}
		Assert.assertEquals(asthmaDiagnosisCount, 1);
		Assert.assertEquals(malariaDiagnosisCount, 1);
		Assert.assertEquals(voidedEpilepsyDiagnosisCount, 0);
	}
	
	@Test
	public void generateHtml_shouldCodeDiagnosisUsingPreferredCodingSource() {
		// Setup
		String code = "Pref-code-1234";
		sourceName = "Preferred-Coding-Source";
		setupDiagnosis(sourceName, code);
		EncounterDiagnosesElement element = new EncounterDiagnosesElement();
		element.setUiUtils(new BasicUiUtils());
		element.setPreferredCodingSource(sourceName);
		
		when(context.getMode()).thenReturn(Mode.VIEW);
		when(existingEncounter.getDiagnoses()).thenReturn(
                new LinkedHashSet<Diagnosis>(Arrays.asList(new Diagnosis(existingEncounter, new CodedOrFreeText(concept, null, null), ConditionVerificationStatus.CONFIRMED, null, null))));
		when(conceptService.getConceptSourceByName(source.getName())).thenReturn(source);
		
		// Replay
		String generatedHtml = element.generateHtml(context);
		
		// Verify
		Mockito.verify(conceptService, Mockito.times(1)).getConceptSourceByName(source.getName());
		Assert.assertEquals("<p>"
		                    + "<small>coreapps.patientDashBoard.diagnosisQuestion.SECONDARY</small>"
		                    + "<span>(coreapps.Diagnosis.Certainty.CONFIRMED) Malaria [Pref-code-1234]</span>"
		                    + "</p>", generatedHtml);
	}
	
	@Test
	public void generateHtml_shouldCodeDiagnosisEmrApiPropertiesDiagnosisSources() {
		// Setup
		String code = "ICD-WHO";
		sourceName = "EmrApi-source";
		setupDiagnosis(sourceName, code);
		EncounterDiagnosesElement element = new EncounterDiagnosesElement();
		element.setUiUtils(new BasicUiUtils());
		element.setEmrApiProperties(emrApiProperties);
		
		when(context.getMode()).thenReturn(Mode.VIEW);
		when(existingEncounter.getDiagnoses()).thenReturn(
                new LinkedHashSet<Diagnosis>(Arrays.asList(new Diagnosis(existingEncounter, new CodedOrFreeText(concept, null, null), ConditionVerificationStatus.CONFIRMED, null, null))));
		when(emrApiProperties.getConceptSourcesForDiagnosisSearch()).thenReturn(Arrays.asList(source));
		
		// Replay
		element.setPreferredCodingSource(null);
		String generatedHtml = element.generateHtml(context);
		
		// Verify
		Mockito.verify(emrApiProperties, Mockito.times(1)).getConceptSourcesForDiagnosisSearch();
		Assert.assertEquals("<p>"
		                    + "<small>coreapps.patientDashBoard.diagnosisQuestion.SECONDARY</small>"
		                    + "<span>(coreapps.Diagnosis.Certainty.CONFIRMED) Malaria [ICD-WHO]</span>"
		                    + "</p>", generatedHtml);
	}
	
	private void setupDiagnosis(String sourceName, String code) {
		
		source = new ConceptSource();
		source.setName(sourceName);
		
		ConceptReferenceTerm referenceTerm = new ConceptReferenceTerm();
		referenceTerm.setConceptSource(source);
		referenceTerm.setCode(code);

		ConceptMapType mapType = new ConceptMapType();
		mapType.setName("Occurrence");
		mapType.setUuid(EmrApiConstants.SAME_AS_CONCEPT_MAP_TYPE_UUID);
		
		ConceptMap conceptMap = new ConceptMap();
		conceptMap.setConceptReferenceTerm(referenceTerm);
		conceptMap.setConceptMapType(mapType);
		
		ConceptName conceptName = new ConceptName();
		concept = new Concept();
		concept.addConceptMapping(conceptMap);
		concept.addName(conceptName);
		
		conceptName.setConcept(concept);
		conceptName.setName("Malaria");
		conceptName.setLocalePreferred(true);
		conceptName.setLocale(Locale.ENGLISH);

	}
	
	private class BasicUiUtils extends UiUtils {

		public BasicUiUtils() {
			super();
		}

		@Override
		public String message(String property, Object... args) {
			return property;
		}
		
		@Override
		public Locale getLocale() {
			return Locale.ENGLISH;
		}
	}
}
