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

    def editDateFormat = new java.text.SimpleDateFormat("yyyy-MM-dd")
    def  formatter = new java.text.SimpleDateFormat("yyyy-MM-dd");


%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("Provider List")}", link: '${ui.pageLink("coreapps", "providermanagement/providerList")}' },
        { label: "${ ui.message("Edit Provider")}" }

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

        jq("select[name='providerRole']").on('change', function(event) {
            var roleId = this.value;
        });

        if ('${createAccount}' == 'true') {
            jq("input[name='givenName']").focus();
        } else {
            jq("#add-patient-button").focus();
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

<div id="remove-patient-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <h3>${ ui.message("End Relationship") }</h3>
    </div>
    <div class="dialog-content">
        <input type="hidden" id="providerId" value="${account.person.personId}"/>
        <input type="hidden" id="patientId" value=""/>

        <span>${ ui.message("Are you sure you want to unassign Patient from Provider?") }</span>

        <div class="panel-body ">
            <fieldset>
                <p>
                    ${ ui.includeFragment("uicommons", "field/datetimepicker", [
                            id: "relationshipEndDate",
                            formFieldName: "relationshipEndDate",
                            label:"End Date: &nbsp;&nbsp;",
                            defaultDate: new Date(),
                            endDate: editDateFormat.format(new Date()),
                            useTime: false,
                    ])}
                </p>
            </fieldset>
        </div>
        <button id="remove-patient-button" class="confirm right">${ ui.message("End relationship") }</button>
        <button class="cancel">${ ui.message("Cancel") }</button>
    </div>
</div>

<div id="add-patient-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <h3>${ ui.message("Create Relationship") }</h3>
    </div>
    <div class="dialog-content">
        <input type="hidden" id="providerId" value="${account.person.personId}"/>
        <input type="hidden" id="patientId" value=""/>

        <span>${ ui.message("Assign Patient to Provider") }</span>

        <div class="panel-body ">
            <fieldset>
                <p>
                    ${ ui.message("Find Patient:") }
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
                            label:"Start Date: &nbsp;&nbsp;",
                            defaultDate: new Date(),
                            endDate: editDateFormat.format(new Date()),
                            useTime: false,
                    ])}
                </p>
                <br><br>
                <p>
                    ${ ui.includeFragment("uicommons", "field/dropDown", [
                            label: ui.message("Relationship Type"),
                            formFieldName: "relationshipType",
                            classes: ["required"],
                            options: relationshipTypesOptions,
                            hideEmptyLabel: true,
                            expanded: true
                    ])}
                </p>
            </fieldset>

        </div>
        <button class="confirm right">${ ui.message("Assign") }</button>
        <button class="cancel">${ ui.message("Cancel") }</button>
    </div>
</div>

<h3>${ (createAccount) ? ui.message("Create Provider") : ui.message("Edit Provider") }</h3>

<div class="row">
    <div class="col-sm-4">
        <div class="panel panel-info">
            <div class="panel-heading">
                <h3 class="panel-title">${ui.message("Provider")}</h3>
            </div>
            <div class="panel-body ">

                <form method="post" id="accountForm" autocomplete="off">
                    <!-- dummy fields so that Chrome doesn't autocomplete the real username/password fields with the users own password -->
                    <input style="display:none" type="text" name="wrong-username-from-autocomplete"/>
                    <input style="display:none" type="password" name="wrong-username-from-autocomplete"/>

                    ${ ui.includeFragment("uicommons", "field/text", [
                            label: ui.message("Given Name"),
                            formFieldName: "givenName",
                            initialValue: (account.givenName ?: '')
                    ])}

                    ${ ui.includeFragment("uicommons", "field/text", [
                            label: ui.message("Family Name"),
                            formFieldName: "familyName",
                            initialValue: (account.familyName ?: '')
                    ])}

                    ${ ui.includeFragment("uicommons", "field/radioButtons", [
                            label: ui.message("emr.gender"),
                            formFieldName: "gender",
                            initialValue: (account.gender ?: 'M'),
                            options: genderOptions
                    ])}

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
                        <% } else { %>
                        ${ ui.includeFragment("uicommons", "field/text", [
                                label: attribute.attributeType.name,
                                formFieldName: "providerAttributeId_" + attribute.providerAttributeId,
                                initialValue: attribute.valueReference
                        ])}
                        <% } %>
                        <% } %>
                        <%}
                            if (providerAttributeTypes != null && providerAttributeTypes.size() > 0) {
                                providerAttributeTypes.each { attributeType ->
                                    if ( attributeType.datatypeClassname == 'org.openmrs.customdatatype.datatype.DateDatatype' ) {  %>
                        ${ ui.includeFragment("uicommons", "field/datetimepicker", [
                                id: "attributeTypeId_" + attributeType.providerAttributeTypeId,
                                formFieldName: "attributeTypeId_" + attributeType.providerAttributeTypeId,
                                label: attributeType.name,
                                useTime: false,
                        ])}
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
                    <div class="emr_providerDetails">
                        ${ ui.includeFragment("uicommons", "field/text", [
                                label: ui.message("Identifier"),
                                formFieldName: "providerIdentifier",
                                initialValue: (account.provider ? account.provider.identifier: '')
                        ])}
                        <p>
                            ${ ui.includeFragment("uicommons", "field/dropDown", [
                                    label: ui.message("Provider Role"),
                                    formFieldName: "providerRole",
                                    initialValue: (account.providerRole?.id ?: ''),
                                    options: providerRolesOptions,
                                    hideEmptyLabel: true,
                                    expanded: true
                            ])}
                        </p>

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
                <h3 class="panel-title">${ui.message("Active Patients")}</h3>
            </div>

            <div class="panel-body ">
                <table class="table table-condensed borderless">
                    <tbody>
                    <tr>
                        <div id="addPatientToList">
                            <% if (createAccount != true) { %>
                            <a href="">
                                <button id="add-patient-button">${ ui.message("Add Patient") }
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
                                <th>${ ui.message("Identifier") }</th>
                                <th>${ ui.message("Name") }</th>
                                <th>${ ui.message("Start Date") }</th>
                                <th>&nbsp;</th>
                            </tr>
                            </thead>

                            <tbody>
                            <% if ((patientsList == null) ||
                                    (patientsList != null && patientsList.size() == 0)) { %>
                            <tr>
                                <td colspan="4">${ ui.message("None") }</td>
                            </tr>
                            <% } %>
                            <% patientsList.each { row ->

                            %>
                            <tr id="patient-${ row.patient.patientId }">
                                <td>${ ui.format(row.patient.patientIdentifier.identifier) }</td>
                                <td>${ ui.format(row.patient.personName) }</td>
                                <td>${ ui.format(row.relationship.startDate) }</td>
                                <td><a><i class="delete-relationship icon-remove"
                                          data-provider-id="${account.person.personId}"
                                          data-relationship-type="${row.relationshipType.id}"
                                          data-patient-relationship="${row.relationship.id}"
                                ></i></a>
                                </td>
                            </tr>
                            <% } %>
                            </tbody>
                        </table>
                    </tr>

                    </tbody>
                </table>
            </div>
        </div>

        <div class="panel panel-info">
            <div class="panel-heading">
                <h3 class="panel-title">History List</h3>
            </div>
            <div class="panel-body">

                <table class="table table-condensed borderless">
                    <tbody>
                    <tr>
                        <table id="patients-history-list" width="100%" border="1" cellspacing="0" cellpadding="2">
                            <thead>
                            <tr>
                                <th>${ ui.message("Identifier") }</th>
                                <th>${ ui.message("Name") }</th>
                                <th>${ ui.message("Start Date") }</th>
                                <th>${ ui.message("End Date") }</th>
                            </tr>
                            </thead>

                            <tbody>
                            <% if ((patientsHistoryList == null) ||
                                    (patientsHistoryList != null && patientsHistoryList.size() == 0)) { %>
                            <tr>
                                <td colspan="4">${ ui.message("None") }</td>
                            </tr>
                            <% } %>
                            <% patientsHistoryList.each { row ->

                            %>
                            <tr id="patient-${ row.patient.patientId }">
                                <td>${ ui.format(row.patient.patientIdentifier.identifier) }</td>
                                <td>${ ui.format(row.patient.personName) }</td>
                                <td>${ ui.format(row.relationship.startDate) }</td>
                                <td>${ ui.format(row.relationship.endDate) }</td>
                            </tr>
                            <% } %>
                            </tbody>
                        </table>
                    </tr>

                    </tbody>
                </table>

            </div>
        </div>
    </div>

</div>