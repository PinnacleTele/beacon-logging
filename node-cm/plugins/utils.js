const _ = require('lodash');
const moment = require('moment');
const momenttz = require('moment-timezone');
const bcryptjs = require('bcryptjs');
const https = require('https');
const Mustache = require('mustache');

const fp = require('fastify-plugin');
const { customAlphabet } = require('nanoid');
const axios = require('axios');
const permissionsPayload = require('../config/permissions_payload.json');

// eslint-disable-next-line func-names
const utils = async function (fastify, opts) {
    fastify.decorate('nanoid', () => {
        const nid = customAlphabet('1234567890abcdefghijklmnopqrstuvwxyz', 36);
        return nid();
    });

    fastify.decorate('nanoid22', () => {
        const nid = customAlphabet('1234567890abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ', 22);
        return nid();
    });

    fastify.decorate('nanoiduipass', () => {
        const nid = customAlphabet('0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz-', 8);
        return nid();
    });

    fastify.decorate('apipass', async (cli_id) => {
        const url = process.env.PASSWORD_SERVICE_URL;
        // get the pass from remote service
        const resp = await axios.get(`${url}?cli_id=${cli_id}&reset_api=1`);

        return resp.data;
    });

    fastify.decorate('smpppass', async (cli_id) => {
        const url = process.env.PASSWORD_SERVICE_URL;
        // get the pass from remote service
        const resp = await axios.get(`${url}?cli_id=${cli_id}&reset_smpp=1`);

        return resp.data;
    });

    fastify.decorate('getSMSLeft', async (wallet_bal, smsrate, dltrate) => {
        let smsleft = -1;

        // calculate the total sms that can be sent for this wallet balance
        if (+wallet_bal > 0) {
            const rates = smsrate + dltrate;
            if (rates > 0) smsleft = _.floor(+wallet_bal / rates);
            else smsleft = _.floor(wallet_bal);
        } else {
            smsleft = 0;
        }

        return smsleft;
    });

    fastify.decorate('sendMail', async (template, dataObj) => {
        
        const { EMAIL_URL, EMAIL_OWNERID, EMAIL_TOKEN, EMAIL_SMTP_USERNAME, EMAIL_FROM, EMAIL_REPLYTO } = process.env;

        const { subject, from_email, from_name, to_email, to_name, reply_to } = dataObj;
        const renderedTemplate = Mustache.render(template, dataObj);

        const reqPayload = {
            owner_id: EMAIL_OWNERID,
            token: EMAIL_TOKEN,
            smtp_user_name: EMAIL_SMTP_USERNAME,
            message: {
                html: renderedTemplate,
                subject,
                from_email: EMAIL_FROM,
                from_name,
                to: [
                    {
                        email: to_email,
                        name: to_name,
                        type: 'to',
                    },
                ],
                headers: {
                    'Reply-To': EMAIL_REPLYTO,
                    'X-Unique-Id': fastify.nanoid(),
                },
                attachments: [

                ],
                images: [

                ],
            },
        };

        //console.log(JSON.stringify(reqPayload));
        // process.env.NODE_TLS_REJECT_UNAUTHORIZED = 0;
        const resp = await axios.post(EMAIL_URL, reqPayload, { httpsAgent: new https.Agent({ rejectUnauthorized: false }) });

        return resp.data;
    });

    fastify.decorate('sendMailOTP', async (template, dataObj) => {
        
        const { EMAIL_URL_OTP, EMAIL_OWNERID_OTP, EMAIL_TOKEN_OTP, EMAIL_SMTP_USERNAME_OTP, EMAIL_FROM, EMAIL_REPLYTO } = process.env;

        const { subject, from_email, from_name, to_email, to_name, reply_to } = dataObj;
        const renderedTemplate = Mustache.render(template, dataObj);

        const reqPayload = {
            owner_id: EMAIL_OWNERID_OTP,
            token: EMAIL_TOKEN_OTP,
            smtp_user_name: EMAIL_SMTP_USERNAME_OTP,
            message: {
                html: renderedTemplate,
                subject,
                from_email: EMAIL_FROM,
                from_name,
                to: [
                    {
                        email: to_email,
                        name: to_name,
                        type: 'to',
                    },
                ],
                headers: {
                    'Reply-To': EMAIL_REPLYTO,
                    'X-Unique-Id': fastify.nanoid(),
                },
                attachments: [

                ],
                images: [

                ],
            },
        };

        //console.log(JSON.stringify(reqPayload));
        // process.env.NODE_TLS_REJECT_UNAUTHORIZED = 0;
        const resp = await axios.post(EMAIL_URL_OTP, reqPayload, { httpsAgent: new https.Agent({ rejectUnauthorized: false }) });

        return resp.data;
    });

    fastify.decorate('msginfo', async (cli_id, msg) => {
        const url = process.env.MSGINFO_SERVICE_URL;

        // get the data from remote service
        const reqPayload = { cli_id, header: '', message: msg };
        const resp = await axios.post(url, reqPayload);
        return resp.data;
    });

    fastify.decorate('hash', (str) => {
        const hash = bcryptjs.hashSync(str);
        return hash;
    });

    fastify.decorate('hasDoubleByte', (str) => {
        for (let i = 0, n = str.length; i < n; i += 1) {
            if (str.charCodeAt(i) > 127) {
                return true;
            }
        }
        return false;
    });

    fastify.decorate('coolFormat', (n, iteration) => {
        const c = ['k', 'm', 'b', 't'];

        if (_.isNaN(n)) return '0';
        if (_.isNull(n)) return '0';

        if (n < 1000) {
            return `${n}`;
        }

        const d = (n / 100) / 10.0;
        const isRound = (d * 10) % 10 === 0;// true if the decimal part is equal to 0 (then it's trimmed anyway)
        const ld = d;

        const result = (d < 1000 // this determines the class, i.e. 'k', 'm' etc
            ? (`${d > 99.9 || isRound || (!isRound && d > 9.99) // this decides whether to trim the decimals
                ? (_.round(ld * 10 / 10, 1)) : `${_.round(ld, 1)}` // (int) d * 10 / 10 drops the decimal
                }${c[iteration]}${!isRound ? '' : ''}`) : fastify.coolFormat(d, iteration + 1));

        return result;
    });

    fastify.decorate('constructEmailData', async (from_fname, from_lname, to_fname, to_lname, from_email, email, reply_to, subject, ui_pass_cust, api_pass_cust, smpp_pass_cust, services) => {
        const from_name = `${from_fname} ${from_lname}`;
        const servicesArr = [];

        _.forEach(services, (subServiceName) => {
            if (_.eq(_.toLower(subServiceName), 'cm')) {
                servicesArr.push({
                    service_name: 'Campaign Manager',
                    pass: ui_pass_cust,
                    pass_addl_text: 'Password: ',
                });
            }
            if (_.eq(_.toLower(subServiceName), 'api')) {
                servicesArr.push({
                    service_name: 'API',
                    pass: api_pass_cust,
                    pass_addl_text: 'API Key: ',
                });
            }
            if (_.eq(_.toLower(subServiceName), 'smpp')) {
                servicesArr.push({
                    service_name: 'SMPP',
                    pass: smpp_pass_cust,
                    pass_addl_text: 'Password: ',
                });
            }
        });

        const dataObj = { showLogo: false, from_email, from_name, subject, to_email: email, to_fname, to_lname, reply_to, services: servicesArr };
        return dataObj;
    });

    fastify.decorate('CONSTANTS', {
        TEMPLATE_NAME_NOT_UNIQUE_CODE: -410,
        GROUP_NAME_NOT_UNIQUE_CODE: -400,
        GROUP_IN_USE_CODE: -401,
        USERNAME_NOT_UNIQUE_CODE: -600,
        INVALID_USER_TYPE_IN_ACCOUNT_CREATION: -601,
        NOT_ENOUGH_WALLET_BAL: -602,
        INVALID_AUTH: -603,
        ACCOUNT_DEACTIVATED: -604,
        INVALID_EMAIL: -605,
        ACCOUNT_SUSPENDED: -606,
        ACCOUNT_EXPIRED: -607
    });

    fastify.decorate('getFromAndToDate', (zoneName, dateselectiontype, fdate, tdate, includeToday = false) => {
        let zoneFrom = null;
        let zoneTo = null;
        let fromdateInIST = null;
        let todateInIST = null;
        let fromdateStrInIST = null;
        let todateStrInIST = null;
        let fromdatetimeStrInIST = null;
        let todatetimeStrInIST = null;

        let isSameTz = false;
        let typeofdate = '';

        console.log(zoneName);
        // initialize with user timezone
        const momentInUserTZ = momenttz().tz(zoneName);
        const currentDateTZStr = momenttz().tz(zoneName).format('YYYY-MM-DD');
        const momentInLocalTZ = momenttz().tz(process.env.IST_ZONE_NAME);
        isSameTz = (momentInUserTZ.format('z') == momentInLocalTZ.format('z'));
        momentInUserTZ.startOf('day');

        console.log(isSameTz);

        if (_.eq('custom range', dateselectiontype)) {
            console.log('custom range incoming from and to dates => ', fdate, tdate);
            zoneFrom = momenttz.tz(`${fdate} 00:00:00`, zoneName);
            zoneTo = momenttz.tz(`${tdate} 23:59:59`, zoneName);

            // if tdate > current date in users zone, set tdate to current date of users zone
            if (moment(tdate).isAfter(currentDateTZStr, 'day')) {
                console.log('tdate is after zones current date');
                tdate = currentDateTZStr;
            }

            // check if the dates has today
            if (moment(fdate).isSame(tdate, 'day')) {
                // chk if its today
                if (moment(currentDateTZStr).isSame(tdate, 'day')) {
                    typeofdate = 'onlytoday';
                } else {
                    typeofdate = 'onlyprev';
                }
            } else if (moment(currentDateTZStr).isSame(tdate, 'day')) {
                // chk if it has today
                typeofdate = 'both';
            } else {
                typeofdate = 'onlyprev';
            }
        } else if (_.eq('today', dateselectiontype)) {
            zoneFrom = momenttz().tz(zoneName).startOf('day');
            zoneTo = zoneFrom.clone().endOf('day');
            typeofdate = 'onlytoday';
        } else if (_.eq('yesterday', dateselectiontype)) {
            zoneFrom = momenttz().tz(zoneName).startOf('day').subtract(1, 'days');
            zoneTo = zoneFrom.clone().endOf('day');
            typeofdate = 'onlyprev';
        } else if (_.eq('last 7 days', dateselectiontype)) {
            zoneFrom = momenttz().tz(zoneName).startOf('day').subtract(7, 'days');
            zoneTo = momenttz().tz(zoneName).endOf('day').subtract(1, 'days');
            typeofdate = 'onlyprev';
        } else if (_.eq('last 15 days', dateselectiontype)) {
            zoneFrom = momenttz().tz(zoneName).startOf('day').subtract(15, 'days');
            zoneTo = momenttz().tz(zoneName).endOf('day').subtract(1, 'days');
            typeofdate = 'onlyprev';
        } else if (_.eq('last 30 days', dateselectiontype)) {
            zoneFrom = momenttz().tz(zoneName).startOf('day').subtract(30, 'days');
            zoneTo = momenttz().tz(zoneName).endOf('day').subtract(1, 'days');
            typeofdate = 'onlyprev';
        } else if (_.eq('this week', dateselectiontype)) {
            zoneFrom = momenttz().tz(zoneName).startOf('week');
            zoneTo = momenttz().tz(zoneName).endOf('day');
            
            typeofdate = 'both';
        } else if (_.eq('this month', dateselectiontype)) {
            zoneFrom = momenttz().tz(zoneName).startOf('month');
            zoneTo = momenttz().tz(zoneName).endOf('day');
            typeofdate = 'both';
        } else if (_.eq('last month', dateselectiontype)) {
            zoneFrom = momenttz().tz(zoneName).subtract(1, 'month').startOf('month');
            zoneTo = momenttz().tz(zoneName).subtract(1, 'month').endOf('month');
            typeofdate = 'onlyprev';
        } else if (_.eq('this year', dateselectiontype)) {
            zoneFrom = momenttz().tz(zoneName).startOf('year');
            zoneTo = momenttz().tz(zoneName).endOf('day');
            typeofdate = 'both';
        } else {
            console.log(`*INVALID dateselectiontype param DEFAULTING to LAST 7 DAYS* incoming params => ${dateselectiontype}`);
            throw new Error('Invalid date selection');
        }

        // convert to local tz
        todateInIST = zoneTo.clone().tz(process.env.IST_ZONE_NAME);
        fromdateInIST = zoneFrom.clone().tz(process.env.IST_ZONE_NAME);

        fromdateStrInIST = fromdateInIST.format('YYYY-MM-DD');
        todateStrInIST = todateInIST.format('YYYY-MM-DD');
        fromdatetimeStrInIST = fromdateInIST.format('YYYY-MM-DD HH:mm:ss');
        todatetimeStrInIST = todateInIST.format('YYYY-MM-DD HH:mm:ss');
console.log(typeofdate,"%%%%%%.....")
        return {
            todateInIST,
            fromdateInIST,
            todateStrInIST,
            fromdateStrInIST,
            todatetimeStrInIST,
            fromdatetimeStrInIST,
            typeofdate,
        };
    });

    fastify.decorate('getFromAndToDateForSchedule', (zoneName, dateselectiontype, fdate, tdate) => {
        let zoneFrom = null;
        let zoneTo = null;
        let istFrom = null;
        let istTo = null;
        let istFromStr = null;
        let istToStr = null;
        let isSameTz = false;

        console.log(zoneName);
        zoneFrom = momenttz().tz(zoneName).set('hour', 0).set('minute', 0)
            .set('second', 0);

        isSameTz = (zoneFrom.format('z') == momenttz().tz(process.env.IST_ZONE_NAME).format('z'));
        console.log(isSameTz);

        if (_.eq('custom range', dateselectiontype)) {
            console.log('custom range incoming from and to dates => ', fdate, tdate);
            zoneFrom = momenttz.tz(`${fdate} 00:00:00`, zoneName);
            zoneTo = momenttz.tz(`${tdate} 23:59:59`, zoneName);
        } else if (_.eq('today', dateselectiontype)) {
            zoneTo = zoneFrom.clone().endOf('day');
        } else if (_.eq('next 7 days', dateselectiontype)) {
            zoneTo = zoneFrom.clone().add(7, 'day').endOf('day'); // next 7 days
        } else if (_.eq('next 15 days', dateselectiontype)) {
            zoneTo = zoneFrom.clone().add(15, 'day').endOf('day'); // next 15 days
        } else if (_.eq('next 30 days', dateselectiontype)) {
            zoneTo = zoneFrom.clone().add(30, 'day').endOf('day'); // next 30 days
        } else if (_.eq('this week', dateselectiontype)) {
            zoneTo = zoneFrom.clone().endOf('week');
        } else if (_.eq('this month', dateselectiontype)) {
            zoneTo = zoneFrom.clone().endOf('month');
        } else if (_.eq('next month', dateselectiontype)) {
            zoneFrom = zoneFrom.clone().add(1, 'month').startOf('month').set('hour', 0)
                .set('minute', 0)
                .set('second', 0);
            zoneTo = zoneFrom.clone().endOf('month');
        } else {
            console.log(`*INVALID dateselectiontype param DEFAULTING to NEXT 7 DAYS* incoming params => ${dateselectiontype}`);
            zoneTo = zoneFrom.clone().add(7, 'day').endOf('day'); // next 7 days
        }

        // convert to ist
        istFrom = zoneFrom.clone().tz(process.env.IST_ZONE_NAME);
        istTo = zoneTo.clone().tz(process.env.IST_ZONE_NAME);
        istFromStr = istFrom.format('YYYY-MM-DD HH:mm:ss');
        istToStr = istTo.format('YYYY-MM-DD HH:mm:ss');
        return { istFrom, istTo, istFromStr, istToStr };
    });

    fastify.decorate('getBaseTemplateForQuickLinks', () => {
        const template = {
            quicklinks: {
                campaign: {
                    cq: false,
                    cotm: false,
                    cmtm: false,
                    cg: false,
                    ct: false,
                },
                createnew: {
                    account: false,
                    template: false,
                    group: false,
                },
                myaccount: {
                    mysettings: false,
                    wallet: false,
                },
                report: {
                    summary: false,
                    detailed: false,
                    search: false,
                },
            },
        };

        return template;
    });
    fastify.decorate('fillWithDates', (fromDateStr, toDateStr, outputDateFormat) => {
        const arr = [];
        const from = moment(fromDateStr);
        const to = moment(toDateStr);
        let loop = true;
        while (loop) {
            arr.push(from.format(outputDateFormat));
            if (from.isSame(to)) {
                loop = false;
            } else {
                from.add(1, 'days');
            }
        }

        return arr;
    });
    fastify.decorate('fillWithMonths', (outputDateFormat) => {
        const arr = [];
        // month of this year. till cur month
        const tos = moment().format('MMM YY');
        const froms = moment().startOf('year').format('MMM YY');
        const from = moment(froms, 'MMM YY');
        const to = moment(tos, 'MMM YY');
        let loop = true;
        while (loop) {
            arr.push(from.format(outputDateFormat));

            if (from.isSame(to)) {
                loop = false;
            } else {
                from.add(1, 'month');
            }
        }

        return arr;
    });

    /**
     *  method to find the conv rate from billing currency to Other currency
     */
    fastify.decorate('getConversionRateForUser', async (cli_id, to_currency) => {
        console.log('getConversionRateForUser() incoming param =>', [cli_id, to_currency]);
        const result = await fastify.findUserById(cli_id);
        // find the user conversion type to use
        const userObj = _.get(result, '0', {});
        const convType = _.get(userObj, 'billing_currency_conv_type');
        const billing_currency = _.get(userObj, 'billing_currency');

        console.log('/getConversionRateForUser the conversion type for client id is ', [cli_id, convType]);

        // get the conversion rate for billing_currency to bill rates (eur)
        const result2 = await fastify.getConversionRate(billing_currency, to_currency, convType);
        const rateObj = _.get(result2, '0', {});

        if (_.isEmpty(rateObj)) {
            throw fastify.httpErrors.internalServerError('Error processing your request. Pleae try again');
        }

        const convRate = _.get(rateObj, 'rate');
        return convRate;
    });

    fastify.decorate('getConversionRateForUserV2', async (cli_id, billing_currency, to_currency, convType) => {
        console.log('getConversionRateForUserV2() incoming param =>', [cli_id, billing_currency, to_currency, convType]);

        // get the conversion rate for billing_currency to bill rates (eur)
        const result2 = await fastify.getConversionRate(billing_currency, to_currency, convType);
        const rateObj = _.get(result2, '0', {});

        if (_.isEmpty(rateObj)) {
            throw fastify.httpErrors.internalServerError('Error processing your request. Pleae try again');
        }

        const convRate = _.get(rateObj, 'rate');
        return convRate;
    });

    fastify.decorate('generateClientId', async (loggedin_cli_id, user_type) => {
        const { TYPE_ADMIN, MAX_ADMINS_UNDER_SA, MAX_USERS_UNDER_ADMIN } = process.env;
        let newCliId = null;
        if (user_type == TYPE_ADMIN) {
            console.log('creating new admin user under ', loggedin_cli_id);
            // new acc for admin
            const saPortion = `${loggedin_cli_id}`.substr(0, 8);
            let newAdmin = await fastify.redis.GEN.hincrby('acc:current:admin', saPortion, 1);
            if (newAdmin < 1) {
                newAdmin = await fastify.redis.GEN.hincrby('acc:current:admin', saPortion, 1);
            }
            // check if new admin id > allowed
            if (newAdmin > +MAX_ADMINS_UNDER_SA) {
                // cannot create more than 9999 admins under this SA
                throw new Error(`max allowed admins has been reached for sa ${loggedin_cli_id}`);
            }
            const newAdminPadded = _.padStart(newAdmin, 4, '0');
            // stitch new cli id
            newCliId = `${saPortion}${newAdminPadded}0000`;
        } else {
            // new account for user
            const saPortion = `${loggedin_cli_id}`.substr(0, 8);
            const adminPortion = `${loggedin_cli_id}`.substr(8, 4);
            let newUser = await fastify.redis.GEN.hincrby('acc:current:user', `${saPortion}:${adminPortion}`, 1);
            if (newUser < 1) {
                newUser = await fastify.redis.GEN.hincrby('acc:current:user', `${saPortion}:${adminPortion}`, 1);
            }
            // check if new user id > allowed
            if (newUser > +MAX_USERS_UNDER_ADMIN) {
                // cannot create more than 9999 users under this SA/admin
                throw new Error(`max allowed users has been reached for ${loggedin_cli_id}`);
            }
            const newUserPadded = _.padStart(newUser, 4, '0');
            // stitch new cli id
            newCliId = `${saPortion}${adminPortion}${newUserPadded}`;
        }
        return newCliId;
    });

    function recursiveFunction(payload){ 
        _.forOwn(payload, function(value, key) {
            if(typeof value === 'boolean'){
                _.set(payload, key, false);
            }else{
                recursiveFunction(value); 
            }
        });
    };

    fastify.decorate('getUIPermissions', async (userObj) => {
        //let response = null;
        let payload = {};
        try {
            let linksToBeDisabled = [];
            payload = _.cloneDeep(permissionsPayload.generic);

            const expiry_date = _.get(userObj, 'expiry_date', null);
            if (!_.isNull(expiry_date)){
                const expMoment = momenttz.tz(expiry_date, process.env.IST_ZONE_NAME);
                const istMoment = momenttz.tz(process.env.IST_ZONE_NAME);
                if(istMoment.isAfter(expMoment)){
                    fastify.log.debug(`/getUIPermissions cli_id => ${userObj.cli_id} => ${userObj.user} expiry date reached (${expMoment.format('YYYY-MM-DD HH:mm:ss z')}), considering acc_status to ${process.env.ACCOUNT_STATUS_EXPIRED}(expried)`);
                    _.set(userObj, 'acc_status', _.toNumber(process.env.ACCOUNT_STATUS_EXPIRED));
                }
            }

            if(+userObj.acc_status === +process.env.ACCOUNT_STATUS_DEACTIVATED ||  +userObj.acc_status === +process.env.ACCOUNT_STATUS_EXPIRED){
                payload = _.cloneDeep(permissionsPayload.deactivated_expired);
                fastify.log.debug(`/getUIPermissions cli_id => ${userObj.cli_id} => ${userObj.user} getting deactivated/expired payload config `);
            }else{
                fastify.log.debug(`/getUIPermissions cli_id => ${userObj.cli_id} => ${userObj.user} getting generic payload config `);
            }

            const blockedLinksAndCards = await fastify.getBlacklistedLinksForCliId(userObj.cli_id);
            if(_.size(blockedLinksAndCards) > 0) {
                _.forEach(blockedLinksAndCards, (row) => {
                    let linkOrCard = row.feature;
                    if(!_.isUndefined(linkOrCard) && !_.isNull(linkOrCard) && !_.isEmpty(_.trim(linkOrCard))){
                        linkOrCard = _.toLower(_.trim(linkOrCard));
                        const splittedPath =  _.split(linkOrCard, '.', 2);
                        if(!_.isNull(splittedPath) && _.size(splittedPath) > 0){
                            if(!_.isUndefined(splittedPath[1]) && !_.isNull(splittedPath[1]) && _.isEqual(_.trim(splittedPath[1]), 'visible')){
                                // main link
                                linksToBeDisabled.push(_.trim(splittedPath[0]));
                            }else{
                                // sublinks/cards/actions
                                _.set(payload, linkOrCard, false);
                            }
                        }
                    }                    
                });
            }
            
            if(!_.isUndefined(userObj.platform_cluster) && !_.isNull(userObj.platform_cluster)){
                const cluster = _.toLower(_.trim(userObj.platform_cluster));
                if(_.isEqual(cluster, 'otp')){
                    let otpLinks = process.env.PLATFORM_CLUSTER_OTP_DISABLED_LINKS;
                    if(!_.isNull(otpLinks) && _.size(otpLinks) > 0){
                        otpLinks = _.split(otpLinks, ',');
                        linksToBeDisabled = _.concat(linksToBeDisabled, otpLinks);
                    }
                }                
            }

            if(+userObj.user_type === +process.env.TYPE_USER){
                linksToBeDisabled.push('accounts');
                linksToBeDisabled.push('myaccount.managewallet');
                linksToBeDisabled.push('billingrates.modifyuserrates');
                linksToBeDisabled.push('myaccount.wallettransactions.users');
            }

            fastify.log.debug(`/getUIPermissions cli_id => ${userObj.cli_id} => ${userObj.user} links to be disabled ${JSON.stringify(linksToBeDisabled)} `);

            if(+userObj.user_type > +process.env.TYPE_SUPERADMIN){
                _.set(payload, 'myaccount.overview.sharedgroups', true);
            }

            if(!_.isNull(linksToBeDisabled) && _.size(linksToBeDisabled) > 0){
                _.forEach(linksToBeDisabled, (obj) => {
                    let subpayload = _.get(payload, obj, null);
                    if(!_.isNull(subpayload)){
                        if(typeof subpayload === 'boolean'){
                            _.set(payload, obj, false);
                            fastify.log.debug(`/getUIPermissions cli_id => ${userObj.cli_id} => ${userObj.user} links to be disabled ${obj} `);
                        }else{
                            recursiveFunction(subpayload, userObj.cli_id, userObj.user);
                        }                        
                    }                
                });
            }
            
            //fastify.log.debug(`/getUIPermissions cli_id => ${userObj.cli_id} => ${userObj.user} permissions available => ${payload}`);
            //payload = JSON.stringify(payload);
            
            //response = btoa(payload);
            //response = Buffer.from(payload).toString('base64');
            //const orgi = Buffer.from(enc, 'base64').toString('ascii');
            //fastify.log.debug(`getUIPermissions payload => ${response}`);
        } catch (error) {
            fastify.log.error(`/getUIPermissions error   => ${error}`);
            throw error;
        }
        return payload;
    });

};

module.exports = fp(utils);
