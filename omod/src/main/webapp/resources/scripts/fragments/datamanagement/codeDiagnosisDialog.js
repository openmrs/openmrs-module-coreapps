var codeDiagnosisDialog = null;

function showCodeDiagnosisDialog (patientId, visitId, patientIdentifier, personName, obsId, nonCodedDiagnosis) {
    jq("#hiddenPatientId").val(patientId);
    jq("#hiddenNonCodedObsId").val(obsId);

    patientDashboardLink = patientDashboardLink + "?patientId=" + patientId + "&visitId=" + visitId;
    instructionsTemplate = instructionsTemplate.replace("{1}", "<span><b>" + nonCodedDiagnosis + "</b></span>" );
    instructionsTemplate = instructionsTemplate.replace("{2}",
            "<span><b><a href="
            + patientDashboardLink + ">" + personName  + " (" + patientIdentifier + " )"
            + "</a></b></span>" );
    jq("#instructions").html(instructionsTemplate + ": ");
    codeDiagnosisDialog.show();
    return false;
}

function createCodeDiagnosisDialog() {
    codeDiagnosisDialog = null;
    codeDiagnosisDialog = emr.setupConfirmationDialog({
        selector: '#code-diagnosis-dialog',
        actions: {
            confirm: function() {
                var newDiagnoses = jq("input[name='diagnosis']");
                var diagnosesArray = new Array();
                jq.each(newDiagnoses, function(i, item) {
                    var newDiagnosis = jq.parseJSON(jq(item).val());
                    diagnosesArray.push(newDiagnosis);
                });

                emr.getFragmentActionWithCallback('coreapps', 'diagnoses', 'codeDiagnosis'
                    , { patientId: jq("#hiddenPatientId").val(),
                        nonCodedObsId: jq("#hiddenNonCodedObsId").val(),
                        diagnosis: JSON.stringify(diagnosesArray) }
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
