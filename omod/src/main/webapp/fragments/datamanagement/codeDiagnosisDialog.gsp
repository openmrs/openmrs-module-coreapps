<%
    def fragmentProvider = "coreapps"
    def placeholder =  ui.message("coreapps.dataManagement.searchCoded")
    def configId =  "diagnosis-search"
    def maxResults = 10
    def itemLabelFunction = "nonCodedDiagnosisLabelFunction"


    ui.includeJavascript("coreapps", "diagnoses/diagnoses.js")
    ui.includeJavascript("coreapps", "fragments/datamanagement/codeDiagnosisDialog.js")
    ui.includeCss("coreapps", "diagnoses/encounterDiagnoses.css")
    ui.includeCss("mirebalais", "consult.css", -200)
%>
<script type="text/javascript">
    var formatTemplate;
    var instructionsTemplate = '${ ui.escapeJs(ui.message("coreapps.dataManagement.replaceNonCoded")) }';

    var patientDashboardLink = '${ui.pageLink("coreapps", "patientdashboard/patientDashboard")}';

    jq(function() {
        var xhr=null;//used to track active ajax requests
        formatTemplate = _.template(jq('#autocomplete-render-item').html());

        jq('#${ configId }').focus();

        function nonCodedDiagnosisLabelFunction(item) {
            var diagnosisObject = formatTemplate({ item: item });
            // return item ? item.concept.preferredName : "";
            return diagnosisObject;
        }

        jq('#${ configId }').autocomplete({
            source: function(request, response) {
                var ajaxUrl = '${ ui.actionLink(fragmentProvider, "diagnoses", "search")}';
                jq('#${ configId }-value').val(0);
                if(xhr){
                    xhr.abort();
                    xhr = null;
                }
                xhr= jq.ajax({
                    url: ajaxUrl,
                    dataType: 'json',
                    data: { term: request.term , maxResults: 10 } ,
                    success: function (data) {
                        if (data.length == 0){
                            console.log("no diagnoses matches for: " + request.term);
                        }
                        response(data);
                    }
                }).complete(function(){
                            xhr = null;
                        }).error(function(){
                            xhr = null;
                            console.log("error on searching for diagnoses");
                        });
            },
            autoFocus: false,
            minLength: 2,
            delay: 300,
            response: function(event, ui){
                var query = event.target.value.toLowerCase();
                var items = ui.content;
                for (var i = items.length - 1; i >= 0; --i) {
                    items[i] = diagnoses.CodedOrFreeTextConceptAnswer(items[i]);
                }
            },
            select: function(event, ui) {
                console.log(ui.item.matchedName + " has been selected");
                var selectedDiagnosis = ui.item;
                if(selectedDiagnosis.conceptId != null ){
                    jq('#hiddenCodedConceptId').val(selectedDiagnosis.conceptId);
                }
                return false;
            },
            focus: function(event, ui){
                jq('#${ configId }').val(ui.item.matchedName);
                return false;
            }
        });

        jq('#${ configId }').data('autocomplete')._renderItem = function(ul, item) {
            return jq('<li>')
                    .data('item.autocomplete', item)
                    .append('<a>' + ((item.patientId == 0) ? item.label : (${ itemLabelFunction }(item))) + '</a>')
                    .appendTo(ul);
        };

        jq('#${ configId }').data('autocomplete')._renderMenu = function(ul, items){
            var self= this;
            var fieldId =  '${ configId }';
            if (items.length == 0 ) {
                jq('#${ configId }-value').val(0);
            }
            jq.each( items , function(i, item) {
                self._renderItem(ul, item);
            });
        };
    });
</script>

${ ui.includeFragment("coreapps", "datamanagement/diagnosisAutocompleteTemplate") }

<div id="code-diagnosis-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <h3>${ ui.message("coreapps.dataManagement.codeDiagnosis.title") }</h3>
    </div>

    <div class="dialog-content">
        <input type="hidden" id="hiddenPatientId" value=""/>
        <input type="hidden" id="hiddenNonCodedObsId" value=""/>
        <input type="hidden" id="hiddenCodedConceptId" value=""/>
        <ul>
            <li class="info">
                <span id="instructions"></span>
            </li>
            <br>
            <li class="info">
                <input type="hidden" id="${ configId }-value" value="0"/>
                <input type="text" class="field-display" id="${ configId }" placeholder="${ placeholder }" value="" size="45"/>
            </li>
        </ul>

        <button id="confirmCodedDiagnosisId" class="confirm right">${ui.message("coreapps.confirm")}</button>
        <button class="cancel">${ui.message("coreapps.cancel")}</button>
    </div>
</div>
