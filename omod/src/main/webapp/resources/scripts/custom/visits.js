var visit = visit || {};

visit.quickVisitCreationDialog = null;
visit.createQuickVisitCreationDialog = function(patientId) {
    visit.quickVisitCreationDialog = emr.setupConfirmationDialog({
        selector: '#quick-visit-creation-dialog',
        actions: {
            confirm: function() {
                emr.getFragmentActionWithCallback('coreapps', 'visit/quickVisit', 'create',
                    { patientId: patientId, locationId: sessionLocationModel.id() },
                    function(data) {
                        visit.quickVisitCreationDialog.close();
                        window.location.reload();
                    });
            },
            cancel: function() {
                visit.quickVisitCreationDialog.close();
            }
        }
    });

    visit.quickVisitCreationDialog.close();
}

visit.showQuickVisitCreationDialog = function() {
    visit.quickVisitCreationDialog.show();
};

visit.retrospectiveVisitCreationDialog = null;

visit.createRetrospectiveVisitDialog = function(patientId) {
    visit.retrospectiveVisitCreationDialog = emr.setupConfirmationDialog({
        selector: '#retrospective-visit-creation-dialog',
        actions: {
            confirm: function() {
                emr.getFragmentActionWithCallback('coreapps', 'visit/retrospectiveVisit', 'create',
                    { patientId: patientId, locationId: sessionLocationModel.id(),
                        startDate: jq('[name=retrospectiveVisitStartDate]').val(),
                        stopDate: jq('[name=retrospectiveVisitStopDate]').val() },
                    function(data) {
                        visit.retrospectiveVisitCreationDialog.close();
                        window.location.reload();
                    });
            },
            cancel: function() {
                visit.retrospectiveVisitCreationDialog.close();
            }
        }
    });
}

visit.showRetrospectiveVisitCreationDialog = function() {
    visit.retrospectiveVisitCreationDialog.show();
};