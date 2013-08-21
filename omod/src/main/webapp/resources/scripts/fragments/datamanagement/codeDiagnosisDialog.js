var codeDiagnosisDialog = null;

function showCodeDiagnosisDialog (patientId, patientIdentifier, personName, obsId, nonCodedDiagnosis) {
    jq("#hiddenPatientId").val(patientId);
    jq("#hiddenNonCodedObsId").val(obsId);

    patientDashboardLink = patientDashboardLink + "?patientId=" + patientId;
    instructionsTemplate= instructionsTemplate.replace("{1}", "<span><b>" + nonCodedDiagnosis + "</b></span>" );
    instructionsTemplate = instructionsTemplate.replace("{2}",
            "<span><b><a href="
            + patientDashboardLink + ">" + personName  + " (" + patientIdentifier + " )"
            + "</a></b></span>" );
    jq("#instructions").html(instructionsTemplate + ": ");
    codeDiagnosisDialog.show();
    return false;
}

function createCodeDiagnosisDialog() {
    codeDiagnosisDialog = emr.setupConfirmationDialog({
        selector: '#code-diagnosis-dialog',
        actions: {
            confirm: function() {
                emr.getFragmentActionWithCallback('coreapps', 'diagnoses', 'codeDiagnosis'
                    , { patientId: jq("#hiddenPatientId").val(),
                        nonCodedObsId: jq("#hiddenNonCodedObsId").val(),
                        codedConceptId: jq("#hiddenCodedConceptId").val()}
                    , function(data) {
                        emr.successMessage(data.message);
                        codeDiagnosisDialog.close();
                        emr.navigateTo({
                            provider: 'mirebalaisreports',
                            page: 'nonCodedDiagnoses'
                        });
                    });
            },
            cancel: function() {
                codeDiagnosisDialog.close();
            }
        }
    });
}