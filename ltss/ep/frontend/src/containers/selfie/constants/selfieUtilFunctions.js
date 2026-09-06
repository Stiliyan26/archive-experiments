const getCurrentDate = () => {
    const today = new Date();
    const year = today.getFullYear();
    const month = String(today.getMonth() + 1).padStart(2, '0'); // Months are 0-based
    const day = String(today.getDate()).padStart(2, '0');
    return `${year}-${month}-${day}`;
};


function getLastDayOfMonthStr(currentMonth) {
    if (
        currentMonth == 1 || // Jan
        currentMonth == 3 || // Mar
        currentMonth == 5 || // May
        currentMonth == 7 || // Jul
        currentMonth == 8 || // Aug
        currentMonth == 10 || // Oct
        currentMonth == 12 // Dec
    ) {
        return "31";

    } else if (
        currentMonth == 4 || // Apr
        currentMonth == 6 || // Jun
        currentMonth == 9 || // Sep
        currentMonth == 11 // Nov 
    ) {
        return "30";

    } else if (currentMonth == 2) { // Feb (Check for Leap Year)
        const currentYear = new Date().getFullYear();
        const lastDayOfFebruary = new Date(currentYear + '-2-29').getDate();

        if (lastDayOfFebruary == 29) {
            return "29";
        }

        return "28";
    }
}

function formatMonthStr(monthIndex) {
    switch (monthIndex) {
        case "1":
            return "01";
        case "2":
            return "02";
        case "3":
            return "03";
        case "4":
            return "04";
        case "5":
            return "05";
        case "6":
            return "06";
        case "7":
            return "07";
        case "8":
            return "08";
        case "9":
            return "09";
        case "10":
            return "10";
        case "11":
            return "11";
        case "12":
            return "12";
        default:
            break;
    }
}

export { getCurrentDate, getLastDayOfMonthStr, formatMonthStr };