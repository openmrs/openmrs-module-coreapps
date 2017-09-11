<%
    ui.decorateWith("appui", "standardEmrPage")

    ui.includeCss("coreapps", "bootstrap.css")
    ui.includeCss("coreapps", "providermanagement/providermanagement.css")

    ui.includeJavascript("coreapps", "providermanagement/editProvider.js")

    def genderOptions = [ [label: ui.message("emr.gender.M"), value: 'M'],
                          [label: ui.message("emr.gender.F"), value: 'F'] ]

    def createAccount = (account.person.personId == null || account.provider == null) ? true : false;

    def afterSelectedUrl = '/coreapps/providermanagement/editProvider.page?patientId={{patientId}}&personId=' + account.person.personId

    def providerRolesOptions = []
    providerRoles. each {
        providerRolesOptions.push([ label: ui.format(it), value: it.id ])
    }
    providerRolesOptions = providerRolesOptions.sort { it.id };

    def relationshipTypesOptions = []
    relationshipTypes. each {
        relationshipTypesOptions.push([ label: ui.format(it.aIsToB + "/" + it.bIsToA), value: it.relationshipTypeId ])
    }
    relationshipTypesOptions = relationshipTypesOptions.sort { it.label };

    def assignedSupervisor = null
    supervisorsForProvider.each {
        if (it.relationship && it.relationship.endDate == null) {
            assignedSupervisor = it.person.personName
        }
    }
    def hasActivePatients = false
    patientsList.each {
        if ((it.relationship != null) && (it.relationship.endDate == null)) {
            hasActivePatients = true
        }
    }
    def editDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")
    def formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");
    def fieldClasses = ["required", "validateField"]

%>

<style>
span.field-error {
    padding: 1px 6px 1px 6px;
    margin-left: 4px;
    margin-right: 4px;
    vertical-align: middle;
    color: red;
}
</style>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("providermanagement.providerList")}", link: '${ui.pageLink("coreapps", "providermanagement/providerList")}' },
        { label: "${ ui.message("Provider.edit")}" }

    ];

    var selectPatientHandler = {
        handle: function (row, widgetData) {
            var query = widgetData.lastQuery;
            jq('#patientId').val(row.uuid);

            jq('#patient-search').val(
                    row.patientIdentifier.identifier + ", "
                    + row.person.personName.display + ", "
                    + row.person.gender + ", "
                    + row.person.age);

            jq('#patient-search-results').fadeOut();
            jq('#addPatientToList').show();
        }
    }

    jq(function() {

        var addPatientDialog = null;
        var transferPatientsDialog = null;
        var removePatientDialog = null;
        var addSupervisorDialog = null;
        var addSuperviseeDialog = null;
        var unassignSuperviseeDialog = null;
        var retireProviderDialog = null;
        var supervisors = null;
        var supervisees = null;
        var allProviders = null;

        jq('#patient-search').attr("size", "40");

        if ('${hasActivePatients}' == 'true' ) {
            jq("#retire-button").prop('disabled', true);
            jq("#retire-button").addClass('disabled');
        }

        jq("#select-all-patients").change(function(){
            if(this.checked){
                jq(".transferPatient").prop("checked", true);
            } else {
                jq(".transferPatient").prop("checked", false);
            }
            enableTransferButton();
        });

        jq(".transferPatient").change(function(){
            enableTransferButton();
        });

        jq("#add-patient-button").click(function(event) {
            createAddPatientDialog();
            showAddPatientDialog();
            event.preventDefault();
        });

        jq("#retire-button").click(function(event) {
            var providerId = jq(event.target).attr("data-provider-id");
            createRetireProviderDialog(providerId);
            showRetireProviderDialog();
            event.preventDefault();
        });

        jq(document).on('click', '.delete-relationship', function(event) {
            var providerId = jq(event.target).attr("data-provider-id");
            var relationshipTypeId = jq(event.target).attr("data-relationship-type");
            var relationshipId = jq(event.target).attr("data-patient-relationship");

            createRemovePatientDialog(providerId, relationshipTypeId, relationshipId);
            showRemovePatientDialog();

            event.preventDefault();
            // this is just to prevent datimepicker dropdown to display by default
            setTimeout(function() {
                jq(".datetimepicker").hide();
            }, 100);

        });

        jq(document).on('click', '.delete-supervisee', function(event) {
            var supervisorId = jq(event.target).attr("data-supervisor-id");
            var superviseeId = jq(event.target).attr("data-supervisee-id");
            var superviseeLabel = jq(event.target).attr("data-supervisor-label");

            createUnassignSuperviseeDialog(supervisorId, superviseeId);
            showUnassignSuperviseeDialog(superviseeLabel);

            event.preventDefault();
            // this is just to prevent datimepicker dropdown to display by default
            setTimeout(function() {
                jq(".datetimepicker").hide();
            }, 100);

        });

        jq(document).on('click', '.edit-supervisor', function(event) {
            var supervisorId = jq(event.target).attr("data-supervisor-id");
            var supervisorLabel = jq(event.target).attr("data-supervisor-label");
            var relationshipTypeId = jq(event.target).attr("data-relationship-type");
            var relationshipId = jq(event.target).attr("data-supervisor-relationship");

            createAddSupervisorDialog(relationshipTypeId, relationshipId);
            showAddSupervisorDialog(supervisorId, supervisorLabel);

            event.preventDefault();
            // this is just to prevent datimepicker dropdown to display by default
            setTimeout(function() {
                jq(".datetimepicker").hide();
            }, 100);

        });

        jq("#add-supervisor-button").click(function(event) {
            createAddSupervisorDialog(null, null, null);
            showAddSupervisorDialog(null, null);
            event.preventDefault();
        });

        jq("#add-supervisee-button").click(function(event) {
            createAddSuperviseeDialog();
            showAddSuperviseeDialog(null, null);
            event.preventDefault();
        });

        jq("#transfer-patients").click(function(event) {
            createTransferPatientsDialog();
            showTransferPatientsDialog();
            event.preventDefault();
        });

        jq("select[name='providerRole']").on('change', function(event) {
            var roleId = this.value;
            getProviderAttributes(parseInt(roleId));
            getSupervisors(parseInt(roleId));
        });

        if ('${createAccount}' == 'true') {
            jq("input[name='givenName']").focus();
        } else {
            jq("#add-patient-button").focus();
        }

        var selectedProviderRole = jq("select[name='providerRole']").val();
        if ( (selectedProviderRole != null) && (parseInt(selectedProviderRole) > 0) ) {
            getProviderAttributes(parseInt(selectedProviderRole));
            getSupervisors(parseInt(selectedProviderRole));
            getSupervisees(parseInt(selectedProviderRole));
            getAllProviders();
        }

        jq("#accountForm").submit(function(e) {
            var returnValue = true;

            var thisForm = this;
            e.preventDefault();
            e.returnValue = false;

            jq('.validateField').each(function(i, item) {
                var fieldId = jq(item).attr("id");
                if (isFieldEmpty(fieldId) === true) {
                    showError(fieldId, "Field is required");
                    returnValue = false;
                    return false;
                }
            });

            if ( returnValue == true ) {
                var fieldId = "providerIdentifier-field";
                jq.when(validateProviderIdentifier(fieldId, '${account.person.personId}')).then(function (status) {
                    if (status == false) {
                        showError(fieldId, "Identifier in use already");
                    }
                    if (status == true ) {
                        thisForm.submit();
                    }
                    return status;
                }, function (status) {
                    return status;
                });
            } else {
                return returnValue;
            }

        });

        jq(".validateField").change(function() {
            var fieldId = jq(this).attr("id");
            if (isFieldEmpty(fieldId) !== true) {
                hideError(fieldId);
            }
        });

        jq("#providerIdentifier-field").change(function() {
            var fieldId = jq(this).attr("id");
            hideError(fieldId);
        });

    });


</script>

<style type="text/css">
#unlock-button {
    margin-top: 1em;
}
#add-patient-dialog {
    width: 650px;
}
</style>

<div id="add-supervisor-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <h3>${ ui.message("providermanagement.createRelationship") }</h3>
    </div>
    <div class="dialog-content">
        <input type="hidden" id="add-supervisor-dialog-superviseeId" value="${account.person.personId}"/>
        <input type="hidden" id="supervisorId" value=""/>

        <span>${ ui.message("providermanagement.assignSupervisor") }</span>

        <div class="panel-body ">
            <fieldset>
                <p>
                    ${ ui.message("providermanagement.findSupervisor") }:
                    <input id="availableSupervisors" value="" autocomplete="off">
                </p>
                <br><br>
                <p>
                    ${ ui.includeFragment("uicommons", "field/datetimepicker", [
                            id: "supevisor-relationshipStartDate",
                            formFieldName: "supevisor-relationshipStartDate",
                            label: ui.message("providermanagement.startDate") + ": &nbsp;&nbsp;",
                            defaultDate: new Date(),
                            endDate: editDateFormat.format(new Date()),
                            useTime: false,
                    ])}
                </p>
                <br><br>

            </fieldset>

        </div>
        <button class="confirm right">${ ui.message("coreapps.confirm") }</button>
        <button class="cancel">${ ui.message("coreapps.cancel") }</button>
    </div>
</div>

<div id="transfer-patients-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <h3>${ ui.message("providermanagement.transfer") }</h3>
    </div>
    <div class="dialog-content">
        <input type="hidden" id="oldProviderId" value="${account.person.personId}"/>
        <input type="hidden" id="newProviderId" value=""/>

        <span>${ ui.message("providermanagement.chw.transferPatients") }</span>

        <div class="panel-body ">
            <fieldset>
                <p>
                    ${ ui.message("providermanagement.provider.find") }:
                    <input id="availableProviders" value="" autocomplete="off">
                </p>
                <br><br>
                <p>
                    ${ ui.includeFragment("uicommons", "field/datetimepicker", [
                            id: "transfer-start-date",
                            formFieldName: "transfer-start-date",
                            label: ui.message("providermanagement.transferDate") + ": &nbsp;&nbsp;",
                            defaultDate: new Date(),
                            endDate: editDateFormat.format(new Date()),
                            useTime: false,
                    ])}
                </p>
                <br><br>
                <p>
                    ${ ui.includeFragment("uicommons", "field/dropDown", [
                            label: ui.message("providermanagement.relationshipType"),
                            formFieldName: "transferRelationshipType",
                            classes: ["required"],
                            options: relationshipTypesOptions,
                            hideEmptyLabel: true,
                            expanded: true
                    ])}
                </p>

            </fieldset>

        </div>
        <button class="confirm right">${ ui.message("coreapps.confirm") }</button>
        <button class="cancel">${ ui.message("coreapps.cancel") }</button>
    </div>
</div>

<div id="add-supervisee-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <h3>${ ui.message("providermanagement.createRelationship") }</h3>
    </div>
    <div class="dialog-content">
        <input type="hidden" id="currentSupervisorId" value="${account.person.personId}"/>
        <input type="hidden" id="superviseeId" value=""/>

        <span>${ ui.message("providermanagement.supervisee.add") }</span>

        <div class="panel-body ">
            <fieldset>
                <p>
                    ${ ui.message("providermanagement.supervisee.find") }:
                    <input id="availableSupervisees" value="" autocomplete="off">
                </p>
                <br><br>
                <p>
                    ${ ui.includeFragment("uicommons", "field/datetimepicker", [
                            id: "supevisee-relationshipStartDate",
                            formFieldName: "supevisee-relationshipStartDate",
                            label: ui.message("providermanagement.startDate") + ": &nbsp;&nbsp;",
                            defaultDate: new Date(),
                            endDate: editDateFormat.format(new Date()),
                            useTime: false,
                    ])}
                </p>
                <br><br>

            </fieldset>

        </div>
        <button class="confirm right">${ ui.message("coreapps.confirm") }</button>
        <button class="cancel">${ ui.message("coreapps.cancel") }</button>
    </div>
</div>

<div id="retire-provider-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <h3>${ ui.message("providermanagement.retireProvider") }</h3>
    </div>
    <div class="dialog-content">
        <input type="hidden" id="providerId" value="${account.person.personId}"/>

        <span>${ ui.message("providermanagement.confirmRetire") }</span>

        <div class="panel-body ">
            <fieldset>
                <p>
                    ${ ui.includeFragment("uicommons", "field/text", [
                            id: "retireReason",
                            formFieldName: "retireReason",
                            label: ui.message("providermanagement.retireReason")
                    ])}
                </p>
            </fieldset>
        </div>

        <button id="retire-provider-button" class="confirm right">${ ui.message("general.retire") }</button>
        <button class="cancel">${ ui.message("coreapps.cancel") }</button>
    </div>
</div>

<div id="remove-patient-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <h3>${ ui.message("coreapps.relationships.delete.header") }</h3>
    </div>
    <div class="dialog-content">
        <input type="hidden" id="providerId" value="${account.person.personId}"/>
        <input type="hidden" id="patientId" value=""/>

        <span>${ ui.message("providermanagement.removePatientFromProvider") }?</span>

        <div class="panel-body ">
            <fieldset>
                <p>
                    ${ ui.includeFragment("uicommons", "field/datetimepicker", [
                            id: "relationshipEndDate",
                            formFieldName: "relationshipEndDate",
                            label: ui.message("providermanagement.stopDate") + ": &nbsp;&nbsp;",
                            defaultDate: new Date(),
                            endDate: editDateFormat.format(new Date()),
                            useTime: false,
                    ])}
                </p>
            </fieldset>
        </div>
        <button id="remove-patient-button" class="confirm right">${ ui.message("coreapps.relationships.delete.header") }</button>
        <button class="cancel">${ ui.message("coreapps.cancel") }</button>
    </div>
</div>

<div id="unassign-supervisee-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <h3>${ ui.message("providermanagement.supervisee.unassign") }</h3>
    </div>
    <div class="dialog-content">
        <input type="hidden" id="providerId" value="${account.person.personId}"/>
        <input type="hidden" id="patientId" value=""/>

        <span>${ ui.message("providermanagement.supervisee.unassign.confirm") }?</span>

        <div class="panel-body ">
            <fieldset>
                <p>
                    ${ ui.message("providermanagement.supervisee.name") }:
                    <strong><label id="superviseeName"></label></strong>
                </p>
                <br><br>
                <p>
                    ${ ui.includeFragment("uicommons", "field/datetimepicker", [
                            id: "superviseeEndDate",
                            formFieldName: "superviseeEndDate",
                            label: ui.message("providermanagement.stopDate") + ": &nbsp;&nbsp;",
                            defaultDate: new Date(),
                            endDate: editDateFormat.format(new Date()),
                            useTime: false,
                    ])}
                </p>
            </fieldset>
        </div>
        <button id="unassign-supervisee-button" class="confirm right">${ ui.message("coreapps.relationships.delete.header") }</button>
        <button class="cancel">${ ui.message("coreapps.cancel") }</button>
    </div>
</div>

<div id="add-patient-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <h3>${ ui.message("providermanagement.createRelationship") }</h3>
    </div>
    <div class="dialog-content">
        <input type="hidden" id="providerId" value="${account.person.personId}"/>
        <input type="hidden" id="patientId" value=""/>

        <span>${ ui.message("providermanagement.assignPatientToProvider") }</span>

        <div class="panel-body ">
            <fieldset>
                <p>
                    ${ ui.message("Patient.find") }:
                    ${ ui.includeFragment("coreapps", "patientsearch/patientSearchWidget",
                            [ afterSelectedUrl: afterSelectedUrl,
                              rowSelectionHandler: "selectPatientHandler",
                              showLastViewedPatients: 'false' ])}
                </p>
                <br><br>
                <p>
                    ${ ui.includeFragment("uicommons", "field/datetimepicker", [
                            id: "relationshipStartDate",
                            formFieldName: "relationshipStartDate",
                            label: ui.message("providermanagement.startDate") + ": &nbsp;&nbsp;",
                            defaultDate: new Date(),
                            endDate: editDateFormat.format(new Date()),
                            useTime: false,
                    ])}
                </p>
                <br><br>
                <p>
                    ${ ui.includeFragment("uicommons", "field/dropDown", [
                            label: ui.message("providermanagement.relationshipType"),
                            formFieldName: "relationshipType",
                            classes: ["required"],
                            options: relationshipTypesOptions,
                            hideEmptyLabel: true,
                            expanded: true
                    ])}
                </p>
            </fieldset>

        </div>
        <button class="confirm right">${ ui.message("coreapps.confirm") }</button>
        <button class="cancel">${ ui.message("coreapps.cancel") }</button>
    </div>
</div>

<h3>${ (createAccount) ? ui.message("Provider.create") : ui.message("Provider.edit") }</h3>

<div class="row">
    <div class="col-sm-5">
        <div class="panel panel-info">
            <div class="panel-heading">
                <h3 class="panel-title">${ui.message("providermanagement.provider")}</h3>
            </div>
            <div class="panel-body ">

                <form method="post" id="accountForm" autocomplete="off">
                    <!-- dummy fields so that Chrome doesn't autocomplete the real username/password fields with the users own password -->
                    <input style="display:none" type="text" name="wrong-username-from-autocomplete"/>
                    <input style="display:none" type="password" name="wrong-username-from-autocomplete"/>

                    ${ ui.includeFragment("uicommons", "field/text", [
                            label: ui.message("coreapps.person.givenName"),
                            id: "givenName",
                            formFieldName: "givenName",
                            classes: fieldClasses,
                            initialValue: (account.givenName ?: '')
                    ])}

                    ${ ui.includeFragment("uicommons", "field/text", [
                            label: ui.message("coreapps.person.familyName"),
                            formFieldName: "familyName",
                            id: "familyName",
                            classes: fieldClasses,
                            initialValue: (account.familyName ?: '')
                    ])}

                    ${ ui.includeFragment("uicommons", "field/radioButtons", [
                            label: ui.message("emr.gender"),
                            formFieldName: "gender",
                            initialValue: (account.gender ?: 'M'),
                            options: genderOptions
                    ])}

                    ${ ui.includeFragment("uicommons", "field/text", [
                            label: ui.message("providermanagement.identifier"),
                            formFieldName: "providerIdentifier",
                            id: "providerIdentifier",
                            initialValue: (account.provider ? account.provider.identifier: '')
                    ])}

                    <div class="emr_providerDetails">
                        <p>
                            ${ ui.includeFragment("uicommons", "field/dropDown", [
                                    label: ui.message("providermanagement.providerRole"),
                                    formFieldName: "providerRole",
                                    initialValue: (account.providerRole?.id ?: ''),
                                    options: providerRolesOptions,
                                    classes: fieldClasses,
                                    hideEmptyLabel: true,
                                    expanded: true
                            ])}
                        </p>

                    </div>

                    <% if (useAddressHierarchy == true ) { %>
                        ${ ui.includeFragment("registrationapp", "field/personAddressWithHierarchy", [
                                id: "providerAddress",
                                initialValue: (account.person ? account.person.personAddress : null)
                        ])}
                    <% } %>

                    <div id="providerAttributesDiv" class="emr_providerDetails">
                            <% if (createAccount != true ) {
                                if ( account.provider.attributes !=null && account.provider.attributes.size() > 0 ) {
                                    account.provider.attributes.each { attribute ->
                                                if ( attribute.attributeType.datatypeClassname == 'org.openmrs.module.coreapps.customdatatype.LocationDatatype' ) { %>
                                                        <label>${attribute.attributeType.name}</label>
                                                        <select id="location-data-types" name="providerAttributeId_${attribute.providerAttributeId}"></select>
                                                        <script>
                                                            var tagId = '${attribute.attributeType.datatypeConfig}';
                                                            getLocations(tagId, 'providerAttributeId_${attribute.providerAttributeId}', '${attribute.valueReference}');
                                                        </script>

                                            <% } else if ( attribute.attributeType.datatypeClassname == 'org.openmrs.customdatatype.datatype.DateDatatype' ) {  %>
                                                    ${ ui.includeFragment("uicommons", "field/datetimepicker", [
                                                            id: "providerAttributeId_" + attribute.providerAttributeId,
                                                            formFieldName: "providerAttributeId_" + attribute.providerAttributeId,
                                                            label: attribute.attributeType.name,
                                                            defaultDate: formatter.parse(attribute.valueReference),
                                                            useTime: false,
                                                ])}
                                                <% } else if ( attribute.attributeType.datatypeClassname == 'org.openmrs.module.coreapps.customdatatype.CodedConceptDatatype' ) { %>
                                                    <label>${attribute.attributeType.name}</label>
                                                    <select id="coded-data-types" name="providerAttributeId_${attribute.providerAttributeId}"></select>
                                                    <script>
                                                        var conceptId = '${attribute.attributeType.datatypeConfig}';
                                                        getCodedConcepts(conceptId, 'providerAttributeId_${attribute.providerAttributeId}', '${attribute.valueReference}');
                                                    </script>
                                            <% } else { %>
                                                ${ ui.includeFragment("uicommons", "field/text", [
                                                        label: attribute.attributeType.name,
                                                        formFieldName: "providerAttributeId_" + attribute.providerAttributeId,
                                                        initialValue: attribute.valueReference
                                                ])}
                                            <% } %>
                                    <% } %>
                                <% }
                            }
                            if (providerAttributeTypes != null && providerAttributeTypes.size() > 0) {
                                providerAttributeTypes.each { attributeType ->
                                    if ( attributeType.datatypeClassname == 'org.openmrs.customdatatype.datatype.DateDatatype' ) {  %>
                                            <div id="attributeTypeId_${attributeType.providerAttributeTypeId}" class="providerAttributeDiv hidden">
                                                ${ ui.includeFragment("uicommons", "field/datetimepicker", [
                                                        id: "attributeTypeId_" + attributeType.providerAttributeTypeId,
                                                        formFieldName: "attributeTypeId_" + attributeType.providerAttributeTypeId,
                                                        label: attributeType.name,
                                                        useTime: false,
                                                ])}
                                            </div>
                                     <% } else if ( attributeType.datatypeClassname == 'org.openmrs.module.coreapps.customdatatype.CodedConceptDatatype' ) { %>
                                            <div id="attributeTypeId_${attributeType.providerAttributeTypeId}" class="providerAttributeDiv hidden">
                                                <label>${attributeType.name}</label>
                                                <select id="attributeTypeId_${attributeType.providerAttributeTypeId}" name="attributeTypeId_${attributeType.providerAttributeTypeId}"></select>
                                                <script>
                                                    var conceptId = '${attributeType.datatypeConfig}';
                                                    getCodedConcepts(conceptId, 'attributeTypeId_${attributeType.providerAttributeTypeId}');
                                                </script>
                                            </div>
                                <% } else if ( attributeType.datatypeClassname == 'org.openmrs.module.coreapps.customdatatype.LocationDatatype' ) { %>
                                            <div id="attributeTypeId_${attributeType.providerAttributeTypeId}" class="providerAttributeDiv hidden">
                                                <label>${attributeType.name}</label>
                                                <select id="attributeTypeId_${attributeType.providerAttributeTypeId}" name="attributeTypeId_${attributeType.providerAttributeTypeId}"></select>
                                                <script>
                                                    var tagId = '${attributeType.datatypeConfig}';
                                                    getLocations(tagId, 'attributeTypeId_${attributeType.providerAttributeTypeId}');
                                                </script>
                                            </div>
                                    <% } else { %>
                                            <div id="attributeTypeId_${attributeType.providerAttributeTypeId}" class="providerAttributeDiv hidden">
                                                ${ ui.includeFragment("uicommons", "field/text", [
                                                        label: attributeType.name,
                                                        id: "attributeTypeId_" + attributeType.providerAttributeTypeId,
                                                        formFieldName: "attributeTypeId_" + attributeType.providerAttributeTypeId
                                                ])}
                                            </div>
                                <% } %>
                            <% } %>
                            <% } %>

                    </div>

                    <div>
                        <input type="button" class="cancel" value="${ ui.message("emr.cancel") }" onclick="javascript:window.location='/${ contextPath }/coreapps/providermanagement/providerList.page'" />
                        <input type="submit" class="confirm" id="save-button" value="${ ui.message("emr.save") }"  />
                        <% if (createAccount != true ) { %>
                            <input type="button" id="retire-button" value="${ ui.message("general.retire") }" data-provider-id="${account.person.personId}" />
                        <% } %>
                    </div>
                </form>

            </div>
        </div>
    </div>

    <div class="col-sm-7">

        <div class="panel panel-info">
            <div class="panel-heading">
                <h3 class="panel-title">${ ui.message("providermanagement.supervisor") }</h3>
            </div>
            <div class="panel-body">
                <% if (assignedSupervisor != null) { %>
                    <table class="table table-condensed borderless">
                        <tbody>
                        <tr>
                            <table id="supervisor-list" width="100%" border="1" cellspacing="0" cellpadding="2">
                                <thead>
                                <tr>
                                    <th>${ ui.message("providermanagement.identifier") }</th>
                                    <th>${ ui.message("coreapps.person.name") }</th>
                                    <th>${ ui.message("providermanagement.startDate") }</th>
                                    <th>&nbsp;</th>
                                </tr>
                                </thead>

                                <tbody>
                                <% if ((supervisorsForProvider == null) ||
                                        (supervisorsForProvider != null && supervisorsForProvider.size() == 0)) { %>
                                <tr>
                                    <td colspan="4">${ ui.message("coreapps.none") }</td>
                                </tr>
                                <% } %>
                                <% supervisorsForProvider.each { supervisor ->
                                    if (supervisor.relationship && supervisor.relationship.endDate == null) {  // display only active supervisors
                                %>
                                <tr id="patient-${ supervisor.person.personId }">
                                    <td>${ ui.format(supervisor.identifier) }</td>
                                    <td>
                                        <a href="/${ contextPath }/coreapps/providermanagement/editProvider.page?personId=${ supervisor.person.personId }">${ ui.format(supervisor.person.personName) }</a>
                                    </td>
                                    <td>${ ui.format(supervisor.relationship.startDate) }</td>
                                    <td><a><i class="edit-supervisor icon-pencil"
                                              data-provider-id="${account.person.personId}"
                                              data-supervisor-id="${ supervisor.person.personId }"
                                              data-supervisor-label="${ supervisor.person.personName }"
                                              data-relationship-type="${supervisor.relationshipType.id}"
                                              data-supervisor-relationship="${ supervisor.relationship.id }"
                                    ></i></a>
                                    </td>
                                </tr>
                                <% }
                                } %>
                                </tbody>
                            </table>
                        </tr>
                        </tbody>
                    </table>
                <% } else if (createAccount != true ) { %>
                    <a href="">
                        <button id="add-supervisor-button">${ ui.message("providermanagement.addSupervisor") }
                        &nbsp; <i class="icon-plus"></i>
                        </button>
                    </a>
                <% } %>

            </div>
        </div>

        <% if (isSupervisor == true) { %>
            <div class="panel panel-info">
                <div class="panel-heading">
                    <h3 class="panel-title">${ ui.message("providermanagement.currentSupervisees") }</h3>
                </div>
                <div class="panel-body">

                    <table class="table table-condensed borderless">
                        <tbody>

                        <% if (createAccount != true ) { %>
                            <a href="">
                                <button id="add-supervisee-button">${ ui.message("providermanagement.supervisee.add") }
                                &nbsp; <i class="icon-plus"></i>
                                </button>
                            </a>
                        <% } %>

                        <tr>
                            <table id="supervisee-list" width="100%" border="1" cellspacing="0" cellpadding="2">
                                <thead>
                                <tr>
                                    <th>${ ui.message("providermanagement.identifier") }</th>
                                    <th>${ ui.message("coreapps.person.name") }</th>
                                    <th>${ ui.message("providermanagement.startDate") }</th>
                                    <th>&nbsp;</th>
                                </tr>
                                </thead>

                                <tbody>
                                <% if ((superviseesForSupervisor == null) ||
                                        (superviseesForSupervisor != null && superviseesForSupervisor.size() == 0)) { %>
                                <tr>
                                    <td colspan="4">${ ui.message("coreapps.none") }</td>
                                </tr>
                                <% } %>
                                <% superviseesForSupervisor.each { supervisee ->
                                    if (supervisee.relationship && supervisee.relationship.endDate == null) {  // display only active supervisee
                                %>
                                <tr id="patient-${ supervisee.person.personId }">
                                    <td>${ ui.format(supervisee.identifier) }</td>
                                    <td>
                                        <a href="/${ contextPath }/coreapps/providermanagement/editProvider.page?personId=${ supervisee.person.personId }">
                                            ${ ui.format(supervisee.person.personName) }
                                        </a>
                                    </td>
                                    <td>${ ui.format(supervisee.relationship.startDate) }</td>
                                    <td><a><i title="${ ui.message("providermanagement.supervisee.unassign") }"
                                              class="delete-supervisee icon-remove"
                                              data-supervisor-id="${account.person.personId}"
                                              data-supervisee-id="${ supervisee.person.personId }"
                                              data-supervisor-label="${ supervisee.person.personName }"
                                    ></i></a>
                                    </td>
                                </tr>
                                <% }
                                } %>
                                </tbody>
                            </table>
                        </tr>
                        </tbody>
                    </table>
                </div>
            </div>
        <% } %>

        <div class="panel panel-info">
            <div class="panel-heading">
                <h3 class="panel-title">${ui.message("providermanagement.activePatients")}</h3>
            </div>

            <div class="panel-body ">
                <table class="table table-condensed borderless">
                    <tbody>
                    <tr>
                        <div id="addPatientToList">
                            <% if (createAccount != true) { %>
                            <a href="">
                                <button id="add-patient-button">${ ui.message("providermanagement.addPatientToProvider") }
                                &nbsp; <i class="icon-plus"></i>
                                </button>
                            </a>
                            <% } %>
                        </div>
                    </tr>

                    <tr>
                        <table id="patients-list" width="100%" border="1" cellspacing="0" cellpadding="2">
                            <thead>
                            <tr>
                                <th><input type="checkbox" id="select-all-patients"/></th>
                                <th>${ ui.message("providermanagement.identifier") }</th>
                                <th>${ ui.message("coreapps.person.name") }</th>
                                <th>${ ui.message("providermanagement.startDate") }</th>
                                <th>&nbsp;</th>
                            </tr>
                            </thead>

                            <tbody>
                            <% if ((patientsList == null) ||
                                    (patientsList != null && patientsList.size() == 0)) { %>
                            <tr>
                                <td colspan="4">${ ui.message("coreapps.none") }</td>
                            </tr>
                            <% } %>
                            <% patientsList.each { row ->
                                if (row.relationship && row.relationship.endDate == null) {
                            %>
                            <tr id="patient-${ row.person.id }">
                                <td><input type="checkbox" class="transferPatient" data-patient-relationship="${row.relationship.id}"/></td>
                                <td>${ ui.format(row.identifier) }</td>
                                <td>${ ui.format(row.person.personName) }</td>
                                <td>${ ui.format(row.relationship.startDate) }</td>
                                <td><a><i class="delete-relationship icon-remove"
                                          data-provider-id="${account.person.personId}"
                                          data-relationship-type="${row.relationshipType.id}"
                                          data-patient-relationship="${row.relationship.id}"
                                ></i></a>
                                </td>
                            </tr>
                            <% }
                            } %>
                            </tbody>
                        </table>
                    </tr>

                    </tbody>
                </table>
                <button id="transfer-patients" disabled="disabled" class="disabled">Transfer</button>
            </div>
        </div>

        <div class="panel panel-info">
            <div class="panel-heading">
                <h3 class="panel-title">${ ui.message("providermanagement.historyList") }</h3>
            </div>
            <div class="panel-body">

                <table class="table table-condensed borderless">
                    <tbody>
                    <tr>
                        <table id="patients-history-list" width="100%" border="1" cellspacing="0" cellpadding="2">
                            <thead>
                            <tr>
                                <th>${ ui.message("providermanagement.identifier") }</th>
                                <th>${ ui.message("coreapps.person.name") }</th>
                                <th>${ ui.message("providermanagement.startDate") }</th>
                                <th>${ ui.message("providermanagement.stopDate") }</th>
                            </tr>
                            </thead>

                            <tbody>
                            <% if ((patientsList == null) ||
                                    (patientsList != null && patientsList.size() == 0)) { %>
                            <tr>
                                <td colspan="4">${ ui.message("coreapps.none") }</td>
                            </tr>
                            <% } %>
                            <% patientsList.each { row ->
                                if ((row.relationship == null) ||
                                        (row.relationship && row.relationship.endDate != null) ) {
                            %>
                            <tr id="patient-${ row.person.id }">
                                <td>${ ui.format(row.identifier) }</td>
                                <td>${ ui.format(row.person.personName) }</td>
                                <td>${ ui.format(row.relationship.startDate) }</td>
                                <td>${ ui.format(row.relationship.endDate) }</td>
                            </tr>
                            <% }
                            }%>
                            </tbody>
                        </table>
                    </tr>

                    </tbody>
                </table>

            </div>
        </div>
    </div>

</div>