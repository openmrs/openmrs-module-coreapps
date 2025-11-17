angular.module('att.service.configService').factory('ConfigService', [ 'ModuleUtils', '$q', function(module, $q) {
    var getConfig = function() {
        var deferred = $q.defer();
        if (module.clientConfig) {
            deferred.resolve(module.clientConfig);
        } else {
            emr.getFragmentActionWithCallback(module.getProvider(), "attachments/clientConfig", "get", {}, function(response) {
                deferred.resolve(response);
                module.clientConfig = response;
            });
        }
        return deferred.promise;
    };

    return {
        getConfig : getConfig
    };
} ]);