<%

    ui.decorateWith("appui", "standardEmrPage")
    ui.includeCss("registrationapp", "findPatient.css")

    ui.includeCss("coreapps", "bootstrap.css")
    ui.includeCss("coreapps", "providermanagement/providermanagement.css")

    ui.includeJavascript("coreapps", "providermanagement/editProvider.js")

    def baseUrl = ui.pageLink("coreapps", "providermanagement/findPatient")
    def afterSelectedUrl = '/coreapps/providermanagement/editProvider?personId={{personId}}'

%>
${ ui.includeFragment("uicommons", "validationMessages")}

<script type="text/javascript">

    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("providermanagement.providerList")}", link: '${ui.pageLink("coreapps", "providermanagement/providerList")}' },
        { label: "${ ui.message("registrationapp.findPatient") }", link: "${ ui.pageLink("coreapps", "providermanagement/findPatient") }" }
    ];

    jq(function() {
        jq('#patient-search').focus();
    });

    var selectPatientHandler = {
        handle: function (row, widgetData) {
            location.href = emr.pageLink("coreapps", "providermanagement/editProvider", { personId: row.patientId });
        }
    }

</script>


${ ui.message("coreapps.provider.search") }

<div id="register-provider-div" class="search-div">
    <a href="${ ui.pageLink("coreapps", "providermanagement/editProvider", ) }">
        <button id="create-provider-button">${ ui.message("Provider.create") }</button>
    </a>
</div>

<div class="container">
    <div id="search-patient-div" class="search-div">
        ${ ui.includeFragment("coreapps", "patientsearch/patientSearchWidget",
                [ afterSelectedUrl: afterSelectedUrl,
                  rowSelectionHandler: "selectPatientHandler",
                  initialSearchFromParameter: "search",
                  showLastViewedPatients: 'false' ])}
    </div>

</div>