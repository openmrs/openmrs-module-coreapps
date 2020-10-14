/**
 * The contents of this file are subject to the OpenMRS Public License
 * Version 1.0 (the "License"); you may not use this file except in
 * compliance with the License. You may obtain a copy of the License at
 * http://license.openmrs.org
 *
 * Software distributed under the License is distributed on an "AS IS"
 * basis, WITHOUT WARRANTY OF ANY KIND, either express or implied. See the
 * License for the specific language governing rights and limitations
 * under the License.
 *
 * Copyright (C) OpenMRS, LLC.  All Rights Reserved.
 */
package org.openmrs.module.coreapps.web.resource;

import org.openmrs.Concept;
import org.openmrs.Obs;
import org.openmrs.Patient;
import org.openmrs.Person;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.api.context.Context;
import org.openmrs.module.webservices.rest.web.RequestContext;
import org.openmrs.module.webservices.rest.web.RestConstants;
import org.openmrs.module.webservices.rest.web.annotation.Resource;
import org.openmrs.module.webservices.rest.web.api.RestService;
import org.openmrs.module.webservices.rest.web.resource.api.PageableResult;
import org.openmrs.module.webservices.rest.web.resource.impl.EmptySearchResult;
import org.openmrs.module.webservices.rest.web.resource.impl.NeedsPaging;
import org.openmrs.module.webservices.rest.web.response.ResponseException;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_11.ObsResource1_11;
import org.openmrs.module.webservices.rest.web.v1_0.resource.openmrs1_8.PatientResource1_8;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

@Resource(name = RestConstants.VERSION_1 + "/latestobs", order = 1, supportedClass = Obs.class, supportedOpenmrsVersions = {
		"1.8.*", "1.9.*", "1.10.*", "1.11.*", "1.12.*", "2.0.*", "2.1.*", "2.2.*", "2.3.*", "2.4.*" })
public class LatestObsResource extends ObsResource1_11 {

	@Override
	protected NeedsPaging<Obs> doGetAll(RequestContext context) throws ResponseException {
		return new NeedsPaging<Obs>(new ArrayList<Obs>(), context);
	}
	
	@Override
	protected PageableResult doSearch(RequestContext context) {
		
		List<Person> who = new Vector<Person>();
		
		String patientUuid = context.getRequest().getParameter("patient");
		if (patientUuid != null) {
			Patient patient = ((PatientResource1_8) Context.getService(RestService.class).getResourceBySupportedClass(
			    Patient.class)).getByUniqueId(patientUuid);
			
			if (patient == null) {
				return new EmptySearchResult();
			}
			
			who.add(patient);
		}
		
		ConceptService conceptService = Context.getConceptService();
		ObsService obsService = Context.getObsService();
		
		String conceptUuids = context.getRequest().getParameter("concept");
		int nLatestObs = context.getRequest().getParameter("nLatestObs") != null ? Integer.parseInt(context.getRequest().getParameter("nLatestObs")) : 1;
		if (conceptUuids != null) {
			String[] conceptUuidList = conceptUuids.split(",");

			List<String> sort = new ArrayList<String>();
			sort.add("obsDatetime");
			
			List<Obs> obsList = new ArrayList<Obs>();
			
			for (String conceptUuid : conceptUuidList) {
				Concept concept = conceptService.getConceptByUuid(conceptUuid.trim());
				if (concept == null) {
					continue;
				}
				
				List<Concept> questions = new Vector<Concept>();
				questions.add(concept);
				
				List<Obs> latestObs = obsService.getObservations(who, null, questions, null, null, null, sort, nLatestObs, null, null, null, false);
				if (latestObs.size() > 0) {
					obsList.addAll(latestObs);
				}
			}

			return new NeedsPaging<Obs>(obsList, context);
		}
		
		return new EmptySearchResult();
	}
}