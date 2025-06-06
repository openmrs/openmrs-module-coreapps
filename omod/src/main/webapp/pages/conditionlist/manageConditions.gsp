<%
    ui.decorateWith("appui", "standardEmrPage")

    ui.includeJavascript("uicommons", "angular.min.js")
    ui.includeJavascript("uicommons", "angular-ui/ui-bootstrap-tpls-0.11.2.min.js")
    ui.includeJavascript("uicommons", "angular-resource.min.js")
    ui.includeJavascript("uicommons", "angular-common.js")
    ui.includeJavascript("uicommons", "underscore-min.js")
    ui.includeJavascript("uicommons", "moment-with-locales.min.js")

    ui.includeJavascript("coreapps", "conditionlist/lib/polyfills.js")
    ui.includeJavascript("coreapps", "conditionlist/restful-services/restful-service.js");
    ui.includeJavascript("coreapps", "conditionlist/emr.messages.js")
    ui.includeJavascript("coreapps", "conditionlist/common.functions.js")
    ui.includeJavascript("coreapps", "conditionlist/controllers/conditions.controller.js")

    ui.includeCss("uicommons", "ngDialog/ngDialog.min.css")
    ui.includeCss("coreapps", "conditionlist/conditions.css")
%>

${ui.includeFragment("coreapps", "patientHeader", [patient: patient])}

<script type="text/javascript">
    var breadcrumbs = [
        {icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm'},
        {
            label: "${ ui.encodeJavaScript(ui.encodeHtmlContent(ui.format(patient.familyName))) }, ${ ui.encodeJavaScript(ui.encodeHtmlContent(ui.format(patient.givenName))) }",
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

<h2>${ui.message("coreapps.conditionui.manageConditions")}</h2>

<div id="condition" ng-app="conditionsApp" ng-controller="ConditionsController"
     ng-init="conditions = getConditions('${patient.uuid}'); config = { dateFormat: '${ ui.getJSDateFormat() }', locale: '${ ui.getLocale().getLanguage() }'};">
    <div id="tabs">
        <ul>
            <li>
                <a href="#ACTIVE" class="tabs-height">
                    ${ui.message('coreapps.conditionui.active.label')}
                </a>
            </li>
            <li>
                <a href="#INACTIVE" class="tabs-height">
                    ${ui.message('coreapps.conditionui.inactive.label')}
                </a>
            </li>
        </ul>

        <span ng-repeat="tab in tabs">
            <div id="{{ tab }}">
                <table class="conditions-table">
                    <thead>
                    <tr>
                        <th>${ui.message("coreapps.conditionui.condition")}</th>
                        <th>${ui.message("coreapps.conditionui.onsetdate")}</th>
                        <th ng-if="tab === 'INACTIVE'">${ui.message("coreapps.stopDate.label")}</th>
                        <th ng-if="'${hasModifyConditionsPrivilege}'">${ui.message("coreapps.actions")}</th>
                    </tr>
                    </thead>
                    <tbody>
                    <tr ng-if="conditions.length === 0">
                        <td colspan="6" align="center">
                            ${ui.message("coreapps.conditionui.noKnownConditions")}
                        </td>
                    </tr>
                    <tr class="clickable-tr" ng-repeat="condition in conditions track by condition.uuid" ng-if="condition.clinicalStatus === tab">
                        <td>{{formatCondition(condition)}}</td>
                        <td>{{formatDate(condition.onsetDate)}}</td>
                        <td ng-if="tab === 'INACTIVE'">{{formatDate(condition.endDate)}}</td>
                        <td ng-if="'${hasModifyConditionsPrivilege}'">
                            <i class="icon-pencil edit-action" title="${ui.message("coreapps.conditionui.editCondition")}"
                               ng-click="redirectToEditCondition('${ ui.pageLink("coreapps/conditionlist", "manageCondition", [patientId: patient.uuid, returnUrl: returnUrl]) }', condition)"></i>
                            <i class="icon-remove delete-action" title="${ui.message("coreapps.delete")}"
                               ng-click="conditionConfirmation(condition)" ng-if="condition.voided === false"></i>
                            <button style="background-color: #cccccc;border: none; color: black; padding: 5px;font-size: 10px; margin: 2px 2px; border-radius: 4px;"
                                  ng-click="activateCondition(condition)" ng-if="condition.clinicalStatus === 'INACTIVE'">${ ui.message("coreapps.conditionui.setActive") }</button>
                            <button style="background-color: #cccccc;border: none; color: black; padding: 5px;font-size: 10px; margin: 2px 2px; border-radius: 4px;"
                                 ng-click="deactivateCondition(condition)" ng-if="condition.clinicalStatus === 'ACTIVE'">${ ui.message("coreapps.conditionui.setInactive") }</button>
                        </td>
                    </tr>
                    </tbody>
                </table>
            </div>
        </span>
    </div>

    
    <div id="remove-condition-dialog" class="dialog" style="display: none; position: absolute; left: 35%; top:30%;">
        <div class="dialog-header">
            <h3>${ ui.message("coreapps.conditionui.removeCondition") }</h3>
        </div>
        <div class="dialog-content">
            <ul>
                <li class="info">
                    <span id="remove-condition-message">${ ui.message("coreapps.conditionui.removeCondition.message")}</span>
                </li>
            </ul>
                <button class="confirm right" type="submit" ng-click="removeCondition()">${ ui.message("general.yes") }</button>
                <button class="cancel" ng-click="cancelDeletion()">${ ui.message("general.no") }</button>
        </div>
    </div>

    <div class="actions">
        <button class="cancel"
                onclick="location.href = '${ ui.encodeHtml(returnUrl) }'">${ui.message("coreapps.return")}</button>
        <button id="conditionui-addNewCondition" class="confirm right"
                onclick="location.href = '${ ui.pageLink("coreapps/conditionlist", "manageCondition", [patientId: patient.uuid, returnUrl: returnUrl]) }'">
            ${ui.message("coreapps.conditionui.addNewCondition")}
        </button>
    </div>
</div>
