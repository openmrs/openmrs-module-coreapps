package org.openmrs.module.coreapps.customdatatype;

import org.openmrs.customdatatype.SerializingCustomDatatype;
import org.springframework.stereotype.Component;

/**
 * This class allows for coded concepts to be displayed as a drop-down when creating visit attributes.
 * The concept id MUST be set in the datatype configuration field.
 */
@Component
public class CodedConceptDatatype extends SerializingCustomDatatype<String> {

	/**
	 * @see org.openmrs.customdatatype.SerializingCustomDatatype#serialize(java.lang.Object)
	 */
	@Override
	public String serialize(String typedValue) {
		return typedValue;
	}

	/**
	 * @see org.openmrs.customdatatype.SerializingCustomDatatype#deserialize(java.lang.String)
	 */
	@Override
	public String deserialize(String serializedValue) {
		return serializedValue;
	}

}
