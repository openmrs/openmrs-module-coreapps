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
package org.openmrs.module.coreapps.contextmodel;

import org.openmrs.Encounter;
import org.openmrs.EncounterType;
import org.openmrs.Location;
import org.openmrs.Patient;
import org.openmrs.PatientProgram;
import org.openmrs.PatientState;
import org.openmrs.Program;
import org.openmrs.api.EncounterService;
import org.openmrs.api.ProgramWorkflowService;
import org.openmrs.module.appframework.context.AppContextModel;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.adt.AdtService;
import org.openmrs.module.emrapi.visit.VisitDomainWrapper;
import org.openmrs.module.webservices.rest.web.ConversionUtil;
import org.openmrs.module.webservices.rest.web.representation.Representation;
import org.openmrs.ui.framework.SimpleObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class AppContextModelGenerator {
    
    @Autowired
    AdtService adtService;
    
    @Autowired
    EncounterService encounterService;

    @Autowired
    ProgramWorkflowService programWorkflowService;

    /**
     * Generates an AppContextModel for the given patient
     */
    public AppContextModel generateAppContextModel(UiSessionContext sessionContext, Patient patient) {
        AppContextModel contextModel = sessionContext.generateAppContextModel();

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

        contextModel.put("patient", new PatientContextModel(patient));
		contextModel.put("patientId", patient != null ? patient.getUuid() : null);  // support legacy substitution methods that use "{{patientId}}" as a template and expect a uuid substitution
        contextModel.put("visit", activeVisit == null ? null : new VisitContextModel(activeVisit));

        List<EncounterType> encounterTypes = new ArrayList<>();
        ArrayList<PatientEncounterContextModel> encountersModel = new ArrayList<>();
        List<Encounter> encounters = encounterService.getEncountersByPatient(patient);
        for (Encounter encounter : encounters) {
            encounterTypes.add(encounter.getEncounterType());
            encountersModel.add(new PatientEncounterContextModel(encounter));
        }
        contextModel.put("encounterTypes", ConversionUtil.convertToRepresentation(encounterTypes, Representation.DEFAULT));
        contextModel.put("encounters", encountersModel);

        List<Program> programs = new ArrayList<>();
        List<PatientProgram> patientPrograms = programWorkflowService.getPatientPrograms(patient, null, null, null, null, null, false);
        List<SimpleObject> activePrograms = new ArrayList<>();
        List<SimpleObject> activeProgramStates = new ArrayList<>();
        for (PatientProgram patientProgram : patientPrograms) {
            programs.add(patientProgram.getProgram());
            if (patientProgram.getActive()) {
                SimpleObject activeProgram = new SimpleObject();
                activeProgram.put("programUuid", patientProgram.getProgram().getUuid());
                activeProgram.put("dateEnrolled", patientProgram.getDateEnrolled());
                activePrograms.add(activeProgram);
                for (PatientState ps : patientProgram.getCurrentStates()) {
                    SimpleObject activeState = new SimpleObject();
                    activeState.put("stateUuid", ps.getState().getUuid());
                    activeState.put("workflowUuid", ps.getState().getProgramWorkflow().getUuid());
                    activeState.put("programUuid", ps.getState().getProgramWorkflow().getProgram().getUuid());
                    activeState.put("startDate", ps.getStartDate());
                    activeProgramStates.add(activeState);
                }
            }
        }
        contextModel.put("patientPrograms", ConversionUtil.convertToRepresentation(programs, Representation.DEFAULT));
        contextModel.put("activePrograms", ConversionUtil.convertToRepresentation(activePrograms, Representation.DEFAULT));
        contextModel.put("activeProgramStates", ConversionUtil.convertToRepresentation(activeProgramStates, Representation.DEFAULT));
        return contextModel;
    }
}
