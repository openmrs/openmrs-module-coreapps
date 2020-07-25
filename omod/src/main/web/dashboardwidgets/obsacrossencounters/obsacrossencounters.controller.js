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
    const encounterTypes = this.config.encounterTypes ? this.config.encounterType.split(',').map(c => c.trim()) : [];
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

  getObsValue(obs) {
    if (['8d4a505e-c2cc-11de-8d13-0010c6dffd0f',
        '8d4a591e-c2cc-11de-8d13-0010c6dffd0f',
        '8d4a5af4-c2cc-11de-8d13-0010c6dffd0f'].includes(obs.concept.datatype.uuid)) {
        // If value is date, time or datetime
        return this.$filter('date')(new Date(obs.value), this.config.dateFormat);
    } else if (angular.isDefined(obs.value.display)) {
        // If value is a concept
        return this.config.useConceptNameForValue ? obs.value.concept.display : obs.value.display
    } else {
        return obs.value;
    }
  }

  addToSimpleEncs(encounters) {
    for (let encounter of encounters) {
      const conceptKeys = Object.keys(this.conceptsMap);
      // all normal concepts will go in one row
      const foundObsByUuid = {};
      for (let obs of encounter.obs) {
        // see if the concept matches
        if (conceptKeys.includes(obs.concept.uuid)) {
          foundObsByUuid[obs.concept.uuid] = obs;
        }
        // add a row for each group with matches
        if (obs.groupMembers) {
          const foundMembers = obs.groupMembers.filter(member => conceptKeys.includes(member.concept.uuid));
          if (foundMembers) {
            const foundMembersByUuid = Object.fromEntries(foundMembers.map(m => [m.concept.uuid, m]));
            this.simpleEncs.push({
              encounterType: encounter.encounterType.name,
              encounterDatetime: encounter.encounterDatetime,
              obs: foundMembersByUuid
            });
          }
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
   * Iterates through all obs, and for all value coded,
   * replace the default obs.value.display with
   * the value display of the appropriate shortName
   * @param encounters
   */
  updateWithConceptShortNames(encounters) {
    for (let encounter of encounters) {
      for (let obs of encounter.obs) {
        // if the obs value is coded then we try to look for the short names of the coded answers
        // some obs.value nodes contain the concept property that points to the concept
        // and other coded obs(e.g. Drug Frequency) do not contain a child concept property
        const conceptUuid = obs.value.concept ? obs.value.concept.uuid : obs.value.uuid;
        if (conceptUuid) {
          this.openmrsRest.get("concept/" + conceptUuid, {
            v: this.CONCEPT_CUSTOM_REP
          }).then((concept) => {
            obs.value.display = this.widgetsCommons.getConceptName(concept, "shortName", this.sessionLocale);
          }).catch(err => console.error(`failed to retrieve concept ${conceptUuid}, ${err}`));
        }
      }
    }
  }
}
