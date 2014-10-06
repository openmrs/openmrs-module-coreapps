package org.openmrs.module.coreapps;

public class CoreAppsConstants {

    public static final String GP_DEFAULT_PATIENT_IDENTIFIER_LOCATION = "coreapps.defaultPatientIdentifierLocation";

    public static final String HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_NAME = "encounterDiagnoses";
    public static final String HTMLFORMENTRY_ENCOUNTER_DISPOSITION_TAG_NAME = "encounterDisposition";
    public static final String HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_INCLUDE_PRIOR_DIAGNOSES_ATTRIBUTE_NAME = "includePriorDiagnosesFromMostRecentEncounterWithDispositionOfType";


    public static final String VITALS_ENCOUNTER_TYPE_UUID = "67a71486-1a54-468f-ac3e-7091a9a79584";

    public static final String PRIVILEGE_PATIENT_DASHBOARD = "App: coreapps.patientDashboard";
    public static final String PRIVILEGE_PATIENT_VISITS = "App: coreapps.patientVisits";
	public static final String PRIVILEGE_START_VISIT = "Task: coreapps.createVisit";
	public static final String PRIVILEGE_END_VISIT = "Task: coreapps.endVisit";

	public static final String GP_RECENT_DIAGNOSIS_PERIOD_IN_DAYS = "coreapps.recentDiagnosisPeriodInDays";

    public static final String ENCOUNTER_TEMPLATE_EXTENSION = "org.openmrs.referenceapplication.encounterTemplate";
}
