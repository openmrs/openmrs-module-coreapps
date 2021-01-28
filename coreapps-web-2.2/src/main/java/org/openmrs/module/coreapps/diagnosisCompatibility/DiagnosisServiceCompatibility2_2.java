package org.openmrs.module.coreapps.diagnosisCompatibility;

import java.util.ArrayList;
import java.util.List;
import org.openmrs.Diagnosis;
import org.openmrs.Encounter;
import org.openmrs.api.DiagnosisService;
import org.openmrs.ui.framework.SimpleObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

@Component
public class DiagnosisServiceCompatibility2_2   {
	
	@Autowired
	@Qualifier("diagnosisService")
	private DiagnosisService diagnosisService;
	
	
	public List<SimpleObject> getPrimaryDiagnosesAsSimpleObjects(Encounter encounter) throws Exception {
		List<Diagnosis> list = diagnosisService.getDiagnoses(encounter);
		List<SimpleObject> results = new ArrayList<SimpleObject>(list.size());
		for (Diagnosis diagnosis : list) {
			SimpleObject simpleObject = new SimpleObject();
			simpleObject.put("answer",diagnosis.getDiagnosis());
			results.add(simpleObject);
		}
		return results;
	}

}
