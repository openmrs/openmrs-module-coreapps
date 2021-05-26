<%
    ui.includeFragment("appui", "standardEmrIncludes")

    ui.includeJavascript("uicommons", "angular.min.js")
    ui.includeJavascript("uicommons", "angular-ui/ui-bootstrap-tpls-0.11.2.min.js")
    ui.includeJavascript("uicommons", "angular-resource.min.js")
    ui.includeJavascript("uicommons", "angular-common.js")

    ui.includeJavascript("coreapps", "conditionlist/restful-services/restful-service.js");
    ui.includeJavascript("coreapps", "conditionlist/emr.messages.js")
    ui.includeJavascript("coreapps", "conditionlist/common.functions.js")
    ui.includeJavascript("coreapps", "conditionlist/controllers/conditions.controller.js")

    ui.includeJavascript("coreapps", "conditionlist/common.functions.js")

    ui.includeCss("coreapps", "conditionlist/conditions.css")
%>

<div class="info-section conditions" ng-app="conditionsApp" ng-controller="ConditionsController"
     ng-init="conditions = getConditions('${patient.patient.uuid}')">
    <div class="info-header">
        <i class="icon-diagnosis"></i>

        <h3>${ui.message('coreapps.conditionui.conditions').toUpperCase()}</h3>
        <i class="icon-pencil edit-action right" title="${ui.message("coreapps.edit")}" ng-if="'${hasModifyConditionsPrivilege}'"
           onclick="location.href = '${ui.pageLink("coreapps/conditionlist", "manageConditions", [patientId: patient.patient.uuid])}'"></i>
    </div>

    <div class="info-body">
        <ul ng-repeat="condition in conditions track by condition.uuid" ng-if="conditions.length > 0 && condition.status === 'ACTIVE' && condition.voided === false">
            <li class="conditionStatus">{{condition.concept.name}}</li>
        </ul>
       <p ng-if="conditions.length === 0">
           ${ui.message("coreapps.none")}
       </p>
    </div>

    <div id="remove-condition-dialog" class="dialog" style="display: none; position: absolute; left: 35%; top:30%; z-index: 1000 !important;">
        <div class="dialog-header">
            <h3>${ ui.message("coreapps.conditionui.removeCondition") }</h3>
        </div>
        <div class="dialog-content">
            <ul>
                <li class="info">
                    <span id="removeConditionMessage">${ ui.message("coreapps.conditionui.removeCondition.message")}</span>
                </li>
            </ul>
                <button class="confirm right" type="submit" ng-click="removeCondition()">${ ui.message("general.yes") }</button>
                <button class="cancel" ng-click="cancelDeletion()">${ ui.message("general.no") }</button>
        </div>
    </div>
</div>
