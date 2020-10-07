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
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.openmrs.Concept;
import org.openmrs.ConceptClass;
import org.openmrs.ConceptName;
import org.openmrs.ConceptSearchResult;
import org.openmrs.ConceptSource;
import org.openmrs.Obs;
import org.openmrs.api.ConceptService;
import org.openmrs.api.ObsService;
import org.openmrs.module.appui.UiSessionContext;
import org.openmrs.module.emrapi.EmrApiProperties;
import org.openmrs.module.emrapi.concept.EmrConceptService;
import org.openmrs.module.emrapi.diagnosis.CodedOrFreeTextAnswer;
import org.openmrs.module.emrapi.diagnosis.Diagnosis;
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
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

/**
 *
 */
public class DiagnosesFragmentController {

    private static final String USE_NULL_VALUE = "0";

    /**
     * Searches for diagnoses with names matching query in the specified 1) diagnosisSets, 2) diagnosisConceptSources and/or diagnosisConceptClasses
     * If diagnosisSets or diagnosisConceptSources attributes are missing, default values specided by emrApiProperties are used. 
     * If '0' is specified for any of diagnosisSets or diagnosisConceptSources attributes, null is used for their corresponding values in the search.
     * 
     * @param context
     * @param ui
     * @param emrApiProperties
     * @param emrConceptService
     * @param conceptService
     * @param query
     * @param diagnosisSetUuids
     * @param diagnosisConceptSources
     * @param diagnosisConceptClasses
     * @param start
     * @param size
     * @return diagnoses search results
     * @throws Exception
     */
    public List<SimpleObject> search(UiSessionContext context,
                                     UiUtils ui,
                                     @SpringBean("emrApiProperties") EmrApiProperties emrApiProperties,
                                     @SpringBean("emrConceptService") EmrConceptService emrConceptService,
                                     @SpringBean("conceptService") ConceptService conceptService,
                                     @RequestParam("term") String query,
                                     @RequestParam(value = "diagnosisSets", required = false) String diagnosisSetUuids,
                                     @RequestParam(value = "diagnosisConceptSources", required = false) String diagnosisConceptSources,
                                     @RequestParam(value = "diagnosisConceptClasses", required = false) String diagnosisConceptClasses,
                                     @RequestParam(value = "start", defaultValue = "0") Integer start,
                                     @RequestParam(value = "size", defaultValue = "50") Integer size) throws Exception {

        List<ConceptClass> conceptClasses = new ArrayList<ConceptClass>();
        if (StringUtils.isNotEmpty(diagnosisConceptClasses)) {
            String [] conceptClassNames = diagnosisConceptClasses.split(",");
            for (String className : conceptClassNames) {
                ConceptClass conceptClass = conceptService.getConceptClassByName(className);
                if (conceptClass != null) {
                    conceptClasses.add(conceptClass);
                }                
            }
        }
        else {
            conceptClasses = null;            
        }

        Collection<Concept> diagnosisSets = new ArrayList<Concept>();
        if (StringUtils.isNotEmpty(diagnosisSetUuids)) {
            if (USE_NULL_VALUE.equals(diagnosisSetUuids) ) {
                diagnosisSets = null;
            } else {
                String [] setUuids = diagnosisSetUuids.split(",");
                for (String setUuid : setUuids) {
                    Concept conceptSet = conceptService.getConceptByUuid(setUuid);
                    if (conceptSet != null) {
                        diagnosisSets.add(conceptSet);
                    }                
                }
            }
        }
        else {
            diagnosisSets = emrApiProperties.getDiagnosisSets();
        }
                       
        List<ConceptSource> sources = new ArrayList<ConceptSource>();
        if (StringUtils.isNotEmpty(diagnosisConceptSources)) {
            if (USE_NULL_VALUE.equals(diagnosisConceptSources) ) {
                sources = null;
            } else {
                String [] sourceNames = diagnosisConceptSources.split(",");
                for (String sourceName : sourceNames) {
                    ConceptSource source = conceptService.getConceptSourceByName(sourceName);
                    if (source != null) {
                        sources.add(source);
                    }                
                }
            }
        }
        else {
            sources = emrApiProperties.getConceptSourcesForDiagnosisSearch();
        }

        Locale locale = context.getLocale();

        List<ConceptSearchResult> hits = emrConceptService.conceptSearch(query, locale, conceptClasses, diagnosisSets, sources, null);
        List<SimpleObject> ret = new ArrayList<SimpleObject>();
        for (ConceptSearchResult hit : hits) {
            ret.add(simplify(hit, ui, locale));
        }
        return ret;
    }

    @Deprecated
    public List<SimpleObject> searchNonCoded(UiSessionContext context,
                                     UiUtils ui,
                                     @SpringBean("emrApiProperties") EmrApiProperties emrApiProperties,
                                     @SpringBean("emrConceptService") EmrConceptService emrConceptService,
                                     @SpringBean("conceptService") ConceptService conceptService,
                                     @RequestParam("term") String query,
                                     @RequestParam(value = "start", defaultValue = "0") Integer start,
                                     @RequestParam(value = "size", defaultValue = "50") Integer size) throws Exception {

        return search(context, ui, emrApiProperties, emrConceptService, conceptService, query, null, null, null, start, size);
    }

    public FragmentActionResult codeDiagnosis(UiUtils ui,
                                             @RequestParam("nonCodedObsId") Integer nonCodedObsId,
                                             @RequestParam("diagnosis") List<String> diagnoses, // each string is json, like {"certainty":"PRESUMED","diagnosisOrder":"PRIMARY","diagnosis":"ConceptName:840"}
                                             @SpringBean("obsService") ObsService obsService,
                                             @SpringBean("emrDiagnosisService") DiagnosisService diagnosisService,
                                             @SpringBean("conceptService") ConceptService conceptService
                                             ) throws Exception {


        if(nonCodedObsId != null){
            Obs nonCodedObs = obsService.getObs(nonCodedObsId);
            if (nonCodedObs !=null ){
                List<Diagnosis> diagnosisList = new ArrayList<Diagnosis>();
                for (String diagnosisJson : diagnoses) {
                    ObjectMapper mapper = new ObjectMapper();
                    JsonNode rootNode = mapper.readTree(diagnosisJson);
                    Iterator<JsonNode> iterator = rootNode.iterator();
                    while (iterator.hasNext()){
                        JsonNode next = iterator.next();
                        Diagnosis diagnosis = parseDiagnosisJson(next.toString(), conceptService);
                        diagnosisList.add(diagnosis);
                    }
                }
                List<Obs> newDiagnoses= diagnosisService.codeNonCodedDiagnosis(nonCodedObs, diagnosisList);
                if ((newDiagnoses != null) && (newDiagnoses.size()>0) ){
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
        SimpleObject simple = SimpleObject.fromObject(result, ui, "word", "conceptName.id", "conceptName.uuid", "conceptName.conceptNameType", "conceptName.name", "concept.id", "concept.uuid", "concept.conceptMappings.conceptMapType", "concept.conceptMappings.conceptReferenceTerm.code", "concept.conceptMappings.conceptReferenceTerm.name", "concept.conceptMappings.conceptReferenceTerm.conceptSource.name");

        Concept concept = result.getConcept();
        ConceptName preferredName = getPreferredName(locale, concept);
        PropertyUtils.setProperty(simple, "concept.preferredName", preferredName.getName());

        return simple;
    }

    private ConceptName getPreferredName(Locale locale, Concept concept) {
        ConceptName name = concept.getPreferredName(locale);
        if (name == null && (StringUtils.isNotEmpty(locale.getCountry()) || StringUtils.isNotEmpty(locale.getVariant()))) {
            name = concept.getPreferredName(new Locale(locale.getLanguage()));
        }
        if (name == null) {
            name = concept.getName(locale);
        }
        return name;

    }

    /**
     * @param diagnosisJson  like {"certainty":"PRESUMED","diagnosisOrder":"PRIMARY","diagnosis":"ConceptName:840"}
     * @param conceptService
     * @return
     * @throws Exception
     */
    private Diagnosis parseDiagnosisJson(String diagnosisJson, ConceptService conceptService) throws Exception {
        JsonNode node = new ObjectMapper().readTree(diagnosisJson);

        CodedOrFreeTextAnswer answer = new CodedOrFreeTextAnswer(node.get("diagnosis").getTextValue(), conceptService);
        Diagnosis.Order diagnosisOrder = Diagnosis.Order.valueOf(node.get("diagnosisOrder").getTextValue());
        Diagnosis.Certainty certainty = Diagnosis.Certainty.valueOf(node.get("certainty").getTextValue());

        Diagnosis diagnosis = new Diagnosis(answer, diagnosisOrder);
        diagnosis.setCertainty(certainty);

        return diagnosis;
    }

}
