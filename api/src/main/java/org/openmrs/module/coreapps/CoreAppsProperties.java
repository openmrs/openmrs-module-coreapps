package org.openmrs.module.coreapps;

import org.openmrs.Location;
import org.openmrs.module.emrapi.utils.ModuleProperties;
import org.springframework.stereotype.Component;


/**
 * Properties for this module.
 */
@Component("coreAppsProperties")
public class CoreAppsProperties extends ModuleProperties {

    // when adding a new patient identifier via the patient dashboard, the location to use if not specified (and the identifier type requires a location)
    public Location getDefaultPatientIdentifierLocation() {
        return getLocationByGlobalProperty(CoreAppsConstants.GP_DEFAULT_PATIENT_IDENTIFIER_LOCATION);
    }

}
