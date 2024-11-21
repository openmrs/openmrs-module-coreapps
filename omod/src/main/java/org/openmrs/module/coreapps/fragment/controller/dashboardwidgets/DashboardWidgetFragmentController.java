package org.openmrs.module.coreapps.fragment.controller.dashboardwidgets;

import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.ui.framework.UiFrameworkConstants;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.io.IOException;
import java.util.Map;

public class DashboardWidgetFragmentController {

    public void controller(FragmentConfiguration config, @FragmentParam("app") AppDescriptor app, @InjectBeans PatientDomainWrapper patientWrapper,
                           @SpringBean("adminService") AdministrationService adminService, FragmentModel model) throws IOException {
        ObjectMapper mapper = new ObjectMapper();

        Object patient = null;
        patient = config.get("patient");
        if (patient == null ) {
            patient = config.get("patientId");
        }

        ObjectNode appConfig = app.getConfig();

        if (patient != null) {
            if (patient instanceof Patient) {
                patientWrapper.setPatient((Patient) patient);
            } else if (patient instanceof PatientDomainWrapper) {
                patientWrapper = (PatientDomainWrapper) patient;
            } else if (patient instanceof Integer) {
                // assume we have patientId
                patientWrapper.setPatient(Context.getPatientService().getPatient((Integer) patient));
            } else {
                throw new IllegalArgumentException("Patient must be of type Patient or PatientDomainWrapper");
            }
            appConfig.put("patientUuid", patientWrapper.getPatient().getUuid());
            appConfig.put("patientId", patientWrapper.getPatient().getId());
        }

        if (appConfig.get("dateFormat") == null) {
            appConfig.put("dateFormat", adminService.getGlobalProperty(UiFrameworkConstants.GP_FORMATTER_DATE_FORMAT, "yyyy-MM-dd"));
        }
        if (appConfig.get("JSDateFormat") == null) {
            appConfig.put("JSDateFormat", adminService.getGlobalProperty(UiFrameworkConstants.GP_FORMATTER_JS_DATE_FORMAT, "YYYY-MMM-DD"));
        }

        if (appConfig.get("JSDateTimeFormat") == null) {
            appConfig.put("JSDateTimeFormat", adminService.getGlobalProperty(UiFrameworkConstants.GP_FORMATTER_JS_DATETIME_FORMAT, "YYYY-MMM-DD HH:mm"));
        }

        appConfig.put("locale", Context.getLocale().toString());
        appConfig.put("language", Context.getLocale().getLanguage());

        Map<String, Object> appConfigMap = mapper.convertValue(appConfig, Map.class);
        config.merge(appConfigMap);
        config.addAttribute("json", appConfig.toString().replace('\"', '\''));
        model.put("app", app);
    }
}
