package org.openmrs.module.coreapps.diagnosisCompatibility;

import java.util.List;

import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.annotation.OpenmrsProfile;
import org.openmrs.api.DiagnosisService;
import org.openmrs.api.EncounterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
@OpenmrsProfile(openmrsPlatformVersion = "2.5.0-SNAPSHOT")
public class DiagnosisServiceCompatibility2_2 implements DiagnosisServiceCompatibility {
	
	@Autowired
	@Qualifier("encounterService")
	EncounterService encounterService;
	
	

	@Autowired
	@Qualifier("diagnosisService")
	DiagnosisService diagnosisService;
	
	

	@Override
	public List<Diagnosis> getDiagnoses(Encounter encounter) throws Exception {
		List<Diagnosis> list = diagnosisService.getDiagnoses(encounter);
		return list;
	}

}
