package org.openmrs.module.coreapps.parser;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;

public class ParseEncounterToJson {

    private AppFrameworkService appFrameworkService;
    private UiUtils uiUtils;
    private EncounterService encounterService;

    public ParseEncounterToJson(AppFrameworkService appFrameworkService, UiUtils uiUtils, EncounterService encounterService) {
        this.appFrameworkService = appFrameworkService;
        this.uiUtils = uiUtils;
        this.encounterService = encounterService;
    }


    public SimpleObject createEncounterJSON(User authenticatedUser, Encounter encounter) {

        boolean canDelete = authenticatedUser.hasPrivilege(EmrApiConstants.PRIVILEGE_DELETE_ENCOUNTER);
        boolean canEdit = authenticatedUser.hasPrivilege(EmrApiConstants.PRIVILEGE_EDIT_ENCOUNTER);

        SimpleObject simpleEncounter = SimpleObject.fromObject(new EncounterDomainWrapper(encounter), uiUtils, "encounterId",
                "location", "encounterDatetime", "encounterProviders.provider", "voided", "form");

        // UUID is not provided by EncounterDomainWrapper, adding it here.
        simpleEncounter.put("uuid", encounter.getUuid());
        // manually set the date and time components so we can control how we format them
        simpleEncounter.put("encounterDate",
                DateFormatUtils.format(encounter.getEncounterDatetime(), "dd MMM yyyy", Context.getLocale()));
        simpleEncounter.put("encounterTime",
                DateFormatUtils.format(encounter.getEncounterDatetime(), "hh:mm a", Context.getLocale()));
    
        // do the same for other date fields
        simpleEncounter.put("dateCreated",
                DateFormatUtils.format(encounter.getDateCreated(), "dd MMM yyyy", Context.getLocale()));
        simpleEncounter.put("creator", uiUtils.format(encounter.getCreator()));
        
        if (encounter.getDateChanged() != null) {
            simpleEncounter.put("dateChanged",
                    DateFormatUtils.format(encounter.getDateChanged(), "dd MMM yyyy", Context.getLocale()));
        }
    
        if (encounter.getChangedBy() != null) {
            simpleEncounter.put("changedBy", uiUtils.format(encounter.getChangedBy()));
        }
        EncounterType encounterType = encounter.getEncounterType();
        simpleEncounter.put("encounterType",
                SimpleObject.create("uuid", encounterType.getUuid(), "name", uiUtils.format(encounterType)));

        // determine the provider
        Provider primaryProvider = null;

        // if a primary encounter role has been specified, get the provider for that role
        EncounterRole primaryEncounterRole = getPrimaryEncounterRoleForEncounter(encounter);
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
        } else {
            simpleEncounter.put("canEdit", false);
        }

        if (encounter.getVisit() != null) {
            simpleEncounter.put("visitId", encounter.getVisit().getId());
        }
        return simpleEncounter;
    }

    private boolean verifyIfUserHasPermissionToEditAnEncounter(Encounter encounter, User authenticatedUser,
                                                               boolean canEdit) {
        EncounterDomainWrapper encounterDomainWrapper = new EncounterDomainWrapper(encounter);
        boolean userParticipatedInEncounter = encounterDomainWrapper.participatedInEncounter(authenticatedUser);
        boolean userIsAdmin = authenticatedUser.isSuperUser();
        return (canEdit || userParticipatedInEncounter) || userIsAdmin;
    }

    private EncounterRole getPrimaryEncounterRoleForEncounter(Encounter encounter) {

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

    private boolean verifyIfUserHasPermissionToDeleteAnEncounter(Encounter encounter, User authenticatedUser,
                                                                 boolean canDelete) {
        EncounterDomainWrapper encounterDomainWrapper = new EncounterDomainWrapper(encounter);
        boolean userParticipatedInEncounter = encounterDomainWrapper.participatedInEncounter(authenticatedUser);
        return canDelete || userParticipatedInEncounter;
    }
}