<%
    // although called "patientDashboard" this is actually the patient visits screen, and clinicianfacing/patient is the main patient dashboard

    ui.decorateWith("appui", "standardEmrPage")

	ui.includeCss("coreapps", "patientdashboard/patientDashboard.css")

    ui.includeJavascript("uicommons", "bootstrap-collapse.js")
    ui.includeJavascript("uicommons", "bootstrap-transition.js")


    def tabs = [
        [ id: "visits", label: ui.message("coreapps.patientDashBoard.visits"), provider: "coreapps", fragment: "patientdashboard/visits" ],
        patientTabs.collect{
            [id: it.id, label: ui.message(it.label), provider: it.extensionParams.provider, fragment: it.extensionParams.fragment]
        }
    ]

    tabs = tabs.flatten()

%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.escapeJs(ui.format(patient.patient)) }" ,
            link: '${ ui.urlBind("/" + contextPath + dashboardUrl, [ patientId: patient.patient.id ] ) }'}
    ];

    jq(function(){
        jq(".tabs").tabs();

        // make sure we reload the page if the location is changes; this custom event is emitted by by the location selector in the header
        jq(document).on('sessionLocationChanged', function() {
            window.location.reload();
        });
    });

    var patient = { id: ${ patient.id } };
</script>


<% if (includeFragments) {
    includeFragments.each { %>
        ${ ui.includeFragment(it.extensionParams.provider, it.extensionParams.fragment, it.extensionParams.fragmentConfig) }
<%  }
} %>

${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient.patient, activeVisit: activeVisit, appContextModel: appContextModel ]) }
<div class="actions dropdown">
    <span class="dropdown-name"><i class="icon-cog"></i>${ ui.message("coreapps.actions") }<i class="icon-sort-down"></i></span>
    <ul>
        <% overallActions.each {
            def url = it.url
            url = it.url(contextPath, appContextModel, ui.thisUrl())
        %>
            <li>
                <a href="${ url }"><i class="${ it.icon }"></i>${ ui.message(it.label) }</a>
            </li>
        <% } %>
    </ul>
</div>

<div class="tabs" xmlns="http://www.w3.org/1999/html">
    <div class="dashboard-container">

        <ul>
            <% tabs.each { %>
            <li>
                <a href="#${ it.id }">
                    ${ it.label }
                </a>
            </li>
            <% } %>

        </ul>

        <% tabs.each { %>
        <div id="${it.id}">
            ${ ui.includeFragment(it.provider, it.fragment, [ patient: patient ]) }
        </div>
        <% } %>

    </div>
</div>