package org.openmrs.module.coreapps.page.controller.summarydashboard;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.ProgramWorkflow;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.module.coreapps.CoreAppsProperties;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.Redirect;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ProgramEnrollmentsPageController {

    public Object controller(@RequestParam("app") AppDescriptor app,
                             @SpringBean EmrApiProperties emrApiProperties,
                             @SpringBean("coreAppsProperties") CoreAppsProperties coreAppsProperties,
                             @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
                             PageModel model,
                             UiSessionContext sessionContext) {

        if (!Context.hasPrivilege(CoreAppsConstants.PRIVILEGE_SUMMARY_DASHBOARD)) {
            return new Redirect("coreapps", "noAccess", "");
        }

        model.addAttribute("app", app);

        AppContextModel contextModel = sessionContext.generateAppContextModel();
        model.addAttribute("appContextModel", contextModel);

        List<Extension> firstColumnFragments = appFrameworkService.getExtensionsForCurrentUser(app.getId() + ".firstColumnFragments", contextModel);
        Collections.sort(firstColumnFragments);
        model.addAttribute("firstColumnFragments", firstColumnFragments);

        String programUuid = null;
        ObjectNode config = app.getConfig();
        if (config != null) {
            for (Iterator<Map.Entry<String, JsonNode>> fields = config.getFields(); fields.hasNext();) {
                Map.Entry<String, JsonNode> field = fields.next();
                if (StringUtils.equalsIgnoreCase(field.getKey(), "program") ){
                    programUuid = field.getValue().getTextValue();
                }
            }
        }
        Set<ProgramWorkflow> workflows = null;
        List<SimpleObject> simpleObjects = new ArrayList<>();
        if (programUuid != null ) {
            Program program = Context.getProgramWorkflowService().getProgramByUuid(programUuid);
            workflows = program.getWorkflows();
            if (program != null ) {
                Date today = Calendar.getInstance().getTime();
                // retrieve all program enrollments still active as now
                List<PatientProgram> patientPrograms = Context.getProgramWorkflowService().getPatientPrograms(null, program, null, today, today, null, false);
                for (PatientProgram patientProgram : patientPrograms) {
                    Patient patient = patientProgram.getPatient();
                    SimpleObject prgObject = SimpleObject.create(
                            "patientUuid", patient.getUuid(),
                            "patientId", patient.getPatientId(),
                            "personName", patient.getPersonName().getFamilyName() + ", " + patient.getPersonName().getGivenName(),
                            "emrId", patient.getPatientIdentifier(emrApiProperties.getPrimaryIdentifierType()),
                            "dateEnrolled", patientProgram.getDateEnrolled(),
                            "dateCompleted", patientProgram.getDateCompleted()
                    );
                    for(ProgramWorkflow workflow : workflows) {
                        PatientState currentState = patientProgram.getCurrentState(workflow);
                        prgObject.put(workflow.getUuid(), (currentState != null) ? currentState.getState().getConcept().getName(Context.getLocale()) : "");
                    }
                    simpleObjects.add(prgObject);
                }
                model.addAttribute("programEnrollments", simpleObjects);
            }
        }
        model.addAttribute("programWorkflows", workflows);
        model.addAttribute("dashboardUrl", coreAppsProperties.getDashboardUrl());

        return null;
    }
}
