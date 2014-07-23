angular.module('relationships', ['relationshipTypeService', 'relationshipService', 'personService', 'uicommons.widget.select-person' ]).

    controller('PersonRelationshipsCtrl', ['$scope', 'RelationshipTypeService', 'RelationshipService', 'PersonService', '$modal',
    function($scope, RelationshipTypeService, RelationshipService, PersonService, $modal) {

        $scope.thisPersonUuid = null;

        $scope.relationshipTypes = [];

        $scope.relationships = [];

        $scope.init = function(personUuid) {
            $scope.thisPersonUuid = personUuid;
            RelationshipService.getRelationships({ v: 'default', person: personUuid }).then(function(result) {
                $scope.relationships = result;
            });
            RelationshipTypeService.getRelationshipTypes({ v: 'default' }).then(function(result) {
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

        $scope.addDialogMode = null;

        $scope.addDialogThisLabel = '';
        $scope.addDialogOtherLabel = '';
        $scope.otherPerson = null;

        $scope.showAddDialog = function(relationshipType, whichSide) {
            $scope.otherPerson = null;
            $scope.addDialogMode = {
                relationshipType: relationshipType,
                whichSide: whichSide
            };
            $scope.addDialogOtherLabel = whichSide == 'A' ? relationshipType.aIsToB : relationshipType.bIsToA;
            $scope.addDialogThisLabel = whichSide == 'A' ? relationshipType.bIsToA : relationshipType.aIsToB;
            $('#select-other-person').focus();
        }

        $scope.doAddRelationship = function() {
            var relationship = {
                relationshipType: $scope.addDialogMode.relationshipType.uuid,
                personA: $scope.addDialogMode.whichSide == 'A' ? $scope.otherPerson.uuid : $scope.thisPersonUuid,
                personB: $scope.addDialogMode.whichSide == 'A' ? $scope.thisPersonUuid : $scope.otherPerson.uuid
            };
            var created = RelationshipService.createRelationship(relationship);
            $scope.relationships.push(created);
            $scope.otherPerson = null;
            $scope.addDialogMode = null;
        }

        $scope.cancelAddRelationship = function() {
            $scope.otherPerson = null;
            $scope.addDialogMode = null;
        }

        $scope.relationshipToDelete = null;

        $scope.showDeleteDialog = function(relationship) {
            $scope.relationshipToDelete = relationship;
        }

        $scope.doDeleteRelationship = function(relationship) {
            RelationshipService.deleteRelationship(relationship);
            $scope.relationships = _.reject($scope.relationships, function(item) {
                return item.uuid == relationship.uuid;
            });
            $scope.relationshipToDelete = null;
        }

        $scope.cancelDeleteRelationship = function(relationship) {
            $scope.relationshipToDelete = null;
        }
    }]);