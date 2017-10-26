<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeCss("coreapps", "clinicianfacing/patient.css")
%>

<!-- TODO breadcrumbs -->

<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message(app.label)}"}
    ];

    jq(function(){
        jq(".tabs").tabs();
        // make sure we reload the page if the location is changes; this custom event is emitted by by the location selector in the header
        jq(document).on('sessionLocationChanged', function() {
            window.location.reload();
        });
    });

</script>

<% if (includeFragments){

    includeFragments.each {
        // create a base map from the fragmentConfig if it exists, otherwise just create an empty map
        def configs = [:];
        if(it.extensionParams.fragmentConfig != null){
            configs = it.extensionParams.fragmentConfig;
        }
 %>
${ui.includeFragment(it.extensionParams.provider, it.extensionParams.fragment, configs)}
<%}
} %>

<div class="clear"></div>
<div class="container">
    <div class="dashboard clear">
        <!-- only show the title div if a title has been defined in the messages.properties -->
        <% if (ui.message(app.id + ".custom.title") != app.id + ".custom.title") { %>
        <div class="title">
            <h3>${ ui.message(app.id + ".custom.title") }</h3>
        </div>
        <% } %>
        <div class="info-container column">
            <% if (firstColumnFragments) {
                firstColumnFragments.each {
                    // create a base map from the fragmentConfig if it exists, otherwise just create an empty map
                    def configs = [:];
                    if(it.extensionParams.fragmentConfig != null){
                        configs = it.extensionParams.fragmentConfig;
                    }
                    configs << [  app: it.appId ]
            %>
            ${ ui.includeFragment(it.extensionParams.provider, it.extensionParams.fragment, configs)}
            <%  }
            } %>

        </div>
        <div class="info-container column">
            <% if (secondColumnFragments) {
                secondColumnFragments.each {
                    // create a base map from the fragmentConfig if it exists, otherwise just create an empty map
                    def configs = [:];
                    if(it.extensionParams.fragmentConfig != null){
                        configs = it.extensionParams.fragmentConfig;
                    }
                    configs << [ app: it.appId ]
            %>
            ${ ui.includeFragment(it.extensionParams.provider, it.extensionParams.fragment, configs)}
            <%   }
            } %>

        </div>
        <% if (actions && actions.size() > 0) { %>
        <div class="action-container column">
            <div class="action-section">
                <ul class="float-left">
                    <h3>${ ui.message("coreapps.clinicianfacing.overallActions") }</h3>
                    <%
                            actions.each { ext -> %>
                    <li class="float-left">
                        <a href="${ ui.escapeJs(ext.url("/" + ui.contextPath(), appContextModel, ui.thisUrl())) }" id="${ ext.id }" class="float-left">
                            <i class="${ ext.icon } float-left"></i>
                            ${ ui.message(ext.label) }
                        </a>
                    </li>
                    <% } %>
                </ul>
            </div>
        </div>
        <% } %>
    </div>
</div>
