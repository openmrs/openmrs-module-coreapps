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
package org.openmrs.module.coreapps.page.controller.clinicianfacing;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PersonAttribute;
import org.openmrs.PersonAttributeType;
import org.openmrs.Program;
import org.openmrs.api.EncounterService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appframework.domain.AppDescriptor;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.module.coreapps.CoreAppsProperties;
import org.openmrs.module.coreapps.contextmodel.PatientContextModel;
import org.openmrs.module.coreapps.contextmodel.VisitContextModel;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.event.ApplicationEventService;
import org.openmrs.module.emrapi.patient.PatientDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.annotation.InjectBeans;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.page.PageModel;
import org.openmrs.ui.framework.page.Redirect;
import org.springframework.web.bind.annotation.RequestParam;
import org.openmrs.module.webservices.rest.web.representation.Representation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PatientPageController {

    public Object controller(@RequestParam("patientId") Patient patient, PageModel model,
                             @RequestParam(required = false, value = "app") AppDescriptor app,
                             @RequestParam(required = false, value = "dashboard") String dashboard,
                             @InjectBeans PatientDomainWrapper patientDomainWrapper,
                             @SpringBean("adtService") AdtService adtService,
                             @SpringBean("visitService") VisitService visitService,
                             @SpringBean("encounterService") EncounterService encounterService,
                             @SpringBean("emrApiProperties") EmrApiProperties emrApiProperties,
                             @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
                             @SpringBean("applicationEventService") ApplicationEventService applicationEventService,
                             @SpringBean("coreAppsProperties") CoreAppsProperties coreAppsProperties,
                             UiSessionContext sessionContext) {
    
      
        
       try{
           if (!Context.hasPrivilege(CoreAppsConstants.PRIVILEGE_PATIENT_DASHBOARD)) {
            return new Redirect("coreapps", "noAccess", "");
        }

         else if (patient.isVoided() || patient.isPersonVoided()) {
            return new Redirect("coreapps", "patientdashboard/deletedPatient", "patientId=" + patient.getId());
        }
      
        if (StringUtils.isEmpty(dashboard)) {
            dashboard = "patientDashboard";
        }

        patientDomainWrapper.setPatient(patient);
        model.addAttribute("patient", patientDomainWrapper);
        model.addAttribute("app", app);

        Location visitLocation = null;
        try {
            visitLocation = adtService.getLocationThatSupportsVisits(sessionContext.getSessionLocation());
        }
        catch (IllegalArgumentException ex) {
            // location does not support visits
        }

        VisitDomainWrapper activeVisit = null;
        if (visitLocation != null) {
            activeVisit = adtService.getActiveVisit(patient, visitLocation);
        }
        model.addAttribute("activeVisit", activeVisit);

        AppContextModel contextModel = sessionContext.generateAppContextModel();
        contextModel.put("patient", new PatientContextModel(patient));
        contextModel.put("visit", activeVisit == null ? null : new VisitContextModel(activeVisit));

        List<EncounterType> encounterTypes = new ArrayList<EncounterType>();
        List<Encounter> encounters = encounterService.getEncountersByPatient(patient);
        for (Encounter encounter : encounters) {
            encounterTypes.add(encounter.getEncounterType());
        }
        contextModel.put("encounterTypes", ConversionUtil.convertToRepresentation(encounterTypes, Representation.DEFAULT));

        List<Program> programs = new ArrayList<Program>();
        List<PatientProgram> patientPrograms = Context.getProgramWorkflowService().getPatientPrograms(patient, null, null, null, null, null, false);
        for (PatientProgram patientProgram : patientPrograms) {
         programs.add(patientProgram.getProgram());
        }
        contextModel.put("patientPrograms", ConversionUtil.convertToRepresentation(programs, Representation.DEFAULT));

        model.addAttribute("appContextModel", contextModel);

        List<Extension> overallActions = appFrameworkService.getExtensionsForCurrentUser(dashboard + ".overallActions", contextModel);
        Collections.sort(overallActions);
        model.addAttribute("overallActions", overallActions);

        List<Extension> visitActions;
        if (activeVisit == null) {
            visitActions = new ArrayList<Extension>();
        } else {
            visitActions = appFrameworkService.getExtensionsForCurrentUser(dashboard + ".visitActions", contextModel);
            Collections.sort(visitActions);
        }
        model.addAttribute("visitActions", visitActions);

        List<Extension> includeFragments = appFrameworkService.getExtensionsForCurrentUser(dashboard + ".includeFragments", contextModel);
        Collections.sort(includeFragments);
        model.addAttribute("includeFragments", includeFragments);

        List<Extension> firstColumnFragments = appFrameworkService.getExtensionsForCurrentUser(dashboard + ".firstColumnFragments", contextModel);
        Collections.sort(firstColumnFragments);
        model.addAttribute("firstColumnFragments", firstColumnFragments);

        List<Extension> secondColumnFragments = appFrameworkService.getExtensionsForCurrentUser(dashboard + ".secondColumnFragments", contextModel);
        Collections.sort(secondColumnFragments);
        model.addAttribute("secondColumnFragments", secondColumnFragments);

        List<Extension> otherActions = appFrameworkService.getExtensionsForCurrentUser(
                ("patientDashboard".equals(dashboard) ? "clinicianFacingPatientDashboard" : dashboard) + ".otherActions", contextModel);
        Collections.sort(otherActions);
        model.addAttribute("otherActions", otherActions);

        model.addAttribute("baseDashboardUrl", coreAppsProperties.getDashboardUrl());  // used for breadcrumbs to link back to the base dashboard in the case when this is used to render a context-specific dashboard
        model.addAttribute("dashboard", dashboard);
        
        model.addAttribute("breadCrumbsDetails", getBreadCrumbsDetails(patient));
        
        model.addAttribute("breadCrumbsFormatters", Context.getAdministrationService().getGlobalProperty("breadCrumbs.formatters", "(;, ;)").split(";"));

        applicationEventService.patientViewed(patient, sessionContext.getCurrentUser());

        return null;

         }catch(NullPointerException x){
          return new Redirect("coreapps", "patientdashboard/patientNotFound", "patientId=" + "Not Found");  
       }
    
    }
    
    /**
     * @since 2.24.0
     * 
     * Gets person attributes containing additional breadcrumbs details defined by 'breadCrumbs.details.uuids' global
     * property
     * 
     * @param patient whose breadcrumbs details are required from person attribute types specified by global property
     * @return list of person attributes with breadcrumbs details if present or null otherwise
     */
    private List<PersonAttribute> getBreadCrumbsDetails(Patient patient) {
    	// Get person names from person attribute types available in global properties
    	String breadCrumbsDetailsAttrTypeUuids = Context.getAdministrationService().getGlobalProperty("breadCrumbs.details.personAttr.uuids");
    	List<PersonAttribute> personNamePersonAttrs = new ArrayList<PersonAttribute>();
    	if (StringUtils.isNotBlank(breadCrumbsDetailsAttrTypeUuids)) {
    		for (String breadCrumbDetailAttrTypeUuid : breadCrumbsDetailsAttrTypeUuids.split(",")) {
    			PersonAttributeType pat = Context.getPersonService().getPersonAttributeTypeByUuid(breadCrumbDetailAttrTypeUuid);
    			PersonAttribute pa = patient.getAttribute(pat);
    			if (pa != null) {
    				personNamePersonAttrs.add(pa);
    			}
    		}
    	}
    	return CollectionUtils.isNotEmpty(personNamePersonAttrs) ? personNamePersonAttrs : null;
    } 
}
