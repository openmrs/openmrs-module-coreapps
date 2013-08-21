/*
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

package org.openmrs.module.coreapps.fragment.controller;

import org.apache.commons.beanutils.PropertyUtils;
import org.openmrs.Concept;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSource;
import org.openmrs.Obs;
import org.openmrs.api.ObsService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.emrapi.diagnosis.DiagnosisService;
import org.openmrs.ui.framework.SimpleObject;
import org.openmrs.ui.framework.UiUtils;
import org.openmrs.ui.framework.annotation.SpringBean;
import org.openmrs.ui.framework.fragment.action.FailureResult;
import org.openmrs.ui.framework.fragment.action.FragmentActionResult;
import org.openmrs.ui.framework.fragment.action.SuccessResult;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

/**
 *
 */
public class DiagnosesFragmentController {

    public List<SimpleObject> search(UiSessionContext context,
                                     UiUtils ui,
                                     @SpringBean("emrApiProperties") EmrApiProperties emrApiProperties,
                                     @SpringBean("emrConceptService") EmrConceptService emrConceptService,
                                     @RequestParam("term") String query,
                                     @RequestParam(value = "start", defaultValue = "0") Integer start,
                                     @RequestParam(value = "size", defaultValue = "50") Integer size) throws Exception {

        Collection<Concept> diagnosisSets = emrApiProperties.getDiagnosisSets();
        Locale locale = context.getLocale();

        List<ConceptSource> sources = emrApiProperties.getConceptSourcesForDiagnosisSearch();

        List<ConceptSearchResult> hits = emrConceptService.conceptSearch(query, locale, null, diagnosisSets, sources, null);
        List<SimpleObject> ret = new ArrayList<SimpleObject>();
        for (ConceptSearchResult hit : hits) {
            ret.add(simplify(hit, ui, locale));
        }
        return ret;
    }

    public List<SimpleObject> searchNonCoded(UiSessionContext context,
                                     UiUtils ui,
                                     @SpringBean("emrApiProperties") EmrApiProperties emrApiProperties,
                                     @SpringBean("emrConceptService") EmrConceptService emrConceptService,
                                     @RequestParam("term") String query,
                                     @RequestParam(value = "start", defaultValue = "0") Integer start,
                                     @RequestParam(value = "size", defaultValue = "50") Integer size) throws Exception {

        Collection<Concept> diagnosisSets = emrApiProperties.getDiagnosisSets();
        Locale locale = context.getLocale();

        List<ConceptSource> sources = emrApiProperties.getConceptSourcesForDiagnosisSearch();

        List<ConceptSearchResult> hits = emrConceptService.conceptSearch(query, locale, null, diagnosisSets, sources, null);
        List<SimpleObject> ret = new ArrayList<SimpleObject>();
        for (ConceptSearchResult hit : hits) {
            ret.add(simplify(hit, ui, locale));
        }
        return ret;
    }

    public FragmentActionResult codeDiagnosis(UiUtils ui,
                                             @RequestParam("nonCodedObsId") Integer nonCodedObsId,
                                             @RequestParam("codedConceptId") Concept codedConcept,
                                             @SpringBean("obsService") ObsService obsService,
                                             @SpringBean("diagnosisService") DiagnosisService diagnosisService
                                             ) throws Exception {


        if(nonCodedObsId != null){
            Obs nonCodedObs = obsService.getObs(nonCodedObsId);
            if (nonCodedObs !=null ){
                nonCodedObs = diagnosisService.codeNonCodedDiagnosis(nonCodedObs, codedConcept);
                if (nonCodedObs != null){
                    return new SuccessResult(ui.message("coreapps.dataManagement.codeDiagnosis.success"));
                }
            }
        }
        return new FailureResult(ui.message("coreapps.dataManagement.codeDiagnosis.failure"));
    }

    /**
     * This is public so that it can be used by a fragment that needs to prepopulate a diagnoses widget that is normally
     * populated with AJAX results from the #search method.
     * @param result
     * @param ui
     * @param locale
     * @return
     * @throws Exception
     */
    public SimpleObject simplify(ConceptSearchResult result, UiUtils ui, Locale locale) throws Exception {
        SimpleObject simple = SimpleObject.fromObject(result, ui, "word", "conceptName.id", "conceptName.conceptNameType", "conceptName.name", "concept.id", "concept.conceptMappings.conceptMapType", "concept.conceptMappings.conceptReferenceTerm.code", "concept.conceptMappings.conceptReferenceTerm.name", "concept.conceptMappings.conceptReferenceTerm.conceptSource.name");

        Concept concept = result.getConcept();
        ConceptName conceptName = result.getConceptName();
        ConceptName preferredName = concept.getPreferredName(locale);
        PropertyUtils.setProperty(simple, "concept.preferredName", preferredName.getName());

        return simple;
    }

}
