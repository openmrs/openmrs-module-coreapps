(function () {
    'use strict';

    angular.module('app.commonFunctionsFactory', []).service('CommonFunctions', CommonFunctions);

    CommonFunctions.$inject = ['$filter'];

    function CommonFunctions($filter) {
        var format = 'yyyy-MM-dd';

        return {
            extractUrlArgs: extractUrlArgs,
            formatDate: formatDate
        };

        function extractUrlArgs(urlArgs) {
            var urlParams = {};
            var args = urlArgs.replace(/^\?/, '');
            if (args.indexOf("&") > 0) {
                var params = args.split("&");
                for (var i = 0; i < params.length; i++) {
                    var paramArgs = params[i].split("=", 2);

                    if (paramArgs && Array.isArray(paramArgs) && paramArgs.length === 2) {
                        var key = paramArgs[0];
                        if (key in urlParams) {
                            var val = urlParams[key];
                            if (Array.isArray(val)) {
                                val.push(paramArgs[1]);
                            } else {
                                val = [val];
                            }
                            urlParams[key] = val;
                        } else {
                            urlParams[key] = paramArgs[1];
                        }
                    } else {
                        urlParams[params[i]] = params[i];
                    }
                }
            }

            return urlParams;
        }

        function formatDate(date) {
            if (typeof date !== 'undefined' && date !== null) {
                return ($filter('date')(new Date(date), format));
            }
        }
    }
})();
