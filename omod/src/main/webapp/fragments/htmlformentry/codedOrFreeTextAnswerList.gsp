<%
    config.require("formFieldName")

    ui.includeJavascript("uicommons", "angular.min.js")
    ui.includeJavascript("coreapps", "htmlformentry/codedOrFreeTextAnswerList.js")
    ui.includeJavascript("uicommons", "angular-ui/ui-bootstrap-tpls-0.6.0.min.js")

    ui.includeCss("coreapps", "htmlformentry/codedOrFreeTextAnswerList.css")
%>

<div id="${ config.id }" ng-controller="ListCtrl" ng-init="init('initialValue${ config.id }')">

    <script type="text/ng-template" id="searchResultTemplate.html">
        <a>
            <span ng-show="match.model.code">
                {{ match.model.code }}
            </span>
            <span ng-hide="match.model.code">
                ${ ui.message("coreapps.consult.nonCoded") }
            </span>
            <strong bind-html-unsafe="match.label | typeaheadHighlight:query"></strong>
            <span ng-hide="match.model.nameIsPreferred || match.model.nonCodedValue" class="preferred-name">
                <small>${ ui.message("coreapps.consult.synonymFor") }</small>
                {{ match.model.concept.preferredName }}
            </span>
        </a>
    </script>

    <p data-display-value="{{ displayValue() }}" data-value-from="#${ config.id } input[type=hidden]"
       <% if (config.containerClasses) { %>class="${ ui.escapeAttribute(config.containerClasses) }"<% } %>
    >
        <label>${ config.title }</label>
        <input type="text"
               <% if (config.placeholder) { %> placeholder="${ ui.escapeAttribute(config.placeholder) }" <% } %>
               autocomplete="off"
               style="min-width: 500px"
               class="manual-exit"
               ng-model="selection"
               typeahead="diagnosis as diagnosis.matchedName for diagnosis in searchDiagnoses(\$viewValue)"
               typeahead-on-select="selected(\$item)"
               typeahead-min-length="2"
               typeahead-wait-ms="20"
               typeahead-editable="false"
               typeahead-template-url="searchResultTemplate.html"
        />
    </p>

    <input type="hidden" name="${ config.formFieldName }" ng-value="toSubmit()"/>

    <div>
        <ul ng-show="answerList.length > 0">
            <li ng-repeat="item in answerList">
                <% if (config.betweenElements) { %>
                    <span ng-show="!\$first">
                        ${ config.betweenElements }
                    </span>
                <% } %>
                <div class="selected-answer">
                    <span ng-show="item.code" class="code">
                        {{ item.code }}
                    </span>
                    <span ng-hide="item.code" class="code">
                        ${ ui.message("coreapps.consult.nonCoded") }
                    </span>
                    <span class="name">
                        <strong>
                            {{ item.matchedName }}
                        </strong>
                        <span ng-hide="item.nameIsPreferred || item.nonCodedValue" class="preferred-name">
                            <small>${ ui.message("coreapps.consult.synonymFor") }</small>
                            {{ item.concept.preferredName }}
                        </span>
                    </span>
                    <span class="actions">
                        <a ng-show="!\$first" ng-click="moveUp(\$index)">
                            <i class="icon-arrow-up small"></i>
                        </a>
                        <a ng-show="!\$last" ng-click="moveDown(\$index)">
                            <i class="icon-arrow-down small"></i>
                        </a>
                        <a ng-click="remove(\$index)">
                            <i class="icon-remove small"></i>
                        </a>
                    </span>
                </div>
            </li>
        </ul>
    </div>

</div>

<script type="text/javascript">
    var initialValue${ config.id } = ${ config.initialValue };

    // manually bootstrap, in case there are multiple angular apps on a page
    angular.bootstrap('#${ config.id }', ['codedOrFreeTextAnswerList']);
</script>