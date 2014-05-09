package org.openmrs.module.coreapps.page.controller.adt;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.emrapi.adt.reporting.query.AwaitingAdmissionVisitQuery;
import org.openmrs.module.reporting.data.patient.definition.PatientDataDefinition;
import org.openmrs.module.reporting.data.visit.definition.VisitDataDefinition;
import org.openmrs.module.reporting.dataset.DataSet;
import org.openmrs.module.reporting.dataset.DataSetUtil;
import org.openmrs.module.reporting.dataset.definition.VisitDataSetDefinition;
import org.openmrs.module.reporting.dataset.definition.service.DataSetDefinitionService;
import org.openmrs.module.reporting.definition.library.AllDefinitionLibraries;
import org.openmrs.module.reporting.evaluation.EvaluationContext;
import org.openmrs.module.reporting.evaluation.EvaluationException;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;

import java.util.Collections;
import java.util.List;

public class AwaitingAdmissionPageController {
    private final Log log = LogFactory.getLog(getClass());

    public void get(PageModel model,
                    @SpringBean AllDefinitionLibraries libraries,
                    @SpringBean DataSetDefinitionService dsdService,
                    @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService) throws EvaluationException {

        EvaluationContext context = new EvaluationContext();
        List<Extension> admissionActions = appFrameworkService.getAllEnabledExtensions("mirebalaisreports.awaitingAdmissionActions");
        Collections.sort(admissionActions);
        model.addAttribute("admissionActions", admissionActions);

        DataSet dataSet = null;

        VisitDataSetDefinition dsd = new VisitDataSetDefinition();
        AwaitingAdmissionVisitQuery query = new AwaitingAdmissionVisitQuery();
        dsd.addRowFilter(query, null);

        dsd.addColumn("patientId", libraries.getDefinition(PatientDataDefinition.class, "reporting.library.patientDataDefinition.builtIn.patientId"), "");
        dsd.addColumn("patientLastName", libraries.getDefinition(PatientDataDefinition.class, "reporting.library.patientDataDefinition.builtIn.preferredName.familyName"), "");
        dsd.addColumn("patientFirstName", libraries.getDefinition(PatientDataDefinition.class, "reporting.library.patientDataDefinition.builtIn.preferredName.givenName"), "");
        dsd.addColumn("primaryIdentifier", libraries.getDefinition(PatientDataDefinition.class, "emrapi.library.patientDataDefinition.primaryIdentifier"), "");
        dsd.addColumn("visitId", libraries.getDefinition(VisitDataDefinition.class, "reporting.library.visitDataDefinition.builtIn.visitId"), "" );
        dsd.addColumn("mostRecentAdmissionRequest", libraries.getDefinition(VisitDataDefinition.class, "emrapi.library.visitDataDefinition.mostRecentAdmissionRequest"), "");

        // add the paper record identifier, if the definition is available (provided by the paper record module)
        PatientDataDefinition paperRecordIdentifierDefinition =  libraries.getDefinition(PatientDataDefinition.class, "paperrecord.library.patientDataDefinition.paperRecordIdentifier");
        if (paperRecordIdentifierDefinition != null) {
            model.addAttribute("paperRecordIdentifierDefinitionAvailable", true);
            dsd.addColumn("paperRecordIdentifier", paperRecordIdentifierDefinition, "");
        }
        else {
            model.addAttribute("paperRecordIdentifierDefinitionAvailable", false);
        }

        dataSet = dsdService.evaluate(dsd, context);
        model.addAttribute("awaitingAdmissionList", DataSetUtil.simplify(dataSet));
    }


}
