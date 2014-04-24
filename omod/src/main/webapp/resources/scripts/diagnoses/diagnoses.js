(function(diagnoses, $, _, undefined) {

    var codingSystemToUse = 'ICD-10-WHO';

    var mapTypeOrder = [ "SAME-AS", "NARROWER-THAN" ]

    findConceptMapping = function(concept, sourceName) {
        var matches = _.filter(concept.conceptMappings, function(item) {
            return item.conceptReferenceTerm.conceptSource.name == sourceName
        });
        if (!matches || matches.length == 0) {
            return "";
        }
        return _.sortBy(matches, function(item) {
            var temp = _.indexOf(mapTypeOrder, item.conceptMapType);
            return temp < 0 ? 9999 : temp;
        })[0].conceptReferenceTerm.code;
    };

    diagnoses.sameDiagnosis = function(d1, d2) {
        if (d1.diagnosis.conceptId && d2.diagnosis.conceptId) {
            return d1.diagnosis.conceptId === d2.diagnosis.conceptId;
        } else {
            return d1.diagnosis.matchedName === d2.diagnosis.matchedName;
        }
    };

    diagnoses.validateDiagnosis = function(d) {
        if (!d.hasOwnProperty("diagnosis") || !d.hasOwnProperty("confirmed") || !d.hasOwnProperty("primary")) {
            throw "required properties: diagnosis, confirmed, primary";
        }
    };

    diagnoses.CodedOrFreeTextConceptAnswer = function(item) {
        if (typeof item === "string") {
            // free-text case

            var api = {
                type: "free-text",
                matchedName: item,
                preferredName: null,
                code: null,
                conceptId: null,
                valueToSubmit: function() {
                    return "Non-Coded:" + item;
                }
            };
            return api;

        }
        else {
            // expect object like
            // {
            //     concept: { id, conceptMappings, preferredName },
            //     conceptName: { id, conceptNameType, name }
            // }

            var api = _.extend(item, {
                type: "concept",
                matchedName: item.conceptName ? item.conceptName.name : item.concept.preferredName,
                preferredName: item.conceptName && item.conceptName.name != item.concept.preferredName ? item.concept.preferredName : null,
                nameIsPreferred: item.conceptName ? (item.conceptName === item.concept.preferredName) : true,
                code: findConceptMapping(item.concept, codingSystemToUse),
                conceptId: item.concept.id,
                exactlyMatchesQuery: function(query) {
                    query = emr.stripAccents(query.toLowerCase());
                    return (api.code && api.code.toLowerCase() == query) ||
                        (api.preferredName && emr.stripAccents(api.preferredName.toLowerCase()) == query) ||
                        (api.matchedName && emr.stripAccents(api.matchedName.toLowerCase()) == query);
                },
                valueToSubmit: function() {
                    return item.conceptName ? "ConceptName:" + item.conceptName.id : "Concept:" + item.concept.id;
                }
            });
            return api;
        }
    };

    // takes either a CodedOrFreeTextConceptAnswer or else { diagnosis: CodedOrFreeTextConceptAnswer, confirmed: boolean, primary: boolean }
    diagnoses.Diagnosis = function(item) {
        var api = { };

        if (item.diagnosis) {
            api.diagnosis = item.diagnosis;
            api.confirmed = !! item.confirmed;
            api.primary = !! item.primary;
            api.existingObs = item.existingObs;
        } else {
            api.diagnosis = item;
            api.confirmed = false;
            api.primary = false;
            api.existingObs = null;
        }

        api.certainty = function() {
            return api.confirmed ? "CONFIRMED" : "PRESUMED";
        };

        api.order = function() {
            return api.primary ? "PRIMARY" : "SECONDARY";
        };

        api.valueToSubmit = function() {
            return JSON.stringify({
                certainty: api.certainty(),
                order: api.order(),
                diagnosis: api.diagnosis.valueToSubmit(),
                existingObs: api.existingObs
            });
        };

        return api;
    };

    diagnoses.EncounterDiagnoses = function() {

        var api = {
            diagnoses: []
        };

        api.getDiagnoses = function() {
            return api.diagnoses;
        }

        api.findSelectedSimilarDiagnosis = function(diagnosis) {
            return _.find(api.diagnoses, function(candidate) {
                return diagnoses.sameDiagnosis(diagnosis, candidate);
            });
        };

        api.diagnosisWithConceptId = function(conceptId) {
            return _.find(api.diagnoses, function(candidate) {
                return candidate.diagnosis.type === 'concept' && candidate.diagnosis.conceptId === conceptId;
            });
        };

        api.hasPrimaryDiagnosis = function() {
            return _.some(api.diagnoses, function(item) {
                return item.primary;
            });
        };

        api.addDiagnosis = function(diagnosis) {
            diagnoses.validateDiagnosis(diagnosis);
            if (api.findSelectedSimilarDiagnosis(diagnosis)) {
                return;
            }
            if (!api.hasPrimaryDiagnosis()) {
                diagnosis.primary = true;
            }
            api.diagnoses.push(diagnosis);
        };

        api.addDiagnoses = function(diagnoses) {
            _.each(diagnoses, function(diagnosis) {
                api.addDiagnosis(diagnosis);
            });
        };

        api.removeDiagnosis = function(diagnosis) {
            api.diagnoses = _.reject(api.diagnoses, function(candidate) {
                return diagnoses.sameDiagnosis(diagnosis, candidate);
            });
            if (!api.hasPrimaryDiagnosis() && api.diagnoses.length > 0) {
                api.diagnoses[0].primary = true;
            }
        }

        api.primaryDiagnoses = function() {
            return _.filter(api.diagnoses, function(candidate) {
                return candidate.primary;
            });
        };

        api.secondaryDiagnoses = function() {
            return _.filter(api.diagnoses, function(candidate) {
                return !candidate.primary;
            });
        };

        api.render = function(template, target) {
            $(target).html(template({ diagnoses: api }));
        };

        return api;
    };


} ( window.diagnoses = window.diagnoses || {}, jQuery, _));