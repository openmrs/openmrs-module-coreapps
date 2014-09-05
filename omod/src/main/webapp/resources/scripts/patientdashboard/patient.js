var editPatientIdentifierDialog = null;
var deleteEncounterDialog= null;
var deleteVisitDialog= null;

function showEditPatientIdentifierDialog () {
    editPatientIdentifierDialog.show();
    return false;
}

function showDeleteEncounterDialog () {
    deleteEncounterDialog.show();
    return false;
}

function showDeleteVisitDialog () {
    deleteVisitDialog.show();
    return false;
}

function showEndVisitDialog () {
    endVisitDialog.show();
    return false;
}

function createEditPatientIdentifierDialog(patientId) {
    editPatientIdentifierDialog = emr.setupConfirmationDialog({
        selector: '#edit-patient-identifier-dialog',
        actions: {
            confirm: function() {
                emr.getFragmentActionWithCallback('coreapps', 'editPatientIdentifier', 'editPatientIdentifier'
                    , { patientId: patientId,
                        identifierTypeId: jq("#hiddenIdentifierTypeId").val(),
                        identifierValue: jq("#patientIdentifierValue").val(),
                        patientIdentifierId: jq("#hiddenPatientIdentifierId").val()
                    }
                    , function(data) {
                        emr.successMessage(data.message);
                        editPatientIdentifierDialog.close();
                        var newValue= jq("#patientIdentifierValue").val();
                        var patientIdentifierId = jq("#hiddenPatientIdentifierId").val();
                        var identifierTypeId = jq("#hiddenIdentifierTypeId").val();
                        var identifierElement = patientIdentifierId ? jq(".editPatientIdentifier[data-patient-identifier-id="+ patientIdentifierId  +"]")
                            : jq(".editPatientIdentifier[data-identifier-type-id="+ identifierTypeId  +"]");  // we identify the element to update by patient identifier id if it exists (for edit) and otherwise identifier type id (for add)
                        if(newValue.length>0){
                            identifierElement.parents("span:first").removeClass('add-id');
                            identifierElement.attr("data-patient-identifier-value", newValue);
                            identifierElement.text(newValue);
                        }else{
                            identifierElement.parents("span:first").addClass('add-id');
                            identifierElement.text(addMessage);
                        }
                    },function(err){
                        emr.handleError(err);
                    });

            },
            cancel: function() {
                editPatientIdentifierDialog.close();
            }
        }
    });
}
function createDeleteEncounterDialog(encounterId, deleteElement) {
    deleteEncounterDialog = emr.setupConfirmationDialog({
        selector: '#delete-encounter-dialog',
        actions: {
            confirm: function() {
                jq('#delete-encounter-dialog' + ' .icon-spin').css('display', 'inline-block').parent().addClass('disabled');
                emr.getFragmentActionWithCallback('coreapps', 'visit/visitDetails', 'deleteEncounter'
                    , { encounterId: encounterId}
                    , function(data) {
                        emr.successMessage(data.message);
                        deleteEncounterDialog.close();
                        jq('#delete-encounter-dialog' + ' .icon-spin').css('display', 'none').parent().removeClass('disabled');
                        var visitId = deleteElement.attr("data-visit-id");
                        var encounterElement = deleteElement.parents("li:first");
                        if(encounterElement!=null && encounterElement!=undefined){
                            encounterElement.remove();
                        }
                        //
                        $(".viewVisitDetails[data-visit-id=" + visitId + "]").click();
                    },function(err){
                        emr.handleError(err);
                        deleteEncounterDialog.close();
                        jq('#delete-encounter-dialog' + ' .icon-spin').css('display', 'none').parent().removeClass('disabled');
                    });
            },
            cancel: function() {
                deleteEncounterDialog.close();
            }
        }
    });
}

function createDeleteVisitDialog(visitId, patientId) {
    deleteVisitDialog = emr.setupConfirmationDialog({
        selector: '#delete-visit-dialog',
        actions: {
            confirm: function() {
                emr.getFragmentActionWithCallback('coreapps', 'visit/visitDetails', 'deleteVisit'
                    , { visitId: visitId}
                    , function(data) {
                        jq('#delete-visit-dialog' + ' .icon-spin').css('display', 'inline-block').parent().addClass('disabled');
                        emr.navigateTo({
                            provider: 'coreapps',
                            page: 'patientdashboard/patientDashboard',
                            query: { patientId: patientId }
                        });
                    },function(err){
                        emr.handleError(err);
                        deleteVisitDialog.close();
                    });
            },
            cancel: function() {
                deleteVisitDialog.close();
            }
        }
    });
}
