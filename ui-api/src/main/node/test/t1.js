const path = require('path');
const moment = require('moment');
const momenttz = require('moment-timezone');
const _ = require('lodash');

const dotenv = require('dotenv');

dotenv.config();

// const file = 'demo_path.js';
// const fileext = path.extname(file);
// const justFilename = path.basename(file, fileext);
// const filename = path.basename(file);
// const basePath = process.env.CAMPAIGN_FILES_BASE_PATH;
// console.log(basePath);
// const newFilename = path.join(basePath, 'client1', `${justFilename}_${moment().format('YYYYMMDD')}${fileext}`);

// console.log(fileext, justFilename, filename, newFilename);

const dt = '2021-06-30T12:14:29.000Z';
console.log(moment(dt).format('YYYY-MM-DD HH:mm:ss'));
console.log('>>> ', moment(dt).toDate());

const ist = momenttz.tz(dt, 'Asia/Kolkata').format('YYYY-MM-DD HH:mm:ss z Z');
const tor = momenttz.tz(dt, 'America/Toronto').format('YYYY-MM-DD HH:mm:ss z Z');
const los = momenttz.tz(dt, 'America/Los_Angeles').format('YYYY-MM-DD HH:mm:ss Z');

console.log(ist, tor, los);
console.log(momenttz.tz.guess(true));
console.log(momenttz.tz.zonesForCountry('IN'));
console.log(momenttz.tz.zonesForCountry('US'));

// get all country codes
// console.log(momenttz.tz.countries());

console.log(_.isEmpty(null));
console.log(_.isEmpty(_.trim(' ')));
console.log(_.isEmpty(undefined));
console.log(_.isNull(''));

const a = ['a', '2', '3'];
console.log(_.join(a, ','));
let ids = '';
_.forEach(a, (v, i) => {
  ids += `'${v}',`;
});
// remove comma at the end
ids = _.trimEnd(ids, ',');
console.log(ids);

// [fastify.nanoid(), id, rFilename, filename, ext, fileloc, valid]
const va = [
  [
    '',
    'g0zfw4zqowg3hccdy208ywwoqzwzsrtlrhfg',
    'cq_3rwpcue3zpq18qiz322riyl65y1nt0hf6ltz_20210716.csv',
    'cq_3rwpcue3zpq18qiz322riyl65y1nt0hf6ltz_20210716.csv',
    '.csv',
    ' /home/teamwork/cm/campaigns/wintest/cq_3rwpcue3zpq18qiz322riyl65y1nt0hf6ltz_20210716.csv',
    34,
  ],
  [
    null,
    'g0zfw4zqowg3hccdy208ywwoqzwzsrtlrhf',
    'cq_3rwpcue3zpq18qiz322riyl65y1nt0hf6ltz_2021071.csv',
    'cq_3rwpcue3zpq18qiz322riyl65y1nt0hf6ltz_2021071.csv',
    '.cs',
    ' /home/teamwork/cm/campaigns/wintest/cq_3rwpcue3zpq18qiz322riyl65y1nt0hf6ltz_20210716.cs',
    35,
  ],
];
const unziped = _.unzip(va);
console.log(unziped);
console.log(_.join(_.get(unziped, 0), ','));

const users = [
  { user: 'barney', ag_e: 36, active: false },
  { user: 'fred', ag_e: 40, active: false },
];

console.log('>>>>> ', _.map(users, (o) => o.ag_e));

console.log(_.flattenDeep([[]]));
console.log(_.isEmpty(_.flattenDeep([[]])));
const s = '12.0234~a';
console.log(_.split(s, '~'));

console.log(+'12.005000');

const a1 = [1, 2, 3, 4, 5];
console.log(_.slice(a1, 0, 40));

const resultSSParentUser = [
  {
    sub_service_name: 'API',
    sub_service: 'api',
    service: 'sms',
  },
  {
    sub_service_name: 'Campaign UI',
    sub_service: 'cm',
    service: 'sms',
  },
  {
    sub_service_name: 'INTL',
    sub_service: 'international',
    service: 'sms',
  },
  {
    sub_service_name: 'SMPP',
    sub_service: 'smpp',
    service: 'whats',
  },
];
const resultSSThisUser = [
];

const gbThisUser = _.groupBy(resultSSThisUser, 'service');
console.log(gbThisUser);
_.forEach(resultSSParentUser, (obj) => {
  const service = _.get(obj, 'service');
  const sub_service = _.get(obj, 'sub_service');
  const arr = _.map(_.get(gbThisUser, service), 'sub_service');
  if (_.includes(arr, sub_service)) {
    _.set(obj, 'enabled', 1);
  } else {
    _.set(obj, 'enabled', 0);
  }
});
// console.log(ss);

// console.log(_.includes(ss[1],'cm'));

console.log(+'12.005000000000000000000');
console.log(Number('12.005000000000000000000'));
if ((1)) {
  console.log('not emp');
} else {
  console.log('emp');
}

const c = ['k', 'm', 'b', 't'];

function cf(n, iteration) {
  if (n < 1000) {
    return `${n}`;
  }

  const d = (n / 100) / 10.0;
  const isRound = (d * 10) % 10 === 0;// true if the decimal part is equal to 0 (then it's trimmed anyway)
  const ld = d;

  const result = (d < 1000 // this determines the class, i.e. 'k', 'm' etc
    ? (`${d > 99.9 || isRound || (!isRound && d > 9.99) // this decides whether to trim the decimals
      ? (_.round(ld * 10 / 10, 1)) : `${_.round(ld, 1)}` // (int) d * 10 / 10 drops the decimal
      }${c[iteration]}${!isRound ? '' : ''}`) : cf(d, iteration + 1));

  return result;
}
let str = '';
for (const v of c) {
  str += ` group_ids like '%${v}%' or`;
}
const d = 'XLS 65K fmm.xls,XLSX 1 million fmm.xlsx,XLSX 10K fmm.xlsx';
const ar = _.split(d, ',');
const us = 0;
if (_.isNull(us) || _.isUndefined(us)) console.log('us not empty');
else console.log('us empty');

console.log(_.isNull(0), _.isUndefined(us));

const warr = _.split('', '~');
const srate = (warr[0] ? warr[0] : 0);
const drate = (warr[1] ? warr[1] : 0);
console.log(srate, drate);

console.log('>>', _.parseInt('-1'));

const selectedArr = _.split('Tue Oct 05 2021 00:00:00 GMT+0300', ' ');

// const parsed = moment(`${mm[3]}-${mm[1]}-${mm[2]} ${mm[4]}`, 'YYYY-MMM-DD HH:mm:ss');
//
// console.log(parsed.format('YYYY-MM-DD HH:mm:ss'));

console.log(`${selectedArr[3]}-${selectedArr[1]}-${selectedArr[2]} ${selectedArr[4]}`);
const selectedtsParsed = moment(`${selectedArr[3]}-${selectedArr[1]}-${selectedArr[2]} ${selectedArr[4]}`, 'YYYY-MMM-DD HH:mm:ss');
console.log(selectedtsParsed.format('YYYY-MM-DD HH:mm:ss'));
// Europe/Istanbul
console.log(moment.tz.zone('America/Los_Angeles').abbr(new Date()));
console.log(moment.tz.zone('Europe/Istanbul').abbr(new Date()));
console.log(moment.tz.zone('Asia/Calcutta').abbr(new Date()));
console.log(moment.tz.zone('Australia/Sydney').abbr(new Date()));
// console.log(moment.tz.zone('Australia/Sydney').format('YYYY-MM-DD'));
// moment.tz('2013-12-01', 'America/Los_Angeles').format('YYYY-MM-DD');

const from = '2021-10-05';
const to = '2021-10-06';
// const tz = 'Asia/Calcutta';
// const tz = 'Africa/Banjul';
const tz = 'Australia/Sydney';

const curtz = momenttz().tz(tz).format('YYYY-MM-DD');
console.log('cur date', curtz);

if (moment(from).isAfter(curtz, 'day')) {
  console.log('from is > cur');
}
if (moment(to).isAfter(curtz, 'day')) {
  console.log('to is > cur');
}

let typeofdate = '';
// check if the dates has today
if (moment(from).isSame(to, 'day')) {
  // chk if its today
  if (moment(curtz).isSame(to, 'day')) {
    typeofdate = 'onlytoday';
  } else {
    typeofdate = 'onlyprev';
  }
} else {
  // chk if it has today
  if (moment(curtz).isSame(to, 'day')) {
    typeofdate = 'both';
  } else {
    typeofdate = 'onlyprev';
  }
}

console.log(from, to, typeofdate);
//
// console.log(momenttz().tz('Asia/Calcutta').format('YYYY-MM-DD'))
// console.log(momenttz().tz('Australia/Sydney').format('YYYY-MM-DD'))
console.log(moment.tz('Australia/Sydney').format('Z'));
console.log(moment.tz('Asia/Calcutta').format('Z'));
const zoneFrom = momenttz.tz(`${from} 00:00:00`, tz);
const zoneTo = momenttz.tz(`${to} 23:59:59`, tz);

// console.log(zoneFrom.format('YYYY-MM-DD'), zoneTo.format('YYYY-MM-DD'))
// console.log(moment(curtz).isSame(to, 'day'))
// console.log(moment(to).isSame(to, 'day'))
console.log(Number('0.0935'));
console.log(_.round(_.floor(123.5)));
console.log(_.floor(123.9));

console.log(_.isNumber('123'));
console.log(_.isNumber('123 '));
console.log(_.isNumber('123 a'));
console.log(_.isNumber(Number('123 ')));
console.log(Number(_.trim(' 1 ')));
if (_.isNaN(Number(' '))) console.log('its nan');

const o = { a: 1, aa: '' };
console.log(_.get(o, 'aa', undefined));

// const t = '   "   1 234\'5"';
// const t = 'Dear {#var#} {#var#},\nYour appointment (Booking Id: {#var#}) with {#var#} {#var#} scheduled on {#var#}{#var#} is accepted. Please pay {#var#} using the below link\n{#var#}{#var#}{#var#}\nThe above link is valid for {#var#} minutes.\n\nThank you\nGleneagles Global Health City Chennai';
const t = 'Dear {#var#} {#var#},\r\\nYour appointment ';
let t1 = _.replace(t, /\\n/g, '\n');
console.log(t1);
t1 = _.replace(t1, /\r\n/g, '\n');
console.log(t1);

const s1 = 'பேரன்புடையீர் ,  காவேரி மருத்துவமனையின் வணக்கம் !!! One day meal plan பற்றிய தகவல் அறிய இந்த இணையதள முகவரியை {#var#} கிளிக் செய்யவும். {#var#} Kauvery Hospital, Trichy.';
const s2 = 'பேரன்புடையீர் ,  காவேரி மருத்துவமனையின் வணக்கம் !!! One day meal plan பற்றிய தகவல் அறிய இந்த இணையதள முகவரியை {#var#} கிளிக் செய்யவும். {#var#} Kauvery Hospital, Trichy.';

console.log(_.eq(s1, s2));

console.log(Number('60000002000000012'));

const converted_rate = Number(0.011696011696 * 5.112268011696);
const converted_rate2 = _.floor(0.011696 * 10.123562, 6);
console.log(converted_rate);
console.log(converted_rate2);

const z1 = 0.011696;
const z2 = 10.123562;

console.log(z1 * z2);
console.log(0.118405 / z1);
console.log(0.118405181152 / z1);

console.log(0.3 * 1.571650);
console.log(0.471495 * 0.636274);

const userss = [
  { user: 'fred', age: 48 },
  { user: 'barney', age: 36 },
  { user: 'fred', age: 40 },
  { user: 'barney', age: 34 },
];

const eur = 0.011696;
console.log(1 / eur);
console.log(_.floor(1 / eur, 12));
console.log(_.ceil(1 / eur, 12));
console.log(_.ceil(1 / eur, 6));
console.log(Number('123234239.12345678'));
console.log(_.toNumber('123234239.12345678'));

const msg = 'this is a ""msg"';
console.log(_.replace(msg, '/"/g', '""'));
