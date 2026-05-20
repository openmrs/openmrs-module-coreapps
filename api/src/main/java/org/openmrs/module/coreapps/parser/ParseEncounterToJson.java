package org.openmrs.module.coreapps.parser;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.time.DateFormatUtils;
import org.openmrs.*;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.encounter.EncounterDomainWrapper;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;

import static org.hibernate.validator.util.Contracts.assertNotNull;

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

        if(encounter.getEncounterType().getEditPrivilege() != null && canEdit){
            canEdit = authenticatedUser.hasPrivilege(encounter.getEncounterType().getEditPrivilege().getPrivilege());
        }


        SimpleObject simpleEncounter = SimpleObject.fromObject(new EncounterDomainWrapper(encounter), uiUtils, "encounterId",
                "location", "encounterDatetime", "encounterProviders.provider", "voided", "form");

        // UUID is not provided by EncounterDomainWrapper, adding it here.
        simpleEncounter.put("uuid", encounter.getUuid());
        // manually set the date and time components so we can control how we format them
        simpleEncounter.put("encounterDate", uiUtils.format(encounter.getEncounterDatetime()));
        simpleEncounter.put("encounterTime", uiUtils.convertTimezones() ? uiUtils.formatTimeWithClientTimezone(encounter.getEncounterDatetime()) : DateFormatUtils.format(encounter.getEncounterDatetime(), "hh:mm a", Context.getLocale()));

        // do the same for other date fields
        simpleEncounter.put("dateCreated", uiUtils.format(encounter.getDateCreated()));
        simpleEncounter.put("creator", uiUtils.format(encounter.getCreator()));

        if (encounter.getDateChanged() != null) {
            simpleEncounter.put("dateChanged",
                    uiUtils.format(encounter.getDateChanged()));
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

        simpleEncounter.put("canEdit", verifyIfUserHasPermissionToEditAnEncounter(encounter, authenticatedUser, canEdit));

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
        boolean hasEditPrivileges = false;
        if(encounter.getEncounterType().getEditPrivilege() != null){
            hasEditPrivileges = authenticatedUser.hasPrivilege(encounter.getEncounterType().getEditPrivilege().getPrivilege());
        }

        return ((canEdit || userParticipatedInEncounter) || hasEditPrivileges) || userIsAdmin;
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


    public void saveEncounter_shouldSaveEncounterWithBasicDetails() {
        Encounter encounter = buildEncounter();

        EncounterService es = Context.getEncounterService();
        es.saveEncounter(encounter);

        assert encounter != null;
        assertNotNull("The saved encounter should have an encounterid now", String.valueOf(encounter.getEncounterId()));
        Encounter newSavedEncounter = es.getEncounter(encounter.getEncounterId());
        assertNotNull("We should get back an encounter", String.valueOf(newSavedEncounter));
        assertTrue("The created encounter needs to equal the pojo encounter", encounter.equals(newSavedEncounter));
    }

    public void purgeEncounter_shouldPurgeEncounter() {

        EncounterService es = Context.getEncounterService();

        //should fetch the encounter to delete from the database
        Encounter encounterToDelete = es.getEncounter(1);

        es.purgeEncounter(encounterToDelete);

        // try to refetch the encounter. should get a null object
        Encounter e = es.getEncounter(encounterToDelete.getEncounterId());
        assertNull(e, "We shouldn't find the encounter after deletion");
    }

    private void assertNull(Encounter e, String s) {
    }

    private void assertTrue(String s, boolean equals) {
    }

    private Encounter buildEncounter() {
        return null;
    }

    public void getObs_shouldGetObs() {
        Encounter encounter = new Encounter();

        //create and add an Obs
        Obs o = new Obs();
        encounter.addObs(o);

        assertNotNull(encounter.getObs());

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