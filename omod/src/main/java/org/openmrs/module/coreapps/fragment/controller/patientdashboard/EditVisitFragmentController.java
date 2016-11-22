package org.openmrs.module.coreapps.fragment.controller.patientdashboard;

import org.openmrs.*;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;

import java.util.List;

/**
 * EditVisit fragment. Allow editing of visit type and attributes
 */
public class EditVisitFragmentController {

	public void controller(FragmentConfiguration config,
			FragmentModel model,
			@SpringBean("adtService") AdtService adtService,
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

		VisitService vs = Context.getVisitService();
		// get visit types
		List<VisitType> visitTypes = vs.getAllVisitTypes(false);

		// get visit attribute types
		List<VisitAttributeType> visitAttributeTypes = vs.getAllVisitAttributeTypes();

		model.addAttribute("patient", patientWrapper);
		model.addAttribute("visit", visit);
		model.addAttribute("visitTypes", visitTypes);
		model.addAttribute("visitAttributeTypes", visitAttributeTypes);
	}
}
