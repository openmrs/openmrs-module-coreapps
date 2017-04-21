//Determine current script path
var scripts = document.getElementsByTagName("script");
var relationshipsPath = scripts[scripts.length - 1].src;

function RelationshipsController(openmrsRest, $scope) {
    var ctrl = this;

    ctrl.minSearchLength = 2;
    // the default patient page is the clinician dashboard
    ctrl.patientPage = "/coreapps/clinicianfacing/patient.page?patientId={{patientUuid}}";

    initialize();
    activate();

    function initialize() {
        ctrl.relationships = [];
        ctrl.types = [];
        ctrl.relationshipType = [];
        ctrl.hasEditPrivileges = false;
        ctrl.edit = false;
        ctrl.removeFlag = false;
        ctrl.showFindOtherPerson = false;
        ctrl.showSaveButton = false;
        ctrl.searchPerson = null;
        ctrl.otherPerson = null;
        ctrl.relatedPersons = [];
    }

    function activate() {
        if ( ctrl.config.baseAppPath ) {
            openmrsRest.setBaseAppPath(ctrl.config.baseAppPath);
        } else {
            openmrsRest.setBaseAppPath("/coreapps");
        }

        if( ctrl.config.patientPage ) {
            ctrl.patientPage = ctrl.config.patientPage;
        }
        fetchPrivileges();
        fetchRelationships();
        fetchRelationshipTypes();
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
            rel.uuid = relationship.uuid;
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


    function fetchRelationshipTypes() {
        openmrsRest.get('relationshiptype', {
            v: 'custom:(uuid,aIsToB,bIsToA)'
        }).then(function (response) {
            getRelationshipTypes(response.results);
        })
    }

    function fetchPrivileges() {
        openmrsRest.get('session', {
            v: 'custom:(privileges)'
        }).then(function (response) {
            getPrivileges(response.user.privileges);
        })
    }

    function getPrivileges(privileges) {
        var editPrivilege = ctrl.config.editPrivilege;
        if( angular.isDefined(editPrivilege) ) {
            if (angular.isArray(privileges)) {
                if (privileges.some(function(privilege) { return privilege.name === editPrivilege; })) {
                    ctrl.hasEditPrivileges = true;
                };
            }
        }
    }

    function getPersons(searchString) {
        openmrsRest.get('person', {
            q: searchString,
            v: 'custom:(uuid,display,gender,age)'
        }).then(function(response) {
            ctrl.relatedPersons = response.results;
        });
    }

    function getRelationshipTypes(types) {
        angular.forEach(types, function (type) {
            if (findRelTypeByName(type.aIsToB) == null) {
                var relTypeA = {};
                relTypeA.uuid = type.uuid;
                relTypeA.name = type.aIsToB;
                relTypeA.type = "B";
                ctrl.types.push(relTypeA);
            }
            if (findRelTypeByName(type.bIsToA) == null) {
                var relTypeB = {};
                relTypeB.uuid = type.uuid;
                relTypeB.name = type.bIsToA;
                relTypeB.type = "A";
                ctrl.types.push(relTypeB);
            }
        });
    }

    function updateRelationshipType() {
        if (angular.isDefined(ctrl.relationshipType)) {
            ctrl.showFindOtherPerson = true;
        }
    }

    function findRelTypeByName(value) {
        var item = null;
        if (angular.isDefined(ctrl.types) && ctrl.types.length > 0) {
            angular.forEach(ctrl.types, function(type) {
                if (type.name == value) {
                    item = type;
                }
            });
        }
        return item;
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

    function addRelationship() {
        ctrl.edit = true;
    }

    function save() {
        if (angular.isDefined(ctrl.relationshipType) &&
            angular.isDefined(ctrl.otherPerson)) {

            var personA = null;
            var personB = null;
            if (ctrl.relationshipType.type == "A") {
                personA = ctrl.config.patientUuid;
                personB = ctrl.otherPerson.uuid;
            } else if (ctrl.relationshipType.type == "B") {
                personA = ctrl.otherPerson.uuid;
                personB = ctrl.config.patientUuid;
            }

            openmrsRest.create('relationship', {
                relationshipType: ctrl.relationshipType.uuid,
                personA: personA,
                personB: personB
            }).then(function (response) {
                initialize();
                activate();
            });
        }
    }

    function removeRelationship(relUuid) {
        if (angular.isDefined(relUuid)) {

            ctrl.removeFlag = true;
            var tempRelationships = [];
            angular.forEach(ctrl.relationships, function (relationship) {
                if (relationship.uuid == relUuid) {
                    tempRelationships.push(relationship);
                }
            });
            ctrl.relationships = [];
            ctrl.relationships = tempRelationships;
        }
    }
    
    function remove() {
        if (angular.isDefined(ctrl.relationships) && (ctrl.relationships.length == 1) ){
            //we only allow to delete one relationship at the time
            openmrsRest.remove('relationship', {
                 uuid: ctrl.relationships[0].uuid
             }).then(function (response) {
                 initialize();
                 activate();
            });
        }
    }

    ctrl.goTo = function(patientId) {
        navigateTo(patientId);
    }

    ctrl.removeRelationship = function(relUuid) {
        removeRelationship(relUuid);
    }

    ctrl.addRelationship = function() {
        addRelationship();
    }

    ctrl.updateRelationshipType = function() {
        updateRelationshipType();
    }

    ctrl.searchPersons = function() {
        if (angular.isDefined(ctrl.searchPerson) && (ctrl.searchPerson.length > ctrl.minSearchLength) ) {
            getPersons(ctrl.searchPerson);
        }
    }

    ctrl.onSelect = function($item, $model, $label) {
        if (angular.isDefined($item) && angular.isDefined(ctrl.relationshipType)) {
            ctrl.otherPerson = $item;
            ctrl.showSaveButton = true;
        }
    }

    ctrl.cancel = function() {
        ctrl.edit = false;
        initialize();
        activate();
    }

    ctrl.save = function() {
        save();
    }

    ctrl.remove = function() {
        remove();
    }

    $scope.getTemplate = function () {
        return relationshipsPath.replace(".controller.js", ".html");
    };
}
