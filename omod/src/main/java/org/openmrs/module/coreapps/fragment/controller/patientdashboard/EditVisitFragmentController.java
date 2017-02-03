package org.openmrs.module.coreapps.fragment.controller.patientdashboard;

import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.VisitAttributeType;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.utils.VisitTypeHelper;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.util.ArrayList;
import java.util.List;

/**
 * EditVisit fragment. Allow editing of visit type and attributes
 */
public class EditVisitFragmentController {

	public void controller(FragmentConfiguration config,
			FragmentModel model,
			@SpringBean("adtService") AdtService adtService,
			@SpringBean("visitService") VisitService visitService,
			@SpringBean("visitTypeHelper") VisitTypeHelper visitTypeHelper,
			UiSessionContext sessionContext){

		config.require("patient");
		config.require("visit");

		PatientDomainWrapper patientWrapper;
		Object patient = config.get("patient");
		if (patient instanceof PatientDomainWrapper) {
			patientWrapper = (PatientDomainWrapper) patient;
		} else {
			throw new IllegalArgumentException("Patient must be of type PatientDomainWrapper");
		}

		Visit visit = (Visit) config.get("visit");

		// get visit types
		List<VisitType> visitTypes = visitTypeHelper.getUnRetiredVisitTypes();

		// get visit attribute types
		List<VisitAttributeType> visitAttributeTypes = visitService.getAllVisitAttributeTypes();

		model.addAttribute("patient", patientWrapper);
		model.addAttribute("visit", visit);
		model.addAttribute("visitTypes", visitTypes);
		model.addAttribute("visitAttributeTypes", visitAttributeTypes);
	}
}
