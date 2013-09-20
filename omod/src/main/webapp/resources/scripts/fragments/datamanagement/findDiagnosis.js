var mapTypeOrder = [ "SAME-AS", "NARROWER-THAN" ]

function findConceptMapping(concept, sourceName) {
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


function ConceptSearchResult(item) {
    var api = _.extend(item, {
        matchedName: item.conceptName ? item.conceptName.name : item.concept.preferredName,
        preferredName: item.conceptName && item.conceptName.name != item.concept.preferredName ? item.concept.preferredName : null,
        code: findConceptMapping(item.concept, "ICD-10-WHO"),
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

function FreeTextListItem(text) {
    var api = {
        matchedName: text,
        preferredName: null,
        code: null,
        conceptId: null,
        valueToSubmit: function() {
            return "Non-Coded:" + text;
        }
    };
    return api;
}

function Diagnosis(item) {
    var api = {
        diagnosis: ko.observable(item),
        confirmed: ko.observable(false),
        primary: ko.observable(false)
    }
    api.certainty = ko.computed(function() {
        return api.confirmed() ? "CONFIRMED" : "PRESUMED";
    });
    api.diagnosisOrder = ko.computed(function() {
        return api.primary() ? "PRIMARY" : "SECONDARY";
    });
    api.valueToSubmit = ko.computed(function() {
        return JSON.stringify({
            certainty: api.certainty(),
            diagnosisOrder: api.diagnosisOrder(),
            diagnosis: api.diagnosis().valueToSubmit()
        });
    });
    return api;
}

function ConsultFormViewModel() {
    var sameDiagnosis = function(d1, d2) {
        if (d1.diagnosis().conceptId && d2.diagnosis().conceptId) {
            return d1.diagnosis().conceptId === d2.diagnosis().conceptId;
        } else {
            return d1.diagnosis().matchedName === d2.diagnosis().matchedName;
        }
    }

    var api = {};

    api.submitting = ko.observable(false);

    api.searchTerm = ko.observable();
    api.diagnoses = ko.observableArray();

    api.validations = [];

    api.primaryDiagnoses = ko.computed(function() {
        return _.filter(api.diagnoses(), function(candidate) {
            return candidate.primary();
        });
    });

    api.secondaryDiagnoses = ko.computed(function() {
        return _.filter(api.diagnoses(), function(candidate) {
            return !candidate.primary();
        });
    });

    api.startSubmitting = function() {
        api.submitting(true);
    }

    api.hasPrimaryDiagnosis = function() {
        return _.some(api.diagnoses(), function(item) {
            return item.primary();
        });
    }

    api.validations.push(api.hasPrimaryDiagnosis);

    api.isValid = function() {
        var validated = true;
        _.each(api.validations, function(validation) {
            validated = validated && validation();
        })
        return validated;
    }

    api.canSubmit = function() {
        return api.isValid() && !api.submitting();
    }

    api.findSelectedSimilarDiagnosis = function(diagnosis) {
        return _.find(api.diagnoses(), function(candidate) {
            return sameDiagnosis(diagnosis, candidate);
        });
    }

    api.addDiagnosis = function(diagnosis) {
        if (api.findSelectedSimilarDiagnosis(diagnosis)) {
            return;
        }
        if (!api.hasPrimaryDiagnosis()) {
            diagnosis.primary(true);
        }
        api.diagnoses.push(diagnosis);
    }

    api.removeDiagnosis = function(diagnosis) {
        api.diagnoses.remove(function(candidate) {
            return sameDiagnosis(diagnosis, candidate);
        });
        if (!api.hasPrimaryDiagnosis() && api.diagnoses().length > 0) {
            api.diagnoses()[0].primary(true);
        }
    }

    api.selectedConceptIds = function() {
        return _.map(api.diagnoses(), function(item) {
            return item.diagnosis().conceptId;
        });
    }

    return api;
}

ko.bindingHandlers.autocomplete = {
    init: function(element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) {

        $(element).keypress(function(e) {
            return e.which != 13;
        });
        $(element).autocomplete({
            source: emr.fragmentActionLink("coreapps", "diagnoses", "search"),
            response: function(event, ui) {
                var query = event.target.value.toLowerCase();
                var items = ui.content;
                var selected = viewModel.selectedConceptIds();
                // remove any already-selected concepts, and look for exact matches by name/code
                var exactMatch = false;
                for (var i = items.length - 1; i >= 0; --i) {
                    items[i] = ConceptSearchResult(items[i]);
                    if (!exactMatch && items[i].exactlyMatchesQuery(query)) {
                        exactMatch = true;
                    }
                    if (_.contains(selected, items[i].conceptId)) {
                        items.splice(i, 1);
                    }
                }
                if (!exactMatch) {
                    items.push(FreeTextListItem($(element).val()))
                }
            },
            select: function( event, ui ) {
                viewModel.addDiagnosis(Diagnosis(ui.item));
                $(this).val("");
                return false;
            }
        })
            .data( "autocomplete" )._renderItem = function( ul, item ) {
            var formatted = allBindingsAccessor().itemFormatter(item);
            return jq('<li>').append('<a>' + formatted + '</a>').appendTo(ul);
        };
    },
    update: function(element, valueAccessor, allBindingsAccessor, viewModel, bindingContext) {
        // It's invoked everytime we update the observable associated to the element
        // In this cases, we assume we'll always want to reset it
        $(element).val("");
    }
}
