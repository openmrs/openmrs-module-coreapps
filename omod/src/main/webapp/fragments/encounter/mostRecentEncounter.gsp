<div class="info-section most-recent-encounter">
    <div class="info-header">
        <i class="${ app.icon }"></i>
        <h3>${ ui.message(app.label) }</h3>
    </div>
    <div class="info-body">

        <div id="most-recent-encounter-container" class="in collapse">
            ${ ui.message(encounter ? "uicommons.loading.placeholder" : "coreapps.clinicianfacing.noneRecorded") }
        </div>

        <% if (encounter) { %>
            <script type="text/javascript">
                emr.getFragmentActionWithCallback("htmlformentryui", "htmlform/viewEncounterWithHtmlForm", "getAsHtml", { encounterId: ${encounter.id} }, function(result) {
                    jq('#most-recent-encounter-container').html(result.html);
                    jq('.collapse .title').text('${ ui.escapeJs(ui.message(app.config.get('encounterDateLabel').textValue , ui.formatDatetimePretty(encounter.encounterDatetime))) } ');
                });
            </script>
        <% } %>
    </div>
</div>