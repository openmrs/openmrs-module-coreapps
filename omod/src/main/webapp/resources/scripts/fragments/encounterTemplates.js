//Uses the namespace pattern from http://stackoverflow.com/a/5947280
(function( encounterTemplates, $, undefined) {

    var templates = {};
    var defaultTemplate;
    var parameters = {};

    encounterTemplates.setTemplate = function(encounterTypeUuid, templateId) {
        templates[encounterTypeUuid] = _.template(jq('#' + templateId).html());
    };

    encounterTemplates.setDefaultTemplate = function(templateId) {
        defaultTemplate = _.template(jq('#' + templateId).html());
    };

    encounterTemplates.setParameter = function(encounterTypeUuid, name, value) {
        if (!(encounterTypeUuid in parameters)) {
            parameters[encounterTypeUuid] = {};
        }
        parameters[encounterTypeUuid][name] = value;
    };

    encounterTemplates.displayEncounter = function(encounter, patient) {
        var template;
        if (templates[encounter.encounterType.uuid]) {
            template = templates[encounter.encounterType.uuid];
        } else {
            template = defaultTemplate;
        }

        var data = {
            encounter: encounter,
            patient: patient
        };
        data.config = _.extend({
            icon: "icon-time"
        }, parameters[encounter.encounterType.uuid]);

        return template(data);
    };

}( window.encounterTemplates = window.encounterTemplates || {}, jQuery ));