export default class ObsAcrossEncountersController {
  constructor($q, $filter, openmrsRest, openmrsTranslate, widgetsCommons) {
    'ngInject';

    Object.assign(this, {$q, $filter, openmrsRest, openmrsTranslate, widgetsCommons});
  }

  $onInit() {
    this.sessionLocale = null;
    this.CONCEPT_CUSTOM_REP = 'custom:(uuid,display,names:(voided,locale,conceptNameType,localePreferred,name)';
    this.conceptsMap = {};  // a map of conceptUUID --> concept(REST response)
    this.conceptKeys = [];
    this.simpleEncs = [];
    this.headers = [];
    this.openmrsRest.setBaseAppPath("/coreapps");
    this.isArray = angular.isArray;

    this.output = {
      headers: [],
      rows: [],
      maxAgeInDays: this.widgetsCommons.maxAgeToDays(this.config.maxAge)
    }

    return Promise.all([
        this.fetchSessionInfo(),
        this.fetchConcepts(),
        this.fetchEncounters()
    ]).then(() => this.formatOutput());
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

  fetchConcepts() {
    this.conceptKeys = this.config.concepts.split(",").map(c => c.trim());
    return Promise.all(this.conceptKeys.map((key) => (
      this.openmrsRest.get("concept/" + key, {
        v: this.CONCEPT_CUSTOM_REP
      }).then((concept) => {
        // update the concept map with the REST representation of the concept
        concept.display = this.widgetsCommons.getConceptName(concept, "shortName", this.sessionLocale);
        this.conceptsMap[concept.uuid] = concept;
      })
    )));
  }

  fetchEncounters() {
    const encounterTypes = this.config.encounterTypes ? this.config.encounterTypes.split(',').map(c => c.trim()) : [];
    const legacyEncounterTypes = this.config.encounterType ? this.config.encounterType.split(',').map(c => c.trim()) : [];
    encounterTypes.push(...legacyEncounterTypes);

    const encounterQueryParams = {
      patient: this.config.patientUuid,
      v: 'custom:(uuid,encounterDatetime,encounterProviders:(uuid,provider:(uuid,name)),encounterType:(name,description),obs:(id,uuid,value,concept:(id,uuid,name:(display),datatype:(uuid)),groupMembers:(id,uuid,display,value,concept:(id,uuid,name:(display),datatype:(uuid))))',
      limit: this.config.maxRecords || 4,
      fromdate: this.widgetsCommons.maxAgeToDate(this.config.maxAge)
    };
    const encounterPromises = encounterTypes.length ? encounterTypes.map(e =>
      this.openmrsRest.get("encounter",
          Object.assign({ encounterType: e }, encounterQueryParams)).then(response => response.results))
    : [this.openmrsRest.get("encounter",
          Object.assign({ encounterType: '' }, encounterQueryParams)  // `encounterType: ''` is required to prevent a 400 error
      ).then(response => response.results)];

    return Promise.all(encounterPromises).then((encounterSets) => (
      Promise.all(encounterSets.map(encounterSet => this.addToSimpleEncs(encounterSet)))
    ));
  }

  formatOutput() {
    this.output.headers = this.config.headers ? this.config.headers.split(",") : [];
    if (this.output.headers.length === 0) {
      if (this.config.showEncounterTypeName) {
        this.output.headers.push('coreapps.patientDashBoard.encounter');
      }
      if (this.config.showEncounterProviderName) {
        this.output.headers.push('coreapps.patientDashBoard.provider');
      }
      this.output.headers.push('coreapps.date');

      this.conceptKeys.map(uuid => {
        this.output.headers.push(this.conceptsMap[uuid].display);
      });
    }

    this.output.rows = [];
    let encounterDateToDisplay;
    let providersNameToDisplay;
    let sortedEncs = [];
    if (this.config.sortOrder === 'desc') {
      // sort obs by encounter datetime in descending order
      sortedEncs = this.simpleEncs.sort((a, b) => (new Date(b.encounterDatetime)).getTime() - (new Date(a.encounterDatetime)).getTime());
    } else {
      //by default, sort obs by encounter datetime in ascending order(the oldest obs will be on top of the list)
      sortedEncs = this.simpleEncs.sort((a, b) => (new Date(a.encounterDatetime)).getTime() - (new Date(b.encounterDatetime)).getTime());
    }
    for (let i = 0; i < sortedEncs.length; i++) {
      encounterDateToDisplay = "";
      providersNameToDisplay = "";
      const encounter = sortedEncs[i]
      const row = [];
      this.output.rows.push(row);
      if (this.config.showEncounterTypeName) {
        row.push({ value: encounter.encounterType, translate: true });
      }

      if (this.config.showEncounterProviderName) {
        encounter.encounterProviders.map((element , index) => {
          providersNameToDisplay += element.provider.name;
          if(index != (encounter.encounterProviders.length - 1)){
            providersNameToDisplay +=  ", "
          }
        });
        row.push({ value: providersNameToDisplay , translate: false });
      }
      if (this.config.showDateWithTime) {
         encounterDateToDisplay = this.widgetsCommons.formatDateTime(
            encounter.encounterDatetime,
            this.config.JSDateTimeFormat,
            this.config.language);
      } else {
        encounterDateToDisplay = this.widgetsCommons.formatDate(
            encounter.encounterDatetime,
            this.config.JSDateFormat,
            this.config.language);
      }
        row.push({
          value: encounterDateToDisplay
        });

      this.conceptKeys.map(uuid => {
        let obsArray = [];
        if (encounter.obs[uuid]) {
          for (let obs of encounter.obs[uuid]) {
            obsArray.push({
              value: (obs ? this.getObsValue(obs) : '') || '',
              className: this.isRetired(obs) ? 'retiredConcept' : ''
            });
          }
        } else {
          obsArray.push({
            value: ''
          });
        }
        row.push(obsArray);
      });
    }
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
    const resultPromises = []
    for (let encounter of encounters) {
      // all normal concepts will go in one row
      const foundObsByUuid = {};
      for (let obs of encounter.obs) {
        // if it's a group, add a row for each member matching one of the concepts
        if (obs.groupMembers) {
          const foundMembers = obs.groupMembers.filter(member => this.conceptKeys.includes(member.concept.uuid));
          if (foundMembers.length) {
            const foundMembersByUuid = {};
            for (let m of foundMembers) {
              if (!foundMembersByUuid[m.concept.uuid]) {
                foundMembersByUuid[m.concept.uuid] = [];
              }
              foundMembersByUuid[m.concept.uuid].push(m);
            }
            this.simpleEncs.push({
              encounterType: encounter.encounterType.name,
              encounterDatetime: encounter.encounterDatetime,
              encounterProviders: encounter.encounterProviders,
              obs: foundMembersByUuid
            });
          }
        } else // otherwise, add the obs value (if it matches one of our concepts)
          if (this.conceptKeys.includes(obs.concept.uuid)) {
            if (!foundObsByUuid[obs.concept.uuid]) {
              foundObsByUuid[obs.concept.uuid] = [];
            }
            foundObsByUuid[obs.concept.uuid].push(obs);
        }
      }
      if (Object.keys(foundObsByUuid).length > 0) {
        this.simpleEncs.push({
          encounterType: encounter.encounterType.name,
          encounterDatetime: encounter.encounterDatetime,
          encounterProviders: encounter.encounterProviders,
          obs: foundObsByUuid
        });
      }
      // if the widget was configured to display the obs concept SHORT name instead of the default obs.value.display value
      // By default, the widget displays the obs.value.display property
      if (this.config.useConceptShortName) {
        resultPromises.push(this.updateWithConceptShortNames(this.simpleEncs));
      }
    }
    return Promise.all(resultPromises);
  }

  /**
   * Sets all obs coded concept values and drug value concepts to use the short name
   * @param encounters
   */
  updateWithConceptShortNames(encounters) {
    const resultPromises = [];
    for (let encounter of encounters) {
      for (let obsArray of Object.values(encounter.obs)) {
        for (let obs of obsArray) {
          if (this.widgetsCommons.isDrug(obs.value)) {
            resultPromises.push(this.updateWithShortName(obs.value.concept));
          } else if (this.widgetsCommons.hasDatatypeCoded(obs.concept)) {
            resultPromises.push(this.updateWithShortName(obs.value));
          }
        }
      }
    }
    return resultPromises;
  }

  updateWithShortName(concept) {
    return this.openmrsRest.get("concept/" + concept.uuid, {
      v: this.CONCEPT_CUSTOM_REP
    }).then((conceptRes) => {
      concept.display = this.widgetsCommons.getConceptName(conceptRes, "shortName", this.sessionLocale);
    }).catch(err => console.error(`failed to retrieve concept ${conceptUuid}, ${err}`));
  }
}
