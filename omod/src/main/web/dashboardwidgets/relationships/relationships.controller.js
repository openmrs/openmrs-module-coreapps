import angular from 'angular';

export default class RelationshipsController  {
    constructor(openmrsRest, openmrsTranslate) {
        'ngInject';

        Object.assign(this, { openmrsRest, openmrsTranslate });
    }

    $onInit() {
        this.minSearchLength = 2;
        // the default patient page is the clinician dashboard
        this.dashboardPage = "/coreapps/clinicianfacing/patient.page?patientId={{patientUuid}}";

        this.relationships = [];
        this.types = [];
        this.allowedTypes = [];
        this.relationshipType = [];
        this.hasEditPrivileges = false;
        this.edit = false;
        this.removeFlag = false;
        this.showFindOtherPerson = false;
        this.showSaveButton = false;
        this.searchPerson = null;
        this.otherPerson = null;
        this.relatedPersons = [];

        if (this.config.baseAppPath) {
            this.openmrsRest.setBaseAppPath(this.config.baseAppPath);
        } else {
            this.openmrsRest.setBaseAppPath("/coreapps");
        }

        if( this.config.dashboardPage ) {
            this.dashboardPage = this.config.dashboardPage;
        }

        if (this.config.includeRelationshipTypes) {
            this.allowedTypes = this.config.includeRelationshipTypes.split(',');
        }

        //fetchPrivileges
        this.openmrsRest.get('session', {
            v: 'custom:(privileges)'
        }).then((response) => {
            this.getPrivileges(response.user.privileges);
        });

        //fetchRelationships
        this.openmrsRest.get('relationship', {
            person: this.config.patientUuid,
            limit: this.getMaxRecords(),
            v: 'custom:(uuid,personA:(uuid,display,birthdate,isPatient),personB:(uuid,display,birthdate,isPatient),relationshipType:(uuid,aIsToB,bIsToA))'
        }).then((response) => {
            this.getRelationships(response.results);
        });

        //fetchRelationshipTypes
        this.openmrsRest.get('relationshiptype', {
            v: 'custom:(uuid,aIsToB,bIsToA)'
        }).then((response) => {
            this.getRelationshipTypes(response.results);
        });
    }

    getRelationships(relationships) {
        angular.forEach(relationships, (relationship) => {
            var rel = {};
            rel.uuid = relationship.uuid;
            if(relationship.personA.uuid !== this.config.patientUuid){
                rel.toPerson = relationship.personA;
                rel.isPatient = relationship.personA.isPatient;
                rel.type = relationship.relationshipType.aIsToB;
            } else {
                rel.toPerson = relationship.personB;
                rel.isPatient = relationship.personB.isPatient;
                rel.type = relationship.relationshipType.bIsToA;
            }
            this.relationships.push(rel);
        })
    }

    relationshipsContain(personUuid) {
        var result = false;
        angular.forEach(this.relationships, (relationship) => {
            if (relationship.toPerson.uuid == personUuid) {
                result = true;
            }
        });
        return result;
    }

    getPrivileges(privileges) {
        var editPrivilege = this.config.editPrivilege;
        if( angular.isDefined(editPrivilege) ) {
            if (angular.isArray(privileges)) {
                if (privileges.some(function(privilege) { return privilege.name === editPrivilege; })) {
                    this.hasEditPrivileges = true;
                };
            }
        }
    }

    getPersons(searchString) {
        this.openmrsRest.get('patient', {
            q: searchString,
            v: 'custom:(uuid,display,gender,age)'
        }).then((response) => {
            this.relatedPersons = [];
            this.filterSearchedPersons(response.results);
        });
    }

    filterSearchedPersons(searchedPersons) {
        if (angular.isArray(searchedPersons) && searchedPersons.length > 0) {
            angular.forEach(searchedPersons, (person) => {
                if( !this.relationshipsContain( person.uuid ) ) {
                    // when searching for persons to create new relationships exclude the persons that already have a relationship with this person
                    this.relatedPersons.push(person);
                }
            });
        }
    }

    getRelationshipTypes(types) {
        angular.forEach(types, (type) => {
            if ((this.allowedTypes.length < 1) ||
                ((this.allowedTypes.length > 0) && (this.allowedTypes.indexOf(type.uuid) !== -1))) {
                // if a relationship type filter was not specified then we display all types,
                // OR if a filter was defined then we only display the types included in the filter
                if (this.findRelTypeByName(type.aIsToB) == null) {
                    var relTypeA = {};
                    relTypeA.uuid = type.uuid;
                    relTypeA.name = type.aIsToB;
                    relTypeA.type = "B";
                    this.types.push(relTypeA);
                }
                if (this.findRelTypeByName(type.bIsToA) == null) {
                    var relTypeB = {};
                    relTypeB.uuid = type.uuid;
                    relTypeB.name = type.bIsToA;
                    relTypeB.type = "A";
                    this.types.push(relTypeB);
                }
            }
        });
    }

    updateRelationshipType() {
        if (angular.isDefined(this.relationshipType)) {
            this.showFindOtherPerson = true;
        }
    }

    findRelTypeByName(value) {
        var item = null;
        if (angular.isDefined(this.types) && this.types.length > 0) {
            angular.forEach(this.types, (type) => {
                if (type.name == value) {
                    item = type;
                }
            });
        }
        return item;
    }

    getMaxRecords() {
        if(this.config.maxRecords == '' || angular.isUndefined(this.config.maxRecords)){
            return 6;
        } else {
            return this.config.maxRecords;
        }
    }
    
    navigateTo(patientId) {
        var destinationPage ="";
        if (this.dashboardPage) {
            destinationPage = Handlebars.compile(this.dashboardPage)({
                patientUuid: patientId
            });
        }
        this.openmrsRest.getServerUrl().then((url) => {
            window.location.href = url + destinationPage;
        });

    }

    addRelationship() {
        this.edit = true;
    }

    save() {
        if (angular.isDefined(this.relationshipType) &&
            angular.isDefined(this.otherPerson)) {

            var personA = null;
            var personB = null;
            if (this.relationshipType.type == "A") {
                personA = this.config.patientUuid;
                personB = this.otherPerson.uuid;
            } else if (this.relationshipType.type == "B") {
                personA = this.otherPerson.uuid;
                personB = this.config.patientUuid;
            }

            this.openmrsRest.create('relationship', {
                relationshipType: this.relationshipType.uuid,
                personA: personA,
                personB: personB
            }).then((response) => {
                this.$onInit();
            });
        }
    }

    removeRelationship(relUuid) {
        if (angular.isDefined(relUuid)) {

            this.removeFlag = true;
            var tempRelationships = [];
            angular.forEach(this.relationships, (relationship) => {
                if (relationship.uuid == relUuid) {
                    tempRelationships.push(relationship);
                }
            });
            this.relationships = [];
            this.relationships = tempRelationships;
        }
    }
    
    remove() {
        if (angular.isDefined(this.relationships) && (this.relationships.length == 1) ){
            //we only allow to delete one relationship at the time
            this.openmrsRest.remove('relationship', {
                 uuid: this.relationships[0].uuid
             }).then((response) => {
                 this.$onInit();
            });
        }
    }

    goTo(patientId) {
        this.navigateTo(patientId);
    }

    searchPersons() {
        if (angular.isDefined(this.searchPerson) &&
            (this.searchPerson.length > this.minSearchLength) ) {
            this.getPersons(this.searchPerson);
        }
    }

    onSelect($item, $model, $label) {
        if (angular.isDefined($item) && angular.isDefined(this.relationshipType)) {
            this.otherPerson = $item;
            this.showSaveButton = true;
        }
    }

    cancel() {
        this.edit = false;
        this.$onInit();
    }
};
