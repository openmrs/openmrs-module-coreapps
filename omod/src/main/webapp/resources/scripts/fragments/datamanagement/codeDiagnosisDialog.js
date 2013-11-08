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
    viewModel =ConsultFormViewModel();
    formatTemplate = _.template(jq('#autocomplete-render-template').html());
    ko.applyBindings(viewModel, jq('#contentForm').get(0));
    codeDiagnosisDialog = null;
    codeDiagnosisDialog = emr.setupConfirmationDialog({
        selector: '#code-diagnosis-dialog',
        actions: {
            confirm: function() {
                var newDiagnoses = jq("input[name='diagnosis']");
                var diagnosesArray = new Array();
                var obsId = jq("#hiddenNonCodedObsId").val();
                jq.each(newDiagnoses, function(i, item) {
                    var newDiagnosis = jq.parseJSON(jq(item).val());
                    diagnosesArray.push(newDiagnosis);
                });

                emr.getFragmentActionWithCallback('coreapps', 'diagnoses', 'codeDiagnosis'
                    , { patientId: jq("#hiddenPatientId").val(),
                        nonCodedObsId: obsId,
                        diagnosis: JSON.stringify(diagnosesArray) }
                    , function(data) {
                        emr.successMessage(data.message);
                        jq("#obs-id-" + obsId).remove();
                        codeDiagnosisDialog.close();
                    });
            },
            cancel: function() {
                codeDiagnosisDialog.close();
            }
        }
    });
}
