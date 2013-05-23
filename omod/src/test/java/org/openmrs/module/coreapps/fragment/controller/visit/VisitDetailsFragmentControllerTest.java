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

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.text.ParseException;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;

import org.hamcrest.Matcher;
import org.junit.Test;
import org.mockito.ArgumentMatcher;
import org.openmrs.Encounter;
import org.openmrs.EncounterProvider;
import org.openmrs.EncounterRole;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Provider;
import org.openmrs.User;
import org.openmrs.Visit;
import org.openmrs.api.AdministrationService;
import org.openmrs.module.appframework.feature.FeatureToggleProperties;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.test.TestUiUtils;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiFrameworkConstants;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.util.OpenmrsUtil;

public class VisitDetailsFragmentControllerTest {
	
	@Test
	public void shouldReturnEncountersForVisit() throws ParseException {
		UiSessionContext sessionContext = mock(UiSessionContext.class);
		User authenticatedUser = new User();
		FeatureToggleProperties featureToggleProperties = mock(FeatureToggleProperties.class);
		
		when(sessionContext.getCurrentUser()).thenReturn(authenticatedUser);
		when(featureToggleProperties.isFeatureEnabled(anyString())).thenReturn(true);
		
		AdministrationService administrationService = mock(AdministrationService.class);
		when(administrationService.getGlobalProperty(UiFrameworkConstants.GP_FORMATTER_DATETIME_FORMAT)).thenReturn(
		    "dd.MMM.yyyy, HH:mm:ss");
		
		Visit visit = new Visit();
		Location visitLocation = new Location();
		visitLocation.setName("Visit Location");
		visit.setLocation(visitLocation);
		visit.setStartDatetime(new Date());
		visit.setStopDatetime(new Date());
		Location encounterLocation = new Location();
		encounterLocation.setName("Location");
		EncounterType encounterType = new EncounterType();
		encounterType.setName("Encounter Type");
		encounterType.setUuid("abc-123-def-456");
		Provider provider = new Provider();
		provider.setName("Provider");
		EncounterProvider encounterProvider = new EncounterProvider();
		encounterProvider.setProvider(provider);
		encounterProvider.setEncounterRole(new EncounterRole());
		
		Encounter encounter = new Encounter();
		encounter.setEncounterId(7);
		encounter.setEncounterDatetime(new Date());
		encounter.setLocation(encounterLocation);
		encounter.setEncounterType(encounterType);
		encounter.setEncounterProviders(new LinkedHashSet<EncounterProvider>());
		encounter.getEncounterProviders().add(encounterProvider);
		encounter.setCreator(authenticatedUser);
		
		visit.addEncounter(encounter);
		
		UiUtils uiUtils = new TestUiUtils(administrationService);
		VisitDetailsFragmentController controller = new VisitDetailsFragmentController();
		
		SimpleObject response = controller.getVisitDetails(featureToggleProperties, administrationService, visit, uiUtils,
		    sessionContext);
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
		List<SimpleObject> actualProviders = (List<SimpleObject>) actualEncounter.get("encounterProviders");
		assertThat(actualProviders.size(), is(1));
		assertThat((String) actualProviders.get(0).get("provider"), is("Provider"));
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
	
}
