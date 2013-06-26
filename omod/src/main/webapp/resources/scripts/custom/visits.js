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
                        if (data.success) {
                            window.location = data.url;
                        }
                        else {
                           // display the past visits within the existing visits dialog (which we open below)
                           jq('#past-visit-dates').empty();

                            // TODO: this all seems a little hacky/confusing/non-sustainable to me
                            // TODO: I trigger a certain visit by triggering a click on property visit details item
                            // TODO: might be something we want to do with angularJS going forward?
                            data.forEach(function (v) {
                                var listItem = jq("<li class=\"menu-item past-visit-date-item\" visitId=\"visit.id\">" + v.startDate + " - " + v.stopDate + "</li>");

                                listItem.click(function() {
                                    visit.retrospectiveVisitExistingVisitsDialog.close();
                                    jq('.viewVisitDetails').filter('[visitId=' + v.id + ']').click();
                                });

                                jq('#past-visit-dates').append(listItem);
                            })

                            visit.retrospectiveVisitCreationDialog.close();
                            visit.retrospectiveVisitExistingVisitsDialog.show();
                        }
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

visit.retrospectiveVisitExistingVisitsDialog = null;

visit.createRetrospectiveVisitExistingVisitsDialog = function() {

    visit.retrospectiveVisitExistingVisitsDialog = emr.setupConfirmationDialog({
        selector: '#retrospective-visit-existing-visits-dialog',
        actions: {

            confirm: function() {
                visit.retrospectiveVisitExistingVisitsDialog.close();
                visit.retrospectiveVisitCreationDialog.show();
            },

            cancel: function() {
                visit.retrospectiveVisitExistingVisitsDialog.close();
            }
        }

    });
}

var editVisitDialogs = [];
function showEditVisitDateDialog(visitId) {
    if (!editVisitDialogs[visitId]) {
        editVisitDialogs[visitId] = emr.setupConfirmationDialog({
            selector: '#edit-visit-dates-dialog-' + visitId,
            actions: {
                confirm: function() {
                    var url = emr.fragmentActionLink("coreapps", "visit/visitDates", "setDuration");
                    $.getJSON(url, $('#edit-visit-dates-dialog-form-' + visitId).serialize()).success(function() {
                            window.location.reload();
                        }
                    );
                    return false;
                }
            }

        });
    }
    editVisitDialogs[visitId].show();
}
