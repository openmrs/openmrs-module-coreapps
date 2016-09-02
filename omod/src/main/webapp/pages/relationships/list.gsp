<%
    ui.decorateWith("appui", "standardEmrPage")

    ui.includeJavascript("uicommons", "angular.js")
    ui.includeJavascript("uicommons", "angular-resource.min.js")
    ui.includeJavascript("uicommons", "angular-common.js")
    ui.includeJavascript("uicommons", "angular-ui/ui-bootstrap-tpls-0.6.0.min.js")
    ui.includeJavascript("uicommons", "ngDialog/ngDialog.js")
    ui.includeJavascript("uicommons", "services/relationshipService.js")
    ui.includeJavascript("uicommons", "services/relationshipTypeService.js")
    ui.includeJavascript("uicommons", "services/personService.js")
    ui.includeJavascript("uicommons", "directives/select-person.js")
    ui.includeJavascript("coreapps", "relationships/relationships.js")

    ui.includeCss("uicommons", "ngDialog/ngDialog.min.css")
    ui.includeCss("coreapps", "relationships/list.css")
%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.escapeJs(ui.encodeHtmlContent(ui.format(patient.patient))) }" ,
            link: '${ui.pageLink("coreapps", "clinicianfacing/patient", [patientId: patient.patient.id])}'},
        { label: "${ ui.escapeJs(ui.message("coreapps.task.relationships.label")) }" }
    ]
</script>

${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient.patient ]) }

<h3>${ ui.message(app ? app.config.title.textValue : "coreapps.task.relationships.label") }</h3>

<div id="relationships-app" ng-controller="PersonRelationshipsCtrl" ng-init='init("${ patient.patient.uuid }", ${ ui.toJson(app?.config?.excludeRelationshipTypes) })'>

    <script type="text/ng-template" id="addDialogTemplate">
        <div class="dialog-header">
            <h3><%= ui.message("coreapps.relationships.add.header", "{{ ngDialogData.otherLabel }}") %></h3>
        </div>
        <div class="dialog-content">
            <div>
                <label>
                    <%= ui.message("coreapps.relationships.add.choose", "{{ ngDialogData.otherLabel }}") %>
                </label>
                <select-person ng-model="otherPerson" id="select-other-person" exclude-person="${ patient.patient.uuid }" />
            </div>
            <div class="add-confirm-spacer">
                <div ng-show="otherPerson" >
                    <h3>${ ui.message("coreapps.relationships.add.confirm") }</h3>
                    <p>{{ ngDialogData.thisLabel }}: ${ ui.encodeHtmlContent(ui.format(patient.patient)) } ${ ui.message("coreapps.relationships.add.thisPatient") }</p>
                    <p>{{ ngDialogData.otherLabel }}: {{ otherPerson.display }}</p>
                </div>
            </div>
            <div>
                <button class="confirm right" ng-disabled="!otherPerson" ng-click="confirm(otherPerson)">${ ui.message("uicommons.save") }</button>
                <button class="cancel" ng-click="closeThisDialog()">${ ui.message("uicommons.cancel") }</button>
            </div>
        </div>
    </script>

    <script type="text/ng-template" id="deleteDialogTemplate">
        <div class="dialog-header">
            <h3>${ ui.message("coreapps.relationships.delete.header") }</h3>
        </div>
        <div class="dialog-content">
            <form>
                ${ ui.message("coreapps.relationships.delete.title") }
                <p>
                    <label>{{ relType(ngDialogData.relationship).aIsToB }}</label>
                    {{ ngDialogData.relationship.personA.display }}
                </p>
                <p>
                    <label>{{ relType(ngDialogData.relationship).bIsToA }}</label>
                    {{ ngDialogData.relationship.personB.display }}
                </p>
                <button  class="confirm right" ng-click="confirm()">${ ui.message("uicommons.delete") }</button>
                <button class="cancel" ng-click="closeThisDialog()">${ ui.message("uicommons.cancel") }</button>
            </form>
        </div>
    </script>

    <div ng-repeat="relType in relationshipTypes">
        <h6>
            {{ relType.aIsToB }}
            <a ng-click="showAddDialog(relType, 'A')">
                <i class="icon-plus-sign edit-action"></i>
            </a>
        </h6>
        <span ng-repeat="rel in relationshipsByType(relType, 'A')" class="relationship">
            {{ rel.personA.display }}
            <a ng-click="goToPerson(rel.personA)">
                <i class="icon-user small"></i>
            </a>
            <a ng-click="showDeleteDialog(rel)">
                <i class="icon-remove delete-action"></i>
            </a>
        </span>
        <span ng-show="relType.aIsToB == relType.bIsToA" ng-repeat="rel in relationshipsByType(relType, 'B')" class="relationship">
            {{ rel.personB.display }}
            <a ng-click="goToPerson(rel.personB)">
                <i class="icon-user small"></i>
            </a>
            <a ng-click="showDeleteDialog(rel)">
                <i class="icon-remove delete-action"></i>
            </a>
        </span>

        <span ng-hide="relType.aIsToB == relType.bIsToA">
            <h6>
                {{ relType.bIsToA }}
                <a ng-click="showAddDialog(relType, 'B')">
                    <i class="icon-plus-sign edit-action"></i>
                </a>
            </h6>
            <span ng-repeat="rel in relationshipsByType(relType, 'B')" class="relationship">
                {{ rel.personB.display }}
                <a ng-click="goToPerson(rel.personB)">
                    <i class="icon-user small"></i>
                </a>
                <a ng-click="showDeleteDialog(rel)">
                    <i class="icon-remove delete-action"></i>
                </a>
            </span>
        </span>
    </div>

</div>

<script type="text/javascript">
    // manually bootstrap angular app, in case there are multiple angular apps on a page
    angular.bootstrap('#relationships-app', ['relationships']);
</script>