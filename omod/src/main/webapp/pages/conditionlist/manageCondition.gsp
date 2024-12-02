<%
    ui.decorateWith("appui", "standardEmrPage")

    ui.includeJavascript("uicommons", "angular.min.js")
    ui.includeJavascript("uicommons", "angular-ui/ui-bootstrap-tpls-0.11.2.min.js")
    ui.includeJavascript("uicommons", "angular-resource.min.js")
    ui.includeJavascript("uicommons", "angular-common.js")
    ui.includeJavascript("uicommons", "angular-sanitize.min.js")
    ui.includeJavascript("uicommons", "ngDialog/ngDialog.js")
    ui.includeJavascript("uicommons", "services/conceptSearchService.js")
    ui.includeJavascript("uicommons", "directives/coded-or-free-text-answer.js")
    ui.includeJavascript("uicommons", "underscore-min.js")

    ui.includeJavascript("coreapps", "conditionlist/restful-services/restful-service.js");
    ui.includeJavascript("coreapps", "conditionlist/emr.messages.js")
    ui.includeJavascript("coreapps", "conditionlist/common.functions.js")
    ui.includeJavascript("coreapps", "conditionlist/controllers/condition.controller.js")

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
        {
            label: "${ ui.message("coreapps.conditionui.conditions") }",
            link: '${ui.pageLink("coreapps/conditionlist", "manageConditions", [patientId: patient.id, returnUrl: returnUrl])}'
        },
        {label: "${editingCondition ? ui.message("coreapps.conditionui.editCondition") : ui.message('coreapps.conditionui.addNewCondition')}"}
    ];

    window.messages = window.messages || {
        "coreapps.conditionui.addNewCondition": "${ui.message("coreapps.conditionui.addNewCondition")}",
        "coreapps.conditionui.editCondition": "${ui.message("coreapps.conditionui.editCondition")}"
    }
</script>

<div id="condition" ng-app="conditionApp" ng-controller="ConditionController">
    <h2>{{title}}</h2>

    <div class="horizontal">
        <ul id="concept-and-date">
            <li class="group">
                <label>${ui.message('coreapps.conditionui.condition')}</label>
                <coded-or-free-text-answer id="conceptId" class="concept"
                                           concept-classes="${conditionListClasses}"
                                           ng-model="concept"/>
            </li>
            <li class="group">
            <br/> <br/>
                ${ui.includeFragment("uicommons", "field/datetimepicker", [
                        formFieldName    : "conditionStartDate",
                        datePickerFormat : "yyyy-MM-dd",
                        label            : ui.message('coreapps.conditionui.onsetdate'),
                        useTime          : false,
                        endDate          : new Date(),
                        clearButton      : true
                ])}
            </li>
            &nbsp;&nbsp;
            <li class="group">
                ${ui.includeFragment("uicommons", "field/datetimepicker", [
                        formFieldName    : "conditionEndDate",
                        datePickerFormat : "yyyy-MM-dd",
                        label            : ui.message('coreapps.stopDate.label'),
                        useTime          : false,
                        endDate          : new Date(),
                        clearButton      : true
                ])}
            </li>
            <br/><br/>
            <div id="status" class="horizontal">
                <p>
                    <input type="radio" id="status-1" class="condition-status" value="ACTIVE" name="status" ng-model="condition.clinicalStatus"  ng-change="showEndDate()"/>
                    <label for="status-1">${ui.message('coreapps.conditionui.active.label')}</label>
                </p>
                <p>
                    <input type="radio" id="status-2" class="condition-status" value="INACTIVE" name="status" ng-model="condition.clinicalStatus"  ng-change="showEndDate()"/>
                    <label for="status-2">${ui.message('coreapps.conditionui.inactive.label')}</label>
                </p>
            </div>

        </ul>
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
