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
package org.openmrs.module.coreapps;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openmrs.api.ConceptService;
import org.openmrs.api.context.Context;
import org.openmrs.module.BaseModuleActivator;
import org.openmrs.module.ModuleActivator;
import org.openmrs.module.ModuleFactory;
import org.openmrs.module.coreapps.htmlformentry.EncounterDiagnosesTagHandler;
import org.openmrs.module.coreapps.htmlformentry.EncounterDispositionTagHandler;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.disposition.DispositionService;
import org.openmrs.module.htmlformentry.HtmlFormEntryService;

/**
 * This class contains the logic that is run every time this module is either started or stopped.
 */
public class CoreAppsActivator extends BaseModuleActivator {
	
	protected Log log = LogFactory.getLog(getClass());

    /**
     * Public static so it can be used in tests
     * @param emrApiProperties
     * @return
     */
    public static EncounterDispositionTagHandler setupEncounterDispositionTagHandler(EmrApiProperties emrApiProperties, DispositionService dispositionService) {
        EncounterDispositionTagHandler encounterDispositionTagHandler = new EncounterDispositionTagHandler();
        encounterDispositionTagHandler.setEmrApiProperties(emrApiProperties);
        encounterDispositionTagHandler.setDispositionService(dispositionService);
        return encounterDispositionTagHandler;
    }

    /**
     * Public static so it can be used in tests
     * @param conceptService
     * @param emrApiProperties
     * @return
     */
    public static EncounterDiagnosesTagHandler setupEncounterDiagnosesTagHandler(ConceptService conceptService, EmrApiProperties emrApiProperties) {
        EncounterDiagnosesTagHandler encounterDiagnosesTagHandler = new EncounterDiagnosesTagHandler();
        encounterDiagnosesTagHandler.setEmrApiProperties(emrApiProperties);
        encounterDiagnosesTagHandler.setConceptService(conceptService);
        return encounterDiagnosesTagHandler;
    }

    /**
	 * @see ModuleActivator#willRefreshContext()
	 */
	public void willRefreshContext() {
		log.info("Refreshing Core Apps Module");
	}
	
	/**
	 * @see ModuleActivator#contextRefreshed()
	 */
	public void contextRefreshed() {
		log.info("Core Apps Module refreshed");
	}
	
	/**
	 * @see ModuleActivator#willStart()
	 */
	public void willStart() {
		log.info("Starting Core Apps Module");
	}
	
	/**
	 * @see ModuleActivator#started()
	 */
	public void started() {
        ConceptService conceptService = Context.getConceptService();
        EmrApiProperties emrApiProperties = Context.getRegisteredComponent("emrApiProperties", EmrApiProperties.class);
        DispositionService dispositionService = Context.getRegisteredComponent("dispositionService", DispositionService.class);

        if (ModuleFactory.isModuleStarted("htmlformentry")) {
            HtmlFormEntryService htmlFormEntryService = Context.getService(HtmlFormEntryService.class);

            EncounterDiagnosesTagHandler encounterDiagnosesTagHandler = CoreAppsActivator.setupEncounterDiagnosesTagHandler(conceptService, emrApiProperties);
            htmlFormEntryService.addHandler(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_NAME, encounterDiagnosesTagHandler);

            EncounterDispositionTagHandler encounterDispositionTagHandler = CoreAppsActivator.setupEncounterDispositionTagHandler(emrApiProperties, dispositionService);
            htmlFormEntryService.addHandler(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DISPOSITION_TAG_NAME, encounterDispositionTagHandler);
        }

        log.info("Core Apps Module started");
    }
	
	/**
	 * @see ModuleActivator#willStop()
	 */
	public void willStop() {
		log.info("Stopping Core Apps Module");
	}
	
	/**
	 * @see ModuleActivator#stopped()
	 */
	public void stopped() {
        try {
            HtmlFormEntryService htmlFormEntryService = Context.getService(HtmlFormEntryService.class);
            htmlFormEntryService.getHandlers().remove(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DIAGNOSES_TAG_NAME);
            htmlFormEntryService.getHandlers().remove(CoreAppsConstants.HTMLFORMENTRY_ENCOUNTER_DISPOSITION_TAG_NAME);
        } catch (Exception ex) {
            // pass
        }

		log.info("Core Apps Module stopped");
	}
	
}
