(function() {
    'use strict';

    var baseModel;
    try {
        baseModel = angular.module('app.models');
    } catch(err) {
        baseModel = angular.module('app.models', []);
    }

    function ConditionModel(ConceptModel) {

        function ConditionModel(patientUuid) {
            this.status = 'ACTIVE';
            this.patientUuid = patientUuid;
            this.previousConditionUuid = '';
            this.concept = new ConceptModel('', '');
            this.conditionNonCoded = '';

            this.onSetDate = '';
            this.additionalDetail = '';
            this.endDate = '';
            this.endReason = new ConceptModel();
            this.voided = false;
        }

        ConditionModel.prototype = {

            getStatus: function() {
                return this.status;
            },

            setStatus: function(status) {
                this.status = status;
            },

            getPatientUuid: function() {
                return this.patientUuid;
            },

            setPatientUuid: function(patientUuid) {
                this.patientUuid = patientUuid;
            },

            getPreviousConditionUuid: function() {
                return this.previousConditionUuid;
            },

            setPreviousConditionUuid: function(previousConditionUuid) {
                this.previousConditionUuid = previousConditionUuid;
            },

            getConcept: function() {
                return this.concept;
            },

            setConcept: function(concept) {
                this.concept = concept;
            },

            setConditionNonCoded: function(conditionNonCoded) {
                this.conditionNonCoded = conditionNonCoded;
            },

            getConditionNonCoded: function() {
                return this.conditionNonCoded;
            },

            getOnSetDate: function() {
                return this.onSetDate;
            },

            setOnSetDate: function(onSetDate) {
                this.onSetDate = onSetDate;
            },

            getAdditionalDetail: function() {
                return this.additionalDetail;
            },

            setAdditionalDetail: function(additionalDetail) {
                this.additionalDetail = additionalDetail;
            },

            setEndDate: function(endDate) {
                this.endDate = endDate;
            },

            getEndDate: function() {
                return this.endDate;
            },

            setEndReason: function(endReason) {
                this.endReason = endReason;
            },

            getEndReason: function() {
                return this.endReason;
            }
        };

        return ConditionModel;
    }

    baseModel.factory("ConditionModel", ConditionModel);
    ConditionModel.$inject = ['ConceptModel'];
})();
