angular.module('relationships', ['relationshipTypeService', 'relationshipService', 'personService', 'uicommons.widget.select-person', 'ngDialog' ]).

    controller('PersonRelationshipsCtrl', ['$scope', 'RelationshipTypeService', 'RelationshipService', 'PersonService', 'ngDialog',
    function($scope, RelationshipTypeService, RelationshipService, PersonService, ngDialog) {

        $scope.thisPersonUuid = null;

        $scope.relationshipTypes = [];

        $scope.relationships = [];

        $scope.init = function(personUuid, excludeRelationshipTypes) {
            $scope.thisPersonUuid = personUuid;
            RelationshipService.getRelationships({ v: 'default', person: personUuid }).then(function(result) {
                $scope.relationships = result;
            });
            RelationshipTypeService.getRelationshipTypes({ v: 'default' }).then(function(result) {
                if (excludeRelationshipTypes) {
                    result = _.reject(result, function(item) {
                        return _.contains(excludeRelationshipTypes, item.uuid);
                    });
                }
                $scope.relationshipTypes = result;
            });
        }

        // since we aren't guaranteed that a relationship has a full rep of its relationshipType, this will get our full one
        $scope.relType = function(relationship) {
            if (!relationship) {
                return null;
            }
            return _.findWhere($scope.relationshipTypes, { uuid: relationship.relationshipType.uuid });
        }

        $scope.relationshipsByType = function(relationshipType, whichSide) {
            return _.filter($scope.relationships, function(item) {
                if (item.relationshipType.uuid != relationshipType.uuid) {
                    return false;
                }
                if (whichSide == 'A' && item.personB.uuid == $scope.thisPersonUuid) {
                    return true;
                } else if (whichSide == 'B' && item.personA.uuid == $scope.thisPersonUuid) {
                    return true;
                }
            });
        }

        $scope.showAddDialog = function(relationshipType, whichSide) {
            ngDialog.openConfirm({
                showClose: false,
                closeByEscape: true,
                closeByDocument: true,
                data: angular.toJson({
                    otherLabel: whichSide == 'A' ? relationshipType.aIsToB : relationshipType.bIsToA,
                    thisLabel: whichSide == 'A' ? relationshipType.bIsToA : relationshipType.aIsToB
                }),
                template: 'addDialogTemplate'
            }).then(function(otherPerson) {
                var relationship = {
                    relationshipType: relationshipType.uuid,
                    personA: whichSide == 'A' ? otherPerson.uuid : $scope.thisPersonUuid,
                    personB: whichSide == 'A' ? $scope.thisPersonUuid : otherPerson.uuid
                };
                var created = RelationshipService.createRelationship(relationship);
                $scope.relationships.push(created);
            });
            angular.element('#select-other-person-input').focus();
        }

        $scope.showDeleteDialog = function(relationship) {
            ngDialog.openConfirm({
                showClose: false,
                closeByEscape: true,
                closeByDocument: true,
                template: 'deleteDialogTemplate',
                data: angular.toJson({ relationship: relationship }),
                scope: $scope
            }).then(function(relationshipToDelete) {
                RelationshipService.deleteRelationship(relationship);
                $scope.relationships = _.reject($scope.relationships, function(item) {
                    return item.uuid == relationship.uuid;
                });
            });
        }

        $scope.goToPerson = function(patientOrPerson) {
            emr.navigateTo({
                provider: "coreapps",
                page: "clinicianfacing/patient",
                query: { patientId: patientOrPerson.uuid }
            })
        }

    }]);