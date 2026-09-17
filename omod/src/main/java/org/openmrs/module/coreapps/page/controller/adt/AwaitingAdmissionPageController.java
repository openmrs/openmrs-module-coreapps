package org.openmrs.module.coreapps.page.controller.adt;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.time.StopWatch;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.Provider;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.PatientService;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.module.coreapps.CoreAppsProperties;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.adt.InpatientRequest;
import org.openmrs.module.emrapi.adt.InpatientRequestSearchCriteria;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
import org.openmrs.module.emrapi.diagnosis.DiagnosisService;
import org.openmrs.module.emrapi.utils.GeneralUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.util.OpenmrsUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class AwaitingAdmissionPageController {
    private final Log log = LogFactory.getLog(getClass());

    public void get(PageModel model,
                    @SpringBean("patientService") PatientService patientService,
                    @SpringBean("adtService") AdtService adtService,
                    @SpringBean("emrApiProperties") EmrApiProperties emrApiProperties,
                    @SpringBean("emrDiagnosisService") DiagnosisService diagnosisService,
                    @SpringBean("adminService") AdministrationService adminService,
                    @SpringBean CoreAppsProperties coreAppsProperties,
                    @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService) {

        List<Extension> admissionActions = appFrameworkService.getExtensionsForCurrentUser("coreapps.app.awaitingAdmissionActions");
        Collections.sort(admissionActions);
        model.addAttribute("admissionActions", admissionActions);

        // add the paper record identifier, if the definition is available (provided by the paper record module)
        PatientIdentifierType paperRecordIdentifierType = null;
        String gpPaperRecordIdDef = adminService.getGlobalProperty("emr.paperRecordIdentifierType");
        if (StringUtils.isNotBlank(gpPaperRecordIdDef)) {
            paperRecordIdentifierType = GeneralUtils.getPatientIdentifierType(gpPaperRecordIdDef, patientService);
        }
        model.addAttribute("paperRecordIdentifierDefinitionAvailable", (paperRecordIdentifierType != null));

        log.warn("Searching for inpatient requests");
        InpatientRequestSearchCriteria criteria = new InpatientRequestSearchCriteria();
        StopWatch sw = new StopWatch();
        sw.start();
        List<InpatientRequest> requests = adtService.getInpatientRequests(criteria);
        sw.stop();
        log.warn("Found " + requests.size() + " inpatient requests in " + sw);

        sw.reset();
        sw.start();
        List<Map<String, Object>> rows = new ArrayList<>();
        for (InpatientRequest request : requests) {
            Map<String, Object> row = new LinkedHashMap<>();
            row.put("visitId", request.getVisit().getId());
            row.put("patientId", request.getPatient().getId());
            row.put("patientLastName", request.getPatient().getPersonName().getFamilyName());
            row.put("patientFirstName", request.getPatient().getPersonName().getGivenName());
            row.put("primaryIdentifier", getIdentifier(request.getPatient(), emrApiProperties.getPrimaryIdentifierType()));
            row.put("mostRecentAdmissionRequestFromLocation", request.getDispositionEncounter().getLocation());
            row.put("mostRecentAdmissionRequestToLocation", request.getDispositionLocation());
            row.put("mostRecentAdmissionRequestDatetime", request.getDispositionEncounter().getEncounterDatetime());
            row.put("mostRecentAdmissionRequestProvider", getProvider(request.getDispositionEncounter()));
            row.put("mostRecentAdmissionRequestDiagnoses", getPrimaryDiagnoses(request.getDispositionEncounter(), diagnosisService));
            if (paperRecordIdentifierType != null) {
                row.put("paperRecordIdentifier", getIdentifier(request.getPatient(), paperRecordIdentifierType));
            }
            rows.add(row);
        }
        String sortBy = "mostRecentAdmissionRequestDatetime";
        rows.sort((r1, r2) -> OpenmrsUtil.compare((Date)r1.get(sortBy), (Date)r2.get(sortBy)));
        model.addAttribute("awaitingAdmissionList", rows);
        sw.stop();
        log.warn("Retrieved data from inpatient requests in: " + sw);

        // add location tag constants
        model.addAttribute("supportsAdmissionLocationTag", EmrApiConstants.LOCATION_TAG_SUPPORTS_ADMISSION);
        model.addAttribute("supportsLoginLocationTag", EmrApiConstants.LOCATION_TAG_SUPPORTS_LOGIN);

        // used to determine whether we display a link to the patient in the results list
        model.addAttribute("privilegePatientDashboard", CoreAppsConstants.PRIVILEGE_PATIENT_DASHBOARD);

        AppDescriptor app = appFrameworkService.getApp(CoreAppsConstants.AWAITING_ADMISSION);
        String patientPageUrl = app.getConfig().get("patientPageUrl") != null
                ? app.getConfig().get("patientPageUrl").getTextValue() : coreAppsProperties.getDashboardUrl();
        model.addAttribute("patientPageUrl", patientPageUrl);
    }

    protected String getIdentifier(Patient patient, PatientIdentifierType type) {
        if (type != null) {
            PatientIdentifier pi = patient.getPatientIdentifier(type);
            if (pi != null) {
                return pi.getIdentifier();
            }
        }
        return "";
    }

    protected Provider getProvider(Encounter encounter) {
        for (EncounterProvider encounterProvider : encounter.getActiveEncounterProviders()) {
            return encounterProvider.getProvider();
        }
        return null;
    }

    protected String getPrimaryDiagnoses(Encounter encounter, DiagnosisService diagnosisService) {
        List<Diagnosis> diagnoses = diagnosisService.getPrimaryDiagnoses(encounter);
        return (String) new AwaitingAdmissionDiagnosisFormatter().convert(diagnoses);
    }
}
