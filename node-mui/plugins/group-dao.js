
const fp = require('fastify-plugin');
const _ = require('lodash');
const groupDAO = async (fastify, opts) => {
    fastify.decorate('getAssignedGroups', async (cli_id) => {
        const params = [cli_id];
        const sql = 'select gm.* from cm.group_master gm, cm.group_user_mapping gum where gum.cli_id =? and gum.g_id=gm.id';
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });
}
module.exports = fp(groupDAO);