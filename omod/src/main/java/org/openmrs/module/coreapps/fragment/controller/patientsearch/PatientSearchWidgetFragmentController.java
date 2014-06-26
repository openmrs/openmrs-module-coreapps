package org.openmrs.module.coreapps.fragment.controller.patientsearch;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.utils.GeneralUtils;
import org.openmrs.ui.framework.annotation.FragmentParam;
import org.openmrs.ui.framework.fragment.FragmentModel;
import org.openmrs.util.OpenmrsConstants;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Fragment controller for patient search widget; sets the min # of search characters based on global property,
 * and loads last viewed patients for current user if "showLastViewedPatients" fragment config param=true
 */
public class PatientSearchWidgetFragmentController {

    public void controller(FragmentModel model, UiSessionContext sessionContext,
                           @FragmentParam(value = "showLastViewedPatients", required = false) Boolean showLastViewedPatients) {

        showLastViewedPatients = showLastViewedPatients != null ? showLastViewedPatients : false;

        model.addAttribute("minSearchCharacters",
                Context.getAdministrationService()
                        .getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_MIN_SEARCH_CHARACTERS, "1"));

        model.addAttribute("dateFormatter", new SimpleDateFormat("dd-MMM-yyy", Context.getLocale()));
        model.addAttribute("showLastViewedPatients", showLastViewedPatients);

        if (showLastViewedPatients) {
            List<Patient> patients = GeneralUtils.getLastViewedPatients(sessionContext.getCurrentUser());
            model.addAttribute("lastViewedPatients", patients);
        }

    }

}
