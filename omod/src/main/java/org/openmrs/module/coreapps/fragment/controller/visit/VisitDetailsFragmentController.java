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
import org.openmrs.EncounterType;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.api.APIAuthenticationException;
import org.openmrs.api.EncounterService;
import org.openmrs.api.VisitService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.feature.FeatureToggleProperties;
import org.openmrs.module.appui.UiSessionContext;
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

public class VisitDetailsFragmentController {

    public SimpleObject getVisitDetails(@SpringBean("featureToggles") FeatureToggleProperties featureToggleProperties,
                                        @SpringBean("emrApiProperties") EmrApiProperties emrApiProperties,
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
            encounters.add(createEncounterJSON(uiUtils, authenticatedUser, canDelete, canEdit, encounter));
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
                                            UiUtils uiUtils) {

        ParserEncounterIntoSimpleObjects parserEncounter = new ParserEncounterIntoSimpleObjects(encounter, uiUtils,
                emrApiProperties);

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
                return new FailureResult(ui.message("emr.patientDashBoard.deleteEncounter.notAllowed"));
            }
        }
        return new SuccessResult(ui.message("emr.patientDashBoard.deleteEncounter.successMessage"));
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
		if (visit != null ) {
			try {
				visitService.endVisit(visit, new Date());
			} catch (APIAuthenticationException e) {
				return new FailureResult(ui.message("coreapps.task.endVisit.notAllowed"));
			}
		}
		return new SuccessResult(ui.message("coreapps.task.endVisit.successMessage"));
	}

    private SimpleObject createEncounterJSON(UiUtils uiUtils, User authenticatedUser, boolean canDelete, boolean canEdit, Encounter encounter) {
        SimpleObject simpleEncounter = SimpleObject.fromObject(new EncounterDomainWrapper(encounter), uiUtils, "encounterId", "primaryProvider",
                "location", "encounterDatetime", "encounterProviders.provider", "voided", "form");

        // manually set the date and time components so we can control how we format them
        simpleEncounter.put("encounterDate",
                DateFormatUtils.format(encounter.getEncounterDatetime(), "dd MMM yyyy", Context.getLocale()));
        simpleEncounter.put("encounterTime",
                DateFormatUtils.format(encounter.getEncounterDatetime(), "hh:mm a", Context.getLocale()));

        EncounterType encounterType = encounter.getEncounterType();
        simpleEncounter.put("encounterType",
                SimpleObject.create("uuid", encounterType.getUuid(), "name", uiUtils.format(encounterType)));

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
}
