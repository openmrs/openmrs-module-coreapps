package org.openmrs.module.coreapps.diagnosis;


import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.Location;
import org.openmrs.Patient;


public class DiagnosisServiceCompatibilityTest {
	
	@Test
	public void getPrimaryDiagnosis_shouldReturnNotNull() {
		
	Encounter encounter = new Encounter();
    
	Date date = new Date();
	Location location = new Location(1);
	Patient patient = new Patient(1);
	
	Diagnosis diagnosis = new Diagnosis();
	diagnosis.setDiagnosisId(2);
	
	Set<Diagnosis> diag = new HashSet<Diagnosis>();
	diag.add(diagnosis);
	
	encounter.setEncounterDatetime(date);
	encounter.setLocation(location);
	encounter.setPatient(patient);
	DiagnosisServiceCompatibility2_2 diagnosisServiceCompatibility = new DiagnosisServiceCompatibility2_2();

	Assert.assertEquals(1,diagnosisServiceCompatibility.getPrimaryDiagnoses(encounter).size());	
		
	}

}
