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