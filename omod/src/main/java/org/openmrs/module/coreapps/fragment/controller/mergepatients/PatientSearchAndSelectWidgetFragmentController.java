package org.openmrs.module.coreapps.fragment.controller.mergepatients;

import org.openmrs.Patient;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.module.emrapi.utils.GeneralUtils;
import org.openmrs.ui.framework.UiFrameworkConstants;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.util.OpenmrsConstants;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

/**
 * Fragment controller for patient search widget; sets the min # of search characters based on global property,
 * and loads last viewed patients for current user if "showLastViewedPatients" fragment config param=true
 */
public class PatientSearchAndSelectWidgetFragmentController {

    public void controller(FragmentModel model, UiSessionContext sessionContext,
                           @SpringBean("adminService") AdministrationService administrationService,
                           @FragmentParam(value = "showLastViewedPatients", required = false) Boolean showLastViewedPatients) {

        showLastViewedPatients = showLastViewedPatients != null ? showLastViewedPatients : false;

        model.addAttribute("minSearchCharacters",
                administrationService.getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_MIN_SEARCH_CHARACTERS, "1"));

        model.addAttribute("searchDelayShort",
                administrationService.getGlobalProperty(CoreAppsConstants.GP_SEARCH_DELAY_SHORT, "300"));

        model.addAttribute("searchDelayLong",
                administrationService.getGlobalProperty(CoreAppsConstants.GP_SEARCH_DELAY_LONG, "1000"));

        model.addAttribute("dateFormatJS", "DD MMM YYYY");   // TODO really should be driven by global property, but currently we only have a property for the java date format
        model.addAttribute("locale", Context.getLocale().getLanguage());
        model.addAttribute("defaultLocale", new Locale(administrationService.getGlobalProperty((OpenmrsConstants.GLOBAL_PROPERTY_DEFAULT_LOCALE), "en")).getLanguage());
        model.addAttribute("dateFormatter", new SimpleDateFormat(administrationService.getGlobalProperty(UiFrameworkConstants.GP_FORMATTER_DATE_FORMAT),
                Context.getLocale()));
        model.addAttribute("showLastViewedPatients", showLastViewedPatients);

        if (showLastViewedPatients) {
            List<Patient> patients = GeneralUtils.getLastViewedPatients(sessionContext.getCurrentUser());
            model.addAttribute("lastViewedPatients", patients);
        }

    }

}
