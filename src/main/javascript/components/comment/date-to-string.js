module.exports = function (date) {
    var year = padZeros(date.getFullYear(), 4);
    var month = padZeros(date.getMonth() + 1, 2);
    var dayOfMonth = padZeros(date.getDate(), 2);
    var hours = padZeros(date.getHours(), 2);
    var minutes = padZeros(date.getMinutes(), 2);

    return year + '-' + month + '-' + dayOfMonth + ' at ' + hours + ':' + minutes;
};

function padZeros(number, numDigits) {
    var numberStr = number + '';
    return numberStr.length >= numDigits
        ? numberStr
        : new Array(numDigits - numberStr.length + 1).join('0') + numberStr;
}
