package org.openmrs.module.coreapps.diagnosisCompatibility;



import java.util.Calendar;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import static org.hamcrest.Matchers.hasProperty;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.openmrs.CodedOrFreeText;
import org.openmrs.Concept;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.Patient;
import org.openmrs.api.DiagnosisService;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.Matchers.containsInAnyOrder;
import org.openmrs.api.EncounterService;
import org.openmrs.test.jupiter.BaseModuleContextSensitiveTest;
import org.openmrs.ui.framework.SimpleObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


public class DiagnosisServiceCompatibility2_2Test  extends BaseModuleContextSensitiveTest {
	
	@Autowired
	@Qualifier("encounterService")
	private EncounterService encounterService;
	
	@Autowired
	private DiagnosisServiceCompatibility2_2 diagnosisServiceCompatibility;
	@Autowired
	@Qualifier("diagnosisService")
	private DiagnosisService diagnosisService;
	
	protected static final String ENCOUNTER_DATA_XML = "org/openmrs/module/coreapps/Encounter.xml";
	
	/**
	 * @throws Exception 
	 * @see DiagnosisServiceCompatibility2_2#getDiagnoses(Encounter)
	 */
	@Test
	public void getDiagnoses_shouldGetDiagnosesOfTheProvidedEncounter() throws Exception {
	
		executeDataSet(ENCOUNTER_DATA_XML);
		
	    Encounter encounter  = encounterService.getEncounter(6);
	    List<SimpleObject> lists = diagnosisServiceCompatibility.getPrimaryDiagnosesAsSimpleObjects(encounter);
	    
		Assert.assertEquals(1,lists.size());		
	}
	
}
