function loadTemplates () {
    function loadVisit(visitElement) {
        var localVisitId = visitElement.attr('visitId');
        if (visitElement != null &&  localVisitId!= undefined) {
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
    }
 
    var visitDetailsTemplate = _.template($('#visitDetailsTemplate').html());
    var visitsSection = $("#visits-list");

    var visitDetailsSection = $("#visit-details");

    //load first visit
    loadVisit($('.viewVisitDetails').first());

    $('.viewVisitDetails').click(function() {
        loadVisit($(this));
        return false;
    });
};