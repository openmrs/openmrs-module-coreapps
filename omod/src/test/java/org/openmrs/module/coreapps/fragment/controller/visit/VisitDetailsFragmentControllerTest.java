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

import static junit.framework.Assert.assertTrue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.Privilege;
import org.openmrs.Provider;
import org.openmrs.Role;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.api.EncounterService;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appframework.domain.Extension;
import org.openmrs.module.appframework.service.AppFrameworkService;
import org.openmrs.module.appui.TestUiUtils;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.coreapps.CoreAppsConstants;
import org.openmrs.module.coreapps.CoreAppsProperties;
import org.openmrs.module.coreapps.parser.ParseEncounterToJson;
import org.openmrs.module.emrapi.EmrApiConstants;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiFrameworkConstants;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.util.OpenmrsUtil;

public class VisitDetailsFragmentControllerTest {

   private static final String encounterTypeUuid = "abc-123-def-456";

   private static final String primaryEncounterRoleUuid = "ghi-789-jkl-012";

   @Test
   public void shouldReturnEncountersForVisit() throws ParseException {

      CoreAppsProperties coreAppsProperties = mock(CoreAppsProperties.class);
      when(coreAppsProperties.getPatientDashboardEncounterCount()).thenReturn(100);

      UiSessionContext sessionContext = mock(UiSessionContext.class);
      User authenticatedUser = new User();
      when(sessionContext.getCurrentUser()).thenReturn(authenticatedUser);
      AppContextModel appContextModel = new AppContextModel();
      when(sessionContext.generateAppContextModel()).thenReturn(appContextModel);

      authenticatedUser.addRole(createRoleForUser());

      AdministrationService administrationService = mock(AdministrationService.class);
      when(administrationService.getGlobalProperty(UiFrameworkConstants.GP_FORMATTER_DATETIME_FORMAT)).thenReturn(
            "dd.MMM.yyyy, HH:mm:ss");

      AppFrameworkService appFrameworkService = mock(AppFrameworkService.class);
      when(appFrameworkService.getExtensionsForCurrentUser(CoreAppsConstants.ENCOUNTER_TEMPLATE_EXTENSION))
      .thenReturn(generateMockEncounterTemplateExtensions());

      EncounterService encounterService = mock(EncounterService.class);

      Patient patient = mock(Patient.class);
      when(patient.getPatientId()).thenReturn(1);
      when(patient.isDead()).thenReturn(false);

      Visit visit = new Visit();
      Location visitLocation = new Location();
      visitLocation.setName("Visit Location");
      visit.setVisitId(1);
      visit.setLocation(visitLocation);
      visit.setStartDatetime(new Date());
      visit.setStopDatetime(new Date());
      visit.setCreator(authenticatedUser);
      visit.setPatient(patient);
      Location encounterLocation = new Location();
      encounterLocation.setName("Location");
      EncounterType encounterType = new EncounterType();
      encounterType.setName("Encounter Type");
      encounterType.setUuid(encounterTypeUuid);

      Provider primaryProvider = new Provider();
      primaryProvider.setName("Primary Provider");
      EncounterProvider primaryEncounterProvider = new EncounterProvider();
      primaryEncounterProvider.setProvider(primaryProvider);
      EncounterRole primaryEncounterRole = new EncounterRole();
      primaryEncounterRole.setUuid(primaryEncounterRoleUuid);
      primaryEncounterProvider.setEncounterRole(primaryEncounterRole);

      when(encounterService.getEncounterRoleByUuid(primaryEncounterRoleUuid)).thenReturn(primaryEncounterRole);

      Provider secondaryProvider = new Provider();
      secondaryProvider.setName("Secondary Provider");
      EncounterProvider secondaryEncounterProvider = new EncounterProvider();
      secondaryEncounterProvider.setProvider(secondaryProvider);
      secondaryEncounterProvider.setEncounterRole(new EncounterRole());

      Encounter encounter = new Encounter();
      encounter.setEncounterId(7);
      encounter.setEncounterDatetime(new Date());
      encounter.setLocation(encounterLocation);
      encounter.setEncounterType(encounterType);
      encounter.setEncounterProviders(new LinkedHashSet<EncounterProvider>());
      encounter.getEncounterProviders().add(secondaryEncounterProvider);   // add secondary provider so that it is the one that is "arbitrarily" returned first
      encounter.getEncounterProviders().add(primaryEncounterProvider);
      encounter.setCreator(authenticatedUser);
      encounter.setDateCreated(new Date());

      visit.addEncounter(encounter);

      UiUtils uiUtils = new TestUiUtils(administrationService);
      VisitDetailsFragmentController controller = new VisitDetailsFragmentController();

      SimpleObject response = controller.getVisitDetails(mock(EmrApiProperties.class), coreAppsProperties, appFrameworkService, encounterService,
            visit, null, null, uiUtils, sessionContext);
      List<SimpleObject> actualEncounters = (List<SimpleObject>) response.get("encounters");
      SimpleObject actualEncounter = actualEncounters.get(0);

      assertThat(response.get("startDatetime"), notNullValue());
      assertThat(response.get("stopDatetime"), notNullValue());
      assertThat((String) response.get("location"), is("Visit Location"));

      assertThat(actualEncounters.size(), is(1));
      assertThat((Integer) actualEncounter.get("encounterId"), is(7));
      assertThat((String) actualEncounter.get("location"), is("Location"));
      assertThat((SimpleObject) actualEncounter.get("encounterType"),
            isSimpleObjectWith("uuid", encounterType.getUuid(), "name", encounterType.getName()));
      assertThat(actualEncounter.get("encounterDatetime"), notNullValue());
      assertThat(actualEncounter.get("encounterDate"), notNullValue());
      assertThat(actualEncounter.get("encounterTime"), notNullValue());
      assertTrue((Boolean) actualEncounter.get("canEdit"));
      assertThat((String) actualEncounter.get("primaryProvider"), is("Primary Provider"));
      List<SimpleObject> actualProviders = (List<SimpleObject>) actualEncounter.get("encounterProviders");
      assertThat(actualProviders.size(), is(2));
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

   private Matcher<SimpleObject> isSimpleObjectWith(final Object... propertiesAndValues) {
      return new ArgumentMatcher<SimpleObject>() {

         @Override
         public boolean matches(Object o) {
            SimpleObject so = (SimpleObject) o;
            for (int i = 0; i < propertiesAndValues.length; i += 2) {
               String property = (String) propertiesAndValues[i];
               Object expectedValue = propertiesAndValues[i + 1];
               if (!OpenmrsUtil.nullSafeEquals(so.get(property), expectedValue)) {
                  return false;
               }
            }
            return true;
         }
      };
   }

   private List<Extension> generateMockEncounterTemplateExtensions() {
      Extension extension = new Extension();

      Map<String,Object> encounterTypeParams = new HashMap<String, Object>();
      encounterTypeParams.put("primaryEncounterRole", primaryEncounterRoleUuid);

      Map<String, Object> supportedEncounterTypes = new HashMap<String, Object>();
      supportedEncounterTypes.put(encounterTypeUuid, encounterTypeParams);

      Map<String, Object> extensionParams = new HashMap<String, Object>();
      extensionParams.put("supportedEncounterTypes", supportedEncounterTypes);

      extension.setExtensionParams(extensionParams);

      return Collections.singletonList(extension);
   }

   @Test
   public void shouldReturnEncounterWithinBoundaries() {
      ParseEncounterToJson parseEncounterToJson = mock(ParseEncounterToJson.class);
      when(parseEncounterToJson.createEncounterJSON(any(User.class), any(Encounter.class))).thenReturn(new SimpleObject());
      VisitDomainWrapper visitWrapper = mock(VisitDomainWrapper.class);
      when(visitWrapper.getSortedEncounters()).thenReturn(getTestEncounterList(100));

      VisitDetailsFragmentController controller = new VisitDetailsFragmentController();
      List<SimpleObject> encounters;

      encounters = controller.getEncounterListAsJson(parseEncounterToJson, visitWrapper, new User(), 0, 50);
      assertThat(encounters.size(), is(50));

      encounters = controller.getEncounterListAsJson(parseEncounterToJson, visitWrapper, new User(), 50, 50);
      assertThat(encounters.size(), is(50));

      encounters = controller.getEncounterListAsJson(parseEncounterToJson, visitWrapper, new User(), 0, 200);
      assertThat(encounters.size(), is(100));

      encounters = controller.getEncounterListAsJson(parseEncounterToJson, visitWrapper, new User(), 99, 25);
      assertThat(encounters.size(), is(1));

      encounters = controller.getEncounterListAsJson(parseEncounterToJson, visitWrapper, new User(), 100, 25);
      assertThat(encounters.isEmpty(), is(true));
   }

   private List<Encounter> getTestEncounterList(int size) {
      List<Encounter> encounters = new ArrayList<Encounter>();
      for (int i = 0; i < size; i++)
         encounters.add(new Encounter());
      return encounters;
   }
}