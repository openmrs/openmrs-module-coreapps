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

        // Method used to define days since given date
        dateToDaysAgo(date) {
            const time = new Date(date).getTime();
            const seconds = Math.floor((new Date().getTime() - time) / 1000);
            const interval = Math.floor(seconds / 86400);
            let days = 0;
            if (interval > 1) {
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
                today.setDate(today.getYear()-(parseInt(maxAge)));
            } else {
                return null;
            }
            return today;
        };
}