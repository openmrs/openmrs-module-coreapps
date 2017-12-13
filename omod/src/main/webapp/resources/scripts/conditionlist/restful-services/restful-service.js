(function () {
    'use strict';
    var app = angular.module('app.restfulServices', ['restangular']);

    app.service('RestfulService', RestfulService);

    RestfulService.$inject = ['Restangular', '$filter'];

    function RestfulService(Restangular, $filter) {
        var service;

        service = {
            setBaseUrl: setBaseUrl,
            get: get,
            post: post
        };

        return service;

        function setBaseUrl(restWsUrl) {
            if (!angular.isUndefined(restWsUrl)) {
                Restangular.setBaseUrl(restWsUrl);
            }
        }

        function get(resource, request, successCallback, errorCallback) {
            Restangular.all(resource).customGET('', request).then(function (data) {
                if (typeof successCallback === 'function') {
                    successCallback(data);
                }
            }, function (error) {
                if (typeof errorCallback === 'function') {
                    errorCallback(error);
                }
            });
        }

        function post(resource, request, successCallback, errorCallback) {
            var params = JSON.stringify(request);

            Restangular.one(resource).customPOST(params).then(function (data) {
                if (typeof successCallback === 'function') successCallback(data);
            }, function (error) {
                if (typeof errorCallback === 'function') {
                    errorCallback(error);
                }
            });
        }
    }
})();