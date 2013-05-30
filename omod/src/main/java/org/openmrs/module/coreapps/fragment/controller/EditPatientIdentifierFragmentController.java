package org.openmrs.module.coreapps.fragment.controller;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.springframework.web.bind.annotation.RequestParam;

/**
 */
public class EditPatientIdentifierFragmentController {
	
	public FragmentActionResult editPatientIdentifier(UiUtils ui,
	                                                  @RequestParam("patientId") Patient patient,
	                                                  @RequestParam("identifierTypeId") PatientIdentifierType identifierType,
	                                                  @RequestParam(value = "identifierValue", required = false) String identifierValue,
	                                                  @RequestParam(value = "locationId", required = false) Location location,
	                                                  @SpringBean("patientService") PatientService service) {
		
		if (patient != null && identifierType != null) {
			PatientIdentifier patientIdentifier = patient.getPatientIdentifier(identifierType);
			if (patientIdentifier == null && StringUtils.isBlank(identifierValue)) {
				//nothing to do
				return new SuccessResult(ui.message("emr.patientDashBoard.editPatientIdentifier.warningMessage"));
			}
			if (patientIdentifier == null) {
				patientIdentifier = new PatientIdentifier(identifierValue, identifierType, location);
			} else {
				if (StringUtils.isNotBlank(identifierValue)) {
					patientIdentifier.setIdentifier(identifierValue);
				} else {
					patientIdentifier.setVoided(true);
				}
			}
			patient.addIdentifier(patientIdentifier);
			try {
				service.savePatient(patient);
			}
			catch (Exception e) {
				return new FailureResult(ui.message("emr.patientDashBoard.editPatientIdentifier.failureMessage"));
			}
		}
		return new SuccessResult(ui.message("emr.patientDashBoard.editPatientIdentifier.successMessage"));
	}
}
