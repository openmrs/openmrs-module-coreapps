
var jq = jQuery;
function getPatientById(id, hiddenId, fullNameField, callbackFunction, patientNotFoundMsg) {
    const clearInput = function(msg) {
        jq("#" + hiddenId).val(0);
        jq("#" + fullNameField).html(msg);
    }

    if (!id) {
        clearInput('');
        callbackFunction();
    }
    else {
        jQuery.ajax({
            url: emr.fragmentActionLink("coreapps", "findPatient", "searchById", {primaryId: id}),
            dataType: 'json',
            type: 'POST'
        })
            .success(function (data) {
                const patientId = data.patientId;
                if (patientId && patientId !== 0 && data.preferredName) {
                    jq("#" + hiddenId).val(patientId);
                    jq("#" + fullNameField).html(data.preferredName.name);
                } else {
                    clearInput(patientNotFoundMsg);
                }
            })
            .error(function () {
                clearInput(patientNotFoundMsg);
            })
            .complete(function () {
                callbackFunction();
            });
    }
}


