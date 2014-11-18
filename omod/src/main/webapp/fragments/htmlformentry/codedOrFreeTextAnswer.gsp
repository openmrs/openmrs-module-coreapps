<%
    config.require("formFieldName")

    ui.includeJavascript("uicommons", "angular.min.js")
    ui.includeJavascript("uicommons", "angular-resource.min.js")
    ui.includeJavascript("uicommons", "angular-ui/ui-bootstrap-tpls-0.6.0.min.js")
    ui.includeJavascript("uicommons", "angular-common.js")
    ui.includeJavascript("uicommons", "services/conceptSearchService.js")
    ui.includeJavascript("uicommons", "directives/coded-or-free-text-answer.js")
    ui.includeJavascript("coreapps", "htmlformentry/codedOrFreeTextAnswer.js")

    ui.includeCss("coreapps", "htmlformentry/codedOrFreeTextAnswerList.css")
%>

<div id="${ config.id }" ng-controller="AnswerCtrl" ng-init="init('initialValue${ config.id }')">
    <p<% if (config.containerClasses) { %>class="${ ui.escapeAttribute(config.containerClasses) }"<% } %>>
        <label>${ config.title }</label>
        <!-- TODO: placeholder -->
        <coded-or-free-text-answer ng-model="answer" concept-classes="Diagnosis,Finding,Symptom/Finding" />
    </p>
    <input type="hidden" name="${ config.formFieldName }" ng-value="toSubmit()"/>
</div>

<script type="text/javascript">
    var initialValue${ config.id } = ${ config.initialValue };

    window.messages = window.messages || {};
    window.messages['uicommons.synonymFor'] = '${ ui.escapeJs(ui.message("uicommons.synonymFor")) }';

    // manually bootstrap, in case there are multiple angular apps on a page
    angular.bootstrap('#${ config.id }', ['codedOrFreeTextAnswer']);
</script>