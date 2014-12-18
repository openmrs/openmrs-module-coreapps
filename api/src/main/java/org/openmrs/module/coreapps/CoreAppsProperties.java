package org.openmrs.module.coreapps;

import org.openmrs.Location;
import org.openmrs.module.emrapi.utils.ModuleProperties;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;


/**
 * Properties for this module.
 */
@Component("coreAppsProperties")
public class CoreAppsProperties extends ModuleProperties {

	// when adding a new patient identifier via the patient dashboard, the location to use if not specified (and the identifier type requires a location)
	public Location getDefaultPatientIdentifierLocation() {
		return getLocationByGlobalProperty(CoreAppsConstants.GP_DEFAULT_PATIENT_IDENTIFIER_LOCATION);
	}

	public int getRecentDiagnosisPeriodInDays() {
		String gp = getGlobalProperty(CoreAppsConstants.GP_RECENT_DIAGNOSIS_PERIOD_IN_DAYS, false);
		if (StringUtils.hasText(gp)) {
			try {
				return Integer.parseInt(gp);
			} catch (NumberFormatException e) {
				throw new IllegalStateException("Invalid configuration: number of days expected in " + CoreAppsConstants.GP_RECENT_DIAGNOSIS_PERIOD_IN_DAYS, e);
			}
		}
		return 730; //2 years
	}

    // TODO remove this and just always default to clinician dashboard once legacy implementations (Mirebalais) move to clinician dashboard
    public String getDefaultDashboard() {
        return getGlobalProperty(CoreAppsConstants.GP_DEFAULT_DASHBOARD, false);
    }

}
