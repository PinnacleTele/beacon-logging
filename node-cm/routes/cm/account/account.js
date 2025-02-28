/* eslint-disable no-restricted-syntax */
const _ = require('lodash');
const mtz = require('moment-timezone');
const fs = require('fs');
const Mustache = require('mustache');


const {
    unameuniqueSchema, countriesSchema, tgroupsSchema, assignedSubServicesSchema, anewSchema, ainfoSchema, accountsSchema, astatsSchema,
    aupdatePISchema,
    aupdateTGroups,
    aupdate2FASchema,
    aupdatepasswordSchema,
    aupdateMSSchema,
    aupdateServicesSchema,
    aupdateGroupsSchema,
    aupdateWRateSchema,
    aupdateEncryptionSchema,
    aupdateWAmountSchema,
    abalSchema,
    updateaccstatusSchema,
    ausersmwSchema,
    aupdatewamountmultiSchema,
    aquicklinkssettingsSchema,
    aupdatequicklinksettingsSchema,
    aquicklinksSchema,
    awtSchema,
    parentbrrateintlSchema,
    brrateintlSchema,
} = require('../../../schema/account-schema');

async function account(fastify, opts) {
    fastify.addHook('preValidation', async (req, reply) => {
        console.log('preValidation hook called');
        return req.jwtVerify();
    });

    function constructEmailDataa(from_fname, from_lname, to_fname, to_lname, from_email, email, reply_to, subject, ui_pass_cust, api_pass_cust, smpp_pass_cust, services) {
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
    }

    fastify.get('/unameunique', { preValidation: [], schema: unameuniqueSchema }, async (req, reply) => {
        try {
            const { uname } = req.query;

            const result = await fastify.isUsernameFound(uname);
            const { counts } = result[0];

            console.log('matching username count => ', counts);
            const payload = { isUnique: (counts === 0) };
            return payload;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not get username uniqueness. Please try again', { code });
            // const e = fastify.httpErrors.createError(500, err);

            return e;
        }
    });

    fastify.post('/updateaccstatus', { preValidation: [], schema: updateaccstatusSchema }, async (req, reply) => {
        try {
            const { cli_id, astatus } = req.body;
            const { cli_id: loggedin_cli_id } = req.user;
            let newstatus = 0, status = '';

            req.log.debug('/updateaccstatus incoming params => ', [cli_id, astatus]);

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
            req.log.debug('/updateaccstatus resp from db => ', result1);

            const resp = {
                statusCode: 200,
                message: `Account ${status} successfully`,
            };
            reply.code(200);

            return reply.send(resp);
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not update account status. Please try again', { code });
            // const e = fastify.httpErrors.createError(500, err);

            return e;
        }
    });

    fastify.get('/accounts', { preValidation: [], schema: accountsSchema }, async (req, reply) => {
        try {
            const { cli_id, tz } = req.user;

            const result = await fastify.getAllUsersForId(cli_id);
            req.log.debug('/accounts total users => ', result.length);

            const cliIds = [];
            let cliIdWiseBalance = {};
            _.forEach(result, (obj)=>{
                cliIds.push(_.get(obj, 'cli_id'));
            });

            if(_.size(cliIds) > 0){
                cliIdWiseBalance = await fastify.getWalletBalancesForCliIds(cliIds);
            }

            for (const obj of result) {
                const cts = _.get(obj, 'created_ts', null);
                const ctsMoment = mtz.tz(cts, tz);
                const user_cli_id = _.get(obj, 'cli_id');
                if (!_.isNull(cts)) {
                    // convert to acc tz
                    const formattedDt = ctsMoment.format('MMM DD, YYYY HH:mm:ss');
                    _.set(obj, 'created_ts', formattedDt);
                    _.set(obj, 'created_ts_unix', ctsMoment.unix());
                }
                const expiry_date = _.get(obj, 'expiry_date', null);
                if (!_.isNull(expiry_date)) {
                    // convert to acc tz
                    const expiryMoment = mtz.tz(expiry_date, tz);
                    const formattedDt = expiryMoment.format('MMM DD, YYYY HH:mm:ss');
                    _.set(obj, 'expiry_date', formattedDt);
                    _.set(obj, 'expiry_ts_unix', expiryMoment.unix());
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

                // _.set(obj, 'created_ts', '');
                // _.set(obj, 'created_ts_unix', 0);

                const smsrate = _.floor(_.get(obj, 'base_sms_rate', 0), 6);
                const dltrate = _.floor(_.get(obj, 'base_add_fixed_rate', 0), 6);
                let smsleft = -1;
                let walletBal = 0;

                const bill_type = _.get(obj, 'bill_type', null);
                if (!_.isNull(bill_type)){
                    if (+bill_type == 1){
                        walletBal = _.get(cliIdWiseBalance, user_cli_id, null);
                        if(!_.isNull(walletBal)){
                            walletBal = _.floor(+walletBal, 6);
                            smsleft = await fastify.getSMSLeft(walletBal, smsrate, dltrate);                        
                        }                        
                    }
                    _.set(obj, 'wallet_bal', walletBal);
                    _.set(obj, 'sms_left_india', smsleft);
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

    fastify.get('/astats', { preValidation: [], schema: astatsSchema }, async (req, reply) => {
        try {
            const { cli_id } = req.user;
            let total_admins = 0;
            let total_users = 0;
            let total_active = 0;
            let total_inactive = 0;
            let total_groups = 0;
            let total_templates = 0;

            const [result, result1, result2] = await Promise.all([fastify.getAccountStatsForId(cli_id), fastify.getGroups(cli_id, 'all', 'all'), fastify.getTemplates(cli_id, null)]);

            console.log('/astats total templates for cli_id => ', result2.length, cli_id);
            console.log('/astats total groups for cli_id => ', result1.length, cli_id);

            total_groups = result1.length;
            total_templates = result2.length;

            for (const obj of result) {
                const userType = _.get(obj, 'user_type');
                const total = _.get(obj, 'total', 0);
                const accStatus = _.get(obj, 'acc_status');

                console.log(userType, total, accStatus);

                if (+userType === 1) total_admins += +total;
                if (+userType === 2) total_users += +total;
                if (+accStatus === 0) total_active += +total;
                if (+accStatus === 2) total_inactive += +total;
            }
            const payload = {
                total_accounts: (total_admins + total_users),
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

    // TODO: optimize method calls with ||ism
    fastify.get('/ainfo', { preValidation: [], schema: ainfoSchema }, async (req, reply) => {
        try {
            const { cli_id } = req.query;
            const { bill_type, cli_id: loggedin_cli_id } = req.user;

            const result = await fastify.findUserById(cli_id);
            const userObj = _.get(result, '0', {});

            if (_.isEmpty(userObj)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            }

            const { bill_encrypt_type } = userObj;

            // set mobile message encryption
            // 0-Disabled, 1-Encrypt Messages only 2-Encrypt Mobile Only, 3- Encrypt Message and Mobile
            let encrypt_mobile_yn = 0;
            let encrypt_message_yn = 0;
            if (+bill_encrypt_type === 1) encrypt_message_yn = 1;
            if (+bill_encrypt_type === 2) encrypt_mobile_yn = 1;
            if (+bill_encrypt_type === 3) {
                encrypt_mobile_yn = 1;
                encrypt_message_yn = 1;
            }
            // get the accounts/users under him
            const utypeResult = await fastify.getUserCountsByType(cli_id);
            _.forEach(utypeResult, (obj) => {
                const userType = _.get(obj, 'user_type');
                const total = _.get(obj, 'total', 0);
                if (+userType === 1) _.set(userObj, 'total_admins', +total);
                if (+userType === 2) _.set(userObj, 'total_users', +total);
            });

            // get allocated services for parent user
            const resultSSParentUser = await fastify.getAssignedSubServices(loggedin_cli_id);
            // get allocated services for this user
            const resultSSThisUser = await fastify.getAssignedSubServices(cli_id);
            const gbThisUser = _.groupBy(resultSSThisUser, 'service');

            _.forEach(resultSSParentUser, (obj) => {
                const service = _.get(obj, 'service');
                const sub_service = _.get(obj, 'sub_service');
                const arr = _.map(_.get(gbThisUser, service), 'sub_service');
                if (_.includes(arr, sub_service)) {
                    _.set(obj, 'enabled_yn', 1);
                } else {
                    _.set(obj, 'enabled_yn', 0);
                }
            });

            // get the SMPP settings for this user
            // TODO: optimize this method call. call this only if SMPP service is enabled
            let smpp_charset = '';
            const resultSMPP = await fastify.getSMPPConfigForId(cli_id);
            console.log('/ainfo resultSMPP => ', resultSMPP);
            const smppObj = _.get(resultSMPP, '0', {});

            if (!_.isEmpty(smppObj)) {
                smpp_charset = _.get(smppObj, 'charset', '');
            }

            // get assigned groups (shared)
            const resultAG = await fastify.getAssignedGroups(cli_id);
            const assignedGroups = [];
            _.forEach(resultAG, (obj) => {
                const o = {
                    g_id: _.get(obj, 'id'),
                    g_name: _.get(obj, 'g_name'),
                    g_type: _.get(obj, 'g_type'),
                };
                assignedGroups.push(o);
            });

            let [resultTG, resultTGInfo, user_configs] = await Promise.all([fastify.getTemplateGroups(cli_id), fastify.getTemplateGroupInfo(userObj.dlt_templ_grp_id), fastify.getUserConfigs(cli_id)]);
            
            // get allocated dlt template groups
            //const resultTG = await fastify.getTemplateGroups(cli_id);
            console.log('/ainfo Allocated dlt template groups from db =>', resultTG);

            //const resultTGInfo = await fastify.getTemplateGroupInfo(userObj.dlt_templ_grp_id);
            console.log('/ainfo template group info from db =>', resultTGInfo);

            const templateGroupName = _.get(resultTGInfo[0], 'template_group_name', ' ');
            const smsrate = _.floor(_.get(userObj, 'base_sms_rate', 0), 6);
            const dltrate = _.floor(_.get(userObj, 'base_add_fixed_rate', 0), 6);
            let smsleft = -1;

            // check if ROW rate is defined for this user
            const r = await fastify.getROWForAllUsers([cli_id]);
            const has_row = r.length > 0 ? 1 : 0;

            _.set(userObj, 'encrypt_mobile_yn', encrypt_mobile_yn);
            _.set(userObj, 'encrypt_message_yn', encrypt_message_yn);
            _.set(userObj, 'services', resultSSParentUser);
            _.set(userObj, 'has_row_yn', has_row);
            _.set(userObj, 'allocated_tgroups', resultTG);
            _.set(userObj, 'assigned_groups', assignedGroups);
            _.set(userObj, 'dlt_templ_grp_name', templateGroupName);
            _.set(userObj, 'smsrate', smsrate);
            _.set(userObj, 'dltrate', dltrate);
            _.set(userObj, 'smpp_charset', smpp_charset);

            if (+bill_type === 1) {
                const amount = await fastify.redis.WALLET.hget('wallet:amount', cli_id);
                _.set(userObj, 'wallet', +amount);
                // get the total sms that can be sent for this wallet (only for india)
                smsleft = await fastify.getSMSLeft(amount, smsrate, dltrate);
            }

            _.set(userObj, 'sms_left', smsleft);

            const cts = _.get(userObj, 'created_ts', null);
            const mts = _.get(userObj, 'updated_ts', null);
            const expiry_date = _.get(userObj, 'expiry_date', null);
            let tz = _.get(userObj, 'time_zone', null);

            // if tz is empty, defalt it to IST (this case shud not happen, just in case)
            if (!tz) tz = process.env.IST_ZONE_NAME;

            const ctsMoment = mtz.tz(cts, tz);
            const mtsMoment = mtz.tz(mts, tz);

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
                const expiryMoment = mtz.tz(expiry_date, tz);
                _.set(userObj, 'expiry_date', expiryMoment.format('YYYY-MM-DD HH:mm:ss'));
                _.set(userObj, 'expiry_ts', expiryMoment.format('MMM DD, YYYY HH:mm:ss'));
                _.set(userObj, 'expiry_ts_unix', expiryMoment.unix());
            }

            const zoneAbbr = mtz.tz.zone(tz).abbr(new Date()); // e.g IST
            _.set(userObj, 'time_zone_abbr', zoneAbbr);

            let auto_gen_cname_enabled = 0;
            let subusers_reports_enabled = 0;
            let fullmsg_enabled = 0;
            let inactive_account_login = 0;
            if (!_.isEmpty(user_configs)) {
                auto_gen_cname_enabled = _.get(user_configs, 'auto_gen_cname', 0);
                subusers_reports_enabled = _.get(user_configs, 'subusers_reports', 0);
                fullmsg_enabled = _.get(user_configs, 'full_message', 0);
                inactive_account_login = _.get(user_configs, 'inactive_account_login', 0);
            }
            _.set(userObj, 'autogen_cname_yn', auto_gen_cname_enabled);
            _.set(userObj, 'subusers_reports_yn', subusers_reports_enabled);
            _.set(userObj, 'full_message_yn', fullmsg_enabled);
            _.set(userObj, 'inactive_login', inactive_account_login);

            return userObj;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not get account info. Please try again', { code });
            // const e = fastify.httpErrors.createError(500, err);

            return e;
        }
    });

    fastify.post('/anew', { preValidation: [], schema: anewSchema }, async (req, reply) => {
        let isParentAccountDebited = false;
        let isSubAccountCredited = false;
        let amount = 0;
        let parent_cliId = null;
        let subaccount_cliId = null;
        try {
            let newCliId = '';
            let ui_pass = '';
            let api_pass = '';
            let smpp_pass = '';
            let vl_shortner = 0;
            let ui_pass_cust = '';
            let api_pass_cust = '';
            let smpp_pass_cust = '';
            const servicesValuesArr = [];
            const tgValuesArr = [];
            const assignedGroupsValuesArr = [];
            const intlRatesValuesArr = [];
            let considerdefaultlength_as_domestic = 1;

            const { user_type, firstname, company, username, email, mobile, offset, country_code_iso3, allocated_tgroup_ids, assigned_tgroup_id, smpp_charset, address, twofa_yn, smsrate, dltrate, newline_chars, encrypt_mobile_yn, encrypt_message_yn, assigned_groups, services, billing_currency, intl_rates,
                full_message, camp_name_auto_gen, inactive_login } = req.body;
            let { wallet, lastname, is_16bit_udh, ip_validation, ip_list, subusers_reports, zone_name, expiry_date } = req.body;

            const { user_type: loggedin_user_type, cli_id: loggedin_cli_id, su_id, bill_type, sessionid, user, billing_currency: logged_in_user_billing_currency } = req.user;
            const { TYPE_USER, TYPE_ADMIN, MAX_ADMINS_UNDER_SA, MAX_USERS_UNDER_ADMIN } = process.env;

            parent_cliId = loggedin_cli_id;

            /* check if the user_type is a valid type for the logged in user (logged in admin cannot create another admin) */
            if (+loggedin_user_type === +TYPE_USER) {
                return fastify.httpErrors.forbidden('Forbidden from creating account by user');
            }

            if (+loggedin_user_type === +TYPE_ADMIN) {
                // check what is the user he is trying to create. he can create only user account
                if (+user_type != +TYPE_USER) {
                    const resp = {
                        statusCode: fastify.CONSTANTS.INVALID_USER_TYPE_IN_ACCOUNT_CREATION,
                        message: `Invalid user type. You cant create user type ${user_type}`,
                    };
                    reply.code(200);
                    return reply.send(resp);
                }
            }

            /* check if username is unique */
            const [result, r1] = await Promise.all([fastify.isUsernameFound(username), fastify.findUserById(loggedin_cli_id)]);

            const userObj = _.get(r1, '0', {});

            if (_.isEmpty(userObj)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            }

            const { counts } = result[0];

            req.log.debug('/anew matching username count => ', counts);
            const isUnique = (counts === 0);

            if (!isUnique) {
                const resp = {
                    statusCode: fastify.CONSTANTS.USERNAME_NOT_UNIQUE_CODE,
                    message: 'Username is not unique',
                    status: 'failed',
                };
                reply.code(200);
                return reply.send(resp);
            }

            const subServices = _.map(services, (o) => o.sub_service);
            // check if the req has intl rates if intl service is enabled for this user
            if (_.includes(subServices, 'international')) {
                if (intl_rates.length === 0) {
                    return fastify.httpErrors.badRequest('intl_rates value is missing in req payload');
                }
                // disabling since it is intl enabled account
                considerdefaultlength_as_domestic = 0;
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

            /* compose cli_id */
            // finding a unique cli_id - to avoid duplicate cli_id issue
            let subusersList;
            let existingCliIds = [];
            [newCliId, subusersList] = await Promise.all([fastify.generateClientId(loggedin_cli_id, user_type), fastify.getAllUsersForId(loggedin_cli_id)]);
            if(!_.isUndefined(subusersList) && !_.isNull(subusersList) && _.size(subusersList) > 0){
                _.forEach(subusersList, (acc)=>{
                    existingCliIds.push(acc.cli_id);
                });

                while (_.indexOf(existingCliIds, +newCliId) > -1){
                    newCliId = await fastify.generateClientId(loggedin_cli_id, user_type);
                }
            }

            req.log.debug('/anew new user id to be created => ', newCliId);

            if (_.includes(subServices, 'cm')) {
                // generate new password
                ui_pass_cust = fastify.nanoiduipass();
                console.log('new password for the user => ', newCliId, ' is ', ui_pass_cust);
                // hash the password
                ui_pass = fastify.hash(ui_pass_cust);
                req.log.debug('hashed password => ', ui_pass);
                // set vl shortner to 1
                vl_shortner = 1;
                const a0 = await fastify.redis.GEN.hset('cli:gui:pass', newCliId, ui_pass_cust);
            }

            if (_.includes(subServices, 'smpp')) {
                const smppdata = await fastify.smpppass(newCliId);
                smpp_pass = _.get(smppdata.passwords, 0).dbinsert_password;
                smpp_pass_cust = _.get(smppdata.passwords, 0).customer_password;
                const a2 = await fastify.redis.GEN.hset('cli:smpp:pass', newCliId, smpp_pass_cust);
            }

            if (_.includes(subServices, 'api')) {
                const apidata = await fastify.apipass(newCliId);
                api_pass = _.get(apidata.passwords, 0).dbinsert_password;
                api_pass_cust = _.get(apidata.passwords, 0).customer_password;
                const a1 = await fastify.redis.GEN.hset('cli:api:pass', newCliId, api_pass_cust);
            }

            /* prepare values for user_service_map table */
            if (!_.isEmpty(services)) {
                _.forEach(services, (obj) => {
                    const service = _.get(obj, 'service');
                    const sub_service = _.get(obj, 'sub_service');
                    servicesValuesArr.push([newCliId, service, sub_service, loggedin_cli_id]);
                });
            }

            /* prepare values for group_user_mapping table */
            if (!_.isEmpty(assigned_groups)) {
                _.forEach(assigned_groups, (val) => {
                    assignedGroupsValuesArr.push([fastify.nanoid(), newCliId, val, loggedin_cli_id]);
                });
            }

            /* prepare values for users_templategroup_ids table */
            if (!_.isEmpty(allocated_tgroup_ids) && +user_type != +TYPE_USER) {
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
            if (intl_rates.length > 0) {
                for await (const obj of intl_rates) {
                    const { country, smsrate: rate } = obj;

                    const billtobaseRate = await fastify.getConversionRateForUser(loggedin_cli_id, process.env.INTL_BASE_CURRENCY);

                    const converted_rate = _.ceil(billtobaseRate * rate, 12);
                    intlRatesValuesArr.push([newCliId, country, converted_rate]);
                }
            }

            if(_.isUndefined(lastname) || _.isNull(lastname) || _.isEmpty(_.trim(lastname))){
                lastname = null;
            }

            if(+user_type === +TYPE_USER){
                subusers_reports = 0;
            }

            const acc_type = _.get(userObj, 'acc_type');

            let loggedin_bal_before = 0;
            let loggedin_bal_after = 0;
            let user_bal_after = 0;
            const user_bal_before = 0;

            /*  create wallet info */
            if (+bill_type === 1) { // its wallet account
                // trim to max 6 decimal places
                wallet = _.floor(wallet, 6);
                amount = wallet;
                if (+wallet > 0) {
                    // check if loggedin user has enough bal before allocating to this cli_id
                    loggedin_bal_after = _.toNumber(await fastify.redis.WALLET.hincrbyfloat('wallet:amount', loggedin_cli_id, -wallet));
                    loggedin_bal_before = +loggedin_bal_after + +wallet;
                    isParentAccountDebited = true;
                    req.log.debug('/anew new bal after deducting the amount from logged_in_user [after deduction bal, loggedinuser, amount deducted] => ', loggedin_bal_after, loggedin_cli_id, wallet);

                    if (+loggedin_bal_after < 0) {
                        req.log.debug('/anew *** not enough bal available in loggedinuser\'s wallet for the allocation, HENCE returning back the deducted amount to his wallet [after deduction, amount deducted, loggedinuser]  => ', loggedin_bal_after, wallet, loggedin_cli_id);

                        // TODO: ***CRITICAL*** logic to be fixed. Wallet amount loss if there is an error here
                        let loggedin_newbal = _.toNumber(await fastify.redis.WALLET.hincrbyfloat('wallet:amount', loggedin_cli_id, wallet));

                        // trim to max 6 decimal places
                        loggedin_newbal = _.floor(loggedin_newbal, 6);

                        req.log.debug('/anew SUCCESSFULLY returned back the bal to loggedin user\'s wallet, new bal is  => ', loggedin_newbal, loggedin_cli_id);
                        isParentAccountDebited = false;
                        // send the error response
                        const resp = {
                            statusCode: fastify.CONSTANTS.NOT_ENOUGH_WALLET_BAL,
                            message: `You do not have enough wallet balance to make allocation. Your current balance is ${loggedin_newbal}`,
                        };
                        reply.code(200);
                        return reply.send(resp);
                    }
                }
            }

            if (_.includes(subServices, 'cm') || _.includes(subServices, 'api')){
                if(_.isUndefined(is_16bit_udh) || _.isNull(is_16bit_udh)){
                    is_16bit_udh = 0;
                }
            }else{
                is_16bit_udh = 0;
            }

            if(_.isUndefined(ip_validation) || _.isNull(ip_validation)){
                ip_validation = 0;
                ip_list = null;
            }else if(+ip_validation == 1){
                if(_.isUndefined(ip_list) || _.isNull(ip_list) || _.isEmpty(_.trim(ip_list))){
                    return fastify.httpErrors.badRequest('IP address is required');
                }
                ip_list = _.trim(ip_list);
            }else {
                ip_validation = 0;
                ip_list = null;
            }

            subaccount_cliId = newCliId;

            const obj = {
                newCliId,
                su_id,
                loggedin_cli_id,
                bill_type,
                user_type,
                firstname,
                'lastname':lastname,
                company,
                username,
                ui_pass,
                api_pass,
                smpp_pass,
                email,
                mobile,
                zone_name,
                offset,
                country_code_iso3,
                allocated_tgroup_ids,
                assigned_tgroup_id,
                smpp_charset,
                address,
                twofa_yn,
                wallet,
                smsrate,
                dltrate,
                newline_chars,
                encrypt_mobile_yn,
                encrypt_message_yn,
                assigned_groups,
                services,
                vl_shortner,
                servicesValuesArr,
                tgValuesArr,
                assignedGroupsValuesArr,
                walletTransValuesArr,
                billing_currency: logged_in_user_billing_currency,
                intlRatesValuesArr,
                platform_cluster: userObj.platform_cluster,
                considerdefaultlength_as_domestic,
                is_16bit_udh,
                ip_validation,
                ip_list,
                full_message,
                camp_name_auto_gen,
                subusers_reports,
                acc_type,
                expiry_date, inactive_login
            };

            const resultAcc = await fastify.createAccount(obj);

            /** ********************************************* Send Email  ******************************************** */
            if (_.includes(subServices, 'api') || _.includes(subServices, 'smpp') || _.includes(subServices, 'cm')) {
                const from_fname = _.get(userObj, 'firstname');
                const from_lname = _.get(userObj, 'lastname');
                const from_email = _.get(userObj, 'email');
                const subject = Mustache.render(process.env.CREATE_ACCOUNT_MAIL_SUBJECT, { to_fname: firstname, to_lname: lastname });

                const emailTemplate = fs.readFileSync('./templates/create_account_email_template.html', 'utf8');

                const dataObj = await fastify.constructEmailData(from_fname, from_lname, firstname, lastname, from_email, email, from_email, subject, ui_pass_cust, api_pass_cust, smpp_pass_cust, subServices);

                try {
                    const respMail = await fastify.sendMail(emailTemplate, dataObj);
                    console.log(respMail);
                } catch (e) {
                    console.error('/anew ERROR while sending mail ***IGNORING*** ', e);
                }
            }

            /** ********************************************* End of Send Email  ******************************************** */

            // do wallet
            if (+bill_type === 1) { // its wallet account
                try {
                    // trim to max 4 decimal places
                    wallet = _.floor(wallet, 6);
                    user_bal_after = await fastify.redis.WALLET.hincrbyfloat('wallet:amount', newCliId, wallet);
                    console.log('/anew New balance after adding amount [cli_id, amount]', [newCliId, wallet], user_bal_after);
                    isSubAccountCredited = true;
                } catch (e) {
                    /* error at this stage denotes that account is successfully created and the wallet amount has been deducted from logged in user but not allocated to new user */
                    // TODO ***CRITICAL*** Need to log this data in table for manual processing
                    console.error('/anew ***LEAKAGE*** Wallet amount is not allocated to this new user. but account creation and logged in user wallet deduction is successful ');
                }

                // persist to wallet transaction
                try {
                    walletTransValuesArr = [];
                    walletTransValuesArr.push([fastify.nanoid(), newCliId, username, 'add', wallet, 'New Account', '', sessionid, loggedin_cli_id, user, user_bal_after, user_bal_before, loggedin_bal_before, loggedin_bal_after]);
                    const result = await fastify.persistWalletTransaction(walletTransValuesArr);
                    console.log('/anew SUCCESSFULLY persisted the wallet transaction... ', JSON.stringify(walletTransValuesArr));
                } catch (e) {
                    // TODO: *** Data Leakage *** this needs to be handled
                    console.error('/anew *** Error Persisting Wallet transaction ***', JSON.stringify(walletTransValuesArr));
                }
            }
            const resp = { statusCode: 200, message: 'Account has been created successfully' };
            reply.code(200);
            return reply.send(resp);
        } catch
        (err) {
            try{
                if(isParentAccountDebited && !isSubAccountCredited){
                    const parent_newbal = _.toNumber(await fastify.redis.WALLET.hincrbyfloat('wallet:amount', parent_cliId, amount));
                    console.log('/anew New balance after refunding amount [cli_id, amount, new bal]', [parent_cliId, amount, parent_newbal]);
                }
            }catch(err1){
                console.error(`/anew ***LEAKAGE*** Wallet amount refund is not successfull to logged in user. cli_id ${parent_cliId} amount to be refund ${amount}`);
                console.error(`error =>${err1}`);
            }
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not create account. Please try again', { err });
            // const e = fastify.httpErrors.createError(500, err);

            return e;
        }
    });

    fastify.get('/countries', { preValidation: [], schema: countriesSchema }, async (req, reply) => {
        try {
            const result = await fastify.getCountries();
            console.log('/countries total => ', result.length);

            return result;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not get countries. Please try again', { code });
            // const e = fastify.httpErrors.createError(500, err);

            return e;
        }
    });

    fastify.get('/tgroups', { preValidation: [], schema: tgroupsSchema }, async (req, reply) => {
        try {
            const { cli_id } = req.user;
            const result = await fastify.getTemplateGroups(cli_id);
            console.log(`/tgroups total for cli_id ${cli_id} =>  ${result.length}`);

            return result;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not get template groups. Please try again', { code });
            // const e = fastify.httpErrors.createError(500, err);

            return e;
        }
    });

    fastify.get('/assignedSubServices', {
        preValidation: [],
        schema: assignedSubServicesSchema,
    }, async (req, reply) => {
        try {
            const { cli_id } = req.user;
            const result = await fastify.getAssignedSubServices(cli_id);
            console.log(`/assignedSubServices total for cli_id ${cli_id} =>  ${result.length}`);

            return result;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not get assigned sub services. Please try again', { code });
            // const e = fastify.httpErrors.createError(500, err);

            return e;
        }
    });

    fastify.post('/aupdatePI', { preValidation: [], schema: aupdatePISchema }, async (req, reply) => {
        try {
            const { cli_id: loggedin_cli_id } = req.user;
            const { cli_id, firstname, company, address } = req.body;
            let { lastname } = req.body;

            if(_.isUndefined(lastname) || _.isNull(lastname) || _.isEmpty(_.trim(lastname))){
                lastname = null;
            }

            const result = await fastify.updateAccountInfo(cli_id, firstname, lastname, company, address);

            // TODO: need to check the updated status and reply accordingly { affectedRows: 1, insertId: 0, warningStatus: 0 }

            console.log('/aupdatePI resp from db => ', result);
            const resp = { statusCode: 200, message: 'Account Info has been modified successfully' };

            return resp;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not update account info details. Please try again', { code });

            return e;
        }
    });

    fastify.post('/aupdateEncryption', { preValidation: [], schema: aupdateEncryptionSchema }, async (req, reply) => {
        try {
            const { cli_id: loggedin_cli_id } = req.user;
            const { cli_id, encrypt_mobile_yn, encrypt_message_yn } = req.body;

            const result = await fastify.updateAccountEncryption(cli_id, loggedin_cli_id, encrypt_mobile_yn, encrypt_message_yn);

            // TODO: need to check the updated status and reply accordingly { affectedRows: 1, insertId: 0, warningStatus: 0 }

            console.log('/aupdatePI resp from db => ', result);
            const resp = { statusCode: 200, message: 'Account Info has been modified successfully' };

            return resp;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not update account info details. Please try again', { code });

            return e;
        }
    });

    fastify.post('/aupdateTGroups', { preValidation: [], schema: aupdateTGroups }, async (req, reply) => {
        try {
            const { cli_id: loggedin_cli_id } = req.user;
            const { cli_id, allocated_tgroup_ids, assigned_tgroup_id } = req.body;
            const tgValuesArr = [];

            const r1 = await fastify.findUserById(cli_id);

            const userObj = _.get(r1, '0', {});
            if (_.isEmpty(userObj)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            }

            const userType = _.get(userObj, 'user_type');

            /* prepare values for users_templategroup_ids table */
            if (!_.isEmpty(allocated_tgroup_ids)) {
                _.forEach(allocated_tgroup_ids, (val) => {
                    tgValuesArr.push([cli_id, val, loggedin_cli_id]);
                });
            }

            const result = await fastify.updateAccountTGroups(cli_id, assigned_tgroup_id, tgValuesArr, userType);

            // TODO: need to check the updated status and reply accordingly { affectedRows: 1, insertId: 0, warningStatus: 0 }

            console.log('/aupdateTGroups resp from db => ', result);
            const resp = { statusCode: 200, message: 'Account template groups has been modified successfully' };

            return resp;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not update account info details. Please try again', { code });

            return e;
        }
    });

    fastify.post('/aupdate2FA', { preValidation: [], schema: aupdate2FASchema }, async (req, reply) => {
        try {
            const { cli_id: loggedin_cli_id } = req.user;
            const { cli_id, twofa_yn } = req.body;

            const result = await fastify.updateAccount2FA(cli_id, twofa_yn);

            // TODO: need to check the updated status and reply accordingly { affectedRows: 1, insertId: 0, warningStatus: 0 }

            console.log('/aupdate2FA resp from db => ', result);
            const resp = { statusCode: 200, message: 'Account 2FA has been modified successfully' };

            return resp;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not update 2fa. Please try again', { code });

            return e;
        }
    });

    fastify.post('/aupdatepassword', { preValidation: [], schema: aupdatepasswordSchema }, async (req, reply) => {
        try {
            const { cli_id } = req.user;
            const { newpass } = req.body;

            // hash the password
            const hashedPass = fastify.hash(newpass);
            console.log('/aupdatepassword hashed password => ', hashedPass);

            const result = await fastify.updateAccountPassword(cli_id, hashedPass);

            // TODO: need to check the updated status and reply accordingly { affectedRows: 1, insertId: 0, warningStatus: 0 }

            console.log('/aupdatepassword resp from db => ', result);
            const resp = { statusCode: 200, message: 'Account password has been updated successfully' };

            return resp;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not update password. Please try again', { code });

            return e;
        }
    });

    fastify.post('/aupdateMS', { preValidation: [], schema: aupdateMSSchema }, async (req, reply) => {
        try {
            const { cli_id: loggedin_cli_id } = req.user;
            const { cli_id, offset, country_code_iso3, newline_chars, inactive_login } = req.body;
            let { is_16bit_udh, ip_validation, ip_list, full_message, camp_name_auto_gen, subusers_reports, zone_name, expiry_date } = req.body;

            if(_.isUndefined(is_16bit_udh) || _.isNull(is_16bit_udh)){
                is_16bit_udh = 0;
            }

            if(_.isUndefined(subusers_reports) || _.isNull(subusers_reports)){
                subusers_reports = 0;
            }

            if(_.isUndefined(ip_validation) || _.isNull(ip_validation)){
                ip_validation = 0;
                ip_list = null;
            }else if(+ip_validation == 1){
                if(_.isUndefined(ip_list) || _.isNull(ip_list) || _.isEmpty(_.trim(ip_list))){
                    return fastify.httpErrors.badRequest('IP address is required');
                }
                ip_list = _.trim(ip_list);
            }else {
                ip_validation = 0;
                ip_list = null;
            }

            const result1 = await fastify.findUserById(cli_id);
            const userObj = _.get(result1, '0', {});

            if (_.isEmpty(userObj)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            }

            const user_type = _.get(userObj, 'user_type');
            if(+user_type === 2){
                subusers_reports = 0;
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

            const req_obj = {
                cli_id, zone_name, offset, country_code_iso3, newline_chars, loggedin_cli_id,
                is_16bit_udh, ip_validation, full_message, camp_name_auto_gen, ip_list, subusers_reports,
                expiry_date, inactive_login
            }
            const result = await fastify.updateAccountMS(req_obj);
            //const result = await fastify.updateAccountMS(cli_id, zone_name, offset, country_code_iso3, newline_chars, loggedin_cli_id);

            // TODO: need to check the updated status and reply accordingly { affectedRows: 1, insertId: 0, warningStatus: 0 }

            console.log('/aupdateMS resp from db => ', result);
            const resp = { statusCode: 200, message: 'Account messaging settings has been modified successfully' };

            return resp;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not update messaging settings. Please try again', { code });

            return e;
        }
    });

    fastify.post('/aupdateServices', { preValidation: [], schema: aupdateServicesSchema }, async (req, reply) => {
        try {
            const { cli_id: loggedin_cli_id } = req.user;
            const { cli_id, services, smpp_charset, row_rate } = req.body;
            const servicesValuesArr = [];
            let smpppass = null;
            let apipass = null;
            let cmpass = null;
            let ui_pass_cust = '';
            let api_pass_cust = '';
            let smpp_pass_cust = '';
            let converted_row_rate = 0;
            let considerdefaultlength_as_domestic = 1;

            // get existing allocated services for this user and user info

            const [resultExistingServices, rLoggedin, rEdited] = await Promise.all([fastify.getAssignedSubServices(cli_id), fastify.findUserById(loggedin_cli_id), fastify.findUserById(cli_id)]);

            const userObjLoggedin = _.get(rLoggedin, '0', {});
            const userObjEdited = _.get(rEdited, '0', {});

            if (_.isEmpty(userObjLoggedin) || _.isEmpty(userObjEdited)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            }

            const subServices = _.map(resultExistingServices, (o) => o.sub_service);
            console.log('/aupdateServices existing services from db => ', subServices);

            const passwordRequiredList = [];
            /* prepare values for user_service_map table */
            if (!_.isEmpty(services)) {
                _.forEach(services, (obj) => {
                    const service = _.get(obj, 'service');
                    const sub_service = _.get(obj, 'sub_service');
                    // check if sub service is already available in the existing list, if not, requires to generate password if its api/cm/smpp
                    if (!_.includes(subServices, sub_service)) {
                        if (_.eq(sub_service, 'cm')) passwordRequiredList.push(sub_service);
                        if (_.eq(sub_service, 'api')) passwordRequiredList.push(sub_service);
                        if (_.eq(sub_service, 'smpp')) passwordRequiredList.push(sub_service);
                    }
                    servicesValuesArr.push([cli_id, service, sub_service, loggedin_cli_id, new Date(), loggedin_cli_id]);
                    if (_.eq(sub_service, 'international')){
                        considerdefaultlength_as_domestic = 0;
                    }
                });
            }

            console.log('/aupdateServices password to be generated for the new services => ', passwordRequiredList);

            // generate passwords for services if needed
            if (_.includes(passwordRequiredList, 'smpp')) {
                const smppdata = await fastify.smpppass(cli_id);
                smpppass = _.get(smppdata.passwords, 0).dbinsert_password;
                smpp_pass_cust = _.get(smppdata.passwords, 0).customer_password;
            }
            if (_.includes(passwordRequiredList, 'api')) {
                console.log('calling api');
                const apidata = await fastify.apipass(cli_id);
                console.log(apidata);
                apipass = _.get(apidata.passwords, 0).dbinsert_password;
                api_pass_cust = _.get(apidata.passwords, 0).customer_password;
            }
            if (_.includes(passwordRequiredList, 'cm')) {
                // generate new password
                ui_pass_cust = fastify.nanoiduipass();
                console.log('/aupdateServices new password for the user => ', cli_id, ' is ', ui_pass_cust);
                // hash the password
                cmpass = fastify.hash(ui_pass_cust);
                console.log('/aupdateServices hashed password => ', cmpass);
            }

            // handle row_rate
            if (row_rate > 0) {
                const billtobaseRate = await fastify.getConversionRateForUser(cli_id, process.env.INTL_BASE_CURRENCY);
                converted_row_rate = _.ceil(billtobaseRate * row_rate, 12);
            }

            const result = await fastify.updateAccountServices(cli_id, servicesValuesArr, smpp_charset, loggedin_cli_id, smpppass, apipass, cmpass, converted_row_rate, considerdefaultlength_as_domestic);

            // send pass thru email
            if (_.includes(passwordRequiredList, 'smpp')) {
                try {
                    const a2 = await fastify.redis.GEN.hset('cli:smpp:pass', cli_id, smpp_pass_cust);
                } catch (e) {
                    console.log('/aupdateServices ERROR while sending mail for smpp ***IGNORING*** ', e);
                }
            }
            if (_.includes(passwordRequiredList, 'api')) {
                try {
                    const a1 = await fastify.redis.GEN.hset('cli:api:pass', cli_id, api_pass_cust);
                } catch (e) {
                    console.log('/aupdateServices ERROR while sending mail for api ***IGNORING*** ', e);
                }
            }
            if (_.includes(passwordRequiredList, 'cm')) {
                try {
                    const a0 = await fastify.redis.GEN.hset('cli:gui:pass', cli_id, ui_pass_cust);
                } catch (e) {
                    console.log('/aupdateServices ERROR while sending mail for ui ***IGNORING*** ', e);
                }
            }

            /** ********************************************* Send Email  ******************************************** */
            if (_.includes(passwordRequiredList, 'api') || _.includes(passwordRequiredList, 'smpp') || _.includes(passwordRequiredList, 'cm')) {
                console.log('sending mail...');
                const from_fname = _.get(userObjLoggedin, 'firstname');
                const from_lname = _.get(userObjLoggedin, 'lastname');
                const from_email = _.get(userObjLoggedin, 'email');
                const to_fname = _.get(userObjEdited, 'firstname');
                const to_lname = _.get(userObjEdited, 'lastname');
                const to_email = _.get(userObjEdited, 'email');
                const subject = Mustache.render(process.env.CREATE_ACCOUNT_MAIL_SUBJECT, { to_fname, to_lname });

                const emailTemplate = fs.readFileSync('./templates/add_service_email_template.html', 'utf8');

                const dataObj = await fastify.constructEmailData(from_fname, from_lname, to_fname, to_lname, from_email, to_email, from_email, subject, ui_pass_cust, api_pass_cust, smpp_pass_cust, passwordRequiredList);

                try {
                    const respMail = await fastify.sendMail(emailTemplate, dataObj);
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

    fastify.post('/aupdateGroups', { preValidation: [], schema: aupdateGroupsSchema }, async (req, reply) => {
        try {
            const { cli_id: loggedin_cli_id } = req.user;
            const { cli_id, assigned_groups } = req.body;
            const assignedGroupsValuesArr = [];

            /* prepare values for group_user_mapping table */
            if (!_.isEmpty(assigned_groups)) {
                _.forEach(assigned_groups, (val) => {
                    assignedGroupsValuesArr.push([fastify.nanoid(), cli_id, val, loggedin_cli_id, new Date()]);
                });
            }

            const result = await fastify.updateAccountGroups(cli_id, assignedGroupsValuesArr);

            // TODO: need to check the updated status and reply accordingly { affectedRows: 1, insertId: 0, warningStatus: 0 }

            console.log('/aupdateGroups resp from db => ', result);
            const resp = { statusCode: 200, message: 'Account shared groups has been modified successfully' };

            return resp;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not update account shared groups. Please try again', { code });

            return e;
        }
    });

    fastify.post('/aupdateWRates', { preValidation: [], schema: aupdateWRateSchema }, async (req, reply) => {
        try {
            const { cli_id: loggedin_cli_id } = req.user;
            const { cli_id, smsrate, dltrate, smsrate_old, dltrate_old } = req.body;
            const req_id = req.id;

            console.log('/aupdateWRates incoming params => ', [cli_id, smsrate, dltrate, smsrate_old, dltrate_old]);

            const result = await fastify.findUserById(cli_id);
            const userObj = _.get(result, '0', {});

            if (_.isEmpty(userObj)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            }
            const user = _.get(userObj, 'user');
            const billing_currency = _.get(userObj, 'billing_currency');

            const result1 = await fastify.updateAccountRates(cli_id, smsrate, dltrate, smsrate_old, dltrate_old, req_id, user, loggedin_cli_id, billing_currency);
            console.log('/aupdateWRates resp from db => ', result1);

            const resp = { statusCode: 200, message: 'Account rates has been modified successfully' };

            return resp;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not update account rates. Please try again', { code });

            return e;
        }
    });

    fastify.post('/aupdateWAmount', { preHandler: [], schema: aupdateWAmountSchema }, async (req, reply) => {
        try {
            const { cli_id: loggedin_cli_id, bill_type, sessionid, user } = req.user;
            const { cli_id, amount, action, comments } = req.body;
            const walletTransValuesArr = [];
            let loggedin_bal_before = 0;
            let loggedin_bal_after = 0;
            let user_bal_after = 0;
            let user_bal_before = 0;

            console.log('/aupdateWAmount incoming params => ', [cli_id, amount, action, comments]);

            // dont allow if loggedin user and the target users are same
            if (loggedin_cli_id == cli_id) {
                return fastify.httpErrors.badRequest('It does not appear to be a authorized transaction');
            }

            if (+bill_type != 1) { // not a wallet account
                return fastify.httpErrors.badRequest('It does not appear to be a valid user');
            }

            /* pre validation */
            // error if amount is <= 0
            if (+amount <= 0) {
                return fastify.httpErrors.badRequest('Invalid wallet amount');
            }

            // find the username of cli_id
            const result1 = await fastify.findUserById(cli_id);
            const userObj = _.get(result1, '0', {});
            const username = _.get(userObj, 'user', '');

            if (_.eq(action, 'add')) {
                // check if loggedin user has enough bal before allocating to this cli_id
                loggedin_bal_after = _.toNumber(await fastify.redis.WALLET.hincrbyfloat('wallet:amount', loggedin_cli_id, -amount));
                loggedin_bal_before = +loggedin_bal_after + +amount;

                console.log('/aupdateWAmount [add] new bal after deducting the amount from logged_in_user [after deduction bal, loggedinuser, amount deducted] => ', loggedin_bal_after, loggedin_cli_id, amount);

                if (loggedin_bal_after < 0) {
                    console.log('/aupdateWAmount [add] *** not enough bal available in logedinuser\'s wallet for the allocation, HENCE returning back the deducted amount to his wallet  [after deduction, amount deducted, loggedinuser]  => ', loggedin_bal_after, amount, loggedin_cli_id);

                    // TODO: ***CRITICAL*** logic to be fixed. Wallet amount loss if there is an error here
                    let loggedin_newbal = await fastify.redis.WALLET.hincrbyfloat('wallet:amount', loggedin_cli_id, amount);

                    // trim to max 4 decimal places
                    loggedin_newbal = _.floor(loggedin_newbal, 6);

                    console.log('/aupdateWAmount [add] SUCCESSFULLY returned back the bal to loggedin user\'s wallet, new bal is  => ', loggedin_newbal, loggedin_cli_id);

                    // send the error response
                    const resp = {
                        statusCode: fastify.CONSTANTS.NOT_ENOUGH_WALLET_BAL,
                        message: `You do not have enough wallet balance to make allocation. Your current balance is ${loggedin_newbal}`,
                    };
                    reply.code(200);
                    return reply.send(resp);
                }

                user_bal_after = _.toNumber(await fastify.redis.WALLET.hincrbyfloat('wallet:amount', cli_id, amount));
                user_bal_before = user_bal_after - +amount;

                // id, cli_id, username, action, amount, source, description, sessionid, done_by, done_by_username,new_bal,old_bal,loggedin_bal_before, loggedin_bal_after
                walletTransValuesArr.push([fastify.nanoid(), cli_id, username, 'add', amount, 'Edit Account /aupdateWAmount', comments, sessionid, loggedin_cli_id, user, user_bal_after, user_bal_before, loggedin_bal_before, loggedin_bal_after]);

                console.log('/aupdateWAmount [deduct] SUCCESSFULLY added amount to the cli_id and the new bal is ', [amount, cli_id, user_bal_after]);
            }

            if (_.eq(action, 'deduct')) {
                // check if cli_id has enough bal for the deduction
                user_bal_after = _.toNumber(await fastify.redis.WALLET.hincrbyfloat('wallet:amount', cli_id, -amount));
                user_bal_before = user_bal_after + amount;

                console.log('/aupdateWAmount [deduct] new bal after deducting the amount from cli_id [after deduction bal, cli_id, amount deducted] => ', user_bal_after, cli_id, amount);

                if (user_bal_after < 0) {
                    console.log('/aupdateWAmount [deduct] *** not enough bal available in user\'s wallet for the deduction, HENCE returning back the deducted amount to his [after deduction, amount deducted, cli_id]  => ', user_bal_after, amount, cli_id);

                    // TODO: ***CRITICAL*** logic to be fixed. Wallet amount loss if there is an error here
                    let newbal = _.toNumber(await fastify.redis.WALLET.hincrbyfloat('wallet:amount', cli_id, amount));

                    // trim to max 4 decimal places
                    newbal = _.floor(newbal, 6);

                    console.log('/aupdateWAmount [deduct] SUCCESSFULLY returned back the bal to user\'s wallet, new bal is  => ', newbal, cli_id);

                    // send the error response
                    const resp = {
                        statusCode: fastify.CONSTANTS.NOT_ENOUGH_WALLET_BAL,
                        message: `User does not have enough wallet balance to make deduction. User's current balance is ${newbal}`,
                    };
                    reply.code(200);
                    return reply.send(resp);
                }

                loggedin_bal_after = _.toNumber(await fastify.redis.WALLET.hincrbyfloat('wallet:amount', loggedin_cli_id, amount));
                loggedin_bal_before = +loggedin_bal_after - +amount;

                // id, cli_id, username, action, amount, source, description, sessionid, done_by, done_by_username,new_bal,old_bal,loggedin_bal_before, loggedin_bal_after
                walletTransValuesArr.push([fastify.nanoid(), cli_id, username, 'deduct', amount, 'Edit Account /aupdateWAmount', comments, sessionid, loggedin_cli_id, user, user_bal_after, user_bal_before, loggedin_bal_before, loggedin_bal_after]);

                console.log('/aupdateWAmount [deduct] SUCCESSFULLY added amount to the loggedin user and the new bal is ', [amount, loggedin_cli_id, loggedin_bal_after]);
            }

            // persist to wallet transaction
            try {
                const result = await fastify.persistWalletTransaction(walletTransValuesArr);
                console.log('/aupdateWAmount SUCCESSFULLY persisted the wallet transaction... ', JSON.stringify(walletTransValuesArr));
            } catch (e) {
                // TODO: *** Data Leakage *** this needs to be handled
                console.error('/aupdateWAmount *** Error Persisting Wallet transaction ***', JSON.stringify(walletTransValuesArr));
            }
            // trim to max 4 decimal places
            user_bal_after = _.floor(user_bal_after, 6);

            const resp = {
                statusCode: 200,
                message: 'Wallet amount has been updated successfully',
                wallet_bal: user_bal_after,
            };

            return resp;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not update wallet amount. Please try again', { code });

            return e;
        }
    });

    fastify.post('/abal', { preValidation: [], schema: abalSchema }, async (req, reply) => {
        try {
            const { cli_id: loggedin_cli_id, bill_type } = req.user;
            const { cli_id } = req.body;

            if (+bill_type != 1) { // not a wallet account
                return fastify.httpErrors.badRequest('It does not appear to be a valid user');
            }

            const [amount, result] = await Promise.all([fastify.walletBalance(cli_id), fastify.findUserById(cli_id)]);
            // const amount = await fastify.redis.WALLET.hget('wallet:amount', cli_id);

            const userObj = _.get(result, '0', {});

            if (_.isEmpty(userObj)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            }

            console.log('/abal resp from redis for user => ', cli_id, amount);

            const smsrate = _.get(userObj, 'base_sms_rate', 0);
            const dltrate = _.get(userObj, 'base_add_fixed_rate', 0);
            // get the total sms that can be sent for this wallet
            const smsleft = await fastify.getSMSLeft(amount, smsrate, dltrate);

            const resp = { wallet_bal: _.floor(amount, 6), sms_left: smsleft };

            return resp;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not update account wallet rates. Please try again', { code });

            return e;
        }
    });
    fastify.post('/ausersmw', { preValidation: [], schema: ausersmwSchema }, async (req, reply) => {
        try {
            const { cli_id: loggedin_cli_id, bill_type } = req.user;
            const { amount, action } = req.body;
            const payload = [];

            if (+bill_type != 1) { // not a wallet account
                return fastify.httpErrors.badRequest('It does not appear to be a valid user');
            }

            // get all the users under loggedin user
            const result = await fastify.getAllUsersForId(loggedin_cli_id);
            console.log('/ausersmw total users under loggedin id => ', loggedin_cli_id, result.length);

            if (result.length === 0) {
                return [];
            }

            const cliArr = _.map(result, 'cli_id');
            console.log('/ausersmw list of users under logged in user', loggedin_cli_id, cliArr);

            const result1 = await fastify.getAllUsersInfoForIds(cliArr);
            console.log('/ausersmw all users info under loggedin id => ', loggedin_cli_id, result1.length);

            const amountArr = await fastify.redis.WALLET.hmget('wallet:amount', cliArr);
            console.log('/ausersmw resp from redis for user => ', amountArr);

            for (let i = 0; i < result.length; i++) {
                const obj = _.get(result, i, {});
                let wallet = _.get(amountArr, i, 0);
                wallet = _.floor(wallet, 6);

                // filter users only if action is deduct
                if (_.eq(action, 'deduct')) {
                    if (+wallet >= amount) {
                        _.set(obj, 'wallet_bal', wallet);
                        payload.push(obj);
                    }
                } else {
                    _.set(obj, 'wallet_bal', wallet);
                    payload.push(obj);
                }
            }

            return payload;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not get users list. Please try again', { code });

            return e;
        }
    });

    fastify.post('/aupdatewamountmulti', { preValidation: [], schema: aupdatewamountmultiSchema }, async (req, reply) => {
        // TODO: /aupdatewamountmulti => logic to be re-visited.
        try {
            const { cli_id: loggedin_cli_id, bill_type, sessionid, user } = req.user;
            const { amount, action, cli_ids, comments } = req.body;
            const walletTransValuesArr = [];
            let loggedin_bal_before = 0;
            let loggedin_bal_after = 0;
            let user_bal_after = 0;
            let user_bal_before = 0;

            if (+bill_type != 1) { // not a wallet account
                return fastify.httpErrors.badRequest('It does not appear to be a valid user');
            }

            /* pre validation */
            // error if amount is <= 0
            if (+amount <= 0) {
                return fastify.httpErrors.badRequest('Invalid wallet amount');
            }

            // find the username of cli_id
            const result1 = await fastify.getAllUsersInfoForIds(cli_ids);
            const grpbyCliid = _.groupBy(result1, 'cli_id');

            // find the total amount to be deducted from loggedin user
            const totalAmount = +amount * cli_ids.length;

            if (_.eq(action, 'add')) {
                // get the cur bal for the logged in user and check if his bal is enough for the entire allocation
                let loggedin_bal = await fastify.redis.WALLET.hget('wallet:amount', loggedin_cli_id);
                loggedin_bal = _.floor(loggedin_bal, 6);

                console.log('/aupdatewamountmulti [add] the current bal for logged in and the total amount from selected users => ', loggedin_bal, totalAmount);

                if (+totalAmount > +loggedin_bal) {
                    // send the error response
                    const resp = {
                        statusCode: fastify.CONSTANTS.NOT_ENOUGH_WALLET_BAL,
                        message: `You do not have enough wallet balance to make allocation. Your current balance is ${loggedin_bal} and your required balance is ${totalAmount} Please update the allocation amount and continue`,
                    };
                    reply.code(200);
                    return reply.send(resp);
                }

                // proceed with allocation
                for await (const cid of cli_ids) {
                    const bal = _.toNumber(await fastify.redis.WALLET.hget('wallet:amount', cid));
                    console.log('/aupdatewamountmulti [add] current bal for user before allocation is  and amount to be added ', [cid, bal, amount]);

                    // add to user
                    user_bal_after = _.toNumber(await fastify.redis.WALLET.hincrbyfloat('wallet:amount', cid, amount));
                    user_bal_before = user_bal_after - +amount;

                    // deduct from loggedin user
                    loggedin_bal_after = _.toNumber(await fastify.redis.WALLET.hincrbyfloat('wallet:amount', loggedin_cli_id, -amount));
                    loggedin_bal_before = +loggedin_bal_after + +amount;

                    console.log('/aupdatewamountmulti [add] current bal for logged in user after deduction is ', [loggedin_cli_id, loggedin_bal_after]);

                    if (+loggedin_bal_after < 0) {
                        // this case should not happen
                        console.log('/aupdatewamountmulti [add] *** WENT TO NEGATIVE BAL *** bal for logged in user after deduction is ', [loggedin_cli_id, loggedin_bal_after]);
                    }

                    const userobj = _.get(grpbyCliid, cid)[0];
                    const username = _.get(userobj, 'user', '');

                    walletTransValuesArr.push([fastify.nanoid(), cid, username, 'add', amount, 'Edit Account /aupdatewamountmulti', comments, sessionid, loggedin_cli_id, user, user_bal_after, user_bal_before, loggedin_bal_before, loggedin_bal_after]);
                }
            }
            const notfitForDeduction = [];
            if (_.eq(action, 'deduct')) {
                const amountArr = await fastify.redis.WALLET.hmget('wallet:amount', cli_ids);
                console.log('/aupdatewamountmulti [deduct] cur bal for all the users selected => ', [cli_ids, amountArr]);
                for (let i = 0; i < amountArr.length; i++) {
                    let bal = _.get(amountArr, i, 0);
                    bal = _.floor(bal, 6);
                    if (+amount > +bal) {
                        notfitForDeduction.push(_.get(cli_ids, i, 0));
                    }
                }
                console.log('/aupdatewamountmulti [deduct] users who are not fit for deduction for the amount => ', [notfitForDeduction, amount]);
                if (notfitForDeduction.length > 0) {
                    // find the username
                    const result = await fastify.getAllUsersInfoForIds(notfitForDeduction);
                    const usernameArr = _.map(result, 'user');

                    // send the error response
                    const resp = {
                        statusCode: fastify.CONSTANTS.NOT_ENOUGH_WALLET_BAL,
                        message: `User's [${usernameArr}] do not have enough wallet balance for a deduction. Please update the deduction amount or remove the users and continue`,
                    };
                    reply.code(200);
                    return reply.send(resp);
                }

                // proceed for deduction
                for await (const cid of cli_ids) {
                    const bal = _.toNumber(await fastify.redis.WALLET.hget('wallet:amount', cid));
                    console.log('/aupdatewamountmulti [deduct] current bal for user before deduction is  and amount to be deducted ', [cid, bal, amount]);

                    // deduct from user
                    user_bal_after = _.toNumber(await fastify.redis.WALLET.hincrbyfloat('wallet:amount', cid, -amount));
                    user_bal_before = user_bal_after + amount;

                    // add to loggedin user
                    loggedin_bal_after = _.toNumber(await fastify.redis.WALLET.hincrbyfloat('wallet:amount', loggedin_cli_id, amount));
                    console.log('/aupdatewamountmulti [deduct] current bal for logged in user after allocation is ', [loggedin_cli_id, loggedin_bal_after]);

                    if (+user_bal_after < 0) {
                        // this case should not happen
                        console.log('/aupdatewamountmulti [deduct] *** WENT TO NEGATIVE BAL *** bal for the user after deduction is ', [cid, user_bal_after]);
                    }

                    const userobj = _.get(grpbyCliid, cid)[0];
                    const username = _.get(userobj, 'user', '');

                    walletTransValuesArr.push([fastify.nanoid(), cid, username, 'deduct', amount, 'Edit Account /aupdatewamountmulti', comments, sessionid, loggedin_cli_id, user, user_bal_after, user_bal_before, loggedin_bal_before, loggedin_bal_after]);
                }
            }

            // persist to wallet transaction
            try {
                const result = await fastify.persistWalletTransaction(walletTransValuesArr);
                console.log('/aupdatewamountmulti SUCCESSFULLY persisted the wallet transaction... ', JSON.stringify(walletTransValuesArr));
            } catch (e) {
                // TODO: *** Data Leakage *** this needs to be handled
                console.error('/aupdatewamountmulti *** Error Persisting Wallet transaction ***', JSON.stringify(walletTransValuesArr));
            }

            const resp = { statusCode: 200, message: 'Wallet amount has been updated successfully' };

            return resp;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not update account wallet rates. Please try again', { code });

            return e;
        }
    });

    fastify.get('/aquicklinks', { preValidation: [], schema: aquicklinksSchema }, async (req, reply) => {
        try {
            const { cli_id } = req.user;

            // get the user configuration
            const result = await fastify.getConfiguredQuickLinks(cli_id);
            console.log('/aquicklinks resp from db => ', result);

            return result;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not get quick links. Please try again', { code });

            return e;
        }
    });

    fastify.get('/aquicklinksettings', { preValidation: [], schema: aquicklinkssettingsSchema }, async (req, reply) => {
        try {
            const { cli_id } = req.user;

            const template = await fastify.getBaseTemplateForQuickLinks();

            console.log('/aquicklinkssettings base template for quick links => ', template);

            // get the user configuration
            const result = await fastify.getConfiguredQuickLinks(cli_id);
            for (const o of result) {
                const ql = _.get(o, 'quicklink');

                if (_.eq(ql, 'cq')) _.set(template, 'quicklinks.campaign.cq', true);
                if (_.eq(ql, 'cotm')) _.set(template, 'quicklinks.campaign.cotm', true);
                if (_.eq(ql, 'cmtm')) _.set(template, 'quicklinks.campaign.cmtm', true);
                if (_.eq(ql, 'cg')) _.set(template, 'quicklinks.campaign.cg', true);
                if (_.eq(ql, 'ct')) _.set(template, 'quicklinks.campaign.ct', true);
                if (_.eq(ql, 'account')) _.set(template, 'quicklinks.createnew.account', true);
                if (_.eq(ql, 'group')) _.set(template, 'quicklinks.createnew.group', true);
                if (_.eq(ql, 'template')) _.set(template, 'quicklinks.createnew.template', true);
                if (_.eq(ql, 'mysettings')) _.set(template, 'quicklinks.myaccount.mysettings', true);
                if (_.eq(ql, 'wallet')) _.set(template, 'quicklinks.myaccount.wallet', true);
                if (_.eq(ql, 'summary')) _.set(template, 'quicklinks.report.summary', true);
                if (_.eq(ql, 'detailed')) _.set(template, 'quicklinks.report.detailed', true);
                if (_.eq(ql, 'search')) _.set(template, 'quicklinks.report.search', true);
            }

            return template;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not get quick links. Please try again', { code });

            return e;
        }
    });

    fastify.post('/aupdatequicklinksettings', {
        preValidation: [],
        schema: aupdatequicklinksettingsSchema,
    }, async (req, reply) => {
        try {
            const { cli_id, user } = req.user;
            const { selected_quicklinks } = req.body;

            const quicklinksValuesArr = [];

            /* prepare values for group_user_mapping table */
            if (!_.isEmpty(selected_quicklinks)) {
                _.forEach(selected_quicklinks, (obj) => {
                    const ql = _.get(obj, 'ql');
                    const group = _.get(obj, 'group');
                    // d, cli_id, username, quicklink, `group`, done_by
                    quicklinksValuesArr.push([fastify.nanoid(), cli_id, user, ql, group, cli_id]);
                });

                const result = await fastify.updateQuicklinkSettings(cli_id, quicklinksValuesArr);

                // TODO: need to check the updated status and reply accordingly { affectedRows: 1, insertId: 0, warningStatus: 0 }

                console.log('/aupdatequicklinksettings resp from db => ', result);
            } else {
                // delete all the settings
                const result = await fastify.deleteQuicklinkSettings(cli_id);
                console.log('/aupdatequicklinksettings resp from db => ', result);
            }

            const resp = { statusCode: 200, message: 'Quick link settings updated successfully' };

            return resp;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not update quick link settings. Please try again', { code });

            return e;
        }
    });

    // wallet transactions
    fastify.post('/awt', { preValidation: [], schema: awtSchema }, async (req, reply) => {
        try {
            const { dateselectiontype, fdate, tdate, q } = req.body;
            const { cli_id, tz } = req.user;
            let { billing_currency } = req.user;
            let result = [];

            const { fromdatetimeStrInIST, todatetimeStrInIST } = await fastify.getFromAndToDate(tz, dateselectiontype, fdate, tdate, true);

            if (_.eq(q, 'lu')) {
                // get the transaction for loggedin user
                result = await fastify.getLoggedinUserWalletTransactions(cli_id, fromdatetimeStrInIST, todatetimeStrInIST);
            } else {
                // get wallet transactions done by this user
                result = await fastify.getWalletTransactions(cli_id, fromdatetimeStrInIST, todatetimeStrInIST);
            }

            if (result.length === 0) return [];

            // get the billing curency for the user list
            const cliidArr = _.map(result, 'cli_id');

            // group by cli_id
            const result3 = await fastify.getAllUsersInfoForIds(cliidArr);
            const groupbyCli = _.groupBy(result3, 'cli_id');

            for (const obj of result) {
                const cts = _.get(obj, 'created_ts', null);

                if (_.eq(q, 'u')) {
                    // get bill currency of user
                    billing_currency = _.get(_.get(groupbyCli, obj.cli_id)[0], 'billing_currency', '');
                }
                _.set(obj, 'billing_currency', billing_currency);

                if (!_.isNull(cts)) {
                    const ctsMoment = mtz.tz(cts, tz);
                    // convert to acc tz
                    const formattedDt = ctsMoment.format('MMM DD, YYYY HH:mm:ss');
                    _.set(obj, 'created_ts', formattedDt);
                    _.set(obj, 'created_ts_unix', ctsMoment.unix());
                }
            }
            // console.log('/awt resp from db => ', result);

            return result;
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get wallet transactions. Please try again', { err });

            return e;
        }
    });

    /**
     *  New account => billing rates other countries
     *  get parent user's bill rates for other countries
     * call this if intl is enabled
     */

    fastify.get('/aparentbrrateintl', { preValidation: [], schema: parentbrrateintlSchema }, async (req, reply) => {
        try {
            const { cli_id, bill_type } = req.user;
            const rates = [];
            let smsleft = 0;
            let walletBal = 0;

            const result1 = await fastify.getBillRatesForIntl(cli_id);

            let convRate = await fastify.getConversionRateForUser(cli_id, process.env.INTL_BASE_CURRENCY);

            // convert the rate to base_currency to billing_currency as the above method return billing to base
            convRate = 1 / convRate;
            console.log('/aparentbrrateintl Convertion rate to be used => ', convRate);

            if (+bill_type === 1) {
                walletBal = await fastify.redis.WALLET.hget('wallet:amount', cli_id);
            }
            console.log(walletBal);

            for await (const row of result1) {
                const country = _.get(row, 'country');
                const baserate = _.get(row, 'base_sms_rate');
                const addlrate = _.get(row, 'base_add_fixed_rate');
                const conv_baserate = baserate * convRate;
                const conv_addlrate = addlrate * convRate;

                if (+bill_type === 1) {
                    // get the total sms that can be sent for this wallet
                    smsleft = await fastify.getSMSLeft(walletBal, conv_baserate, conv_addlrate);
                }
                rates.push({ smsrate: _.floor(conv_baserate, 6), country, sms_left: smsleft });
            }

            return rates;
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Something went wrong. Please try again', { err });
            return e;
        }
    });

    fastify.post('/abrratesintl', { preValidation: [], schema: brrateintlSchema }, async (req, reply) => {
        try {
            const { cli_id: loggedin_cli_id } = req.user;
            const { cli_id } = req.body;
            const rates = [];
            const respObj = {
                conv_rate: 0,
                rates_arr: [],
            };

            const [result1, result2] = await Promise.all([fastify.getBillRatesForIntl(cli_id), fastify.findUserById(cli_id)]);
            // find the user conversion type to use
            const userObj = _.get(result2, '0', {});
            const billing_currency = _.get(userObj, 'billing_currency');

            const billtobaseRate = await fastify.getConversionRateForUser(cli_id, process.env.INTL_BASE_CURRENCY);
            const basetobillRate = 1 / billtobaseRate;

            for (const row of result1) {
                const country = _.get(row, 'country');
                const baserate = _.get(row, 'base_sms_rate');
                rates.push({ sms_rate: _.floor(baserate * basetobillRate, 6), country, billing_currency, base_sms_rate: _.floor(baserate, 6) });
            }
            _.set(respObj, 'conv_rate', billtobaseRate);
            _.set(respObj, 'rates_arr', rates);

            return respObj;
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Something went wrong. Please try again', { err });
            return e;
        }
    });

    fastify.get('/permissions', { preValidation: [] }, async (req, reply) => {
        const { cli_id, user } = req.user;
        try {            
            req.log.debug(`/permissions getting permissions for cli_id => ${cli_id}  username => ${user}`);

            if(_.isUndefined(cli_id) || _.isNull(cli_id)){
                return fastify.httpErrors.badRequest('Could not find cli_id in token');
            }

            const result = await fastify.findUserById(cli_id);
            const userObj = _.get(result, '0', {});
            req.log.debug(`/permissions user details for cli_id => ${cli_id}  => ${JSON.stringify(userObj)}`);

            if (_.isEmpty(userObj)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            }

            let encodedPayload = await fastify.getUIPermissions(userObj);           
            fastify.log.debug(`/permissions cli_id => ${cli_id} => ${user} permissions available => ${JSON.stringify(encodedPayload)}`);
            return encodedPayload;
        } catch (err) {
            req.log.error(`/permissions cli_id => ${cli_id} => ${user}  error   => ${err}`);
            const e = fastify.httpErrors.createError(500, 'Something went wrong', { err });
            return e;
        }
    });
}

module.exports = account;
