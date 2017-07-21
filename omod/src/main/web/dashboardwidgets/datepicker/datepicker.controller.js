import angular from 'angular';

export default class DatepickerController  {
    constructor($document, $element, $scope, $filter) {
        'ngInject';

        Object.assign(this, { $document, $element, $scope, $filter });
    }

    $onInit() {
        this.$document.ready(() => {
            $(this.$element).datepicker({
                format: this.convertDateFormat(this.format),
                autoclose: true,
                container: "html",
                language:  this.language ? this.language : 'en'
            }).on("changeDate", (e) => {
                if (e.date != null) {
					if (this.ngModel == null || this.stripTime(this.ngModel).getTime() !== this.stripTime(e.date).getTime()) {
						//apply changes if not triggered by the watch
						this.$scope.$apply(() => {
                            this.ngModel = this.stripTime(e.date);
						});
					}
                }
            });

            this.$scope.$watch(() => { return this.ngModel; },
                (value) => { this.updateDates(); }
            );

            this.$scope.$watch(() => { return this.startDate; },
                (value) => { this.updateDates(); }
            );
            
            this.$scope.$watch(() => { return this.endDate; },
                (value) => { this.updateDates(); }
            )
        });
    }

    formatDate(date) {
        if (date == null) {
            return null;
        } else {
            return this.$filter('date')(date, this.format);
        }
    }

    updateDates() {
        //I need to update all at once due to a bug in the datepicker, which resets the selected date.
        //I use formatDate to strip off the time part of the date so that endDate and startDate
        //do not have to be set to end of a day and start of a day.
        $(this.$element).datepicker("setStartDate", this.formatDate(this.startDate));
        $(this.$element).datepicker("setEndDate", this.formatDate(this.endDate));
        $(this.$element).datepicker("setDate", this.formatDate(this.ngModel));
    }

    stripTime(date) {
        if (date == null) {
            return null;
        } else {
            return new Date(date.getFullYear(), date.getMonth(), date.getDate());
        }
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
