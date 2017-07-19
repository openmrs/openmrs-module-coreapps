import angular from 'angular';

export default class DatepickerController  {
    constructor($document, $element, $scope) {
        'ngInject';

        Object.assign(this, { $document, $element, $scope });
    }

    $onInit() {
        this.$document.ready(() => {
            $(this.$element).datepicker({
                format: this.convertDateFormat(this.format),
                startDate: this.startDate,
                endDate: this.endDate,
                autoclose: true,
                container: "html"
            }).on("changeDate", (e) => {
                if (e.date != null) {
					if (this.ngModel == null || this.ngModel.getTime() !== e.date.getTime()) {
						//apply changes if not triggered by the watch
						this.$scope.$apply(() => {
							this.ngModel = e.date;
						});
					}
                }
            });

            this.$scope.$watch(() => { return this.ngModel; },
                (value) => { $(this.$element).datepicker("setDate", value); }
            );

         /*   this.$scope.$watch(() => { return this.startDate; },
                (value) => { $(this.$element).datepicker("setStartDate", value); }
            );

            this.$scope.$watch(() => { return this.endDate; },
                (value) => { $(this.$element).datepicker("setEndDate", value); }
            );*/
        });
    }

    convertDateFormat(dateFormat) {
        let mappings = {
            "MMMM": "MM",
            "MMM": "M",
            "MM": "mm",
            "M": "m",
            "yyyy": "yy",
            "yy": "y",
            'EEEE': 'DD',
            'EEE': 'D'
        };

        let regex = "";
        for (let mapping in mappings) {
            regex = regex + mapping + '|';
        }
        regex = regex.substring(0, regex.length - 1);

        dateFormat = dateFormat.replace(new RegExp(regex, "g"), (match) => {
            if (mappings[match]) {
                return mappings[match];
            } else {
                return match;
            }
        });
        return dateFormat;
    }
};
