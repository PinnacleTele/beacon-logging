const fp = require('fastify-plugin');
const _ = require('lodash');

const authDAO = async (fastify) => {
    fastify.decorate('findAdmin', async (uname) => {
        const result = await fastify.mariadb.query('select * from imp.management_user where lower(username)=?', [_.toLower(uname)]);
        return result;
    });

    fastify.decorate('logSession', async (user_id, user, action, sessionid, uainfo, ip) => {
        const sql = 'insert into imp.management_user_session_log ( userid, username, action, sessionid, ua_info, ip, action_ts) values (?,?,?,?,?,?,now())';
        const params = [user_id, user, action, sessionid, uainfo, ip];

        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('hasLoggedOut', async (userid, sessionid) => {
        const result = await fastify.mariadb.query('select * from imp.management_user_session_log  where  userid=? and sessionid=? and action="logout"', [userid, sessionid]);
        return result;
    });

    fastify.decorate('updateAccountPassword', async (cli_id, newpass) => {
        const sql = 'update imp.management_user set password=?, updated_ts=? where id=?';
        const params = [newpass, new Date(), cli_id];
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });
};

module.exports = fp(authDAO);
