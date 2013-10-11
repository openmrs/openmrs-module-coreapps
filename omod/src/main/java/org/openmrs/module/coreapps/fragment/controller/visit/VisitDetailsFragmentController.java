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
package org.openmrs.module.coreapps.fragment.controller.visit;

import org.apache.commons.lang.time.DateFormatUtils;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.LocationService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.feature.FeatureToggleProperties;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.springframework.web.bind.annotation.RequestParam;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VisitDetailsFragmentController {

    public SimpleObject getVisitDetails(@SpringBean("emrApiProperties") EmrApiProperties emrApiProperties,
                                        @SpringBean("appFrameworkService") AppFrameworkService appFrameworkService,
                                        @SpringBean("encounterService") EncounterService encounterService,
                                        @RequestParam("visitId") Visit visit, UiUtils uiUtils,
                                        UiSessionContext sessionContext) throws ParseException {

        SimpleObject simpleObject = SimpleObject.fromObject(visit, uiUtils, "id", "location");

        User authenticatedUser = sessionContext.getCurrentUser();

        boolean canDelete = authenticatedUser.hasPrivilege(EmrApiConstants.PRIVILEGE_DELETE_ENCOUNTER);
        boolean canEdit = authenticatedUser.hasPrivilege(EmrApiConstants.PRIVILEGE_EDIT_ENCOUNTER);
        boolean canDeleteVisit = authenticatedUser.hasPrivilege(EmrApiConstants.PRIVILEGE_DELETE_VISIT);

        Date startDatetime = visit.getStartDatetime();
        Date stopDatetime = visit.getStopDatetime();

        simpleObject.put("startDatetime", DateFormatUtils.format(startDatetime, "dd MMM yyyy hh:mm a", Context.getLocale()));

        if (stopDatetime != null) {
            simpleObject.put("stopDatetime",
                    DateFormatUtils.format(stopDatetime, "dd MMM yyyy hh:mm a", Context.getLocale()));
        } else {
            simpleObject.put("stopDatetime", null);
        }

        VisitDomainWrapper visitWrapper = new VisitDomainWrapper(visit, emrApiProperties);

        List<SimpleObject> encounters = new ArrayList<SimpleObject>();
        for (Encounter encounter : visitWrapper.getSortedEncounters()) {
            encounters.add(createEncounterJSON(appFrameworkService, encounterService, uiUtils, authenticatedUser,
                    canDelete, canEdit, encounter));
        }
        simpleObject.put("encounters", encounters);

        simpleObject.put("admitted", visitWrapper.isAdmitted());
        simpleObject.put("canDeleteVisit", verifyIfUserHasPermissionToDeleteVisit(visit, authenticatedUser, canDeleteVisit));

        return simpleObject;
    }

    private boolean verifyIfUserHasPermissionToDeleteVisit(Visit visit, User authenticatedUser, boolean canDeleteVisit) {
        VisitDomainWrapper visitDomainWrapper = new VisitDomainWrapper(visit);
        if ( visitDomainWrapper.hasEncounters() ){
            // allowed to delete only empty visits
            return false;
        }
        boolean userParticipatedInVisit = visitDomainWrapper.verifyIfUserIsTheCreatorOfVisit(authenticatedUser);
        return canDeleteVisit || userParticipatedInVisit;
    }

    public SimpleObject getEncounterDetails(@RequestParam("encounterId") Encounter encounter,
                                            @SpringBean("emrApiProperties") EmrApiProperties emrApiProperties,
                                            @SpringBean("locationService") LocationService locationService,
                                            UiUtils uiUtils) {

        ParserEncounterIntoSimpleObjects parserEncounter = new ParserEncounterIntoSimpleObjects(encounter, uiUtils,
                emrApiProperties, locationService);

        ParsedObs parsedObs = parserEncounter.parseObservations(uiUtils.getLocale());
        List<SimpleObject> orders = parserEncounter.parseOrders();

        return SimpleObject.create("observations", parsedObs.getObs(), "orders", orders, "diagnoses",
                parsedObs.getDiagnoses(), "dispositions", parsedObs.getDispositions());
    }

    public FragmentActionResult deleteEncounter(UiUtils ui,
                                                @SpringBean("featureToggles") FeatureToggleProperties featureToggleProperties,
                                                @RequestParam("encounterId") Encounter encounter,
                                                @SpringBean("encounterService") EncounterService encounterService,
                                                UiSessionContext sessionContext) {

        if (encounter != null) {
            User authenticatedUser = sessionContext.getCurrentUser();
            boolean canDelete = authenticatedUser.hasPrivilege(EmrApiConstants.PRIVILEGE_DELETE_ENCOUNTER);
            if (verifyIfUserHasPermissionToDeleteAnEncounter(encounter, authenticatedUser, canDelete)) {
                encounterService.voidEncounter(encounter, "delete encounter");
                encounterService.saveEncounter(encounter);
            } else {
                return new FailureResult(ui.message("coreapps.patientDashBoard.deleteEncounter.notAllowed"));
            }
        }
        return new SuccessResult(ui.message("coreapps.patientDashBoard.deleteEncounter.successMessage"));
    }

    public FragmentActionResult deleteVisit(UiUtils ui,
                                                @RequestParam("visitId") Visit visit,
                                                @SpringBean("visitService") VisitService visitService,
                                                UiSessionContext sessionContext) {

        if (visit != null ) {
            User authenticatedUser = sessionContext.getCurrentUser();
            boolean canDeleteVisit = authenticatedUser.hasPrivilege(EmrApiConstants.PRIVILEGE_DELETE_VISIT);
            if (verifyIfUserHasPermissionToDeleteVisit(visit, authenticatedUser, canDeleteVisit)) {
                visitService.voidVisit(visit, "delete visit");
                visitService.saveVisit(visit);
            } else {
                return new FailureResult(ui.message("emr.patientDashBoard.deleteVisit.notAllowed"));
            }
        }
        return new SuccessResult(ui.message("coreapps.task.deleteVisit.successMessage"));
    }

	public FragmentActionResult endVisit(UiUtils ui,
											@RequestParam("visitId") Visit visit,
											@SpringBean("visitService") VisitService visitService,
											UiSessionContext sessionContext) {
		User currentUser = sessionContext.getCurrentUser();
		if (currentUser == null || !currentUser.hasPrivilege(CoreAppsConstants.PRIVILEGE_END_VISIT)) {
			return new FailureResult(ui.message("coreapps.task.endVisit.notAllowed"));
		}

		if (visit != null ) {
			try {
				visitService.endVisit(visit, new Date());
			} catch (APIAuthenticationException e) {
				return new FailureResult(ui.message("coreapps.task.endVisit.notAllowed"));
			}
		}
		return new SuccessResult(ui.message("coreapps.task.endVisit.successMessage"));
	}

    private SimpleObject createEncounterJSON(AppFrameworkService appFrameworkService, EncounterService encounterService,
                                             UiUtils uiUtils, User authenticatedUser, boolean canDelete, boolean canEdit, Encounter encounter) {

        SimpleObject simpleEncounter = SimpleObject.fromObject(new EncounterDomainWrapper(encounter), uiUtils, "encounterId",
                "location", "encounterDatetime", "encounterProviders.provider", "voided", "form");

        // manually set the date and time components so we can control how we format them
        simpleEncounter.put("encounterDate",
                DateFormatUtils.format(encounter.getEncounterDatetime(), "dd MMM yyyy", Context.getLocale()));
        simpleEncounter.put("encounterTime",
                DateFormatUtils.format(encounter.getEncounterDatetime(), "hh:mm a", Context.getLocale()));

        EncounterType encounterType = encounter.getEncounterType();
        simpleEncounter.put("encounterType",
                SimpleObject.create("uuid", encounterType.getUuid(), "name", uiUtils.format(encounterType)));

        // determine the provider
        Provider primaryProvider = null;

        // if a primary encounter role has been specified, get the provider for that role
        EncounterRole primaryEncounterRole = getPrimaryEncounterRoleForEncounter(appFrameworkService, encounterService, encounter);
        if (primaryEncounterRole != null) {
            Set<Provider> providers = encounter.getProvidersByRole(primaryEncounterRole);
            if (providers != null && !providers.isEmpty()) {
                // for now, if there are multiple providers with this role, return an arbitrary one
                primaryProvider = providers.iterator().next();
            }
        }

        // otherwise, just pick an arbitrary (non-voided) provider
        if (primaryProvider == null) {
            primaryProvider = getFirstNonVoidedProvider(encounter);
        }
        simpleEncounter.put("primaryProvider", uiUtils.format(primaryProvider));

        if (verifyIfUserHasPermissionToDeleteAnEncounter(encounter, authenticatedUser, canDelete)) {
            simpleEncounter.put("canDelete", true);
        }

        if (verifyIfUserHasPermissionToEditAnEncounter(encounter, authenticatedUser, canEdit)) {
            simpleEncounter.put("canEdit", true);
        }
        if ( encounter.getVisit() != null ){
            simpleEncounter.put("visitId" , encounter.getVisit().getId());
        }
        return simpleEncounter;
    }

    private boolean verifyIfUserHasPermissionToDeleteAnEncounter(Encounter encounter, User authenticatedUser,
                                                                   boolean canDelete) {
        EncounterDomainWrapper encounterDomainWrapper = new EncounterDomainWrapper(encounter);
        boolean userParticipatedInEncounter = encounterDomainWrapper.participatedInEncounter(authenticatedUser);
        return canDelete || userParticipatedInEncounter;
    }

    private boolean verifyIfUserHasPermissionToEditAnEncounter(Encounter encounter, User authenticatedUser,
                                                                 boolean canEdit) {
        EncounterDomainWrapper encounterDomainWrapper = new EncounterDomainWrapper(encounter);
        boolean userParticipatedInEncounter = encounterDomainWrapper.participatedInEncounter(authenticatedUser);
        return canEdit || userParticipatedInEncounter;
    }

    private EncounterRole getPrimaryEncounterRoleForEncounter(AppFrameworkService appFrameworkService,
                                                              EncounterService encounterService, Encounter encounter) {

        // TODO this whole thing seems a bit back-to-front
        List<Extension> encounterTemplateExtensions = appFrameworkService
                .getExtensionsForCurrentUser(CoreAppsConstants.ENCOUNTER_TEMPLATE_EXTENSION);

        for (Extension extension : encounterTemplateExtensions) {
            Object supportedEncounterTypes = extension.getExtensionParams().get("supportedEncounterTypes");

            if (supportedEncounterTypes != null) {
                for (String encounterTypeUuid : ((Map<String,Object>) supportedEncounterTypes).keySet()) {
                    if (encounterTypeUuid.equals(encounter.getEncounterType().getUuid())) {
                        Object primaryEncounterRole = ((Map<String,Object>) ((Map<String, Object>) supportedEncounterTypes)
                                .get(encounterTypeUuid)).get("primaryEncounterRole");
                        if (primaryEncounterRole != null) {
                            return encounterService.getEncounterRoleByUuid((String) primaryEncounterRole);
                        }
                        else {
                            return null;
                        }
                    }
                }
            }
        }

        return null;
    }

    private Provider getFirstNonVoidedProvider(Encounter encounter) {
        for (EncounterProvider provider : encounter.getEncounterProviders()) {
            if (!provider.isVoided()) {
                return provider.getProvider();
            }
        }
        return null;
    }
}
