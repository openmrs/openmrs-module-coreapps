/**
 * This Source Code Form is subject to the terms of the Mozilla Public License,
 * v. 2.0. If a copy of the MPL was not distributed with this file, You can
 * obtain one at http://mozilla.org/MPL/2.0/. OpenMRS is also distributed under
 * the terms of the Healthcare Disclaimer located at http://openmrs.org/license.
 *
 * Copyright (C) OpenMRS Inc. OpenMRS is a registered trademark and the OpenMRS
 * graphic logo is a trademark of OpenMRS Inc.
 */
package org.openmrs.module.coreapps.growthchart;

public class ChartJSAgeAxis {

	private Integer startAge;

	private Integer lastAge;

	private Integer ageDifference;

	private AgeUnit ageUnit;

	public ChartJSAgeAxis(Integer startAge, Integer lastAge, Integer ageDifference, AgeUnit ageUnit) {
		setAgeDifference(ageDifference);
		setAgeUnit(ageUnit);
		setStartAge(startAge);
		setLastAge(lastAge);
	}

	public Integer getStartAge() {
		return startAge;
	}

	public void setStartAge(Integer startAge) {
		this.startAge = startAge;
	}

	public Integer getLastAge() {
		return lastAge;
	}

	public void setLastAge(Integer lastAge) {
		this.lastAge = lastAge;
	}

	public Integer getAgeDifference() {
		return ageDifference;
	}

	public void setAgeDifference(Integer ageDifference) {
		this.ageDifference = ageDifference;
	}

	public AgeUnit getAgeUnit() {
		return ageUnit;
	}

	public void setAgeUnit(AgeUnit ageUnit) {
		this.ageUnit = ageUnit;
	}

}