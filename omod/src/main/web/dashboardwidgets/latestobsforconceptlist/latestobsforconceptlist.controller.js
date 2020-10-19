export default class LatestObsForConceptListController {
    constructor($filter, openmrsRest, widgetsCommons , openmrsTranslate) {
        'ngInject';

        Object.assign(this, {$filter, openmrsRest, openmrsTranslate, widgetsCommons });
    }

    $onInit() {
        this.openmrsRest.setBaseAppPath("/coreapps");
        this.maxAgeInDays = this.widgetsCommons.maxAgeToDays(this.config.maxAge);

        // Fetch last obs or obsGroup for the list of concepts
        this.openmrsRest.list('latestobs', {
            patient: this.config.patientUuid,
            v: 'custom:(' +
                'obsDatetime,' +
                'concept:(uuid,display,datatype:(uuid),names:(name,locale,localePreferred,voided,conceptNameType)),' +
                'value:(uuid,display,names:(name,locale,localePreferred,voided,conceptNameType)),' +
                'groupMembers:(value,concept:(display,names:(name,locale,localePreferred,voided,conceptNameType))))',
            concept: this.config.concepts.split(',').map(c => c.trim()).join(','),
            nLatestObs: this.config.nLatestObs || 1
        }).then((resp) => {
            // Process the results from the list of concepts as not all of them may have data
            this.obs = resp.results.filter(
                // Don't add obs older than maxAge
                obs => angular.isUndefined(this.maxAgeInDays) || this.widgetsCommons.dateToDaysAgo(obs.obsDatetime) <= this.maxAgeInDays
            ).map(inputObs => {
                const displayObs = { obsDatetime: inputObs.obsDatetime };
                displayObs.conceptName = this.widgetsCommons.getConceptName(inputObs.concept, this.config.conceptNameType, this.config.locale);
                if (inputObs.groupMembers) { // If obs is obs group
                    displayObs.groupMembers = inputObs.groupMembers.map(member => {
                        const prefix = ["FSN", "shortName", "preferred"].includes(this.config.obsGroupLabels) ?
                            "(" + this.widgetsCommons.getConceptName(member.concept, this.config.obsGroupLabels, this.config.locale) + ") " : "";
                        const value = this.getObsValue(member);
                        return { "prefix": prefix, "value": value };
                    });
                } else {
                    displayObs.value = this.getObsValue(inputObs);
                }
                return displayObs;
            });
        });
    }

    getObsValue(obs) {
        if (this.widgetsCommons.hasDatatypeDateOrSimilar(obs.concept)) {
            return this.$filter('date')(new Date(obs.value), this.config.dateFormat);
        } else if (this.widgetsCommons.hasDatatypeCoded(obs.concept)) {
            return this.widgetsCommons.getConceptName(obs.value, this.config.conceptNameType, this.config.locale);
        } else {
            return obs.value.display || obs.value;
        }
    }
}