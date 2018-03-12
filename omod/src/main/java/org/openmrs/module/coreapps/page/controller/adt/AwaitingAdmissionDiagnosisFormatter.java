package org.openmrs.module.coreapps.page.controller.adt;

import org.openmrs.api.context.Context;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.reporting.data.converter.DataConverter;

import java.util.List;

public class AwaitingAdmissionDiagnosisFormatter implements DataConverter {

    @Override
    public Object convert(Object o) {
        List<Diagnosis> diagnoses = (List<Diagnosis>) o;
        StringBuffer output = new StringBuffer();
        String prefix = "";

        for (Diagnosis diagnosis : diagnoses) {
            if (diagnosis.getDiagnosis() != null) {
                output.append(prefix);
                prefix = ", ";
                output.append(diagnosis.getDiagnosis().formatWithoutSpecificAnswer(Context.getLocale()));
            }
        }

        return output.toString();
    }

    @Override
    public Class<?> getInputDataType() {
        return List.class;
    }

    @Override
    public Class<?> getDataType() {
        return String.class;
    }
}
