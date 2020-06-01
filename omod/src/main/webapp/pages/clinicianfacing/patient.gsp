<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeCss("coreapps", "clinicianfacing/patient.css")
    ui.includeJavascript("coreapps", "custom/deletePatient.js")
    ui.includeJavascript("appui", "jquery-3.4.1.min.js")

    def formattedBreadCrumbs = "";

    // allows displaying additional breadcrumbs details defined by 'breadCrumbs.details.uuids' global property
    if (breadCrumbsDetails) {
    	formattedBreadCrumbs += " " + breadCrumbsFormatters[0]
    	breadCrumbsDetails.eachWithIndex {attr, index ->
    		formattedBreadCrumbs += ui.escapeJs(ui.encodeHtmlContent(ui.format(attr)));
    		if (breadCrumbsDetails.size()-1 != index) {
    			formattedBreadCrumbs += breadCrumbsFormatters[1]
    		}
    	}
    	formattedBreadCrumbs += breadCrumbsFormatters[2]
    }
%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.escapeJs(ui.encodeHtmlContent(ui.format(patient.patient))) }${ formattedBreadCrumbs }" ,
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

        <!-- only show the title div if a title has been defined in the messages.properties -->
    <% if (ui.message(dashboard + ".custom.title") != dashboard + ".custom.title") { %>
        <div class="dashboard clear row">
            <div class="col-12">
                <div class="title">
                    <h3>${ ui.message(dashboard + ".custom.title") }</h3>
                </div>
            </div>
        </div>
    <% } %>

    <div class="dashboard clear row">
        <div class="col-12 col-lg-9">
            <div class="row">
                <div class="col-12 col-lg-6">
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
                <div class="col-12 col-lg-6">
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

        <!-- The Visit Actions section is duplicated. This is because it needs to be rendered
          in a different position in the DOM depending on screen size. To make it a little easier
          to work with, the two blocks are immediately below each other. -->

        <!-- This is the first Visit Actions section, which is displayed at the top of the screen
          using `order-first` on mobile, and is hidden on large screens. -->
        <% if (activeVisit && visitActions && visitActions.size() > 0) { %>
            <div class="action-section col-12 order-first d-lg-none">
                <ul class="float-left">
                    <h3 >${ ui.message("coreapps.clinicianfacing.activeVisitActions") }</h3>
                    <% visitActions.each { ext -> %>
                    <li class="float-left">
                        <a href="${ ui.escapeJs(ext.url("/" + ui.contextPath(), appContextModel, ui.thisUrl())) }" id="${ ext.id }" class="float-left">
                            <div class="row">
                               <div class="col-1 col-lg-2">
                                  <i class="${ ext.icon }"></i>
                                </div>
                                <div class="col-11 col-lg-10">
                                   ${ ui.message(ext.label) }
                                </div>
                            </div>
                        </a>
                    </li>
                    <% } %>
                </ul>
            </div>
        <% } %>
        <div class="col-12 col-lg-3 p-0">
            <% if ((visitActions && visitActions.size() > 0) || (overallActions && overallActions.size() > 0) || (otherActions && otherActions.size() > 0))  { %>
                <div class="action-section">
                    <!-- This is the second Visit Actions section, which is hidden on mobile
                      and displayed in the right column on large screens (i.e., its display
                      position matches its DOM position). -->
                    <% if (activeVisit && visitActions && visitActions.size() > 0) { %>
                        <ul class="float-left d-none d-lg-block">
                            <h3 >${ ui.message("coreapps.clinicianfacing.activeVisitActions") }</h3>
                            <% visitActions.each { ext -> %>
                            <li class="float-left">
                                <a href="${ ui.escapeJs(ext.url("/" + ui.contextPath(), appContextModel, ui.thisUrl())) }" id="${ ext.id }" class="float-left">
                                    <div class="row">
                                       <div class="col-1 col-lg-2">
                                          <i class="${ ext.icon }"></i>
                                        </div>
                                        <div class="col-11 col-lg-10">
                                           ${ ui.message(ext.label) }
                                        </div>
                                    </div>
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
                                            <div class="row">
                                                <div class="col-1 col-lg-2">
                                                    <i class="${ ext.icon }"></i>
                                                </div>
                                                <div class="col-11 col-lg-10">
                                                    ${ ui.message(ext.label) }
                                                </div>
                                            </div>
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
