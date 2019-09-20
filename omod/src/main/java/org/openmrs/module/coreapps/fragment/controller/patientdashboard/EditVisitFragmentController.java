package org.openmrs.module.coreapps.fragment.controller.patientdashboard;

import java.sql.Date;
import java.util.List;

import org.openmrs.Visit;
import org.openmrs.VisitAttributeType;
import org.openmrs.VisitType;
import org.openmrs.api.VisitService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.utils.VisitTypeHelper;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.FragmentConfiguration;
import org.openmrs.ui.framework.fragment.FragmentModel;

import com.ibm.icu.text.SimpleDateFormat;

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
		
		//this will record current date of every editing of visit
		SimpleDateFormat formatter= new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
		Date date = new Date(System.currentTimeMillis());
		System.out.print(formatter.format(date));
		
        
		//edit visit type
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
