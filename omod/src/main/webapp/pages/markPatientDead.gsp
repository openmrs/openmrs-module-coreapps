<%
    ui.includeJavascript("coreapps", "custom/markPatientDead.js")
    ui.includeCss("coreapps", "markpatientdead/markPatientDead.css")
    ui.decorateWith("appui", "standardEmrPage", [title: ui.message("coreapps.markPatientDead.label")])

    Calendar cal = Calendar.getInstance()
    def maxAgeYear = cal.get(Calendar.YEAR)
    def minAgeYear = patient.getAge() ? maxAgeYear - patient.getAge() : null;
%>

<style>

    form fieldset {
        min-width: 100%;
    }

    span.date input {
        min-width: unset;
    }

    form input, form select {
        display: inline;
    }

    select.time-selector {
        width: 70px;
        min-width: 70px;
    }

    div.cause-with-subcauses a {
        cursor: pointer;
    }

    .cause-of-death-radio, .cause-with-subcauses {
        padding-top: 5px;
    }

    .subcauses .cause-of-death-radio {
        padding-left: 20px;
    }

</style>

<script type="text/javascript">
    var breadcrumbs = [
        {icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm'},
        { label: "${ ui.escapeJs(ui.format(patient)) }" , link: '${ returnUrl }'},
        { label: "${ ui.encodeJavaScript(ui.message("coreapps.markPatientDead.label")) }" }
    ];

    jq(function () {
        jq('#deceased').change(function () {
            if (this.checked) {
                showContainer('#death-date-container');
                showContainer('#cause-of-death-container');
                <% if (patient?.getDead() != true) { %>
                    showAlert('#mark-patient-dead-warning');
                <% } else { %>
                    hideAlert('#mark-patient-not-dead-warning');
                <% } %>
            } else {
                hideContainer('#death-date-container');
                hideContainer('#cause-of-death-container');
                <% if (patient?.getDead() != true) { %>
                    hideAlert('#mark-patient-dead-warning');
                <% } else if (renderProgramWarning == true) { %>
                    showAlert('#mark-patient-not-dead-warning');
                <% } %>
            }
        });

        jq('#deceased').each(function () {
            if (this.checked) {
                showContainer('#death-date-container');
                showContainer('#cause-of-death-container');
                <% if (patient?.getDead() != true) { %>
                    showAlert('#mark-patient-dead-warning');
                <% } %>
            } else {
                hideContainer('#death-date-container');
                hideContainer('#cause-of-death-container');
                <% if (patient?.getDead() == true && renderProgramWarning == true) { %>
                  showAlert('#mark-patient-not-dead-warning');
                <% } %>
            }
        });

        jq('#mark-patient-dead').submit(function () {
            jq(".field-error").html("");

            var hasError = false;
            // check if the deceased checkbox is selected
            if (jq('#deceased').is(":checked")) {
                // validate that the date of death and cause of death are not empty
                if (jq('#death-date-display').val() === "") {
                    jq("#death-date > .field-error").append("${ui.message("coreapps.markPatientDead.dateOfDeath.errorMessage")}").show();
                    hasError = true;
                }
                if (jq('#cause-of-death').val() === "") {
                    jq("#cause-of-death-container > .field-error").append("${ui.message("coreapps.markPatientDead.causeOfDeath.errorMessage")}").show();
                    hasError = true;
                }
                if(!hasError && ${ui.convertTimezones()}){
                    var inputDate = new Date(jq("#death-date-field").val());
                    jq("#death-date-field").val(inputDate.toISOString())
                }
            }
            return !hasError;
        });

        jq("#cause-of-death-1").change(function() {
            const v = jq(this).val();
            jq("#cause-of-death").val(v);
            jq(".cause-of-death-2-container").hide();
            jq(".cause-of-death-2").val("");
            jq("#" + v + "-cause-of-death-2-container").show();
        });

        jq(".cause-of-death-2").change(function() {
            const v = jq(this).val();
            if (v !== "") {
                jq("#cause-of-death").val(v);
            }
            else {
                jq("#cause-of-death").val(jq("#cause-of-death-1").val());
            }
        });
    });
</script>

${ui.includeFragment("coreapps", "patientHeader", [patient: patient])}
<h3>${ui.message("coreapps.markPatientDead.label")}</h3>

<div id="mark-patient-dead-warning" class="alert alert-warning hidden" role="alert">
    <% if (renderProgramWarning == true) { %>
        ${ui.message("coreapps.markPatientDead.warning")}
    <% } else { %>
        ${ui.message("coreapps.markPatientDead.warning.visitsOnly")}
    <% } %>
</div>

<div id="mark-patient-not-dead-warning" class="alert alert-warning hidden" role="alert">
    ${ui.message("coreapps.markPatientDead.markPatientNotDead.warning")}
</div>

<form method="post" id="mark-patient-dead">

    <fieldset>

        <span id="deceased-container">
            <input id="deceased" name="dead" type="checkbox"${ deadSelected ? ' checked="checked"' : '' } />
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
                        defaultDate  : deathDate,
                        useTime      : includesTime && timeWidget == "",
                        showEstimated: false,
                        initialValue : lastVisitDate ?:new Date(),
                        startDate    : lastVisitDate?:birthDate,
                        endDate      : new Date(),
                        minYear      : minAgeYear,
                        maxYear      : maxAgeYear,
                        minuteStep   : minuteStep,
                        id           : 'death-date'
                ])}
                <% if (includesTime && timeWidget == "select-lists") { %>
                    <span id="death-date-time-container">
                        <select id="death-date-hour" name="deathDateHour" class="time-selector hour-selector">
                            <% for (def i=0; i<24; i++) { %>
                            <option value="${i}"${deathDateHour == i ? " selected" : ""}>${i<10 ? "0" + i : i}</option>
                            <% } %>
                        </select> :
                        <select id="death-date-minute" name="deathDateMinute" class="time-selector minute-selector">
                            <% for (def i=0; i<60; i+=minuteStep) { %>
                            <option value="${i}"${deathDateMinute == i ? " selected" : ""}>${i<10 ? "0" + i : i}</option>
                            <% } %>
                        </select>
                    </span>
                <% } %>
            </span>
        </p>

        <p>
            <span id="cause-of-death-container">
                <label for="cause-of-death">
                    <span>${ui.message("coreapps.markPatientDead.causeOfDeath")}</span>
                </label>
                <% if (!causeOfDeathAnswers.isEmpty()) { %>
                    <select name="causeOfDeath" id="cause-of-death">
                        <option value="">${ui.message("coreapps.markPatientDead.causeOfDeath.selectTitle")}</option>
                        <% causeOfDeathAnswers.each { cause -> %>
                            <option value="${cause.uuid}"${ (causeOfDeath == cause) ? " selected=\"selected\"" : "" }>
                                ${ui.format(cause)}
                            </option>
                        <% } %>
                    </select>
                    <span class="field-error" style="display: none;"></span>
                <% } else if (!causeOfDeathSetMembers.isEmpty()) { %>
                    <% if (!duplicateCausesOfDeath.isEmpty()) { %>
                        <div>${ui.message("coreapps.markPatientDead.causeOfDeath.duplicateConceptsInSets")}</div>
                    <% } else { %>

                        <% causeOfDeathSetMembers.keySet().each{ cause ->
                            def subCauses = causeOfDeathSetMembers.get(cause)
                            def causeSelected = causeOfDeath != null && causeOfDeath == cause
                            def subCauseSelected = subCauses != null && causeOfDeath != null && subCauses.contains(causeOfDeath)
                        %>
                            <% if (subCauses == null || subCauses.isEmpty()) { %>
                                <div class="cause-of-death-radio">
                                    <input type="radio" name="causeOfDeath" value="${ cause.uuid }"${ causeSelected ? "checked=\"checked\"" : "" } />
                                    ${ui.format(cause)}
                                </div>
                            <% } else { %>
                                <div class="cause-with-subcauses cause-with-subcauses-${ cause.id }" style="${ !subCauseSelected ? "" : "display:none;" }">
                                    <a href="#" onclick="jq('.cause-with-subcauses-${ cause.id }').toggle();">
                                        <span class="icon-plus"></span>
                                        ${ui.format(cause)}
                                    </a>
                                </div>
                                <div class="cause-with-subcauses subcauses cause-with-subcauses-${ cause.id }" style="${ subCauseSelected ? "" : "display:none;" }">
                                    <a href="#" onclick="jq('.cause-with-subcauses-${ cause.id }').toggle();">
                                        <span class="icon-minus"></span>
                                        ${ui.format(cause)}
                                    </a>
                                    <% subCauses.each { subCause -> %>
                                        <div class="cause-of-death-radio">
                                            <input type="radio" name="causeOfDeath" value="${ subCause.uuid }"${ (causeOfDeath == subCause) ? "checked=\"checked\"" : "" } />
                                            ${ui.format(subCause)}
                                        </div>
                                    <% } %>
                                </div>
                            <% } %>
                        <% } %>
                    <% } %>
                <% } else { %>
                    <div><${ui.message("coreapps.markPatientDead.causeOfDeath.missingConcepts")}</div>
                <% } %>
            </span>
        </p>

        <p>
            <span>
                <input type="button" class="cancel" value="${ ui.message("coreapps.cancel") }" onclick="javascript:window.history.back()" />
            </span>
            <span>
                <input type="submit" class="confirm" value="${ ui.message("coreapps.markPatientDead.submitValue")}">
            </span>
        </p>
    </fieldset>
</form>
