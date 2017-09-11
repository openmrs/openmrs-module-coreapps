function isFieldEmpty(fieldName) {
    var fieldValue = jq.trim(jq("#" + fieldName).val());
    //check if empty
    if (fieldValue === '') {
        return true;
    }
    return false;
}

function showError(fieldId, message) {
    var errorField = jq("#" + fieldId).nextAll(".field-error");
    errorField.text(message);
    errorField.show();
}

function hideError(fieldId) {
    var errorField = jq("#" + fieldId).nextAll(".field-error");
    errorField.text("");
    errorField.hide();
}

function enableTransferButton() {
    var transfer = jq(".transferPatient:checked").length;
    if (transfer > 0 ) {
        jq("#transfer-patients").prop('disabled', false);
        jq("#transfer-patients").removeClass('disabled');
    } else {
        jq("#transfer-patients").prop('disabled', true);
        jq("#transfer-patients").addClass('disabled');
    }

}

function validateProviderIdentifier(fieldId, personId) {
    var dfd = jq.Deferred();
    var existingPersonId = jq.trim(personId);
    if (existingPersonId === 'null' || existingPersonId === '') {
        existingPersonId = 0;
    }
    var inputValue = jq.trim(jq("#" + fieldId).val());
    if (isFieldEmpty(fieldId) == false) {
        // a value has been manually entered
        var url = '/' + OPENMRS_CONTEXT_PATH + '/ws/rest/v1/provider?v=custom:(uuid,identifier,display,person:(uuid,personId,display,gender,age,birthdate,birthdateEstimated,names))' +
            '&q=' + inputValue;
        jq.getJSON(url, function( data ) {
            if (data !=null && data.results != null && (data.results.length == 1) ) {
                var provider = data.results[0];
                if ( provider.identifier === inputValue
                        && ( parseInt(provider.person.personId) !== parseInt(existingPersonId) ) ) {
                    // the user entered Identifier that is already assigned to a different provider
                    dfd.resolve(false);
                }
            }
            dfd.resolve(true);
        });
    } else {
        dfd.resolve(true);
    }

    return dfd.promise();
}

function createAddPatientDialog() {
    addPatientDialog = emr.setupConfirmationDialog({
        selector: '#add-patient-dialog',
        actions: {
            confirm: function() {
                var patientId = jq("#patientId").val();
                var personId = jq("#providerId").val();
                var relationshipStartDateField = jq("#relationshipStartDate-field").val();
                var relationshipType = jq("select[name='relationshipType']").val();
                emr.getFragmentActionWithCallback('providermanagement', 'providerEdit', 'addPatient'
                    , { provider: personId,
                        patient: patientId,
                        relationshipType: relationshipType,
                        date: relationshipStartDateField
                    }
                    , function(data) {
                        addPatientDialog.close();
                        window.location.reload();
                    },function(err){
                        emr.handleError(err);
                        addPatientDialog.close();
                    });
            },
            cancel: function() {
                addPatientDialog.close();
            }
        }
    });
}

function showAddPatientDialog(){
    addPatientDialog.show();
}

function createAddSupervisorDialog(relationshipTypeId, relationshipId) {
    addSupervisorDialog = emr.setupConfirmationDialog({
        selector: '#add-supervisor-dialog',
        actions: {
            confirm: function() {
                var supervisorId = jq("#supervisorId").val();
                var superviseeId = jq("#add-supervisor-dialog-superviseeId").val();
                var relationshipStartDateField = jq("#supevisor-relationshipStartDate-field").val();
                emr.getFragmentActionWithCallback('providermanagement', 'providerEdit', 'editSupervisor'
                    , { supervisee: superviseeId,
                        supervisor: supervisorId,
                        relationship: (relationshipId !=null) ? relationshipId : "",
                        date: relationshipStartDateField
                    }
                    , function(data) {
                        addSupervisorDialog.close();
                        window.location.reload();
                    },function(err){
                        emr.handleError(err);
                        addSupervisorDialog.close();
                    });
            },
            cancel: function() {
                addSupervisorDialog.close();
            }
        }
    });
}

function showAddSupervisorDialog(supervisorId, supervisorLabel){
    addSupervisorDialog.show();
    if ((typeof supervisors !== 'undefined') && (supervisors != null) && supervisors.length > 0 ) {
        if (supervisorId != null && supervisorLabel != null && supervisorLabel.length > 0) {
            jq("#availableSupervisors").val(supervisorLabel);
            jq("#supervisorId").val(supervisorId);
        } else {
            jq("#availableSupervisors").autocomplete("search", "");
        }

    }
}

function createAddSuperviseeDialog() {
    addSuperviseeDialog = emr.setupConfirmationDialog({
        selector: '#add-supervisee-dialog',
        actions: {
            confirm: function() {
                var supervisorId = jq("#currentSupervisorId").val();
                var superviseeId = jq("#superviseeId").val();
                var relationshipStartDateField = jq("#supevisee-relationshipStartDate-field").val();
                emr.getFragmentActionWithCallback('providermanagement', 'providerEdit', 'addSupervisee'
                    , {
                        supervisor: supervisorId,
                        supervisee: superviseeId,
                        date: relationshipStartDateField
                    }
                    , function(data) {
                        addSuperviseeDialog.close();
                        window.location.reload();
                    },function(err){
                        emr.handleError(err);
                        addSuperviseeDialog.close();
                    });
            },
            cancel: function() {
                addSuperviseeDialog.close();
            }
        }
    });
}

function showAddSuperviseeDialog(){
    addSuperviseeDialog.show();
    if ((typeof supervisees !== 'undefined') && (supervisees != null) && supervisees.length > 0 ) {
        jq("#availableSupervisees").autocomplete("search", "");
    }
}

function createTransferPatientsDialog() {
    transferPatientsDialog = emr.setupConfirmationDialog({
        selector: '#transfer-patients-dialog',
        actions: {
            confirm: function() {
                var oldProviderId = jq("#oldProviderId").val();
                var newProviderId = jq("#newProviderId").val();
                var relationshipType = jq("select[name='transferRelationshipType']").val();
                var relationshipStartDateField = jq("#transfer-start-date-field").val();
                var relationships = [];
                jq(".transferPatient:checkbox:checked").each(function(){
                    var relationshipId = jq(this).attr("data-patient-relationship");
                    if (relationshipId) {
                        relationships.push(relationshipId);
                    }
                });                
                emr.getFragmentActionWithCallback('providermanagement', 'providerEdit', 'transferPatients'
                    , {
                        oldProvider: oldProviderId,
                        newProvider: newProviderId,
                        relationshipType: relationshipType,
                        patientRelationships: relationships,
                        date: relationshipStartDateField
                    }
                    , function(data) {
                        transferPatientsDialog.close();
                        window.location.reload();
                    },function(err){
                        emr.handleError(err);
                        transferPatientsDialog.close();
                    });
            },
            cancel: function() {
                transferPatientsDialog.close();
            }
        }
    });
}

function showTransferPatientsDialog(){
    transferPatientsDialog.show();

}

function createRemovePatientDialog(providerId, relationshipTypeId, relationshipId) {
    removePatientDialog = emr.setupConfirmationDialog({
        selector: '#remove-patient-dialog',
        actions: {
            confirm: function() {
                var relationshipEndDateField = jq("#relationshipEndDate-field").val();
                emr.getFragmentActionWithCallback('providermanagement', 'providerEdit', 'removePatient'
                    , { provider: providerId,
                        relationshipType: relationshipTypeId,
                        patientRelationship: relationshipId,
                        date: relationshipEndDateField
                    }
                    , function(data) {
                        removePatientDialog.close();
                        window.location.reload();
                    }, function(err){
                        emr.handleError(err);
                        removePatientDialog.close();
                    });
            },
            cancel: function() {
                removePatientDialog.close();
            }
        }
    });
}

function showRemovePatientDialog(){
    removePatientDialog.show();
}

function createUnassignSuperviseeDialog(supervisorId, superviseeId) {
    unassignSuperviseeDialog = emr.setupConfirmationDialog({
        selector: '#unassign-supervisee-dialog',
        actions: {
            confirm: function() {
                var relationshipEndDateField = jq("#superviseeEndDate-field").val();
                emr.getFragmentActionWithCallback('providermanagement', 'providerEdit', 'unassignSupervisee'
                    , { supervisor: supervisorId,
                        supervisee: superviseeId,
                        endDate: relationshipEndDateField
                    }
                    , function(data) {
                        unassignSuperviseeDialog.close();
                        window.location.reload();
                    }, function(err){
                        emr.handleError(err);
                        unassignSuperviseeDialog.close();
                    });
            },
            cancel: function() {
                unassignSuperviseeDialog.close();
            }
        }
    });
}

function showUnassignSuperviseeDialog(superviseeLabel){
    unassignSuperviseeDialog.show();
    jq("#superviseeName").text(superviseeLabel);

}

function createRetireProviderDialog(providerId) {
    retireProviderDialog = emr.setupConfirmationDialog({
        selector: '#retire-provider-dialog',
        actions: {
            confirm: function() {
                var retireReasonField = jq("#retireReason-field").val();
                emr.getFragmentActionWithCallback('providermanagement', 'providerEdit', 'retireProvider'
                    , { provider: providerId,
                        reason: retireReasonField
                    }
                    , function(data) {
                        retireProviderDialog.close();
                        window.location = "/" + OPENMRS_CONTEXT_PATH + "/coreapps/providermanagement/providerList.page";
                    }, function(err){
                        emr.handleError(err);
                        retireProviderDialog.close();
                    });
            },
            cancel: function() {
                retireProviderDialog.close();
            }
        }
    });
}
function showRetireProviderDialog(){
    retireProviderDialog.show();
}


function getSupervisors(roleId) {
    emr.getFragmentActionWithCallback('providermanagement', 'providerSearch', 'getSupervisors'
        , { roleId: roleId
        }
        , function(data) {
            supervisors = null;
            supervisors = jq.map(data, function (value, key) {
               return {
                   label:  value.givenName + " " + value.familyName,
                   value: value.personId
               };
            });

            if (supervisors.length > 0 ) {
                jq("#availableSupervisors").autocomplete({
                    source: supervisors,
                    minLength: 0,
                    scroll: true,
                    focus: function (event, ui ) {
                        jq("#availableSupervisors").val(ui.item.label);
                        return false;
                    },
                    select: function (event, ui ) {
                        jq("#availableSupervisors").val(ui.item.label);
                        jq("#supervisorId").val(ui.item.value);
                        return false;
                    }
                });
            }
        }, function(err){
            emr.handleError(err);
        });
}

function getAllProviders() {
    emr.getFragmentActionWithCallback('providermanagement', 'providerSearch', 'getSupervisees'
        , null
        , function(data) {
            allProviders = null;
            allProviders = jq.map(data, function (value, key) {
                return {
                    label:  value.givenName + " " + value.familyName,
                    value: value.personId
                };
            });

            if (allProviders.length > 0 ) {
                jq("#availableProviders").autocomplete({
                    source: allProviders,
                    minLength: 0,
                    scroll: true,
                    focus: function (event, ui ) {
                        jq("#availableProviders").val(ui.item.label);
                        return false;
                    },
                    select: function (event, ui ) {
                        jq("#availableProviders").val(ui.item.label);
                        jq("#newProviderId").val(ui.item.value);
                        return false;
                    }
                });
            }
        }, function(err){
            emr.handleError(err);
        });
}

function getSupervisees(roleId) {
    emr.getFragmentActionWithCallback('providermanagement', 'providerSearch', 'getSupervisees'
        , { roleId: roleId
        }
        , function(data) {
            supervisees = null;
            supervisees = jq.map(data, function (value, key) {
                return {
                    label:  value.givenName + " " + value.familyName,
                    value: value.personId
                };
            });

            if (supervisees.length > 0 ) {
                jq("#availableSupervisees").autocomplete({
                    source: supervisees,
                    minLength: 0,
                    scroll: true,
                    focus: function (event, ui ) {
                        jq("#availableSupervisees").val(ui.item.label);
                        return false;
                    },
                    select: function (event, ui ) {
                        jq("#availableSupervisees").val(ui.item.label);
                        jq("#superviseeId").val(ui.item.value);
                        return false;
                    }
                });
            }
        }, function(err){
            emr.handleError(err);
        });
}

function getProviderAttributes(roleId) {
    // hide all provider attributes
    jq(".providerAttributeDiv").addClass("hidden");
    emr.getFragmentActionWithCallback('providermanagement', 'providerSearch', 'getProviderAttributes'
        , { roleId: roleId
        }
        , function(data) {
            jq.each(data, function( index, value) {
                 jq("div#attributeTypeId_" + value.providerAttributeTypeId).removeClass("hidden");
            });
        }, function(err){
            emr.handleError(err);
        });
}

function getCodedConcepts(conceptId, elementName, selectedValue){
    jQuery.ajax({
        url: emr.fragmentActionLink("providermanagement", "providerView", "getCodedConcepts",   { conceptId: conceptId }),
        dataType: 'json',
        type: 'POST'
    }).success(function(data) {
        var options;
        options = jq("select[name='" + elementName + "']");
        
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

function getLocations(tagId, elementName, selectedValue){

    var url = '/' + OPENMRS_CONTEXT_PATH + '/ws/rest/v1/location?v=custom:(id,uuid,name)';
    if ( tagId ) {
        url = url + '&tag=' + tagId;
    }

    jq.getJSON(url, function( data ) {
        if ( data !=null && data.results != null ) {
            var options;
            options = jq("select[name='" + elementName + "']");
            options.empty();
            var results = data.results;
            options.append(jq("<option>", {"value":'', "text": ''}));
            jq.each(results, function(i, obj){
                var option = jq("<option>", {"value": obj.uuid, "text": obj.name});
                option.attr('class', 'dialog-drop-down small');
                if(selectedValue === obj.uuid){
                    option.attr('selected', 'selected');
                }
                options.append(option);
            });
        }
    });
}