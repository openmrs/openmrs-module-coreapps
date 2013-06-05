<%
    ui.decorateWith("appui", "standardEmrPage")
    ui.includeCss("coreapps", "findpatient/findPatient.css")
%>
<script type="text/javascript">
    var breadcrumbs = [
        { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
        { label: "${ ui.message("coreapps.findPatient.app.label")}"}
    ];

    jq(function() {
        var resultTemplate = _.template(jq('#result-template').html());

        // this is a quick hack -- we really want to autosearch after they type a few keys
        jq('#patient-search-form').submit(function() {
            var query = jq('#patient-search').val();
            var customRep = 'custom:(uuid,identifiers:(identifierType:(name),identifier),person)';
            jq.getJSON('/' + OPENMRS_CONTEXT_PATH + '/ws/rest/v1/patient', {v: customRep, q: query }, function(data) {
                var resultTarget = jq('#patient-search-results');
                resultTarget.html('');
                _.each(data.results, function(patient) {
                    resultTarget.append(resultTemplate({ patient: patient }));
                });
            });
            return false;
        });

        jq('#patient-search').focus();

        jq('#patient-search-results').on('click', '.patient-search-result', function(evt) {
            location.href = jq(this).find('a.button').attr('href');
        });
    });
</script>

<form method="get" id="patient-search-form">
    <input type="text" id="patient-search" placeholder="${ ui.message("coreapps.findPatient.search.placeholder") }" autocomplete="off"/>
    <input type="submit" value="${ ui.message("coreapps.findPatient.search.button") }"/>
</form>

<ul id="patient-search-results">
</ul>

<script type="text/template" id="result-template">
    <li class="patient-search-result" data-patient-uuid="{{- patient.uuid }}">
        {{ _.each(patient.identifiers, function(id) { }}
            <span class="patient-identifier">
                <span class="identifier-type">{{- id.identifierType.name }}</span>
                <span class="identifier">{{- id.identifier }}</span>
            </span>
        {{ }) }}
        <span class="preferred-name">
            {{- patient.person.preferredName.display }}
        </span>
        <span class="age">{{- patient.person.age }}</span>
        <span class="gender">{{- patient.person.gender }}</span>
        <a class="button" href="{{= '/' + OPENMRS_CONTEXT_PATH + '/coreapps/patientdashboard/patientDashboard.page?patientId=' + patient.uuid }}">${ ui.message("coreapps.findPatient.result.view") }</a>
    </li>
</script>