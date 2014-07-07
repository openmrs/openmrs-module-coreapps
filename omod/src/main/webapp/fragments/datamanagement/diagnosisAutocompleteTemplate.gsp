<% /* This is a knockout template, so we can use data-binds inside */ %>
<script type="text/html" id="selected-diagnosis-template">
    <li>
        <div class="diagnosis" data-bind="css: { primary: primary }">
            <span class="code">
                <span data-bind="if: diagnosis().code, text: diagnosis().code"></span>
                <span data-bind="if: !diagnosis().code && diagnosis().concept">
                    ${ ui.message("emr.consult.codedButNoCode") }
                </span>
                <span data-bind="if: !diagnosis().code && !diagnosis().concept">
                    ${ ui.message("emr.consult.nonCoded") }
                </span>
            </span>
            <strong class="matched-name" data-bind="text: diagnosis().matchedName"></strong>
            <span class="preferred-name" data-bind="if: diagnosis().preferredName">
                <small>${ ui.message("emr.consult.synonymFor") }</small>
                <span data-bind="text: diagnosis().concept.preferredName"></span>
            </span>
            <div class="actions">
                <label>
                    <input type="checkbox" data-bind="checked: primary"/>
                    ${ ui.message("emr.Diagnosis.Order.PRIMARY") }
                </label>
                <label>
                    <input type="checkbox" data-bind="checked: confirmed"/>
                    ${ ui.message("emr.Diagnosis.Certainty.CONFIRMED") }
                </label>
            </div>
        </div>
        <i data-bind="click: \$parent.removeDiagnosis" tabindex="-1" class="icon-remove delete-item"></i>
        <input type="hidden" id="diagnosis" name="diagnosis" data-bind="value: valueToSubmit()">
    </li>
</script>
<% /* This is an underscore template */ %>
<script type="text/template" id="autocomplete-render-template">
    <span class="code">
        {{ if (item.code) { }}
        {{- item.code }}
        {{ } else if (item.concept) { }}
        ${ ui.message("emr.consult.codedButNoCode") }
        {{ } else { }}
        ${ ui.message("emr.consult.nonCoded") }
        {{ } }}
    </span>
    <strong class="matched-name">
        {{- item.matchedName }}
    </strong>
    {{ if (item.preferredName) { }}
    <span class="preferred-name">
        <small>${ ui.message("emr.consult.synonymFor") }</small>
        {{- item.concept.preferredName }}
    </span>
    {{ } }}
</script>