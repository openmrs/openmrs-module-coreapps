function loadTemplates (visitId, patientId) {
    function loadVisit(visitElement) {
        var localVisitId = visitElement.data('visit-id');

        visitDetailsSection.html("<i class=\"icon-spinner icon-spin icon-2x pull-left\"></i>");
        $.getJSON(
            emr.fragmentActionLink("coreapps", "visit/visitDetails", "getVisitDetails", {
                visitId: localVisitId
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