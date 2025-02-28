/* eslint-disable no-useless-catch */
/* eslint-disable no-restricted-syntax */
const _ = require('lodash');
const bcryptjs = require('bcryptjs');
const readline = require('readline');
const moment = require('moment');
const fs = require('fs');
const csv = require('csv-parser');
const readXlsxFile = require('read-excel-file/node');
const excel = require('fast-xlsx-reader');

const { loginSchema, verifypasswordSchema, forgotpasswordSchema } = require('../../../schema/auth-schema');

async function test(fastify, opts) {
    async function constructSuperAdminObj(uObj, newcliid, tgidusersAllocationGroupBy, tgidusersAssignmentGroupBy, adminServicesDataMap, ausername) {
        const acctype = _.get(uObj, 'acctype');
        let bill_type = 0; // 0-post 1-pre
        let wallet = 0;
        const loggedin_cli_id = newcliid;
        if (_.eq(acctype, 'prepaid')) {
            bill_type = 1;
            wallet = 100;
        }
        // get the allocation list for this admin
        const allocated_tgroup_ids = _.map(_.get(tgidusersAllocationGroupBy, ausername), 'tgid');
        const asstgobj = _.get(tgidusersAssignmentGroupBy, ausername)[0];
        const assigned_tgroup_id = _.get(asstgobj, 'tgid');

        // get services
        const serviceobj = _.get(adminServicesDataMap, ausername)[0];
        const servicesArr = _.get(serviceobj, 'sArr');
        const services = [];
        let smpp_charset = '';
        for (const v of servicesArr) {
            if (!_.eq(_.toLower(v), 'ui')) {
                if (_.eq(_.toLower(v), 'api')) services.push({ service: 'sms', sub_service: 'api' });
                if (_.eq(_.toLower(v), 'smpp')) {
                    services.push({ service: 'sms', sub_service: 'smpp' });
                    smpp_charset = 'ISO-8859';
                }
            }
        }
        services.push({ service: 'sms', sub_service: 'cm' });
        console.log('sa smsrate ', uObj.admin_smsrate, Number(uObj.admin_smsrate));

        const accObj = {
            loggedin_user_type: 0,
            loggedin_cli_id,
            su_id: loggedin_cli_id,
            bill_type,
            sessionid: fastify.nanoid(),
            username: uObj.pusername,
            password: uObj.ppassword,
            user_type: 0,
            firstname: uObj.pusername,
            lastname: 'L',
            company: uObj.company,
            email: uObj.pemail,
            mobile: uObj.pmobile,
            zone_name: 'Asia/Calcutta',
            offset: '+05:30',
            country_code_iso3: '',
            allocated_tgroup_ids,
            assigned_tgroup_id,
            smpp_charset,
            address: 'To be updated',
            twofa_yn: 0,
            smsrate: uObj.admin_smsrate,
            dltrate: 0.025,
            newline_chars: '',
            encrypt_mobile_yn: 0,
            encrypt_message_yn: 0,
            assigned_groups: [],
            services,
            wallet,
        };

        return accObj;
    }

    async function constructAdminObj(uObj, postpaidSACliid, postpaidSAUsername, prepaidSACliid, prepaidSAUsername, tgidusersAllocationGroupBy, tgidusersAssignmentGroupBy, adminServicesDataMap, ausername) {
        const acctype = _.get(uObj, 'acctype');
        let bill_type = 0; // 0-post 1-pre
        let wallet = 0;
        let loggedin_cli_id = postpaidSACliid;
        let user = postpaidSAUsername;
        if (_.eq(acctype, 'prepaid')) {
            bill_type = 1;
            wallet = 10;
            loggedin_cli_id = prepaidSACliid;
            user = prepaidSAUsername;
        }
        // get the allocation list for this admin
        const allocated_tgroup_ids = _.map(_.get(tgidusersAllocationGroupBy, ausername), 'tgid');
        const asstgobj = _.get(tgidusersAssignmentGroupBy, ausername)[0];
        const assigned_tgroup_id = _.get(asstgobj, 'tgid');

        // get services
        const serviceobj = _.get(adminServicesDataMap, ausername)[0];
        const servicesArr = _.get(serviceobj, 'sArr');
        const services = [];
        let smpp_charset = '';
        for (const v of servicesArr) {
            if (!_.eq(_.toLower(v), 'ui')) {
                if (_.eq(_.toLower(v), 'api')) services.push({ service: 'sms', sub_service: 'api' });
                if (_.eq(_.toLower(v), 'smpp')) {
                    services.push({ service: 'sms', sub_service: 'smpp' });
                    smpp_charset = 'ISO-8859';
                }
            }
        }
        services.push({ service: 'sms', sub_service: 'cm' });
        console.log('admin smsrate ', uObj.admin_smsrate, Number(uObj.admin_smsrate));

        const accObj = {
            loggedin_user_type: 0,
            loggedin_cli_id,
            su_id: loggedin_cli_id,
            user,
            bill_type,
            sessionid: fastify.nanoid(),
            username: uObj.pusername,
            password: uObj.ppassword,
            user_type: 1,
            firstname: uObj.pusername,
            lastname: 'L',
            company: uObj.company,
            email: uObj.pemail,
            mobile: uObj.pmobile,
            zone_name: 'Asia/Calcutta',
            offset: '+05:30',
            country_code_iso3: '',
            allocated_tgroup_ids,
            assigned_tgroup_id,
            smpp_charset,
            address: 'To be updated',
            twofa_yn: 0,
            smsrate: uObj.admin_smsrate,
            dltrate: 0.025,
            newline_chars: '',
            encrypt_mobile_yn: 0,
            encrypt_message_yn: 0,
            assigned_groups: [],
            services,
            wallet,
        };

        return accObj;
    }

    async function constructUserObj(uObj, loggedin_cli_id, loggedin_user, su_id, acctype, tgidusersAssignmentGroupBy) {
        let bill_type = 0; // 0-post 1-pre
        let wallet = 0;
        if (_.eq(acctype, 'prepaid')) {
            bill_type = 1;
            wallet = 10;
        }
        // get the allocation list for this user
        const allocated_tgroup_ids = [];
        const asstgobj = _.get(tgidusersAssignmentGroupBy, uObj.username)[0];
        const assigned_tgroup_id = _.get(asstgobj, 'tgid');

        // get services
        const servicesArr = _.get(uObj, 'services');
        const services = [];
        let smpp_charset = '';
        for (const v of servicesArr) {
            if (!_.eq(_.toLower(v), 'ui')) {
                if (_.eq(_.toLower(v), 'api')) services.push({ service: 'sms', sub_service: 'api' });
                if (_.eq(_.toLower(v), 'smpp')) {
                    services.push({ service: 'sms', sub_service: 'smpp' });
                    smpp_charset = 'ISO-8859';
                }
            }
        }
        services.push({ service: 'sms', sub_service: 'cm' });

        const accObj = {
            loggedin_user_type: 0,
            loggedin_cli_id,
            su_id,
            user: loggedin_user,
            bill_type,
            sessionid: fastify.nanoid(),
            username: uObj.username,
            password: uObj.password,
            user_type: 2,
            firstname: uObj.username,
            lastname: 'L',
            company: uObj.company,
            email: uObj.email,
            mobile: uObj.mobile,
            zone_name: 'Asia/Calcutta',
            offset: '+05:30',
            country_code_iso3: '',
            allocated_tgroup_ids,
            assigned_tgroup_id,
            smpp_charset,
            address: 'To be updated',
            twofa_yn: 0,
            smsrate: uObj.user_smsrate,
            dltrate: 0.025,
            newline_chars: '',
            encrypt_mobile_yn: 0,
            encrypt_message_yn: 0,
            assigned_groups: [],
            services,
            wallet,
        };

        return accObj;
    }

    async function createSAAccount(accObj, newCliId) {
        try {
            let ui_pass = '';
            let api_pass = '';
            let smpp_pass = '';
            let api_pass_cust = '';
            let smpp_pass_cust = '';
            let vl_shortner = 0;
            const servicesValuesArr = [];
            const tgValuesArr = [];
            const assignedGroupsValuesArr = [];

            const { user_type, username, password, firstname, lastname, company, email, mobile, zone_name, offset, country_code_iso3, allocated_tgroup_ids, assigned_tgroup_id, smpp_charset, address, twofa_yn, smsrate, dltrate, newline_chars, encrypt_mobile_yn, encrypt_message_yn, assigned_groups, services } = accObj;
            let { wallet } = accObj;

            const { loggedin_user_type, loggedin_cli_id, su_id, bill_type, sessionid } = accObj;
            const { TYPE_USER, TYPE_ADMIN, MAX_ADMINS_UNDER_SA, MAX_USERS_UNDER_ADMIN } = process.env;

            /* check if username is unique */
            const result = await fastify.isUsernameFound(username);
            const { counts } = result[0];

            console.log('matching username count => ', counts);
            const isUnique = (counts === 0);

            if (!isUnique) {
                const resp = {
                    statusCode: fastify.CONSTANTS.USERNAME_NOT_UNIQUE_CODE,
                    message: 'Username is not unique',
                    status: 'failed',
                };
                throw new Error(JSON.stringify(resp));
            }

            /*  create wallet info */
            if (+bill_type === 1) { // its wallet account
                // trim to max 4 decimal places
                wallet = _.floor(wallet, 6);
            }

            const subServices = _.map(services, (o) => o.sub_service);
            if (_.includes(subServices, 'cm')) {
                console.log('new password for the user => ', newCliId, ' is ', password);
                // hash the password
                ui_pass = fastify.hash(password);
                console.log('hashed password => ', ui_pass);
                // set vl shortner to 1
                vl_shortner = 1;
            }

            if (_.includes(subServices, 'smpp')) {
                const smppdata = await fastify.smpppass(newCliId);
                smpp_pass = _.get(smppdata.passwords, 0).dbinsert_password;
                smpp_pass_cust = _.get(smppdata.passwords, 0).customer_password;
            }

            if (_.includes(subServices, 'api')) {
                const apidata = await fastify.apipass(newCliId);
                api_pass = _.get(apidata.passwords, 0).dbinsert_password;
                api_pass_cust = _.get(apidata.passwords, 0).customer_password;
            }

            const a1 = await fastify.redis.GEN.hset('cli:api:pass', newCliId, api_pass_cust);
            const a2 = await fastify.redis.GEN.hset('cli:smpp:pass', newCliId, smpp_pass_cust);

            // TODO: send password thru mail

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
                walletTransValuesArr = [fastify.nanoid(), newCliId, username, 'add', wallet, 'New Account', '', sessionid, loggedin_cli_id, loggedin_cli_id];
            }

            const obj = {
                newCliId,
                su_id,
                loggedin_cli_id,
                bill_type,
                user_type,
                firstname,
                lastname,
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
            };

            const resultAcc = await fastify.createSAAccount(obj);

            // do wallet
            if (+bill_type === 1) { // its wallet account
                try {
                    // trim to max 4 decimal places
                    wallet = _.floor(wallet, 6);
                    const rr1 = await fastify.redis.WALLET.hincrbyfloat('wallet:amount', newCliId, wallet);
                    console.log(rr1);
                } catch (e) {
                    /* error at this stage denotes that account is successfully created and the wallet amount has been deducted from logged in user but not allocated to new user */
                    // TODO ***CRITICAL*** Need to log this data in table for manual processing
                    console.error('/anew ***LEAKAGE*** Wallet amount is not allocated to this new user. but account creation and logged in user wallet deduction is successful ');
                }
            }
            const resp = { statusCode: 200, message: 'Account has been created successfully' };
            return newCliId;
        } catch (err) {
            throw err;
        }
    }

    async function createAccount(accObj) {
        let newCliId = '';
        try {
            let ui_pass = '';
            let api_pass = '';
            let smpp_pass = '';
            let api_pass_cust = '';
            let smpp_pass_cust = '';
            let vl_shortner = 0;
            const servicesValuesArr = [];
            const tgValuesArr = [];
            const assignedGroupsValuesArr = [];

            const { user_type, username, password, firstname, lastname, company, email, mobile, zone_name, offset, country_code_iso3, allocated_tgroup_ids, assigned_tgroup_id, smpp_charset, address, twofa_yn, smsrate, dltrate, newline_chars, encrypt_mobile_yn, encrypt_message_yn, assigned_groups, services } = accObj;
            let { wallet } = accObj;

            const { loggedin_user_type, loggedin_cli_id, su_id, bill_type, sessionid, user } = accObj;
            const { TYPE_USER, TYPE_ADMIN, MAX_ADMINS_UNDER_SA, MAX_USERS_UNDER_ADMIN } = process.env;

            /* check if the user_type is a valid type for the logged in user (logged in admin cannot create another admin) */
            if (+loggedin_user_type == +TYPE_USER) {
                // return fastify.httpErrors.forbidden('Forbidden from creating account by user');
                throw new Error('Forbidden from creating account by user');
            }

            if (+loggedin_user_type == +TYPE_ADMIN) {
                // check what is the user he is trying to create. he can create only user account
                if (+user_type != +TYPE_USER) {
                    const resp = {
                        statusCode: fastify.CONSTANTS.INVALID_USER_TYPE_IN_ACCOUNT_CREATION,
                        message: `Invalid user type. You cant create user type ${user_type}`,
                    };
                    throw new Error(JSON.stringify(resp));
                }
            }

            /* check if username is unique */
            const result = await fastify.isUsernameFound(username);
            const { counts } = result[0];

            console.log('matching username count => ', counts);
            const isUnique = (counts === 0);

            if (!isUnique) {
                const resp = {
                    statusCode: fastify.CONSTANTS.USERNAME_NOT_UNIQUE_CODE,
                    message: 'Username is not unique',
                    status: 'failed',
                };
                throw new Error(JSON.stringify(resp));
            }

            /* compose cli_id */
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

            console.log('new user id to be created => ', newCliId);

            /*  create wallet info */
            if (+bill_type === 1) { // its wallet account
                // trim to max 4 decimal places
                wallet = _.floor(wallet, 6);
            }

            const subServices = _.map(services, (o) => o.sub_service);
            if (_.includes(subServices, 'cm')) {
                console.log('new password for the user => ', newCliId, ' is ', password);
                // hash the password
                ui_pass = fastify.hash(password);
                console.log('hashed password => ', ui_pass);
                // set vl shortner to 1
                vl_shortner = 1;
            }

            if (_.includes(subServices, 'smpp')) {
                const smppdata = await fastify.smpppass(newCliId);
                smpp_pass = _.get(smppdata.passwords, 0).dbinsert_password;
                smpp_pass_cust = _.get(smppdata.passwords, 0).customer_password;
            }

            if (_.includes(subServices, 'api')) {
                const apidata = await fastify.apipass(newCliId);
                api_pass = _.get(apidata.passwords, 0).dbinsert_password;
                api_pass_cust = _.get(apidata.passwords, 0).customer_password;
            }

            const a1 = await fastify.redis.GEN.hset('cli:api:pass', newCliId, api_pass_cust);
            const a2 = await fastify.redis.GEN.hset('cli:smpp:pass', newCliId, smpp_pass_cust);

            // TODO: send password thru mail

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

            const obj = {
                newCliId,
                su_id,
                loggedin_cli_id,
                bill_type,
                user_type,
                firstname,
                lastname,
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
            };

            const resultAcc = await fastify.createAccount(obj);

            // do wallet
            if (+bill_type === 1) { // its wallet account
                try {
                    // trim to max 4 decimal places
                    wallet = _.floor(wallet, 6);
                    const rr1 = await fastify.redis.WALLET.hincrbyfloat('wallet:amount', newCliId, wallet);
                    console.log(rr1);
                } catch (e) {
                    /* error at this stage denotes that account is successfully created and the wallet amount has been deducted from logged in user but not allocated to new user */
                    // TODO ***CRITICAL*** Need to log this data in table for manual processing
                    console.error('/anew ***LEAKAGE*** Wallet amount is not allocated to this new user. but account creation and logged in user wallet deduction is successful ');
                }
            }
            const resp = { statusCode: 200, message: 'Account has been created successfully' };
            return newCliId;
        } catch (err) {
            throw err;
        }
    }

    fastify.get('/migratedlt1', { preValidation: [], schema: null }, async (req, reply) => {
        const csvData = [];

        const results = [];
        try {
            fs.createReadStream('dlt-templates-latest.csv')
                .pipe(csv())
                .on('data', (data) => {
                    console.log('on data', data);
                    // console.log('on data', _.get(data, ' dlt_req_uid'));

                    results.push([data.dlt_req_uid, data.username, data.entity_id, data.template_id, data.consent_type, data.header_reg_id, data.senderid, data.register_at_operator, data.telemarketer, data.senderid_reg_date, data.template_msg, data.template_sample_msg, data.senderid_req_date, data.approval_status, null, 'active', null, data.if_deleted]);
                })
                .on('end', async () => {
                    console.log('>>>>>>>> done');
                    await fastify.insertToDLT(results);
                    console.log('>>>>>>>> done db');

                    return results;
                    // [
                    //   { NAME: 'Daffy Duck', AGE: '24' },
                    //   { NAME: 'Bugs Bunny', AGE: '22' }
                    // ]
                });
            // const fileStream = fs.createReadStream('dlt-templates-latest.csv');
            //
            // const rl = readline.createInterface({
            //     input: fileStream,
            //     crlfDelay: Infinity,
            // });
            //
            //
            // for await (const line of rl) {
            //     // Each line in input.txt will be successively available here as `line`.
            //     // console.log(`Line from file: ${line}`);
            //     csvData.push(_.split(line, ','));
            // }
        } catch (e) {
            return e;
        }

        // return results;
    });

    fastify.get('/migrate1', { preValidation: [], schema: null }, async (req, reply) => {
        const csvData = [];
        let groupby = '';
        const tgidusersGroupBy = '';
        const accArr = [];
        const tobeUpdateArr = [];
        const saPostServiceArr = [];
        const saPreServiceArr = [];
        const saPostTgidArr = [];
        const saPreTgidArr = [];

        try {
            const fileStream = fs.createReadStream('customers.csv');

            const rl = readline.createInterface({
                input: fileStream,
                crlfDelay: Infinity,
            });
            let prepaidSACliid = 40000000;
            const prepaidSAUsername = 'sapremigration';
            let postpaidSACliid = 30000000;
            const postpaidSAUsername = 'sapostmigration';

            for await (const line of rl) {
                // Each line in input.txt will be successively available here as `line`.
                // console.log(`Line from file: ${line}`);
                const rowArr = _.split(line, ',');
                // console.log('row => ', rowArr);
                const company = _.trim(_.get(rowArr, 0, ''));
                const pusername = _.trim(_.get(rowArr, 1, ''));
                const ppassword = _.trim(_.get(rowArr, 2, ''));
                const pmobile = _.trim(_.get(rowArr, 3, ''));
                const pemail = _.trim(_.get(rowArr, 4, ''));
                const username = _.trim(_.get(rowArr, 5, ''));
                const password = _.trim(_.get(rowArr, 6, 'password'));
                const mobile = _.trim(_.get(rowArr, 7, ''));
                const email = _.trim(_.get(rowArr, 8, ''));
                const servicesstr = _.trim(_.get(rowArr, 9, ''));
                const acctype = _.toLower(_.trim(_.get(rowArr, 10, '')));
                // console.log('admin smsrate => ', _.trim(_.get(rowArr, 16, '')));
                const admin_smsrate = Number(_.trim(_.get(rowArr, 16, '')));
                const user_smsrate = Number(_.trim(_.get(rowArr, 18, '')));
                const servicesArr = _.compact(_.split(servicesstr, '+'));
                csvData.push({
                    company,
                    pusername,
                    ppassword,
                    pmobile,
                    pemail,
                    username,
                    password,
                    mobile,
                    email,
                    acctype,
                    admin_smsrate,
                    user_smsrate,
                    services: servicesArr,
                });
            }

            const tgidForAdmin = {};
            const tgidForUsers = [];
            let tgid = 6000000; // start from this
            const servicesByAdmin = [];
            // group by pusername
            groupby = _.groupBy(csvData, 'pusername');

            // get admin accounts
            // const adminDataMap = {};
            // _.map(groupby, (o, adminuser) => {
            //     const adminusername = _.trim(adminuser);
            //     const listofUsers = _.get(groupby, adminuser, []);
            //     _.set(adminDataMap, adminusername, listofUsers[0]);
            // });

            // console.log('>>>>', adminDataMap); // >>>>>>>>>>>>>>>>>> data for admin user { <admin name>: { } }
            _.map(groupby, (o, adminuser) => {
                const adminusername = _.trim(adminuser);
                // accArr.push({ adminusername: o });
                const listofUsers = _.get(groupby, adminuser, []);
                for (const u of listofUsers) {
                    servicesByAdmin.push({ pusername: _.trim(adminusername), services: u.services });
                }
            });

            const servicesByAdminGroupBy = _.groupBy(servicesByAdmin, 'pusername');
            const finalServicesByAdmin = [];
            _.map(servicesByAdminGroupBy, (o, adminusername) => {
                const sArr = _.uniq(_.flattenDeep(_.map(o, 'services')));
                finalServicesByAdmin.push({ adminusername: _.trim(adminusername), sArr });
            });

            // console.log(finalServicesByAdmin); //  [ { adminusername: 'liveuser', sArr: [ 'API', 'SMPP', 'UI' ] }]

            const userUsernameArr = [];

            const adminusernamesArr = _.map(groupby, (o, adminuser) => {
                tgid += 1;
                const adminusername = _.trim(adminuser);
                _.set(tgidForAdmin, adminusername, tgid);

                // add admin tgid to tgidForUsers as well (this will form the allocated tgids)
                tgidForUsers.push({ username: adminusername, pusername: adminusername, tgid });

                // list of admin's users
                const adminuserarr = _.get(groupby, adminuser, []);
                for (const userObj of adminuserarr) {
                    tgid += 1;
                    tgidForUsers.push({ username: userObj.username, pusername: userObj.pusername, tgid });
                }

                const usernamesArr = _.map(adminuserarr, (o1) => `${_.toLower(_.trim(o1.username))}`);
                userUsernameArr.push(...usernamesArr);
                return `${_.toLower(_.trim(adminusername))}`;
            });
            // console.log(userUsernameArr);
            // console.log('total admin users =>', adminusernamesArr.length);
            // console.log('total users =>', userUsernameArr.length);

            // const finalArr = [];
            // finalArr.push(...adminusernamesArr);
            // finalArr.push(...userUsernameArr);
            // console.log('overall users =>', finalArr.length);
            // console.log('uniq users =>', _.uniq(finalArr).length);
            // console.log('tgids for admins =>', tgidForAdmin);
            // console.log('tgids for users =>', tgidForUsers); // >>>>>>>>>>>>>>>>>>>>> used for allocation (includes all users) [{ username: 'username', pusername: 'admin_name', tgid: 6000002 }]
            // console.log('tgids for users total =>', tgidForUsers.length);
            /** ****************************** */
            const adminnameList = _.uniq(_.map(csvData, 'pusername'));
            const tgidusersAssignmentGroupBy = _.groupBy(tgidForUsers, 'username'); // >>>>>>>>>>>>>>>>>>>> tgid assignment (includes all users) { chncorp: [ { username: 'chncorp', pusername: 'chncorp', tgid: 6000161 } ] }

            // format for tgidusersAllocationGroupBy
            /* chncorp: [
                { username: 'chncorp', pusername: 'chncorp', tgid: 6000161 },
                { username: 'edpcell', pusername: 'chncorp', tgid: 6000162 },
                { username: 'chncorphttp', pusername: 'chncorp', tgid: 6000163 },
                { username: 'chncorphttps', pusername: 'chncorp', tgid: 6000164 },
                { username: 'chncorpotp', pusername: 'chncorp', tgid: 6000165 }
            ],
            */
            const tgidusersAllocationGroupBy = _.groupBy(tgidForUsers, 'pusername'); // >>>>>>>>>>>>>>>>>>>> tgid allocation format above

            const adminServicesDataMap = _.groupBy(finalServicesByAdmin, 'adminusername'); // >>>>>>>>>>>>>>>>>>>>>>>>>>> {<admin username>: [ { sArr:["API","SMPP","UI"]} ] }
            // console.log(JSON.stringify(adminServicesDataMap));
            console.log(tgidusersAllocationGroupBy);
            console.log(Object.keys(tgidusersAllocationGroupBy).length);

            // const { user_type, firstname, lastname, company, username, email, mobile, zone_name, offset, country_code_iso3, allocated_tgroup_ids, assigned_tgroup_id, smpp_charset, address, twofa_yn, smsrate, dltrate, newline_chars, encrypt_mobile_yn, encrypt_message_yn, assigned_groups, services } = accObj;
            // let { wallet } = accObj;
            // const { loggedin_user_type, loggedin_cli_id, su_id, bill_type, sessionid, user } = accObj;

            // loop thru admin acc list
            const salist = [];
            for await (const ausername of adminnameList) {
                // get admin data
                const adminDataList = _.get(groupby, ausername);
                const uObj = adminDataList[0];
                const aacctype = _.toLower(_.get(uObj, 'acctype'));
                let sacliid = 0;
                if (_.eq(aacctype, 'prepaid')) {
                    prepaidSACliid += 1;
                    sacliid = `${prepaidSACliid}00000000`;
                } else {
                    postpaidSACliid += 1;
                    sacliid = `${postpaidSACliid}00000000`;
                }

                const accObj = await constructSuperAdminObj(uObj, sacliid, tgidusersAllocationGroupBy, tgidusersAssignmentGroupBy, adminServicesDataMap, ausername);
                // console.log(accObj);
                // if (_.eq(ausername, 'telebuadmin')) {
                // console.log('final acc obj', accObj);
                salist.push(accObj);

                const newCliId = await createSAAccount(accObj, sacliid);
                // // const newCliId = 1;
                console.log('SA created >>> ', newCliId, ausername);
                //
                tobeUpdateArr.push({ username: accObj.username, cli_id: newCliId, tgid: accObj.assigned_tgroup_id });
                if (_.eq(aacctype, 'prepaid')) {
                    saPreTgidArr.push(accObj.assigned_tgroup_id);
                } else {
                    saPostTgidArr.push(accObj.assigned_tgroup_id);
                }

                for await (const uObj1 of adminDataList) {
                    const su_id = sacliid;
                    const uname = _.get(uObj1, 'username');

                    // if (_.eq(uname, 'telebu')) {
                    const accObj1 = await constructUserObj(uObj1, newCliId, ausername, su_id, aacctype, tgidusersAssignmentGroupBy);
                    // console.log('user obj >>>>', accObj);
                    // const newuserCliId = 2;
                    const newuserCliId = await createAccount(accObj1);

                    if (_.eq(aacctype, 'prepaid')) {
                        saPreTgidArr.push(accObj1.assigned_tgroup_id);
                    } else {
                        saPostTgidArr.push(accObj1.assigned_tgroup_id);
                    }

                    console.log('User created >>> ', newuserCliId, ausername, uname);
                    tobeUpdateArr.push({
                        username: accObj1.username,
                        cli_id: newuserCliId,
                        tgid: accObj1.assigned_tgroup_id,
                    });
                }
            }
            // this is another bug fix
            /** ****************************************************** */
            // prepare values for dlt_template_group insert
            const tgValuesArr = [];
            // this is the fix for prod
            for (const o of tgidForUsers) {
                const id = _.get(o, 'tgid');
                const gname = `${_.toUpper(_.get(o, 'username'))} Templates`;
                tgValuesArr.push([id, gname]);
            }
            // persist
            const r11 = await fastify.insertToDLTTemplateGroup(tgValuesArr);
            /** ****************************** */
            console.log('>>>>>>> accounts migrated');

            /** ****************************** */
            // update the respective cli_id and tgid for the user
            // tobeUpdateArr.push({
            //     username: accObj1.username,
            //     cli_id: newuserCliId,
            //     tgid: accObj1.assigned_tgroup_id,
            // });
            for await (const o of tobeUpdateArr) {
                const r6 = await fastify.update(o.username, o.tgid, o.cli_id);
                console.log('resp from db =>', r6);
            }
        } catch (e) {
            return e;
        }
        console.log(_.uniq(saPreServiceArr), _.uniq(saPostServiceArr));
        return { tobeUpdateArr };
    });

    fastify.get('/migrateold1', { preValidation: [], schema: null }, async (req, reply) => {
        const csvData = [];
        let groupby = '';
        const tgidusersGroupBy = '';
        const accArr = [];
        const tobeUpdateArr = [];
        const saPostServiceArr = [];
        const saPreServiceArr = [];
        const saPostTgidArr = [];
        const saPreTgidArr = [];

        try {
            const fileStream = fs.createReadStream('customers.csv');

            const rl = readline.createInterface({
                input: fileStream,
                crlfDelay: Infinity,
            });
            const prepaidSACliid = '4000000100000000';
            const prepaidSAUsername = 'sapremigration';
            const postpaidSACliid = '3000000100000000';
            const postpaidSAUsername = 'sapostmigration';

            for await (const line of rl) {
                // Each line in input.txt will be successively available here as `line`.
                // console.log(`Line from file: ${line}`);
                const rowArr = _.split(line, ',');
                // console.log('row => ', rowArr);
                const company = _.trim(_.get(rowArr, 0, ''));
                const pusername = _.trim(_.get(rowArr, 1, ''));
                const ppassword = _.trim(_.get(rowArr, 2, ''));
                const pmobile = _.trim(_.get(rowArr, 3, ''));
                const pemail = _.trim(_.get(rowArr, 4, ''));
                const username = _.trim(_.get(rowArr, 5, ''));
                const password = _.trim(_.get(rowArr, 6, 'password'));
                const mobile = _.trim(_.get(rowArr, 7, ''));
                const email = _.trim(_.get(rowArr, 8, ''));
                const servicesstr = _.trim(_.get(rowArr, 9, ''));
                const acctype = _.toLower(_.trim(_.get(rowArr, 10, '')));
                console.log('admin smsrate => ', _.trim(_.get(rowArr, 16, '')));
                const admin_smsrate = Number(_.trim(_.get(rowArr, 16, '')));
                const user_smsrate = Number(_.trim(_.get(rowArr, 18, '')));
                const servicesArr = _.compact(_.split(servicesstr, '+'));
                csvData.push({
                    company,
                    pusername,
                    ppassword,
                    pmobile,
                    pemail,
                    username,
                    password,
                    mobile,
                    email,
                    acctype,
                    admin_smsrate,
                    user_smsrate,
                    services: servicesArr,
                });
            }

            const tgidForAdmin = {};
            const tgidForUsers = [];
            let tgid = 6000000; // start from this
            const servicesByAdmin = [];
            // group by pusername
            groupby = _.groupBy(csvData, 'pusername');

            // get admin accounts
            const adminDataMap = {};
            _.map(groupby, (o, adminuser) => {
                const adminusername = _.trim(adminuser);
                const listofUsers = _.get(groupby, adminuser, []);
                _.set(adminDataMap, adminusername, listofUsers[0]);
            });

            // console.log('>>>>', adminDataMap); // >>>>>>>>>>>>>>>>>> data for admin user { <admin name>: { } }
            _.map(groupby, (o, adminuser) => {
                const adminusername = _.trim(adminuser);
                accArr.push({ adminusername: o });
                const listofUsers = _.get(groupby, adminuser, []);
                for (const u of listofUsers) {
                    servicesByAdmin.push({ pusername: _.trim(adminusername), services: u.services });
                }
            });

            const servicesByAdminGroupBy = _.groupBy(servicesByAdmin, 'pusername');
            const finalServicesByAdmin = [];
            _.map(servicesByAdminGroupBy, (o, adminusername) => {
                const sArr = _.uniq(_.flattenDeep(_.map(o, 'services')));
                finalServicesByAdmin.push({ adminusername: _.trim(adminusername), sArr });
            });

            // console.log(finalServicesByAdmin); //  [ { adminusername: 'liveuser', sArr: [ 'API', 'SMPP', 'UI' ] }]

            const userUsernameArr = [];

            const adminusernamesArr = _.map(groupby, (o, adminuser) => {
                tgid += 1;
                const adminusername = _.trim(adminuser);
                _.set(tgidForAdmin, adminusername, tgid);

                // add admin tgid to tgidForUsers as well (this will form the allocated tgids)
                tgidForUsers.push({ username: adminusername, pusername: adminusername, tgid });

                // list of admin's users
                const adminuserarr = _.get(groupby, adminuser, []);
                for (const userObj of adminuserarr) {
                    tgid += 1;
                    tgidForUsers.push({ username: userObj.username, pusername: userObj.pusername, tgid });
                }

                const usernamesArr = _.map(adminuserarr, (o1) => `${_.toLower(_.trim(o1.username))}`);
                userUsernameArr.push(...usernamesArr);
                return `${_.toLower(_.trim(adminusername))}`;
            });
            // console.log(userUsernameArr);
            // console.log('total admin users =>', adminusernamesArr.length);
            // console.log('total users =>', userUsernameArr.length);

            const finalArr = [];
            finalArr.push(...adminusernamesArr);
            finalArr.push(...userUsernameArr);
            // console.log('overall users =>', finalArr.length);
            // console.log('uniq users =>', _.uniq(finalArr).length);
            // console.log('tgids for admins =>', tgidForAdmin);
            // console.log('tgids for users =>', tgidForUsers); // >>>>>>>>>>>>>>>>>>>>> used for allocation (includes all users) [{ username: 'username', pusername: 'admin_name', tgid: 6000002 }]
            // console.log('tgids for users total =>', tgidForUsers.length);
            /** ****************************** */
            const adminnameList = _.uniq(_.map(csvData, 'pusername'));
            const tgidusersAssignmentGroupBy = _.groupBy(tgidForUsers, 'username'); // >>>>>>>>>>>>>>>>>>>> tgid assignment (includes all users) { chncorp: [ { username: 'chncorp', pusername: 'chncorp', tgid: 6000161 } ] }

            // format for tgidusersAllocationGroupBy
            /* chncorp: [
                { username: 'chncorp', pusername: 'chncorp', tgid: 6000161 },
                { username: 'edpcell', pusername: 'chncorp', tgid: 6000162 },
                { username: 'chncorphttp', pusername: 'chncorp', tgid: 6000163 },
                { username: 'chncorphttps', pusername: 'chncorp', tgid: 6000164 },
                { username: 'chncorpotp', pusername: 'chncorp', tgid: 6000165 }
            ],
            */
            const tgidusersAllocationGroupBy = _.groupBy(tgidForUsers, 'pusername'); // >>>>>>>>>>>>>>>>>>>> tgid allocation format above
            const adminServicesDataMap = _.groupBy(finalServicesByAdmin, 'adminusername'); // >>>>>>>>>>>>>>>>>>>>>>>>>>> {<admin username>: [ { sArr:["API","SMPP","UI"]} ] }
            // console.log(JSON.stringify(adminServicesDataMap));
            console.log(tgidusersAllocationGroupBy);
            console.log(Object.keys(tgidusersAllocationGroupBy).length);

            // const { user_type, firstname, lastname, company, username, email, mobile, zone_name, offset, country_code_iso3, allocated_tgroup_ids, assigned_tgroup_id, smpp_charset, address, twofa_yn, smsrate, dltrate, newline_chars, encrypt_mobile_yn, encrypt_message_yn, assigned_groups, services } = accObj;
            // let { wallet } = accObj;
            // const { loggedin_user_type, loggedin_cli_id, su_id, bill_type, sessionid, user } = accObj;

            // loop thru admin acc list
            for await (const ausername of adminnameList) {
                // get admin data
                const adminDataList = _.get(groupby, ausername);
                const uObj = adminDataList[0];
                const aacctype = _.get(uObj, 'acctype');

                const accObj = await constructAdminObj(uObj, postpaidSACliid, postpaidSAUsername, prepaidSACliid, prepaidSAUsername, tgidusersAllocationGroupBy, tgidusersAssignmentGroupBy, adminServicesDataMap, ausername);
                // console.log(accObj);
                // if (_.eq(ausername, 'telebuadmin')) {
                console.log('final acc obj', accObj);

                const newCliId = await createAccount(accObj);
                // const newCliId = 1;
                console.log('Admin created >>> ', newCliId, ausername);

                tobeUpdateArr.push({ username: accObj.username, cli_id: newCliId, tgid: accObj.assigned_tgroup_id });
                if (_.eq(aacctype, 'prepaid')) {
                    saPreTgidArr.push(accObj.assigned_tgroup_id);
                } else {
                    saPostTgidArr.push(accObj.assigned_tgroup_id);
                }

                for await (const uObj1 of adminDataList) {
                    let su_id = postpaidSACliid;
                    const uname = _.get(uObj1, 'username');
                    if (_.eq(aacctype, 'prepaid')) su_id = prepaidSACliid;
                    // if (_.eq(uname, 'telebu')) {
                    const accObj1 = await constructUserObj(uObj1, newCliId, ausername, su_id, aacctype, tgidusersAssignmentGroupBy);
                    // console.log('user obj >>>>', accObj);
                    // const newuserCliId = 2;
                    const newuserCliId = await createAccount(accObj1);

                    if (_.eq(aacctype, 'prepaid')) {
                        saPreTgidArr.push(accObj1.assigned_tgroup_id);
                    } else {
                        saPostTgidArr.push(accObj1.assigned_tgroup_id);
                    }

                    console.log('User created >>> ', newCliId, ausername, uname);
                    tobeUpdateArr.push({
                        username: accObj1.username,
                        cli_id: newuserCliId,
                        tgid: accObj1.assigned_tgroup_id,
                    });
                    // }
                }
                // }
            }
            /** ****************************************************** */
            // prepare values for dlt_template_group insert
            const tgValuesArr = [];
            for (const o of tgidForUsers) {
                const id = _.get(o, 'tgid');
                const gname = `${_.toUpper(_.get(o, 'username'))} Templates`;
                tgValuesArr.push([id, gname]);
            }
            // persist
            const r11 = await fastify.insertToDLTTemplateGroup(tgValuesArr);
            /** ****************************** */
            let satgValuesArr = [];
            for (const id of saPostTgidArr) {
                satgValuesArr.push([postpaidSACliid, id, postpaidSACliid]);
            }
            // persist
            const r1 = await fastify.insertToUsersTemplateGroupMap(satgValuesArr);

            satgValuesArr = [];
            for (const id of saPreTgidArr) {
                satgValuesArr.push([prepaidSACliid, id, prepaidSACliid]);
            }
            // persist
            const r2 = await fastify.insertToUsersTemplateGroupMap(satgValuesArr);
            /** ****************************** */
            const servicesArray = ['cm', 'api', 'smpp'];
            let servicesValuesArr = [];
            for (const s of servicesArray) {
                servicesValuesArr.push([postpaidSACliid, 'sms', s, postpaidSACliid]);
            }
            // persist
            const r3 = await fastify.insertToServicesMap(servicesValuesArr);

            servicesValuesArr = [];
            for (const s of servicesArray) {
                servicesValuesArr.push([prepaidSACliid, 'sms', s, prepaidSACliid]);
            }
            // persist
            const r4 = await fastify.insertToServicesMap(servicesValuesArr);

            console.log('>>>>>>> accounts migrated');

            /** ****************************** */
            // update the respective cli_id and tgid for the user
            // tobeUpdateArr.push({
            //     username: accObj1.username,
            //     cli_id: newuserCliId,
            //     tgid: accObj1.assigned_tgroup_id,
            // });
            for await (const o of tobeUpdateArr) {
                const r6 = await fastify.update(o.username, o.tgid, o.cli_id);
                console.log('resp from db =>', r6);
            }
        } catch (e) {
            return e;
        }
        console.log(_.uniq(saPreServiceArr), _.uniq(saPostServiceArr));
        return { tobeUpdateArr };
    });

    fastify.get('/downloadlogfile1', {}, async (req, reply) => {
        try {
            const { id } = req.query;
            // const { cli_id } = req.user;

            console.log('/downloadlog incoming params => ', [id]);

            // get file info
            const result = await fastify.getLogDownloadFileInfo(id);
            const obj = _.get(result, 0, {});
            console.log('/downloadlog resp from db =>', obj);

            if (_.isEmpty(obj)) return fastify.httpErrors.badRequest('Could not find file information');

            if (!_.eq(obj.status, 'completed')) return fastify.httpErrors.badRequest('Invalid download request');

            const downloadpath = obj.download_xl_path;
            console.log(downloadpath);

            // check if the file exists
            try {
                if (!fs.existsSync(downloadpath)) {
                    return fastify.httpErrors.notFound('Could not download file');
                }
            } catch (err) {
                console.error(err);
            }

            const filename = `log_${moment(obj.from_tz).format('YYYY-MM-DD')}-${moment(obj.to_tz).format('YYYY-MM-DD')}_${id}.zip`;
            // const readstream = fs.createReadStream('./package.json', 'utf8');
            const readstream = fs.createReadStream(downloadpath);

            reply.type('application/zip');
            reply.header('Content-Disposition', `attachment;filename=${filename}`);
            reply.send(readstream);
        } catch (err) {
            return fastify.httpErrors.createError(500, 'Could not persist download req. Please try again', { err });
        }
    });

    fastify.get('/testmail', { preValidation: [], schema: null }, async (req, reply) => {
        const subject = 'New Account - Campaign Manager';
        const message = 'Dear User, <br><br> Your password for accessing campaign manager <br><br> <div style=\'font-size: large;color: cadetblue\'>dee34rw3</div> <br><br> -Winnovature Team';

        const result = await fastify.findUserById('6000000200000000');
        const userObj = _.get(result, '0', {});
        const fname = _.get(userObj, 'firstname');
        const lname = _.get(userObj, 'lastname');
        const from_name = `${fname} ${lname}`;
        // const from_email = _.get(userObj, 'email');
        const from_email = 'alerts@malertservices.com';
        const to_email = 'am@winnovature.com';
        const to_name = 'AM';
        const reply_to = 'alerts@malertservices.com';

        const emailTemplate = fs.readFileSync('./templates/create_account_email_template.html', 'utf8');

        const services = [
            {
                service_name: 'Campaign Manager',
                pass: 'e335e',
                pass_addl_text: 'Password: ',
            },
            {
                service_name: 'API',
                pass: 'de3335e',
                pass_addl_text: 'API Key: ',
            },
        ];
        const dataObj = { showLogo: true, from_email, from_name, subject, to_email, to_name, reply_to, services };

        // const resp = await fastify.sendMail(from_email, from_name, subject, 'am@winnovature.com', 'to_name', 'reply_to', emailTemplate);
        const resp = await fastify.sendMail(emailTemplate, dataObj);
        console.log(resp);
        return resp;
    });

    fastify.post('/tes', { preValidation: [fastify.verifyAccessToken], schema: null }, async (req, reply) => {
        try {
            const { pass } = req.body;
            const { cli_id } = req.user;

            const { body } = await fastify.elastic.search({
                index: 'sub_del_t2',
                body: { query: { match_all: {} }, size: 50 },
            });

            // const { body } = await fastify.elastic.search({
            //     index: 'sub_del_t2',
            //     body: {
            //         query: { term: { country_ci: { value: 'india' } } },
            //         fields: ['recv_time'],
            //         _source: '{field}',
            //         size: 10,
            //     },
            // });

            // const { body } = await fastify.elastic.search({
            //     index: 'sub_del_t2',
            //     body: {
            //         query: {
            //             range: {
            //                 recv_time: {
            //                     gte: '2021-08-20 17:36:35',
            //                     lte: '2021-08-20 17:36:35',
            //                 },
            //             },
            //         },
            //     },
            // });

            // const { body } = await fastify.elastic.search({
            //     index: 'sub_del_t2',
            //     body: {
            //         query: {
            //             term: {
            //                 country_ci: 'iNdia',
            //             },
            //         },
            //         fields: ['msg', 'msg_id'],
            //         _source:
            //             '{field}',
            //         size:
            //             10,
            //     },
            // });
            // const { body } = await fastify.elastic.search({
            //     index: 'sub_del_t2',
            //     body: {
            //         query: {
            //             bool: {
            //                 filter: [
            //                     { term: { country_ci: 'india' } },
            //                     { terms: { msg_id: ['3002108201737023645200', '3002108201737023633800'] } },
            //                 ],
            //             },
            //         },
            //         size: 10,
            //     },
            // });
            // const { body } = await fastify.elastic.search({
            //     index: 'sub_del_t2',
            //     body: {
            //         query: {
            //             bool: {
            //                 filter: [
            //                     { term: { country_ci: 'india' } },
            //                     { terms: { msg_id: ['3002108201737023645200', '3002108201737023633800'] } },
            //                 ],
            //             },
            //         },
            //         aggs: {
            //             country_agg: {
            //                 terms: { field: 'country' }
            //             },
            //         },
            //         size: 1,
            //     },
            // });
            return body;
        } catch (err) {
            req.log.error(`error =>${err}`);
            return err;
        }
    });

    fastify.get('/testcsv', { preValidation: [], schema: null }, async (req, reply) => {
        try {
            // const { pass } = req.query;
            // const { cli_id } = req.user;

            const readstream = fs.createReadStream('./dummy.csv', 'utf8');
            const ws = fs.createWriteStream('dummy2.csv', { encoding: 'UTF-16le' });
            readstream.pipe(ws);
            reply.type('text/plain;charset=utf-8');
            reply.header('Content-Disposition', 'attachment;filename=dummy1.csv');
            reply.send(readstream);
        } catch (err) {
            req.log.error(`error =>${err}`);
            return err;
        }
    });

    fastify.get('/updatetemplate', { preValidation: [], schema: null }, async (req, reply) => {
        try {
            // const sql = 'select template_content from accounts.dlt_template_info_20211019 where template_id=?';
            const sql = 'select * from test.dlt_template_info where template_id=?';
            const sqlupdate = 'update test.dlt_template_info set template_content=? where template_id=?';
            const tobeupdated = [];
            const notfound = [];
            const totalupdated = 0;

            readXlsxFile('./boan_all_template_list.xlsx').then(async (rows) => {
                // `rows` is an array of rows
                // each row being an array of cells.
                console.log(rows[2130]);
                console.log();
                // const tcontent = _.replace(rows[560][1], /\\n/g, '\n');
                console.log(rows.length);
                // for await (const row of rows) {
                //     // console.log(row[0], row[1]);
                //     const templateid = _.replace(row[0], /["' ]/g, '');
                //     let templatecontent = _.trim(row[1]);
                //     templatecontent = _.replace(templatecontent, /\\n/g, '\n');
                //     templatecontent = _.replace(templatecontent, /\\r/g, '\r');
                //     templatecontent = _.replace(templatecontent, /\r\n/g, '\n');
                //     const result = await fastify.mariadb.query(sql, [templateid]);
                //     const dbresp = _.get(result, 0, {});
                //     if (!_.isEmpty(dbresp)) {
                //         const dbcontent = _.get(dbresp, 'template_content');
                //         if (!_.eq(dbcontent, templatecontent)) {
                //             tobeupdated.push(templateid);
                //             // console.log(templateid);
                //             const result1 = await fastify.mariadb.query(sqlupdate, [templatecontent, templateid]);
                //             console.log('content updated - ', templateid);
                //             totalupdated += 1;
                //         }
                //     } else {
                //         notfound.push(templateid);
                //     }
                // }
                // const tcontent = 'Dear {#var#} {#var#},\nYour appointment (Booking Id: {#var#}) with {#var#} {#var#} scheduled on {#var#}{#var#} is accepted. Please pay {#var#} using the below link\n{#var#}{#var#}{#var#}\nThe above link is valid for {#var#} minutes.\n\nThank you\nGleneagles Global Health City Chennai';
                // const result = await fastify.mariadb.query(sqlupdate, [tcontent, '1007162218765617561']);
                // const result1 = await fastify.mariadb.query(sql, ['1007162218765617561']);

                reply.send({ totalupdated, notfound, tobeupdated });
            });

            // const result = await fastify.mariadb.query(sql);
            // for await (const row of result) {
            //     const header = _.get(row, 'header');
            //     const tgid = _.get(row, 'template_group_id');
            //     const tid = _.get(row, 'template_id');
            //     let is_numeric = 1;
            //     if (_.isNaN(Number(header))) is_numeric = 0;
            //     console.log(header, is_numeric);
            //     const sql1 = 'update accounts.dlt_template_group_header_entity_map set is_numeric_header=? where template_group_id=? and template_id=? and header=?';
            //     const params = [is_numeric, tgid, tid, header];
            //     const result0 = await fastify.mariadb.query(sql1, params);
            // }
        } catch (err) {
            req.log.error(`error =>${err}`);
            return err;
        }
    });

    fastify.get('/readxl', { preValidation: [], schema: null }, async (req, reply) => {
        const reader = excel.createReader({ input: './boan_all_template_list.xlsx' });
        const r = [];
        let row = '';
        // eslint-disable-next-line no-cond-assign
        while (row = reader.readNext()) {
            // console.log(`Row #${reader.rowIndex + 1}: `, row);
            r.push(row);
        }
        // const rowobj = r[2623];
        const rowobj = r[560];
        const sqlupdate = 'update test.dlt_template_info set template_content=? where template_id=?';
        // const result1 = await fastify.mariadb.query(sqlupdate, [rowobj[1], rowobj[0]]);

        reply.send(rowobj);
    });

    fastify.get('/updateIsNumeric', { preValidation: [], schema: null }, async (req, reply) => {
        try {
            const sql = 'select * from accounts.dlt_template_group_header_entity_map';

            const result = await fastify.mariadb.query(sql);
            for await (const row of result) {
                const header = _.get(row, 'header');
                const tgid = _.get(row, 'template_group_id');
                const tid = _.get(row, 'template_id');
                let is_numeric = 1;
                if (_.isNaN(Number(header))) is_numeric = 0;
                console.log(header, is_numeric);
                const sql1 = 'update accounts.dlt_template_group_header_entity_map set is_numeric_header=? where template_group_id=? and template_id=? and header=?';
                const params = [is_numeric, tgid, tid, header];
                const result0 = await fastify.mariadb.query(sql1, params);
            }

            reply.send('done');
        } catch (err) {
            req.log.error(`error =>${err}`);
            return err;
        }
    });
}

module.exports = test;
