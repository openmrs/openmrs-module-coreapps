// angular.lowercase/uppercase were removed in 1.8.x but are still used by angular-translate
var angular = require('angular');
if (!angular.lowercase) {
    angular.lowercase = function(s) { return s.toLowerCase(); };
    angular.uppercase = function(s) { return s.toUpperCase(); };
}

var context = require.context('.', true, /.spec\.js$/);
context.keys().forEach(context);