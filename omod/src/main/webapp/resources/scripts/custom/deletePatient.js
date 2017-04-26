var delPatient = delPatient || {};

delPatient.deletePatientCreationDialog = null;

/**
 * Functions used to Delete a Patient
 */

delPatient.returnUrl = "/coreapps/findpatient/findPatient.page?app=coreapps.findPatient";

delPatient.createDeletePatientCreationDialog = function() {
    emr.loadMessages([
        "coreapps.task.deletePatient.deletePatientSuccessful",
        "coreapps.task.deletePatient.deletePatientUnsuccessful"
    ]);
    delPatient.deletePatientCreationDialog = emr.setupConfirmationDialog({
        selector: '#delete-patient-creation-dialog',
        actions: {
            confirm: function() {
                var deleteReason = jq('#delete-reason').val().trim(); //Retrieve text from text box
                if(deleteReason && deleteReason.length > 0) { //Should not be invalid or empty
                    jq.ajax({
                        url: '/' + OPENMRS_CONTEXT_PATH + '/ws/rest/v1/patient/'+delPatient.patientUUID+'?reason='+deleteReason,
                        type: 'DELETE',
                        success: function() {
                            emr.successMessage('coreapps.task.deletePatient.deletePatientSuccessful');
                            jq('#delete-patient-creation-dialog' + ' .icon-spin').css('display', 'inline-block').parent().addClass('disabled');
                            delPatient.deletePatientCreationDialog.close();
                            jq(setTimeout(delPatient.goToReturnUrl, 1275)); //Allow the success message to display before redirecting to another page
                        },
                        error: function() {
                            emr.errorMessage("coreapps.task.deletePatient.deletePatientUnsuccessful");
                            delPatient.deletePatientCreationDialog.close();
                        }
                    });
                } else {
                    jq('#delete-reason-empty').css({'color' : 'red', display : 'inline'}).show(); //Show warning message if empty
                    jq('#delete-reason').val(""); //Clear the text box
                }
            },
            cancel: function() {
                //Clear fields, close dialog box
                jq('#delete-reason').val("");
                jq('#delete-reason-empty').hide();
                delPatient.deletePatientCreationDialog.close();
            }
        }
    });
};

delPatient.showDeletePatientCreationDialog = function(patientUUID) {
    delPatient.patientUUID = patientUUID;
    if (delPatient.deletePatientCreationDialog == null) {
        delPatient.createDeletePatientCreationDialog();
    }
    jq('#delete-reason-empty').hide();
    delPatient.deletePatientCreationDialog.show();
};

delPatient.goToReturnUrl = function() {
    emr.navigateTo({ applicationUrl: emr.applyContextModel(delPatient.returnUrl)});
};