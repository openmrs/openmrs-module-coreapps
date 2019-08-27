<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeJavascript("uicommons", "angular.min.js")
    ui.includeJavascript("uicommons", "angular-ui/ui-bootstrap-tpls-0.11.2.min.js")
    ui.includeJavascript("uicommons", "angular-resource.min.js")
    ui.includeJavascript("uicommons", "angular-common.js")
    ui.includeJavascript("uicommons", "ngDialog/ngDialog.js")
    ui.includeJavascript("uicommons", "ngDialog/ngDialog.js")
    ui.includeJavascript("uicommons", "services/conceptSearchService.js")
    ui.includeJavascript("uicommons", "directives/coded-or-free-text-answer.js")
    ui.includeFragment("coreapps", "patientHeader", [patient: patient])
    ui.includeJavascript("coreapps", "conditionlist/lib/restangular.min.js")
    ui.includeJavascript("coreapps", "conditionlist/restful-services/restful-service.js");
    ui.includeJavascript("coreapps", "conditionlist/models/model.module.js")
    ui.includeJavascript("coreapps", "conditionlist/models/concept.model.js")
    ui.includeJavascript("coreapps", "conditionlist/models/condition.model.js")
    ui.includeJavascript("coreapps", "conditionlist/emr.messages.js")
    ui.includeJavascript("coreapps", "conditionlist/common.functions.js")
    ui.includeJavascript("coreapps", "conditionlist/controllers/editcondition.controller.js")
    ui.includeCss("uicommons", "ngDialog/ngDialog.min.css")
    ui.includeCss("coreapps", "conditionlist/conditions.css")
%>
<script type="text/javascript">
    var breadcrumbs = [
        {icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm'},
        {
            label: "${ ui.escapeJs(ui.encodeHtmlContent(ui.format(patient.familyName))) }, ${ ui.escapeJs(ui.encodeHtmlContent(ui.format(patient.givenName))) }",
            link: '${ui.pageLink("coreapps", "clinicianfacing/patient", [patientId: patient.id])}'
        },
        { 
            label: "${ ui.message("coreapps.conditionui.conditions") }",
            link: '${ui.pageLink("coreapps/conditionlist", "manageConditions", [patientId: patient.id, returnUrl: returnUrl])}'
        },
        {label: "${ui.message('coreapps.conditionui.editCondition',"")}"}
    ];
</script>

<div id="condition" ng-app="conditionApp" ng-controller="ConditionController">
    <h2 class="inline">${ui.message('coreapps.conditionui.editCondition',"")}</h2><br/>

    <div class="horizontal">    
        <ul id="concept-and-date">
            <li class="group">
            <label>${ui.message('coreapps.conditionui.condition')} </label>
                <coded-or-free-text-answer id="conceptId" class="concept"
                                           concept-classes="8d4918b0-c2cc-11de-8d13-0010c6dffd0f,8d492954-c2cc-11de-8d13-0010c6dffd0f,8d492b2a-c2cc-11de-8d13-0010c6dffd0f,8d491a9a-c2cc-11de-8d13-0010c6dffd0f"
                                           ng-model="concept"/>
            </li>
            <li class="group">
            <br/> <br/> <label> ${ui.message('coreapps.conditionui.onsetdate')} </label>
                ${ui.includeFragment("uicommons", "field/datetimepicker", [
                        formFieldName: "conditionStartDate",
                        label        : "",
                        useTime      : false,
                        endDate    : new Date(),
                ])}
            </li>
            &nbsp;&nbsp;
            <li id="status" class="group">
            <label>${ui.message('coreapps.stopDate.label')} </label>
                ${ui.includeFragment("uicommons", "field/datetimepicker", [
                        formFieldName: "conditionEndDate",
                        label        : "",
                        useTime      : false,
                        endDate    : new Date(),
                ])}
            </li>
            <br/> <br/>
        </ul>
    </div>

    <div id="status" class="horizontal">
        <p>
            <input type="radio" id="status-1" class="condition-status" value="${ui.message('coreapps.conditionui.active.label')}" name="status" ng-model="condition.status" ng-change="showEndDate()"/>
            <label for="status-1">${ui.message('coreapps.conditionui.active.label')}</label>
        </p>
        <p>
            <input type="radio" id="status-2" class="condition-status" value="${ui.message('coreapps.conditionui.inactive.label')}" name="status" ng-model="condition.status" ng-change="showEndDate()"/>
            <label for="status-2">${ui.message('coreapps.conditionui.inactive.label')}</label>
        </p>
    </div>
    <br/>
    <div id="actions">
        <input type="submit" id="addConditionBtn" class="confirm right"
               value="${ui.message("coreapps.save")}" ng-click="validateCondition()"/>
        <button class="cancel"
                onclick="location.href='${ui.pageLink("coreapps/conditionlist", "manageConditions", [patientId: patient.uuid, returnUrl: returnUrl])}'">
            ${ ui.message("coreapps.cancel") }
        </button>
    </div>
</div>