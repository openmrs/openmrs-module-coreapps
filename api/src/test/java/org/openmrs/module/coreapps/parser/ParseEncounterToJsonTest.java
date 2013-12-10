package org.openmrs.module.coreapps.parser;


import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.openmrs.*;
import org.openmrs.api.EncounterService;
import org.openmrs.api.context.Context;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.util.RoleConstants;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import java.util.Date;

@PrepareForTest(Context.class)
@RunWith(PowerMockRunner.class)
public class ParseEncounterToJsonTest {

    public static final int ENCOUNTER_ID = 7;
    private ParseEncounterToJson parseEncounterToJson;
    private EncounterService encounterService;
    private UiUtils uiUtils;
    private AppFrameworkService appFrameworkService;

    @Before
    public void setUp(){
        appFrameworkService = Mockito.mock(AppFrameworkService.class);
        uiUtils = Mockito.mock(UiUtils.class);
        encounterService = Mockito.mock(EncounterService.class);
        parseEncounterToJson = new ParseEncounterToJson(appFrameworkService, uiUtils, encounterService);
    }

    @Test
    public void userShouldNotEditEncounterWhenHasPrivilegeButDidNotParticipatedInTheEncounter(){

        User user = new User();
        user.addRole(createRoleForUser());

        Encounter encounter = createEncounter();
        encounter.setCreator(new User());

        SimpleObject encounterJSON = parseEncounterToJson.createEncounterJSON(user, encounter);

        Assert.assertFalse((Boolean) encounterJSON.get("canEdit"));

    }

    @Test
    public void userShouldEditEncounterWhenHasPrivilegeAndParticipatedInTheEncounter(){

        User user= new User();
        user.addRole(createRoleForUser());

        user.setPerson(new Person());

        Encounter encounter = createEncounter();
        Provider provider = new Provider();
        provider.setPerson(user.getPerson());
        PowerMockito.mockStatic(Context.class);
        Mockito.when(Context.getAuthenticatedUser()).thenReturn(new User());
        encounter.addProvider(new EncounterRole(5), provider);
        encounter.setCreator(new User());

        SimpleObject encounterJSON = parseEncounterToJson.createEncounterJSON(user, encounter);

        Assert.assertTrue((Boolean) encounterJSON.get("canEdit"));
    }

    @Test
    public void userShouldNotEditEncounterWhenHasPrivelegeAndDidNotParticipateInTheEncounter(){

        User user = new User();
        user.addRole(createRoleForUser());

        Encounter encounter = createEncounter();
        encounter.setCreator(new User());

        SimpleObject encounterJSON = parseEncounterToJson.createEncounterJSON(user,encounter);

        Assert.assertFalse((Boolean) encounterJSON.get("canEdit"));

    }

    @Test
    public void userShouldNotEditEncounterWhenHasNotPrivilegeAndDidNotParticipateInTheEncounter(){

        User user = new User();


        Encounter encounter = createEncounter();
        encounter.setCreator(new User());

        SimpleObject encounterJSON = parseEncounterToJson.createEncounterJSON(user,encounter);
        Assert.assertFalse((Boolean) encounterJSON.get("canEdit"));

    }

    @Test
    public void userShouldEditEncounterWhenIsAdmin(){

        User user = new User();
        user.addRole(createRoleAdminForUser());

        Encounter encounter = createEncounter();
        encounter.setCreator(new User());

        SimpleObject encounterJSON = parseEncounterToJson.createEncounterJSON(user,encounter);
        Assert.assertTrue((Boolean) encounterJSON.get("canEdit"));

    }

    private Role createRoleAdminForUser() {
        Role role = new Role();
        role.setRole(RoleConstants.SUPERUSER);
        return role;
    }

    private Encounter createEncounter() {
        Encounter encounter = new Encounter();
        encounter.setEncounterId(ENCOUNTER_ID);
        encounter.setEncounterDatetime(new Date());
        EncounterType encounterType = new EncounterType();
        encounterType.setUuid("encounterTypeUuid");
        encounter.setEncounterType(encounterType);
        return encounter;
    }


    private Role createRoleForUser() {
        Role role = new Role();
        role.setRole("Test");
        role.addPrivilege(createPrivilegeToEditEncounters());
        return role;
    }

    private Privilege createPrivilegeToEditEncounters() {
        Privilege privilege = new Privilege();
        privilege.setPrivilege(EmrApiConstants.PRIVILEGE_EDIT_ENCOUNTER);
        return privilege;
    }
}
