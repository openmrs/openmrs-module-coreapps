<%
    ui.decorateWith("appui", "standardEmrPage")

    ui.includeCss("coreapps", "bootstrap.css")

    ui.includeJavascript("coreapps", "providermanagement/editProvider.js")

    def genderOptions = [ [label: ui.message("emr.gender.M"), value: 'M'],
                          [label: ui.message("emr.gender.F"), value: 'F'] ]

    def createAccount = (account.person.personId == null ? true : false);

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
        assignedSupervisor = it.person.personName
    }
    def editDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")
    def  formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");


%>

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
        var removePatientDialog = null;
        var addSupervisorDialog = null;
        var supervisors = null;

        jq('#patient-search').attr("size", "40");

        jq("#add-patient-button").click(function(event) {
            createAddPatientDialog();
            showAddPatientDialog();
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

        jq("select[name='providerRole']").on('change', function(event) {
            var roleId = this.value;
            getSupervisors(parseInt(roleId));
        });

        if ('${createAccount}' == 'true') {
            jq("input[name='givenName']").focus();
        } else {
            jq("#add-patient-button").focus();
        }

        var selectedProviderRole = jq("select[name='providerRole']").val();
        if ( (selectedProviderRole != null) && (parseInt(selectedProviderRole) > 0) ) {
            getSupervisors(parseInt(selectedProviderRole));
        }

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
        <input type="hidden" id="superviseeId" value="${account.person.personId}"/>
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
                            label: ui.message("providermanagement.endDate") + ": &nbsp;&nbsp;",
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
    <div class="col-sm-4">
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
                            formFieldName: "givenName",
                            initialValue: (account.givenName ?: '')
                    ])}

                    ${ ui.includeFragment("uicommons", "field/text", [
                            label: ui.message("coreapps.person.familyName"),
                            formFieldName: "familyName",
                            initialValue: (account.familyName ?: '')
                    ])}

                    ${ ui.includeFragment("uicommons", "field/radioButtons", [
                            label: ui.message("emr.gender"),
                            formFieldName: "gender",
                            initialValue: (account.gender ?: 'M'),
                            options: genderOptions
                    ])}

                    ${ ui.includeFragment("registrationapp", "field/personAddressWithHierarchy", [
                            id: "providerAddress",
                            initialValue: (account.person ? account.person.personAddress : null)
                    ])}


                    <div class="emr_providerDetails">
                        <% if (createAccount != true ) { %>
                        ${ ui.includeFragment("uicommons", "field/text", [
                                label: ui.message("providermanagement.identifier"),
                                formFieldName: "providerIdentifier",
                                initialValue: (account.provider ? account.provider.identifier: '')
                        ])}
                        <% } %>
                        <p>
                            ${ ui.includeFragment("uicommons", "field/dropDown", [
                                    label: ui.message("providermanagement.providerRole"),
                                    formFieldName: "providerRole",
                                    initialValue: (account.providerRole?.id ?: ''),
                                    options: providerRolesOptions,
                                    hideEmptyLabel: true,
                                    expanded: true
                            ])}
                        </p>

                    </div>

                    <div id="providerAttributesDiv" class="emr_providerDetails">
                            <% if (createAccount != true ) {
                                if ( account.provider.attributes !=null && account.provider.attributes.size() > 0 ) {
                                    account.provider.attributes.each { attribute ->

                                        if ( attribute.attributeType.datatypeClassname == 'org.openmrs.customdatatype.datatype.DateDatatype' ) {  %>
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
                            if (providerAttributeTypes != null && providerAttributeTypes.size() > 0) {
                                providerAttributeTypes.each { attributeType ->
                                    if ( attributeType.datatypeClassname == 'org.openmrs.customdatatype.datatype.DateDatatype' ) {  %>
                                            ${ ui.includeFragment("uicommons", "field/datetimepicker", [
                                                    id: "attributeTypeId_" + attributeType.providerAttributeTypeId,
                                                    formFieldName: "attributeTypeId_" + attributeType.providerAttributeTypeId,
                                                    label: attributeType.name,
                                                    useTime: false,
                                            ])}
                                     <% } else if ( attributeType.datatypeClassname == 'org.openmrs.module.coreapps.customdatatype.CodedConceptDatatype' ) { %>
                                            <label>${attributeType.name}</label>
                                            <select id="coded-data-types" name="attributeTypeId_${attributeType.providerAttributeTypeId}"></select>
                                            <script>
                                                var conceptId = '${attributeType.datatypeConfig}';
                                                getCodedConcepts(conceptId, 'attributeTypeId_${attributeType.providerAttributeTypeId}');
                                            </script>
                                <% } else { %>
                                        ${ ui.includeFragment("uicommons", "field/text", [
                                                label: attributeType.name,
                                                formFieldName: "attributeTypeId_" + attributeType.providerAttributeTypeId
                                        ])}
                                <% } %>
                            <% } %>
                            <% } %>
                        <% } %>
                    </div>

                    <div>
                        <input type="button" class="cancel" value="${ ui.message("emr.cancel") }" onclick="javascript:window.location='/${ contextPath }/coreapps/providermanagement/providerList.page'" />
                        <input type="submit" class="confirm" id="save-button" value="${ ui.message("emr.save") }"  />
                    </div>
                </form>

            </div>
        </div>
    </div>

    <div class="col-sm-8">

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
                                    if (supervisor.relationship.endDate == null) {  // display only active supervisors
                                %>
                                <tr id="patient-${ supervisor.person.personId }">
                                    <td>${ ui.format(supervisor.identifier) }</td>
                                    <td>${ ui.format(supervisor.person.personName) }</td>
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
                                if (row.relationship.endDate == null) {
                            %>
                            <tr id="patient-${ row.person.id }">
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
                                <th>${ ui.message("providermanagement.endDate") }</th>
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
                                if (row.relationship.endDate != null) {
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