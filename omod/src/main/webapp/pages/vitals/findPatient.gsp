<%
    if (sessionContext.authenticated && !sessionContext.currentProvider) {
        throw new IllegalStateException("Logged-in user is not a Provider")
    }

    ui.decorateWith("appui", "standardEmrPage")
%>

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("referenceapplication.app.capturevitals.title") }", link: "${ ui.pageLink("coreapps", "vitals/findPatient") }" }
    ];
</script>

<h1>
    ${ ui.message("referenceapplication.app.capturevitals.title") }
</h1>

${ ui.includeFragment("uicommons", "widget/findPatient", [
        targetPageProvider: "coreapps",
        targetPage: "vitals/patient"
]) }