/* eslint-disable camelcase */
const fp = require('fastify-plugin');
const _ = require('lodash');

const testdb = async (fastify, opts) => {
    fastify.decorate('createSAAccount', async (reqObj) => {
        const con = await fastify.mariadb.getConnection();
        const { newCliId, su_id, loggedin_cli_id, bill_type, user_type, firstname, lastname, company, username, ui_pass, api_pass, smpp_pass, email, mobile, zone_name, offset, country_code_iso3, allocated_tgroup_ids, assigned_tgroup_id, smpp_charset, address, twofa_yn, wallet, smsrate, dltrate, newline_chars, encrypt_mobile_yn, encrypt_message_yn, vl_shortner, servicesValuesArr, tgValuesArr, assignedGroupsValuesArr, walletTransValuesArr } = reqObj;
        let r1 = {};
        const r2 = {};

        try {
            let bill_encrypt_type = 0;
            if (+encrypt_mobile_yn === 1 && +encrypt_message_yn === 1) {
                bill_encrypt_type = 3;
            } else if (+encrypt_mobile_yn === 1) {
                bill_encrypt_type = 2;
            } else if (+encrypt_message_yn === 1) {
                bill_encrypt_type = 1;
            } else {
                bill_encrypt_type = 0;
            }

            const sasql = 'insert into accounts.su_config (cli_id, bill_type, time_zone, time_offset) values (?,?,?,?)';
            const saparams = [newCliId, bill_type, zone_name, offset];

            const sql = `insert into accounts.user_config (cli_id, firstname, lastname, email, mobile, company, address, user, ui_pass, api_pass, smpp_pass, user_type, pu_id, su_id, newline_replace_char,  bill_encrypt_type, smpp_charset, dlt_templ_grp_id, time_zone, time_offset, two_level_auth, vl_shortner, created_user, sms_rate, dlt_rate)
                values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)`;
            const params = [newCliId, firstname, lastname, email, mobile, company, address, username, ui_pass, api_pass, smpp_pass, user_type, loggedin_cli_id, su_id, newline_chars, bill_encrypt_type, smpp_charset, assigned_tgroup_id, zone_name, offset, twofa_yn, vl_shortner, loggedin_cli_id, smsrate, dltrate];

            const serviceSQL = 'insert into accounts.user_service_map (cli_id, service, sub_service, created_user) values (?,?,?,?)';
            const tgSQL = 'insert into accounts.users_templategroup_ids (cli_id, template_group_id, created_user) values (?,?,?)';
            const usergroupSQL = 'insert into cm.group_user_mapping (id, cli_id, g_id, assigned_by) values (?,?,?,?)';
            const walletTransSQL = 'insert into cm.wallet_transactions (id, cli_id, username, action, amount, source, description, sessionid, done_by, done_by_username) values (?,?,?,?,?,?,?,?,?,?)';

            con.beginTransaction();

            console.log('createAccount() [su_config] sql & params => ', sasql, saparams);
            // insert to user_config
            const r0 = await con.query(sasql, saparams);

            console.log('createAccount() [user_config] sql & params => ', sql, params);
            // insert to user_config
            r1 = await con.query(sql, params);

            if (!_.isEmpty(tgValuesArr)) {
                console.log('createAccount() [users_templategroup_ids] sql & params => ', tgSQL, tgValuesArr);
                const r2 = await con.batch(tgSQL, tgValuesArr);
            }

            if (!_.isEmpty(assignedGroupsValuesArr)) {
                console.log('createAccount() [group_user_mapping] sql & params => ', usergroupSQL, assignedGroupsValuesArr);
                const r2 = await con.batch(usergroupSQL, assignedGroupsValuesArr);
            }

            if (!_.isEmpty(servicesValuesArr)) {
                console.log('createAccount() [user_service_map] sql & params => ', serviceSQL, servicesValuesArr);
                const r2 = await con.batch(serviceSQL, servicesValuesArr);
            }
            // insert to wallet transaction
            if (+wallet > 0 && (+bill_type === 1)) {
                console.log('createAccount() [wallet_transactions] sql & params => ', walletTransSQL, walletTransValuesArr);
                const r2 = await con.query(walletTransSQL, walletTransValuesArr);
            }

            con.commit();
        } catch (e) {
            con.rollback();
            console.error('con rollback', e);
            throw e;
        } finally {
            await con.release();
        }
        return { m: 'sucess' };
    });

    fastify.decorate('insertToDLTTemplateGroup', async (tgValuesArr) => {
        const con = await fastify.mariadb.getConnection();

        try {
            const sql = 'insert into accounts.dlt_template_group (template_group_id, template_group_name) values (?,?)';

            con.beginTransaction();

            if (!_.isEmpty(tgValuesArr)) {
                // console.log('createAccount() [users_templategroup_ids] sql & params => ', sql, tgValuesArr);
                const r = await con.batch(sql, tgValuesArr);
            }

            con.commit();
        } catch (e) {
            con.rollback();
            console.error('con rollback', e);
            throw e;
        } finally {
            await con.release();
        }
        return { m: 'sucess' };
    });

    fastify.decorate('insertToUsersTemplateGroupMap', async (satgValuesArr) => {
        const con = await fastify.mariadb.getConnection();

        try {
            const sql = 'insert into accounts.users_templategroup_ids (cli_id, template_group_id, created_user) values (?,?,?)';

            con.beginTransaction();

            if (!_.isEmpty(satgValuesArr)) {
                // console.log('createAccount() [users_templategroup_ids] sql & params => ', sql, tgValuesArr);
                const r = await con.batch(sql, satgValuesArr);
            }

            con.commit();
        } catch (e) {
            con.rollback();
            console.error('con rollback', e);
            throw e;
        } finally {
            await con.release();
        }
        return { m: 'sucess' };
    });

    fastify.decorate('insertToServicesMap', async (servicesValuesArr) => {
        const con = await fastify.mariadb.getConnection();

        try {
            const sql = 'insert into accounts.user_service_map (cli_id, service, sub_service, created_user) values (?,?,?,?)';

            con.beginTransaction();

            if (!_.isEmpty(servicesValuesArr)) {
                // console.log('createAccount() [users_templategroup_ids] sql & params => ', sql, servicesValuesArr);
                const r = await con.batch(sql, servicesValuesArr);
            }

            con.commit();
        } catch (e) {
            con.rollback();
            console.error('con rollback', e);
            throw e;
        } finally {
            await con.release();
        }
        return { m: 'sucess' };
    });

    fastify.decorate('insertToDLT', async (valuesArr) => {
        const con = await fastify.mariadb.getConnection();

        try {
            const sql = 'insert into cm.xls_user_dlt_templates_all_06oct21 (dlt_req_uid, username, entity_id, template_id, consent_type, header_reg_id, senderid, register_at_operator, telemarketer, senderid_reg_date, template_msg, template_sample_msg, senderid_req_date, approval_status, approved_on, remarks, rodt, if_deleted) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)';

            con.beginTransaction();

            if (!_.isEmpty(valuesArr)) {
                // console.log('createAccount() [users_templategroup_ids] sql & params => ', sql, servicesValuesArr);
                const r = await con.batch(sql, valuesArr);
            }

            con.commit();
        } catch (e) {
            con.rollback();
            console.error('con rollback', e);
            throw e;
        } finally {
            await con.release();
        }
        return { m: 'sucess' };
    });

    fastify.decorate('update', async (username, tgid, cli_id) => {
        const sql = 'update xls_user_dlt_templates_all_06oct21 set cli_id=?, tg_id=? where username=?';
        const params = [cli_id, tgid, username];
        console.log('update() sql & params => ', sql, params);

        const result = await fastify.mariadb.query(sql, params);
        return result;
    });
};

module.exports = fp(testdb);
