function loadTemplates (visitId) {
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

            $('.status-container a').click(function() {
                showEditVisitDateDialog($(this).data('visit-id'));
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