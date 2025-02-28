/* eslint-disable no-restricted-syntax */
const _ = require('lodash');
const mtz = require('moment-timezone');
const fs = require('fs');
const Mustache = require('mustache');
const {
 tgroupsSchema, subServicesSchema, anewSchema, ainfoSchema, accountsSchema, unameuniqueSchema,
     astatsSchema, accountProfileInfoSchema, accountSettingsSchema, accountServicesSchema,
     accountSMPPSettingsSchema, accountDLTGroupsSchema, accountOtherSettingsSchema,
     smsDomesticRatesSchema, smsInternationalRatesSchema, walletUpdateSchema,
      updateaccstatusSchema, smsRateRowSchema, salesPersonsSchema, creditsSchema, cappingSchema, twoLevelAuthUpdationSchema
} = require('../../../schemas/account-schema');

async function account(fastify) {
    fastify.addHook('preValidation', async (req) => req.jwtVerify());

    function constructEmailData(from_fname, to_fname, to_lname, from_email, email, reply_to, subject, ui_pass_cust, api_pass_cust, smpp_pass_cust, services) {
        const from_name = `${from_fname}`;
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

        const dataObj = { showLogo: true, from_email, from_name, subject, to_email: email, to_fname, to_lname, reply_to, services: servicesArr };
        return dataObj;
    }

    function validateBlockoutTimes(stime, etime) {
        let startHour = '';
        let startMinute = '';
        let endHour = '';
        let endMinute = '';
        const start = _.split(stime, ':');
        const end = _.split(etime, ':');
        const response = {
            field: '',
            starttime: '',
            endtime: '',
        };

        if (!_.isUndefined(start[0]) && !_.isNull(start[0]) && !_.isEmpty(_.trim(start[0]))) {
            startHour = _.trim(start[0]);
            if (_.size(startHour) == 2 || _.size(startHour) == 1) {
                startHour = _.size(startHour) == 2 ? startHour : `0${startHour}`;
                let value = _.parseInt(startHour);
                value = _.toNumber(startHour);
                if (_.isNaN(value) || +startHour < 0 || +startHour > 23) {
                    response.field = 'start';
                    return response;
                }
            } else {
                response.field = 'start';
                return response;
            }
        } else {
            response.field = 'start';
            return response;
        }

        if (!_.isUndefined(start[1]) && !_.isNull(start[1]) && !_.isEmpty(_.trim(start[1]))) {
            startMinute = _.trim(start[1]);
            if (_.size(startMinute) == 2 || _.size(startMinute) == 1) {
                startMinute = _.size(startMinute) == 2 ? startMinute : `0${startMinute}`;
                let value = _.parseInt(startMinute);
                value = _.toNumber(startMinute);
                if (_.isNaN(value) || +startMinute < 0 || +startMinute > 59) {
                    response.field = 'start';
                    return response;
                }
            } else {
                response.field = 'start';
                return response;
            }
        } else {
            response.field = 'start';
            return response;
        }

        if (!_.isUndefined(end[0]) && !_.isNull(end[0]) && !_.isEmpty(_.trim(end[0]))) {
            endHour = _.trim(end[0]);
            if (_.size(endHour) == 2 || _.size(endHour) == 1) {
                endHour = _.size(endHour) == 2 ? endHour : `0${endHour}`;
                let value = _.parseInt(endHour);
                value = _.toNumber(endHour);
                if (_.isNaN(value) || +endHour < 0 || +endHour > 23) {
                    response.field = 'end';
                    return response;
                }
            } else {
                response.field = 'end';
                return response;
            }
        } else {
            response.field = 'end';
            return response;
        }

        if (!_.isUndefined(end[1]) && !_.isNull(end[1]) && !_.isEmpty(_.trim(end[1]))) {
            endMinute = _.trim(end[1]);
            if (_.size(endMinute) == 2 || _.size(endMinute) == 1) {
                endMinute = _.size(endMinute) == 2 ? endMinute : `0${endMinute}`;
                let value = _.parseInt(endMinute);
                value = _.toNumber(endMinute);
                if (_.isNaN(value) || +endMinute < 0 || +endMinute > 59) {
                    response.field = 'end';
                    return response;
                }
            } else {
                response.field = 'end';
                return response;
            }
        } else {
            response.field = 'end';
            return response;
        }

        const startTime = `${startHour}:${startMinute}`;
        const endTime = `${endHour}:${endMinute}`;

        if (_.isEqual(startTime, endTime)) {
            response.field = 'equal';
            return response;
        }

        response.starttime = startTime;
        response.endtime = endTime;
        response.field = '';

        return response;
    }

    fastify.get('/isUnameUnique', { schema: unameuniqueSchema }, async (req) => {
        try {
            const { uname } = req.query;
            const result = await fastify.checkUnameUniqueness(uname);
            const { counts } = result[0];

            const payload = { isUnique: (counts === 0) };
            return payload;
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get username uniqueness. Please try again', { err });
            return e;
        }
    });

    fastify.get('/dltTemplateGroups', { schema: tgroupsSchema }, async (req) => {
        try {
          const result = await fastify.getAllDLTGroups(req);
          return result;
        } catch (err) {
          const e = fastify.httpErrors.createError(500, 'Could not get Countries. Please try again', { err });
          return e;
        }
    });

    fastify.get('/subServices', { schema: subServicesSchema }, async () => {
        try {
            const result = await fastify.getAllSubServices();
            return result;
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get Sub services. Please try again', { err });
            return e;
        }
    });

    fastify.post('/ainfo', { schema: ainfoSchema }, async (req) => {
        try {
            const { cli_id } = req.body;

            const result = await fastify.findUserById(cli_id);
            const userObj = _.get(result, '0', {});

            if (_.isEmpty(userObj)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            }

            const [configs] = await Promise.all([fastify.getUserConfigs(cli_id)]);
            const full_message = _.get(configs[0], 'full_message', 0);
            const subusers_reports = _.get(configs[0], 'subusers_reports', 0);
            const camp_name_auto_gen = _.get(configs[0], 'auto_gen_cname', 0);
            const inactive_account_login = _.get(configs[0], 'inactive_account_login', 0);

            const { bill_type, msg_type, platform_cluster, acc_is_async, is_url_track, sc_len } = userObj;

            const utypeResult = await fastify.getUserCountsByType(cli_id);
            _.forEach(utypeResult, (obj) => {
                const userType = _.get(obj, 'user_type');
                const total = _.get(obj, 'total', 0);
                if (+userType === 1) _.set(userObj, 'total_admins', +total);
                if (+userType === 2) _.set(userObj, 'total_users', +total);
            });

            const resultAllSubServices = await fastify.getAllSubServices();
            // console.log('AllSubServices', resultAllSubServices);

            const resultSSThisUser = await fastify.getAssignedSubServices(cli_id);
            // console.log('SSThisUser', resultSSThisUser);

            const gbThisUser = _.groupBy(resultSSThisUser, 'service');

            _.forEach(resultAllSubServices, (obj) => {
                const service = _.get(obj, 'service');
                const sub_service = _.get(obj, 'sub_service');
                const arr = _.map(_.get(gbThisUser, service), 'sub_service');
                if (_.includes(arr, sub_service)) {
                    _.set(obj, 'enabled_yn', 1);
                } else {
                    _.set(obj, 'enabled_yn', 0);
                }
            });

            let setIntlRates = false;
            _.forEach(resultAllSubServices, (ele) => {
                if (_.isEqual(ele.sub_service, 'international') && _.isEqual(ele.enabled_yn, 1)) {
                    setIntlRates = true;
                }
            });

            // get allocated dlt template groups
            const resultAllTG = await fastify.getAllocTemplateGroups(cli_id);
            // console.log('resultAllTG', resultAllTG);
            const resultTGInfo = await fastify.getTemplateGroupInfo(userObj.dlt_templ_grp_id);
           // console.log('alloc', resultTGInfo);
            const templateGroupName = _.get(resultTGInfo[0], 'template_group_name', ' ');
            const smsrate = _.floor(_.get(userObj, 'base_sms_rate', 0), 6);
            const dltrate = _.floor(_.get(userObj, 'base_add_fixed_rate', 0), 6);
            _.set(userObj, 'smsrate', smsrate);
            _.set(userObj, 'dltrate', dltrate);
            _.set(userObj, 'is_async', acc_is_async);
            _.set(userObj, 'url_track_enabled', is_url_track);
            _.set(userObj, 'vl_shortcode_len', sc_len);

            let smsleft = 0;

            // check if ROW rate is defined for this user
            const r = await fastify.getROWForAllUsers([cli_id]);
            const has_row = r.length > 0 ? 1 : 0;

            const resultIntlRates = await fastify.getBillRatesForIntl(cli_id);
            _.set(userObj, 'services', resultAllSubServices);
            _.set(userObj, 'has_row_yn', has_row);
            _.set(userObj, 'allocated_tgroups', resultAllTG);
            _.set(userObj, 'dlt_templ_grp_name', templateGroupName);

            const resultSMPPConfig = await fastify.getSMPPConfig(cli_id);

            if (resultSMPPConfig.length > 0) {
            _.set(userObj, 'bind_type', resultSMPPConfig[0].bind_type);
            _.set(userObj, 'max_allowed_connections', resultSMPPConfig[0].max_allowed_connections);
            _.set(userObj, 'throttle_limit', resultSMPPConfig[0].max_speed);
            _.set(userObj, 'dlt_entityid_tag', resultSMPPConfig[0].dlt_entityid_tag);
            _.set(userObj, 'dlt_templateid_tag', resultSMPPConfig[0].dlt_templateid_tag);
            _.set(userObj, 'smpp_charset', resultSMPPConfig[0].charset);
            _.set(userObj, 'cli_mid_tag', resultSMPPConfig[0].cli_mid_tag);
            _.set(userObj, 'dn_expiry_in_sec', resultSMPPConfig[0].dn_expiry_in_sec);
            _.set(userObj, 'dn_date_format', resultSMPPConfig[0].dn_date_format);
            }
            let convRate = await fastify.getConversionRateForUser(cli_id, process.env.INTL_BASE_CURRENCY);
            let walletBal = 0;
            convRate = 1 / convRate;
            const rates = [];
            if (+bill_type === 1) {
                walletBal = await fastify.redis.WALLET.hget('wallet:amount', cli_id);
                walletBal = _.floor(+walletBal, 6);
                _.set(userObj, 'wallet', +walletBal);
                smsleft = await fastify.getSMSLeft(walletBal, smsrate, dltrate);
            }
            for await (const row of resultIntlRates) {
                const country = _.get(row, 'country');
                const baserate = _.get(row, 'base_sms_rate');
                const conv_baserate = baserate * convRate;
               /* const addlrate = _.get(row, 'base_add_fixed_rate');

                const conv_addlrate = addlrate * convRate;

                 if (+bill_type === 1) {
                    // get the total sms that can be sent for this wallet
                    smsleft = await fastify.getSMSLeft(walletBal, conv_baserate, conv_addlrate);
                } */
                rates.push({ smsrate: _.floor(conv_baserate, 6), country });
            }
             // send intlrates only if intl service is enabled
             if (setIntlRates) {
                _.set(userObj, 'intl_rates', rates);
            } else {
                _.set(userObj, 'intl_rates', []);
            }
            _.set(userObj, 'msg_type', msg_type);
            _.set(userObj, 'platform_cluster', platform_cluster);

            _.set(userObj, 'sms_left', smsleft);

            const cts = _.get(userObj, 'created_ts', null);
            const mts = _.get(userObj, 'updated_ts', null);
            let tz = _.get(userObj, 'time_zone', null);
            let expiry_date = _.get(userObj, 'expiry_date', null);

            // if tz is empty, defalt it to IST (this case shud not happen, just in case)
            if (!tz) tz = process.env.IST_ZONE_NAME;

            const ctsMoment = mtz.tz(cts, tz);
            const mtsMoment = mtz.tz(mts, tz);
            const expiryMoment = mtz.tz(expiry_date, tz);

            if (!_.isNull(cts)) {
                // convert to acc tz
                const formattedDt = ctsMoment.format('MMM DD, YYYY HH:mm:ss z');
                _.set(userObj, 'created_ts', formattedDt);
                _.set(userObj, 'created_ts_unix', ctsMoment.unix());
            }
            if (!_.isNull(mts)) {
                // convert to acc tz
                const formattedDt = mtsMoment.format('MMM DD, YYYY HH:mm:ss z');
                _.set(userObj, 'modified_ts', formattedDt);
                _.set(userObj, 'modified_ts_unix', mtsMoment.unix());
            }
            if (!_.isNull(expiry_date)) {
                // convert to acc tz
                _.set(userObj, 'expiry_ts', expiryMoment.format('MMM DD, YYYY HH:mm:ss z'));
                _.set(userObj, 'expiry_date', expiryMoment.format('YYYY-MM-DD HH:mm:ss'));
                _.set(userObj, 'expiry_ts_unix', expiryMoment.unix());
            }

            const zoneAbbr = mtz().tz(tz).format('z');
            // const zoneAbbr = mtz.tz.zone(tz).abbr(new Date()); // e.g IST

            _.set(userObj, 'time_zone_abbr', zoneAbbr);
            _.set(userObj, 'trai_blockout', userObj.domestic_promo_trai_blockout_purge);

            const query_result = await fastify.getSalesPersonName(userObj.sales_id);
            //console.log(query_result);
            if (query_result.length > 0) {
            _.set(userObj, 'sales_name', query_result[0].name);
            } else {
                _.set(userObj, 'sales_name', null);
                _.set(userObj, 'sales_id', 0);
            }

            _.set(userObj, 'full_message', full_message);
            _.set(userObj, 'camp_name_auto_gen', camp_name_auto_gen);
            _.set(userObj, 'subusers_reports', subusers_reports);
            _.set(userObj, 'inactive_login', inactive_account_login);            

            if (+bill_type === 0) {
                let credit_limit = await fastify.redis.WALLET.hget('wallet:amount', cli_id);
                if(_.isNull(credit_limit)){
                    credit_limit = 0;
                }
                credit_limit = _.floor(credit_limit, 4);
                _.set(userObj, 'credit_limit_available', +credit_limit);
            }

            return userObj;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not get account info. Please try again', { code });
            return e;
        }
    });

    fastify.get('/accounts', { schema: accountsSchema }, async (req) => {
        try {
             // const result = await fastify.getAllUsers();
             const [result, result1] = await Promise.all([fastify.getAllUsers(), fastify.getWalletBalances()]);

             for (const obj of result) {
                const cts = _.get(obj, 'created_ts', null);
                // TODO -  MUI logins do not have any timezone for now. So showing all dates in IST
                //const tz = _.get(obj, 'time_zone', null);
                const tz = process.env.IST_ZONE_NAME;
                const expiry_date = _.get(obj, 'expiry_date', null);

                const ctsMoment = mtz.tz(cts, tz);
                const cli_id = _.get(obj, 'cli_id');
                const expiryMoment = mtz.tz(expiry_date, tz);

                if (!_.isNull(cts)) {
                    // convert to acc tz
                    const formattedDt = ctsMoment.format('MMM DD, YYYY HH:mm:ss');
                    _.set(obj, 'created_ts', formattedDt);
                    _.set(obj, 'created_ts_unix', ctsMoment.unix());
                }

                if (!_.isNull(expiry_date)) {
                    // convert to acc tz
                    const formattedDt = expiryMoment.format('MMM DD, YYYY HH:mm:ss');
                    _.set(obj, 'expiry_date', formattedDt);
                    _.set(obj, 'expiry_ts_unix', expiryMoment.unix());
                }

                const bill_type = _.get(obj, 'bill_type', null);
                if (!_.isNull(bill_type)) {
                    _.set(obj, 'bill_type', 'Postpaid');
                    _.set(obj, 'wallet', 'NA');
                    if (+bill_type == 1) {
                        _.set(obj, 'bill_type', 'Prepaid');
                        // let walletBal = await fastify.redis.WALLET.hget('wallet:amount', _.get(obj, 'cli_id'));
                        let walletBal = _.get(result1, cli_id, null);
                        if (!_.isNull(walletBal)) {
                            walletBal = _.floor(+walletBal, 6);
                            _.set(obj, 'wallet', walletBal);
                        }
                    }
                }

                let acc_status = _.get(obj, 'acc_status');
                if (acc_status === +process.env.ACCOUNT_STATUS_ACTIVE){
                    _.set(obj, 'acc_status_desc', 'Active');
                } else if(acc_status === +process.env.ACCOUNT_STATUS_DEACTIVATED){
                    _.set(obj, 'acc_status_desc', 'Deactived');
                } else if(acc_status === +process.env.ACCOUNT_STATUS_SUSPENDED){
                   _.set(obj, 'acc_status_desc', 'Suspended');
                } else if(acc_status === +process.env.ACCOUNT_STATUS_EXPIRED){
                   _.set(obj, 'acc_status_desc', 'Expired');
                }

                const currency = _.get(obj, 'billing_currency');
                _.set(obj, 'billing_currency', currency);
            }
            return result;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not get account list. Please try again', { code });
            // const e = fastify.httpErrors.createError(500, err);
            return e;
        }
    });

    fastify.get('/astats', { schema: astatsSchema }, async (req) => {
        try {
            let total_sa = 0;
            let total_admins = 0;
            let total_users = 0;
            let total_active = 0;
            let total_inactive = 0;
            let total_groups = 0;
            let total_templates = 0;

            const [result, result1, result2] = await Promise.all(
                    [fastify.getAccountStats(),
                    fastify.getGroups('all', 'all'),
                    fastify.getTemplates(null)],
);

            total_groups = result1.length;
            total_templates = result2.length;

            for (const obj of result) {
                const userType = _.get(obj, 'user_type');
                const total = _.get(obj, 'total', 0);
                const accStatus = _.get(obj, 'acc_status');

                if (+userType === 0) total_sa += +total;
                if (+userType === 1) total_admins += +total;
                if (+userType === 2) total_users += +total;
                if (+accStatus === 0) total_active += +total;
                if (+accStatus === 2) total_inactive += +total;
            }
            const payload = {
                total_accounts: (total_sa + total_admins + total_users),
                total_sa,
                total_admins,
                total_users,
                total_active,
                total_inactive,
                total_groups,
                total_templates,
            };

            return payload;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not get account stats. Please try again', { code });
            // const e = fastify.httpErrors.createError(500, err);
            return e;
        }
    });

    fastify.post('/anew', { schema: anewSchema }, async (req, reply) => {
        try {
            let newCliId = '';
            let ui_pass = '';
            let api_pass = '';
            let smpp_pass = '';
            let ui_pass_cust = '';
            let api_pass_cust = '';
            let smpp_pass_cust = '';
            const servicesValuesArr = [];
            const tgValuesArr = [];
            const intlRatesValuesArr = [];

            const {
                firstname, company, username, mobile, email, billing_currency, conv_type, offset, bill_type, message_type,
                platform_cluster, services, bind_type, max_allowed_connections, throttle_limit, smpp_charset, dlt_entityid_tag, dlt_templateid_tag,
                smsrate, dltrate, intl_rates, allocated_tgroup_ids, assigned_tgroup_id, sms_priority, sms_retry, bill_encrypt_type,
                domestic_sms_blockout, blklist_chk, spam_chk, dup_chk_req, intl_sms_blockout, optin_chk_req, sales_id,
                domestic_special_series_allow, req_hex_msg, acc_type, invoice_based_on, is_ildo, cli_mid_tag, full_message, camp_name_auto_gen,
                subusers_reports, dnd_chk, two_level_auth, mt_adjust, dn_adjust, dnd_reject_yn, vl_shortner, msg_replace_chk, is_schedule_allow,
                uc_iden_allow, is_remove_uc_chars, url_smartlink_enable, url_track_enabled, is_async, use_default_header, use_default_on_header_fail,
                considerdefaultlength_as_domestic, domestic_tra_blockout_reject, timebound_chk_enable, force_dnd_check, msg_retry_available,
                capping_chk_enable, dn_date_format, inactive_login
            } = req.body;

            let {
                wallet, address, newline_chars, domestic_sms_blockout_start, domestic_sms_blockout_stop, dup_chk_interval,
                intl_sms_blockout_start, intl_sms_blockout_stop, trai_blockout, lastname, is_16bit_udh, ip_validation, ip_list,
                uc_iden_char_len, uc_iden_occur, vl_shortcode_len, acc_default_header, timebound_interval, timebound_max_count_allow,
                dnd_pref, capping_interval_type, capping_interval, capping_max_count_allow, acc_route_id, credit_check, credit_limit,
                zone_name, expiry_date
            } = req.body;

            const { userid: loggedin_cli_id, sessionid, user } = req.user;
            const user_type = process.env.TYPE_SUPERADMIN;

            if (_.isUndefined(username) || _.isNull(username) || _.isEmpty(username)) {
                return fastify.httpErrors.badRequest('username is required');
            }

            /* check if username is unique */
            const result = await fastify.checkUnameUniqueness(_.trim(username));
            const { counts } = result[0];

            req.log.debug(`/anew matching username '${username}' count  =>${counts}`);
            const isUnique = (counts === 0);

            if (!isUnique) {
                const resp = {
                    statusCode: fastify.CONSTANTS.USERNAME_NOT_UNIQUE_CODE,
                    message: 'Username is already exist',
                    status: 'failed',
                };
                reply.code(200);
                return reply.send(resp);
            }

            const subServices = _.map(services, (o) => _.toLower(_.trim(o.sub_service)));

            if (_.size(services) == 0 || _.size(subServices) == 0) {
                return fastify.httpErrors.badRequest('service and sub_service are required');
            }

            let invalidServices = false;
            _.forEach(services, (obj) => {
                const service = _.toLower(_.trim(_.get(obj, 'service', '')));
                const sub_service = _.toLower(_.trim(_.get(obj, 'sub_service', '')));
                if (!_.includes(fastify.CONSTANTS.SERVICES, service)) {
                    invalidServices = true;
                    return false;
                }
                if (!_.includes(fastify.CONSTANTS.SUB_SERVICES, sub_service)) {
                    invalidServices = true;
                    return false;
                }
                return true;
            });

            if (invalidServices) {
                return fastify.httpErrors.badRequest('invalid service/sub_service');
            }

            if (+smsrate <= 0) {
                return fastify.httpErrors.badRequest('smsrate should be greater than 0');
            }
            if (+dltrate <= 0) {
                return fastify.httpErrors.badRequest('dltrate should be greater than 0');
            }

            if(_.isUndefined(zone_name) || _.isNull(zone_name) || _.isEmpty(_.trim(zone_name))){
                return fastify.httpErrors.badRequest('Timezone is required');
            }else{
                zone_name = _.trim(zone_name);
            }

            if(_.isUndefined(expiry_date) || _.isNull(expiry_date) || _.isEmpty(_.trim(expiry_date))){
                return fastify.httpErrors.badRequest('expiry date is required');
            }else{
                expiry_date = _.trim(expiry_date);
                if(_.size(expiry_date) == 10){
                    expiry_date = `${expiry_date} 23:59:59`;
                }
                let calanderInUserTz = mtz.tz(expiry_date, 'YYYY-MM-DD HH:mm:ss', zone_name);
                expiry_date = calanderInUserTz.tz(process.env.IST_ZONE_NAME).format('YYYY-MM-DD HH:mm:ss');
            }

            if (!_.isUndefined(domestic_sms_blockout) && !_.isNull(domestic_sms_blockout) && +domestic_sms_blockout > 0) {
                if (_.isUndefined(domestic_sms_blockout_start) || _.isNull(domestic_sms_blockout_start)
                || _.isEmpty(_.trim(domestic_sms_blockout_start))) {
                    return fastify.httpErrors.badRequest('domestic_sms_blockout_start is required');
                }
                if (_.isUndefined(domestic_sms_blockout_stop) || _.isNull(domestic_sms_blockout_stop)
                || _.isEmpty(_.trim(domestic_sms_blockout_stop))) {
                    return fastify.httpErrors.badRequest('domestic_sms_blockout_stop is required');
                }
                domestic_sms_blockout_start = _.trim(domestic_sms_blockout_start);
                domestic_sms_blockout_stop = _.trim(domestic_sms_blockout_stop);
                const response = validateBlockoutTimes(domestic_sms_blockout_start, domestic_sms_blockout_stop);
                if (!_.isUndefined(response) && !_.isNull(response) && !_.isEmpty(_.trim(response.field))) {
                    const reason = _.get(response, 'field');
                    if (_.isEqual(reason, 'start')) {
                        return fastify.httpErrors.badRequest('domestic_sms_blockout_start is not valid');
                    }
                    if (_.isEqual(reason, 'end')) {
                        return fastify.httpErrors.badRequest('domestic_sms_blockout_stop is not valid');
                    }
                    if (_.isEqual(reason, 'equal')) {
                        return fastify.httpErrors.badRequest('domestic_sms_blockout_start and domestic_sms_blockout_stop can not be same time');
                    }
                    return fastify.httpErrors.badRequest(reason);
                }
                domestic_sms_blockout_start = response.starttime;
                domestic_sms_blockout_stop = response.endtime;
            } else {
                domestic_sms_blockout_start = null;
                domestic_sms_blockout_stop = null;
            }

            if (!_.isUndefined(intl_sms_blockout) && !_.isNull(intl_sms_blockout) && +intl_sms_blockout > 0) {
                if (_.isUndefined(intl_sms_blockout_start) || _.isNull(intl_sms_blockout_start)
                || _.isEmpty(_.trim(intl_sms_blockout_start))) {
                    return fastify.httpErrors.badRequest('intl_sms_blockout_start is required');
                }
                if (_.isUndefined(intl_sms_blockout_stop) || _.isNull(intl_sms_blockout_stop)
                || _.isEmpty(_.trim(intl_sms_blockout_stop))) {
                    return fastify.httpErrors.badRequest('intl_sms_blockout_stop is required');
                }
                intl_sms_blockout_start = _.trim(intl_sms_blockout_start);
                intl_sms_blockout_stop = _.trim(intl_sms_blockout_stop);
                const response = validateBlockoutTimes(intl_sms_blockout_start, intl_sms_blockout_stop);
                if (!_.isUndefined(response) && !_.isNull(response) && !_.isEmpty(_.trim(response.field))) {
                    const reason = _.get(response, 'field');
                    if (_.isEqual(reason, 'start')) {
                        return fastify.httpErrors.badRequest('intl_sms_blockout_start is not valid');
                    }
                    if (_.isEqual(reason, 'end')) {
                        return fastify.httpErrors.badRequest('intl_sms_blockout_stop is not valid');
                    }
                    if (_.isEqual(reason, 'equal')) {
                        return fastify.httpErrors.badRequest('intl_sms_blockout_start and intl_sms_blockout_stop can not be same time');
                    }
                    return fastify.httpErrors.badRequest(reason);
                }
                intl_sms_blockout_start = response.starttime;
                intl_sms_blockout_stop = response.endtime;
            } else {
                intl_sms_blockout_start = null;
                intl_sms_blockout_stop = null;
            }

            if (!_.isUndefined(dup_chk_req) && !_.isNull(dup_chk_req) && +dup_chk_req > 0) {
                if (_.isUndefined(dup_chk_interval) || _.isNull(dup_chk_interval)) {
                    return fastify.httpErrors.badRequest('dup_chk_interval is required');
                }
                if (+dup_chk_interval < 0) {
                    return fastify.httpErrors.badRequest('dup_chk_interval value is invalid');
                }
            } else {
                dup_chk_interval = 0;
            }

            let acc_billing_type = 'postpaid';
            /*  check wallet info */
            if (+bill_type === 1) {
                acc_billing_type = 'prepaid';
                if (_.isUndefined(wallet) || _.isNull(wallet)) {
                    return fastify.httpErrors.badRequest('wallet amount is required');
                }

                if (+wallet <= 0) {
                    return fastify.httpErrors.badRequest('wallet amount should be greater than 0');
                }

                if (_.findIndex(_.lowerCase(_.toString(wallet)), 'e') >= 0) {
                    return fastify.httpErrors.badRequest('invalid wallet amount');
                }
                // credit_check/credit_limit applicable to only postpaid accounts
                credit_check = 0;
                credit_limit = 0;
            }

            if (+bill_type === 0 && +credit_check === 1) {
                if (_.isUndefined(credit_limit) || _.isNull(credit_limit)) {
                    return fastify.httpErrors.badRequest('credit_limit is required');
                }
                if (+credit_limit < 0) {
                    return fastify.httpErrors.badRequest('credit_limit value is invalid');
                }
            } else {
                credit_check = 0;
                credit_limit = 0;
            }

            if (+message_type === 0) {
                if (_.isUndefined(trai_blockout) || _.isNull(trai_blockout)) {
                    return fastify.httpErrors.badRequest('trai_blockout is required');
                }
            } else {
                trai_blockout = 0;
            }

            if (!_.isUndefined(newline_chars) && !_.isNull(newline_chars)) {
                newline_chars = _.trim(newline_chars);
                if (newline_chars.includes('~') || newline_chars.includes(':')) {
                    return fastify.httpErrors.badRequest('newline character can not contain ~ or :');
                }
            }

            if (+uc_iden_allow === 1) {
                if (_.isUndefined(uc_iden_char_len) || _.isNull(uc_iden_char_len)) {
                    return fastify.httpErrors.badRequest('uc_iden_char_len is required');
                }
                if (+uc_iden_char_len < 1 || +uc_iden_char_len > 99) {
                    return fastify.httpErrors.badRequest('uc_iden_char_len value is invalid');
                }
                if (_.isUndefined(uc_iden_occur) || _.isNull(uc_iden_occur)) {
                    return fastify.httpErrors.badRequest('uc_iden_occur is required');
                }
                if (+uc_iden_occur < 1 || +uc_iden_occur > 99) {
                    return fastify.httpErrors.badRequest('uc_iden_occur value is invalid');
                }
            } else {
                uc_iden_char_len = 0;
                uc_iden_occur = 0;
            }

            if (+vl_shortner === 1 || +url_smartlink_enable === 1) {
                if (_.isUndefined(vl_shortcode_len) || _.isNull(vl_shortcode_len)) {
                    return fastify.httpErrors.badRequest('vl_shortcode_len is required');
                }
            } else {
                vl_shortcode_len = 0;
            }

            if (+timebound_chk_enable == 1) {
                if (_.isUndefined(timebound_interval) || _.isNull(timebound_interval)) {
                    return fastify.httpErrors.badRequest('timebound_interval is required');
                }
                if (+timebound_interval < 0 || +timebound_interval > 99) {
                    return fastify.httpErrors.badRequest('timebound_interval value is invalid');
                }
                if (_.isUndefined(timebound_max_count_allow) || _.isNull(timebound_max_count_allow)) {
                    return fastify.httpErrors.badRequest('timebound_max_count_allow is required');
                }
                if (+timebound_max_count_allow < 0 || +timebound_max_count_allow > 99) {
                    return fastify.httpErrors.badRequest('timebound_max_count_allow value is invalid');
                }
            } else {
                timebound_interval = 0;
                timebound_max_count_allow = 0;
            }

            if (!_.isUndefined(dnd_pref) && !_.isNull(dnd_pref)) {
                if (+dnd_pref > 8) {
                    return fastify.httpErrors.badRequest('dnd_pref value is invalid');
                }
            } else {
                dnd_pref = 0;
            }

            if (!_.isUndefined(capping_chk_enable) && !_.isNull(capping_chk_enable)) {
                if (_.isUndefined(capping_interval_type) || _.isNull(capping_interval_type)) {
                    return fastify.httpErrors.badRequest('capping_interval_type is required');
                }
                if (+capping_interval_type > 6) {
                    return fastify.httpErrors.badRequest('capping_interval_type value is invalid');
                }
                if (_.isUndefined(capping_interval) || _.isNull(capping_interval)) {
                    return fastify.httpErrors.badRequest('capping_interval is required');
                }
                if (+capping_interval > 999) {
                    return fastify.httpErrors.badRequest('capping_interval value is invalid');
                }
                if (_.isUndefined(capping_max_count_allow) || _.isNull(capping_max_count_allow)) {
                    return fastify.httpErrors.badRequest('capping_max_count_allow is required');
                }
            } else {
                capping_interval_type = 0;
                capping_interval = 0;
                capping_max_count_allow = 0;
            }

            if (_.isUndefined(acc_route_id) || _.isNull(acc_route_id) || _.isEmpty(_.trim(acc_route_id))) {
                acc_route_id = null;
            } else {
                acc_route_id = _.trim(acc_route_id);
            }

            if (!_.isUndefined(acc_default_header) && !_.isNull(acc_default_header) && !_.isEmpty(_.trim(acc_default_header))) {
                acc_default_header = _.trim(acc_default_header);
            } else {
                return fastify.httpErrors.badRequest('acc_default_header is required');
            }

            if (_.includes(subServices, 'international')) {
                if (intl_rates.length === 0) {
                    return fastify.httpErrors.badRequest('intl_rates value is missing in req payload');
                }
                const intl_countries = _.map(intl_rates, (o) => _.toLower(_.trim(o.country)));
                if (!_.includes(intl_countries, 'row')) {
                    return fastify.httpErrors.badRequest('intl_rates should include row rate');
                }

                const invalid_rates = _.filter(intl_rates, (obj) => obj.smsrate <= 0);

                if (_.size(invalid_rates) > 0) {
                    return fastify.httpErrors.badRequest('intl smsrate should be greater than 0');
                }
            }

            if (_.includes(subServices, 'smpp')) {
                if (_.isUndefined(bind_type) || _.isNull(bind_type)) {
                    return fastify.httpErrors.badRequest('bind_type value is missing in req payload');
                } if (_.isUndefined(max_allowed_connections) || _.isNull(max_allowed_connections)) {
                    return fastify.httpErrors.badRequest('max_allowed_connections value is missing in req payload');
                } if (_.isUndefined(throttle_limit) || _.isNull(throttle_limit)) {
                    return fastify.httpErrors.badRequest('throttle_limit value is missing in req payload');
                } if (_.isUndefined(smpp_charset) || _.isNull(smpp_charset)) {
                    return fastify.httpErrors.badRequest('smpp_charset value is missing in req payload');
                } if (_.isUndefined(dlt_entityid_tag) || _.isNull(dlt_entityid_tag)) {
                    return fastify.httpErrors.badRequest('dlt_entityid_tag value is missing in req payload');
                } if (_.isUndefined(dlt_templateid_tag) || _.isNull(dlt_templateid_tag)) {
                    return fastify.httpErrors.badRequest('dlt_templateid_tag value is missing in req payload');
                } if (_.isUndefined(cli_mid_tag) || _.isNull(cli_mid_tag)) {
                    return fastify.httpErrors.badRequest('cli_mid_tag value is missing in req payload');
                } if (_.isUndefined(dn_date_format) || _.isNull(dn_date_format)) {
                    return fastify.httpErrors.badRequest('dn_date_format value is missing in req payload');
                }
            }

            if (_.includes(subServices, 'cm') || _.includes(subServices, 'api')) {
                if (_.isUndefined(is_16bit_udh) || _.isNull(is_16bit_udh)) {
                    is_16bit_udh = 0;
                }
            } else {
                is_16bit_udh = 0;
            }

            if (_.isUndefined(ip_validation) || _.isNull(ip_validation)) {
                ip_validation = 0;
                ip_list = null;
            } else if (+ip_validation == 1) {
                if (_.isUndefined(ip_list) || _.isNull(ip_list) || _.isEmpty(_.trim(ip_list))) {
                    return fastify.httpErrors.badRequest('IP address is required');
                }
                ip_list = _.trim(ip_list);
            } else {
                ip_validation = 0;
                ip_list = null;
            }

            const saPortion = await fastify.redis.GEN.hincrby('acc:current:sa', acc_billing_type, 1);
            newCliId = _.padEnd(saPortion, 16, '0');

            if (_.includes(subServices, 'cm')) {
                // generate new password
                ui_pass_cust = fastify.nanoiduipass();
                req.log.debug('/anew password for the superadmin => ', newCliId, ' is ', ui_pass_cust);
                // hash the password
                ui_pass = fastify.hash(ui_pass_cust);
                req.log.debug(`/anew hashed password for ${newCliId} =>  ui_pass`);
                // set vl shortner to 1
                // vl_shortner = 1;
                await fastify.redis.GEN.hset('cli:gui:pass', newCliId, ui_pass_cust);
            }

            if (_.includes(subServices, 'smpp')) {
                const smppdata = await fastify.smpppass(newCliId);
                smpp_pass = _.get(smppdata.passwords, 0).dbinsert_password;
                smpp_pass_cust = _.get(smppdata.passwords, 0).customer_password;
                await fastify.redis.GEN.hset('cli:smpp:pass', newCliId, smpp_pass_cust);
            }

            if (_.includes(subServices, 'api')) {
                const apidata = await fastify.apipass(newCliId);
                api_pass = _.get(apidata.passwords, 0).dbinsert_password;
                api_pass_cust = _.get(apidata.passwords, 0).customer_password;
                await fastify.redis.GEN.hset('cli:api:pass', newCliId, api_pass_cust);
            }

            /* prepare values for user_service_map table */
            if (!_.isEmpty(services)) {
                _.forEach(services, (obj) => {
                    const service = _.get(obj, 'service');
                    const sub_service = _.get(obj, 'sub_service');
                    // remove intl sub service if considerdefaultlength_as_domestic = 1
                    if (!(+considerdefaultlength_as_domestic == 1 && _.isEqual(_.trim(_.toLower(sub_service)), 'international'))) {
                        servicesValuesArr.push([newCliId, service, sub_service, loggedin_cli_id]);
                    }
                });
            }

            /* prepare values for users_templategroup_ids table */
            if (!_.isEmpty(allocated_tgroup_ids)) {
                _.forEach(allocated_tgroup_ids, (val) => {
                    tgValuesArr.push([newCliId, val, loggedin_cli_id]);
                });
            }

            /* prepare values for wallet_transactions table */
            let walletTransValuesArr = [];
            if (+bill_type === 1) { // its wallet account
                walletTransValuesArr = [fastify.nanoid(), newCliId, username, 'add', wallet, 'New Account', '', sessionid, loggedin_cli_id, user];
            }

            /* prepare values for client_intl_rates table */
            if (_.includes(subServices, 'international') && intl_rates.length > 0) {
                const result3 = await fastify.getConversionRate(billing_currency, process.env.INTL_BASE_CURRENCY, conv_type);
                const rateObj = _.get(result3, '0', {});

                if (_.isEmpty(rateObj)) {
                    return fastify.httpErrors.internalServerError('Error processing your request. Pleae try again');
                }

                const convRate = _.get(rateObj, 'rate');

                _.forEach(intl_rates, (obj) => {
                    const { country, smsrate: rate } = obj;
                    const converted_rate = _.floor(convRate * rate, 6);
                    intlRatesValuesArr.push([newCliId, country, converted_rate]);
                });
            }
            if (_.isUndefined(address) || _.isNull(address) || _.isEmpty(_.trim(address))) {
                address = null;
            } else {
                address = _.trim(address);
            }

            if (_.isUndefined(lastname) || _.isNull(lastname) || _.isEmpty(_.trim(lastname))) {
                lastname = null;
            } else {
                lastname = _.trim(lastname);
            }

            const obj = {
                newCliId,
                loggedin_cli_id,
                bill_type,
                message_type,
                platform_cluster,
                user_type,
                firstname: _.trim(firstname),
                lastname,
                company: _.trim(company),
                username: _.trim(username),
                ui_pass,
                api_pass,
                smpp_pass,
                email: _.trim(email),
                mobile: _.trim(mobile),
                zone_name: _.trim(zone_name),
                offset: _.trim(offset),
                allocated_tgroup_ids,
                assigned_tgroup_id,
                smpp_charset: _.trim(smpp_charset),
                address,
                wallet,
                smsrate,
                dltrate,
                newline_chars,
                services,
                vl_shortner,
                servicesValuesArr,
                tgValuesArr,
                walletTransValuesArr,
                billing_currency: _.trim(billing_currency),
                conv_type,
                intlRatesValuesArr,
                bind_type,
                max_allowed_connections,
                throttle_limit,
                dlt_entityid_tag,
                dlt_templateid_tag,
                sms_priority,
                sms_retry,
                // client_encrypt,
                bill_encrypt_type,
                trai_blockout,
                domestic_sms_blockout,
                dnd_chk,
                blklist_chk,
                spam_chk,
                dup_chk_req,
                intl_sms_blockout,
                optin_chk_req,
                sales_id,
                domestic_sms_blockout_start,
                domestic_sms_blockout_stop,
                dup_chk_interval,
                intl_sms_blockout_start,
                intl_sms_blockout_stop,
                domestic_special_series_allow,
                req_hex_msg,
                acc_type,
                invoice_based_on,
                is_ildo,
                cli_mid_tag,
                considerdefaultlength_as_domestic,
                is_16bit_udh,
                ip_validation,
                ip_list,
                full_message,
                camp_name_auto_gen,
                subusers_reports,
                two_level_auth,
                mt_adjust,
                dn_adjust,
                dnd_reject_yn,
                msg_replace_chk,
                is_schedule_allow,
                uc_iden_allow,
                is_remove_uc_chars,
                url_smartlink_enable,
                url_track_enabled,
                is_async,
                use_default_header,
                use_default_on_header_fail,
                domestic_tra_blockout_reject,
                timebound_chk_enable,
                force_dnd_check,
                msg_retry_available,
                capping_chk_enable,
                uc_iden_char_len,
                uc_iden_occur,
                vl_shortcode_len,
                acc_default_header,
                timebound_interval,
                timebound_max_count_allow,
                dnd_pref,
                capping_interval_type,
                capping_interval,
                capping_max_count_allow,
                acc_route_id,
                credit_check,
                credit_limit,
                dn_date_format,
                expiry_date,
                inactive_login
            };

            req.log.debug(` /anew user object prepared  =>${obj}`);

            await fastify.createAccount(obj);

            // MUI-5 : Logged in users balance is set to 0
            const loggedin_bal_before = 0;
            const loggedin_bal_after = 0;

            /*  set wallet balance */
            if (+bill_type === 1) { // its wallet account
                // trim to max 6 decimal places
                wallet = _.floor(wallet, 6);
                if (+wallet > 0) {
                   await fastify.redis.WALLET.hset('wallet:amount', newCliId, wallet);
                } else {
                    req.log.warn(`/anew *** amount after rounding(floor) to 6 decimal places ${wallet} cli_id ${newCliId}`);
                }

                // persist to wallet transaction
                try {
                    walletTransValuesArr = [];
                    walletTransValuesArr = [fastify.nanoid(), newCliId, username, 'add', wallet, 'New Account', '', sessionid, loggedin_cli_id,
                     user, wallet, loggedin_bal_before, loggedin_bal_after, wallet];
                    await fastify.persistWalletTransaction(walletTransValuesArr);
                    req.log.debug('/anew SUCCESSFULLY persisted the wallet transaction... ', JSON.stringify(walletTransValuesArr));
                } catch (e) {
                    // TODO: *** Data Leakage *** this needs to be handled
                    req.log.error('/anew *** Error Persisting Wallet transaction ***', JSON.stringify(walletTransValuesArr));
                }
            }

            // add postpaid credit_limit to wallet redis
            if (+bill_type === 0 && +credit_check === 1) {
                // trim to max 4 decimal places
                credit_limit = _.floor(credit_limit, 4);
                if (+credit_limit > 0) {
                   await fastify.redis.WALLET.hset('wallet:amount', newCliId, credit_limit);
                } else {
                    req.log.warn(`/anew *** credit_limit after rounding(floor) to 4 decimal places ${credit_limit}`);
                }
                
                try {
                    walletTransValuesArr = [];
                    walletTransValuesArr = [fastify.nanoid(), newCliId, username, 'add credit_limit', wallet, 'New Account', '', sessionid, loggedin_cli_id,
                    user, credit_limit, loggedin_bal_before, loggedin_bal_after, credit_limit];
                    await fastify.persistWalletTransaction(walletTransValuesArr);
                    req.log.debug('/anew SUCCESSFULLY persisted the wallet transaction for postpaid account with credit_check enabled ... ', JSON.stringify(walletTransValuesArr));
                } catch (e) {
                    // TODO: *** Data Leakage *** this needs to be handled
                    req.log.error('/anew *** Error Persisting Wallet transaction ***', JSON.stringify(walletTransValuesArr));
                }
            }

            /** ********************************************* Send Email  ******************************************** */
            if (_.includes(subServices, 'api') || _.includes(subServices, 'smpp') || _.includes(subServices, 'cm')) {
                const r1 = await fastify.findAdmin(user);
                const userObj = _.get(r1, '0', {});
                const from_fname = _.get(userObj, 'username');
                const from_email = _.get(userObj, 'email');
                const subject = Mustache.render(process.env.CREATE_ACCOUNT_MAIL_SUBJECT, { to_fname: firstname, to_lname: lastname });

                const emailTemplate = fs.readFileSync('./templates/create_account_email_template.html', 'utf8');

                const dataObj = constructEmailData(from_fname, firstname, lastname, from_email, email, from_email, subject, ui_pass_cust, api_pass_cust, smpp_pass_cust, subServices);

                try {
                    const respMail = await fastify.sendEmail(emailTemplate, dataObj);
                    req.log.debug('/anew email', respMail);
                } catch (e) {
                    req.log.error('/anew ERROR while sending mail ***IGNORING*** ', e);
                }
            }

            /** ********************************************* End of Send Email  ******************************************** */

            const resp = { statusCode: 200, message: 'Account has been created successfully' };
            reply.code(200);
            req.log.debug(`/anew response ${resp}`);
            return reply.send(resp);
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not create account. Please try again', { err });
            return e;
        }
    });

    fastify.post('/updateprofileinfo', { schema: accountProfileInfoSchema }, async (req, reply) => {
        try {
            const { cli_id, firstname, company } = req.body;
            let { lastname, address } = req.body;
            const { userid: loggedin_cli_id } = req.user;

            if (_.isUndefined(firstname) || _.isNull(firstname) || _.isEmpty(firstname)) {
                return fastify.httpErrors.badRequest('firstname is required');
            } if (_.isUndefined(company) || _.isNull(company) || _.isEmpty(company)) {
                return fastify.httpErrors.badRequest('company is required');
            } if (_.isUndefined(cli_id) || _.isNull(cli_id)) {
                return fastify.httpErrors.badRequest('cli_id is required');
            }

            if (_.isUndefined(address) || _.isNull(address)) {
                address = null;
            } else {
                address = _.trim(address);
            }

            if (_.isUndefined(lastname) || _.isNull(lastname)) {
                lastname = null;
            } else {
                lastname = _.trim(lastname);
            }

            const result = await fastify.findUserById(cli_id);
            const userObj = _.get(result, '0', {});

            if (_.isEmpty(userObj)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            }

            // update the table
            const result1 = await fastify.updateAccountProfileInfo(cli_id, _.trim(firstname), lastname, _.trim(company), address, loggedin_cli_id);
            console.log('/updateprofileinfo resp from db => ', result1);

            const resp = {
                statusCode: 200,
                message: 'Account profile info has been updated successfully',
            };
            reply.code(200);

            return reply.send(resp);
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not update profile info. Please try again', { err });
            return e;
        }
    });

    fastify.post('/updateaccountsettings', { schema: accountSettingsSchema }, async (req, reply) => {
        try {
            const {
                cli_id, mobile, email, offset, bill_type, message_type, platform_cluster, sms_priority, acc_type, invoice_based_on,
                use_default_header, use_default_on_header_fail, acc_route_id, acc_default_header,
            } = req.body;
            let { zone_name, expiry_date } = req.body;
            const { userid: loggedin_userid } = req.user;

            if (_.isUndefined(mobile) || _.isNull(mobile) || _.isEmpty(mobile)) {
                return fastify.httpErrors.badRequest('mobile is required');
            } if (_.isUndefined(email) || _.isNull(email) || _.isEmpty(email)) {
                return fastify.httpErrors.badRequest('email is required');
            } if (_.isUndefined(zone_name) || _.isNull(zone_name) || _.isEmpty(_.trim(zone_name))) {
                return fastify.httpErrors.badRequest('zone_name is required');
            } if (_.isUndefined(offset) || _.isNull(offset) || _.isEmpty(offset)) {
                return fastify.httpErrors.badRequest('offset is required');
            } if (_.isUndefined(cli_id) || _.isNull(cli_id)) {
                return fastify.httpErrors.badRequest('cli_id is required');
            } if (_.isUndefined(acc_type) || _.isNull(acc_type)) {
                return fastify.httpErrors.badRequest('acc_type is required');
            } if (_.isUndefined(invoice_based_on) || _.isNull(invoice_based_on)) {
                return fastify.httpErrors.badRequest('invoice_based_on is required');
            }

            if(_.isUndefined(expiry_date) || _.isNull(expiry_date) || _.isEmpty(_.trim(expiry_date))){
                return fastify.httpErrors.badRequest('expiry date is required');
            }else{
                zone_name = _.trim(zone_name);
                expiry_date = _.trim(expiry_date);
                if(_.size(expiry_date) == 10){
                    expiry_date = `${expiry_date} 23:59:59`;
                }
                let calanderInUserTz = mtz.tz(expiry_date, 'YYYY-MM-DD HH:mm:ss', zone_name);
                expiry_date = calanderInUserTz.tz(process.env.IST_ZONE_NAME).format('YYYY-MM-DD HH:mm:ss');
            }

            const result = await fastify.findUserById(cli_id);
            const userObj = _.get(result, '0', {});

            if (_.isEmpty(userObj)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            }
            const { user_type } = userObj;
            // update the table

            await fastify.updateAccountSettings(cli_id, _.trim(mobile), _.trim(email), zone_name, _.trim(offset), bill_type, message_type, platform_cluster, sms_priority, loggedin_userid, user_type, acc_type, invoice_based_on,
            use_default_header, use_default_on_header_fail, acc_default_header, acc_route_id, expiry_date);

            const resp = {
                statusCode: 200,
                message: 'Account settings have been updated successfully',
            };
            reply.code(200);
            req.log.debug(`/updateaccountsettings response  =>${resp}`);
            return reply.send(resp);
        } catch (err) {
            req.log.error(`Error in /updateaccountsettings => ${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not update account settings. Please try again', { err });
            return e;
        }
    });

    fastify.post('/updateaccountservices', { schema: accountServicesSchema }, async (req) => {
        try {
            const { userid: loggedin_cli_id, user } = req.user;
            // services contains all enabled services(few may be enabled in this req and few may be already enabled) from ui
            const { cli_id, services } = req.body;
            const { row_sms_rate } = req.body;
            const req_id = req.id;
            const servicesValuesArr = [];
            let smpppass = null;
            let apipass = null;
            let cmpass = null;
            let considerdefaultlength_as_domestic = 1;
            // these columns are not null columns in db
            let ui_pass_cust = '';
            let api_pass_cust = '';
            let smpp_pass_cust = '';

            if (_.isUndefined(cli_id) || _.isNull(cli_id)) {
                return fastify.httpErrors.badRequest('cli_id is required');
            }

            const result = await fastify.findUserById(cli_id);
            const userObj = _.get(result, '0', {});

            if (_.isEmpty(userObj)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            }

            // get existing services for the selected user
            const existingServices = await fastify.getAssignedSubServices(cli_id);
            const userExistingSubServices = _.map(existingServices, (o) => o.sub_service);
            console.log('/updateaccountservices existing services from db => ', userExistingSubServices);

            const passwordRequiredList = [];
            // const requestedServices = _.filter(services, { checked: true });
            // const requestedSubServices = _.map(requestedServices, (o) => _.toLower(o.sub_service));
            const requestedSubServices = _.map(services, (o) => _.toLower(o.sub_service));

            /* prepare values for user_service_map table */
            if (!_.isEmpty(services)) {
                _.forEach(services, (obj) => {
                    const service = _.get(obj, 'service');
                    const sub_service = _.get(obj, 'sub_service');
                    // check if sub service is already available in the existing list, if not, requires to generate password if its api/cm/smpp
                    if (!_.includes(userExistingSubServices, sub_service)) {
                        if (_.eq(sub_service, 'cm')) passwordRequiredList.push(sub_service);
                        if (_.eq(sub_service, 'api')) passwordRequiredList.push(sub_service);
                        if (_.eq(sub_service, 'smpp')) passwordRequiredList.push(sub_service);
                    }
                    servicesValuesArr.push([cli_id, service, sub_service, loggedin_cli_id, new Date(), loggedin_cli_id]);
                });

                if (_.includes(requestedSubServices, 'smpp')) {
                    await fastify.setDefaultSmppSettings(cli_id);
                }

                if (_.includes(requestedSubServices, 'international')) {
                    considerdefaultlength_as_domestic = 0;
                    if (!_.isUndefined(row_sms_rate) && !_.isNull(row_sms_rate) && +row_sms_rate <= 0) {
                        return fastify.httpErrors.badRequest('row_sms_rate should be greater than 0');
                    }
                }
            }

            console.log('/updateaccountservices password to be generated for the new services => ', passwordRequiredList);

            // generate passwords for services if needed
            if (_.includes(passwordRequiredList, 'smpp')) {
                const smppdata = await fastify.smpppass(cli_id);
                smpppass = _.get(smppdata.passwords, 0).dbinsert_password;
                smpp_pass_cust = _.get(smppdata.passwords, 0).customer_password;
            }
            if (_.includes(passwordRequiredList, 'api')) {
                const apidata = await fastify.apipass(cli_id);
                apipass = _.get(apidata.passwords, 0).dbinsert_password;
                api_pass_cust = _.get(apidata.passwords, 0).customer_password;
            }
            if (_.includes(passwordRequiredList, 'cm')) {
                // generate new password
                ui_pass_cust = fastify.nanoiduipass();
                console.log('/updateaccountservices new password for the user => ', cli_id, ' is ', ui_pass_cust);
                // hash the password
                cmpass = fastify.hash(ui_pass_cust);
                console.log('/updateaccountservices hashed password => ', cmpass);
            }

            let row_req_obj = null;
            if (_.includes(requestedSubServices, 'international') && !_.isUndefined(row_sms_rate) && !_.isNull(row_sms_rate) && +row_sms_rate > 0) {
                const billing_currency = _.get(userObj, 'billing_currency');
                const billtobaseRate = await fastify.getConversionRateForUser(cli_id, process.env.INTL_BASE_CURRENCY);
                const converted_rate = _.ceil(billtobaseRate * row_sms_rate, 12);
                row_req_obj = { username: user, converted_rate, sms_rate: row_sms_rate, req_id, billing_currency };
            }

            await fastify.updateAccountServices(cli_id, servicesValuesArr, loggedin_cli_id, smpppass, apipass, cmpass, row_req_obj, considerdefaultlength_as_domestic);

            // send pass thru email
            if (_.includes(passwordRequiredList, 'smpp')) {
                try {
                    await fastify.redis.GEN.hset('cli:smpp:pass', cli_id, smpp_pass_cust);
                } catch (e) {
                    console.log('/updateaccountservices ERROR while sending mail for smpp ***IGNORING*** ', e);
                }
            }
            if (_.includes(passwordRequiredList, 'api')) {
                try {
                    await fastify.redis.GEN.hset('cli:api:pass', cli_id, api_pass_cust);
                } catch (e) {
                    console.log('/updateaccountservices ERROR while sending mail for api ***IGNORING*** ', e);
                }
            }
            if (_.includes(passwordRequiredList, 'cm')) {
                try {
                    await fastify.redis.GEN.hset('cli:gui:pass', cli_id, ui_pass_cust);
                } catch (e) {
                    console.log('/updateaccountservices ERROR while sending mail for ui ***IGNORING*** ', e);
                }
            }

            /** ********************************************* Send Email  ******************************************** */
            if (_.includes(passwordRequiredList, 'api') || _.includes(passwordRequiredList, 'smpp') || _.includes(passwordRequiredList, 'cm')) {
                console.log('sending mail...');
                const r1 = await fastify.findAdmin(user);
                const userObjLoggedin = _.get(r1, '0', {});
                const from_fname = _.get(userObjLoggedin, 'username');
                const from_email = _.get(userObjLoggedin, 'email');
                const to_fname = _.get(userObj, 'firstname');
                const to_lname = _.get(userObj, 'lastname');
                const to_email = _.get(userObj, 'email');
                const subject = Mustache.render(process.env.EDIT_ACCOUNT_MAIL_SUBJECT, { to_fname, to_lname });

                const emailTemplate = fs.readFileSync('./templates/add_service_email_template.html', 'utf8');

                const dataObj = constructEmailData(from_fname, to_fname, to_lname, from_email, to_email, from_email, subject, ui_pass_cust, api_pass_cust, smpp_pass_cust, passwordRequiredList);

                try {
                    const respMail = await fastify.sendEmail(emailTemplate, dataObj);
                    console.log(respMail);
                } catch (e) {
                    console.error('/anew ERROR while sending mail ***IGNORING*** ', e);
                }
            }

            /** ********************************************* End of Send Email  ******************************************** */

            // TODO: need to check the updated status and reply accordingly { affectedRows: 1, insertId: 0, warningStatus: 0 }

            const resp = { statusCode: 200, message: 'Account services has been modified successfully' };

            return resp;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not update account services. Please try again', { code });
            return e;
        }
    });

    fastify.post('/updatesmppsettings', { schema: accountSMPPSettingsSchema }, async (req, reply) => {
        try {
            const { cli_id, bind_type, max_allowed_connections, throttle_limit, smpp_charset, dlt_entityid_tag, dlt_templateid_tag, cli_mid_tag, dn_date_format } = req.body;

            if (_.isUndefined(cli_id) || _.isNull(cli_id)) {
                return fastify.httpErrors.badRequest('cli_id value is missing in req payload');
            } if (_.isUndefined(smpp_charset) || _.isNull(smpp_charset) || _.isEmpty(_.trim(smpp_charset))) {
                return fastify.httpErrors.badRequest('smpp_charset value is missing in req payload');
            } if (_.isUndefined(dlt_entityid_tag) || _.isNull(dlt_entityid_tag)) {
                return fastify.httpErrors.badRequest('dlt_entityid_tag value is missing in req payload');
            } if (_.isUndefined(dlt_templateid_tag) || _.isNull(dlt_templateid_tag)) {
                return fastify.httpErrors.badRequest('dlt_templateid_tag value is missing in req payload');
            } if (_.isUndefined(cli_mid_tag) || _.isNull(cli_mid_tag)) {
                return fastify.httpErrors.badRequest('cli_mid_tag value is missing in req payload');
            } if (_.isUndefined(dn_date_format) || _.isNull(dn_date_format) || _.isEmpty(_.trim(dn_date_format))) {
                return fastify.httpErrors.badRequest('dn_date_format value is missing in req payload');
            }

            const result = await fastify.findUserById(cli_id);
            const userObj = _.get(result, '0', {});

            if (_.isEmpty(userObj)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            }

            // update the table
            await fastify.updateAccountSMPPSettings(cli_id, bind_type, max_allowed_connections, throttle_limit, smpp_charset, dlt_entityid_tag, dlt_templateid_tag, cli_mid_tag, dn_date_format);

            const resp = {
                statusCode: 200,
                message: 'Account settings have been updated successfully',
            };
            reply.code(200);

            return reply.send(resp);
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not update account smpp settings. Please try again', { err });
            return e;
        }
    });

    fastify.post('/updatedlttemplates', { schema: accountDLTGroupsSchema }, async (req, reply) => {
        try {
            const { cli_id, allocated_tgroup_ids, assigned_tgroup_id } = req.body;
            const { userid: loggedin_cli_id } = req.user;
            const tgValuesArr = [];

            if (_.isUndefined(cli_id) || _.isNull(cli_id)) {
                return fastify.httpErrors.badRequest('cli_id value is missing in req payload');
            } if (_.isUndefined(assigned_tgroup_id) || _.isNull(assigned_tgroup_id)) {
                return fastify.httpErrors.badRequest('assigned_tgroup_id value is missing in req payload');
            } if (_.isUndefined(allocated_tgroup_ids) || _.isNull(allocated_tgroup_ids) || _.size(allocated_tgroup_ids) == 0) {
                return fastify.httpErrors.badRequest('allocated_tgroup_ids value is missing in req payload');
            }

            const result = await fastify.findUserById(cli_id);
            const userObj = _.get(result, '0', {});

            if (_.isEmpty(userObj)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            }

            /* prepare values for users_templategroup_ids table */
            if (!_.isEmpty(allocated_tgroup_ids)) {
                _.forEach(allocated_tgroup_ids, (val) => {
                    tgValuesArr.push([cli_id, val, loggedin_cli_id]);
                });
            }

            // update the table
            await fastify.updateAccountDltTemplates(cli_id, tgValuesArr, assigned_tgroup_id, loggedin_cli_id);

            const resp = {
                statusCode: 200,
                message: 'Account dlt templates have been updated successfully',
            };
            reply.code(200);

            return reply.send(resp);
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not update account dlt templates. Please try again', { err });
            return e;
        }
    });

    fastify.post('/updateothersettings', { schema: accountOtherSettingsSchema }, async (req, reply) => {
        try {
            const {
                cli_id, sms_retry, client_encrypt, bill_encrypt_type, domestic_sms_blockout,
                blklist_chk, spam_chk, dup_chk_req, intl_sms_blockout, optin_chk_req, sales_id, domestic_special_series_allow, req_hex_msg, is_ildo,
                full_message, camp_name_auto_gen, subusers_reports,
                dnd_chk, msg_retry_available,
                dnd_reject_yn, dnd_pref, force_dnd_check,
                uc_iden_allow, is_remove_uc_chars, timebound_chk_enable,
                vl_shortner, url_smartlink_enable, url_track_enabled,
                domestic_tra_blockout_reject, is_async, considerdefaultlength_as_domestic,
                mt_adjust, dn_adjust, msg_replace_chk, is_schedule_allow, inactive_login
                } = req.body;

            let {
                newline_chars, domestic_sms_blockout_start, domestic_sms_blockout_stop, dup_chk_interval, intl_sms_blockout_start, intl_sms_blockout_stop, trai_blockout, is_16bit_udh,
                ip_validation, ip_list, uc_iden_char_len, uc_iden_occur, vl_shortcode_len,
                timebound_interval, timebound_max_count_allow,
            } = req.body;

            const { userid: loggedin_cli_id } = req.user;

            if (_.isUndefined(cli_id) || _.isNull(cli_id)) {
                return fastify.httpErrors.badRequest('cli_id value is missing in req payload');
            }

            if (!_.isUndefined(newline_chars) && !_.isNull(newline_chars)) {
                newline_chars = _.trim(newline_chars);
                if (newline_chars.includes('~')) {
                    return fastify.httpErrors.badRequest('newline character can not be set to or have ~');
                }
            } else {
                newline_chars = null;
            }

            if (!_.isUndefined(domestic_sms_blockout) && !_.isNull(domestic_sms_blockout) && +domestic_sms_blockout > 0) {
                if (_.isUndefined(domestic_sms_blockout_start) || _.isNull(domestic_sms_blockout_start)
                || _.isEmpty(_.trim(domestic_sms_blockout_start))) {
                    return fastify.httpErrors.badRequest('domestic_sms_blockout_start is required');
                }
                if (_.isUndefined(domestic_sms_blockout_stop) || _.isNull(domestic_sms_blockout_stop)
                || _.isEmpty(_.trim(domestic_sms_blockout_stop))) {
                    return fastify.httpErrors.badRequest('domestic_sms_blockout_stop is required');
                }
                domestic_sms_blockout_start = _.trim(domestic_sms_blockout_start);
                domestic_sms_blockout_stop = _.trim(domestic_sms_blockout_stop);
                const response = validateBlockoutTimes(domestic_sms_blockout_start, domestic_sms_blockout_stop);
                if (!_.isUndefined(response) && !_.isNull(response) && !_.isEmpty(_.trim(response.field))) {
                    const reason = _.get(response, 'field');
                    if (_.isEqual(reason, 'start')) {
                        return fastify.httpErrors.badRequest('domestic_sms_blockout_start is not valid');
                    }
                    if (_.isEqual(reason, 'end')) {
                        return fastify.httpErrors.badRequest('domestic_sms_blockout_stop is not valid');
                    }
                    if (_.isEqual(reason, 'equal')) {
                        return fastify.httpErrors.badRequest('domestic_sms_blockout_start and domestic_sms_blockout_stop can not be same time');
                    }
                    return fastify.httpErrors.badRequest(reason);
                }
                domestic_sms_blockout_start = response.starttime;
                domestic_sms_blockout_stop = response.endtime;
            } else {
                domestic_sms_blockout_start = null;
                domestic_sms_blockout_stop = null;
            }

            if (!_.isUndefined(intl_sms_blockout) && !_.isNull(intl_sms_blockout) && +intl_sms_blockout > 0) {
                if (_.isUndefined(intl_sms_blockout_start) || _.isNull(intl_sms_blockout_start)
                || _.isEmpty(_.trim(intl_sms_blockout_start))) {
                    return fastify.httpErrors.badRequest('intl_sms_blockout_start is required');
                }
                if (_.isUndefined(intl_sms_blockout_stop) || _.isNull(intl_sms_blockout_stop)
                || _.isEmpty(_.trim(intl_sms_blockout_stop))) {
                    return fastify.httpErrors.badRequest('intl_sms_blockout_stop is required');
                }
                intl_sms_blockout_start = _.trim(intl_sms_blockout_start);
                intl_sms_blockout_stop = _.trim(intl_sms_blockout_stop);
                const response = validateBlockoutTimes(intl_sms_blockout_start, intl_sms_blockout_stop);
                if (!_.isUndefined(response) && !_.isNull(response) && !_.isEmpty(_.trim(response.field))) {
                    const reason = _.get(response, 'field');
                    if (_.isEqual(reason, 'start')) {
                        return fastify.httpErrors.badRequest('intl_sms_blockout_start is not valid');
                    }
                    if (_.isEqual(reason, 'end')) {
                        return fastify.httpErrors.badRequest('intl_sms_blockout_stop is not valid');
                    }
                    if (_.isEqual(reason, 'equal')) {
                        return fastify.httpErrors.badRequest('intl_sms_blockout_start and intl_sms_blockout_stop can not be same time');
                    }
                    return fastify.httpErrors.badRequest(reason);
                }
                intl_sms_blockout_start = response.starttime;
                intl_sms_blockout_stop = response.endtime;
            } else {
                intl_sms_blockout_start = null;
                intl_sms_blockout_stop = null;
            }

            if (!_.isUndefined(dup_chk_req) && !_.isNull(dup_chk_req) && +dup_chk_req > 0) {
                if (_.isUndefined(dup_chk_interval) || _.isNull(dup_chk_interval)) {
                    return fastify.httpErrors.badRequest('dup_chk_interval is required');
                }
                if (+dup_chk_interval < 0) {
                    return fastify.httpErrors.badRequest('dup_chk_interval value is invalid');
                }
            } else {
                dup_chk_interval = 0;
            }

            if (_.isUndefined(is_16bit_udh) || _.isNull(is_16bit_udh)) {
                is_16bit_udh = 0;
            }

            if (+uc_iden_allow === 1) {
                if (_.isUndefined(uc_iden_char_len) || _.isNull(uc_iden_char_len)) {
                    return fastify.httpErrors.badRequest('uc_iden_char_len is required');
                }
                if (+uc_iden_char_len < 1 || +uc_iden_char_len > 99) {
                    return fastify.httpErrors.badRequest('uc_iden_char_len value is invalid');
                }
                if (_.isUndefined(uc_iden_occur) || _.isNull(uc_iden_occur)) {
                    return fastify.httpErrors.badRequest('uc_iden_occur is required');
                }
                if (+uc_iden_occur < 1 || +uc_iden_occur > 99) {
                    return fastify.httpErrors.badRequest('uc_iden_occur value is invalid');
                }
            } else {
                uc_iden_char_len = 0;
                uc_iden_occur = 0;
            }

            if (+vl_shortner === 1 || +url_smartlink_enable === 1) {
                if (_.isUndefined(vl_shortcode_len) || _.isNull(vl_shortcode_len) || vl_shortcode_len == '') {
                    return fastify.httpErrors.badRequest('vl_shortcode_len is required');
                }
            } else {
                vl_shortcode_len = 0;
            }

            const result = await fastify.findUserById(cli_id);
            const userObj = _.get(result, '0', {});

            if (_.isEmpty(userObj)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            }

            const msg_type = _.get(userObj, 'msg_type', null);
            if (!_.isNull(msg_type)) {
                if (+msg_type == 1) {
                    // trai_blockout is not applicable to transactional account, hence setting it to process next day(0)
                    trai_blockout = 0;
                } else if (_.isUndefined(trai_blockout) || _.isNull(trai_blockout)) {
                        return fastify.httpErrors.badRequest('trai_blockout value is missing in req payload');
                    }
            }

            if (_.isUndefined(ip_validation) || _.isNull(ip_validation)) {
                ip_validation = 0;
                ip_list = null;
            } else if (+ip_validation == 1) {
                if (_.isUndefined(ip_list) || _.isNull(ip_list) || _.isEmpty(_.trim(ip_list))) {
                    return fastify.httpErrors.badRequest('IP address is required');
                }
                ip_list = _.trim(ip_list);
            } else {
                ip_validation = 0;
                ip_list = null;
            }

            if (+timebound_chk_enable == 1) {
                if (_.isUndefined(timebound_interval) || _.isNull(timebound_interval)) {
                    return fastify.httpErrors.badRequest('timebound_interval is required');
                }
                if (+timebound_interval < 0 || +timebound_interval > 99) {
                    return fastify.httpErrors.badRequest('timebound_interval value is invalid');
                }
                if (_.isUndefined(timebound_max_count_allow) || _.isNull(timebound_max_count_allow)) {
                    return fastify.httpErrors.badRequest('timebound_max_count_allow is required');
                }
                if (+timebound_max_count_allow < 0 || +timebound_max_count_allow > 99) {
                    return fastify.httpErrors.badRequest('timebound_max_count_allow value is invalid');
                }
            } else {
                timebound_interval = 0;
                timebound_max_count_allow = 0;
            }
            const req_obj = {
                cli_id,
                loggedin_cli_id,
                newline_chars,
                sms_retry,
                client_encrypt,
                bill_encrypt_type,
                trai_blockout,
                domestic_sms_blockout,
                domestic_sms_blockout_start,
                domestic_sms_blockout_stop,
                blklist_chk,
                spam_chk,
                dup_chk_req,
                dup_chk_interval,
                intl_sms_blockout,
                intl_sms_blockout_start,
                intl_sms_blockout_stop,
                optin_chk_req,
                sales_id,
                domestic_special_series_allow,
                req_hex_msg,
                is_ildo,
                msg_type,
                is_16bit_udh,
                ip_validation,
                ip_list,
                full_message,
                camp_name_auto_gen,
                subusers_reports,
                dnd_chk,
                msg_retry_available,
                dnd_reject_yn,
                dnd_pref,
                force_dnd_check,
                uc_iden_allow,
                uc_iden_char_len,
                uc_iden_occur,
                is_remove_uc_chars,
                timebound_chk_enable,
                timebound_interval,
                timebound_max_count_allow,
                vl_shortner,
                url_smartlink_enable,
                vl_shortcode_len,
                url_track_enabled,
                domestic_tra_blockout_reject,
                is_async,
                considerdefaultlength_as_domestic,
                mt_adjust,
                dn_adjust,
                msg_replace_chk,
                is_schedule_allow,
                inactive_login
            };

            // update the table
            await fastify.updateAccountsOtherSettings(req_obj);

            const resp = {
                statusCode: 200,
                message: 'Account other settings have been updated successfully',
            };
            reply.code(200);

            return reply.send(resp);
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not update accounts other settings. Please try again', { err });
            return e;
        }
    });

    fastify.post('/updatedomesticsmsrates', { schema: smsDomesticRatesSchema }, async (req, reply) => {
        try {
            const { cli_id, smsrate, dltrate } = req.body;
            const { userid: loggedin_cli_id } = req.user;
            const req_id = req.id;

            if (_.isUndefined(cli_id) || _.isNull(cli_id)) {
                return fastify.httpErrors.badRequest('cli_id value is missing in req payload');
            } if (_.isUndefined(smsrate) || _.isNull(smsrate) || +smsrate <= 0) {
                return fastify.httpErrors.badRequest('smsrate value is missing in req payload');
            } if (_.isUndefined(dltrate) || _.isNull(dltrate) || +dltrate <= 0) {
                return fastify.httpErrors.badRequest('dltrate value is missing in req payload');
            }

            const result = await fastify.findUserById(cli_id);
            const userObj = _.get(result, '0', {});

            if (_.isEmpty(userObj)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            }

            if (+userObj.user_type === 1 || +userObj.user_type === 2) {
                const parentUser = await fastify.findUserById(userObj.pu_id);
                const parentUserObj = _.get(parentUser, '0', {});
                if (_.isEmpty(parentUserObj)) {
                    return fastify.httpErrors.badRequest('Could not find parent user information');
                }
                const { base_sms_rate: p_smsrate, base_add_fixed_rate: p_dltrate } = parentUserObj;
                if (_.isUndefined(p_smsrate) || _.isNull(p_smsrate) || +p_smsrate <= 0) {
                    return fastify.httpErrors.badRequest('parent smsrate is invalid');
                } if (_.isUndefined(p_dltrate) || _.isNull(p_dltrate) || +p_dltrate <= 0) {
                    return fastify.httpErrors.badRequest('parent dltrate is invalid');
                } if (+p_smsrate > smsrate) {
                    return fastify.httpErrors.badRequest(`smsrate can not be less than parent smsrate (${p_smsrate})`);
                } if (+p_dltrate > dltrate) {
                    return fastify.httpErrors.badRequest(`dltrate can not be less than parent dltrate (${p_dltrate})`);
                }
            }

            const req_obj = { req_id, cli_id, smsrate, dltrate, loggedin_cli_id, old_smsrate: userObj.base_sms_rate, old_dltrate: userObj.base_add_fixed_rate, billing_currency: userObj.billing_currency, username: userObj.user };

            // update the table
            await fastify.updateDomesticSmsRates(req_obj);

            const resp = {
                statusCode: 200,
                message: 'Account domestic rates have been updated successfully',
            };
            reply.code(200);

            return reply.send(resp);
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not update accounts domestic rates. Please try again', { err });
            return e;
        }
    });

    fastify.post('/updateintlsmsrates', { schema: smsInternationalRatesSchema }, async (req, reply) => {
        try {
            const { userid: loggedin_cli_id } = req.user;
            const { cli_id, add_arr, update_arr, delete_arr } = req.body;
            // const { row_sms_rate, row_sms_rate_old } = req.body;
            const req_id = req.id;
            const addValuesArr = [];
            const updateValuesArr = [];
            const updateChangesValuesArr = [];

            console.log('/updateintlsmsrates incoming params => ', [cli_id, add_arr, update_arr, delete_arr]);

            if (_.isUndefined(cli_id) || _.isNull(cli_id)) {
                return fastify.httpErrors.badRequest('cli_id value is missing in req payload');
            }
            // if (_.isUndefined(row_sms_rate) || _.isNull(row_sms_rate) || +row_sms_rate <= 0) {
              //  return fastify.httpErrors.badRequest('row_sms_rate should be greater than 0');
            // }

            if (_.size(add_arr) > 0) {
                const invalid_rates = _.filter(add_arr, (obj) => obj.sms_rate <= 0);

                if (_.size(invalid_rates) > 0) {
                    return fastify.httpErrors.badRequest('sms_rate should be greater than 0');
                }
            }

            if (_.size(update_arr) > 0) {
                const invalid_rates = _.filter(update_arr, (obj) => obj.sms_rate <= 0);

                if (_.size(invalid_rates) > 0) {
                    return fastify.httpErrors.badRequest('sms_rate should be greater than 0');
                }
            }

            const [result, hasIntlService] = await Promise.all([fastify.findUserById(cli_id), fastify.hasInternationalService(cli_id)]);
            const userObj = _.get(result, '0', {});

            if (_.isEmpty(userObj)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            } if (!hasIntlService) {
                return fastify.httpErrors.badRequest('international service is not enabled for the user');
            }

            const user = _.get(userObj, 'user');
            const billing_currency = _.get(userObj, 'billing_currency');

            const billtobaseRate = await fastify.getConversionRateForUser(cli_id, process.env.INTL_BASE_CURRENCY);

            // for new
            if (add_arr.length > 0) {
                _.forEach(add_arr, (o) => {
                    const converted_rate = _.ceil(billtobaseRate * o.sms_rate, 12);
                    addValuesArr.push([cli_id, o.country, converted_rate]);
                });
            }
            // for update
            if (update_arr.length > 0) {
                _.forEach(update_arr, (o) => {
                    const converted_rate = _.ceil(billtobaseRate * o.sms_rate, 12);
                    updateValuesArr.push([converted_rate, cli_id, _.toLower(o.country)]);
                    updateChangesValuesArr.push([fastify.nanoid(), req_id, cli_id, user, o.country, o.sms_rate, o.sms_rate_old, 0, 0, loggedin_cli_id, billing_currency]);
                });
            }

            // const converted_rate = _.ceil(billtobaseRate * row_sms_rate, 12);
            // const row_req_obj = { username: user, converted_rate, sms_rate: row_sms_rate, sms_rate_old: row_sms_rate_old, loggedin_cli_id, req_id, billing_currency };
            const row_req_obj = null;
            const result1 = await fastify.updateIntlSmsRates(cli_id, addValuesArr, updateValuesArr, updateChangesValuesArr, delete_arr, row_req_obj);
            console.log('/updateintlsmsrates resp from db => ', result1);

            const resp = { statusCode: 200, message: 'International SMS Rates have been modified successfully' };

            reply.code(200);

            return reply.send(resp);
        } catch (err) {
          const code = fastify.nanoid();
          req.log.error(`error ${code}  =>${err}`);
          const e = fastify.httpErrors.createError(500, 'Could not update account rates. Please try again', { code });
          return e;
        }
    });

    fastify.post('/updaterowsmsrate', { schema: smsRateRowSchema }, async (req, reply) => {
        try {
          const { userid: loggedin_cli_id } = req.user;
          const { cli_id, sms_rate, sms_rate_old } = req.body;
          const req_id = req.id;

          console.log('/updaterowsmsrate incoming params => ', [cli_id, sms_rate, sms_rate_old]);

          const result = await fastify.findUserById(cli_id);
          const userObj = _.get(result, '0', {});

          if (_.isEmpty(userObj)) {
            return fastify.httpErrors.badRequest('Could not find user information');
          }

          const user = _.get(userObj, 'user');
          const billing_currency = _.get(userObj, 'billing_currency');

          const billtobaseRate = await fastify.getConversionRateForUser(cli_id, process.env.INTL_BASE_CURRENCY);

          const converted_rate = _.ceil(billtobaseRate * sms_rate, 12);

          const result1 = await fastify.updateSMSRatesForROW(cli_id, user, converted_rate, sms_rate, sms_rate_old, loggedin_cli_id, req_id, billing_currency);
          console.log('/updaterowsmsrate resp from db => ', result1);

          const resp = { statusCode: 200, message: 'SMS Rate (ROW) has been modified successfully' };

          reply.code(200);

          return reply.send(resp);
        } catch (err) {
          const code = fastify.nanoid();
          req.log.error(`error ${code}  =>${err}`);
          const e = fastify.httpErrors.createError(500, 'Could not update ROW rates. Please try again', { code });
          return e;
        }
    });

    fastify.post('/updatewalletamount', { schema: walletUpdateSchema }, async (req, reply) => {
        try {
            const { userid: loggedin_cli_id, sessionid, user } = req.user;
            const { cli_id, amount, action, comments } = req.body;
            let cur_bal = 0;
            const walletTransValuesArr = [];

            console.log('/updatewalletamount incoming params => ', [cli_id, amount, action, comments]);

            if (_.isUndefined(cli_id) || _.isNull(cli_id)) {
                return fastify.httpErrors.badRequest('cli_id value is missing in req payload');
            }

            if (_.isUndefined(amount) || _.isNull(amount) || +amount <= 0) {
                return fastify.httpErrors.badRequest('Invalid wallet amount');
            }

            // find the username of cli_id
            const result1 = await fastify.findUserById(cli_id);
            const userObj = _.get(result1, '0', {});

            if (_.isEmpty(userObj)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            } if (+userObj.user_type === 1 || +userObj.user_type === 2) {
                // restricted actions
                return fastify.httpErrors.badRequest('Can not edit admin/users directly. Action restricted.');
            }

            const username = _.get(userObj, 'user', '');

            if (+userObj.bill_type != 1) { // not a wallet account
                return fastify.httpErrors.badRequest('It does not appear to be a prepaid account');
            }

            // MUI-5 : Logged in users balance is set to 0
            const loggedin_bal_before = 0;
            const loggedin_bal_after = 0;
//            const old_bal = 0;
            const cli_id_bal = await fastify.redis.WALLET.hget('wallet:amount', cli_id);
            if (_.eq(action, 'add')) {
                cur_bal = await fastify.redis.WALLET.hincrbyfloat('wallet:amount', cli_id, amount);

                walletTransValuesArr.push(fastify.nanoid(), cli_id, username, 'add', amount, 'Edit Account /updatewalletamount', comments, sessionid, loggedin_cli_id, user, cur_bal, loggedin_bal_before, loggedin_bal_after, cli_id_bal);
            }

            if (_.eq(action, 'deduct')) {
                // check if cli_id has enough bal for the deduction

                if (_.subtract(cli_id_bal, amount) < 0) {
                    console.log('/updatewalletamount [deduct] *** not enough bal available in user\'s wallet for the deduction  => ', cli_id_bal, amount, cli_id);

                    // send the error response
                    const resp = {
                        statusCode: fastify.CONSTANTS.NOT_ENOUGH_WALLET_BAL,
                        message: `User does not have enough wallet balance to make deduction. User's current balance is ${cli_id_bal}`,
                    };
                    reply.code(200);
                    return reply.send(resp);
                }

                cur_bal = await fastify.redis.WALLET.hincrbyfloat('wallet:amount', cli_id, -amount);

                // walletTransValuesArr.push([fastify.nanoid(), cli_id, username, 'deduct', amount, 'Edit Account /updatewalletamount', comments, sessionid, loggedin_cli_id, user, cur_bal, loggedin_bal_before, loggedin_bal_after, old_bal]);
                walletTransValuesArr.push(fastify.nanoid(), cli_id, username, 'deduct', amount, 'Edit Account /updatewalletamount', comments, sessionid, loggedin_cli_id, user, cur_bal, loggedin_bal_before, loggedin_bal_after, cli_id_bal);
            }

            // persist to wallet transaction
            try {
                await fastify.persistWalletTransaction(walletTransValuesArr);
                console.log('/updatewalletamount SUCCESSFULLY persisted the wallet transaction... ', JSON.stringify(walletTransValuesArr));
            } catch (e) {
                // TODO: *** Data Leakage *** this needs to be handled
                console.error('/updatewalletamount *** Error Persisting Wallet transaction ***', JSON.stringify(walletTransValuesArr));
            }
            // trim to max 6 decimal places
            cur_bal = _.floor(cur_bal, 6);

            const resp = {
                statusCode: 200,
                message: 'Wallet amount has been updated successfully',
                wallet_bal: cur_bal,
            };

            return resp;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not update wallet amount. Please try again', { code });
            return e;
        }
    });

    fastify.post('/updateaccountstatus', { schema: updateaccstatusSchema }, async (req, reply) => {
        try {
            const { cli_id, astatus } = req.body;
            const { userid: loggedin_cli_id } = req.user;
            let newstatus = 0, status = '';

            req.log.debug('/updateaccountstatus incoming params => ', [cli_id, astatus]);

            const result = await fastify.findUserById(cli_id);
            const userObj = _.get(result, '0', {});

            if (_.isEmpty(userObj)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            }

            if(_.eq(astatus, 'activate')){
                newstatus = +process.env.ACCOUNT_STATUS_ACTIVE;
                status = 'activated';
            } else if(_.eq(astatus, 'deactivate')){
                newstatus = +process.env.ACCOUNT_STATUS_DEACTIVATED;
                status = 'deactivated';
            } else if(_.eq(astatus, 'suspend')){
                newstatus = +process.env.ACCOUNT_STATUS_SUSPENDED;
                status = 'suspended';
            }

            // update the table
            const result1 = await fastify.updateAccountStatus(loggedin_cli_id, cli_id, newstatus);
            req.log.debug('/updateaccountstatus resp from db => ', result1);

            const resp = {
                statusCode: 200,
                message: `Account ${status} successfully`,
            };
            reply.code(200);
            req.log.debug('/updateaccountstatus resp  => ', JSON.stringify(resp));
            return reply.send(resp);
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not update account status. Please try again', { code });
            return e;
        }
    });

    fastify.get('/salespersons', { schema: salesPersonsSchema }, async () => {
        try {
            const result = await fastify.getAllSalesPersons();
            return result;
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get Sales Persons details. Please try again', { err });
            return e;
        }
    });

    fastify.post('/updatecreditsettings', { schema: creditsSchema }, async (req, reply) => {
        let sa_add_success = false;
        let sa_deduct_success = false;
        let amount = 0;
        let cli_id = 0;
        try {
            const { credit_check } = req.body;
            cli_id = req.body.cli_id;
            amount = req.body.amount;
            let action = req.body.action;
            const { userid: loggedin_cli_id, sessionid, user } = req.user;

            if (_.isUndefined(cli_id) || _.isNull(cli_id)) {
                return fastify.httpErrors.badRequest('cli_id value is missing in req payload');
            } if (_.isUndefined(credit_check) || _.isNull(credit_check)) {
                return fastify.httpErrors.badRequest('credit_check value is missing in req payload');
            } if (+credit_check === 1) {
                if (!_.isUndefined(amount) && !_.isNull(amount) && +amount > 0) {
                    if (_.isUndefined(action) || _.isNull(action)) {
                        return fastify.httpErrors.badRequest('action is missing in req payload');
                    }
                } else if (!_.isUndefined(amount) && !_.isNull(amount) && +amount < 0) {
                    return fastify.httpErrors.badRequest('amount should be greater than 0');
                } else{
                    // amount is undefined/null/0, hence it could be just credit check enable operation
                    action = null;
                }
            }

            const result = await fastify.findUserById(cli_id);
            const userObj = _.get(result, '0', {});

            if (_.isEmpty(userObj)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            }

            const bill_type = _.get(userObj, 'bill_type', null);
            if (_.isNull(bill_type) || +bill_type === 1) {
                return fastify.httpErrors.badRequest('Invalid operation');
            }

            let credit_limit_after_action = 0;
            let credit_limit_available_after_action = 0;
            let credit_limit_available_before_action = 0;
            const walletTransValuesArr = [];
            let comments = null;
            const user_config_params= [];

            if(+credit_check === 1){
                if(_.isEqual(action, 'add')){
                    credit_limit_available_after_action = _.toNumber(await fastify.redis.WALLET.hincrbyfloat('wallet:amount', cli_id, amount));
                    sa_add_success = true;
                    credit_limit_available_before_action = +credit_limit_available_after_action - +amount;
                    credit_limit_after_action = +userObj.credit_limit + +amount;
                    comments = 'Credit limit increased';
                    walletTransValuesArr.push([fastify.nanoid(), cli_id, userObj.user, 'add', amount, 'Edit Account /updatecreditsettings', comments, sessionid, loggedin_cli_id, user, credit_limit_available_after_action, 0, 0, credit_limit_available_before_action]);
                }

                if (_.isEqual(action, 'deduct')) {
                    // check if the amount to deduct is less than credit_limit
                    if(+amount > +userObj.credit_limit){
                        return fastify.httpErrors.badRequest('Invalid amount');
                    }

                    //let cliIdWiseBalance = {};                    
                    let cliIdWiseBalance = await fastify.getWalletBalancesForCliIds([cli_id]);
                    const sa_available_credit = _.get(cliIdWiseBalance, cli_id, 0);
                    comments = 'Credit limit decreased';
                    if(+sa_available_credit >= +amount){
                        // superadmin's account have sufficient credit to deduct
                        credit_limit_available_after_action = _.toNumber(await fastify.redis.WALLET.hincrbyfloat('wallet:amount', cli_id, -amount));
                        sa_deduct_success = true;
                        credit_limit_available_before_action = +credit_limit_available_after_action + +amount;
                        credit_limit_after_action = +userObj.credit_limit - +amount;                  
                        walletTransValuesArr.push([fastify.nanoid(), cli_id, userObj.user, 'deduct', amount, 'Edit Account /updatecreditsettings', comments, sessionid, loggedin_cli_id, user, credit_limit_available_after_action, 0, 0, credit_limit_available_before_action]);
                    } else{
                        // since superadmin's account does not have sufficient credit, deduct some portion from every account and add it to superadmin
                        const subusers = await fastify.getAllUsersForId(cli_id);
                        if(_.size(subusers) == 0){
                            return fastify.httpErrors.badRequest('Insufficient amount');
                        }
                        
                        const cliIds = [];                    
                        _.forEach(subusers, (obj)=>{
                            cliIds.push(_.get(obj, 'cli_id'));
                        });
                        cliIds.push(cli_id);

                        cliIdWiseBalance = await fastify.getWalletBalancesForCliIds(cliIds);
                        
                        let remaining_amount = +amount - +sa_available_credit;
                        const amount_tobe_deduct_per_subaccount = remaining_amount / _.size(subusers);

                        const accounts_with_less_balance = _.filter(subusers, function(user) {
                            let bal = _.get(cliIdWiseBalance, user.cli_id, 0);
                            return _.isNull(bal) || +bal < +amount_tobe_deduct_per_subaccount; 
                        });

                        if(_.size(accounts_with_less_balance) > 0){
                            return fastify.httpErrors.badRequest('Insufficient amount');
                        }

                        // TODO: *** Data Leakage can happen if any errors *** this needs to be handled
                        for await (const obj of subusers){
                            credit_limit_available_after_action = _.toNumber(await fastify.redis.WALLET.hincrbyfloat('wallet:amount', obj.cli_id, -amount_tobe_deduct_per_subaccount));
                            credit_limit_available_before_action = +credit_limit_available_after_action + +amount_tobe_deduct_per_subaccount;
                            credit_limit_after_action = +obj.credit_limit - +amount_tobe_deduct_per_subaccount;
                            
                            credit_limit_available_after_action = _.floor(credit_limit_available_after_action, 4);
                            credit_limit_available_before_action = _.floor(credit_limit_available_before_action, 4);
                            credit_limit_after_action = _.floor(credit_limit_after_action, 4);

                            user_config_params.push({cli_id:obj.cli_id, deduct:amount_tobe_deduct_per_subaccount});
                            
                            let loggedin_bal_after = await fastify.redis.WALLET.hincrbyfloat('wallet:amount', cli_id, amount_tobe_deduct_per_subaccount);
                            let loggedin_bal_before = +loggedin_bal_after - +amount_tobe_deduct_per_subaccount;
                            loggedin_bal_after = _.floor(loggedin_bal_after, 4);
                            loggedin_bal_before = _.floor(loggedin_bal_before, 4);

                            walletTransValuesArr.push([fastify.nanoid(), obj.cli_id, obj.user, 'deduct', amount_tobe_deduct_per_subaccount, 'Edit Account /updatecreditsettings', comments, sessionid, cli_id, userObj.user, credit_limit_available_after_action, loggedin_bal_before, loggedin_bal_after, credit_limit_available_before_action]);
                        } 

                        // Now superadmin's account have sufficient credit to deduct
                        credit_limit_available_after_action = _.toNumber(await fastify.redis.WALLET.hincrbyfloat('wallet:amount', cli_id, -amount));
                        sa_deduct_success = true;
                        credit_limit_available_before_action = +credit_limit_available_after_action + +amount;
                        credit_limit_after_action = +userObj.credit_limit - +amount;                        
                        walletTransValuesArr.push([fastify.nanoid(), cli_id, userObj.user, 'deduct', amount, 'Edit Account /updatecreditsettings', comments, sessionid, loggedin_cli_id, user, credit_limit_available_after_action, 0, 0, credit_limit_available_before_action]);
                    }
                }
            }  

            await fastify.updateCreditSettings(cli_id, credit_check, amount, loggedin_cli_id, action, user_config_params, walletTransValuesArr);

            if(+credit_check === 0 || _.isNull(action)){
                credit_limit_after_action = +userObj.credit_limit;                
                credit_limit_available_after_action = _.toNumber(await fastify.redis.WALLET.hget('wallet:amount', cli_id));
            }

            credit_limit_after_action = _.floor(credit_limit_after_action, 4);
            credit_limit_available_after_action = _.floor(credit_limit_available_after_action, 4);

            const resp = {
                statusCode: 200,
                message: 'Account credit settings have been updated successfully',
                credit_limit: credit_limit_after_action,
                credit_limit_available: credit_limit_available_after_action
            };
            reply.code(200);

            return reply.send(resp);
        } catch (err) {
            if(sa_add_success){
                try {
                    await fastify.redis.WALLET.hincrbyfloat('wallet:amount', cli_id, -amount);
                } catch (error) {
                    console.error(` Credit limit of ${cli_id} increased by ${amount}, but wallet transaction failed. Deduction of the same is failed.`);
                }                
            }
            if(sa_deduct_success){
                try {
                    await fastify.redis.WALLET.hincrbyfloat('wallet:amount', cli_id, amount);
                } catch (error) {
                    console.error(` Credit limit of ${cli_id} decreased by ${amount}, but wallet transaction failed. Refund of the same is failed.`);
                }
            }
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not update account credit settings. Please try again', { err });
            return e;
        }
    });

    fastify.post('/updatecappingsettings', { schema: cappingSchema }, async (req, reply) => {
        try {
            const { cli_id, capping_chk_enable } = req.body;
            let { capping_interval_type, capping_interval, capping_max_count_allow } = req.body;
            const { userid: loggedin_cli_id } = req.user;

            if (_.isUndefined(cli_id) || _.isNull(cli_id)) {
                return fastify.httpErrors.badRequest('cli_id value is missing in req payload');
            } if (_.isUndefined(capping_chk_enable) || _.isNull(capping_chk_enable)) {
                return fastify.httpErrors.badRequest('capping_chk_enable value is missing in req payload');
            }

            const result = await fastify.findUserById(cli_id);
            const userObj = _.get(result, '0', {});

            if (_.isEmpty(userObj)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            }

            if (+capping_chk_enable === 0) {
                capping_interval_type = 0;
                capping_interval = 0;
                capping_max_count_allow = 0;
            } else {
                if (_.isUndefined(capping_interval_type) || _.isNull(capping_interval_type)) {
                    return fastify.httpErrors.badRequest('capping_interval_type value is missing in req payload');
                } if (_.isUndefined(capping_interval) || _.isNull(capping_interval)) {
                    return fastify.httpErrors.badRequest('capping_interval value is missing in req payload');
                } if (+capping_interval < 0 || +capping_interval > 999) {
                    return fastify.httpErrors.badRequest('capping_interval value is invalid');
                } if (_.isUndefined(capping_max_count_allow) || _.isNull(capping_max_count_allow)) {
                    return fastify.httpErrors.badRequest('capping_max_count_allow value is missing in req payload');
                }
            }

            await fastify.updateCappingSettings(cli_id, capping_chk_enable, capping_interval_type, capping_interval, capping_max_count_allow, loggedin_cli_id);

            const resp = {
                statusCode: 200,
                message: 'Account capping settings have been updated successfully',
            };
            reply.code(200);

            return reply.send(resp);
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not update account capping settings. Please try again', { err });
            return e;
        }
    });

    fastify.post('/update2levelauth', { schema: twoLevelAuthUpdationSchema }, async (req, reply) => {
        try {
            const { cli_id, two_level_auth } = req.body;
            const { userid: loggedin_cli_id } = req.user;

            req.log.debug('/update2levelauth incoming params => ', [cli_id, two_level_auth]);

            const result = await fastify.findUserById(cli_id);
            const userObj = _.get(result, '0', {});

            if (_.isEmpty(userObj)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            }

            // update the table
            const result1 = await fastify.update2LevelAuthStatus(loggedin_cli_id, cli_id, two_level_auth, userObj.user_type);
            req.log.debug('/update2levelauth resp from db => ', result1);

            const resp = {
                statusCode: 200,
                message: `Two level authentication ${+two_level_auth === 1 ? 'enabled' : 'disabled'} successfully`,
            };
            reply.code(200);
            req.log.debug('/update2levelauth resp  => ', JSON.stringify(resp));
            return reply.send(resp);
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not update two level authentication. Please try again', { code });
            return e;
        }
    });
}

module.exports = account;
