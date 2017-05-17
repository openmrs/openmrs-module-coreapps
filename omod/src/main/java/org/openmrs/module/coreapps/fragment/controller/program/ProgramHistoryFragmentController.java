package org.openmrs.module.coreapps.fragment.controller.program;

import groovy.lang.MissingPropertyException;
import org.apache.commons.lang3.StringUtils;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.Program;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ProgramHistoryFragmentController {

    public void controller(FragmentConfiguration config, @FragmentParam("app") AppDescriptor app, @InjectBeans PatientDomainWrapper patientWrapper,
                           @SpringBean("programWorkflowService") ProgramWorkflowService programWorkflowService) throws IOException {

        ObjectMapper mapper = new ObjectMapper();

        Object patient = null;
        patient = config.get("patient");
        if (patient == null ) {
            patient = config.get("patientId");
        }
        if (patient == null) {
            config.require("patient");
        }
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

        ObjectNode appConfig = app.getConfig();
        appConfig.put("patientUuid", patientWrapper.getPatient().getUuid());

        if (appConfig.get("program") == null || StringUtils.isEmpty(appConfig.get("program").getTextValue())) {
            throw new MissingPropertyException("Program must be specified");
        }

        Program program = programWorkflowService.getProgramByUuid(appConfig.get("program").getTextValue());

        if (program == null) {
            throw new MissingPropertyException("Invalid program");
        }

        List<PatientProgram> patientPrograms = programWorkflowService.getPatientPrograms(patientWrapper.getPatient(),
                program, null, null, null, null, false);

        // TODO: assumption here is that "getPatientPrograms" returns results in chronological order--need to confirm?
        Collections.reverse(patientPrograms);
        List<String> programJson = new ArrayList<String>();

        Boolean includeActive = appConfig.get("includeActive") != null ? appConfig.get("includeActive").getBooleanValue() : true;
        Boolean includeInactive = appConfig.get("includeInactive") != null ? appConfig.get("includeInactive").getBooleanValue() : true;

        for (PatientProgram patientProgram : patientPrograms) {
            if ((patientProgram.getDateCompleted() == null && includeActive)
                || (patientProgram.getDateCompleted() != null && includeInactive)) {
                appConfig.put("patientProgram", patientProgram.getUuid());
                programJson.add(appConfig.toString().replace('\"', '\''));
            }
        }

        Map<String, Object> appConfigMap = mapper.convertValue(appConfig, Map.class);
        config.merge(appConfigMap);
        config.addAttribute("programJson", programJson);
    }

}
