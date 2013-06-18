function loadTemplates (visitId) {
    function loadVisit(visitElement) {
        var localVisitId = visitElement.attr('visitId');
        visitDetailsSection.html("<i class=\"icon-spinner icon-spin icon-2x pull-left\"></i>");
        $.getJSON(
            emr.fragmentActionLink("emr", "visit/visitDetails", "getVisitDetails", {
                visitId: localVisitId
            })
        ).success(function(data) {
            $('.viewVisitDetails').removeClass('selected');
            visitElement.addClass('selected');
            visitDetailsSection.html(visitDetailsTemplate(data));
            visitDetailsSection.show();
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

    if (visitId || !emr.isFeatureEnabled('noActiveVisitView')) {
        visitElement = $('.viewVisitDetails[visitId=' + visitId + ']');
        //load provided visit
        loadVisit(visitElement);
    }

    $('.viewVisitDetails').click(function() {
        loadVisit($(this));
        return false;
    });
};