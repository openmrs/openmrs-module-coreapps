export default class ObsAcrossEncountersController {
  constructor($q, $filter, openmrsRest, openmrsTranslate, widgetsCommons) {
    'ngInject';

    Object.assign(this, {$q, $filter, openmrsRest, openmrsTranslate, widgetsCommons});
  }

  $onInit() {
    this.order = 'desc';
    this.sessionLocale = null;
    this.CONCEPT_CUSTOM_REP = 'custom:(uuid,display,names:(voided,locale,conceptNameType,localePreferred,name)';

    // a map of conceptUUID --> concept(REST response)
    this.conceptsMap = {};
    this.simpleEncs = [];
    this.headers = [];
    this.openmrsRest.setBaseAppPath("/coreapps");

    this.fetchSessionInfo();
    this.fetchHeaders();
    this.fetchConcepts();
    this.fetchEncounters();
    this.maxAgeInDays = this.widgetsCommons.maxAgeToDays(this.config.maxAge);
  }

  fetchSessionInfo() {
    this.openmrsRest.get("session?", {
      v: "ref"
    }).then((session) => {
      this.sessionLocale = session.locale;
    }, function(error) {
       console.error(`failed to retrieve session info, error: ${error}`)
    });
  }
  
  fetchHeaders() {
    if (this.config.headers) {
      let columnNames = this.config.headers.split(",");
      this.headers = columnNames;
    }
  }

  fetchConcepts() {
    let conceptKeys = this.config.concepts.split(",").map(c => c.trim());
    for (let i = 0; i < conceptKeys.length; i++) {
      this.openmrsRest.get("concept/" + conceptKeys[i], {
        v: this.CONCEPT_CUSTOM_REP
      }).then((concept) => {
        // update the concept map with the REST representation of the concept
        concept.display = this.widgetsCommons.getConceptName(concept, "shortName", this.sessionLocale);
        this.conceptsMap[concept.uuid] = concept;
      });
    }
  }

  fetchEncounters() {
    const encounterTypes = this.config.encounterTypes ? this.config.encounterTypes.split(',').map(c => c.trim()) : [];
    const legacyEncounterTypes = this.config.encounterType ? this.config.encounterType.split(',').map(c => c.trim()) : [];
    encounterTypes.push(...legacyEncounterTypes);

    const encounterPromises = encounterTypes.map(e =>
      this.openmrsRest.get("encounter", {
        patient: this.config.patientUuid,
        encounterType: e,
        v: 'custom:(uuid,encounterDatetime,encounterType:(name,description),obs:(id,uuid,value,concept:(id,uuid,name:(display),datatype:(uuid)),groupMembers:(id,uuid,display,value,concept:(id,uuid,name:(display),datatype:(uuid))))',
        limit: this.config.maxRecords || 4,
        fromdate: this.widgetsCommons.maxAgeToDate(this.config.maxAge),
        order: this.order
      }).then(response => response.results)
    );

    this.$q.all(encounterPromises).then((encounterSets) => {
      for (let encounterSet of encounterSets){
        this.addToSimpleEncs(encounterSet); 
      }
    });
  }

  isRetired(obs) {
    return Boolean(obs && obs.value && obs.value.retired);
  }

  // `obs` must *not* be an obsGroup
  getObsValue(obs) {
    if (this.widgetsCommons.hasDatatypeDateOrSimilar(obs.concept)) {
        return this.$filter('date')(new Date(obs.value), this.config.dateFormat);
    } else if (this.widgetsCommons.isDrug(obs.value)) {
        return this.config.useConceptNameForDrugValues ? obs.value.concept.display : obs.value.display
    } else {
        return obs.value.display || obs.value;
    }
  }

  addToSimpleEncs(encounters) {
    for (let encounter of encounters) {
      const conceptKeys = Object.keys(this.conceptsMap);
      // all normal concepts will go in one row
      const foundObsByUuid = {};
      for (let obs of encounter.obs) {
        // if it's a group, add a row for each member matching one of the concepts
        if (obs.groupMembers) {
          const foundMembers = obs.groupMembers.filter(member => conceptKeys.includes(member.concept.uuid));
          if (foundMembers.length) {
            const foundMembersByUuid = Object.fromEntries(foundMembers.map(m => [m.concept.uuid, m]));
            this.simpleEncs.push({
              encounterType: encounter.encounterType.name,
              encounterDatetime: encounter.encounterDatetime,
              obs: foundMembersByUuid
            });
          }
        } else // otherwise, add the obs value (if it matches one of our concepts)
          if (conceptKeys.includes(obs.concept.uuid)) {
          foundObsByUuid[obs.concept.uuid] = obs;
        }
      }
      if (Object.keys(foundObsByUuid).length > 0) {
        this.simpleEncs.push({
          encounterType: encounter.encounterType.name,
          encounterDatetime: encounter.encounterDatetime,
          obs: foundObsByUuid
        });
      }
      // if the widget was configured to display the obs concept SHORT name instead of the default obs.value.display value
      // By default, the widget displays the obs.value.display property
      if (this.config.useConceptShortName) {
        this.updateWithConceptShortNames(this.simpleEncs);
      }
    }
  }

  /**
   * Sets all obs coded concept values and drug value concepts to use the short name
   * @param encounters
   */
  updateWithConceptShortNames(encounters) {
    for (let encounter of encounters) {
      for (let obs of encounter.obs) {
        if (this.widgetsCommons.isDrug(obs.value)) {
          this.updateWithShortName(obs.value.concept);
        } else if (this.widgetsCommons.hasDatatypeCoded(obs.concept)) {
          this.updateWithShortName(obs.value);
        }
      }
    }
  }

  updateWithShortName(concept) {
    this.openmrsRest.get("concept/" + concept.uuid, {
      v: this.CONCEPT_CUSTOM_REP
    }).then((concept) => {
      concept.display = this.widgetsCommons.getConceptName(concept, "shortName", this.sessionLocale);
    }).catch(err => console.error(`failed to retrieve concept ${conceptUuid}, ${err}`));
  }
}
