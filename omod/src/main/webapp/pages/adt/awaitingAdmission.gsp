<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeCss("mirebalais", "inpatient.css")
    ui.includeCss("mirebalais", "awaitingAdmission.css")
    def awaitingAdmissionNumber = 0;
    if (awaitingAdmissionList != null ){
        awaitingAdmissionNumber = awaitingAdmissionList.size();
    }
%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("mirebalaisreports.awaitingAdmission.label")}"}
    ];

    var WARD_COLUMN_INDEX = 5;
    var inpatientsTable = jq("#active-visits").dataTable();

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
                    jq('#active-visits').dataTable({ "bRetrieve": true }).fnDraw();
                } else {
                    jq('#active-visits').dataTable({ "bRetrieve": true }).fnFilter('', WARD_COLUMN_INDEX);
                }

                jq("#listSize").text(jq('#active-visits').dataTable({ "bRetrieve": true }).fnSettings().fnRecordsDisplay());
            });
        });
    });

</script>

<h2>${ ui.message("mirebalaisreports.awaitingAdmission.title") }</h2>
<strong class="inpatient-count">${ ui.message("emr.inpatients.patientCount") }: <span id="listSize">${awaitingAdmissionNumber}</span></strong>
<div class="inpatient-filter">
    ${ ui.includeFragment("uicommons", "field/location", [
            "id": "inpatients-filterByLocation",
            "formFieldName": "filterByLocationId",
            "label": "mirebalaisreports.awaitingAdmission.filterByAdmittedTo",
            "withTag": "Admission Location",
            "initialValue": sessionContext.sessionLocation
    ] ) }
</div>

<table id="awaiting-admission" width="100%" border="1" cellspacing="0" cellpadding="2">
    <thead>
    <tr>
        <th>${ ui.message("emr.patient.identifier") }</th>
        <% if (paperRecordIdentifierDefinitionAvailable) { %>
            <th>${ ui.message("ui.i18n.PatientIdentifierType.name.e66645eb-03a8-4991-b4ce-e87318e37566") }</th>
        <% } %>
        <th>${ ui.message("emr.person.name") }</th>
        <th>${ ui.message("emr.inpatients.currentWard") }</th>
        <th>${ ui.message("emr.patientDashBoard.provider") }</th>
        <th>${ ui.message("disposition.emrapi.admitToHospital.admissionLocation") }</th>
        <th>${ ui.message("mirebalaisreports.noncodeddiagnoses.diagnosis") }</th>
        <th>${ ui.message("mirebalaisreports.awaitingAdmission.admitPatient") }</th>

    </tr>
    </thead>
    <tbody>
    <% if ((awaitingAdmissionList == null) || (awaitingAdmissionList != null && awaitingAdmissionList.size() == 0)) { %>
    <tr>
        <td colspan="8">${ ui.message("emr.none") }</td>
    </tr>
    <% } %>
    <% awaitingAdmissionList.each { v ->
        def patientId = v.patientId
        def visitId = v.visitId
    %>
    <tr id="visit-${ v.patientId
    }">
        <td>${ v.primaryIdentifier ?: ''}</td>
        <% if (paperRecordIdentifierDefinitionAvailable) { %>
            <td>${ v.paperRecordIdentifier ?: ''}</td>
        <% } %>
        <td>
            <a href="${ ui.pageLink("coreapps", "patientdashboard/patientDashboard", [ patientId: v.patientId ]) }">
                ${ ui.format((v.patientFirstName ? v.patientFirstName : '') + " " + (v.patientLastName ? v.patientLastName : '')) }
            </a>
        </td>

        <td>
            ${ ui.format(v.mostRecentAdmissionRequest.fromLocation) }
            <br/>
            <small>
                ${ ui.format(v.mostRecentAdmissionRequest.datetime)}
            </small>
        </td>
        <td>
            ${ ui.format(v.mostRecentAdmissionRequest.provider) }
        </td>
        <td>${ ui.format(v.mostRecentAdmissionRequest.toLocation) }</td>
        <td>
            <% v.mostRecentAdmissionRequest.diagnoses.each { %>
                ${ ui.format(it.diagnosis.codedAnswer ?: it.diagnosis.nonCodedAnswer) }
                ${ it != v.mostRecentAdmissionRequest.diagnoses.last() ? ',' : '' }
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
${ ui.includeFragment("uicommons", "widget/dataTable", [ object: "#active-visits",
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