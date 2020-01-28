package org.openmrs.module.htmlformentry;

import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.openmrs.CodedOrFreeText;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.module.coreapps.htmlformentry.EncounterDiagnosesElement;
import org.openmrs.module.htmlformentry.FormEntryContext.Mode;
import org.openmrs.test.BaseContextMockTest;

public class EncounterDiagnosesElementTest extends BaseContextMockTest {

    @Mock
	FormEntryContext context;
    
    @Mock
    Encounter existingEncounter;
    
    @Before
	public void setup() {
		when(context.getExistingEncounter()).thenReturn(existingEncounter);
		when(context.getMode()).thenReturn(Mode.EDIT);
		
	}
	
	@Test
	public void testGetExistingDiagnoses() {
		// Setup
		when(existingEncounter.getDiagnoses()).thenReturn(
                new LinkedHashSet<Diagnosis>(Arrays.asList(new Diagnosis(existingEncounter, new CodedOrFreeText(null, null, "Asthma"), null, null, null), 
                		new Diagnosis(existingEncounter, new CodedOrFreeText(null, null, "Malaria"), null, null, null))));

		// Replay
		EncounterDiagnosesElement element = new EncounterDiagnosesElement();
		Set<Diagnosis> existingDiangnoses = element.getExistingDiagnoses(context);
		
		// Verify
		Assert.assertEquals(2, existingDiangnoses.size());
		Assert.assertEquals("Asthma", ((Diagnosis) existingDiangnoses.toArray()[0]).getDiagnosis().getNonCoded());
		Assert.assertEquals("Malaria", ((Diagnosis) existingDiangnoses.toArray()[1]).getDiagnosis().getNonCoded());

	}
}
