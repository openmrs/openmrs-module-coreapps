<%
    ui.includeJavascript("coreapps", "custom/markPatientDead.js")
    ui.includeCss("coreapps", "markpatientdead/markPatientDead.css")
    ui.decorateWith("appui", "standardEmrPage", [title: ui.message("coreapps.markPatientDead.label")])

    def htmlSafeId = { extension ->
        "${extension.id.replace(".", "-")}-${extension.id.replace(".", "-")}-extension"
    }

    Calendar cal = Calendar.getInstance()
    def maxAgeYear = cal.get(Calendar.YEAR)
    def minAgeYear = maxAgeYear - patient.getAge()
    def breadcrumbMiddle = breadcrumbOverride ?: '';
%>
<script type="text/javascript">
    var breadcrumbs = [
        {icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm'},
        {
            label: "${ ui.escapeJs(ui.message("coreapps.markPatientDead.label")) }",
            link: '${ ui.urlBind("/" + contextPath + "coreapps/clinicianfacing/patient.page?patientId="+patientId, [ patientId: patient ] ) }'
        }
    ];


    jq(function () {
        jq('#deceased').change(function () {
            if (this.checked) {
                showContainer('#death-date-container');
                showContainer('#cause-of-death-container');
            } else {
                hideContainer('#death-date-container');
                hideContainer('#cause-of-death-container');
            }
        });

        jq('#deceased').each(function () {
            if (this.checked) {
                showContainer('#death-date-container');
                showContainer('#cause-of-death-container');
            } else {
                hideContainer('#death-date-container');
                hideContainer('#cause-of-death-container');
            }
        });

        jq('#mark-patient-dead').submit(function () {
            jq(".field-error").html("");

            var hasError = false;
            // check if the deceased checkbox is selected
            if (jq('#deceased').is(":checked")) {
                // validate that the date of death and cause of death are not empty
                if (jq('#death-date-display').val()=="") {
                    jq("#death-date > .field-error").append("${ui.message("coreapps.markPatientDead.dateOfDeath.errorMessage")}").show();
                    hasError = true;
                }
                if (jq('#cause-of-death').val()=="") {
                    jq("#cause-of-death-container > .field-error").append("${ui.message("coreapps.markPatientDead.causeOfDeath.errorMessage")}").show();
                    hasError = true;
                }
            }
            return !hasError;
        });

    });
</script>

${ui.includeFragment("coreapps", "patientHeader", [patient: patient])}
<h3>${ui.message("coreapps.markPatientDead.label")}</h3>

<form method="post" id="mark-patient-dead">
    <fieldset style="min-width: 40%">

        <span id="deceased-container">
            <% if (patient?.getDead() == true) {

            %>
            <input checked="checked" id="deceased" name="dead" type="checkbox"/>
            <% } else {
            %>
            <input id="deceased" name="dead" type="checkbox"/>
            <%
                }
            %>
            <label for="deceased">
                <span>${ui.message("coreapps.markPatientDead.label")}</span>
            </label>
        </span>

        <p>
            <span id="death-date-container">
                ${ui.includeFragment("uicommons", "field/datetimepicker", [
                        label        : "coreapps.markPatientDead.dateOfDeath",
                        formFieldName: "deathDate",
                        left         : true,
                        defaultDate  : patient?.getDeathDate() ?: null,
                        useTime      : false,
                        showEstimated: false,
                        initialValue : new Date(),
                        startDate    : birthDate,
                        endDate      : new Date(),
                        minYear      : minAgeYear,
                        maxYear      : maxAgeYear,
                        id           : 'death-date'
                ])}
            </span>
        </p>

        <p>
            <span id="cause-of-death-container">
                <label for="cause-of-death">
                    <span>${ui.message("coreapps.markPatientDead.causeOfDeath")}</span>
                </label>
                <select name="causeOfDeath" id="cause-of-death">
                    <option value="">${ui.message("coreapps.markPatientDead.causeOfDeath.selectTitle")}</option>
                    <% if (conceptAnswers != null) {
                        conceptAnswers.each {
                    %>
                    <% if (patient?.getCauseOfDeath()?.getUuid() == it.getAnswerConcept().getUuid()) { %>
                    <option selected="selected"
                            value="${it.getAnswerConcept().getUuid()}">${it.getAnswerConcept().getName()}</option>
                    <% } else { %>
                    <option value="${it.getAnswerConcept().getUuid()}">${it.getAnswerConcept().getName()}</option>
                    <% } %>
                    <%
                            }
                        }
                    %>
                </select>
                <span class="field-error" style="display: none;"></span>
                <% if (conceptAnswers == null) { %>
                <div><${ui.message("coreapps.markPatientDead.causeOfDeath.missingConcepts")}</div>
                <% } %>
            </span>
        </p>

        <p>
            <span>
                <input type="submit" class="confirm" value="${ ui.message("coreapps.markPatientDead.submitValue")}">
            </span>
        </p>
    </fieldset>
</form>