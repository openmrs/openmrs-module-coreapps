var requestPaperRecordDialog = null;
var editPatientIdentifierDialog = null;
var deleteEncounterDialog= null;
var deleteVisitDialog= null;

// TODO: move the request chart dialog, print ID Card label, and print paper record label dialogs out into paperrecord module

function showRequestChartDialog () {
    requestPaperRecordDialog.show();
    return false;
}
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

function createPaperRecordDialog(patientId) {
    requestPaperRecordDialog = emr.setupConfirmationDialog({
        selector: '#request-paper-record-dialog',
        actions: {
            confirm: function() {
                emr.getFragmentActionWithCallback('paperrecord', 'requestPaperRecord', 'requestPaperRecord'
                    , { patientId: patientId, locationId: sessionLocationModel.id() }
                    , function(data) {
                        emr.successMessage(data.message);
                        requestPaperRecordDialog.close();
                    });
            },
            cancel: function() {
                requestPaperRecordDialog.close();
            }
        }
    });
}

function printIdCardLabel() {
    emr.getFragmentActionWithCallback('paperrecord', 'requestPaperRecord', 'printIdCardLabel'
        , { patientId: patient.id, locationId: sessionLocationModel.id() }
        , function(data) {
            if(data.success) {
                emr.successMessage(data.message);
            } else {
                emr.errorMessage(data.message);
            }
        }
    );
}

function printPaperRecordLabel() {
    emr.getFragmentActionWithCallback('paperecord', 'requestPaperRecord', 'printPaperRecordLabel'
        , { patientId: patient.id, locationId: sessionLocationModel.id() }
        , function(data) {
            if(data.success) {
                emr.successMessage(data.message);
            } else {
                emr.errorMessage(data.message);
            }
        }
    );   
}

function createEditPatientIdentifierDialog(patientId) {
    editPatientIdentifierDialog = emr.setupConfirmationDialog({
        selector: '#edit-patient-identifier-dialog',
        actions: {
            confirm: function() {
                emr.getFragmentActionWithCallback('coreapps', 'editPatientIdentifier', 'editPatientIdentifier'
                    , { patientId: patientId,
                        identifierTypeId: jq("#hiddenIdentifierTypeId").val(),
                        identifierValue: jq("#patientIdentifierValue").val()
                    }
                    , function(data) {
                        emr.successMessage(data.message);
                        editPatientIdentifierDialog.close();
                        var newValue= jq("#patientIdentifierValue").val();
                        var identifierTypeId = jq("#hiddenIdentifierTypeId").val();
                        var identifierElement =  jq(".editPatientIdentifier[data-identifier-type-id="+ identifierTypeId  +"]");
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
                emr.getFragmentActionWithCallback('coreapps', 'visit/visitDetails', 'deleteEncounter'
                    , { encounterId: encounterId}
                    , function(data) {
                        emr.successMessage(data.message);
                        deleteEncounterDialog.close();
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
                        emr.successMessage(data.message);
                        deleteVisitDialog.close();
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
