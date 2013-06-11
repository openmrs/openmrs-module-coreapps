package org.openmrs.module.coreapps.fragment.controller;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.PatientService;
import org.openmrs.module.coreapps.CoreAppsProperties;
import org.openmrs.patient.IdentifierValidator;
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
	                                                  @SpringBean("patientService") PatientService patientService,
                                                      @SpringBean("coreAppsProperties") CoreAppsProperties coreAppsProperties) {
		
		if (patient != null && identifierType != null) {
			PatientIdentifier patientIdentifier = patient.getPatientIdentifier(identifierType);
			if (patientIdentifier == null && StringUtils.isBlank(identifierValue)) {
				//nothing to do
				return new SuccessResult(ui.message("emr.patientDashBoard.editPatientIdentifier.warningMessage"));
			}

            // create new identifier if necessary
			if (patientIdentifier == null) {
				patientIdentifier = new PatientIdentifier(identifierValue, identifierType, location);
			} else {
                // otherwise update identifier value and location
				if (StringUtils.isNotBlank(identifierValue)) {
					patientIdentifier.setIdentifier(identifierValue);
				} else {
					patientIdentifier.setVoided(true);
				}
                if (location != null) {
                    patientIdentifier.setLocation(location);
                }
			}

            // assure that a location has been set if required
            if (patientIdentifier.getLocation() == null
                    && !PatientIdentifierType.LocationBehavior.NOT_USED.equals(patientIdentifier.getIdentifierType().getLocationBehavior())) {
                patientIdentifier.setLocation(coreAppsProperties.getDefaultPatientIdentifierLocation());
            }

            // make sure that the identifier is not already in use
            if (patientService.isIdentifierInUseByAnotherPatient(patientIdentifier)) {
                return new FailureResult(ui.format(identifierType) + " "
                        + ui.message("coreapps.patientDashBoard.editPatientIdentifier.duplicateMessage"));
            }

            // run any validator that might exist for this identifier
            // TODO: test this better
            IdentifierValidator validator = patientService.getIdentifierValidator(identifierType.getValidator());
            if (validator != null && !validator.isValid(patientIdentifier.getIdentifier())) {
                return new FailureResult(ui.format(identifierType) + " "
                        + ui.message("coreapps.patientDashBoard.editPatientIdentifier.invalidMessage"));
            }

            // now go ahead and try to save
			patient.addIdentifier(patientIdentifier);
			try {
				patientService.savePatient(patient);
			}
			catch (Exception e) {
				return new FailureResult(ui.message("coreapps.patientDashBoard.editPatientIdentifier.failureMessage" + " "
                        + ui.format(identifierType)));
			}
		}
		return new SuccessResult(ui.format(identifierType) + " "
                + ui.message("coreapps.patientDashBoard.editPatientIdentifier.successMessage"));
	}
}
