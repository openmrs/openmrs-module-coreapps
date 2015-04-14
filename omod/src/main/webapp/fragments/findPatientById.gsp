<%
    ui.includeJavascript("coreapps", "custom/findPatientById.js")

%>

<script type="text/javascript">
jq(function() {
    var KEYCODE_ENTER = 13;

    function evaluatePrimaryId() {
        var primaryId = jq("#${config.textFieldName}").val().trim();
        if (primaryId.length > 0) {
            getPatientById(primaryId,"${ config.hiddenFieldName}", "${config.fullNameField}", ${config.callBack},
                "${ ui.message("coreapps.mergePatients.patientNotFound") }");
        } else {
        	//If the field is blank simply trigger callback.
        	${config.callBack}();
        }
    };

    jq("#${config.textFieldName}").blur(function() {
        evaluatePrimaryId();
    });

    jq("#${config.textFieldName}").keypress(function(event) {
        if (event.keyCode == KEYCODE_ENTER) {
          evaluatePrimaryId();
        }
    });
});
</script>

<label>${config.label}</label>
<input type="hidden" id="${config.hiddenFieldName}"  name="${config.hiddenFieldName}" />
<div class="scan-input">
    <input type="text" id="${config.textFieldName}" AUTOCOMPLETE="OFF" size="40" placeholder=""/>
</div>
<br/>
<span id="${config.fullNameField}"></span>