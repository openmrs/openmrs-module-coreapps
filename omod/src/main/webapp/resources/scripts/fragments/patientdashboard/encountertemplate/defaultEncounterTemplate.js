jq(function() {
	jq(document).on('click','.view-details.collapsed', function(event){
        var jqTarget = jq(event.currentTarget);
        var encounterId = jqTarget.data("encounter-id");
        var isHtmlForm = jqTarget.data("encounter-form");
        var dataTarget = jqTarget.data("target");
        var customTemplateId = jqTarget.data("display-template");
        getEncounterDetails(encounterId, isHtmlForm, dataTarget, customTemplateId ? customTemplateId : "defaultEncounterDetailsTemplate");
    });
	    
	jq(document).on('click', '.deleteEncounterId', function(event) {
		var encounterId = jq(event.target).attr("data-encounter-id");
		createDeleteEncounterDialog(encounterId, jq(this));
		showDeleteEncounterDialog();
	});

    jq(document).on('click', '.editEncounter', function(event) {
        var encounterId = jq(event.target).attr("data-encounter-id");
        var patientId = jq(event.target).attr("data-patient-id");
        emr.navigateTo({
            provider: "emr",
            page: "htmlform/editHtmlFormWithStandardUi",
            query: { patientId: patientId, encounterId: encounterId }
        });
    });
	
	//We cannot assign it here due to Jasmine failure: 
	//net.sourceforge.htmlunit.corejs.javascript.EcmaError: TypeError: Cannot call method "replace" of undefined
    var detailsTemplates = {};

	function getEncounterDetails(id, isHtmlForm, dataTarget, displayTemplateId) {
        if (!detailsTemplates[displayTemplateId]) {
            detailsTemplates[displayTemplateId] = _.template(jq('#' + displayTemplateId).html());
        }
        var displayTemplate = detailsTemplates[displayTemplateId];

	    var encounterDetailsSection = jq(dataTarget + ' .encounter-summary-container');
	    if (isHtmlForm) {
	    		if(encounterDetailsSection.html() == "") { encounterDetailsSection.html("<i class=\"icon-spinner icon-spin icon-2x pull-left\"></i>");}
	        jq.getJSON(
	        		emr.fragmentActionLink("emr", "htmlform/viewEncounterWithHtmlForm", "getAsHtml", { encounterId: id })
	        ).success(function(data){
	            encounterDetailsSection.html(data.html);
	        }).error(function(err){
	            emr.errorAlert(err);
	        });
	    } else {
	    		if(encounterDetailsSection.html() == "") { encounterDetailsSection.html("<i class=\"icon-spinner icon-spin icon-2x pull-left\"></i>");}
	        jq.getJSON(
	            emr.fragmentActionLink("coreapps", "visit/visitDetails", "getEncounterDetails", { encounterId: id })
	        ).success(function(data){
	            encounterDetailsSection.html(displayTemplate(data));
	        }).error(function(err){
	            emr.errorAlert(err);
	        });
	    }
	}
});