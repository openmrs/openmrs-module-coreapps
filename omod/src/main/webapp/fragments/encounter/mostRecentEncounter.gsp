<div class="info-section most-recent-encounter">
    <div class="info-header">
        <i class="${ app.icon }"></i>
        <h3>${ ui.message(app.label).toUpperCase() }</h3>
    </div>
    <div class="info-body">

        <!-- TODO: technically we should assign a random number here, not 0, if no encounter id present -->
        <div id="most-recent-encounter-container-${encounter?.id ?: 0}" class="in">
            ${ ui.message(encounter ? "uicommons.loading.placeholder" : "coreapps.clinicianfacing.noneRecorded") }
        </div>

        <% if (encounter) { %>
            <script type="text/javascript">
                emr.getFragmentActionWithCallback("htmlformentryui", "htmlform/viewEncounterWithHtmlForm", "getAsHtml",
                        { encounterId: ${encounter.id}, definitionUiResource: '${ definitionUiResource }' },
                            function(result) {
                                jq('#most-recent-encounter-container-${encounter?.id ?: 0}').html(result.html);
                                // TODO: this method to the display the title seems a little brittle--replaces the first element in the form with class 'title'
                                jq('#most-recent-encounter-container-${encounter?.id ?: 0} .title:first').text('${ ui.escapeJs(ui.message(app.config.get('encounterDateLabel').textValue , ui.formatDatetimePretty(encounter.encounterDatetime))) } ');
                            });
            </script>
        <% } %>
    </div>
</div>