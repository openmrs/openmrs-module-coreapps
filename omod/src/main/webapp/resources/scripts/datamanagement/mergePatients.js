var jq = jQuery;
jq(function() {
    jq('input[type=text]').first().focus();

    jq('#cancel-button').click(function() {
        window.history.back();
    });
});

function checkConfirmButton() {
    //Check if IDs are known
    var patient1Id = jq("#patient1").val();
    var patient2Id = jq("#patient2").val();

    //Check if input fields are not blank
    var patient1Text = jq("#patient1-text").val().trim();
    var patient2Text = jq("#patient2-text").val().trim();
    
    if (patient1Id > 0 && patient2Id > 0 && (patient1Id != patient2Id) && patient1Text.length > 0 && patient2Text.length > 0) {
        enableButton();
        jq("#confirm-button").focus();
    } else {
        disableButton();
    }
}

function enableButton() {
    jq("#confirm-button").removeAttr("disabled");
    jq("#confirm-button").removeClass("disabled");
}

function disableButton() {
    jq("#confirm-button").attr("disabled","disabled");
    jq("#confirm-button").addClass('disabled');
}

function completePatientIdField(pId) {
    var patient1Id = jq("#patient1-text");
    var patient2Id = jq("#patient2-text");

    if (patient1Id.val() == '') {
       patient1Id.val(pId);
       simulateEnterKey(patient1Id);

    } else if (patient2Id.val() == '') {
        patient2Id.val(pId);
        simulateEnterKey(patient2Id);
    } else if (typeof isUnknownPatient !== 'undefined' && isUnknownPatient) {
        patient2Id.val(pId);
        simulateEnterKey(patient2Id);
        } else {
        patient1Id.val(pId);
        simulateEnterKey(patient1Id);
        patient2Id.val('');
        simulateEnterKey(patient2Id);
    }
}

function handlePatientRowSelection(action, callback) {
    this.action = action;
    this.callback = callback;

    this.handle = function (row) {
        action(row.patientIdentifier.identifier);
    }
}

function simulateEnterKey(elementInput) {
    var press = jQuery.Event("keypress");
    press.keyCode = 13;
    press.which = 13;
    elementInput.trigger(press);
}

