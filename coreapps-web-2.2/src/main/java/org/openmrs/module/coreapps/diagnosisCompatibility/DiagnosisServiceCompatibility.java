package org.openmrs.module.coreapps.diagnosisCompatibility;

import java.util.List;

import org.openmrs.Diagnosis;
import org.openmrs.Encounter;

public interface DiagnosisServiceCompatibility {
	
	
	public List<Diagnosis> getDiagnoses(Encounter encounter) throws Exception;

}
