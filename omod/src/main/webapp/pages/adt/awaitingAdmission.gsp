<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeCss("coreapps", "adt/inpatient.css")
    ui.includeCss("coreapps", "adt/awaitingAdmission.css")
    def awaitingAdmissionNumber = 0;
    if (awaitingAdmissionList != null ){
        awaitingAdmissionNumber = awaitingAdmissionList.size();
    }
%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("coreapps.app.awaitingAdmission.label")}"}
    ];

    // TODO: make this more robust--kind of hacky to rely on column index now that it can change
    var WARD_COLUMN_INDEX = ${ paperRecordIdentifierDefinitionAvailable ? '5' : '4' };
    var awaitingAdmissionsTable = jq("#awaiting-admission").dataTable();

    jq(document).ready(function() {
        var ward= "";
        jq.fn.dataTableExt.afnFiltering.push(
                function (oSettings, aData, iDataIndex) {
                    var currentWard = aData[WARD_COLUMN_INDEX];
                    currentWard = currentWard.replace(/'/g, "\\’");
                    if (ward.length < 1 ){
                        return true;
                    }else if (currentWard.match(new RegExp(ward)) != null ){
                        return true;
                    }
                    return false;
                }
        );
        jq("#inpatients-filterByLocation").change(function(event){
            var selectedItemId="";
            jq("select option:selected").each(function(){
                ward = jq(this).text();
                ward = ward.replace(/'/g, "\\’");
                selectedItemId =this.value;
                if (ward.length > 0) {
                    jq('#awaiting-admission').dataTable({ "bRetrieve": true }).fnDraw();
                } else {
                    jq('#awaiting-admission').dataTable({ "bRetrieve": true }).fnFilter('', WARD_COLUMN_INDEX);
                }

                jq("#listSize").text(jq('#awaiting-admission').dataTable({ "bRetrieve": true }).fnSettings().fnRecordsDisplay());
            });
        });
    });

</script>

<h2>${ ui.message("coreapps.app.awaitingAdmission.title") }</h2>
<strong class="inpatient-count">${ ui.message("coreapps.app.awaitingAdmission.patientCount") }: <span id="listSize">${awaitingAdmissionNumber}</span></strong>
<div class="inpatient-filter">
    ${ ui.includeFragment("uicommons", "field/location", [
            "id": "inpatients-filterByLocation",
            "formFieldName": "filterByLocationId",
            "label": "coreapps.app.awaitingAdmission.filterByAdmittedTo",
            "withTag": "Admission Location",
            "initialValue": sessionContext.sessionLocation
    ] ) }
</div>

<table id="awaiting-admission" width="100%" border="1" cellspacing="0" cellpadding="2">
    <thead>
    <tr>
        <th>${ ui.message("coreapps.patient.identifier") }</th>
        <% if (paperRecordIdentifierDefinitionAvailable) { %>
            <th>${ ui.message("paperrecord.archivesRoom.recordNumber.label") }</th>
        <% } %>
        <th>${ ui.message("coreapps.person.name") }</th>
        <th>${ ui.message("coreapps.app.awaitingAdmission.currentWard") }</th>
        <th>${ ui.message("coreapps.app.awaitingAdmission.provider") }</th>
        <th>${ ui.message("coreapps.app.awaitingAdmission.admissionLocation") }</th>
        <th>${ ui.message("coreapps.app.awaitingAdmission.diagnosis") }</th>
        <th>${ ui.message("coreapps.app.awaitingAdmission.admitPatient") }</th>

    </tr>
    </thead>
    <tbody>
    <% if ((awaitingAdmissionList == null) || (awaitingAdmissionList != null && awaitingAdmissionList.size() == 0)) { %>
    <tr>
        <td colspan="8">${ ui.message("coreapps.none") }</td>
    </tr>
    <% } %>
    <% awaitingAdmissionList.each { v ->
        def patientId = v.patientId
        def visitId = v.visitId
    %>
    <tr id="visit-${ v.patientId
    }">
        <td>${ v.primaryIdentifier ? ui.format(v.primaryIdentifier) : ''}</td>
        <% if (paperRecordIdentifierDefinitionAvailable) { %>
            <td>${ v.paperRecordIdentifier ? ui.format(v.paperRecordIdentifier) : ''}</td>
        <% } %>
        <td>
            <a href="${ ui.pageLink("coreapps", "patientdashboard/patientDashboard", [ patientId: v.patientId ]) }">
                ${ ui.format((v.patientFirstName ? v.patientFirstName : '') + " " + (v.patientLastName ? v.patientLastName : '')) }
            </a>
        </td>

        <td>
            ${ ui.format(v.mostRecentAdmissionRequestFromLocation) }
            <br/>
            <small>
                ${ ui.format(v.mostRecentAdmissionRequestDatetime)}
            </small>
        </td>
        <td>
            ${ ui.format(v.mostRecentAdmissionRequestProvider) }
        </td>
        <td>${ ui.format(v.mostRecentAdmissionRequestToLocation) }</td>
        <td>
            <% v.mostRecentAdmissionRequestDiagnoses.each { %>
                ${ ui.format(it.diagnosis.codedAnswer ?: it.diagnosis.nonCodedAnswer) }${ it != v.mostRecentAdmissionRequestDiagnoses.last() ? ', ' : '' }
            <% } %>
        </td>
        <td>
            <% admissionActions.each { task ->
                def url = task.url.replaceAll('\\{\\{patientId\\}\\}', patientId.toString())
                url = url.replaceAll('\\{\\{visit.id\\}\\}', visitId.toString())
            %>
            <a href="/${ contextPath }/${ url }" class="">
                <i class="${task.icon}"></i> ${ ui.message(task.label) }</a>
            <% } %>
        </td>
    </tr>
    <% } %>
    </tbody>
</table>

<% if ( (awaitingAdmissionList != null) && (awaitingAdmissionList.size() > 0) ) { %>
${ ui.includeFragment("uicommons", "widget/dataTable", [ object: "#awaiting-admission",
        options: [
                bFilter: true,
                bJQueryUI: true,
                bLengthChange: false,
                iDisplayLength: 10,
                sPaginationType: '\"full_numbers\"',
                bSort: false,
                sDom: '\'ft<\"fg-toolbar ui-toolbar ui-corner-bl ui-corner-br ui-helper-clearfix datatables-info-and-pg \"ip>\''
        ]
]) }
<% } %>