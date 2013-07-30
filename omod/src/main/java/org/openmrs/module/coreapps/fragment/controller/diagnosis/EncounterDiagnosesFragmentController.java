package org.openmrs.module.coreapps.fragment.controller.diagnosis;

import org.openmrs.ConceptSearchResult;
import org.openmrs.api.context.Context;
import org.openmrs.module.coreapps.fragment.controller.DiagnosesFragmentController;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 *
 */
public class EncounterDiagnosesFragmentController {

    public void controller(UiUtils ui,
                           @FragmentParam(value = "existingDiagnoses", required = false) List<Diagnosis> existingDiagnoses,
                           FragmentModel model) throws Exception {

        // to ensure we have the exact same json format as search results, borrow the simplify method from here
        DiagnosesFragmentController diagnosesFragmentController = new DiagnosesFragmentController();

        List<String> jsForExisting = new ArrayList<String>();
        if (existingDiagnoses != null) {
            Collections.sort(existingDiagnoses, new Comparator<Diagnosis>() {
                @Override
                public int compare(Diagnosis left, Diagnosis right) {
                    return left.getOrder().compareTo(right.getOrder());
                }
            });
            for (Diagnosis d : existingDiagnoses) {
                CodedOrFreeTextAnswer diagnosis = d.getDiagnosis();
                String jsDiagnosis;
                if (diagnosis.getNonCodedAnswer() != null) {
                    jsDiagnosis = "'" + ui.escapeJs(diagnosis.getNonCodedAnswer()) + "'";
                } else {
                    ConceptSearchResult csr = new ConceptSearchResult(null, diagnosis.getCodedAnswer(), diagnosis.getSpecificCodedAnswer());
                    SimpleObject simple = diagnosesFragmentController.simplify(csr, ui, Context.getLocale());
                    jsDiagnosis = simple.toJson();
                }
                jsForExisting.add("{ diagnosis: diagnoses.CodedOrFreeTextConceptAnswer(" + jsDiagnosis + "), confirmed: " + (d.getCertainty().equals(Diagnosis.Certainty.CONFIRMED)) + ", primary: " + (d.getOrder().equals(Diagnosis.Order.PRIMARY)) + ", existingObs: " + d.getExistingObs().getId() + " }");
            }
        }

        model.addAttribute("jsForExisting", jsForExisting);
    }

}
