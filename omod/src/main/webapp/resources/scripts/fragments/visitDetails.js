function loadTemplates (visitId, patientId, fromEncounter, encounterCount, currentUserId) {
    function loadVisit(visitElement) {
        var localVisitId = visitElement.data('visit-id');

        visitDetailsSection.html("<i class=\"icon-spinner icon-spin icon-2x pull-left\"></i>");
        jq.getJSON(
            emr.fragmentActionLink("coreapps", "visit/visitDetails", "getVisitDetails", {
                visitId: localVisitId,
                fromEncounter: fromEncounter,
                encounterCount: encounterCount,
                userId: currentUserId
            })
            ).done(function(data) {
                $('.viewVisitDetails').removeClass('selected');
                visitElement.addClass('selected');
                visitDetailsSection.html(visitDetailsTemplate(data));
                visitDetailsSection.show();

                $('#editVisitDatesLink').click(function() {
                    visit.showEditVisitDateDialog($(this).data('visit-id'));
                    return false;
                });
                $('#deleteVisitLink').click(function() {
                    createDeleteVisitDialog($(this).data('visit-id'), patientId);
                    showDeleteVisitDialog($(this).data('visit-id'));
                    return false;
                });
                $('#editVisitLink').click(function() {
                    visit.showEditVisitDialog($(this).data('visit-id'));
                    return false;
                });

                if (fromEncounter > 0) {    // Showing the 'previous' button
                    $('#visit-paging-buttons').css('visibility', 'visible');
                    $('#visit-paging-button-prev').css('visibility', 'visible');
                    $('#visit-paging-button-prev').click(function() {
                        emr.navigateTo({
                            provider: "coreapps",
                            page: "patientdashboard/patientDashboard",
                            query: {
                                visitId: visitId,
                                patientId: patientId,
                                fromEncounter: Math.max(0, parseInt(fromEncounter) - parseInt(encounterCount)),
                                encounterCount: parseInt(encounterCount)
                            }
                        });
                    });
                }
                if (data.encounters.length >= encounterCount) {  // Showing the 'next' button
                    $('#visit-paging-buttons').css('visibility', 'visible');
                    $('#visit-paging-button-next').css('visibility', 'visible');
                    $('#visit-paging-button-next').click(function() {
                        emr.navigateTo({
                            provider: "coreapps",
                            page: "patientdashboard/patientDashboard",
                            query: {
                                visitId: visitId,
                                patientId: patientId,
                                fromEncounter: parseInt(fromEncounter) + parseInt(encounterCount),
                                encounterCount: parseInt(encounterCount)
                            }
                        });
                    });
                }

            //If find rfc3339-date class, convert from UTC to client Timezone
            jq(".encounter-date.rfc3339-date").each(function() {
                var datetimeInUTC = (jq(this).find(".encounter-datetime").text()).trim();
                jq(this).find(".encounter-time").text(formatTime(new Date(datetimeInUTC), window.patientDashboard.timeFormat));
                jq(this).find(".encounter-datetime").text(function () {
                    return jq(this).text().replace(datetimeInUTC, formatDatetime(new Date(datetimeInUTC), window.patientDashboard.datetimeFormat, window.visitinclude.locale));
                });
            });
            }).fail(function(err) {
                emr.errorMessage(err);
            });
        }

        var visitDetailsTemplate = _.template($('#visitDetailsTemplate').html(), null, {
            interpolate : /\[\[=(.+?)\]\]/g ,
            escape : /\[\[-(.+?)\]\]/g ,
            evaluate : /\[\[(.+?)\]\]/g
        });
        var visitsSection = $("#visits-list");

        var visitDetailsSection = $("#visit-details");

        if (visitId) {
            visitElement = $('.viewVisitDetails[data-visit-id=' + visitId + ']');
        //load provided visit
        loadVisit(visitElement);
    }

    $('.viewVisitDetails').click(function() {
        loadVisit($(this));
        return false;
    });
};
