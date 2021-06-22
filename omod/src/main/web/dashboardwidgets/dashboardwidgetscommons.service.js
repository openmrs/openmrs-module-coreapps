import moment from 'moment';

export default class WidgetsCommons {

    // Method used to define days since given date and return it as message
    dateToDaysAgoMessage(date) {
        let days = this.dateToDaysAgo(date);

        if (days == 0) {
            return "today"
        }
        else if (days == 1) {
            return days + " day ago"
        }
        else {
            return days + " days ago"
        }
    };

    //Number of days since the given date
    daysSinceDate(date) {
        let days = 0;
        if (date !== null) {
            let givenDate = new Date(date).setHours(0,0,0,0);
            let today = new Date().setHours(0,0,0,0);
            let seconds = Math.floor((today - givenDate) / 1000);
            days = Math.floor(seconds / 86400);
        }

        return days;
    };

    // Method used to define days since given date
    dateToDaysAgo(date) {
        const time = new Date(date).getTime();
        const seconds = Math.floor((new Date().getTime() - time) / 1000);
        const interval = Math.floor(seconds / 86400);
        let days = 0;
        if (interval > 0) {
            days = interval;
        }
        return days;
    };

    // Method used to parse maxAge to day count
    maxAgeToDays(maxAge) {
        if (typeof maxAge === 'undefined') {
            return undefined;
        } else {
            let days = 0;
            const values = maxAge.split(" ");
            for (const value of values) {
                if (value.includes("d")) {
                    days += parseInt(value.replace("d", ""));
                }
                if (value.includes("w")) {
                    days += parseInt(value.replace("w", "")) * 7;
                }
                if (value.includes("m")) {
                    days += parseInt(value.replace("m", "")) * 30;
                }
                if (value.includes("y")) {
                    days += parseInt(value.replace("y", "")) * 365;
                }
            }
            return days;
        }
    };

    maxAgeToDate(maxAge) {
        if(typeof maxAge === 'undefined')
            return null;

        var today = new Date();
        if( maxAge.indexOf('d') !== -1 ){
            maxAge = maxAge.replace('d', '');
            today.setDate(today.getDate()-parseInt(maxAge));
        } else if( maxAge.indexOf('w') !== -1 ){
            maxAge = maxAge.replace('w', '');
            today.setDate(today.getDate()-(parseInt(maxAge)*7));
        } else if( maxAge.indexOf('m') !== -1 ){
            maxAge = maxAge.replace('m', '');
            today.setMonth(today.getMonth()-parseInt(maxAge));
        } else if( maxAge.indexOf('y') !== -1 ){
            maxAge = maxAge.replace('y', '');
            today.setFullYear(today.getFullYear()-(parseInt(maxAge)));
        } else {
            return null;
        }
        return today;
    };

    /**
     * Returns the most appropriate name given a concept and a name type preference
     *
     * @param {object} concept Must have rep including `display,names:(voided,locale,conceptNameType,localePreferred,name)`
     * @param {string} nameType One of "FSN", "shortName", "preferred"
     * @param {string} locale The preferred locale
     */
    getConceptName(concept, nameType, locale) {
        const names = concept.names.filter(n => !n.voided && n.locale === locale);
        const fsn = names.filter(n => n.conceptNameType === "FULLY_SPECIFIED")[0];
        const short = names.filter(n => n.conceptNameType === "SHORT")[0];
        const shortEn = concept.names.filter(n => !n.voided && n.locale === 'en' && n.conceptNameType === "SHORT")[0];
        const preferred = names.filter(n => n.localePreferred)[0];
        let resultName;
        if (nameType === "FSN") {
            resultName = fsn || preferred;
        } else if (nameType === "shortName") {
            resultName = short || shortEn;
        } else if (nameType === "preferred") {
            resultName = preferred;
        }
        return resultName ? resultName.name : concept.display;
    }

    hasDatatypeDateOrSimilar(concept) {
        return concept.datatype && ['8d4a505e-c2cc-11de-8d13-0010c6dffd0f',
            '8d4a591e-c2cc-11de-8d13-0010c6dffd0f',
            '8d4a5af4-c2cc-11de-8d13-0010c6dffd0f'].includes(concept.datatype.uuid)
    }

    hasDatatypeCoded(concept) {
        return concept.datatype && concept.datatype.uuid === "8d4a48b6-c2cc-11de-8d13-0010c6dffd0f";
    }

    // use as `isDrug(obs.value)`
    isDrug(obj) {
        return Boolean(obj.concept);
    }

    /**
     * Returns date translated according to preferred locale when Date Format displays the month as an abbreviation (MMM).
     * Ex: Date: 02-Jan-2021 Format: DD-MMM-YYYY Locale: fr --> Return: 02-janv.-2021
     *
     * @param {object} date Input Date
     * @param {string} format Date Format
     * @param {string} locale The preferred locale
     */
    formatDate(date, format, locale) {
        try{
            moment.locale(locale);
            return moment(date).format(format);
        } catch(err) {
            return moment(date).format("DD.MMM.YYYY");
        }
    }

    /**
     * Returns date and time translated according to preferred locale.
     * Ex: Date: 02-Jan-2021 15:10 Format: DD.MMM.YYYY HH:mm Locale: fr --> Return: 02-janv.-2021 15:10
     *
     * @param {object} date Input Datetime
     * @param {string} format Date Format
     * @param {string} locale The preferred locale
     */
    formatDateTime(date, format, locale) {
        try{
            moment.locale(locale);
            return moment(date).format(format);
        } catch(err) {
            return moment(date).format("DD.MMM.YYYY HH:mm");
        }
    }

    /**
     * Return true if the user has the System Developer role
     *
     * @param user
     * @returns {*}
     */
    isSystemDeveloper(user){
        return user.roles.some( (p) => { return p.name === 'System Developer'; });
    }
}
