import angular from 'angular';

export default class DatepickerController  {
    constructor($document, $element) {
        'ngInject';

        Object.assign(this, { $document, $element });
    }

    $onInit() {
        this.$document.ready(() => {
            $(this.$element).datepicker({
                format: this.convertDateFormat(this.format),
                autoclose: true,
            }).on("changeDate", (e) => {
                this.$element.scope().$apply(() => {
                    this.ngModel = e.date;
                });
            });
            $(this.$element).datepicker("setDate", this.ngModel);
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
