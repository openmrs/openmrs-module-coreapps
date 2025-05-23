export default class ObsGraphController {
    constructor( $q, $filter, openmrsRest, openmrsTranslate, widgetsCommons) {
        'ngInject';

        Object.assign(this, { $q, $filter, openmrsRest, openmrsTranslate, widgetsCommons});
    }

    $onInit() {

        var self = this;
        self.xAxis = {};
        // Max age of obs to display
        this.maxAgeInDays = undefined;
        this.conceptRep = "custom:(uuid,display,name:(display),datatype:(uuid,display))";
        this.customRep="custom:(uuid,display,obsDatetime,value,encounter:(encounterType),concept:(uuid,display,name:(display),datatype:(uuid,display)))";
        this.patientProgramRep = 'custom:(uuid,display,program:(uuid,display),dateEnrolled,dateCompleted,dateCreated,outcome:(display),location:(display,uuid))';
      
        this.programDateEnrolled = null;
        this.duringCurrentEnrollmentInProgram = null;
      
        // Chart data
        this.series = [];
        this.labels = [];
        this.data = [];
        this.legend = [];

        this.dataset = [];
        /* Here is an example of the structure of this array of objects
          [
            {
              "uuid": "3ce93b62-26fe-102b-80cb-0017a47871b2",
              "display": "Weight (kg)",
              "values": {
                "9": 70, // the key indicates how many days ago this obs value was recorded
                "10": 70.5,
                "133": 75,
                "235": 63,
                "324": 65.9,
                "996": 100
              }
            },
            {
              "uuid": "3ce93cf2-26fe-102b-80cb-0017a47871b2",
              "display": "Height (cm)",
              "values": {
                "9": 170.2,
                "10": 165.1,
                "133": 165.1,
                "235": 110,
                "324": 153.4
              }
            }
          ]

         */

        this.openmrsRest.setBaseAppPath("/coreapps");



        this.FunctionManager = {
          bmi: function( height, weight ){
            //add height and weight to the list of concepts whose obs should be retrieved
            if (arguments && arguments.length > 0) {
              for (let i=0; i < arguments.length; i++) {
                let foundValue = self.conceptArray.find(element => element.uuid === arguments[i]);
                if (self.conceptArray && (typeof foundValue === 'undefined')) {
                  self.conceptArray.push({
                    uuid: arguments[i],
                    type: "obs",
                    legend: false
                  });
                }
              }
              self.conceptArray.push({
                uuid: null,
                type: "function",
                legend: true,
                function: ["calculateBmi", height, weight]
              });
            }
            return true;
          },
          calculateBmi: function(height, weight) {
            if (height && weight) {
              let heightDataset = self.dataset.find(element => element.uuid === height);
              let weightDataset = self.dataset.find(element => element.uuid === weight);
              if (heightDataset && weightDataset) {
                let xAxisKeys = self.getXAxisKeys();
                if (xAxisKeys !== null && xAxisKeys.length > 0) {
                  let tempData = [];
                  for (let k = 0; k < xAxisKeys.length; k++) {
                    let heightValue = parseInt(heightDataset.values[xAxisKeys[k]]);
                    let weightValue = parseInt(weightDataset.values[xAxisKeys[k]]);
                    let yValue = null;
                    if ((typeof heightValue !== undefined) && (heightValue !== null) && ( typeof weightValue !== undefined ) && (weightValue !== null)) {
                      yValue = (( weightValue / heightValue / heightValue) * 10000 ).toFixed(1);
                    }
                    tempData.push(yValue);
                  }
                  self.series.push("BMI");
                  self.data.push(tempData);
                }
              }
            }
            return null;
          },
          execute: function ( name ) {
            return self.FunctionManager[name] && self.FunctionManager[name].apply(self.FunctionManager, [].slice.call(arguments, 1));
          }
        };


        // order the X Axis values from the oldest obs date the the most recent obs date
        this.orderXAxis = function() {
          let xAxisKeys = this.getXAxisKeys();
          if (xAxisKeys !== null && xAxisKeys.length > 0) {
            for (let k = 0; k < xAxisKeys.length; k++) {
              this.labels.push(this.xAxis[xAxisKeys[k]]);
            }
          }

        };

        this.updateChartData = function() {
          if (angular.isDefined(this.dataset) && this.dataset.length > 0) {
            let xAxisKeys = this.getXAxisKeys();
            if (xAxisKeys !== null && xAxisKeys.length > 0) {
              for (let j=0; j < this.conceptArray.length; j++) {
                let concept = this.conceptArray[j];
                if( this.hideConcepts !== undefined && this.hideConcepts.length && this.hideConcepts.includes(concept.uuid)) {
                  //if it was indicated that this concept should not be plotted, then skip to the next concept
                  continue;
                }
                if (concept.legend === true) {
                  if (concept.uuid && concept.type === "obs") {
                    let obj = this.dataset.find(element => element.uuid === concept.uuid);
                      if (angular.isDefined(obj) && obj !== null && angular.isDefined(obj.values) && obj.values !== null && (Object.keys(obj.values)).length > 0) {
                        // if this concept has any obs values
                        let tempData = [];
                        for (let k = 0; k < xAxisKeys.length; k++) {
                          let yValue = obj.values[xAxisKeys[k]];
                          // all Y arrays of data need to have the same number of values
                          // fill with null the missing values
                          if (angular.isUndefined(yValue) || yValue == null) {
                            yValue = null;
                          }
                          tempData.push(yValue);
                        }
                        this.data.push(tempData);
                      } else {
                        //Removing series without corresponding data points
                        let index = this.series.indexOf(concept.display);
                        if (index >= 0) {
                          this.series.splice(index, 1);
                        }
                      }
                  } else if (concept.type === "function" && concept.function) {
                    this.FunctionManager.execute(...concept.function);
                  }
                }
              }
            }
          }

        };
        
        this.getProgramEnrollmentDate = function() {
          let promises = [];
          if (this.duringCurrentEnrollmentInProgram) {
            let promisedObs = this.openmrsRest.get('programenrollment', {
              patient: this.config.patientUuid,
              v: this.patientProgramRep
            }).then(function (enrollment) {
              return enrollment;
            });
            promises.push(promisedObs);
          }
          
          return this.$q.all(promises).then(function(enrollments) {
            for (let i=0; i < enrollments.length; i++) {
              let results = enrollments[i].results;
              if (results && results.length > 0) {
                for (let j = 0; j < results.length; j++) {
                  let prgEnrollment = results[j];
                  if (prgEnrollment.program.uuid == self.duringCurrentEnrollmentInProgram) {
                    //we found the program whose data needs to be included in this graph
                    if ( (prgEnrollment.dateEnrolled != null) && (prgEnrollment.dateCompleted == null) ) {
                      //this is an active/open enrollment
                      self.programDateEnrolled = prgEnrollment.dateEnrolled;
                      break;
                    }
                  }
                }
              }
            }
          });
        }
        
        this.getConceptNames = function() {
          let promises = [];
          if (this.conceptArray !== null && this.conceptArray.length > 0) {
            for (let i = 0; i < this.conceptArray.length; i++) {
              if (this.conceptArray[i].uuid) {
                let promisedObs = this.openmrsRest.get('concept/' + this.conceptArray[i].uuid, {
                  v: this.conceptRep
                }).then(function (concept) {
                  return concept;
                });
                promises.push(promisedObs);
              }
            }
          }
          return this.$q.all(promises).then(function(concepts) {
            for (let i=0; i < self.conceptArray.length; i++) {
              let concept = self.conceptArray[i];
              if (concept.legend === true) {
                let serverConcept = concepts.find(element => element.uuid === concept.uuid);
                if (serverConcept && serverConcept.display) {
                  self.series.push(serverConcept.display);
                  self.conceptArray[i].display = serverConcept.display;
                }
              }
            }
          });

        };

        this.getAllObs = function() {
          let promises = [];
          if (this.conceptArray !== null && this.conceptArray.length > 0) {
            for (let i = 0; i < this.conceptArray.length; i++) {
              if (this.conceptArray[i].uuid) {
                let promisedObs = this.openmrsRest.list('obs', {
                  patient: this.config.patientUuid,
                  v: this.customRep,
                  limit: this.config.maxResults,
                  concept: this.conceptArray[i].uuid
                }).then(function (response) {
                  return response.results;
                });
                promises.push(promisedObs);
              }
            }
          }
          return this.$q.all(promises).then(function(data) {
            let isEncounterTypeNotAllowed = function (encounterType) {
              return angular.isDefined(self.encounterTypes) &&
                self.encounterTypes.indexOf(encounterType) < 0;
            };
            let getEncounterType = function(observation) {
              if(angular.isUndefined(observation)){
                return null;
              }
              return observation.encounter.encounterType.uuid;
            }

            for (let j=0; j< data.length; j++) {
              let conceptObject = {};

              let obsArray = data[j];
              if (obsArray.length > 0) {
                //we have at least one observation
                conceptObject.uuid = obsArray[0].concept.uuid;
                conceptObject.display = obsArray[0].concept.display;
                conceptObject.values = {};
                for (let k = 0; k < obsArray.length; k++) {
                  let obs = obsArray[k];
                  //Skip obs if encounter type does not match (only when encounter type specified in config)
                  if (isEncounterTypeNotAllowed(getEncounterType(obs))) {
                    continue;
                  }
                  //if duringCurrentEnrollmentInProgram config parameter is defined, then display only the obs from the current/active program enrollment
                  if ( (self.duringCurrentEnrollmentInProgram && (self.programDateEnrolled == null)) ||
                      (self.programDateEnrolled && ( (new Date(self.programDateEnrolled)) > new Date(obs.obsDatetime)))) {
                    // there is no current open enrollment in the program specified or
                    // the obs was taken prior to the current program enrollment date
                    continue;
                  }
                  
                  if (obs.concept.datatype.display === 'Numeric') {
                    // Don't add obs older than maxAge
                    let daysSinceObs = self.widgetsCommons.daysSinceDate(obs.obsDatetime);
                    if (angular.isUndefined(self.maxAgeInDays) || daysSinceObs <= self.maxAgeInDays) {
                      // Add obs data for chart display
                      var obsDate = self.widgetsCommons.formatDate(obs.obsDatetime, self.config.JSDateFormat, self.config.language);
                      let xValue = new Date().getTime() - new Date(obs.obsDatetime).getTime();
                      conceptObject.values[xValue] = obs.value;
                      self.xAxis[xValue] = obsDate;
                    }
                  }
                }
              }
              self.dataset.push(conceptObject);
            }
            self.orderXAxis();
          });
      };

      this.getConfig();
      // chaining the promises sequentially
      this.getProgramEnrollmentDate().then(function(){
        return self.getConceptNames();
      }).then(function() {
        return self.getAllObs();
      }).then(function(){
        self.updateChartData();
      });
    }

    getConfig() {
      // Set default maxResults if not defined
      if (angular.isUndefined(this.config.maxResults)) {
        this.config.maxResults = 4;
      }
      // Parse maxAge to day count
      this.maxAgeInDays = this.widgetsCommons.maxAgeToDays(this.config.maxAge);
      if(angular.isDefined(this.config.encounterTypes)) {
        this.encounterTypes = this.config.encounterTypes.replace(/ /gi, "").split(",");
      }
      
      if (angular.isDefined(this.config.duringCurrentEnrollmentInProgram)) {
        this.duringCurrentEnrollmentInProgram = this.config.duringCurrentEnrollmentInProgram;
      }
      
      // Set default showLegend to true if not defined
      if (angular.isUndefined(this.config.showLegend)) {
        this.config.showLegend = true;
      }

      if(angular.isDefined(this.config.hideConcepts)) {
        this.hideConcepts = this.config.hideConcepts.replace(/ /gi, "").split(",");
      }

      this.options = {
        legend: {
          display: this.config.showLegend,
          position: 'top'
        },
        scales: {
          yAxes: [
            {
              id: 'y-axis-1',
              type: 'linear',
              display: true,
              position: 'left'
            }
          ]
        }
      };

      if (this.config.type && this.config.type === 'logarithmic') {
        this.options.scales.yAxes[0] = {
          id: 'y-axis-1',
          type: 'logarithmic',
          display: true,
          position: 'left',
          ticks: {
            callback: function (value, index, values) {
              if (value.length) {
                return Number(value).toLocaleString();
              }
              return value;
            },
            autoSkip: true,
            maxTicksLimit: 5
          }
          };
      }
      // Parse the comma delimited concept UUIDs into an array
      // First, remove all whitespaces from the input string
      let tempArray = this.config.conceptId.replace(/ /gi, "").split(",");

      this.conceptArray = tempArray.map(function(concept) {
        return {
          uuid: concept,
          type: "obs",
          legend: true,
          display: ''
        };
      });

      if (this.config.function && this.config.function.length > 0) {
        this.parseFunctionConfig(this.config.function)
      }
    }

    parseFunctionConfig(fns){
      if (fns) {
        // remove empty spaces
        let fnsArray = fns.replace(/ /gi, "").split(";");
        if (angular.isDefined(fnsArray) && fnsArray !== null && fnsArray.length > 0 ) {
          for (let i=0; i < fnsArray.length; i++) {
            let params = fnsArray[i];
            if (params && params.length > 0) {
              let line = params.substring(
                params.indexOf("(") + 1,
                params.indexOf(")")
              );
              if(line && line.length > 0) {
                let paramsArray = line.split(",");
                if (paramsArray && paramsArray.length > 0) {
                  this.FunctionManager.execute(...paramsArray);
                }
              }
            }
          }
        }
      }
    }

    getXAxisKeys() {
      let keys = null;
      if (angular.isDefined(this.xAxis) && this.xAxis) {
        keys = Object.keys(this.xAxis);
        if (keys && keys.length > 0) {
          keys.sort(function (a, b) {
            return parseInt(b) - parseInt(a);
          });
        }
      }
      return keys;
    }

}
