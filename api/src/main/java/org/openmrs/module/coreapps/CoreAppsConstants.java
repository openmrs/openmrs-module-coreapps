package org.openmrs.module.coreapps;

public class CoreAppsConstants {

   public static final String GP_DEFAULT_PATIENT_IDENTIFIER_LOCATION = "coreapps.defaultPatientIdentifierLocation";

   public static final String GP_DASHBOARD_URL = "coreapps.dashboardUrl";
   public static final String GP_VISITS_PAGE_URL = "coreapps.visitsPageUrl";
   public static final String GP_VISITS_PAGE_WITH_SPECIFIC_URL = "coreapps.visitsPageWithSpecificVisitUrl";

   public static final String GP_SEARCH_DELAY_SHORT = "coreapps.searchDelayShort";
   public static final String GP_SEARCH_DELAY_LONG = "coreapps.searchDelayLong";

   public static final String GP_PATIENTDASHBOARD_ENCOUNTER_COUNT = "coreapps.patientDashboardEncounterCount";

   public static final String HTMLFORMENTRY_CODED_OR_FREE_TEXT_OBS_TAG_NAME = "codedOrFreeTextObs";
   public static final String HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_NAME = "encounterDiagnoses";
   public static final String HTMLFORMENTRY_ENCOUNTER_DISPOSITION_TAG_NAME = "encounterDisposition";
   public static final String HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_INCLUDE_PRIOR_DIAGNOSES_ATTRIBUTE_NAME = "includePriorDiagnosesFromMostRecentEncounterWithDispositionOfType";

   public static final String VITALS_ENCOUNTER_TYPE_UUID = "67a71486-1a54-468f-ac3e-7091a9a79584";

   public static final String PRIVILEGE_PATIENT_DASHBOARD = "App: coreapps.patientDashboard";
   public static final String PRIVILEGE_SUMMARY_DASHBOARD = "App: coreapps.summaryDashboard";
   public static final String PRIVILEGE_PATIENT_VISITS = "App: coreapps.patientVisits";
   public static final String PRIVILEGE_START_VISIT = "Task: coreapps.createVisit";
   public static final String PRIVILEGE_END_VISIT = "Task: coreapps.endVisit";
   public static final String PRIVILEGE_DELETE_PATIENT = "Task: coreapps.deletePatient";
   public static final String PRIVILEGE_EDIT_RELATIONSHIPS = "Task: coreapps.editRelationships";

   public static final String GP_RECENT_DIAGNOSIS_PERIOD_IN_DAYS = "coreapps.recentDiagnosisPeriodInDays";

   public static final String ENCOUNTER_TEMPLATE_EXTENSION = "org.openmrs.referenceapplication.encounterTemplate";

   public static final String AWAITING_ADMISSION = "coreapps.app.awaitingAdmission";

   @Deprecated  // no longer used, to override set coreapps.dashboardUrl instead
   public static final String GP_DEFAULT_DASHBOARD = "coreapps.defaultDashboard";

   // a JSON like object property to set visit types order
   public final static String VISIT_TYPES_ORDER_PROPERTY = "coreapps.visit.visittype.order";

   public final static String SHOW_VISIT_TYPE_PATIENT_HEADER_SECTION = "coreapps.showVisitTypeOnPatientHeaderSection";

   // Configure the visit type background color as seen on the UI (Patient Header section, Active Visits page).
   // This is visually helpful when there are multiple visit types.
   public final static String VISIT_TYPE_COLORS = "coreapps.visitTypeColors";
   public final static String TRANSFER_ENCOUNTER_TYPE_UUID = "coreapps.transferEncounterUuid";
   
   // the Sticky Note concept global property name
   public static final String CONCEPT_STICKY_NOTE_PROPERTY_NAME = "coreapps.conceptStickyNote";

   // Condition list
   public static final String MANAGE_CONDITIONS_PRIVILEGE = "Task: Manage Condition Lists";
}
