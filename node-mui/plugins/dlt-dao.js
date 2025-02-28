const fp = require('fastify-plugin');
const _ = require('lodash');

const dltDAO = async (fastify, opts) => {
    fastify.decorate('getTemplateGroupInfo', async (id) => {
        const sql = 'select * from accounts.dlt_template_group where template_group_id=?';
        const params = [id];
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('getAllocUsersCount', async () => {
        const sql = 'select count(distinct cli_id) as counts,template_group_id from accounts.users_templategroup_ids uti group by uti.template_group_id';
        const result = await fastify.mariadb.query(sql);
        return result;
    });

    fastify.decorate('getAssignedUsersCount', async () => {
        const sql = 'select count(distinct cli_id) as counts,dlt_templ_grp_id from accounts.user_config group by dlt_templ_grp_id';
        const result = await fastify.mariadb.query(sql);
        return result;
    });

    fastify.decorate('checkDLTnameUniqueness', async (name) => {
        const result = await fastify.mariadb.query('select count(1) as counts from accounts.dlt_template_group where lower(template_group_name)=?', [_.toLower(name)]);
        return result;
    });

    fastify.decorate('insertNewDLT', async (action, dlt_grp_id, dlt_grp_name) => {
        if (action == 'create') {
            const insert_dlt_sql = `insert into accounts.dlt_template_group (template_group_name, created_ts)
                values(?, now())`;
            const result = await fastify.mariadb.query(insert_dlt_sql, dlt_grp_name);
            return result;
        }
            const update_dlt_sql = 'update accounts.dlt_template_group set template_group_name = ?, updated_ts = now() where template_group_id = ?';

            const result = await fastify.mariadb.query(update_dlt_sql, [dlt_grp_name, dlt_grp_id]);
            return result;
    });
};

module.exports = fp(dltDAO);
