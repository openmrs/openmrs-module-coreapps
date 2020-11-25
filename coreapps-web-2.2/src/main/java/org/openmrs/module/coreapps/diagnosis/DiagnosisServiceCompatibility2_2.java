package org.openmrs.module.coreapps.diagnosis;

import java.util.ArrayList;
import java.util.List;

import org.openmrs.CodedOrFreeText;
import org.openmrs.ConditionVerificationStatus;
import org.openmrs.Encounter;
import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;

public class DiagnosisServiceCompatibility2_2 implements DiagnosisServiceCompatibility {

	@Override
	public List<Diagnosis> getPrimaryDiagnosis(Encounter encounter) {
		List<Diagnosis> diagnoses = new ArrayList<Diagnosis>();
		for(org.openmrs.Diagnosis coreDiagnosis:Context.getDiagnosisService().getPrimaryDiagnoses(encounter)) {
			Diagnosis diagnosis = new Diagnosis();
			CodedOrFreeText coded = coreDiagnosis.getDiagnosis();
			diagnosis.setDiagnosis(new CodedOrFreeTextAnswer(coded.getCoded(), coded.getSpecificName(), coded.getNonCoded()));
			diagnosis.setCertainty(coreDiagnosis.getCertainty() == ConditionVerificationStatus.CONFIRMED ? Diagnosis.Certainty.CONFIRMED : Diagnosis.Certainty.PRESUMED);
			diagnosis.setOrder(coreDiagnosis.getRank() == 1 ? Diagnosis.Order.PRIMARY : Diagnosis.Order.SECONDARY);
			diagnoses.add(diagnosis);
		}
		
		return diagnoses;
	}

}
