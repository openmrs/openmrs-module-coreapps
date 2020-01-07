<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeCss("coreapps", "clinicianfacing/patient.css")
    ui.includeJavascript("coreapps", "custom/deletePatient.js")
    ui.includeJavascript("appui", "jquery-3.4.1.min.js")
%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.escapeJs(ui.encodeHtmlContent(ui.format(patient.patient))) }" ,
        link: '${ ui.urlBind("/" + contextPath + baseDashboardUrl, [ patientId: patient.patient.id ] ) }'}
    ];

    // add on breadcrumb if it has been defined in messages.properties
    <% if (ui.message(dashboard + ".breadcrumb") != dashboard + ".breadcrumb") { %>
        breadcrumbs.push({ label: "${ ui.message(dashboard + ".breadcrumb") }"})
    <% } %>

    jq(function(){
        jq(".tabs").tabs();

        // make sure we reload the page if the location is changes; this custom event is emitted by by the location selector in the header
        jq(document).on('sessionLocationChanged', function() {
            window.location.reload();
        });
    });

    var patient = { id: ${ patient.id } };
</script>

<% if(includeFragments){

    includeFragments.each {
        // create a base map from the fragmentConfig if it exists, otherwise just create an empty map
        def configs = [:];
        if(it.extensionParams.fragmentConfig != null){
            configs = it.extensionParams.fragmentConfig;
        }

        configs.patient = patient;   // add the patient to the map %>
        ${ui.includeFragment(it.extensionParams.provider, it.extensionParams.fragment, configs)}
    <%}
} %>

${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient.patient, activeVisit: activeVisit, appContextModel: appContextModel ]) }


<div class="clear"></div>
    <div class="dashboard clear row">
        <!-- only show the title div if a title has been defined in the messages.properties -->
        <% if (ui.message(dashboard + ".custom.title") != dashboard + ".custom.title") { %>
        <div class="title">
            <h3>${ ui.message(dashboard + ".custom.title") }</h3>
        </div>
        <% } %>
        <div class="col-12 col-sm-12 col-md-12 col-lg-9">
            <div class="row">
                <div class="col-12 col-sm-12 col-md-12 col-lg-6">
                    <% if (firstColumnFragments) {
                        firstColumnFragments.each {
                            // create a base map from the fragmentConfig if it exists, otherwise just create an empty map
                            def configs = [:];
                            if(it.extensionParams.fragmentConfig != null){
                                configs = it.extensionParams.fragmentConfig;
                            }
                            configs << [ patient: patient, patientId: patient.patient.id, app: it.appId ]
                    %>
                            ${ ui.includeFragment(it.extensionParams.provider, it.extensionParams.fragment, configs)}
                    <%  }
                    } %>
                </div>
                <div class="col-12 col-sm-12 col-md-12 col-lg-6">
                    <% if (secondColumnFragments) {
                        secondColumnFragments.each {
                            // create a base map from the fragmentConfig if it exists, otherwise just create an empty map
                            def configs = [:];
                            if(it.extensionParams.fragmentConfig != null){
                                configs = it.extensionParams.fragmentConfig;
                            }
                            configs << [ patient: patient, patientId: patient.patient.id, app: it.appId ]
                    %>
                            ${ ui.includeFragment(it.extensionParams.provider, it.extensionParams.fragment, configs)}
                    <%   }
                    } %>
                </div>
            </div>
        </div>
        <div class="col-12 col-sm-12 col-md-12 col-lg-3">
            <% if ((visitActions && visitActions.size() > 0) || (overallActions && overallActions.size() > 0) || (otherActions && otherActions.size() > 0))  { %>
                <div class="action-section">
                    <% if (activeVisit && visitActions && visitActions.size() > 0) { %>
                        <ul class="float-left">
                            <h3 >${ ui.message("coreapps.clinicianfacing.activeVisitActions") }</h3>
                            <% visitActions.each { ext -> %>
                            <li class="float-left">
                                <a href="${ ui.escapeJs(ext.url("/" + ui.contextPath(), appContextModel, ui.thisUrl())) }" id="${ ext.id }" class="float-left">
                                    <i class="${ ext.icon }"></i>
                                    ${ ui.message(ext.label) }
                                </a>
                            </li>
                            <% } %>
                        </ul>
                    <% } %>
                    <% if (overallActions && overallActions.size() > 0) { %>
                        <ul class="float-left">
                            <h3>${ ui.message("coreapps.clinicianfacing.overallActions") }</h3>
                            <%
                                overallActions.each { ext -> %>
                                    <li class="float-left">
                                        <a href="${ ui.escapeJs(ext.url("/" + ui.contextPath(), appContextModel, ui.thisUrl())) }" id="${ ext.id }" class="float-left">
                                            <i class="${ ext.icon }"></i>
                                            ${ ui.message(ext.label) }
                                        </a>
                                    </li>
                            <% } %>
                        </ul>
                    <% } %>
                    <%
                     def cxtModel = [ patientId: patient.id, activeVisitId: activeVisit ? activeVisit.visit.id : null]
                     otherActions.each { action -> %>
                    <a id="${ action.id }" class="button medium" href="${ ui.escapeJs(action.url("/" + ui.contextPath(), cxtModel)) }" class="float-left">
                        <i class="${ action.icon } float-left"></i>${ ui.message(action.label) }
                    </a>
                    <% } %>
                </div>
            <% } %>
        </div>
    </div>
