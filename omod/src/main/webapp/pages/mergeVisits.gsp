<%
    ui.decorateWith("appui", "standardEmrPage")

    ui.includeCss("coreapps", "patientdashboard/patientDashboard.css")

    ui.includeJavascript("coreapps", "patientdashboard/patient.js")
    ui.includeJavascript("coreapps", "custom/visits.js")
    ui.includeJavascript("uicommons", "bootstrap-collapse.js")
    ui.includeJavascript("uicommons", "bootstrap-transition.js")

    def dateFormat = new java.text.SimpleDateFormat("dd MMM yyyy")
    def visits = patient.allVisitsUsingWrappers
    def visitsIndex = ""
    if (visits  != null ){
        visitsIndex = visits.collect{ it.visit.id }.sort().reverse()
    }

%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.format(patient.patient.familyName) }, ${ ui.format(patient.patient.givenName) }" , link: '${ui.pageLink("coreapps", "patientdashboard/patientDashboard", [patientId: patient.patient.id])}'},
        { label: "${ ui.message("coreapps.task.mergeVisits.label")}"}
    ];

    jq(function(){
        var mergeVisitsArray = new Array();
        var visitsIndexArray = ${visitsIndex};
        jq.enableMergeButton = function() {
            if (mergeVisitsArray.length > 1 ) {
                jq("#mergeVisitsBtn").removeAttr('disabled');
                jq("#mergeVisitsBtn").removeClass('disabled');
                jq("#mergeVisitsBtn").addClass('enabled');
            } else {
                jq("#mergeVisitsBtn").removeClass('enabled');
                jq("#mergeVisitsBtn").attr('disabled', 'disabled');
                jq("#mergeVisitsBtn").addClass('disabled');
            }
        };
        jq.enableMergeButton();

        jq('.selectVisit').click(function(event){
            var selectedVisit = jq(event.target).attr("data-visit-id");
            var visitChecked = jq(event.target).is(':checked')
            if ( visitChecked && jq.inArray(selectedVisit, mergeVisitsArray) == -1) {
                //if the visit is checked and the array does not contain the value already
                mergeVisitsArray.push(selectedVisit);
                jq.enableMergeButton();
            }
        });

        jq("form").submit(function(){
            if( mergeVisitsArray.length > 0 ){
                var jsonMergeVisits = JSON.stringify({"mergeVisits": mergeVisitsArray});
                // jq("#mergeVisits").val(jsonMergeVisits);
                return true;
            }
            return false;
        });
    });
</script>
${ ui.includeFragment("coreapps", "patientHeader", [ patient: patient.patient, activeVisit: activeVisit ]) }

<h3>${ ui.message("coreapps.task.mergeVisits.label") }</h3>
${ ui.message("coreapps.task.mergeVisits.instructions") }

<form method="post" action="${ ui.pageLink("coreapps", "mergeVisits") }">
    <table id="active-visits" width="100%" border="1" cellspacing="0" cellpadding="2">
        <thead>
        <tr>
            <th>${ ui.message("coreapps.merge") }</th>
            <th>${ ui.message("coreapps.startDate.label") }</th>
            <th>${ ui.message("coreapps.stopDate.label") }</th>
            <th>${ ui.message("coreapps.patientDashBoard.encounters") }</th>
        </tr>
        </thead>
        <tbody>

        <%  visits.eachWithIndex { wrapper, idx ->
            def visitId = wrapper.visit.id
        %>
        <tr id="visit-${ visitId }">
            <td><input type="checkbox" name="mergeVisits" value="${ visitId }" id="mergeVisit-${ visitId }" class="selectVisit" data-visit-id="${ visitId }"/></td>
            <td>
                ${dateFormat.format(wrapper.visit.startDatetime)}
            </td>
            <td>
                <% if(wrapper.visit.stopDatetime != null) { %>
                    ${dateFormat.format(wrapper.visit.stopDatetime)}
                <% } %>
            </td>
            <td>
               <% def encounters = wrapper.sortedEncounters
                   encounters.each { encounter ->
                   def encounterTypeUuid = "ui.i18n.EncounterType.name." + encounter.encounterType.uuid
               %>
                    ${ ui.message( encounterTypeUuid ) }
                    <% if (encounter != encounters.last()) { %>, <% } %>
               <% } %>

            </td>
        </tr>
        <% } %>
        </tbody>
    </table>
    <!--
    <input type="hidden" name="mergeVisits" id="mergeVisits" value="">
    -->
    <input type="hidden" name="patientId" value="${ patient.patient.id }">

    <div>
        <input type="button" class="cancel" value="${ ui.message("coreapps.cancel") }" onclick="javascript:window.location='${ ui.pageLink("coreapps", "patientdashboard/patientDashboard", [patientId: patient.patient.id]) }'" />
        <input type="submit" id="mergeVisitsBtn" class="confirm" value="${ ui.message("coreapps.task.mergeVisits.mergeSelectedVisits") }" />
    </div>
</form>