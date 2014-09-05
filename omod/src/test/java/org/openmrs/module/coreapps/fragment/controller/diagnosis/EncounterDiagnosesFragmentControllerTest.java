package org.openmrs.module.coreapps.fragment.controller.diagnosis;

import org.junit.Test;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.Obs;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.TestUiUtils;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;

/**
 *
 */
public class EncounterDiagnosesFragmentControllerTest {

    @Test
    public void testController_createdProperExistingDiagnoses() throws Exception {
        EncounterDiagnosesFragmentController controller = new EncounterDiagnosesFragmentController();

        ConceptName specificName = new ConceptName("Specific Name", Context.getLocale());
        specificName.setId(1);
        specificName.setLocalePreferred(true);
        Concept conceptForSpecificName = new Concept();
        conceptForSpecificName.setId(2);
        conceptForSpecificName.addName(specificName);

        Concept nonSpecificConcept = new Concept();
        nonSpecificConcept.setId(3);
        ConceptName nonSpecificConceptName = new ConceptName("Non-specific concept", Context.getLocale());
        nonSpecificConceptName.setId(4);
        nonSpecificConceptName.setLocalePreferred(true);
        nonSpecificConcept.addName(nonSpecificConceptName);

        List<Diagnosis> diagnoses = new ArrayList<Diagnosis>();
        diagnoses.add(new Diagnosis(new CodedOrFreeTextAnswer(specificName), Diagnosis.Order.PRIMARY));
        diagnoses.add(new Diagnosis(new CodedOrFreeTextAnswer(nonSpecificConcept), Diagnosis.Order.SECONDARY));
        diagnoses.add(new Diagnosis(new CodedOrFreeTextAnswer("Unknown Disease"), Diagnosis.Order.SECONDARY));

        for (Diagnosis d : diagnoses) {
            d.setExistingObs(new Obs());
        }

        FragmentModel model = new FragmentModel();
        controller.controller(new TestUiUtils(), diagnoses, null, model);

        assertThat(((List<String>) model.getAttribute("jsForPrior")).size(), is(0));

        List<String> jsForExisting = (List<String>) model.getAttribute("jsForExisting");

        assertThat(jsForExisting.get(0), is("{ diagnosis: diagnoses.CodedOrFreeTextConceptAnswer({\"word\":null,\"conceptName\":{\"id\":1,\"uuid\":\"" + specificName.getUuid() + "\",\"conceptNameType\":\"FULLY_SPECIFIED\",\"name\":\"Specific Name\"},\"concept\":{\"id\":2,\"uuid\":\"" + conceptForSpecificName.getUuid() + "\",\"conceptMappings\":[],\"preferredName\":\"Specific Name\"}}), confirmed: false, primary: true, existingObs: null }"));
        assertThat(jsForExisting.get(1), is("{ diagnosis: diagnoses.CodedOrFreeTextConceptAnswer({\"word\":null,\"conceptName\":null,\"concept\":{\"id\":3,\"uuid\":\"" + nonSpecificConcept.getUuid() + "\",\"conceptMappings\":[],\"preferredName\":\"Non-specific concept\"}}), confirmed: false, primary: false, existingObs: null }"));
        assertThat(jsForExisting.get(2), is("{ diagnosis: diagnoses.CodedOrFreeTextConceptAnswer('Unknown Disease'), confirmed: false, primary: false, existingObs: null }"));
    }

    @Test
    public void testController_createdProperPriorDiagnoses() throws Exception {
        EncounterDiagnosesFragmentController controller = new EncounterDiagnosesFragmentController();

        ConceptName specificName = new ConceptName("Specific Name", Context.getLocale());
        specificName.setId(1);
        specificName.setLocalePreferred(true);
        Concept conceptForSpecificName = new Concept();
        conceptForSpecificName.setId(2);
        conceptForSpecificName.addName(specificName);

        Concept nonSpecificConcept = new Concept();
        nonSpecificConcept.setId(3);
        ConceptName nonSpecificConceptName = new ConceptName("Non-specific concept", Context.getLocale());
        nonSpecificConceptName.setId(4);
        nonSpecificConceptName.setLocalePreferred(true);
        nonSpecificConcept.addName(nonSpecificConceptName);

        List<Diagnosis> diagnoses = new ArrayList<Diagnosis>();
        diagnoses.add(new Diagnosis(new CodedOrFreeTextAnswer(specificName), Diagnosis.Order.PRIMARY));
        diagnoses.add(new Diagnosis(new CodedOrFreeTextAnswer(nonSpecificConcept), Diagnosis.Order.SECONDARY));
        diagnoses.add(new Diagnosis(new CodedOrFreeTextAnswer("Unknown Disease"), Diagnosis.Order.SECONDARY));

        for (Diagnosis d : diagnoses) {
            d.setExistingObs(new Obs());
        }


        FragmentModel model = new FragmentModel();
        controller.controller(new TestUiUtils(), null, diagnoses, model);

        assertThat(((List<String>) model.getAttribute("jsForExisting")).size(), is(0));

        List<String> jsForPrior = (List<String>) model.getAttribute("jsForPrior");

        assertThat(jsForPrior.get(0), is("{ diagnosis: diagnoses.CodedOrFreeTextConceptAnswer({\"word\":null,\"conceptName\":{\"id\":1,\"uuid\":\"" + specificName.getUuid() + "\",\"conceptNameType\":\"FULLY_SPECIFIED\",\"name\":\"Specific Name\"},\"concept\":{\"id\":2,\"uuid\":\"" + conceptForSpecificName.getUuid() + "\",\"conceptMappings\":[],\"preferredName\":\"Specific Name\"}}), confirmed: false, primary: true, existingObs: null }"));
        assertThat(jsForPrior.get(1), is("{ diagnosis: diagnoses.CodedOrFreeTextConceptAnswer({\"word\":null,\"conceptName\":null,\"concept\":{\"id\":3,\"uuid\":\"" + nonSpecificConcept.getUuid() + "\",\"conceptMappings\":[],\"preferredName\":\"Non-specific concept\"}}), confirmed: false, primary: false, existingObs: null }"));
        assertThat(jsForPrior.get(2), is("{ diagnosis: diagnoses.CodedOrFreeTextConceptAnswer('Unknown Disease'), confirmed: false, primary: false, existingObs: null }"));
    }


}
