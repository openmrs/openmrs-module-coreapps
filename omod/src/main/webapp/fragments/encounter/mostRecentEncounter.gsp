<%
    def elementId = ui.randomId('most-recent-encounter-container') + "-"
    def editIcon = editIcon ?: "icon-pencil"
    def editProvider = editProvider ?: "htmlformentryui"
    def editFragment = editFragment ?: "htmlform/editHtmlFormWithStandardUi"
%>
<div class="info-section most-recent-encounter">
    <div class="info-header">
        <i class="${ app.icon }"></i>
        <h3>${ ui.message(app.label).toUpperCase() }</h3>

        <% if (encounter && editable) { %>
            <i class="${editIcon} edit-action right" title="${ ui.message("coreapps.edit") }"
               onclick="location.href='${ui.pageLink(editProvider, editFragment,
               [patientId: encounter.patient.id, encounterId: encounter.id, definitionUiResource: definitionUiResource, returnUrl: returnUrl ])}';"></i>
        <% } %>

    </div>
    <div class="info-body">

        <!-- TODO: technically we should assign a random number here, not 0, if no encounter id present -->
        <div id="${elementId}" class="in">
            ${ ui.message(encounter ? "uicommons.loading.placeholder" : "coreapps.clinicianfacing.noneRecorded") }
        </div>

        <% if (encounter) { %>
            <script type="text/javascript">
                emr.getFragmentActionWithCallback("htmlformentryui", "htmlform/viewEncounterWithHtmlForm", "getAsHtml",
                        { encounterId: ${encounter.id}, definitionUiResource: '${ definitionUiResource }' },
                            function(result) {
                                jq('#${elementId}').html(result.html);
                                var displayEncounterDate = jq('#displayEncounterDate').val();
                                if (displayEncounterDate != "false") {
                                    // TODO: this method to the display the title seems a little brittle--replaces the first element in the form with class 'title'
                                    jq('#${elementId} .title:first').text('${ ui.escapeJs(ui.message(app.config.get('encounterDateLabel').textValue , ui.formatDatetimePretty(encounter.encounterDatetime))) } ');
                                }
                            });
            </script>
        <% } %>
    </div>
</div>