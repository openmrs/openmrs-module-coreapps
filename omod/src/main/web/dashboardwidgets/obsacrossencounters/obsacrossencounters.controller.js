export default class ObsAcrossEncountersController {
  constructor($filter, openmrsRest, widgetsCommons) {
    'ngInject';

    Object.assign(this, {$filter, openmrsRest, widgetsCommons});
  }

  $onInit() {
    this.order = 'desc';
    this.concepts = [];
    // a map of conceptUUID --> concept(REST response)
    this.conceptsMap = {};
    this.simpleEncs = [];

    this.openmrsRest.setBaseAppPath("/coreapps");

    this.fetchConcepts();
    this.fetchEncounters();
  }

  fetchConcepts() {
    this.concepts = this.getConfigConceptsAsArray(this.config.concepts);
    for (let i = 0; i < this.concepts.length; i++) {
      this.openmrsRest.get("concept/" + this.concepts[i], {
        v: 'custom:(uuid,display,names:(display,conceptNameType)'
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
        let conceptKey = conceptArray[i];
        // initialize the conceptsMap object
        if (typeof this.conceptsMap[conceptKey] === 'undefined') {
          this.conceptsMap[conceptKey] = null;
        }
      }
    }
    return conceptArray;
  }

  getConceptWithShortName(concept) {
    angular.forEach(concept.names, (name) => {
      if (name.conceptNameType == 'SHORT') {
        concept.display = name.display;
      }
    });
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
    });
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
