// Sand-boxing widgets so that they use its own version of angular regardless of the version loaded globally.
// Borrowed from http://www.mattburkedev.com/multiple-angular-versions-on-the-same-page/
(function() {
    // Save a copy of the existing angular for later restoration.
    var existingWindowDotAngular = window.angular;

    // Create a new window.angular and a closure variable for
    // angular.js to load itself into.
    var angular = (window.angular = {});

    var dashboardWidgets = require('./dashboardwidgets');

    //Manually bootstrap with our angular version.
    angular.element(document).ready(function() {
        for (var dashboardwidget of document.getElementsByClassName('openmrs-contrib-dashboardwidgets')) {
            angular.bootstrap(dashboardwidget, [ 'openmrs-contrib-dashboardwidgets' ]);
        }

        // Restore the old angular version.
        window.angular = existingWindowDotAngular;
    });
})();