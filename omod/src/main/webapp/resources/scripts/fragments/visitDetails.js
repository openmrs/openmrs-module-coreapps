function loadTemplates (visitId, patientId, fromEncounter, encounterCount) {
    function loadVisit(visitElement) {
        var localVisitId = visitElement.data('visit-id');

        visitDetailsSection.html("<i class=\"icon-spinner icon-spin icon-2x pull-left\"></i>");
        $.getJSON(
            emr.fragmentActionLink("coreapps", "visit/visitDetails", "getVisitDetails", {
                visitId: localVisitId,
                fromEncounter: fromEncounter,
                encounterCount: encounterCount
            })
            ).success(function(data) {
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
                
            }).error(function(err) {
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
