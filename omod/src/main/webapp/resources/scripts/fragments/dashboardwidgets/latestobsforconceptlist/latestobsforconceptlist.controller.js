//Determine current script path
var scripts = document.getElementsByTagName("script");
var latestobsforconceptlistPath = scripts[scripts.length - 1].src;

function LatestObsForConceptListController($scope) {
    var ctrl = this;

    $scope.getTemplate = function () {
        return latestobsforconceptlistPath.replace(".controller.js", ".html");
    };
}
