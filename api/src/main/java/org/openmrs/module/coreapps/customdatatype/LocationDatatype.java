package org.openmrs.module.coreapps.customdatatype;

import org.apache.commons.lang.StringUtils;
import org.openmrs.Location;
import org.openmrs.api.context.Context;
import org.openmrs.customdatatype.SerializingCustomDatatype;
import org.springframework.stereotype.Component;

@Component("coreapps.LocationDatatype")
public class LocationDatatype extends SerializingCustomDatatype<Location>{

    @Override
    public String serialize(Location location) {
        if (location == null || location.getUuid() == null) {
            return null;
        }
        return location.getUuid();
    }

    @Override
    public Location deserialize(String serializedValue) {
        if (StringUtils.isEmpty(serializedValue)) {
            return null;
        }
        return Context.getLocationService().getLocationByUuid(serializedValue);
    }
}
