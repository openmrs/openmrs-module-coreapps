package org.openmrs.module.coreapps.diagnosisCompatibility;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Encounter;
import org.openmrs.api.EncounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;


public class DiagnosisServiceCompatibility2_2Test {
	
	
	@Autowired
	@Qualifier("encounterService")
	EncounterService encounterService;
	
	/**
	 * @see DiagnosisServiceCompatibility2_2#getDiagnoses(Encounter)
	 */
	
	@Test
	public void getDiagnoses_shouldGetDiagnosesOfTheProvidedEncounter() {
	Encounter encounter = encounterService.getEncounter(3);
	
	Assert.assertNotNull(encounter);
	
	}

}
