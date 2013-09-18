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
package org.openmrs.module.coreapps.page.interceptor;

import java.util.List;

import org.openmrs.Patient;
import org.openmrs.api.PatientService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.emrapi.event.ApplicationEventService;
import org.openmrs.ui.framework.interceptor.PageRequestInterceptor;
import org.openmrs.ui.framework.page.PageAction;
import org.openmrs.ui.framework.page.PageContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Interceptor that provides a hook for pages that require a patient viewed event to be published
 * whenever it is requested
 */
@Component
public class PatientViewedPageRequestInterceptor implements PageRequestInterceptor {
	
	@Autowired
	private AppFrameworkService appFrameworkService;
	
	@Autowired
	private ApplicationEventService applicationEventService;
	
	@Autowired
	private PatientService patientService;
	
	/**
	 * @see PageRequestInterceptor#beforeHandleRequest(org.openmrs.ui.framework.page.PageContext)
	 * @param pageContext
	 * @throws PageAction
	 */
	@Override
	public void beforeHandleRequest(PageContext pageContext) throws PageAction {
		if (Context.getAuthenticatedUser() != null) {
			
			List<Extension> extensions = appFrameworkService.getExtensionsForCurrentUser("coreapps.patientViewed");
			String patientIdParamName = null;
			boolean publishEvent = false;
			String providerName = pageContext.getRequest().getProviderName();
			String pageName = pageContext.getRequest().getPageName();
			if (("coreapps".equals(providerName) && ("clinicianfacing/patient".equals(pageName)) || "patientdashboard/patientDashboard"
			        .equals(pageName))) {
				patientIdParamName = "patientId";
				publishEvent = true;
			}
			if (!publishEvent) {
				for (Extension ext : extensions) {
					Object extPageName = ext.getExtensionParams().get("page");
					Object extPageProvider = ext.getExtensionParams().get("provider");
					if (providerName.equals(extPageName) && pageName.equals(extPageProvider)) {
						//TODO cache found ProviderAndName not to execute this every time
						Object extPatientIdRequestParamName = ext.getExtensionParams().get("patientIdRequestParamName");
						if (extPatientIdRequestParamName != null) {
							patientIdParamName = extPatientIdRequestParamName.toString();
							publishEvent = true;
						}
						//No need to look ast the remaining ext
						break;
					}
				}
			}
			
			if (publishEvent) {
				Object patientIdOrUuid = pageContext.getRequest().getAttribute(patientIdParamName);
				if (patientIdOrUuid != null) {
					String patientId = patientIdOrUuid.toString();
					Patient patient = patientService.getPatientByUuid(patientId);
					if (patient == null) {
						try {
							patient = patientService.getPatient(Integer.valueOf(patientId));
						}
						catch (NumberFormatException e) {
							
						}
					}
					if (patient != null) {
						applicationEventService.patientViewed(patient, Context.getAuthenticatedUser());
					}
				}
			}
		}
	}
}
