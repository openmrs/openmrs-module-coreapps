<% /* This is an underscore template */ %>
<script type="text/template" id="autocomplete-render-item">
    <span class="code">
        {{ if (item.code) { }}
        {{- item.code }}
        {{ } else if (item.concept) { }}
        ${ ui.message("coreapps.consult.codedButNoCode") }
        {{ } else { }}
        ${ ui.message("coreapps.consult.nonCoded") }
        {{ } }}
    </span>
    <strong class="matched-name">
        {{- item.matchedName }}
    </strong>
    {{ if (item.preferredName) { }}
    <span class="preferred-name">
        <small>${ ui.message("coreapps.consult.synonymFor") }</small>
        {{- item.concept.preferredName }}
    </span>
    {{ } }}
</script>