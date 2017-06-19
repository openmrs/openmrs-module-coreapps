package org.openmrs.module.coreapps.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParseException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.Visit;
import org.openmrs.VisitType;
import org.openmrs.api.APIException;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.springframework.stereotype.Component;

/**
 * Perform common {@link VisitType} functionality
 *
 */
@Component
public class VisitTypeHelper {

	protected static final Log LOG = LogFactory.getLog(VisitTypeHelper.class);

	/**
	 * Returns a list of ordered visit types, provided by a global property
	 * If a visit type is present in the visitTypes argument, but not found in the global property string,
	 * it will be returned unordered, at the end of the visitTypesOrdered list
	 *
	 * @param visitTypes
	 * @return visitTypesOrdered
	 */
	public List<VisitType> getOrderedVisitTypes (List<VisitType> visitTypes) {
		AdministrationService adminService = Context.getAdministrationService();
		String propertyValue = adminService.getGlobalProperty(CoreAppsConstants.VISIT_TYPES_ORDER_PROPERTY);
		VisitService vs = Context.getVisitService();

		return getOrderedVisitTypes(visitTypes, propertyValue, vs);
	}

	public boolean showVisitTypeOnPatientHeaderSection(){
		AdministrationService adminService = Context.getAdministrationService();
		String propertyValue = adminService.getGlobalProperty(CoreAppsConstants.SHOW_VISIT_TYPE_PATIENT_HEADER_SECTION);
		Boolean result = new Boolean(propertyValue);
		return result;
	}

	/**
	 * Returns a list of ordered visit types.
	 *
	 * @param visitTypes All the visit types
	 * @param propertyValue The visit types to order in JSON-like format
	 * @param visitService
	 * @return visitTypesOrdered The visit types ordered and merged with the input visit type list
	 */
	public List<VisitType> getOrderedVisitTypes (List<VisitType> visitTypes, String propertyValue,VisitService visitService) {

		Map<Integer,String> order = null;
		List<VisitType> visitTypesOrdered = new ArrayList<VisitType>();

		if (propertyValue != null) {
			try {
				order = new ObjectMapper().readValue(propertyValue, HashMap.class);
			} catch (JsonParseException e) {
				VisitTypeHelper.LOG.error("Unable to parse global property \"" + CoreAppsConstants.VISIT_TYPES_ORDER_PROPERTY + "\"");
			} catch (JsonMappingException e) {
				VisitTypeHelper.LOG.error("Unable to map global property \"" + CoreAppsConstants.VISIT_TYPES_ORDER_PROPERTY + "\"");
			} catch (APIException e) {
				VisitTypeHelper.LOG.error("Unable to load global property \"" + CoreAppsConstants.VISIT_TYPES_ORDER_PROPERTY + "\"");
			} catch (IOException e) {
				VisitTypeHelper.LOG.error("Unable to read global property \"" + CoreAppsConstants.VISIT_TYPES_ORDER_PROPERTY + "\"");
			}
		}

		if (order != null) {
			for (int i=1 ; i <= order.size() ; i++) {
				String typeUuid = order.get(Integer.toString(i));
				VisitType type = visitService.getVisitTypeByUuid(typeUuid);
				if (visitTypes.contains(type)) {
					visitTypesOrdered.add(visitService.getVisitTypeByUuid(typeUuid));
				}
			}
			for (VisitType type: visitTypes) {
				if (!order.containsValue(type.getUuid())) {
					visitTypesOrdered.add(type);
				}
			}
		}

		if (!(visitTypes.size()==visitTypesOrdered.size())) {
			VisitTypeHelper.LOG.warn("Visit Types order property is not used.");
			return visitTypes;
		}
		return visitTypesOrdered;
	}

	public List<VisitType> getUnRetiredVisitTypes(){
		VisitService visitService = Context.getVisitService();
		List<VisitType> visitTypes = new ArrayList<VisitType>();
		for(VisitType visitType : visitService.getAllVisitTypes()){
			if(visitType.getRetired() == false){
				visitTypes.add(visitType);
			}
		}

		return visitTypes;
	}

	private Map<String, Object> getVisitTypeColorCodes(VisitType visitType){
		Map<String, Object> colorCode = new HashMap<String, Object>();
		AdministrationService adminService = Context.getAdministrationService();
		String propertyValue = adminService.getGlobalProperty(CoreAppsConstants.VISIT_TYPE_COLORS);
		try{
			if(StringUtils.isNotEmpty(propertyValue)){
				ArrayNode array = new ObjectMapper().readValue(propertyValue, ArrayNode.class);
				for (JsonNode node : array) {
					String visitTypeUuid= node.path("uuid").getTextValue();
					if(visitType.getUuid().equalsIgnoreCase(visitTypeUuid)){
						colorCode.put("uuid", visitTypeUuid);

						String color = node.path("color").getTextValue();
						colorCode.put("color", color);

						String shortName = node.path("shortName").getTextValue();
						colorCode.put("shortName", shortName);
					}
				}
			}
		} catch(IOException ex){
			LOG.error("Error retrieving visit type color codes, " + ex);
		}

		return colorCode;
	}

	/**
	 * Returns the color and short name attributes for a given visit type
	 *
	 * @param type VisitType
	 * @return
	 *
	 */
	public Map<String, Object> getVisitTypeColorAndShortName(VisitType type) {
		Map<String, Object> colorAndShortName = getVisitTypeColorCodes(type);

		// set default values
		if (colorAndShortName.get("color") == null ) {
			colorAndShortName.put("color", "grey");
		}
		if (colorAndShortName.get("shortName") == null) {
			colorAndShortName.put("shortName", "N/A");
		}
		colorAndShortName.put("name", type.getName());
		return colorAndShortName;
	}

	/**
	 *
	 * Returns a map of the color and short name for a given list of visits
	 *
	 * @param visits
	 * @return
	 */
	public Map<Integer, Map<String, Object>> getVisitColorAndShortName(List<VisitDomainWrapper> visits) {

		Map<Integer, Map<String, Object>> visitsWithAttr = new LinkedHashMap<Integer, Map<String, Object>>();

		for (VisitDomainWrapper visit : visits) {
			Map<String,Object>visitColorAndShortName = getVisitTypeColorAndShortName(visit.getVisit().getVisitType());
			visitsWithAttr.put(visit.getVisitId(), visitColorAndShortName);
		}
		return visitsWithAttr;
	}

	/**
	 * Sets encounters based on visit type and login location
	 *
	 * @param visit
	 * @param loginLocation
	 */
	public void setEncounterBasedOnVisitType(Visit visit, Location loginLocation) {
		setEncounterBasedOnVisitType(visit, loginLocation, null);
	}

	/**
	 * Create encounters based on visit type
	 *
	 * @param visit
	 * @param loginLocation
	 * @param previousType
	 */
	public void setEncounterBasedOnVisitType(Visit visit, Location loginLocation, VisitType previousType) {

		// all types are transfer type
		boolean isTransferType = true;

		if (visit.getVisitType() == previousType) {
			// visit type is not changed: do nothing
			return;
		}

		EncounterService es = Context.getEncounterService();
		VisitService vs = Context.getVisitService();
		Patient patient = visit.getPatient();
		Person person = Context.getUserContext().getAuthenticatedUser().getPerson();
		Encounter encounter = new Encounter();

		setTransferEncounter(visit, vs, es, encounter, patient, person, loginLocation, previousType, isTransferType);
	}


	/**
	 *
	 * Creates a transfer encounter when visit type is changed
	 *
	 * @param visit
	 * @param vs
	 * @param es
	 * @param encounter
	 * @param patient
	 * @param person
	 * @param loginLocation
	 * @param previousType
	 * @param isTransferType
	 */
	protected void setTransferEncounter(Visit visit, VisitService vs, EncounterService es, Encounter encounter, Patient patient, Person person, Location loginLocation,
			VisitType previousType, boolean isTransferType) {

		AdministrationService adminService = Context.getAdministrationService();
		String transferEncounterUuid = adminService.getGlobalProperty(CoreAppsConstants.TRANSFER_ENCOUNTER_TYPE_UUID);
		EncounterType transferEncounterType = es
				.getEncounterTypeByUuid(transferEncounterUuid);

		if ( previousType != null && isTransferType) {
			// add Transfer encounter
			encounter.setEncounterType(transferEncounterType);
			encounter.setPatient(patient);
			encounter.setEncounterDatetime(new Date());
			encounter.setProvider(person);
			encounter.setLocation(loginLocation);
			es.saveEncounter(encounter);
			visit.addEncounter(encounter);
			vs.saveVisit(visit);
		}
	}
}
