/* eslint-disable camelcase */
const fp = require('fastify-plugin');
const _ = require('lodash');

const genericdb = async (fastify, opts) => {
    fastify.decorate('getDLTBasePathFromConfigParams', async () => {
        const result = await fastify.mariadb.query('select * from cm.config_params where `key`=\'dlt.template.file.store.path\'');
        return result;
    });

    fastify.decorate('getDLTTelcos', async () => {
        const result = await fastify.mariadb.query('select telco, telco_display_name, sample_file_name as filename from cm.dlt_template_column_mapping');
        return result;
    });

    fastify.decorate('getAllSenderids', async (cli_id, msg_type) => {
        // msg_type => 0-Promotional, 1-Tansactional
        let is_numeric = 0;
        if (msg_type === 0) is_numeric = 1;

        const sql = 'select distinct header from accounts.dlt_template_group_header_entity_map where is_numeric_header=? and template_group_id in(select dlt_templ_grp_id from accounts.accounts_view where cli_id=?) order by header';
        const params = [is_numeric, cli_id];
        console.log('getAllSenderids() sql and params => ', sql, params);

        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('getAllEntityids', async (cli_id, msg_type) => {
        // msg_type => 0-Promotional, 1-Tansactional
        let is_numeric = 0;
        if (msg_type === 0) is_numeric = 1;

        const sql = 'select distinct entity_id from accounts.dlt_template_group_header_entity_map where is_numeric_header=? and template_group_id in(select dlt_templ_grp_id from accounts.accounts_view where cli_id=?) order by entity_id';
        const params = [is_numeric, cli_id];
        console.log('getAllEntityids() sql and params => ', sql, params);

        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('getTemplateGroupInfo', async (id) => {
        const sql = 'select * from accounts.dlt_template_group where template_group_id=?';
        const params = [id];
        console.log('getTemplateGroupInfo() sql and params => ', sql, params);

        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('getAllEntityidsAndTemplateCount', async (cli_id, msg_type) => {
        // msg_type => 0-Promotional, 1-Tansactional
        let is_numeric = 0;
        if (msg_type === 0) is_numeric = 1;

        const sql = 'select count(1) as total, entity_id from accounts.dlt_template_group_header_entity_map where is_numeric_header=? and template_group_id in(select dlt_templ_grp_id from accounts.accounts_view where cli_id=?) group by entity_id order by total desc';
        const params = [is_numeric, cli_id, cli_id];
        console.log('getAllEntityidsAndTemplateCount() sql and params => ', sql, params);

        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('getDLTTemplates', async (cli_id, msg_type, entityid) => {
        // msg_type => 0-Promotional, 1-Tansactional
        let is_numeric = 0;
        if (msg_type === 0) is_numeric = 1;

        const entity_id = _.trim(entityid);
        console.log(cli_id, entity_id);
        let params = [];
        let sql = '';

        if ((cli_id) && (entity_id)) {
            sql = 'select distinct t1.template_id, entity_id, t2.is_static, template_name, template_content, pattern_type from accounts.dlt_template_group_header_entity_map t1, accounts.dlt_template_info t2 where t1.template_id=t2.template_id and is_numeric_header=? and template_group_id in(select dlt_templ_grp_id from accounts.accounts_view where cli_id=?) and entity_id=? order by template_name';
            params = [is_numeric, cli_id, entity_id];
        } else {
            sql = 'select distinct t1.template_id, entity_id, t2.is_static, template_name, template_content, pattern_type from accounts.dlt_template_group_header_entity_map t1, accounts.dlt_template_info t2 where t1.template_id=t2.template_id and is_numeric_header=? and template_group_id in(select dlt_templ_grp_id from accounts.accounts_view where cli_id=?) order by template_name';
            params = [is_numeric, cli_id];
        }

        console.log('getDLTTemplates() sql params => ', sql, params);
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('getDLTTemplatesForCampaign', async (cli_id, msg_type, senderid, c_type) => {
        // msg_type => 0-Promotional, 1-Tansactional
        let is_numeric = 0;
        if (msg_type === 0) is_numeric = 1;
        let params = [];
        let isStatic = 1;

        if (_.eq(c_type, 'template')) isStatic = 0;

        const sql = 'select DISTINCT t1.template_id, entity_id, t2.is_static, template_name, template_content, pattern_type from accounts.dlt_template_group_header_entity_map t1, accounts.dlt_template_info t2 where t1.template_id=t2.template_id and is_numeric_header=? and template_group_id in(select dlt_templ_grp_id from accounts.accounts_view where cli_id=?) and header=? and t2.is_static=? order by template_name';
        params = [is_numeric, cli_id, senderid, isStatic];

        console.log('getDLTTemplatesForCampaign() sql params => ', sql, params);
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('getDLTTemplatesv2', async (cli_id, msg_type, entityid, templateid) => {
        let params = [];
        let sql = '';
        // msg_type => 0-Promotional, 1-Tansactional
        let is_numeric = 0;
        if (msg_type === 0) is_numeric = 1;

        if (!_.eq(entityid, 'all') && !_.eq(templateid, 'all')) {
            // sql = 'select dtm.header, dtm.entity_id, dtm.template_id, dtm.template_name, dtm.template_content, dtm.pattern_type, dtm.created_ts from accounts.accounts_view aview, accounts.dlt_template_master dtm where aview.cli_id = ? and aview.dlt_templ_grp_id=dtm.template_group_id and dtm.entity_id=? and dtm.template_id=?';
            sql = 'select header, entity_id, t1.template_id, template_name, template_content, pattern_type, created_ts from accounts.dlt_template_group_header_entity_map t1, accounts.dlt_template_info t2 where t1.template_id=t2.template_id and is_numeric_header=? and template_group_id in(select dlt_templ_grp_id from accounts.accounts_view where cli_id=?) and entity_id=? and t1.template_id=? order by header, entity_id, template_id';
            params = [is_numeric, cli_id, entityid, templateid];
        } else if (!_.eq(entityid, 'all')) {
            // sql = 'select dtm.header, dtm.entity_id, dtm.template_id, dtm.template_name, dtm.template_content, dtm.pattern_type, dtm.created_ts from accounts.accounts_view aview, accounts.dlt_template_master dtm where aview.cli_id = ? and aview.dlt_templ_grp_id=dtm.template_group_id and dtm.entity_id=?';
            sql = 'select header, entity_id, t1.template_id, template_name, template_content, pattern_type, created_ts from accounts.dlt_template_group_header_entity_map t1, accounts.dlt_template_info t2 where t1.template_id=t2.template_id and is_numeric_header=? and template_group_id in(select dlt_templ_grp_id from accounts.accounts_view where cli_id=?) and entity_id=? order by header, entity_id, template_id';
            params = [is_numeric, cli_id, entityid];
        } else if (!_.eq(templateid, 'all')) {
            // sql = 'select dtm.header, dtm.entity_id, dtm.template_id, dtm.template_name, dtm.template_content, dtm.pattern_type, dtm.created_ts from accounts.accounts_view aview, accounts.dlt_template_master dtm where aview.cli_id = ? and aview.dlt_templ_grp_id=dtm.template_group_id and dtm.template_id=?';
            sql = 'select header, entity_id, t1.template_id, template_name, template_content, pattern_type, created_ts from accounts.dlt_template_group_header_entity_map t1, accounts.dlt_template_info t2 where t1.template_id=t2.template_id and is_numeric_header=? and template_group_id in(select dlt_templ_grp_id from accounts.accounts_view where cli_id=?) and t1.template_id=? order by header, entity_id, template_id';
            params = [is_numeric, cli_id, templateid];
        } else {
            sql = 'select header, entity_id, t1.template_id, template_name, template_content, pattern_type, created_ts from accounts.dlt_template_group_header_entity_map t1, accounts.dlt_template_info t2 where t1.template_id=t2.template_id and is_numeric_header=? and template_group_id in(select dlt_templ_grp_id from accounts.accounts_view where cli_id=?) order by header, entity_id, template_id';
            params = [is_numeric, cli_id];
        }

        console.log('getDLTTemplatesv2() sql params => ', sql, params);
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    // fastify.decorate('getUnusedDLTTemplates', async (req) => {
    //     const cliid = req.user.cli_id;
    //     const entityid = _.trim(_.get(req.query, 'entity_id', null));

    //     const sql = 'select header, entity_id, t1.template_id, template_name, template_content, pattern_type, created_ts from accounts.dlt_template_group_header_entity_map t1, accounts.dlt_template_info t2 where t1.template_id=t2.template_id and template_group_id in(select dlt_templ_grp_id from accounts.accounts_view where cli_id=?) and entity_id=? and t1.template_id not in (select dlt_template_id from cm.template_master where dlt_entity_id=? and cli_id=?)';
    //     const params = [cliid, entityid, entityid, cliid];

    //     console.log('getUnusedDLTTemplates() sql => ', sql, params);
    //     const result = await fastify.mariadb.query(sql, params);
    //     return result;
    // });

    fastify.decorate('addDLTTemplates', async (id, cli_id, username, entityid, templateGroupId, telco, valuesArr) => {
        const con = await fastify.mariadb.getConnection();
        let r1 = {};
        let r2 = {};

        try {
            const sql1 = 'insert  into cm.dlt_template_request (id, username, cli_id, entity_id, template_group_id, telco, created_by) values (?,?,?,?,?,?,?)';
            const params = [id, username, cli_id, entityid, templateGroupId, telco, cli_id];

            con.beginTransaction();

            console.log('addDLTTemplates() [dlt_template_request] sql params => ', sql1, params);
            // update group master status
            r1 = await con.query(sql1, params);

            const sql2 = 'insert into cm.dlt_template_files (id, d_id, filename, filename_ori, fileloc, total) VALUES (?,?,?,?,?,?)';

            console.log('addDLTTemplates() [dlt_template_files] sql params => ', sql2, valuesArr);
            r2 = await con.batch(sql2, valuesArr);

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

    fastify.decorate('getDLTFileUploads', async (cli_id, fdatetime, tdatetime) => {
        const sql = 'select t2.d_id,telco,entity_id,t1.created_ts, sum(valid) as total, t1.status from cm.dlt_template_request t1, cm.dlt_template_files t2 where t1.id=t2.d_id and cli_id=? and t1.created_ts between ? and ? group by d_id,telco,entity_id, t1.created_ts, t1.status order by created_ts desc';
        const params = [cli_id, fdatetime, tdatetime];

        console.log('getDLTFileUploads() sql and params => ', sql, params);
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });
};

module.exports = fp(genericdb);
