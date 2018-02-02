<%

    ui.includeFragment("appui", "standardEmrIncludes")

    ui.includeJavascript("uicommons", "angular.min.js")
    ui.includeJavascript("uicommons", "angular-ui/ui-bootstrap-tpls-0.11.2.min.js")
    ui.includeJavascript("uicommons", "angular-resource.min.js")
    ui.includeJavascript("uicommons", "angular-common.js")

    ui.includeJavascript("coreapps", "conditionlist/lib/restangular.min.js")
    ui.includeJavascript("coreapps", "conditionlist/restful-services/restful-service.js");
    ui.includeJavascript("coreapps", "conditionlist/models/model.module.js")
    ui.includeJavascript("coreapps", "conditionlist/models/concept.model.js")
    ui.includeJavascript("coreapps", "conditionlist/models/condition.model.js")
    ui.includeJavascript("coreapps", "conditionlist/emr.messages.js")
    ui.includeJavascript("coreapps", "conditionlist/common.functions.js")
    ui.includeJavascript("coreapps", "conditionlist/controllers/manageconditions.controller.js")

    ui.includeJavascript("coreapps", "conditionlist/common.functions.js")

    ui.includeCss("coreapps", "conditionlist/conditions.css")
%>

<div class="info-section conditions" ng-app="manageConditionsApp" ng-controller="ManageConditionsController"
     ng-init="conditionHistoryList = getConditions('${patient.patient.uuid}')">
    <div class="info-header">
        <i class="icon-diagnosis"></i>

        <h3>${ui.message('coreapps.conditionui.conditions').toUpperCase()}</h3>
        <i class="icon-pencil edit-action right" title="${ui.message("coreapps.edit")}"
           onclick="location.href = '${ui.pageLink("coreapps/conditionlist", "manageConditions", [patientId: patient.patient.uuid])}'"></i>
    </div>

    <div class="info-body">
        <ul ng-repeat="conditionHistory in conditionHistoryList">
            <li class="conditionStatus" ng-init="condition = conditionHistory.conditions[0]"
                ng-show="condition.status === 'ACTIVE'">
                <span ng-style="strikeThrough(condition.voided)">{{condition.concept.name}}</span>
                <i class="icon-remove delete-action" title="${ui.message("coreapps.delete")}"
                   ng-click="removeCondition(condition)" ng-if="condition.voided===false && '${hasModifyConditionsPrivilege}'"></i>
                <i class="icon-undo delete-action" title="${ui.message("conditionui.undo")}"
                   ng-click="undoCondition(condition)" ng-if="condition.voided===true && '${hasModifyConditionsPrivilege}'"></i>
            </li>
        </ul>
    </div>
</div>
