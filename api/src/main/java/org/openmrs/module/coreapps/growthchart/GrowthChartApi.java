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

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Months;
import org.json.JSONArray;
import org.json.JSONObject;
import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.api.context.Context;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class GrowthChartApi {
	
	private static final Logger log = LoggerFactory.getLogger(GrowthChartApi.class);
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JSONArray getHeightsAtGivenPatientAges(Patient patient, ChartJSAgeAxis ageAxis) {
		List<Obs> heightsObs = new ArrayList(getHeightConceptObsForAPatient(patient));
		
		sortObsListByObsDateTime(heightsObs);
		return getConceptObsValuesAtAGivenAgesOfPatient(heightsObs, ageAxis);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JSONArray getWeightsAtGivenPatientAges(Patient patient, ChartJSAgeAxis ageAxis) {
		List<Obs> weightsObs = new ArrayList(getWeightConceptObsForAPatient(patient));
		
		sortObsListByObsDateTime(weightsObs);
		return getConceptObsValuesAtAGivenAgesOfPatient(weightsObs, ageAxis);
		
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public JSONArray getHeadCircumferenceAtGivenPatientAges(Patient patient, ChartJSAgeAxis ageAxis) {
		Set<Obs> headCircumferenceConceptObsForAPatient = getHeadCircumferenceConceptObsForAPatient(patient);
		
		if (headCircumferenceConceptObsForAPatient != null) {
			List<Obs> headCircumferenceObs = new ArrayList(headCircumferenceConceptObsForAPatient);
			sortObsListByObsDateTime(headCircumferenceObs);
			return getConceptObsValuesAtAGivenAgesOfPatient(headCircumferenceObs, ageAxis);
		} else
			return null;
	}
	
	public JSONArray getPatientBMIsAcrossAnAgeDifference(Patient patient, ChartJSAgeAxis ageAxis) {
		Date birthDate = patient.getBirthdate();
		Calendar atAgeFromBirth = Calendar.getInstance(Context.getLocale());
		AgeUnit ageUnit = ageAxis.getAgeUnit();
		Integer[] ages = createIntegerArrayByRange(ageAxis.getStartAge(), ageAxis.getLastAge(), ageAxis.getAgeDifference());
		JSONArray bmis = new JSONArray();
		
		for (int atAge : ages) {
			atAgeFromBirth.setTime(birthDate);
			if (ageUnit.equals(AgeUnit.MONTHS)) {
				atAgeFromBirth.add(Calendar.MONTH, atAge);
			} else if (ageUnit.equals(AgeUnit.YEARS)) {
				atAgeFromBirth.add(Calendar.YEAR, atAge);
			}
			for (Obs o : getWeightConceptObsForAPatient(patient)) {
				if (o.getValueNumeric() != null) {
					Calendar obsDate = Calendar.getInstance(Context.getLocale());
					
					obsDate.setTime(getObservationDate(o));
					
					Integer diffYears = obsDate.get(Calendar.YEAR) - atAgeFromBirth.get(Calendar.YEAR);
					Integer diffMonths = diffYears * 12 + obsDate.get(Calendar.MONTH) - atAgeFromBirth.get(Calendar.MONTH);
					Obs latestHeightForPatient = getLatestHeightForPatient(patient);
					Double bmi = null;
					JSONObject bmiJson = new JSONObject();
					if (latestHeightForPatient != null) {
						if (ageUnit.equals(AgeUnit.MONTHS) && diffMonths == 0) {
							bmi = roundAbout(
							    o.getValueNumeric() / (Math.pow(latestHeightForPatient.getValueNumeric() * 0.01, 2)), 1);
						} else if (ageUnit.equals(AgeUnit.YEARS) && diffYears == 0) {
							bmi = roundAbout(
							    o.getValueNumeric() / (Math.pow(latestHeightForPatient.getValueNumeric() * 0.01, 2)), 1);
						}
					}
					if (bmi != null) {
						bmiJson.put(Integer.toString(atAge), bmi);
						bmis.put(bmiJson);
					}
				}
			}
		}
		
		return bmis;
	}
	
	public Integer getPatientAgeInMonths(Patient patient) {
		if (patient.getBirthdate() == null) {
			return null;
		}
		Date endDate = patient.getDead() ? patient.getDeathDate() : new Date();
		
		return Months.monthsBetween(new DateTime(patient.getBirthdate()), new DateTime(endDate)).getMonths();
	}
	
	public Integer getPatientAgeInDays(Patient patient) {
		if (patient.getBirthdate() == null) {
			return null;
		}
		Date endDate = patient.getDead() ? patient.getDeathDate() : new Date();
		
		return Days.daysBetween(new DateTime(patient.getBirthdate()), new DateTime(endDate)).getDays();
	}
	
	private Integer[] createIntegerArrayByRange(Integer start, Integer end, Integer difference) {
		Integer[] array = new Integer[((end - start) / difference) + 1];
		int i = 1;
		
		array[0] = start;
		for (int k = start; k <= end; k = k + difference) {
			if (i < array.length) {
				array[i] = k + difference;
				i++;
			}
		}
		return array;
	}
	
	private Set<Obs> getWeightConceptObsForAPatient(Patient patient) {
		return getObsFromConceptForPatient(patient, "concept.weight", 5089);
	}
	
	private Set<Obs> getObsFromConceptForPatient(Patient patient, String gpCodeForConcept, Integer conceptId) {
		return new HashSet<Obs>(Context.getObsService().getObservationsByPersonAndConcept(patient.getPerson(), Context
		        .getConceptService()
		        .getConcept(StringUtils.isNotBlank(Context.getAdministrationService().getGlobalProperty(gpCodeForConcept))
		                ? Integer.parseInt(Context.getAdministrationService().getGlobalProperty(gpCodeForConcept))
		                : conceptId)));
	}
	
	private Date getObservationDate(Obs obs) {
		return obs == null ? null
		        : (obs.getObsDatetime() != null ? obs.getObsDatetime()
		                : (obs.getDateChanged() != null ? obs.getDateChanged() : obs.getDateCreated()));
	}
	
	public Obs getLatestHeightForPatient(Patient patient) {
		return getRecentObsFromSet(getHeightConceptObsForAPatient(patient));
	}
	
	public double roundAbout(double value, int places) {
		if (places < 0)
			throw new IllegalArgumentException();
		
		long factor = (long) Math.pow(10, places);
		value = value * factor;
		long tmp = Math.round(value);
		return (double) tmp / factor;
	}
	
	private Set<Obs> getHeightConceptObsForAPatient(Patient patient) {
		return getObsFromConceptForPatient(patient, "concept.height", 5090);
	}
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Obs getRecentObsFromSet(Set<Obs> obsSet) {
		List<Obs> obsList = new ArrayList(obsSet);
		
		sortObsListByObsDateTime(obsList);
		
		return obsList != null && obsList.size() > 0 ? obsList.get(obsList.size() - 1) : null;
	}
	
	private void sortObsListByObsDateTime(List<Obs> obsList) {
		Collections.sort(obsList, new Comparator<Obs>() {
			
			public int compare(Obs o1, Obs o2) {
				return o1.getObsDatetime().compareTo(o2.getObsDatetime());
			}
		});
	}
	
	private JSONArray getConceptObsValuesAtAGivenAgesOfPatient(List<Obs> conceptObsList, ChartJSAgeAxis ageAxis) {
		JSONArray conceptObsForAges = new JSONArray();
		
		if (conceptObsList != null && ageAxis != null) {
			Integer[] ages = createIntegerArrayByRange(ageAxis.getStartAge(), ageAxis.getLastAge(),
			    ageAxis.getAgeDifference());
			
			for (int i = 0; i < ages.length; i++) {
				JSONObject conceptObsForAge = new JSONObject();
				Double obsValueAtAge = getObsValueAtAGivenAge(conceptObsList, ageAxis.getAgeUnit(), ages[i]);
				
				if (obsValueAtAge != null) {
					conceptObsForAge.put(Integer.toString(ages[i]), obsValueAtAge);
					conceptObsForAges.put(conceptObsForAge);
				}
				
			}
		}
		
		return conceptObsForAges;
	}
	
	/*
	 * Currently only supporting two AgeUnits Months and Years which are the
	 * ones required for the isante growth charts
	 */
	private Double getObsValueAtAGivenAge(List<Obs> conceptObsList, AgeUnit ageUnit, Integer atAge) {
		Double obsValueAtAge = null;
		
		if (conceptObsList != null && conceptObsList.size() > 0 && ageUnit != null && atAge != null) {
			Date birthDate = conceptObsList.get(0).getPerson().getBirthdate();
			Calendar atAgeFromBirth = Calendar.getInstance(Context.getLocale());
			
			atAgeFromBirth.setTime(birthDate);
			if (ageUnit.equals(AgeUnit.MONTHS)) {
				atAgeFromBirth.add(Calendar.MONTH, atAge);
			} else if (ageUnit.equals(AgeUnit.YEARS)) {
				atAgeFromBirth.add(Calendar.YEAR, atAge);
			}
			
			for (Obs obs : conceptObsList) {
				Calendar obsDate = Calendar.getInstance(Context.getLocale());
				
				obsDate.setTime(getObservationDate(obs));
				Integer diffYears = obsDate.get(Calendar.YEAR) - atAgeFromBirth.get(Calendar.YEAR);
				Integer diffMonths = diffYears * 12 + obsDate.get(Calendar.MONTH) - atAgeFromBirth.get(Calendar.MONTH);
				
				if (ageUnit.equals(AgeUnit.MONTHS) && diffMonths == 0) {
					obsValueAtAge = obs.getValueNumeric();
					break;
				} else if (ageUnit.equals(AgeUnit.YEARS) && diffYears == 0) {
					obsValueAtAge = obs.getValueNumeric();
					break;
				}
			}
		}
		
		return obsValueAtAge;
	}
	
	private Set<Obs> getHeadCircumferenceConceptObsForAPatient(Patient patient) {
		Concept headCircumferenceConcept = new GrowthChartGlobalProps().HEAD_CIRCUMFERENCE_CONCEPT;
		return new HashSet<Obs>(headCircumferenceConcept != null
		        ? Context.getObsService().getObservationsByPersonAndConcept(patient.getPerson(), headCircumferenceConcept)
		        : null);
	}
	
}
