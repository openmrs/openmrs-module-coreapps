<%
    ui.decorateWith("appui", "standardEmrPage")

	ui.includeCss("coreapps", "patientDashboard.css")

    ui.includeJavascript("coreapps", "patient.js")
    ui.includeJavascript("coreapps", "custom/visits.js")
    ui.includeJavascript("coreapps", "bootstrap-collapse.js")
    ui.includeJavascript("coreapps", "bootstrap-transition.js")

    def tabs = [
        [ id: "visits", label: ui.message("emr.patientDashBoard.visits") ],
        [ id: "contactInfo", label: ui.message("emr.patientDashBoard.contactinfo") ]
    ]

%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.format(patient.patient.familyName) }, ${ ui.format(patient.patient.givenName) }" , link: '${ui.pageLink("coreapps", "patientDashboard", [patientId: patient.patient.id])}'}
    ];

    jq(function(){
        jq(".tabs").tabs();
        createPaperRecordDialog(${patient.id});
        visit.createQuickVisitCreationDialog(${patient.id});
        ko.applyBindings( sessionLocationModel, jq('#request-paper-record-dialog').get(0) );
    });

    var patient = { id: ${ patient.id } };
</script>



${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient.patient, activeVisit: activeVisit ]) }
<div class="tabs" xmlns="http://www.w3.org/1999/html">
    <div class="dashboard-container">
        <div class="actions">
            <% if (!activeVisit) { %>
                <div><a href="javascript:visit.showQuickVisitCreationDialog()"><i class="icon-check-in"></i>${ ui.message("emr.task.startVisit.label") }</a></div>
            <% } %>

            <% overallActions.each {
                def url = it.url
                if (it.type != "script") {
                    url = "/" + contextPath + "/" + url
                } else{
                    url = "javascript:"+it.script
                }
            %>
            <div><a href="${ url }"><i class="${ it.icon }"></i>${ it.label }</a></div>
            <% } %>
        </div>
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
            ${ ui.includeFragment("coreapps", "patientdashboard/" + it.id) }
        </div>
        <% } %>

    </div>
</div>

<div id="request-paper-record-dialog" class="dialog" style="display: none">
    <div class="dialog-header">
        <i class="icon-folder-open"></i>
        <h3>${ ui.message("emr.patientDashBoard.requestPaperRecord.title") }</h3>
    </div>
    <div class="dialog-content">
        <p class="dialog-instructions">${ ui.message("emr.patientDashBoard.requestPaperRecord.confirmTitle") }</p>
        <ul>
            <li class="info">
                <span>${ ui.message("emr.patient") }</span>
                <h5>${ ui.format(patient.patient) }</h5>
            </li>
            <li class="info">
                <span>${ ui.message("emr.location") }</span>
                <h5 data-bind="text: text"></h5>
            </li>
        </ul>

        <button class="confirm right">${ ui.message("emr.confirm") }</button>
        <button class="cancel">${ ui.message("emr.cancel") }</button>
    </div>
</div>

<div id="quick-visit-creation-dialog" class="dialog">
    <div class="dialog-header">
        <h3>
            <i class="icon-folder-open"></i>
            ${ ui.message("emr.visit.createQuickVisit.title") }</h3>
    </div>
    <div class="dialog-content">
        <p class="dialog-instructions">${ ui.message("emr.task.startVisit.message", ui.format(patient.patient)) }</p>

        <button class="confirm right">${ ui.message("emr.confirm") }</button>
        <button class="cancel">${ ui.message("emr.cancel") }</button>
    </div>
</div>
