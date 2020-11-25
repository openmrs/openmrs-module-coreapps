package org.openmrs.module.coreapps.diagnosis;


import java.util.List;
import org.openmrs.Encounter;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;

public interface DiagnosisServiceCompatibility  {
	
 List<Diagnosis> getPrimaryDiagnosis(Encounter encounter);

}
