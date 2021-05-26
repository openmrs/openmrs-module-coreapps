/**
 * Returns date translated according to preferred locale when Date Format displays the month as an abbreviation (MMM).
 * Ex: Date: 02-Jan-2021 Format: DD-MMM-YYYY Locale: fr_FR --> Return: 02-janv.-2021
 * @param {object} date Input Date
 * @param {string} format Date Format
 * @param {string} locale The preferred locale
 */
function formatDatetime(date, format, locale) {
    const DEFAULT_FORMAT = 'DD.MMM.YYYY, HH:mm:ss';
    if(locale == undefined) {
        locale= 'en'
    }
    try {
        moment.locale(locale);
        return moment(date).format(format);
    } catch (err) {
        return moment(date).format(DEFAULT_FORMAT);
    }
}

function formatDate(date, format, locale) {
    const DEFAULT_FORMAT = 'YYYY-MMM-DD';
    if(locale == undefined) {
        locale= 'en'
    }
    try {
        moment.locale(locale);
        return moment(date).format(format);
    } catch (err) {
        return moment(date).format(DEFAULT_FORMAT);
    }
}


function formatTime(date, format) {
    const DEFAULT_FORMAT = 'HH:mm';
    try {
        return moment(date).format(format);
    } catch (err) {
        return moment(date).format(DEFAULT_FORMAT);
    }
}
