<%
    ui.decorateWith("appui", "standardEmrPage")

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

${ui.includeFragment("coreapps", "patientHeader", [patient: patient])}

<script type="text/javascript">
    var breadcrumbs = [
        {icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm'},
        {
            label: "${ ui.escapeJs(ui.encodeHtmlContent(ui.format(patient.familyName))) }, ${ ui.escapeJs(ui.encodeHtmlContent(ui.format(patient.givenName))) }",
            link: '${ui.pageLink("coreapps", "clinicianfacing/patient", [patientId: patient.id])}'
        },
        {label: "${ ui.message("coreapps.conditionui.conditions") }"}
    ];
</script>

<script>
    jQuery.noConflict();
    jQuery(function () {
        jQuery("#tabs").tabs();
    });
</script>

<h2>${ui.message("coreapps.conditionui.manageConditions")}<td></h2>

<div id="condition" ng-app="manageConditionsApp" ng-controller="ManageConditionsController"
     ng-init="conditionHistoryList = getConditions('${patient.uuid}')">
    <div id="tabs">
        <ul>
            <li>
                <a href="#ACTIVE" class="tabs-height">
                    ${ui.message('coreapps.conditionui.activeConditions')}
                </a>
            </li>
            <li>
                <a href="#INACTIVE" class="tabs-height">
                    ${ui.message('coreapps.conditionui.inactiveConditions')}
                </a>
            </li>
        </ul>

        <span ng-repeat="tab in tabs">
            <div id="{{tab}}">
                <table class="conditions-table">
                    <thead>
                    <tr>
                        <th>${ui.message("coreapps.conditionui.condition")}</th>
                        <th>${ui.message("coreapps.conditionui.status")}</th>
                        <th>${ui.message("coreapps.conditionui.onsetdate")}</th>
                        <th ng-if="'${hasModifyConditionsPrivilege}'">${ui.message("coreapps.actions")}</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr ng-if="conditions.size() == 0">
                        <td colspan="6" align="center">
                            ${ui.message("coreapps.conditionui.noKnownConditions")}
                        </td>
                    </tr>
                    <tbody ng-repeat="conditionHistory in conditionHistoryList">
                    <tr class="clickable-tr" ng-init="condition = conditionHistory.conditions[0]"
                        ng-show="condition.status===tab">
                        <td ng-style="strikeThrough(condition.voided)">{{condition.concept.name}}</td>
                        <td ng-style="strikeThrough(condition.voided)">{{condition.status}}</td>
                        <td ng-style="strikeThrough(condition.voided)">{{formatDate(condition.onSetDate)}}</td>
                        <td ng-if="'${hasModifyConditionsPrivilege}'">
                            <i class="icon-plus-sign edit-action" title="${ui.message("coreapps.conditionui.active")}"
                               ng-click="activateCondition(condition)" ng-if="condition.status==='INACTIVE' && condition.voided===false"></i>
                            <i class="icon-minus-sign edit-action" title="${ui.message("coreapps.conditionui.inactive")}"
                               ng-click="deactivateCondition(condition)" ng-if="condition.status==='ACTIVE' && condition.voided===false"></i>
                            <i class="icon-folder-close edit-action" title="${ui.message("coreapps.conditionui.historyof")}"
                               ng-click="moveToHistoryCondition(condition)" ng-if="condition.status!=='HISTORY_OF' && condition.voided===false"></i>
                            <i class="icon-remove delete-action" title="${ui.message("coreapps.coreapps.delete")}"
                               ng-click="removeCondition(condition)" ng-if="condition.voided===false"></i>
                            <i class="icon-undo delete-action" title="${ui.message("coreapps.conditionui.undo")}"
                               ng-click="undoCondition(condition)" ng-if="condition.voided===true"></i>
                        </td>
                    </tr>
                    </tbody>
                </tbody>
                </table>
            </div>
        </span>
    </div>

    <div class="actions">
        <button class="cancel"
                onclick="location.href = '${ ui.encodeHtml(returnUrl) }'">${ui.message("coreapps.cancel")}</button>
        <button id="conditionui-addNewCondition" class="confirm right"
                onclick="location.href = '${ ui.pageLink("coreapps/conditionlist", "addCondition", [patientId: patient.uuid, returnUrl: returnUrl]) }'">
            ${ui.message("coreapps.conditionui.addNewCondition")}
        </button>
    </div>
</div>