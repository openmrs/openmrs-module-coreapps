<%
    def elementId = ui.randomId('most-recent-encounter-container') + "-"

    def editIcon = editIcon ?: "icon-pencil"
    def editProvider = editProvider ?: "htmlformentryui"
    def editFragment = editFragment ?: "htmlform/editHtmlFormWithStandardUi"

    def createIcon = createIcon ?: "icon-pencil"
    def createProvider = createProvider ?: "htmlformentryui"
    def createFragment = createFragment ?: "htmlform/enterHtmlFormWithStandardUi"

%>
<div id="${ app.id ? app.id.replaceAll('\\.','-') : '' }" class="info-section most-recent-encounter">
    <div class="info-header">
        <i class="${ app.icon }"></i>
        <h3>${ ui.message(app.label).toUpperCase() }</h3>

        <% if (encounter && editable) { %>
            <i class="${editIcon} edit-action right" title="${ ui.message("coreapps.edit") }"
               onclick="location.href='${ui.pageLink(editProvider, editFragment,
               [patientId: patient.id, encounterId: encounter.id, definitionUiResource: definitionUiResource, returnUrl: returnUrl ])}';"></i>
        <% } else if (creatable) { %>
            <i class="${createIcon} edit-action right" title="${ ui.message("coreapps.add") }"
               onclick="location.href='${ui.pageLink(createProvider, createFragment,
                   [patientId: patient.id, definitionUiResource: definitionUiResource, returnUrl: returnUrl ])}';"></i>
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
                                var displayEncounterDate = jq('#${elementId}').find('#displayEncounterDate').val();
                                if (displayEncounterDate != "false") {
                                    // insert the header in the special "most-recent-encounter-title" if it exists, otherwise to the first title element)
                                    if (jq('#${elementId} #most-recent-encounter-title').length) {
                                        jq('#${elementId} #most-recent-encounter-title').text('${ ui.escapeJs(ui.message(app.config.get('encounterDateLabel').textValue , ui.formatDatetimePretty(encounter.encounterDatetime))) } ').show();
                                    }
                                    else {
                                        jq('#${elementId} .title:first').text('${ ui.escapeJs(ui.message(app.config.get('encounterDateLabel').textValue , ui.formatDatetimePretty(encounter.encounterDatetime))) } ');
                                    }
                                }
                            });
            </script>
        <% } %>
    </div>
</div>