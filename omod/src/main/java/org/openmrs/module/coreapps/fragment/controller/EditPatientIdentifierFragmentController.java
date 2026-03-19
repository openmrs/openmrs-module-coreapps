package org.openmrs.module.coreapps.fragment.controller;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientIdentifier;
import org.openmrs.PatientIdentifierType;
import org.openmrs.api.IdentifierNotUniqueException;
import org.openmrs.api.InvalidCheckDigitException;
import org.openmrs.api.InvalidIdentifierFormatException;
import org.openmrs.api.PatientService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.CoreAppsProperties;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.openmrs.validator.PatientIdentifierValidator;
import org.springframework.web.bind.annotation.RequestParam;

import static org.openmrs.module.coreapps.CoreAppsConstants.LOCATION_TAG_MEDICAL_RECORD_LOCATION;

/**
 */
public class EditPatientIdentifierFragmentController {

    protected static final Log log = LogFactory.getLog(EditPatientIdentifierFragmentController.class);

    // if a patient identifier id is passed in, assume editing existing, otherwise assume creating new
	public FragmentActionResult editPatientIdentifier(UiUtils ui,
                                                      UiSessionContext sessionContext,
	                                                  @RequestParam("patientId") Patient patient,
                                                      @RequestParam(value = "patientIdentifierId", required = false) PatientIdentifier patientIdentifier,
	                                                  @RequestParam("identifierTypeId") PatientIdentifierType identifierType,
	                                                  @RequestParam(value = "identifierValue", required = false) String identifierValue,
	                                                  @RequestParam(value = "locationId", required = false) Location location,
	                                                  @SpringBean("patientService") PatientService patientService,
                                                      @SpringBean("coreAppsProperties") CoreAppsProperties coreAppsProperties) {
		
		if (patient != null && identifierType != null) {

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

                Location patientIdentifierLocation = coreAppsProperties.getDefaultPatientIdentifierLocation();

                if (patientIdentifierLocation != null) {
                    patientIdentifier.setLocation(patientIdentifierLocation);
                }
                else {
                    Location medicalRecordLocation = getMedicalRecordLocationAssociatedWith(sessionContext.getSessionLocation());
                    if (medicalRecordLocation != null) {
                        patientIdentifier.setLocation(medicalRecordLocation);
                    }
                    else {
                        log.warn("Unable to set patient identifier location");
                    }
                }
            }

            // validate the identifier
            try {
                PatientIdentifierValidator.validateIdentifier(patientIdentifier);
            }
            catch (IdentifierNotUniqueException e) {
                return new FailureResult(ui.format(identifierType) + " "
                        + ui.message("coreapps.patientDashBoard.editPatientIdentifier.duplicateMessage"));
            }
            catch (InvalidCheckDigitException e) {
                return new FailureResult(ui.format(identifierType) + " "
                        + ui.message("coreapps.patientDashBoard.editPatientIdentifier.invalidMessage"));
            }
            catch (InvalidIdentifierFormatException e) {
                return new FailureResult(ui.format(identifierType) + " "
                        + ui.message("coreapps.patientDashBoard.editPatientIdentifier.invalidFormatMessage")
                        + " \"" + identifierType.getFormatDescription() + "\"");
            }
            catch (Exception e) {
                return new FailureResult(ui.message("coreapps.patientDashBoard.editPatientIdentifier.failureMessage") + " "
                        + ui.format(identifierType));
            }

            // now go ahead and save
			patient.addIdentifier(patientIdentifier);
			try {
                patientService.savePatient(patient);
			}
			catch (Exception e) {
				return new FailureResult(ui.message("coreapps.patientDashBoard.editPatientIdentifier.failureMessage") + " "
                        + ui.format(identifierType));
			}
		}
		return new SuccessResult(ui.format(identifierType) + " "
                + ui.message("coreapps.patientDashBoard.editPatientIdentifier.successMessage"));
	}

    // TODO: update this to use the utility method in the EMR API GeneralUtils once we upgrade Core Apps to run against the latest version of EMR API
    public static Location getMedicalRecordLocationAssociatedWith(Location location) {
        if (location == null) {
            return null;
        } else if (location.hasTag(LOCATION_TAG_MEDICAL_RECORD_LOCATION)) {
            return location;
        } else {
            return getMedicalRecordLocationAssociatedWith(location.getParentLocation());
        }
    }
}
