<%
    ui.decorateWith("appui", "standardEmrPage", [ title: ui.message("coreapps.mergePatientsLong") ])
    ui.includeCss("coreapps", "datamanagement/mergePatients.css")
    ui.includeJavascript("coreapps", "datamanagement/mergePatients.js")

    def id = ""
    def primaryId = ""
    def fullName = ""

    if (patient1 != null) {
         id = patient1.patient.id
         primaryId = patient1.primaryIdentifiers.collect{ it.identifier }.join(',')
         fullName =  ui.encodeHtmlContent(ui.format(patient1.patient))
    }

%>

<script type="text/javascript">
    jq(function() {

        unknownPatient("${id}", "${primaryId}",  "${fullName}", ${isUnknownPatient});

        function unknownPatient(id, primaryId, fullName, isUnknownPatient) {
            if( id>0 ) {
                jq("#patient1").val(id);
                jq("#patient1-text").val(primaryId);
                jq("#full-name-field").text(fullName);
                jq("#patient1-text").attr("disabled","disabled");
                jq("#patient1-text").addClass('disabled');
                jq("#patient2-text").focus();
            }
        }
    });
    var isUnknownPatient = ${isUnknownPatient};
</script>

<script type="text/javascript">

    <% if (breadcrumbs) { %>
        var breadcrumbs = ${ breadcrumbs };
    <% } else { %>
        var breadcrumbs = [
            { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
            { label: "${ ui.message('coreapps.app.dataManagement.label')}", link: '/' + OPENMRS_CONTEXT_PATH + '/coreapps/datamanagement/dataManagement.page' },
            { label: "${ ui.message('coreapps.mergePatientsLong')}" }
        ];
    <% } %>

</script>

<h3>${ ui.message("coreapps.mergePatients.selectTwo") }</h3>
<div id="merge-patient-container">
    <h4>${ ui.message("coreapps.mergePatients.enterIds") }</h4>
    <form>
        <input type="hidden" name="app" value="coreapps.mergePatients"/>
        <input type= "hidden" name= "isUnknownPatient" value= "${isUnknownPatient}"/>
        
        <p>
            ${ ui.includeFragment("coreapps", "findPatientById",[
                    label: ui.message("coreapps.mergePatients.chooseFirstLabel"),
                    hiddenFieldName: "patient1",
                    textFieldName: "patient1-text",
                    callBack: "checkConfirmButton",
                    fullNameField: "full-name-field"
            ] )}
        </p>
        
        <p>
            ${ ui.includeFragment("coreapps", "findPatientById",[
                    label: ui.message("coreapps.mergePatients.chooseSecondLabel"),
                    hiddenFieldName: "patient2",
                    textFieldName: "patient2-text",
                    callBack: "checkConfirmButton"
            ] )}
        </p>
        
        <p class="right">
            <input class="cancel" type="button" id="cancel-button" value="${ ui.message("emr.cancel") }"/>

            <input class="confirm disabled" type="submit" disabled="disabled" id="confirm-button" value="${ ui.message("emr.continue") }"/>
        </p>

    </form>
    <label>${ ui.message("coreapps.mergePatients.dynamicallyFindPatients") }</label>
    ${ ui.includeFragment("coreapps", "mergepatients/patientSearchAndSelectWidget",
            [ showLastViewedPatients: false ])}
    <br>
</div>
