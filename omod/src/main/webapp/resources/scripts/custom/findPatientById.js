
var jq = jQuery;
function getPatientById(id, hiddenId, fullNameField, callbackFunction, patientNotFoundMsg) {
    jQuery.ajax({
        url: emr.fragmentActionLink("coreapps", "findPatient", "searchById",  { primaryId: id }),
        dataType: 'json',
        type: 'POST'
    })
        .success(function(data) {
            var id = data.patientId;
            jq("#" + hiddenId).val(id);

            if (id != 0 && data.primaryIdentifiers && data.primaryIdentifiers[0]) {
                var identifierId = data.primaryIdentifiers[0].identifier;
                jq("#" + fullNameField).text(data.preferredName.fullName);

            }
        })
        .complete(function(data) {
            callbackFunction();
        })
        .error(function(data) {
            jq("#" + hiddenId).val(0);
            jq("#" + fullNameField).text(patientNotFoundMsg);
        });
}


