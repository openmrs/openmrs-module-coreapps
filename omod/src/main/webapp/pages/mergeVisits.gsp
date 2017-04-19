<%
    ui.decorateWith("appui", "standardEmrPage")

    ui.includeCss("coreapps", "patientdashboard/patientDashboard.css")

    ui.includeJavascript("coreapps", "patientdashboard/patient.js")
    ui.includeJavascript("coreapps", "custom/visits.js")
    ui.includeJavascript("uicommons", "bootstrap-collapse.js")
    ui.includeJavascript("uicommons", "bootstrap-transition.js")

    def visits = patient.allVisitsUsingWrappers
    def visitsIndex = ""
    if (visits  != null ){
        visitsIndex = visits.collect{ it.visit.id }.sort().reverse()
    }

	if (!returnUrl) {
		returnUrl = ui.pageLink("coreapps", "patientdashboard/patientDashboard", [patientId: patient.patient.id])
	}

%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.escapeJs(ui.encodeHtmlContent(ui.format(patient.patient))) }" , link: '${ui.escapeJs( ui.encodeHtmlAttribute(returnUrl) )}'},
        { label: "${ ui.message("coreapps.task.mergeVisits.label")}"}
    ];

    jq(function(){
        var mergeVisitsArray = new Array();
        var visitsIndexArray = ${visitsIndex};
        jq.enableMergeButton = function() {
            var enableButton =  false;
            var consecutiveCounter = 0;
            var nonConsecutiveCounter = 0;
            jq('#active-visits').find(':checkbox').each( function (i, element){
                var visitId = element.attributes.getNamedItem('data-visit-id').nodeValue;
                var visitChecked = element.checked;
                if ( visitChecked ) {
                    consecutiveCounter++;
                    if (nonConsecutiveCounter > 0) {
                        enableButton = false;
                        consecutiveCounter = 0;
                        return false;
                    }
                }else {
                    if (consecutiveCounter > 0){
                        nonConsecutiveCounter++;
                    }
                }
            });

            if (consecutiveCounter > 1) {
                enableButton = true;
            }

            if ( enableButton ) {
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
            }else{
                mergeVisitsArray.splice(jq.inArray(selectedVisit, mergeVisitsArray), 1);
            }
            jq.enableMergeButton();

        });

        jq("form").submit(function(){
            if( mergeVisitsArray.length > 0 ){
                var jsonMergeVisits = JSON.stringify({"mergeVisits": mergeVisitsArray});
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
            <td width="8%"><input type="checkbox" name="mergeVisits" value="${ visitId }" id="mergeVisit-${ visitId }" class="selectVisit" data-visit-id="${ visitId }"/></td>
            <td width="14%">
                ${ui.format(wrapper.startDate)}
            </td>
            <td width="14%">
                <% if(wrapper.stopDate != null) { %>
                    ${ui.format(wrapper.stopDate)}
                <% } %>
            </td>
            <td width="64%">
                <%= wrapper.sortedEncounters.collect { ui.format(it.encounterType) }.join(", ") %>
            </td>
        </tr>
        <% } %>
        </tbody>
    </table>
    <input type="hidden" name="patientId" value="${ patient.patient.id }" />
	<input type="hidden" name="returnUrl" value="${ ui.encodeHtmlAttribute(returnUrl) }" />

    <div>
        <input type="button" class="cancel" value="${ ui.message("coreapps.return") }" onclick="javascript:window.location='${ ui.encodeJavaScript(returnUrl) }'" />
        <input type="submit" id="mergeVisitsBtn" class="confirm" value="${ ui.message("coreapps.task.mergeVisits.mergeSelectedVisits") }" />
    </div>
</form>
