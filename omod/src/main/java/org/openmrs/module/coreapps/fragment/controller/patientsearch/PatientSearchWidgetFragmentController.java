package org.openmrs.module.coreapps.fragment.controller.patientsearch;

import javax.servlet.http.HttpServletRequest;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.messagesource.MessageSourceService;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.module.emrapi.utils.GeneralUtils;
import org.openmrs.ui.framework.UiFrameworkConstants;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.util.OpenmrsConstants;

/**
 * Fragment controller for patient search widget; sets the min # of search characters based on global property,
 * and loads last viewed patients for current user if "showLastViewedPatients" fragment config param=true
 */
public class PatientSearchWidgetFragmentController {

    private static final Log log = LogFactory.getLog(PatientSearchWidgetFragmentController.class);

    public void controller(FragmentModel model, UiSessionContext sessionContext,
                           HttpServletRequest request,
                           @SpringBean("adminService") AdministrationService administrationService,
                           @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
                           @SpringBean("messageSourceService") MessageSourceService messageSourceService,
                           @FragmentParam(value = "columnConfig", required = false) ArrayNode columnConfig,
                           @FragmentParam(value = "showLastViewedPatients", required = false) Boolean showLastViewedPatients,
                           @FragmentParam(value = "initialSearchFromParameter", required = false) String searchByParam,
                           @FragmentParam(value = "registrationAppLink", required=false) String registrationAppLink) {

        showLastViewedPatients = showLastViewedPatients != null ? showLastViewedPatients : false;

        model.addAttribute("minSearchCharacters",
                administrationService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_MIN_SEARCH_CHARACTERS, "1"));

        model.addAttribute("searchDelayShort",
                administrationService.getGlobalProperty(CoreAppsConstants.GP_SEARCH_DELAY_SHORT, "300"));

        model.addAttribute("searchDelayLong",
                administrationService.getGlobalProperty(CoreAppsConstants.GP_SEARCH_DELAY_LONG, "1000"));

        String patientSearchHandler = administrationService.getGlobalProperty(CoreAppsConstants.GP_PATIENT_SEARCH_HANDLER, "");
        model.addAttribute("patientSearchHandler", patientSearchHandler);

        model.addAttribute("dateFormatJS", "DD MMM YYYY");  // TODO really should be driven by global property, but currently we only have a property for the java date format
        model.addAttribute("locale", Context.getLocale().getLanguage());
        model.addAttribute("defaultLocale", new Locale(administrationService.getGlobalProperty((OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE), "en")).getLanguage());
        model.addAttribute("dateFormatter", new SimpleDateFormat(administrationService.getGlobalProperty(UiFrameworkConstants.GP_FORMATTER_DATE_FORMAT),
                Context.getLocale()));
        model.addAttribute("searchWidgetDateFormatter", new SimpleDateFormat("yyyy-MM-dd"));
        model.addAttribute("showLastViewedPatients", showLastViewedPatients);

        String doInitialSearch = null;
        if (searchByParam != null && StringUtils.isNotEmpty(request.getParameter(searchByParam))) {
            doInitialSearch = request.getParameter(searchByParam);
        }
        model.addAttribute("doInitialSearch", doInitialSearch);

        if (showLastViewedPatients) {
            List<Patient> patients = GeneralUtils.getLastViewedPatients(sessionContext.getCurrentUser());
            model.addAttribute("lastViewedPatients", patients);
        }

        String listingAttributeTypesStr = administrationService.getGlobalProperty(
                OpenmrsConstants.GLOBAL_PROPERTY_PATIENT_LISTING_ATTRIBUTES, "");
        List<String> listingAttributeTypeNames = new ArrayList<String>();
        if (StringUtils.isNotBlank(listingAttributeTypesStr)) {
            String[] attTypeNames = StringUtils.split(listingAttributeTypesStr.trim(), ",");
            for (String name : attTypeNames) {
                if (StringUtils.isNotBlank(name.trim())) {
                    listingAttributeTypeNames.add(name.trim());
                }
            }
        }
        model.addAttribute("listingAttributeTypeNames", listingAttributeTypeNames);
        model.addAttribute("registrationAppLink", registrationAppLink);

        List<Extension> patientSearchExtensions = appFrameworkService.getExtensionsForCurrentUser("coreapps.patientSearch.extension");
        Collections.sort(patientSearchExtensions);
        model.addAttribute("patientSearchExtensions", patientSearchExtensions);

        // Column config is passed from the appConfig, which we parse and translate any labels
        String columnConfigJson = null;
        try {
            if (columnConfig != null) {
                List<Map<String, String>> columns = new ArrayList<Map<String, String>>();
                for (JsonNode columnNode : columnConfig) {
                    Map<String, String> column = new HashMap<String, String>();
                    for (Iterator<String> fieldNameIter = columnNode.getFieldNames(); fieldNameIter.hasNext();) {
                        String fieldName = fieldNameIter.next();
                        JsonNode fieldNode = columnNode.get(fieldName);
                        String fieldValue = fieldNode.getTextValue();
                        if (fieldName.equalsIgnoreCase("label") && StringUtils.isNotBlank(fieldValue)) {
                            fieldValue = messageSourceService.getMessage(fieldValue);
                        }
                        column.put(fieldName, fieldValue);
                    }
                    columns.add(column);
                }
                ObjectMapper mapper = new ObjectMapper();
                columnConfigJson = mapper.writeValueAsString(columns);
            }
        }
        catch (Exception e) {
            log.warn("Unable to parse columnConfig", e);
        }
        model.addAttribute("columnConfig", columnConfigJson);
    }

}
