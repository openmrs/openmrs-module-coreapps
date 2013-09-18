package org.openmrs.module.coreapps.page.controller.findpatient;

import java.text.SimpleDateFormat;
import java.util.List;

import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.utils.GeneralUtils;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.util.OpenmrsConstants;
import org.springframework.web.bind.annotation.RequestParam;

/**
 *
 */
public class FindPatientPageController {
	
	/**
	 * This page is built to be shared across multiple apps. To use it, you must pass an "app"
	 * request parameter, which must be the id of an existing app that is an instance of
	 * coreapps.template.findPatient
	 * 
	 * @param model
	 * @param app
	 * @param sessionContext
	 */
	public void get(PageModel model, @RequestParam("app") AppDescriptor app, UiSessionContext sessionContext) {
		model.addAttribute("afterSelectedUrl", app.getConfig().get("afterSelectedUrl").getTextValue());
		model.addAttribute("heading", app.getConfig().get("label").getTextValue());
		model.addAttribute("label", app.getConfig().get("label").getTextValue());
		model.addAttribute("minSearchCharacters",
		    Context.getAdministrationService()
		            .getGlobalProperty(OpenmrsConstants.GLOBAL_PROPERTY_MIN_SEARCH_CHARACTERS, "1"));
		List<Patient> patients = GeneralUtils.getLastViewedPatients(sessionContext.getCurrentUser());
		model.addAttribute("lastViewedPatients", patients);
		model.addAttribute("dateFormatter", new SimpleDateFormat("dd-MMM-yyy", Context.getLocale()));
	}
	
}
