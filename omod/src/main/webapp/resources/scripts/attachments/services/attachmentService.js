angular.module('att.service.attachmentService').factory('Attachment', function($resource) {
    return $resource("/" + OPENMRS_CONTEXT_PATH + "/ws/rest/v1/attachment/:uuid", {
        uuid : '@uuid'
    }, {
        query : {
            method : 'GET',
            isArray : false
        }
    // OpenMRS RESTWS returns { "results": [] }
    });
});