(function () {
    'use strict';
    angular.module('app.restfulServices', []).service('RestfulService', RestfulService);

    RestfulService.$inject = ['$http'];

    function RestfulService($http) {
        var baseUrl;

        return {
            setBaseUrl: setBaseUrl,
            get: get,
            post: post
        };

        function setBaseUrl(restWsUrl) {
            if (restWsUrl) {
                baseUrl = restWsUrl;
            }
        }

        function get(resource, request, successCallback, errorCallback) {
            var url = baseUrl + '/' + resource;
            if (typeof request === 'object' && request !== null && Object.keys(request).length > 0) {
                url += '?' + Object.keys(request).map(function (key) {
                    return encodeURI(key) + '=' + encodeURI(request[key]);
                }).join('&');
            }

            return $http({
                method: 'GET',
                headers: { Accept: 'application/json' },
                url: url
            }).then(function (response) {
                if (typeof successCallback === 'function') {
                    successCallback(response.data);
                }
            }, function (response) {
                if (typeof errorCallback === 'function') {
                    errorCallback(response);
                }
            });
        }

        function post(resource, request, successCallback, errorCallback) {
            return $http({
                method: 'POST',
                headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
                url: baseUrl + '/' + resource,
                data: request
            }).then(function (response) {
                if (typeof successCallback === 'function') {
                    successCallback(response.data);
                }
            }, function (error) {
                if (typeof errorCallback === 'function') {
                    errorCallback(error);
                }
            });
        }
    }
})();
