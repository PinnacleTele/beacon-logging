const fp = require('fastify-plugin');
const _ = require('lodash');
// TODO: Change req param to respective individual params. *** for method reusability ***

const groupdb = async (fastify, opts) => {
    fastify.decorate('getGroupBasePathFromConfigParams', async () => {
        const result = await fastify.mariadb.query('select * from cm.config_params where `key`=\'group.file.store.path\'');
        return result;
    });

    fastify.decorate('createGroup', async (reqObj) => {
        const con = await fastify.mariadb.getConnection();
        const { cli_id, g_name, g_visibility, g_type, valuesArr, id } = reqObj;
        let r1 = {};
        let r2 = {};

        try {
            let sql = `insert into cm.group_master (id, cli_id, g_name, g_visibility, g_type)
                values (?,?,?,?,?)`;
            const params = [id, cli_id, g_name, g_visibility, g_type];

            con.beginTransaction();

            console.log('createGroup() [group_master] sql params => ', sql, params);
            // insert to group master
            r1 = await con.query(sql, params);

            // insert to group files
            sql = `insert into cm.group_files (id, g_id, filename, filename_ori, filetype, fileloc, total)
                values (?,?,?,?,?,?,?)`;

            console.log('createGroup() [group_files] sql params => ', sql, valuesArr);
            r2 = await con.batch(sql, valuesArr);
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

    fastify.decorate('isGroupNameFound', async (reqObj) => {
        const { cli_id, g_name } = reqObj;

        const sql = 'select count(1) as counts from cm.group_master where cli_id=? and lower(g_name)=?';
        console.log('isGroupNameFound() sql => ', sql, [cli_id, _.toLower(g_name)]);

        const result = await fastify.mariadb.query(sql, [cli_id, _.toLower(g_name)]);

        return result;
    });

    fastify.decorate('getGroups', async (cli_id, g_type, status) => {
        let params = [];
        let sql = 'select * from cm.group_master where cli_id=? and g_type=? order by created_ts desc ';
        if (g_type === 'all' && status === 'all') {
            sql = 'select * from cm.group_master where cli_id=? order by created_ts desc ';
            params = [cli_id];
        } else if (g_type !== 'all' && status !== 'all') {
            sql = 'select * from cm.group_master where cli_id=? and g_type=? and status=? order by created_ts desc ';
            params = [cli_id, g_type, status];
        } else if (g_type !== 'all') {
            sql = 'select * from cm.group_master where cli_id=? and g_type=? order by created_ts desc ';
            params = [cli_id, g_type];
        } else {
            sql = 'select * from cm.group_master where cli_id=? and status=? order by created_ts desc ';
            params = [cli_id, status];
        }

        console.log('getGroups() sql params => ', sql, params);

        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('getGroupsForCampaign', async (cli_id, g_type) => {
        // TODO: *** to include assigned groups as well ***
        const sql = `select t1.*, 0 as 'is_owner' from cm.group_master t1, cm.group_user_mapping t2 where t2.cli_id=? and t2.g_id=t1.id 
                and t1.status='completed' and t1.g_type=?
                union
                select *, 1 as 'is_owner' from cm.group_master where cli_id=? and status='completed' and g_type=?`;
        const params = [cli_id, g_type, cli_id, g_type];

        console.log('getGroups() sql params => ', sql, params);

        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('getSharedGroups', async (cli_id) => {
        const params = [cli_id];
        const sql = 'select * from cm.group_master where cli_id=? and status=\'completed\' and g_visibility=\'shared\'';

        console.log('getSharedGroups() sql params => ', sql, params);

        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    // get the list of assigned shared group that was allocated during the acc creation
    fastify.decorate('getAssignedGroups', async (cli_id) => {
        const params = [cli_id];
        const sql = 'select gm.* from cm.group_master gm, cm.group_user_mapping gum where gum.cli_id =? and gum.g_id=gm.id';

        console.log('getAssignedGroups() sql params => ', sql, params);

        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('getGroupInfo', async (id) => {
        const sql = 'select * from cm.group_master where id=?';
        const params = [id];

        console.log('getGroupInfo() sql params => ', sql, params);
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });
    fastify.decorate('getGroupInfoForIds', async (ids) => {
        const sql = 'select * from cm.group_master where id in(?)';
        const params = [ids];

        console.log('getGroupInfoForIds() sql params => ', sql, params);
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('isNormalGroupInUseInCG', async (ids) => {
        const sql = 'select count(*) count from cm.campaign_groups where group_id in (?) and status not in (\'completed\', \'failed\')';
        const params = [ids];
        console.log('isNormalGroupInUseInCG() sql params => ', sql, params);
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });
    fastify.decorate('isNormalGroupInUseInCF', async (ids) => {
        const sql = 'select count(*) count from cm.campaign_files where group_id in (?) and status not in (\'completed\')';
        const params = [ids];
        console.log('isNormalGroupInUseInCF() sql params => ', sql, params);
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });
    fastify.decorate('isNormalGroupInUseInSched', async (ids) => {
        let str = '';
        for (const v of ids) {
            str += ` group_ids like '%${v}%' or`;
        }
        str = _.trimEnd(str, 'or');

        const sql = `select count(*) count from cm.campaign_schedule_master csm, cm.campaign_schedule_at csa where ${str} and csm.id=csa.cs_id and scheduled_ts > now()`;

        console.log('isNormalGroupInUseInSched() sql => ', sql);
        const result = await fastify.mariadb.query(sql);
        return result;
    });
    fastify.decorate('isExcludeGroupInUseInSched', async (ids) => {
        let str = '';
        for (const v of ids) {
            str += ` exclude_group_ids like '%${v}%' or`;
        }
        str = _.trimEnd(str, 'or');

        const sql = `select count(*) count from cm.campaign_schedule_master csm, cm.campaign_schedule_at csa where ${str} and csm.id=csa.cs_id and scheduled_ts > now()`;

        console.log('isExcludeGroupInUseInSched() sql => ', sql);
        const result = await fastify.mariadb.query(sql);
        return result;
    });
    fastify.decorate('isExcludeGroupInUseInCF', async (ids) => {
        let str = '';
        for (const v of ids) {
            str += ` exclude_group_ids like '%${v}%' or`;
        }
        str = _.trimEnd(str, 'or');

        const sql = `select count(*) count from cm.campaign_files where ${str} and status not in ('completed')`;

        console.log('isExcludeGroupInUseInCF() sql => ', sql);
        const result = await fastify.mariadb.query(sql);
        return result;
    });
    fastify.decorate('isExcludeGroupInUseInCG', async (ids) => {
        let str = '';
        for (const v of ids) {
            str += ` exclude_group_ids like '%${v}%' or`;
        }
        str = _.trimEnd(str, 'or');

        const sql = `select count(*) count from cm.campaign_groups where ${str} and status not in ('completed', 'failed')`;

        console.log('isExcludeGroupInUseInCG() sql => ', sql);
        const result = await fastify.mariadb.query(sql);
        return result;
    });

    fastify.decorate('updateGroup', async (reqObj) => {
        const { g_name, id, cli_id, g_visibility } = reqObj;

        const sql = 'update cm.group_master set g_name=?, g_visibility=?, modified_ts=? where id=? and cli_id=?';
        const params = [g_name, g_visibility, new Date(), id, cli_id];

        console.log('updateGroup() sql params => ', sql, params);
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('unshareGroup', async (parent_cli_id, g_id) => {
        const sql = 'delete from cm.group_user_mapping where assigned_by=? and g_id=?';
        const params = [parent_cli_id, g_id];

        console.log('unshareGroup() sql params => ', sql, params);
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('deleteGroup', async (reqObj) => {
        const { ids, cli_id } = reqObj; // NOTE: array (ids) gets converted to comma separated string 'id1','id2' automatically
        const con = await fastify.mariadb.getConnection();
        try {
            const sql = 'delete from cm.group_master where id in(?) and cli_id=?';
            const params = [ids, cli_id];

            const sql2 = 'delete from cm.group_user_mapping where g_id in(?)';
            const params2 = [ids];

            console.log('deleteGroup() sql params => ', sql, params);
            const result = await con.query(sql, params);
            console.log('deleteGroup() resp from db [group_master] => ', result);
            console.log('deleteGroup() sql params => ', sql2, params2);
            const result1 = await con.query(sql2, params2);
            console.log('deleteGroup() resp from db [group_user_mapping] => ', result1);

            con.commit();
        } catch (e) {
            con.rollback();
            console.error('deleteGroup con rollback ', e);
            throw e;
        } finally {
            await con.release();
        }
        return true;
    });
};

module.exports = fp(groupdb);
