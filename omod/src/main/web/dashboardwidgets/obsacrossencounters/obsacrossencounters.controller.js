export default class ObsAcrossEncountersController {
  constructor($filter, openmrsRest, openmrsTranslate, widgetsCommons) {
    'ngInject';

    Object.assign(this, {$filter, openmrsRest, openmrsTranslate, widgetsCommons});
  }

  $onInit() {
    this.order = 'desc';
    this.sessionLocale = null;
    this.CONCEPT_CUSTOM_REP = 'custom:(uuid,display,names:(display,conceptNameType,locale)';

    this.concepts = [];
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
       console.log(`failed to retrieve session info, error: ${error}`)
    });
  }
  fetchHeaders() {
    if (this.config.headers && this.config.headers.length > 0) {
      let columnNames = this.config.headers.split(",");
      if (columnNames !== null && columnNames.length > 0) {
        this.headers = columnNames;
      }
    }
  }

  fetchConcepts() {
    this.concepts = this.getConfigConceptsAsArray(this.config.concepts);
    for (let i = 0; i < this.concepts.length; i++) {
      this.openmrsRest.get("concept/" + this.concepts[i], {
        v: this.CONCEPT_CUSTOM_REP
      }).then((concept) => {
        let index = this.concepts.indexOf(concept.uuid);
        this.concepts[index] = this.getConceptWithShortName(concept);
        // update the concept map with the REST representation of the concept
        this.conceptsMap[concept.uuid] = this.getConceptWithShortName(concept);
      });
    }
  }

  fetchEncounters() {
    this.openmrsRest.get("encounter", {
      patient: this.config.patientUuid,
      encounterType: this.config.encounterType ? this.config.encounterType : null,
      v: 'custom:(uuid,encounterDatetime,obs:(id,uuid,value,concept:(id,uuid,name:(display),datatype:(uuid)),groupMembers:(id,uuid,display,value,concept:(id,uuid,name:(display),datatype:(uuid))))',
      limit: this.getMaxRecords(),
      fromdate: this.widgetsCommons.maxAgeToDate(this.config.maxAge),
      order: this.order
    }).then((response) => {
      this.parseEncounters(response.results);
    });
  }

  getMaxRecords() {
    if (this.config.maxRecords == '' || angular.isUndefined(this.config.maxRecords)) {
      return 4;
    } else {
      return this.config.maxRecords;
    }
  }

  isRetired(obs) {
    let retired = false;
    if (obs && angular.isDefined(obs.value) && angular.isDefined(obs.value.retired)) {
      retired = obs.value.retired;
    }
    return retired;
  }

  displayObs(obs) {
    let display = "";
    if (obs.value != null) {
      if (angular.isDefined(obs.value.display)) {
        //If value is a concept
        display = obs.value.display;
      }
      else if (['8d4a505e-c2cc-11de-8d13-0010c6dffd0f',
          '8d4a591e-c2cc-11de-8d13-0010c6dffd0f',
          '8d4a5af4-c2cc-11de-8d13-0010c6dffd0f'].indexOf(obs.concept.datatype.uuid) > -1) {
        //If value is date, time or datetime
        var date = this.$filter('date')(new Date(obs.value), this.config.dateFormat);
        display = date;
      } else {
        display = obs.value;
      }
    }
    return display;
  }

  getConfigConceptsAsArray(commaDelimitedConcepts) {
    let conceptArray = commaDelimitedConcepts.replace(" ", "").split(",");
    if (conceptArray !== null && conceptArray.length > 0) {
      for (let i = 0; i < conceptArray.length; i++) {
        let conceptKey = conceptArray[i].trim();
        // initialize the conceptsMap object
        if (typeof this.conceptsMap[conceptKey] === 'undefined') {
          this.conceptsMap[conceptKey] = null;
        }
      }
    }
    return conceptArray;
  }

  getConceptWithShortName(concept) {
    let lastShortName = null;
    for(let i=0; i < concept.names.length; i++) {
      let conceptName = concept.names[i];
      if (conceptName.conceptNameType == 'SHORT') {
        lastShortName = conceptName.display;
        if (this.sessionLocale !== null && this.sessionLocale === conceptName.locale) {
          // we found a SHORT name that matches the locale of the current session
          break;
        }
      }
    }
    if (lastShortName !== null && lastShortName.length > 0) {
      concept.display = lastShortName;
    }
    return concept;
  }


  parseEncounters(encounters) {
    angular.forEach(encounters, (encounter) => {
      let searchObs = {};
      angular.forEach(encounter.obs, (obs) => {
        let conceptKeys = Object.keys(this.conceptsMap);

        if (conceptKeys.findIndex(conceptKey => conceptKey === obs.concept.uuid) >= 0) {
          //we found an obs match
          searchObs[obs.concept.uuid] = obs;
        }
        if (obs.groupMembers != null && obs.groupMembers.length > 0) {
          // need to search the groupMembers
          let foundObs = this.parseGroupMembers(obs.groupMembers, conceptKeys);
          if (typeof foundObs !== 'undefined' && foundObs !== null && Object.keys(foundObs).length > 0) {
            if (Object.keys(foundObs).every(item => conceptKeys.includes(item))) {
              //this is a complete pair of matching obs with a given concept uuid within the same obsGroup
              let enc = {
                encounterDatetime: encounter.encounterDatetime,
                obs: foundObs
              };
              this.simpleEncs.push(enc);
            } else {
              // we have an incomplete match, just add the values to the existing object
              for (key in Object.keys(foundObs)){
                searchObs[key] = searchObs[key];
              }
            }
          }
        }
      });

      if (Object.keys(searchObs).length > 0) {
        let tempEnc = {
          encounterDatetime: encounter.encounterDatetime,
          obs: searchObs
        };
        this.simpleEncs.push(tempEnc);
      }
      // if the widget was configured to display the obs concept SHORT name instead of the default obs.value.display value
      // By default, the widget displays the obs.value.display property
      if (this.config.useConceptShortName && this.config.useConceptShortName === 'true') {
        this.updateWithConceptShortNames(this.simpleEncs);
      }
    });
  }

  /**
   * Iterates through all obs, and for all value coded,
   * replace the default obs.value.display with
   * the value display of the appropriate shortName
   * @param encounters
   */
  updateWithConceptShortNames(encounters) {
    if (encounters && encounters.length > 0) {
      angular.forEach(encounters, encounter => {
        let encounterObs = encounter.obs;
        for (const prop in encounterObs) {
          let obs = encounterObs[prop];
          let conceptUuid = null;
          // if the obs value is coded then we try to look for the short names of the coded answers
          if (angular.isDefined(obs.value.concept) ) {
            // some obs.value nodes contain the concept property that points to the concept
            conceptUuid = obs.value.concept.uuid;
          } else if (angular.isDefined(obs.value.uuid)) {
            // and other coded obs(e.g. Drug Frequency) do not contain a child concept property
            conceptUuid = obs.value.uuid;
          }
          if (conceptUuid !== null && conceptUuid.length > 0) {
            this.openmrsRest.get("concept/" + conceptUuid, {
              v: this.CONCEPT_CUSTOM_REP
            }).then((concept) => {
              let shortDisplay = this.getConceptWithShortName(concept);
              obs.value.display = shortDisplay.display;
            }, function (err) {
              console.log(`failed to retrieve concept ${conceptUuid}, ${err}`);
            });
          }
        }
      });
    }
  }

  parseGroupMembers(groupMembers, concepts) {
    let matchObs = {};
    angular.forEach(groupMembers, (obs) => {
      if (concepts.includes(obs.concept.uuid)) {
        //we found an obs match
        matchObs[obs.concept.uuid] = obs;
      }
    });
    return matchObs;
  }

}
