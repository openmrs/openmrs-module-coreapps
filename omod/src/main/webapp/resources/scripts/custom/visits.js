var visit = visit || {};

visit.quickVisitCreationDialog = null;
visit.retrospectiveVisitCreationDialog = null;
visit.retrospectiveVisitExistingVisitsDialog = null;
var editVisitDialogs = [];
visit.endVisitDialog = null;

// meant to be overrideable
visit.returnUrl = "/coreapps/patientdashboard/patientDashboard.page?patientId={{patientId}}&visitId={{visitId}}";

visit.reloadPageWithoutVisitId = function() {
    window.location.search = window.location.search.replace(/visitId=[^\&]*/g, "");
}

/**
 * Functions used to start a new visit (uses QuickVisitFragmentController)
 */

visit.createQuickVisitCreationDialog = function() {
    visit.quickVisitCreationDialog = emr.setupConfirmationDialog({
        selector: '#quick-visit-creation-dialog',
        actions: {
            confirm: function() {
                emr.getFragmentActionWithCallback('coreapps', 'visit/quickVisit', 'create', visit.buildVisitTypeAttributeParams(),
                    function(v) {
                        jq('#quick-visit-creation-dialog' + ' .icon-spin').css('display', 'inline-block').parent().addClass('disabled');
                        visit.goToReturnUrl(visit.patientId, v);
                    },function(err){
                        visit.reloadPageWithoutVisitId();
                    });
            },
            cancel: function() {
                visit.quickVisitCreationDialog.close();
            }
        }
    });

    visit.quickVisitCreationDialog.close();
}
visit.showQuickVisitCreationDialog = function(patientId) {
    visit.patientId = patientId;
    if (visit.quickVisitCreationDialog == null) {
        visit.createQuickVisitCreationDialog();
    }
    visit.quickVisitCreationDialog.show();
};

/**
* Functions used to create a retrospective visit (uses RetrospectiveVisitFragmentController)
*/
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
                            jq('#retrospective-visit-creation-dialog .icon-spin').css('display', 'inline-block').parent().addClass('disabled');
                            visit.goToReturnUrl(patientId, data);
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
									if (jq('.viewVisitDetails').length) {
                                    	visit.retrospectiveVisitExistingVisitsDialog.close();
                                    	jq('.viewVisitDetails').filter('[data-visit-Id=' + v.id + ']').click();
									} else {
                                        visit.goToReturnUrl(patientId, v);
									}
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

/**
 * Functions used to edit visit dates (uses VisitDatesFragmentController)
 */
visit.showEditVisitDateDialog = function(visitId) {
    if (!editVisitDialogs[visitId]) {
        editVisitDialogs[visitId] = emr.setupConfirmationDialog({
            selector: '#edit-visit-dates-dialog-' + visitId,
            actions: {
                confirm: function() {
                    var url = emr.fragmentActionLink("coreapps", "visit/visitDates", "setDuration");
                    $.getJSON(url, $('#edit-visit-dates-dialog-form-' + visitId).serialize()).success(function(data) {
                            if (data.success) {
                                jq('#edit-visit-dates-dialog-form-' + visitId + ' .icon-spin').css('display', 'inline-block').parent().addClass('disabled');
                                // TODO Do we need to update this to specify return url, or is this link only going  to ever be used from the old visits view? 
                                window.location.search = data.search;
                            }
                        }
                    );
                    return false;
                }
            }

        });
    }
    editVisitDialogs[visitId].show();
}

/**
 * Functions used to end a visit (uses VisitDetailsFragmentController)
 * TODO is this currently used at all?
 */
visit.createEndVisitDialog = function() {
    visit.endVisitDialog = emr.setupConfirmationDialog({
        selector: '#end-visit-dialog',
        actions: {
            confirm: function() {
                emr.getFragmentActionWithCallback('coreapps', 'visit/visitDetails', 'endVisit'
                    , { visitId: visit.visitId}
                    , function(data) {
                        visit.endVisitDialog.close();
                        // TODO Do we need to update this to specify return url, or is this link only going  to ever be used from the old visits view?
                        visit.reloadPageWithoutVisitId();
                    },function(err){
                        emr.handleError(err);
                        visit.endVisitDialog.close();
                    });
            },
            cancel: function() {
                visit.endVisitDialog.close();
            }
        }
    });
}

visit.showEndVisitDialog = function(visitId) {
    visit.visitId = visitId;
    if (visit.endVisitDialog == null) {
        visit.createEndVisitDialog();
    }
    visit.endVisitDialog.show();
};


visit.goToReturnUrl = function(patientId, v) {
    // hacked to support the different ways we allow one to ask for patient and visit id
    emr.navigateTo({ applicationUrl: emr.applyContextModel(visit.returnUrl,
        { patientId: patientId, visitId: v.id,
            'patient.patientId': patientId, 'visit.visitId': v.id,
            'patient.uuid': patientId, 'visit.uuid': v.id }) });
}
/**
  * Functions used to edit visits (uses EditVisitFragmentController)
 * * */
visit.showEditVisitDialog = function(visitId) {
    var editVisitDialog = emr.setupConfirmationDialog({
        selector: '#edit-visit-dialog-' + visitId,
        actions: {
            confirm: function() {
                if(visitId !== undefined){
                    var url = emr.fragmentActionLink("coreapps", "visit/quickVisit", "update");
                    jq.getJSON(url, visit.buildVisitTypeAttributeParams(visitId)).success(function(data) {
                            if(data.success) {
                                jq('#edit-visit-dialog-form-' + visitId + ' .icon-spin').css('display', 'inline-block').parent().addClass('disabled');
                                window.location.reload();
                            }
                        }
                    );
                }

                return false;
            },
            cancel: function() {
                editVisitDialog.close();
            }
        }
    });
    editVisitDialog.show();
}

visit.getCodedConcepts = function(conceptId, elementName, selectedValue, visitId){
        jQuery.ajax({
            url: emr.fragmentActionLink("coreapps", "visit/quickVisit", "getConceptAnswers",   { conceptId: conceptId }),
            dataType: 'json',
            type: 'POST'
        }).success(function(data) {
            var options;
            if(visitId !== undefined && visitId !== null){
                options = jq("#edit-visit-dialog-form-" + visitId + " select[name='" + elementName + "']");
            } else {
                options = jq("select[name='" + elementName + "']");
            }

            options.empty();

            var results = data.results;
            options.append(jq("<option>", {"value":'', "text": ''}));
            jq.each(results, function(key, value){
                var option = jq("<option>", {"value": value.uuid, "text": value.name});
                option.attr('class', 'dialog-drop-down small');
                if(selectedValue === value.uuid){
                    option.attr('selected', 'selected');
                }
                options.append(option);
            });
        });
}
visit.buildVisitTypeAttributeParams = function(visitId){
    var params = {};

    if(visitId === undefined) {
        params['patientId'] = visit.patientId;
        params['locationId'] = sessionLocationModel.id();
        params['selectedTypeId'] = jq('#visit-visittype-drop-down').find(":selected").val();
    } else {
        params['visitId'] = visitId;
        params['patientId'] = jq('#patientId').val();
        params['selectedTypeId'] = jq('#selectedTypeId').val();
    }

    var dateFormat = jq("#dateFormat").val();
    if(dateFormat != null && dateFormat !== undefined){
        dateFormat = dateFormat.replace("yyyy", "yy");
    }

    var formId = '';
    if(visitId !== undefined && visitId !== null){
        formId = "#edit-visit-dialog-form-" + visitId;
    }

    jq(formId + " input[name^='attribute']").map(function (index, element) {
        if(element.type === 'radio'){
            if(element.checked){
                params[jq(element).attr('name')]  = jq(element).val();
            }
        } else if(jq(element).val() !== null && jq(element).val() !== ''){
            if(element.name.indexOf('date') > -1){
                var date = new Date(jq(element).val());
                var formattedDate = jq.datepicker.formatDate(dateFormat, date);
                var name = element.name.replace('.date', '');
                params[name]  = formattedDate;
            } else {
                params[jq(element).attr('name')]  = jq(element).val();
            }
        }
    });

    // retrieve coded concept values
    jq(formId + " select[name^='attribute']").map(function (index, element) {
        params[jq(element).attr('name')] = jq(element).val();
    });

    return params;
}
visit.setSelectedDropdownValue = function (id, element){
    jq("#" + id).val(element.value);
    if(element.name !== undefined){
        jq("#" + id).attr('name', element.name);
    }
}