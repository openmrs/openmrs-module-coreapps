(function() {
    'use strict';

    var baseModel;
    try {
        baseModel = angular.module('app.models');
    } catch(err) {
        baseModel = angular.module('app.models', []);
    }

    function ConceptModel() {

        function ConceptModel(uuid, name) {
            this.uuid = uuid;
            this.name = name;
            this.shortName = '';
        }

        ConceptModel.prototype = {

            getUuid: function() {
                return this.uuid;
            },

            setUuid: function(uuid) {
                this.uuid = uuid;
            },

            getName: function() {
                return this.name;
            },

            setName: function(name) {
                this.name = name;
            },

            getShortName: function() {
                return this.shortName;
            },

            setShortName: function(shortName) {
                this.shortName = shortName;
            }
        };

        return ConceptModel;
    }

    baseModel.factory("ConceptModel", ConceptModel);
    ConceptModel.$inject = [];
})();
