<%
    ui.decorateWith("appui", "standardEmrPage", [title: ui.message("coreapps.mergePatientsLong")])
    ui.includeCss("coreapps", "datamanagement/mergePatients.css")
%>

<script type="text/javascript">

    var patient1Id = ${patient1.patient.id};
    var patient2Id = ${patient2.patient.id};

    //this value come from the request (isUnknownPatient parameter)
    var isUnknownPatient = ${isUnknownPatient};

    function putFirstPatientAsTheSelectedOne() {
        jq('#second-patient').removeClass('selected');
        jq('#first-patient').addClass('selected');
    }

    function showLeftArrow() {
        jq('#separator-left').removeClass('hidden');
        jq('#separator-right').addClass('hidden');
    }

    function putSecondPatientAsTheSelectedOne() {
        jq('#first-patient').removeClass('selected');
        jq('#second-patient').addClass('selected');
    }

    function showRightArrow() {
        jq('#separator-left').addClass('hidden');
        jq('#separator-right').removeClass('hidden');
    }

    function enableConfirmButton() {
        jq('#confirm-button').removeAttr("disabled");
        jq('#confirm-button').removeClass("disabled");
        jq('#confirm-button').addClass("confirm");
    }


    jq(function () {
        jq('div').on('click', '#first-patient', function () {

            if (!isUnknownPatient) {

                if (!jq('#first-patient').hasClass("selected")) {
                    putFirstPatientAsTheSelectedOne();
                    showLeftArrow();
                    enableConfirmButton();
                    jq('#preferred').val(patient1Id);
                }

            }
        });

        jq('div').on('click', '#second-patient', function () {

            if (!isUnknownPatient) {

                if (!jq('#second-patient').hasClass("selected")) {
                    putSecondPatientAsTheSelectedOne();
                    showRightArrow();
                    enableConfirmButton();
                    jq('#preferred').val(patient2Id);
                }

            }
        });

        jq('#cancel-button').click(function () {
            window.history.back();
        });


    });
</script>

<script type="text/javascript">

    <% if (breadcrumbs) { %>
        var breadcrumbs = ${ breadcrumbs };
    <% } else { %>
        var breadcrumbs = [
            { icon: "icon-home", link: '/' + OPENMRS_CONTEXT_PATH + '/index.htm' },
            { label: "${ ui.message('coreapps.app.dataManagement.label')}", link: '/' + OPENMRS_CONTEXT_PATH + '/coreapps/datamanagement/dataManagement.page' },
            { label: "${ ui.message('coreapps.mergePatientsLong')}", link: '${ui.pageLink("coreapps", "datamanagement/mergePatients?app=coreapps.mergePatients")}'},
            { label: "${ ui.message('coreapps.mergePatients.confirmationQuestion')}" }
        ];
    <% } %>
</script>

<form method="post">
    <% def preferred = (isUnknownPatient ? patient2.patient.id : "") %>
    <input type="hidden" name="patient1" value="${patient1.patient.id}"/>
    <input type="hidden" name="patient2" value="${patient2.patient.id}"/>
    <input type="hidden" name="preferred" id="preferred" value="${preferred}"/>


    <div class="messages-container">
        <% if (isUnknownPatient) { %>
        <h2>${ui.message("coreapps.mergePatients.unknownPatient.message")}</h2>
        <% } else { %>
        <h2>${ui.message("coreapps.mergePatients.confirmationQuestion")}
        <% } %>
            <em>${ui.message("coreapps.mergePatients.choosePreferred.description")}</em>
        </h2>
    </div>

    <div id="patients">
        <div class="center">
            <div class="col">
                <div id="first-patient" class="patient center">
                    <div class="row name">
                        <h3>${ui.message("coreapps.mergePatients.section.names")}</h3>
                        <% patient1.patient.names.each { %>
                        <div>${ui.encodeHtml(it.familyName)}, ${ui.encodeHtml(it.givenName)}</div>
                        <% } %>
                    </div>

                    <div class="row">
                        <h3>${ui.message("coreapps.mergePatients.section.demographics")}</h3>

                        <div>${ui.message("coreapps.gender." + ui.encodeHtml(patient1.patient.gender))}${patient1.patient.age ? ", " + ui.message("coreapps.ageYears", patient1.patient.age) : ""}</div>

                        <div>${ui.message("coreapps.birthdate")}:${ui.format(patient1.patient.birthdate)}</div>
                    </div>

                    <div class="row identifiers">
                        <h3>${ui.message("coreapps.mergePatients.section.primaryIdentifiers")}</h3>
                        <% patient1.primaryIdentifiers.each { %>
                        <% def identifier = (it.preferred ? "<b>${it.identifier}</b>" : it.identifier) %>
                        <div>${identifier}</div>
                        <% } %>
                    </div>

                    <div class="row identifiers">
                        <h3>${ui.message("coreapps.mergePatients.section.extraIdentifiers")}</h3>
                        <%=(!patient1.extraIdentifiers ? ui.message("coreapps.none") : "")%>
                        <% patient1.extraIdentifiers.each { %>
                        <div>
                            <% if (it.preferred) { %> <b><% } %>
                        ${ui.format(it.identifierType)}
                        ${it.identifier}
                        <% if (it.preferred) { %></b> <% } %>
                        </div>
                        <% } %>
                    </div>

                    <div class="row address">
                        <h3>${ui.message("coreapps.mergePatients.section.addresses")}</h3>
                        <% patient1.patient.addresses.each { %>
                        <div><%=ui.includeFragment("coreapps", "formatAddress", [address: it])%></div>
                        <% } %>
                    </div>

                    <div class="row">
                        <h3>${ui.message("coreapps.mergePatients.section.lastSeen")}</h3>

                        <% if (patient1.lastEncounter) { %>
                            <div>${ui.format(patient1.lastEncounter.encounterType)}</div>

                            <div>${ui.message("coreapps.atLocation", ui.format(patient1.lastEncounter.location))}</div>

                            <div>${ui.message("emr.onDatetime", ui.format(patient1.lastEncounter.encounterDatetime))}</div>
                        <% } else { %>
                            ${ ui.message("emr.none") }
                        <% } %>

                    </div>

                    <div class="row">
                        <h3>${ui.message("coreapps.mergePatients.section.activeVisit")}</h3>
                        <% def activeVisit = patient1.getActiveVisit(sessionContext.getSessionLocation())
                        activeVisit = (activeVisit ? ui.format(activeVisit.visit) : ui.message("coreapps.none")) %>
                        <div>${activeVisit}</div>
                    </div>

                    <div class="row">
                        <h3>${ui.message("coreapps.mergePatients.section.dataSummary")}</h3>

                        <div>${ui.message("coreapps.mergePatients.section.dataSummary.numVisits", patient1.countOfVisits)}</div>

                        <div>${ui.message("coreapps.mergePatients.section.dataSummary.numEncounters", patient1.countOfEncounters)}</div>
                    </div>
                </div>
            </div>
             <div class="col-separator">
                <div id="separator" class="separator center">
                    <% def separatorClass = (isUnknownPatient ? "" : "hidden") %>
                    <div id="separator-right" class="${separatorClass}"><i class="icon-arrow-right"></i></div>

                    <div id="separator-left" class="hidden"><i class="icon-arrow-left"></i></div>
                </div>
             </div>
                <% def patientClass = (isUnknownPatient ? "patient selected" : "patient") %>
             <div class="col">
                <div id="second-patient" class="${patientClass} center">
                    <div class="row name">
                        <h3>${ui.message("coreapps.mergePatients.section.names")}</h3>
                        <% patient2.patient.names.each { %>
                        <div>${ui.encodeHtml(it.familyName)}, ${ui.encodeHtml(it.givenName)}</div>
                        <% } %>
                    </div>

                    <div class="row">
                        <h3>${ui.message("coreapps.mergePatients.section.demographics")}</h3>

                        <div>${ui.message("coreapps.gender." + ui.encodeHtml(patient2.patient.gender))}${patient2.patient.age ? ", " + ui.message("coreapps.ageYears", patient2.patient.age) : ""}</div>

                        <div>${ui.message("coreapps.birthdate")}:${ui.format(patient2.patient.birthdate)}</div>
                    </div>

                    <div class="row identifiers">
                        <h3>${ui.message("coreapps.mergePatients.section.primaryIdentifiers")}</h3>
                        <% patient2.primaryIdentifiers.each { %>
                        <% def identifier = (it.preferred ? "<b>${it.identifier}</b>" : it.identifier) %>
                        <div>${identifier}</div>
                        <% } %>
                    </div>

                    <div class="row identifiers">
                        <h3>${ui.message("coreapps.mergePatients.section.extraIdentifiers")}</h3>
                        <%=(!patient2.extraIdentifiers ? ui.message("coreapps.none") : "")%>
                        <% patient2.extraIdentifiers.each { %>
                        <div>
                            <% if (it.preferred) { %> <b><% } %>
                        ${ui.format(it.identifierType)}
                        ${it.identifier}
                        <% if (it.preferred) { %></b> <% } %>
                        </div>
                        <% } %>
                    </div>

                    <div class="row address">
                        <h3>${ui.message("coreapps.mergePatients.section.addresses")}</h3>
                        <% patient2.patient.addresses.each { %>
                        <div><%=ui.includeFragment("coreapps", "formatAddress", [address: it])%></div>
                        <% } %>
                    </div>

                    <div class="row">
                        <h3>${ui.message("coreapps.mergePatients.section.lastSeen")}</h3>

                        <% if (patient2.lastEncounter) { %>
                            <div>${ui.format(patient2.lastEncounter.encounterType)}</div>

                            <div>${ui.message("coreapps.atLocation", ui.format(patient2.lastEncounter.location))}</div>

                            <div>${ui.message("coreapps.onDatetime", ui.format(patient2.lastEncounter.encounterDatetime))}</div>
                        <% } else { %>
                            ${ ui.message("coreapps.none") }
                        <% } %>
                    </div>

                    <div class="row">
                        <h3>${ui.message("coreapps.mergePatients.section.activeVisit")}</h3>
                        <% activeVisit = patient2.getActiveVisit(sessionContext.getSessionLocation())
                        activeVisit = (activeVisit ? ui.format(activeVisit.visit) : ui.message("emr.none")) %>
                        <div>${activeVisit}</div>
                    </div>

                    <div class="row">
                        <h3>${ui.message("coreapps.mergePatients.section.dataSummary")}</h3>

                        <div>${ui.message("coreapps.mergePatients.section.dataSummary.numVisits", patient2.countOfVisits)}</div>

                        <div>${ui.message("coreapps.mergePatients.section.dataSummary.numEncounters", patient2.countOfEncounters)}</div>
                    </div>
                </div>
            </div>
        </div>
    </div>


    <div class="messages-container">
        <h1>
            ${ui.message("coreapps.mergePatients.confirmationSubtext")}  <br/>
            ${ui.message("coreapps.mergePatients.checkRecords")}
            <% if (overlappingVisits) { %>
            <em>${ui.message("coreapps.mergePatients.overlappingVisitsWillBeJoined")}</em>
            <% } %>
        </h1>
    </div>

    <% def buttonClass = (isUnknownPatient ? "button confirm" : "button disabled") %>
    <% def disabledOption = (isUnknownPatient ? "" : "disabled=\"disabled\" ") %>

    <div class="buttons">
        <input type="button" id="cancel-button" class="button cancel" value="${ui.message("emr.no")}"/>
        <input type="submit" id="confirm-button" class="${buttonClass}" ${disabledOption}
               value="${ui.message("emr.yesContinue")}"/>
    </div>

</form>