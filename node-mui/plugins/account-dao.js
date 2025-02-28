const fp = require('fastify-plugin');
const _ = require('lodash');

const accountDAO = async (fastify) => {
    fastify.decorate('checkUnameUniqueness', async (uname) => {
        const result = await fastify.mariadb.query('select count(1) as counts from accounts.user_config where lower(user)=?', [_.toLower(uname)]);
        return result;
    });

    fastify.decorate('getAllDLTGroups', async () => {
        const sql = 'select dtg.template_group_id, dtg.template_group_name,dtg.created_ts from accounts.dlt_template_group dtg order by template_group_name';
        const result = await fastify.mariadb.query(sql);
        return result;
    });

    fastify.decorate('getAllocTemplateGroups', async (cli_id) => {
        const sql = 'select utm.template_group_id, dtg.template_group_name from accounts.dlt_template_group dtg, accounts.users_templategroup_ids utm where dtg.template_group_id=utm.template_group_id and cli_id=?';
        const result = await fastify.mariadb.query(sql, [cli_id]);
        return result;
    });

    fastify.decorate('getAllSubServices', async () => {
        const sql = 'select ss.sub_service_name, ss.sub_service, ss.service, ss.sub_service_desc from accounts.sub_service ss where is_active = 1';
        const result = await fastify.mariadb.query(sql);
        return result;
    });

    fastify.decorate('getAssignedSubServices', async (cli_id) => {
        const sql = 'select ss.sub_service_name, ss.sub_service, ss.service, ss.sub_service_desc from accounts.sub_service ss, accounts.user_service_map usm where usm.sub_service=ss.sub_service and ss.is_active=1 and usm.cli_id=?';
        const result = await fastify.mariadb.query(sql, [cli_id]);
        return result;
    });

    fastify.decorate('getUserCountsByType', async (cli_id) => {
        const result = await fastify.mariadb.query('select user_type, count(1) total from accounts.accounts_view where pu_id=? and cli_id !=? group by user_type', [cli_id, cli_id]);
        return result;
    });

    /* fastify.decorate('getUserCountsByType', async (cli_id) => {
        const result = await fastify.mariadb.query('select count(1) total, user_type from accounts.user_config where created_user =? group by  user_type', [cli_id]);
        return result;
    }); */

    fastify.decorate('getParentUser', async (cli_id) => {
        const result = await fastify.mariadb.query('select * from accounts.su_config where cli_id !=? ', [cli_id, cli_id]);
        return result;
    });

    fastify.decorate('createAccount', async (reqObj) => {
        const con = await fastify.mariadb.getConnection();
        const {
            newCliId, loggedin_cli_id, bill_type, message_type, platform_cluster, user_type, firstname, lastname, company, username,
            ui_pass, api_pass, smpp_pass, email, mobile, zone_name, offset, conv_type, assigned_tgroup_id, bind_type, max_allowed_connections,
            throttle_limit, smpp_charset, dlt_entityid_tag, dlt_templateid_tag, address, smsrate, dltrate, newline_chars, vl_shortner,
            servicesValuesArr, tgValuesArr, billing_currency, intlRatesValuesArr, services, sms_priority, sms_retry,
            bill_encrypt_type, trai_blockout, domestic_sms_blockout, dnd_chk, blklist_chk, spam_chk, dup_chk_req, intl_sms_blockout,
            optin_chk_req, sales_id, domestic_sms_blockout_start, domestic_sms_blockout_stop, dup_chk_interval, intl_sms_blockout_start, intl_sms_blockout_stop,
            domestic_special_series_allow, req_hex_msg, acc_type, invoice_based_on, is_ildo, cli_mid_tag, considerdefaultlength_as_domestic, is_16bit_udh,
            ip_validation, ip_list, full_message, camp_name_auto_gen, subusers_reports,
            two_level_auth, mt_adjust, dn_adjust, dnd_reject_yn, msg_replace_chk, is_schedule_allow,
            uc_iden_allow, is_remove_uc_chars, url_smartlink_enable, url_track_enabled, is_async, use_default_header, use_default_on_header_fail,
            domestic_tra_blockout_reject, timebound_chk_enable, force_dnd_check, msg_retry_available, capping_chk_enable, uc_iden_char_len, uc_iden_occur,
            vl_shortcode_len, acc_default_header, timebound_interval, timebound_max_count_allow, dnd_pref, capping_interval_type,
            capping_interval, capping_max_count_allow, acc_route_id, credit_check, credit_limit, dn_date_format, expiry_date, inactive_login
        } = reqObj;

        const acc_status = 0;

        try {
            con.beginTransaction();

            // insert to su_config
            const su_config_sql = `insert into accounts.su_config (cli_id, bill_type, msg_type, acc_status, platform_cluster, sms_priority, 
                sms_retry_available, newline_replace_char, is_16bit_udh, bill_encrypt_type, dlt_templ_grp_id, dnd_chk,
                dup_chk_req, dup_chk_interval, time_zone, time_offset, two_level_auth, mt_adjust, dn_adjust, optin_chk_req, sales_id,
                credit_check, acc_route_id, credit_limit, created_ts, created_user) 
                values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, current_timestamp(), ?)`;

            const su_config_params = [newCliId, bill_type, message_type, acc_status, platform_cluster, sms_priority, sms_retry, newline_chars, is_16bit_udh,
                bill_encrypt_type, assigned_tgroup_id, dnd_chk, dup_chk_req, dup_chk_interval, zone_name, offset,
                two_level_auth, mt_adjust, dn_adjust, optin_chk_req, sales_id, credit_check, acc_route_id, credit_limit, loggedin_cli_id];

            fastify.log.debug('createAccount() [su_config] sql & params => ', su_config_sql, su_config_params);
            await con.query(su_config_sql, su_config_params);

            // insert to user_config
            const sql = `insert into accounts.user_config (cli_id, firstname, lastname, email, mobile, company, address, user, 
                ui_pass, api_pass, smpp_pass, user_type, pu_id, su_id, acc_status, platform_cluster, sms_priority, newline_replace_char, 
                is_16bit_udh, bill_encrypt_type, domestic_promo_trai_blockout_purge, domestic_sms_blockout, domestic_sms_blockout_start, domestic_sms_blockout_stop,
                dlt_templ_grp_id, dup_chk_req, dup_chk_interval, intl_sms_blockout, intl_sms_blockout_start, intl_sms_blockout_stop,
                time_zone, time_offset, two_level_auth, ip_validation, ip_list, mt_adjust, dn_adjust, dnd_reject_yn, vl_shortner,
                msg_replace_chk, is_schedule_allow, uc_iden_allow, uc_iden_char_len, uc_iden_occur, is_remove_uc_chars, url_smartlink_enable,
                url_track_enabled, vl_shortcode_len, is_async, use_default_header, use_default_on_header_fail, acc_default_header,
                considerdefaultlength_as_domestic, domestic_tra_blockout_reject, timebound_chk_enable, timebound_interval, timebound_max_count_allow,
                base_sms_rate, base_add_fixed_rate, domestic_special_series_allow, req_hex_msg, billing_currency, acc_type, invoice_based_on,
                billing_currency_conv_type, is_ildo, dnd_pref, force_dnd_check, msg_retry_available, capping_chk_enable, capping_interval_type,
                capping_interval, capping_max_count_allow, credit_check, acc_route_id, credit_limit, created_user, created_ts, expiry_date, blklist_chk, spam_chk) 
                values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,
                ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?, current_timestamp(),?,?,?)`;

            const params = [newCliId, firstname, lastname, email, mobile, company, address, username, ui_pass, api_pass, smpp_pass, user_type,
                newCliId, newCliId, acc_status, platform_cluster, sms_priority, newline_chars, is_16bit_udh, bill_encrypt_type, trai_blockout,
                domestic_sms_blockout, domestic_sms_blockout_start, domestic_sms_blockout_stop, assigned_tgroup_id, dup_chk_req, dup_chk_interval,
                intl_sms_blockout, intl_sms_blockout_start, intl_sms_blockout_stop, zone_name, offset, two_level_auth, ip_validation, ip_list,
                mt_adjust, dn_adjust, dnd_reject_yn, vl_shortner, msg_replace_chk, is_schedule_allow, uc_iden_allow, uc_iden_char_len, uc_iden_occur,
                is_remove_uc_chars, url_smartlink_enable, url_track_enabled, vl_shortcode_len, is_async, use_default_header, use_default_on_header_fail,
                acc_default_header, considerdefaultlength_as_domestic, domestic_tra_blockout_reject, timebound_chk_enable, timebound_interval, timebound_max_count_allow,
                smsrate, dltrate, domestic_special_series_allow, req_hex_msg, billing_currency, acc_type, invoice_based_on, conv_type, is_ildo,
                dnd_pref, force_dnd_check, msg_retry_available, capping_chk_enable, capping_interval_type, capping_interval, capping_max_count_allow,
                credit_check, acc_route_id, credit_limit, loggedin_cli_id, expiry_date, blklist_chk, spam_chk];

            fastify.log.debug('createAccount() [user_config] sql & params => ', sql, params);
            await con.query(sql, params);

            const subServices = _.map(services, (o) => _.toLower(_.trim(o.sub_service)));
            if (_.includes(subServices, 'smpp')) {
                // insert to user_smpp_config
                const user_smpp_config_sql = `insert into accounts.user_smpp_config (cli_id, bind_type, max_allowed_connections, max_speed, charset, dlt_entityid_tag, dlt_templateid_tag, dn_date_format, created_ts, cli_mid_tag)
                values(?, ?, ?, ?, ?, ?, ?, ?, current_timestamp(), ?)`;
                const user_smpp_config_params = [newCliId, bind_type, max_allowed_connections, throttle_limit, smpp_charset, dlt_entityid_tag, dlt_templateid_tag, dn_date_format, cli_mid_tag];
                fastify.log.debug('createAccount() [user_smpp_config] sql & params => ', user_smpp_config_sql, user_smpp_config_params);
                await con.query(user_smpp_config_sql, user_smpp_config_params);
            }

            if (!_.isEmpty(tgValuesArr)) {
                const tgSQL = 'insert into accounts.users_templategroup_ids (cli_id, template_group_id, created_user) values (?,?,?)';
                fastify.log.debug('createAccount() [users_templategroup_ids] sql & params => ', tgSQL, tgValuesArr);
                await con.batch(tgSQL, tgValuesArr);
            }

            if (!_.isEmpty(servicesValuesArr)) {
                const serviceSQL = 'insert into accounts.user_service_map (cli_id, service, sub_service, created_user) values (?,?,?,?)';
                fastify.log.debug('createAccount() [user_service_map] sql & params => ', serviceSQL, servicesValuesArr);
                await con.batch(serviceSQL, servicesValuesArr);
            }

            if (_.includes(subServices, 'international') && intlRatesValuesArr.length > 0) {
                const intlRatesSQL = 'insert into configuration.client_intl_rates (cli_id, country, base_sms_rate) values (?,?,?)';
                fastify.log.debug('createAccount() [client_intl_rates] sql & params => ', intlRatesSQL, intlRatesValuesArr);
                await con.batch(intlRatesSQL, intlRatesValuesArr);
            }

            const usrConfigSQL = 'insert into cm.user_configs (cli_id,auto_gen_cname,subusers_reports,full_message,inactive_account_login) values (?,?,?,?,?)';
            fastify.log.debug('createAccount() [user_configs] sql & params => ', usrConfigSQL, [newCliId, camp_name_auto_gen, subusers_reports, full_message, inactive_login]);
            await con.query(usrConfigSQL, [newCliId, camp_name_auto_gen, subusers_reports, full_message, inactive_login]);

            con.commit();
        } catch (e) {
            con.rollback();
            fastify.log.error('con rollback', e);
            throw e;
        } finally {
            await con.release();
        }
        return { m: 'sucess' };
    });

    fastify.decorate('findUserById', async (cli_id) => {
        const result = await fastify.mariadb.query('select * from accounts.accounts_view where cli_id=?', [cli_id]);
        return result;
    });

    fastify.decorate('getROWForAllUsers', async (cli_id_arr) => {
        const sql = 'select * from configuration.client_intl_rates where cli_id in(?) and upper(country)=\'ROW\'';
        const result = await fastify.mariadb.query(sql, [cli_id_arr]);
        return result;
    });

    fastify.decorate('getAllUsers', async () => {
        const result = await fastify.mariadb.query('select * from accounts.accounts_view order by user');
        return result;
    });

    fastify.decorate('getAccountStats', async () => {
        const result = await fastify.mariadb.query('select count(*) total, user_type, acc_status from accounts.accounts_view  group by  user_type,acc_status');
        return result;
    });

    fastify.decorate('getGroups', async (g_type, status) => {
        let sql = '';
        if (g_type === 'all' && status === 'all') {
            sql = 'select * from cm.group_master  order by created_ts desc ';
        }
        const result = await fastify.mariadb.query(sql);
        return result;
    });

    fastify.decorate('getTemplates', async () => {
        const result = await fastify.mariadb.query('select count(*) from cm.template_master cmt ');
        return result;
    });

    fastify.decorate('updateAccountProfileInfo', async (cli_id, firstname, lastname, company, address, loggedin_cli_id) => {
        const sql = 'update accounts.user_config set firstname=?, lastname=?, company=?, address=?, modified_user=?, updated_ts=now() where cli_id=? ';
        const params = [firstname, lastname, company, address, loggedin_cli_id, cli_id];
        console.log('updateAccountProfileInfo() [user_config] sql & params => ', sql, params);

        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('updateAccountSettings', async (cli_id, mobile, email, zone_name, offset, bill_type, message_type, platform_cluster, sms_priority, loggedin_userid, user_type, acc_type, invoice_based_on,
        use_default_header, use_default_on_header_fail, acc_default_header, acc_route_id, expiry_date) => {
            
        let con = null;
        try {
            const user_config_sql = 'update accounts.user_config set mobile=?, email=?, time_zone=?, time_offset=?, modified_user=?, acc_type=?, invoice_based_on=?, updated_ts=now(), platform_cluster=?,'
            + 'use_default_header=?, use_default_on_header_fail=?, acc_default_header =? ,sms_priority = ?, expiry_date=?, acc_route_id =? where cli_id=? ';

            const user_config_params = [mobile, email, zone_name, offset, loggedin_userid, acc_type, invoice_based_on, platform_cluster, use_default_header, use_default_on_header_fail, acc_default_header, sms_priority, expiry_date, acc_route_id, cli_id];
            fastify.log.debug('updateAccountSettings() [user_config] sql & params => ', user_config_sql, user_config_params);

            const su_config_sql = 'update accounts.su_config set msg_type=?, platform_cluster=?, time_zone=?, time_offset=?, modified_user=?, updated_ts=now(), '
            + ' acc_route_id =? ,sms_priority = ?  where cli_id=? ';
            const su_config_params = [message_type, platform_cluster, zone_name, offset, loggedin_userid, acc_route_id, sms_priority, cli_id];
          
            con = await fastify.mariadb.getConnection();
            con.beginTransaction();

            await con.query(user_config_sql, user_config_params);
            if (user_type === 0) {
                fastify.log.debug('updateAccountSettings() [su_config] sql & params => ', su_config_sql, su_config_params);
                await con.query(su_config_sql, su_config_params);
            }

            con.commit();
        } catch (e) {
            con.rollback();
            fastify.log.error('con rollback', e);
            throw e;
        } finally {
            if (!_.isNull(con)) {
                await con.release();
            }
        }

        return { m: 'sucess' };
    });

    fastify.decorate('updateAccountServices', async (cli_id, servicesValuesArr, loggedin_cli_id, smpppass, apipass, cmpass, row_req_obj, considerdefaultlength_as_domestic) => {
        const con = await fastify.mariadb.getConnection();

        try {
            const sql1 = 'delete from accounts.user_service_map where cli_id=?';
            const sql2 = 'insert into accounts.user_service_map (cli_id, service, sub_service, created_user, updated_ts, modified_user) values (?,?,?,?,?,?)';
            // let sql3 = 'update accounts.user_config set updated_ts=?, modified_user=?, considerdefaultlength_as_domestic=? ';
            let sql3 = 'update accounts.user_config set updated_ts=now(), modified_user=? ';
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
            await con.query(sql1, [cli_id]);

            if (!_.isEmpty(servicesValuesArr)) {
                console.log('updateAccountServices() [user_service_map] inserting services, sql & params => ', sql2, servicesValuesArr);
                await con.batch(sql2, servicesValuesArr);
            }

            if (!_.isNull(row_req_obj)) {
                const { username, converted_rate, sms_rate, req_id, billing_currency } = row_req_obj;
                await fastify.updateRowSMSRate(con, cli_id, username, converted_rate, sms_rate, loggedin_cli_id, req_id, billing_currency);
            }

            // console.log('updateAccountServices() [user_config] sql & params => ', sql3, [new Date(), loggedin_cli_id, considerdefaultlength_as_domestic, cli_id]);
            // await con.query(sql3, [new Date(), loggedin_cli_id, considerdefaultlength_as_domestic, cli_id]);
            console.log('updateAccountServices() [user_config] sql & params => ', sql3, [loggedin_cli_id, cli_id]);
            await con.query(sql3, [loggedin_cli_id, cli_id]);

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

    fastify.decorate('updateAccountSMPPSettings', async (cli_id, bind_type, max_allowed_connections, throttle_limit, smpp_charset, dlt_entityid_tag, dlt_templateid_tag, cli_mid_tag, dn_date_format) => {
        const sql = 'update accounts.user_smpp_config set bind_type=?, max_allowed_connections=?, max_speed=?, charset=?, dlt_entityid_tag=?, dlt_templateid_tag=?, cli_mid_tag=?, dn_date_format=?, updated_ts=now() where cli_id=? ';
        const params = [bind_type, max_allowed_connections, throttle_limit, smpp_charset, dlt_entityid_tag, dlt_templateid_tag, cli_mid_tag, dn_date_format, cli_id];
        console.log('updateAccountSMPPSettings() [user_smpp_config] sql & params => ', sql, params);

        let result = await fastify.mariadb.query(sql, params);
        if (result.affectedRows == 0) {
            const user_smpp_config_sql = `insert into accounts.user_smpp_config (cli_id, bind_type, max_allowed_connections, max_speed, charset, dlt_entityid_tag, dlt_templateid_tag, dn_date_format, created_ts, cli_mid_tag)
                values(?, ?, ?, ?, ?, ?, ?, ?, current_timestamp(),?)`;
                const user_smpp_config_params = [cli_id, bind_type, max_allowed_connections, throttle_limit, smpp_charset, dlt_entityid_tag, dlt_templateid_tag, dn_date_format, cli_mid_tag];
                console.log('updateAccountSMPPSettings() [user_smpp_config] sql & params => ', user_smpp_config_sql, user_smpp_config_params);
                result = await fastify.mariadb.query(user_smpp_config_sql, user_smpp_config_params);
        }

        return result;
    });

    fastify.decorate('updateAccountDltTemplates', async (cli_id, tgValuesArr, assigned_tgroup_id, loggedin_cli_id) => {
        const con = await fastify.mariadb.getConnection();

        try {
            const sql1 = 'delete from accounts.users_templategroup_ids where cli_id=?';
            const sql2 = 'insert into accounts.users_templategroup_ids (cli_id, template_group_id, created_user) values (?,?,?)';
            const sql3 = 'update accounts.user_config set dlt_templ_grp_id=?, modified_user=?, updated_ts=now() where cli_id=?';

            con.beginTransaction();

            console.log('updateAccountDltTemplates() [users_templategroup_ids] deleting existing templates, sql & params => ', sql1, cli_id);
            await con.query(sql1, [cli_id]);

            if (!_.isEmpty(tgValuesArr)) {
                console.log('updateAccountDltTemplates() [users_templategroup_ids] inserting templates, sql & params => ', sql2, tgValuesArr);
                await con.batch(sql2, tgValuesArr);
            }

            console.log('updateAccountDltTemplates() [user_config] sql & params => ', sql3, [new Date(), loggedin_cli_id, cli_id]);
            await con.query(sql3, [assigned_tgroup_id, loggedin_cli_id, cli_id]);

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

    fastify.decorate('updateAccountsOtherSettings', async (req_obj) => {
        const {
            cli_id, loggedin_cli_id, newline_chars, sms_retry, bill_encrypt_type, trai_blockout,
            domestic_sms_blockout, domestic_sms_blockout_start, domestic_sms_blockout_stop, blklist_chk, spam_chk,
            dup_chk_req, dup_chk_interval, intl_sms_blockout, intl_sms_blockout_start, intl_sms_blockout_stop, optin_chk_req, sales_id,
            domestic_special_series_allow, req_hex_msg, is_ildo, is_16bit_udh, ip_validation, ip_list, full_message, camp_name_auto_gen,
            subusers_reports,
            dnd_chk,
            msg_retry_available, dnd_reject_yn, dnd_pref, force_dnd_check,
            uc_iden_allow, uc_iden_char_len, uc_iden_occur, is_remove_uc_chars,
            timebound_chk_enable, timebound_interval, timebound_max_count_allow,
            vl_shortner, url_smartlink_enable, vl_shortcode_len, url_track_enabled,
            domestic_tra_blockout_reject,
            mt_adjust, dn_adjust, msg_replace_chk, is_schedule_allow, considerdefaultlength_as_domestic, inactive_login
        } = req_obj;

        const con = await fastify.mariadb.getConnection();

        try {
            con.beginTransaction();

            const su_config_sql = `update accounts.su_config set newline_replace_char=?, sms_retry_available=?,
            bill_encrypt_type=?, domestic_promo_trai_blockout_purge=?, domestic_sms_blockout=?, domestic_sms_blockout_start=?, domestic_sms_blockout_stop=?,
            dup_chk_req=?, dup_chk_interval=?, intl_sms_blockout=?, intl_sms_blockout_start=?, intl_sms_blockout_stop=?,
            optin_chk_req=?, sales_id=?, modified_user=?, updated_ts=now(), is_16bit_udh=?, dnd_chk=? ,
            mt_adjust=?, dn_adjust =?  where cli_id=?`;

            const su_config_params = [newline_chars, sms_retry,
                bill_encrypt_type, trai_blockout, domestic_sms_blockout, domestic_sms_blockout_start, domestic_sms_blockout_stop,
                dup_chk_req, dup_chk_interval, intl_sms_blockout, intl_sms_blockout_start, intl_sms_blockout_stop,
                optin_chk_req, sales_id, loggedin_cli_id, is_16bit_udh, dnd_chk,
                mt_adjust, dn_adjust, cli_id];

            fastify.log.debug('updateAccountsOtherSettings() [su_config] sql params => ', su_config_sql, su_config_params);
            await con.query(su_config_sql, su_config_params);
            let updShortCode = '';
            let user_config_params = [newline_chars, bill_encrypt_type,
                trai_blockout, domestic_sms_blockout, domestic_sms_blockout_start, domestic_sms_blockout_stop,
                dup_chk_req, dup_chk_interval, intl_sms_blockout, intl_sms_blockout_start, intl_sms_blockout_stop,
                loggedin_cli_id, domestic_special_series_allow, req_hex_msg, is_ildo, is_16bit_udh,
                ip_validation, ip_list, msg_retry_available, dnd_reject_yn, dnd_pref, force_dnd_check,
                uc_iden_allow, uc_iden_char_len, uc_iden_occur, is_remove_uc_chars,
                timebound_chk_enable, timebound_interval, timebound_max_count_allow,
                vl_shortner, url_smartlink_enable, url_track_enabled,
                domestic_tra_blockout_reject, mt_adjust, dn_adjust, msg_replace_chk, is_schedule_allow, considerdefaultlength_as_domestic, 
                blklist_chk, spam_chk];

            if (url_smartlink_enable == 1 || url_track_enabled == 1) {
                updShortCode = ', vl_shortcode_len=?';
                user_config_params.push(vl_shortcode_len);

                // user_config_params = [newline_chars, bill_encrypt_type,
                //     trai_blockout, domestic_sms_blockout, domestic_sms_blockout_start, domestic_sms_blockout_stop,
                //     dup_chk_req, dup_chk_interval, intl_sms_blockout, intl_sms_blockout_start, intl_sms_blockout_stop,
                //     loggedin_cli_id, domestic_special_series_allow, req_hex_msg, is_ildo, is_16bit_udh,
                //     ip_validation, ip_list, msg_retry_available, dnd_reject_yn, dnd_pref, force_dnd_check,
                //     uc_iden_allow, uc_iden_char_len, uc_iden_occur, is_remove_uc_chars,
                //     timebound_chk_enable, timebound_interval, timebound_max_count_allow,
                //     vl_shortner, url_smartlink_enable, url_track_enabled,
                //     domestic_tra_blockout_reject,
                //     mt_adjust, dn_adjust, msg_replace_chk, is_schedule_allow, considerdefaultlength_as_domestic, 
                //     vl_shortcode_len, cli_id];
            }
            user_config_params.push(cli_id);

            const user_config_sql = `update accounts.user_config set newline_replace_char=?,  bill_encrypt_type=?,
            domestic_promo_trai_blockout_purge=?, domestic_sms_blockout=?, domestic_sms_blockout_start=?, domestic_sms_blockout_stop=?,
            dup_chk_req=?, dup_chk_interval=?, intl_sms_blockout=?, intl_sms_blockout_start=?, intl_sms_blockout_stop=?,
            modified_user=?, updated_ts=now(), domestic_special_series_allow=?, req_hex_msg=?, is_ildo=?, is_16bit_udh=?,
            ip_validation=?, ip_list=?, msg_retry_available=?, dnd_reject_yn=?, dnd_pref=?, force_dnd_check=?,
            uc_iden_allow=?, uc_iden_char_len=?, uc_iden_occur=?, is_remove_uc_chars=?,
            timebound_chk_enable=?, timebound_interval=?, timebound_max_count_allow=?,
            vl_shortner=?, url_smartlink_enable=?,  url_track_enabled=?,
            domestic_tra_blockout_reject=? ,
            mt_adjust=?, dn_adjust =?, msg_replace_chk=?, is_schedule_allow=?, considerdefaultlength_as_domestic=?, blklist_chk=?, spam_chk=? ${updShortCode} where cli_id=?`;

            fastify.log.debug('updateAccountsOtherSettings() [accounts.user_config] sql & params => ', user_config_sql, user_config_params);
            await con.query(user_config_sql, user_config_params);

            const user_configs_sql = `insert into cm.user_configs (cli_id,auto_gen_cname,subusers_reports,full_message, inactive_account_login) values (?,?,?,?,?) ON DUPLICATE KEY 
            update auto_gen_cname=values(auto_gen_cname), subusers_reports=values(subusers_reports), full_message=values(full_message), inactive_account_login=values(inactive_account_login)`;
            fastify.log.debug('updateAccountsOtherSettings() [cm.user_configs] sql & params => ', user_configs_sql, [cli_id, camp_name_auto_gen, subusers_reports, full_message, inactive_login]);
            await con.query(user_configs_sql, [cli_id, camp_name_auto_gen, subusers_reports, full_message, inactive_login]);

            con.commit();
        } catch (e) {
            con.rollback();
            fastify.log.error('con rollback', e);
            throw e;
        } finally {
            await con.release();
        }
    });

    fastify.decorate('updateDomesticSmsRates', async (req_obj) => {
        const { req_id, cli_id, smsrate, dltrate, loggedin_cli_id, old_smsrate, old_dltrate, billing_currency, username } = req_obj;
        const con = await fastify.mariadb.getConnection();
        const sql = 'update accounts.user_config set base_sms_rate=?, base_add_fixed_rate=?, modified_user=?, updated_ts=now() where cli_id=?';
        const sql2 = 'insert into cm.billing_rate_changes (id, req_id, cli_id, username, country, billing_currency, new_sms_rate, old_sms_rate, new_addl_rate, old_addl_rate, done_by) values (?,?,?,?,?,?,?,?,?,?,?)';
        const params = [smsrate, dltrate, loggedin_cli_id, cli_id];
        const params2 = [fastify.nanoid(), req_id, cli_id, username, 'India', billing_currency, smsrate, old_smsrate, dltrate, old_dltrate, loggedin_cli_id];
        let result1 = null;
        try {
            con.beginTransaction();

            console.log('updateDomesticSmsRates() [user_config] sql params => ', sql, params);
            result1 = await con.query(sql, params);

            console.log('updateDomesticSmsRates() [billing_rate_changes] sql params => ', sql2, params2);
            await con.query(sql2, params2);

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

    fastify.decorate('hasInternationalService', async (cli_id) => {
        const sql = 'select * from accounts.user_service_map where cli_id=? and lower(sub_service)=?';
        const result = await fastify.mariadb.query(sql, [cli_id, 'international']);
        return result.length > 0;
    });

    fastify.decorate('persistWalletTransaction', async (walletTransValuesArr) => {
        const walletTransSQL = `insert into cm.wallet_transactions (id, cli_id, username, action, amount, source, description, sessionid, 
            done_by, done_by_username, new_bal, loggedin_bal_before, loggedin_bal_after, old_bal) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)`;
        console.log('persistWalletTransaction() [wallet_transactions] sql & params => ', walletTransSQL, walletTransValuesArr);
        const result = await fastify.mariadb.query(walletTransSQL, walletTransValuesArr);
        return result;
    });

    fastify.decorate('updateAccountStatus', async (loggedin_cli_id, cli_id, newstatus) => {
        const sql = 'update accounts.user_config set acc_status=?, updated_ts=now(), modified_user=? where cli_id=? ';
        const params = [newstatus, loggedin_cli_id, cli_id];
        fastify.log.debug('updateAccountStatus() [user_config] sql & params => ', sql, params);

        const result = await fastify.mariadb.query(sql, params);
        return result;
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

    fastify.decorate('getSMPPConfig', async (cli_id) => {
        const sql = 'select  bind_type, max_allowed_connections, max_speed, charset, dlt_entityid_tag, dlt_templateid_tag, cli_mid_tag,dn_date_format,dn_expiry_in_sec from accounts.user_smpp_config where cli_id = ?';
        const result = await fastify.mariadb.query(sql, [cli_id]);
        console.log(result);
        return result;
    });

    fastify.decorate('updateIntlSmsRates', async (cli_id, addValuesArr, updateValuesArr, updateChangesValuesArr, delete_arr, row_req_obj) => {
        const con = await fastify.mariadb.getConnection();
        const sqladd = 'insert into configuration.client_intl_rates (cli_id, country, base_sms_rate) values (?,?,?)';
        const sqlupdate = 'update configuration.client_intl_rates set base_sms_rate=? where cli_id=? and lower(country)=?';
        const sqlupdate2 = 'insert into cm.billing_rate_changes (id, req_id, cli_id, username, country, new_sms_rate, old_sms_rate, new_addl_rate, old_addl_rate, done_by, billing_currency) values (?,?,?,?,?,?,?,?,?,?,?)';
        const sqldelete = 'delete from configuration.client_intl_rates where cli_id=? and country in (?)';

        let result1 = null;
        try {
          con.beginTransaction();

          if (addValuesArr.length > 0) {
            console.log('updateIntlSmsRates() [client_intl_rates (ADD)] sql params => ', sqladd, addValuesArr);
            result1 = await con.batch(sqladd, addValuesArr);
          }

          if (updateValuesArr.length > 0) {
            console.log('updateIntlSmsRates() [client_intl_rates (UPDATE)] sql params => ', sqlupdate, updateValuesArr);
            result1 = await con.batch(sqlupdate, updateValuesArr);
            console.log('updateIntlSmsRates() [billing_rate_changes (INSERT)] sql params => ', sqlupdate2, updateChangesValuesArr);
            result1 = await con.batch(sqlupdate2, updateChangesValuesArr);
          }
          if (delete_arr.length > 0) {
            console.log('updateIntlSmsRates() [client_intl_rates (DELETE)] sql params => ', sqldelete, [cli_id, delete_arr]);
            result1 = await con.query(sqldelete, [cli_id, delete_arr]);
          }

          // const { username, converted_rate, sms_rate, sms_rate_old, loggedin_cli_id, req_id, billing_currency } = row_req_obj;

          // if (+sms_rate !== +sms_rate_old) {
            // await fastify.updateSMSRatesForROW(con, cli_id, username, converted_rate, sms_rate, sms_rate_old, loggedin_cli_id, req_id, billing_currency);
          // }

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

    fastify.decorate('updateSMSRatesForROW', async (cli_id, username, converted_rate, sms_rate, sms_rate_old, loggedin_cli_id, req_id, billing_currency) => {
        const con = await fastify.mariadb.getConnection();
        const sql = 'select * from configuration.client_intl_rates where cli_id=? and lower(country)=\'row\'';
        const sqlupdate = 'update configuration.client_intl_rates set base_sms_rate=? where cli_id=? and lower(country)=\'row\'';
        const sqlinsert = 'insert into configuration.client_intl_rates (cli_id, country, base_sms_rate) values (?,?,?)';
        const sqlinsert2 = 'insert into cm.billing_rate_changes (id, req_id, cli_id, username, country, new_sms_rate, old_sms_rate, new_addl_rate, old_addl_rate, done_by, billing_currency) values (?,?,?,?,?,?,?,?,?,?,?)';

        let result = null;
        try {
            con.beginTransaction();
          // check if row already exists
          result = await con.query(sql, [cli_id]);
          if (result.length === 0) {
            // insert
            console.log('updateSMSRatesForROW() [client_intl_rates (INSERT)] sql params => ', sqlinsert, [cli_id, 'ROW', converted_rate]);
            result = await con.query(sqlinsert, [cli_id, 'ROW', converted_rate]);
          } else {
            // update
            const rowData = _.get(result, '0', {});
            const params2 = [fastify.nanoid(), req_id, cli_id, username, 'ROW', sms_rate, rowData.base_sms_rate, 0, 0, loggedin_cli_id, billing_currency];
            console.log('updateSMSRatesForROW() [client_intl_rates (UPDATE)] sql params => ', sqlupdate, [converted_rate, cli_id]);
            result = await con.query(sqlupdate, [converted_rate, cli_id]);
            console.log('updateSMSRatesForROW() [client_intl_rates (INSERT)] sql params => ', sqlinsert2, params2);
            result = await con.query(sqlinsert2, params2);
          }
          con.commit();
        } catch (e) {
            con.rollback();
            console.error('con rollback', e);
            throw e;
        } finally {
            await con.release();
        }

        return result;
    });

    fastify.decorate('setDefaultSmppSettings', async (cli_id) => {
        let result = await fastify.getSMPPConfig(cli_id);
        const smpp = _.get(result, '0', {});
        if (_.isEmpty(smpp)) {
            // smpp service enabled for the first time, insert defaults
            const user_smpp_config_sql = `insert into accounts.user_smpp_config (cli_id, bind_type, max_allowed_connections, max_speed, charset, dlt_entityid_tag, dlt_templateid_tag, dn_expiry_in_sec, dn_date_format, created_ts, updated_ts, cli_mid_tag)
                values(?, ?, ?, ?, ?, ?, ?, 21600, 'yyMMddHHmm', current_timestamp(3), current_timestamp(3), ?)`;
                const user_smpp_config_params = [cli_id, process.env.BIND_TYPE, process.env.MAX_ALLOWED_CONNECTIONS, process.env.THROTTLE_LIMIT, process.env.SMPP_CHARSET, process.env.DLT_ENTITYID_TAG, process.env.DLT_TEMPLATEID_TAG, process.env.CLI_MID_TAG];
                console.log('enableSmppService() [user_smpp_config] sql & params => ', user_smpp_config_sql, user_smpp_config_params);
                result = await fastify.mariadb.query(user_smpp_config_sql, user_smpp_config_params);
        }
        return result;
    });

    fastify.decorate('updateRowSMSRate', async (con, cli_id, username, converted_rate, sms_rate, loggedin_cli_id, req_id, billing_currency) => {
        const sql = 'select * from configuration.client_intl_rates where cli_id=? and lower(country)=\'row\'';
        const sqlupdate = 'update configuration.client_intl_rates set base_sms_rate=? where cli_id=? and lower(country)=\'row\'';
        const sqlinsert = 'insert into configuration.client_intl_rates (cli_id, country, base_sms_rate) values (?,?,?)';
        const sqlinsert2 = 'insert into cm.billing_rate_changes (id, req_id, cli_id, username, country, new_sms_rate, old_sms_rate, new_addl_rate, old_addl_rate, done_by, billing_currency) values (?,?,?,?,?,?,?,?,?,?,?)';

        let result = null;
        try {
          // check if row already exists
          result = await con.query(sql, [cli_id]);
          if (result.length === 0) {
            // insert
            console.log('updateRowSMSRate() [client_intl_rates (INSERT)] sql params => ', sqlinsert, [cli_id, 'ROW', converted_rate]);
            result = await con.query(sqlinsert, [cli_id, 'ROW', converted_rate]);
          } else {
            // update
            const rowData = _.get(result, '0', {});
            const params2 = [fastify.nanoid(), req_id, cli_id, username, 'ROW', sms_rate, rowData.base_sms_rate, 0, 0, loggedin_cli_id, billing_currency];
            console.log('updateRowSMSRate() [client_intl_rates (UPDATE)] sql params => ', sqlupdate, [converted_rate, cli_id]);
            result = await con.query(sqlupdate, [converted_rate, cli_id]);
            console.log('updateRowSMSRate() [client_intl_rates (INSERT)] sql params => ', sqlinsert2, params2);
            result = await con.query(sqlinsert2, params2);
          }
        } catch (e) {
          console.error('con rollback', e);
          throw e;
        }

        return result;
    });

    fastify.decorate('getAllSalesPersons', async () => {
        const result = await fastify.mariadb.query('select * from configuration.sales_master');
        return result;
    });

    fastify.decorate('getSalesPersonName', async (id) => {
        const result = await fastify.mariadb.query('select name from configuration.sales_master where id = ?', [id]);
        return result;
    });

    fastify.decorate('isFullMessageEnabled', async (cli_id) => {
        const result = await fastify.mariadb.query('select count(1) as counts from cm.config_user_fullmessage where cli_id=?', [cli_id]);
        return result;
    });

    fastify.decorate('getUserConfigs', async (cli_id) => {
        const result = await fastify.mariadb.query('select * from cm.user_configs where cli_id=?', [cli_id]);
        return result;
    });

    fastify.decorate('updateCreditSettings', async (cli_id, credit_check, amount, loggedin_cli_id, action, user_config_params, walletTransValuesArr) => {
        const con = await fastify.mariadb.getConnection();
        try {
            con.beginTransaction();
            let sql = `update accounts.su_config set credit_check=?, modified_user=?, updated_ts=now() `;
            let params = [credit_check, loggedin_cli_id];
            if (+credit_check === 1) {
                if(_.isEqual(action, 'add')){
                    sql = `${sql} , credit_limit=ifnull(credit_limit, 0)+? `;
                    params.push(amount);
                }else if(_.isEqual(action, 'deduct')){
                    sql = `${sql} , credit_limit=ifnull(credit_limit, 0)-? `;
                    params.push(amount);
                }        
            }
            sql = `${sql} where cli_id=?`;
            params.push(cli_id);
            console.log('updateCreditSettings() [su_config] sql & params => ', sql, params);
            await con.query(sql, params);

            sql = `update accounts.user_config set credit_check=?, modified_user=?, updated_ts=now() `;
            if (+credit_check === 1) {
                if(_.isEqual(action, 'add')){
                    sql = `${sql} , credit_limit=ifnull(credit_limit, 0)+? `;
                }else if(_.isEqual(action, 'deduct')){
                    sql = `${sql} , credit_limit=ifnull(credit_limit, 0)-? `;
                }
            }
            sql = `${sql} where cli_id=?`;

            console.log('updateCreditSettings() [user_config] sql & params => ', sql, params);
            await con.query(sql, params);

            if(_.size(user_config_params) > 0){
                let queries = '';
                user_config_params.forEach(function (params) {
                    queries += `update accounts.user_config set credit_limit=ifnull(credit_limit, 0)-${params.deduct} where cli_id = ${params.cli_id};`;
                });
                await con.query(queries);
            }
            // TODO - Change it to batch/bulk insert
            if(_.size(walletTransValuesArr) > 0){
                const walletTransSQL = `insert into cm.wallet_transactions (id, cli_id, username, action, amount, source, description, sessionid, 
                    done_by, done_by_username, new_bal, loggedin_bal_before, loggedin_bal_after, old_bal) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)`;
                    for await (const walletTransValues of walletTransValuesArr){
                        console.log('updateCreditSettings() [wallet_transactions] sql & params => ', walletTransSQL, walletTransValues);
                        await con.query(walletTransSQL, walletTransValues);
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
    });

    fastify.decorate('updateCappingSettings', async (cli_id, capping_chk_enable, capping_interval_type, capping_interval, capping_max_count_allow, loggedin_cli_id) => {
        const sql = 'update accounts.user_config set capping_chk_enable=?, capping_interval_type=?, capping_interval=?, capping_max_count_allow=?,  modified_user=?, updated_ts=now() where cli_id=? ';
        const params = [capping_chk_enable, capping_interval_type, capping_interval, capping_max_count_allow, loggedin_cli_id, cli_id];
        console.log('updateCappingSettings() [user_config] sql & params => ', sql, params);
        await fastify.mariadb.query(sql, params);
    });

    fastify.decorate('getAllUsersForId', async (cli_id) => {
        const result = await fastify.mariadb.query('select * from accounts.accounts_view where pu_id=? and cli_id !=? order by user', [cli_id, cli_id]);
        return result;
    });

    fastify.decorate('update2LevelAuthStatus', async (loggedin_cli_id, cli_id, two_level_auth, user_type) => {   
        let con = null;
        try {
            const user_config_sql = 'update accounts.user_config set two_level_auth=?, updated_ts=now(), modified_user=? where cli_id=? ';
            const user_config_params = [two_level_auth, loggedin_cli_id, cli_id];
            fastify.log.debug('update2LevelAuthStatus() [user_config] sql & params => ', user_config_sql, user_config_params);

            con = await fastify.mariadb.getConnection();
            con.beginTransaction();

            await con.query(user_config_sql, user_config_params);
            if (user_type === 0) {
                const su_config_sql = 'update accounts.su_config set two_level_auth=?, modified_user=?, updated_ts=now()  where cli_id=? ';
                fastify.log.debug('update2LevelAuthStatus() [su_config] sql & params => ', su_config_sql, user_config_params);
                await con.query(su_config_sql, user_config_params);
            }

            con.commit();
        } catch (e) {
            con.rollback();
            fastify.log.error('con rollback', e);
            throw e;
        } finally {
            if (!_.isNull(con)) {
                await con.release();
            }
        }

        return { m: 'sucess' };
    });
};

module.exports = fp(accountDAO);
