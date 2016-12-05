<%
    def fragmentProvider = "coreapps"
    def placeholder =  ui.message("coreapps.dataManagement.searchCoded")
    def configId =  "diagnosis-search"
    def maxResults = 10
    def itemLabelFunction = "nonCodedDiagnosisLabelFunction"

    ui.includeJavascript("uicommons", "knockout-2.2.1.js")
    ui.includeJavascript("coreapps", "fragments/datamanagement/findDiagnosis.js")
    ui.includeJavascript("coreapps", "fragments/datamanagement/codeDiagnosisDialog.js")
    ui.includeCss("coreapps", "diagnoses/encounterDiagnoses.css")
    ui.includeCss("mirebalais", "consult.css", -200)
%>
<script type="text/javascript">
    var formatTemplate = null;
    var instructionsTemplate = null;

    var patientDashboardLink = null;

    var formatAutosuggestion = function(item) {
        return item ? formatTemplate({ item: item }) : "";
    };

    var viewModel = null;

    jq(function() {

        jq('#diagnosis-search').focus();

        jq('#consult-note').submit(function() {

            var newDiagnosis = jq("#diagnosis");
            var submitValue = JSON.parse(newDiagnosis.val());

            var valid = viewModel.isValid();
            if (valid) {
                viewModel.startSubmitting();
            }
            return false;
        });
    } );
</script>

${ ui.includeFragment("coreapps", "datamanagement/diagnosisAutocompleteTemplate") }

<div id="code-diagnosis-dialog" class="dialog-diagnoses" style="display: none">
    <div class="dialog-header">
        <h3>${ ui.message("coreapps.dataManagement.codeDiagnosis.title") }</h3>
    </div>

    <div class="dialog-content">
        <form id="consult-note" method="post">
            <input type="hidden" id="hiddenPatientId" value=""/>
            <input type="hidden" id="hiddenNonCodedObsId" value=""/>
            <input type="hidden" id="hiddenCodedConceptId" value=""/>
            <ul>
                <li class="info">
                    <span id="instructions"></span>
                </li>
            </ul>
            <div id="contentForm">
                <div id="entry-fields">
                    <p>
                        <label for="diagnosis-search">${ ui.message("emr.consult.addDiagnosis") }</label>
                        <input type="hidden" id="${ configId }-value" value="0"/>
                        <input id="diagnosis-search" type="text" placeholder="${ ui.message("emr.consult.addDiagnosis.placeholder") }" data-bind="autocomplete: searchTerm, itemFormatter: formatAutosuggestion"/>
                    </p>
                </div>
                <div id="display-diagnoses">
                    <h3>${ ui.message("emr.consult.primaryDiagnosis") }</h3>
                    <div data-bind="visible: primaryDiagnoses().length == 0">
                        ${ ui.message("emr.consult.primaryDiagnosis.notChosen") }
                    </div>
                    <ul data-bind="template: { name: 'selected-diagnosis-template', foreach: primaryDiagnoses }"></ul>
                    <br/>

                    <h3>${ ui.message("emr.consult.secondaryDiagnoses") }</h3>
                    <div data-bind="visible: secondaryDiagnoses().length == 0">
                        ${ ui.message("emr.consult.secondaryDiagnoses.notChosen") }
                    </div>
                    <ul data-bind="template: { name: 'selected-diagnosis-template', foreach: secondaryDiagnoses }"></ul>
                </div>

            </div>
            <button id="confirmCodedDiagnosisId" class="confirm right" data-bind="css: { disabled: !canSubmit() }, enable: canSubmit()">${ui.message("coreapps.confirm")}</button>
            <button class="cancel">${ui.message("coreapps.cancel")}</button>
        </form>
    </div>
</div>
