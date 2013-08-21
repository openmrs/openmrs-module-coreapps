<% /* This is an underscore template */ %>
<script type="text/template" id="autocomplete-render-item">
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