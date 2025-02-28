const path = require('path');
const moment = require('moment');
const momenttz = require('moment-timezone');
const _ = require('lodash');

const dotenv = require('dotenv');

dotenv.config();

// const iststring = ist.format('YYYY-MM-DD HH:mm:ss');
// console.log(momenttz(iststring).tz('America/Toronto').format('YYYY-MM-DD HH:mm:ss'));
// console.log(momenttz().tz('America/Toronto').format('YYYY-MM-DD HH:mm:ss'));
// console.log(ist.tz('America/Los_Angeles').format('YYYY-MM-DD HH:mm:ss'));
// console.log(ist.tz('America/Los_Angeles').format('ha z'));

// const dt = '2021-06-30T12:14:29.000Z';
const dt = '2021-07-26T09:41:27.000Z';
console.log('from db=>', moment(dt).format('YYYY-MM-DD HH:mm:ss'));
console.log('>>> ', moment(dt).toDate());

const ist = momenttz.tz(dt, 'Asia/Kolkata').format('YYYY-MM-DD HH:mm:ss z Z');
const tor = momenttz.tz(dt, 'Asia/Dubai').format('YYYY-MM-DD HH:mm:ss z Z');
const los = momenttz.tz(dt, 'America/Los_Angeles').format('YYYY-MM-DD HH:mm:ss z Z');
console.log(momenttz.tz.zone('Asia/Calcutta').abbr(moment(dt).millisecond()));

console.log(ist, tor, los);
// console.log(momenttz.tz.guess(true));
// console.log(momenttz.tz.zonesForCountry('IN'));
// console.log(momenttz.tz.zonesForCountry('US'));

// get all country codes
// console.log(momenttz.tz.countries());

const dt1 = '2021-08-12';
const time = '08:51:00';

const la = momenttz.tz(`${dt1} ${time}`, 'Asia/Calcutta');
// const la = momenttz.tz(`${dt1} ${time}`, 'America/Los_Angeles');
console.log(la.toString(), ' <<< la');

const india = la.clone().tz('America/Los_Angeles');
console.log(india.toString(), ' <<< india');
console.log(india.toDate());

const lamoment = momenttz().tz('America/Los_Angeles');
lamoment.set({ hour: 0, minute: 0, second: 0 });
const ladt = momenttz().tz('America/Los_Angeles').format('YYYY-MM-DD HH:mm:ss z Z');
const lad = momenttz().tz('America/Los_Angeles').format('YYYY-MM-DD');
console.log('current la time =>', ladt);
console.log('current la date =>', lad);

// const ist1 = lamoment.clone().tz('Asia/Calcutta');
// const todate = ist1.subtract(1, 'days');
// const fromdate = todate.clone().subtract(6, 'days');
// const fromdtStr = fromdate.format('YYYY-MM-DD HH:mm:ss')
// const todtStr = todate.format('YYYY-MM-DD HH:mm:ss')
// console.log('current ist => ', ist1.format('YYYY-MM-DD HH:mm:ss z Z'));
// console.log(fromdtStr, todtStr)

const dayOfWeek = lamoment.day(); // 0-sunday to 6-sat
const fromdateStrInIST = lamoment.clone().day(0).format('YYYY-MM-DD'); // this week sunday
const todateStrInIST = (true) ? lamoment.format('YYYY-MM-DD') : lamoment.subtract(1, 'days').format('YYYY-MM-DD'); // today
console.log(fromdateStrInIST, todateStrInIST, dayOfWeek, lamoment.format('YYYY-MM-DD'));

console.log(la.fromNow());

console.log(momenttz.tz.zone('America/Los_Angeles').abbr(1403465838805)); // PDT
console.log(momenttz.tz.zone('America/Los_Angeles').abbr(new Date())); // PDT
console.log(momenttz.tz.zone('Asia/Dubai').abbr(new Date())); // PDT

const dt11 = '2021-11-20'; // yyyy-mm-dd
const time11 = '16:24:11'; // hh24:mi:ss
// const zone = 'Asia/Calcutta';
const zone = 'Asia/Dubai';

const incomingTZ = momenttz.tz(`${dt11} 00:00:00`, zone);
console.log('?????????????? ', incomingTZ.format('YYYY-MM-DD HH:mm:ss z Z'));
// const zonefrom = incomingTZ.startOf('day');
// const zoneto = incomingTZ.clone().endOf('day')
//
// console.log(zonefrom, zoneto);
// const zoneFrom = momenttz().tz(zone).set('hour', 0).set('minute', 0).set('second', 0);
// const zoneEnd = zoneFrom.clone().endOf('day');
// const zoneEnd = zoneFrom.clone().endOf('week');
// next month
// const zoneFrom = momenttz().tz(zone).add(1, 'month').startOf('month').set('hour', 0).set('minute', 0).set('second', 0);
// const zoneEnd = zoneFrom.clone().endOf('month');

// const zoneFrom = momenttz().tz(zone).set('hour', 0).set('minute', 0).set('second', 0);
// const zoneTo = zoneFrom.clone().add(7, 'day').endOf('day'); // next 7 days
// const zoneTo = zoneFrom.clone().add(15, 'day').endOf('day'); // next 15 days
// const zoneTo = zoneFrom.clone().add(30, 'day').endOf('day'); // next 30 days

// console.log(zoneFrom.toString(), zoneTo.toString());
// convert to ist
// const istFrom = zoneFrom.clone().tz('Asia/Calcutta');
// const istTo = zoneTo.clone().tz('Asia/Calcutta');

// console.log(istFrom.toString(), istTo.toString());

// convert to ist
// const istTZFrom = incomingTZ.clone().tz('Europe/Dublin');
// const istFrom = istTZFrom.format('YYYY-MM-DD HH:mm:ss z');
// console.log(istFrom)
// const istTZFrom = zonefrom.clone().tz('Asia/Calcutta');
// const istTZTo = zoneto.clone().tz('Asia/Calcutta');
// const istFrom = istTZFrom.format('YYYY-MM-DD HH:mm:ss');
// const istTo = istTZTo.format('YYYY-MM-DD HH:mm:ss');
// console.log(istFrom, istTo)

// const zoneFrom = momenttz().tz('Asia/Calcutta').subtract(1, 'month').startOf('month').format('DD-MM-YYYY');
// const zoneTo = momenttz().tz('Asia/Calcutta').subtract(1, 'month').endOf('month').format('DD-MM-YYYY');
//
// console.log(zoneFrom, zoneTo)

const curHour = momenttz().tz('Africa/Banjul').hour();
const z1 = momenttz().tz('Africa/Banjul').set('hour', 0).set('minute', 0)
    .set('second', 0);
console.log(curHour);

for (let i = 0; i <= curHour; i++) {
    const z1hrfrom = z1.format('YYYY-MM-DD HH:mm:ss');
    const z1hrto = z1.clone().endOf('hour');
    const z1hrtostr = z1hrto.format('YYYY-MM-DD HH:mm:ss');
    const istfrom = z1.clone().tz('Asia/Calcutta').format('YYYY-MM-DD HH:mm:ss');
    const istto = z1hrto.clone().tz('Asia/Calcutta').format('YYYY-MM-DD HH:mm:ss');
    z1.add(1, 'hour');
    console.log(z1hrfrom, z1hrtostr, istfrom, istto);
}
//

// console.log(z1hrfrom, z1hrtostr, istfrom, istto)

// momenttz.tz(`${dt11} ${'00:59:59'}`, zone);
// console.log(z1.format('YYYY-MM-DD HH:mm:ss'))
//
// const zfts = z1.clone().startOf('day');
// const istfts = zfts.clone().tz('Asia/Calcutta').startOf('day');
// // console.log(istfts.format('YYYY-MM-DD HH:mm:ss'))
// const zoneName = 'Asia/Dubai';
// const zoneFrom = momenttz().tz(zoneName).startOf('week');
// const zoneTo = momenttz().tz(zoneName).endOf('day');

// console.log(zoneFrom.format('YYYY-MM-DD HH:mm:ss'), zoneTo.format('YYYY-MM-DD HH:mm:ss'));

// const zoneName = 'Asia/Calcutta';
const zoneName = 'Australia/Sydney';
const currentDateTZStr = momenttz().tz(zoneName).format('YYYY-MM-DD');
console.log('current day in ', zoneName, 'is ', currentDateTZStr);
let typeofdate = '';
const fdate = '2021-11-10';
let tdate = '2021-11-22';

if (moment(tdate).isAfter(currentDateTZStr, 'day')) {
    console.log('tdate is after zones current date');
    tdate = currentDateTZStr;
} else {
    console.log('tdate not after zones current date');
}

if (moment(fdate).isSame(tdate, 'day')) {
    // chk if its today
    if (moment(currentDateTZStr).isSame(tdate, 'day')) {
        typeofdate = 'onlytoday';
    } else {
        typeofdate = 'onlyprev';
    }
} else {
    // chk if it has today
    if (moment(currentDateTZStr).isSame(tdate, 'day')) {
        typeofdate = 'both';
    } else {
        typeofdate = 'onlyprev';
    }
}
console.log(typeofdate);
console.log(zoneFrom.format('YYYY-MM-DD HH:mm:ss'), zoneTo.format('YYYY-MM-DD HH:mm:ss'));

console.log(moment.tz.zone('Australia/Sydney').abbr(new Date()));
