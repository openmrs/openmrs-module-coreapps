(function () {
    'use strict';

    var app = angular.module('app.commonFunctionsFactory', []);
    app.service('CommonFunctions', CommonFunctions);

    CommonFunctions.$inject = ['$filter'];

    function CommonFunctions($filter) {
        var service;
        service = {
            extractUrlArgs: extractUrlArgs,
            formatDate: formatDate,
            strikeThrough: strikeThrough
        };

        function extractUrlArgs(urlArgs) {
            var urlParams = [];
            urlArgs = urlArgs.replace('?', '');
            if (urlArgs.indexOf("&") > 0) {
                var params = urlArgs.split("&");
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    var paramArgs = param.split("=");
                    urlParams[paramArgs[0]] = paramArgs[1];
                }
            }

            return urlParams;
        }

        function formatDate(date) {
            if (date !== null) {
                var format = 'yyyy-MM-dd';
                return ($filter('date')(new Date(date), format));
            }
        }

        function strikeThrough(apply) {
            if (apply) {
                return {
                    "text-decoration": "line-through"
                };
            } else {
                return {};
            }
        }

        return service;
    }
})();