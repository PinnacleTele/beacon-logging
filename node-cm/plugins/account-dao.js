/* eslint-disable camelcase */
const fp = require('fastify-plugin');
const _ = require('lodash');

const accountdb = async (fastify, opts) => {
    fastify.decorate('getLogoPathForId', async (cli_id) => {
        const sql = 'select * from cm.config_user_whitelabel where cli_id=?';
        const result = await fastify.mariadb.query(sql, [cli_id]);

        return result;
    });

    fastify.decorate('isUsernameFound', async (uname) => {
        const result = await fastify.mariadb.query('select count(1) as counts from accounts.accounts_view where lower(user)=?', [_.toLower(uname)]);
        return result;
    });

    fastify.decorate('findUser', async (user) => {
        const result = await fastify.mariadb.query('select * from accounts.accounts_view where lower(user)=?', [_.toLower(user)]);
        return result;
    });

    fastify.decorate('findUserById', async (cli_id) => {
        const result = await fastify.mariadb.query('select * from accounts.accounts_view where cli_id=?', [cli_id]);
        return result;
    });

    fastify.decorate('getAllUsersForId', async (cli_id) => {
        const result = await fastify.mariadb.query('select * from accounts.accounts_view where pu_id=? and cli_id !=? order by user', [cli_id, cli_id]);
        return result;
    });

    fastify.decorate('getSMPPConfigForId', async (cli_id) => {
        const result = await fastify.mariadb.query('select * from accounts.user_smpp_config where cli_id =?', [cli_id]);
        return result;
    });

    fastify.decorate('getAllUsersInfoForIds', async (cli_id_arr) => {
        const result = await fastify.mariadb.query('select * from accounts.accounts_view where cli_id in(?)', [cli_id_arr]);
        return result;
    });

    fastify.decorate('getUserCountsByType', async (cli_id) => {
        const result = await fastify.mariadb.query('select user_type, count(1) total from accounts.accounts_view where pu_id=? and cli_id !=? group by user_type', [cli_id, cli_id]);
        return result;
    });

    fastify.decorate('getAccountStatsForId', async (cli_id) => {
        const result = await fastify.mariadb.query('select count(*) total, user_type, acc_status from accounts.accounts_view where pu_id=? and cli_id !=? group by  user_type,acc_status', [cli_id, cli_id]);
        return result;
    });

    fastify.decorate('updateAccountStatus', async (loggedin_cli_id, cli_id, newstatus) => {
        const sql = 'update accounts.user_config set acc_status=?, updated_ts=now(), modified_user=? where cli_id=? ';
        const params = [newstatus, loggedin_cli_id, cli_id];
        fastify.log.debug('updateAccountStatus() [user_config] sql & params => ', sql, params);

        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('createAccount', async (reqObj) => {
        const con = await fastify.mariadb.getConnection();
        const { newCliId, su_id, loggedin_cli_id, bill_type, user_type, firstname, lastname, company, username, ui_pass, api_pass, smpp_pass, email, mobile, zone_name, offset, country_code_iso3, allocated_tgroup_ids, assigned_tgroup_id, smpp_charset, address, twofa_yn, wallet, smsrate, dltrate, newline_chars, encrypt_mobile_yn, encrypt_message_yn, vl_shortner, servicesValuesArr, tgValuesArr, assignedGroupsValuesArr, walletTransValuesArr, billing_currency, intlRatesValuesArr, platform_cluster,
            considerdefaultlength_as_domestic, is_16bit_udh, ip_validation, ip_list, full_message, camp_name_auto_gen, subusers_reports, acc_type,
            expiry_date, inactive_login } = reqObj;
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

            const sql = `insert into accounts.user_config (cli_id, firstname, lastname, email, mobile, company, address, user, ui_pass, api_pass, smpp_pass, user_type, pu_id, su_id, newline_replace_char,  bill_encrypt_type, dlt_templ_grp_id, time_zone, time_offset, two_level_auth, vl_shortner, created_user, base_sms_rate, base_add_fixed_rate, billing_currency, platform_cluster, 
                considerdefaultlength_as_domestic, is_16bit_udh, ip_validation, ip_list, acc_type, expiry_date)
                values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)`;
            const params = [newCliId, firstname, lastname, email, mobile, company, address, username, ui_pass, api_pass, smpp_pass, user_type, loggedin_cli_id, su_id, newline_chars, bill_encrypt_type, assigned_tgroup_id, zone_name, offset, twofa_yn, vl_shortner, loggedin_cli_id, smsrate, dltrate, billing_currency, platform_cluster, considerdefaultlength_as_domestic, is_16bit_udh, ip_validation, ip_list, acc_type, expiry_date];

            const smppSQL = 'insert into accounts.user_smpp_config (cli_id, charset) values (?,?)';
            const serviceSQL = 'insert into accounts.user_service_map (cli_id, service, sub_service, created_user) values (?,?,?,?)';
            const tgSQL = 'insert into accounts.users_templategroup_ids (cli_id, template_group_id, created_user) values (?,?,?)';
            const usergroupSQL = 'insert into cm.group_user_mapping (id, cli_id, g_id, assigned_by) values (?,?,?,?)';
            const intlRatesSQL = 'insert into configuration.client_intl_rates (cli_id, country, base_sms_rate) values (?,?,?)';
            // const walletTransSQL = 'insert into cm.wallet_transactions (id, cli_id, username, action, amount, source, description, sessionid, done_by, done_by_username) values (?,?,?,?,?,?,?,?,?,?)';
            const senderidsSQL = 'insert into accounts.user_headers (cli_id, header, entity_id, created_user) values (?,?,?,?)';

            con.beginTransaction();

            fastify.log.debug('createAccount() [user_config] sql & params => ', sql, params);
            // insert to user_config
            r1 = await con.query(sql, params);

            if (!_.isEmpty(tgValuesArr)) {
                fastify.log.debug('createAccount() [users_templategroup_ids] sql & params => ', tgSQL, tgValuesArr);
                const r2 = await con.batch(tgSQL, tgValuesArr);
            }
            // insert if smpp service is enabled
            if (!_.isEmpty(smpp_pass)) {
                fastify.log.debug('createAccount() [user_smpp_config] sql & params => ', smppSQL, [newCliId, smpp_charset]);
                const r2 = await con.query(smppSQL, [newCliId, smpp_charset]);
            }

            if (!_.isEmpty(assignedGroupsValuesArr)) {
                fastify.log.debug('createAccount() [group_user_mapping] sql & params => ', usergroupSQL, assignedGroupsValuesArr);
                const r2 = await con.batch(usergroupSQL, assignedGroupsValuesArr);
            }

            if (!_.isEmpty(servicesValuesArr)) {
                fastify.log.debug('createAccount() [user_service_map] sql & params => ', serviceSQL, servicesValuesArr);
                const r2 = await con.batch(serviceSQL, servicesValuesArr);
            }

            if (intlRatesValuesArr.length > 0) {
                fastify.log.debug('createAccount() [client_intl_rates] sql & params => ', intlRatesSQL, intlRatesValuesArr);
                const r2 = await con.batch(intlRatesSQL, intlRatesValuesArr);
            }
            // insert to wallet transaction
            // if (+wallet > 0 && (+bill_type === 1)) {
            //     console.log('createAccount() [wallet_transactions] sql & params => ', walletTransSQL, walletTransValuesArr);
            //     const r2 = await con.query(walletTransSQL, walletTransValuesArr);
            // }

            const userConfigsSQL = 'insert into cm.user_configs (cli_id,auto_gen_cname,subusers_reports,full_message,inactive_account_login) values (?,?,?,?,?)';
            fastify.log.debug('createAccount() [cm.user_configs] sql & params => ', userConfigsSQL, [newCliId, camp_name_auto_gen, subusers_reports, full_message, inactive_login]);
            await con.query(userConfigsSQL, [newCliId, camp_name_auto_gen, subusers_reports, full_message, inactive_login]);

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

    fastify.decorate('persistWalletTransaction', async (walletTransValuesArr) => {
        const con = await fastify.mariadb.getConnection();

        try {
            const walletTransSQL = 'insert into cm.wallet_transactions (id, cli_id, username, action, amount, source, description, sessionid, done_by, done_by_username,new_bal,old_bal,loggedin_bal_before, loggedin_bal_after) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)';
            console.log('createAccount() [wallet_transactions] sql & params => ', walletTransSQL, walletTransValuesArr);
            const r2 = await con.batch(walletTransSQL, walletTransValuesArr);
            con.commit();
        } catch (e) {
            con.rollback();
            console.error('con rollback', e);
            throw e;
        } finally {
            await con.release();
        }
        const result = await fastify.mariadb.query('select * from configuration.country_info');
        return result;
    });

    fastify.decorate('getCountries', async () => {
        const sql = 'select * from configuration.country_info ci  where (TRIM(country) != \'\' or country != null) order by country';

        const result = await fastify.mariadb.query(sql);
        return result;
    });

    fastify.decorate('getTemplateGroups', async (cli_id) => {
        const sql = 'select utm.template_group_id, dtg.template_group_name from accounts.dlt_template_group dtg, accounts.users_templategroup_ids utm where dtg.template_group_id=utm.template_group_id and cli_id=?';
        const result = await fastify.mariadb.query(sql, [cli_id]);
        return result;
    });

    fastify.decorate('getAssignedSubServices', async (cli_id) => {
        const sql = 'select ss.sub_service_name, ss.sub_service, ss.service, ss.sub_service_desc from accounts.sub_service ss, accounts.user_service_map usm where usm.sub_service=ss.sub_service and ss.is_active=1 and usm.cli_id=?';
        const result = await fastify.mariadb.query(sql, [cli_id]);
        return result;
    });

    fastify.decorate('hasInternationalService', async (cli_id) => {
        const sql = 'select * from accounts.user_service_map where cli_id=? and lower(sub_service)=?';
        const result = await fastify.mariadb.query(sql, [cli_id, 'international']);
        return result.length > 0;
    });

    fastify.decorate('updateAccountInfo', async (cli_id, firstname, lastname, company, address) => {
        const sql = 'update accounts.user_config set firstname=?, lastname=?, company=?, address=?, updated_ts=? where cli_id=?';
        const params = [firstname, lastname, company, address, new Date(), cli_id];

        console.log('updateAccountInfo() sql params => ', sql, params);
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('getUserIdForTemplateGroupId', async (tgrpid) => {
        const sql = 'select cli_id from accounts.accounts_view where dlt_templ_grp_id=?';
        const params = [tgrpid];

        console.log('getUserIdForTemplateGroupId() sql params => ', sql, params);
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('updateAccountRates', async (cli_id, smsrate, dltrate, smsrate_old, dltrate_old, req_id, user, loggedin_cli_id, billing_currency) => {
        const con = await fastify.mariadb.getConnection();
        const sql = 'update accounts.user_config set base_sms_rate=?, base_add_fixed_rate=?, updated_ts=? where cli_id=?';
        const sql2 = 'insert into cm.billing_rate_changes (id, req_id, cli_id, username, country, new_sms_rate, old_sms_rate, new_addl_rate, old_addl_rate, done_by, billing_currency) values (?,?,?,?,?,?,?,?,?,?,?)';
        const params = [smsrate, dltrate, new Date(), cli_id];
        const params2 = [fastify.nanoid(), req_id, cli_id, user, 'India', smsrate, smsrate_old, dltrate, dltrate_old, loggedin_cli_id, billing_currency];
        let result1 = null;
        try {
            con.beginTransaction();

            console.log('updateAccountRates() [user_config] sql params => ', sql, params);
            result1 = await con.query(sql, params);

            console.log('updateAccountRates() [billing_rate_changes] sql params => ', sql2, params2);
            const result2 = await con.query(sql2, params2);

            con.commit();
        } catch (e) {
            con.rollback();
            console.error('con rollback', e);
            throw e;
        } finally {
            await con.release();
        }
        return result1;
    });

    fastify.decorate('updateAccountEncryption', async (cli_id, loggedin_cli_id, encrypt_mobile_yn, encrypt_message_yn) => {
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

        const sql = 'update accounts.user_config set bill_encrypt_type=?, updated_ts=?, modified_user=? where cli_id=?';
        const params = [bill_encrypt_type, new Date(), loggedin_cli_id, cli_id];

        console.log('updateAccountEncryption() sql params => ', sql, params);
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('updateAccountTGroups', async (cli_id, assigned_tgroup_id, tgValuesArr, userType) => {
        const con = await fastify.mariadb.getConnection();

        try {
            const sql1 = 'update accounts.user_config set dlt_templ_grp_id=?, updated_ts=? where cli_id=?';
            const tgSQL1 = 'delete from accounts.users_templategroup_ids where cli_id=?';
            const tgSQL2 = 'insert into accounts.users_templategroup_ids (cli_id, template_group_id, created_user) values (?,?,?)';

            const params = [assigned_tgroup_id, new Date(), cli_id];

            con.beginTransaction();

            console.log('updateAccountTGroups() [user_config] sql & params => ', sql1, params);
            // update to user_config
            const r1 = await con.query(sql1, params);

            // run below only if the user type are Admin/SA
            if (+userType != 2) {
                console.log('updateAccountTGroups() [users_templategroup_ids] deleting existing tgroups sql & params => ', tgSQL1, [cli_id]);
                // delete tgroups
                const r3 = await con.query(tgSQL1, [cli_id]);

                if (!_.isEmpty(tgValuesArr)) {
                    console.log('updateAccountTGroups() [users_templategroup_ids] adding tgroups sql & params => ', tgSQL2, tgValuesArr);
                    const r2 = await con.batch(tgSQL2, tgValuesArr);
                }
            }
            con.commit();
        } catch (e) {
            con.rollback();
            console.error('con rollback', e);
            throw e;
        } finally {
            await con.release();
        }
        return true;
    });

    fastify.decorate('updateAccount2FA', async (cli_id, twofa_yn) => {
        const sql = 'update accounts.user_config set two_level_auth=?, updated_ts=? where cli_id=?';
        const params = [twofa_yn, new Date(), cli_id];

        console.log('updateAccount2FA() sql params => ', sql, params);
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('updateAccountPassword', async (cli_id, newpass) => {
        const sql = 'update accounts.user_config set ui_pass=?, updated_ts=? where cli_id=?';
        const params = [newpass, new Date(), cli_id];

        console.log('updateAccountPassword() sql params => ', sql, params);
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('updateAccountMS', async (req_obj) => {
        const con = await fastify.mariadb.getConnection();
        const {
            cli_id, zone_name, offset, country_code_iso3, newline_chars, loggedin_cli_id,
            is_16bit_udh, ip_validation, full_message, camp_name_auto_gen, ip_list, subusers_reports, expiry_date, inactive_login
        } = req_obj;
        let result;
        try {
            const sql = `update accounts.user_config set time_zone=?, time_offset=?, newline_replace_char=?, updated_ts=?,modified_user=?,
            is_16bit_udh=?, ip_validation=?, ip_list=?, expiry_date=? where cli_id=?`;
            const params = [zone_name, offset, newline_chars, new Date(), loggedin_cli_id, is_16bit_udh, ip_validation, ip_list, expiry_date, cli_id];
            fastify.log.debug('updateAccountMS() [accounts.user_config] sql params => ', sql, params);

            const user_configs_sql = `insert into cm.user_configs (cli_id,auto_gen_cname,subusers_reports,full_message, inactive_account_login) values (?,?,?,?,?) ON DUPLICATE KEY 
            update auto_gen_cname=values(auto_gen_cname), subusers_reports=values(subusers_reports), full_message=values(full_message), inactive_account_login=values(inactive_account_login)`;
            fastify.log.debug('updateAccountMS() [cm.user_configs] sql & params => ', user_configs_sql, [cli_id, camp_name_auto_gen, subusers_reports, full_message, inactive_login]);
            
            con.beginTransaction();
            result = await con.query(sql, params);
            result = await con.query(user_configs_sql, [cli_id, camp_name_auto_gen, subusers_reports, full_message, inactive_login]);
            con.commit();
        } catch (error) {
            con.rollback();
            fastify.log.error('con rollback', error);
            throw e;
        } finally {
            await con.release();
        }
        return result;
    });

    fastify.decorate('updateAccountGroups', async (cli_id, assignedGroupsValuesArr) => {
        const con = await fastify.mariadb.getConnection();

        try {
            const sql1 = 'delete from cm.group_user_mapping where cli_id=?';
            const sql2 = 'insert into cm.group_user_mapping (id, cli_id, g_id, assigned_by, modified_ts) values (?,?,?,?,?)';

            con.beginTransaction();

            console.log('updateAccountGroups() [group_user_mapping] deleting existing mapping, sql & params => ', sql1, cli_id);
            const r1 = await con.query(sql1, [cli_id]);

            if (!_.isEmpty(assignedGroupsValuesArr)) {
                console.log('updateAccountTGroups() [users_templategroup_ids] adding new shared group mapping, sql & params => ', sql2, assignedGroupsValuesArr);
                const r2 = await con.batch(sql2, assignedGroupsValuesArr);
            }
            con.commit();
        } catch (e) {
            con.rollback();
            console.error('con rollback', e);
            throw e;
        } finally {
            await con.release();
        }
        return true;
    });

    fastify.decorate('updateAccountServices', async (cli_id, servicesValuesArr, smpp_charset, loggedin_cli_id, smpppass, apipass, cmpass, converted_row_rate, considerdefaultlength_as_domestic) => {
        const con = await fastify.mariadb.getConnection();

        try {
            const sql1 = 'delete from accounts.user_service_map where cli_id=?';
            const sql2 = 'insert into accounts.user_service_map (cli_id, service, sub_service, created_user, updated_ts, modified_user) values (?,?,?,?,?,?)';
            let sql3 = 'update accounts.user_config set updated_ts=?, considerdefaultlength_as_domestic=?, modified_user=? ';
            const intlRatesSQL = 'insert into configuration.client_intl_rates (cli_id, country, base_sms_rate) values (?,?,?)';
            const smppSQL = 'insert into accounts.user_smpp_config (cli_id, charset) values (?,?)';
            const smppUpdateSQL = 'update accounts.user_smpp_config set charset=? where cli_id=?';
            const smppSelectSQL = 'select * from accounts.user_smpp_config where cli_id=?';

            if (!_.isEmpty(cmpass)) {
                sql3 = `${sql3}, ui_pass='${cmpass}'`;
            }
            if (!_.isEmpty(apipass)) {
                sql3 = `${sql3}, api_pass='${apipass}'`;
            }
            if (!_.isEmpty(smpppass)) {
                sql3 = `${sql3}, smpp_pass='${smpppass}'`;
            }
            sql3 = `${sql3} where cli_id=?`;

            con.beginTransaction();

            console.log('updateAccountServices() [user_service_map] deleting existing mapping, sql & params => ', sql1, cli_id);
            const r1 = await con.query(sql1, [cli_id]);

            if (!_.isEmpty(servicesValuesArr)) {
                console.log('updateAccountServices() [user_service_map] inserting services, sql & params => ', sql2, servicesValuesArr);
                const r2 = await con.batch(sql2, servicesValuesArr);
            }
            // update user_smpp_config
            if (!_.isEmpty(smpppass)) {
                // check if record exists for this client
                const r13 = await con.query(smppSelectSQL, [cli_id]);

                if (r13.length > 0) {
                    // has service. update smpp charset
                    console.log('updateAccountServices() [user_smpp_config] updating smpp charset, sql & params => ', smppUpdateSQL, [smpp_charset, cli_id]);
                    const r2 = await con.query(smppUpdateSQL, [smpp_charset, cli_id]);
                } else {
                    // no record. insert smpp charset
                    console.log('updateAccountServices() [user_smpp_config] inserting smpp charset, sql & params => ', smppSQL, [cli_id, smpp_charset]);
                    const r2 = await con.query(smppSQL, [cli_id, smpp_charset]);
                }
            } else if (!_.isEmpty(smpp_charset)) {
                console.log('updateAccountServices() [user_smpp_config] updating smpp charset, sql & params => ', smppUpdateSQL, [smpp_charset, cli_id]);
                const r2 = await con.query(smppUpdateSQL, [smpp_charset, cli_id]);
            }

            console.log('updateAccountServices() [user_config] sql & params => ', sql3, [new Date(), loggedin_cli_id, cli_id]);
            const r3 = await con.query(sql3, [new Date(), considerdefaultlength_as_domestic, loggedin_cli_id, cli_id]);

            if (converted_row_rate > 0) {
                console.log('updateAccountServices() [client_intl_rates] sql & params => ', intlRatesSQL, converted_row_rate);
                const r2 = await con.query(intlRatesSQL, [cli_id, 'ROW', converted_row_rate]);
            }

            con.commit();
        } catch (e) {
            con.rollback();
            console.error('con rollback', e);
            throw e;
        } finally {
            await con.release();
        }
        return true;
    });

    fastify.decorate('getConfiguredQuickLinks', async (cli_id) => {
        const sql = 'select * from cm.config_user_quicklinks where cli_id=?';
        console.log('getConfiguredQuickLinks() sql params => ', sql, [cli_id]);

        const result = await fastify.mariadb.query(sql, [cli_id]);
        return result;
    });

    fastify.decorate('updateQuicklinkSettings', async (cli_id, quicklinksValuesArr) => {
        const con = await fastify.mariadb.getConnection();

        try {
            const sql1 = 'delete from cm.config_user_quicklinks where cli_id=?';
            const sql2 = 'insert into cm.config_user_quicklinks (id, cli_id, username, quicklink, `group`, done_by) values (?,?,?,?,?,?)';

            con.beginTransaction();

            console.log('updateQuicklinkSettings() [config_user_quicklinks] deleting existing config, sql & params => ', sql1, [cli_id]);
            const r1 = await con.query(sql1, [cli_id]);

            if (!_.isEmpty(quicklinksValuesArr)) {
                console.log('updateQuicklinkSettings() [config_user_quicklinks] adding tgroups sql & params => ', sql2, quicklinksValuesArr);
                const r2 = await con.batch(sql2, quicklinksValuesArr);
            }
            con.commit();
        } catch (e) {
            con.rollback();
            console.error('con rollback', e);
            throw e;
        } finally {
            await con.release();
        }
        return true;
    });

    fastify.decorate('deleteQuicklinkSettings', async (cli_id) => {
        const sql = 'delete from cm.config_user_quicklinks where cli_id=?';
        console.log('deleteQuicklinkSettings() sql params => ', sql, [cli_id]);

        const result = await fastify.mariadb.query(sql, [cli_id]);
        return result;
    });

    fastify.decorate('getWalletTransactions', async (cli_id, fromdatetimeStrInIST, todatetimeStrInIST) => {
        const sql = 'select * from cm.wallet_transactions where done_by=? and created_ts between ? and ? order by created_ts desc';
        const params = [cli_id, fromdatetimeStrInIST, todatetimeStrInIST];

        console.log('getWalletTransactions() sql params => ', sql, params);
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('getLoggedinUserWalletTransactions', async (cli_id, fromdatetimeStrInIST, todatetimeStrInIST) => {
        const sql = 'select * from cm.wallet_transactions where cli_id=? and created_ts between ? and ? order by created_ts desc';
        const params = [cli_id, fromdatetimeStrInIST, todatetimeStrInIST];

        console.log('getLoggedinUserWalletTransactions() sql params => ', sql, params);
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('getBlacklistedLinksForCliId', async (cli_id) => {
        const sql = 'select * from cm.cm_features_blacklist where cli_id =?';
        fastify.log.debug(` getBlacklistedLinksForCliId sql params => ${sql}   ${cli_id} `);
        const result = await fastify.mariadb.query(sql, [cli_id]);
        fastify.log.debug(` getBlacklistedLinksForCliId result => ${JSON.stringify(result)} `);
        return result;
    });
};

module.exports = fp(accountdb);
