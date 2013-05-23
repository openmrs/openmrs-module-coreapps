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

	encounterTemplates.displayEncounter = function(encounter) {
		var template;
		if (templates[encounter.encounterType.uuid]) {
			template = templates[encounter.encounterType.uuid];
		} else {
			template = defaultTemplate;
		}
		
		var data = {};
		if (parameters[encounter.encounterType.uuid]) {
			data = parameters[encounter.encounterType.uuid];
		} else {
			data.icon = "icon-time";
		}
		data.encounter = encounter;
		
		return template(data);
	};
	
}( window.encounterTemplates = window.encounterTemplates || {}, jQuery ));