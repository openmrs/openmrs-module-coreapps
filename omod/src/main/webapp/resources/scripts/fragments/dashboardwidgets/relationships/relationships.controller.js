//Determine current script path
var scripts = document.getElementsByTagName("script");
var relationshipsPath = scripts[scripts.length - 1].src;

function RelationshipsController(openmrsRest, $scope) {
    var ctrl = this;

    // the default patient page is the clinician dashboard
    ctrl.patientPage = "/coreapps/clinicianfacing/patient.page?patientId={{patientUuid}}";

    ctrl.relationships = [];

    activate();

    function activate() {
        if ( ctrl.config.baseAppPath ) {
            openmrsRest.setBaseAppPath(ctrl.config.baseAppPath);
        } else {
            openmrsRest.setBaseAppPath("/coreapps");
        }

        if( ctrl.config.patientPage ) {
            ctrl.patientPage = ctrl.config.patientPage;
        }
        fetchRelationships();
    }

    function fetchRelationships() {
        openmrsRest.get('relationship', {
            person: ctrl.config.patientUuid,
            limit: getMaxRecords(),
            v: 'custom:(uuid,personA:(uuid,person:(display),birthdate),personB:(uuid,person:(display),birthdate),relationshipType:(uuid,aIsToB,bIsToA))'
        }).then(function (response) {
                getRelationships(response.results);
        })
    }

    function getRelationships(relationships) {
        angular.forEach(relationships, function (relationship) {
            var rel = {};
            if(relationship.personA.uuid !== ctrl.config.patientUuid){
                rel.toPerson = relationship.personA;
                rel.type = relationship.relationshipType.aIsToB;
            } else {
                rel.toPerson = relationship.personB;
                rel.type = relationship.relationshipType.bIsToA;
            }
            ctrl.relationships.push(rel);
        })
    }

    function getMaxRecords() {
        if(ctrl.config.maxRecords == '' || angular.isUndefined(ctrl.config.maxRecords)){
            return 6;
        } else {
            return ctrl.config.maxRecords;
        }
    }
    
    function navigateTo(patientId) {
        var destinationPage ="";
        if (ctrl.patientPage) {
            destinationPage = Handlebars.compile(ctrl.patientPage)({
                patientUuid: patientId
            });
        }
        openmrsRest.getServerUrl().then(function(url) {
            window.location.href = url + destinationPage;
        });

    }

    ctrl.goTo = function(patientId) {
        navigateTo(patientId);
    }

    $scope.getTemplate = function () {
        return relationshipsPath.replace(".controller.js", ".html");
    };
}
