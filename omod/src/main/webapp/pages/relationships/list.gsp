<%
    ui.decorateWith("appui", "standardEmrPage")

    ui.includeJavascript("uicommons", "angular.js")
    ui.includeJavascript("uicommons", "angular-resource.min.js")
    ui.includeJavascript("uicommons", "angular-ui/ui-bootstrap-tpls-0.6.0.min.js")
    ui.includeJavascript("uicommons", "services/relationshipService.js")
    ui.includeJavascript("uicommons", "services/relationshipTypeService.js")
    ui.includeJavascript("uicommons", "services/personService.js")
    ui.includeJavascript("uicommons", "angular-app.js")
    ui.includeJavascript("uicommons", "directives/select-person.js")
    ui.includeJavascript("coreapps", "relationships/relationships.js")
%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.format(patient.patient.familyName) }, ${ ui.format(patient.patient.givenName) }" ,
            link: '${ui.pageLink("coreapps", "clinicianfacing/patient", [patientId: patient.patient.id])}'},
        { label: "${ ui.escapeJs(ui.message("coreapps.task.relationships.label")) }" }
    ]
</script>

<style type="text/css">
    .relationship {
        position: relative;
        border: 1px solid #DADADA;
        padding: 5px 25px 5px 5px;
    }
    .relationship i {
        position: absolute;
        right: 0px;
        cursor: pointer;
    }
    .dialog {
        position: absolute;
        z-index: 999;
    }
    .add-confirm-spacer {
        min-height: 110px;
    }
</style>

${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient.patient ]) }

<h2>${ ui.message("coreapps.task.relationships.label") }</h2>

<div id="relationships-app" ng-controller="PersonRelationshipsCtrl" ng-init="init('${ patient.patient.uuid }')">

    <div ng-show="addDialogMode" class="dialog" id="add-relationship-dialog">
        <div class="dialog-header">
            <%= ui.message("coreapps.relationships.add.header", "{{ addDialogOtherLabel }}") %>
        </div>
        <div class="dialog-content">
            <div>
                <label>
                    <%= ui.message("coreapps.relationships.add.choose", "{{ addDialogOtherLabel }}") %>
                </label>
                <select-person ng-model="otherPerson" exclude-person="${ patient.patient.uuid }" />
            </div>
            <div class="add-confirm-spacer">
                <div ng-show="otherPerson" >
                    <h3>${ ui.message("coreapps.relationships.add.confirm") }</h3>
                    <p>{{ addDialogThisLabel }}: ${ ui.format(patient.patient) } ${ ui.message("coreapps.relationships.add.thisPatient") }</p>
                    <p>{{ addDialogOtherLabel }}: {{ otherPerson.display }}</p>
                </div>
            </div>
            <div>
                <button class="confirm right" ng-disabled="!otherPerson" ng-click="doAddRelationship()">${ ui.message("uicommons.save") }</button>
                <button class="cancel" ng-click="cancelAddRelationship()">${ ui.message("uicommons.cancel") }</button>
            </div>
        </div>
    </div>

    <div ng-show="relationshipToDelete" class="dialog" id="delete-relationship-dialog">
        <div class="dialog-header">${ ui.message("coreapps.relationships.delete.header") }</div>
        <div class="dialog-content">
            <form>
                ${ ui.message("coreapps.relationships.delete.title") }
                <p>
                    <label>{{ relType(relationshipToDelete).aIsToB }}</label>
                    {{ relationshipToDelete.personA.display }}
                </p>
                <p>
                    <label>{{ relType(relationshipToDelete).bIsToA }}</label>
                    {{ relationshipToDelete.personB.display }}
                </p>
                <button class="confirm right" ng-click="doDeleteRelationship(relationshipToDelete)">${ ui.message("uicommons.delete") }</button>
                <button class="cancel" ng-click="cancelDeleteRelationship(relationshipToDelete)">${ ui.message("uicommons.cancel") }</button>
            </form>
        </div>
    </div>

    <form id="existing-relationships">
        <div ng-repeat="relType in relationshipTypes">
            <p>
                <label>{{ relType.aIsToB }}</label>
                <span ng-repeat="rel in relationshipsByType(relType, 'A')" class="relationship">
                    {{ rel.personA.display }}
                    <a ng-click="showDeleteDialog(rel)">
                        <i class="small icon-remove"></i>
                    </a>
                </span>
                <span ng-show="relType.aIsToB == relType.bIsToA" ng-repeat="rel in relationshipsByType(relType, 'B')" class="relationship">
                    {{ rel.personB.display }}
                    <a ng-click="showDeleteDialog(rel)">
                        <i class="small icon-remove"></i>
                    </a>
                </span>
                <a ng-click="showAddDialog(relType, 'A')">
                    <i class="small icon-plus-sign"></i>
                </a>
            </p>

            <p ng-hide="relType.aIsToB == relType.bIsToA">
                <label>{{ relType.bIsToA }}</label>
                <span ng-repeat="rel in relationshipsByType(relType, 'B')" class="relationship">
                    {{ rel.personB.display }}
                    <a ng-click="showDeleteDialog(rel)">
                        <i class="small icon-remove"></i>
                    </a>
                </span>
                <a ng-click="showAddDialog(relType, 'B')">
                    <i class="small icon-plus-sign"></i>
                </a>
            </p>
        </div>
    </form>

</div>

<script type="text/javascript">
    // manually bootstrap angular app, in case there are multiple angular apps on a page
    angular.bootstrap('#relationships-app', ['relationships']);
</script>