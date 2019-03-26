export default class ObsGraphController {
    constructor($q, $filter, openmrsRest, widgetsCommons) {
        'ngInject';

        Object.assign(this, { $q, $filter, openmrsRest, widgetsCommons});
    }

    $onInit() {

        var self = this;
        self.xAxis = {};
        // Max age of obs to display
        this.maxAgeInDays = undefined;
        this.conceptRep = "custom:(uuid,display,name:(display),datatype:(uuid,display))";
        this.customRep="custom:(uuid,display,obsDatetime,value,concept:(uuid,display,name:(display),datatype:(uuid,display)))";


        // Chart data
        this.series = [];
        this.labels = [];
        this.data = [];
        this.legend = [];

        this.dataset = [];
        /* Here is an example of the structure of this arrray of objects
          [
            {
              "conceptUuid": "3ce93b62-26fe-102b-80cb-0017a47871b2",
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
              "conceptUuid": "3ce93cf2-26fe-102b-80cb-0017a47871b2",
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

        this.getConfig();

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
              for (let i = 0; i < this.dataset.length; i++) {
                let obj = this.dataset[i];
                if (angular.isDefined(obj.values) && obj.values !== null && (Object.keys(obj.values)).length > 0) {
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
                }
              }
            }
          }

        };

        this.getConceptNames = function() {
          let promises = [];
          if (this.conceptArray !== null && this.conceptArray.length > 0) {
            for (let i = 0; i < this.conceptArray.length; i++) {
              let promisedObs = this.openmrsRest.get('concept/' + this.conceptArray[i], {
                v: this.conceptRep
              }).then(function(concept) {
                return concept;
              });
              promises.push(promisedObs);
            }
          }
          this.$q.all(promises).then(function(concepts) {
            for (let j=0; j< concepts.length; j++) {
              self.series.push(concepts[j].display);
            }
          });

        };

        this.getAllObs = function() {
          let promises = [];
          if (this.conceptArray !== null && this.conceptArray.length > 0) {
            for (let i = 0; i < this.conceptArray.length; i++) {
              let promisedObs = this.openmrsRest.list('obs', {
                patient: this.config.patientUuid,
                v: this.customRep,
                limit: this.config.maxResults,
                concept: this.conceptArray[i]
              }).then(function(response) {
                return response.results;
              });
              promises.push(promisedObs);
            }
          }
          this.$q.all(promises).then(function(data) {

            for (let j=0; j< data.length; j++) {
              let conceptObject = {};

              let obsArray = data[j];
              if (obsArray.length > 0) {
                //we have at least one observation
                conceptObject.conceptUuid = obsArray[0].concept.uuid;
                conceptObject.display = obsArray[0].concept.display;
                conceptObject.values = {};
                for (let k = 0; k < obsArray.length; k++) {
                  let obs = obsArray[k];
                  if (obs.concept.datatype.display == 'Numeric') {
                    // Don't add obs older than maxAge
                    let xValue = self.widgetsCommons.daysSinceDate(obs.obsDatetime);
                    if (angular.isUndefined(self.maxAgeInDays) || xValue <= self.maxAgeInDays) {
                      // Add obs data for chart display
                      var obsDate = self.$filter('date')(new Date(obs.obsDatetime), self.config.dateFormat);
                      conceptObject.values[xValue] = obs.value;
                      self.xAxis[xValue] = obsDate;
                    }
                  }
                }
              }
              self.dataset.push(conceptObject);
            }
            self.orderXAxis();
            self.updateChartData();
          });
      };

      this.getConceptNames();
      this.getAllObs();

    }

    getConfig() {
      // Set default maxResults if not defined
      if (angular.isUndefined(this.config.maxResults)) {
        this.config.maxResults = 4;
      }
      // Parse maxAge to day count
      this.maxAgeInDays = this.widgetsCommons.maxAgeToDays(this.config.maxAge);

      this.options = {
        legend: {
          display: true,
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
      this.conceptArray = this.config.conceptId.replace(" ", "").split(",");
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
