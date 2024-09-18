package org.openmrs.module.coreapps.page.controller.summarydashboard;

import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.module.coreapps.CoreAppsProperties;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.Redirect;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

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

        List<Extension> includeFragments = appFrameworkService.getExtensionsForCurrentUser(app.getId() + ".includeFragments", contextModel);
        Collections.sort(includeFragments);
        model.addAttribute("includeFragments", includeFragments);

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
        if (programUuid != null ) {
            Program program = Context.getProgramWorkflowService().getProgramByUuid(programUuid);
            if (program != null ) {
                Date today = Calendar.getInstance().getTime();
                // retrieve all program enrollments still active as now
                List<PatientProgram> patientPrograms = Context.getProgramWorkflowService().getPatientPrograms(null, program, null, today, today, null, false);
                model.addAttribute("programEnrollments", patientPrograms);
            }
        }
        model.addAttribute("dashboardUrl", coreAppsProperties.getDashboardUrl());
        model.addAttribute("primaryIdentifierType", emrApiProperties.getPrimaryIdentifierType());
        return null;
    }
}
