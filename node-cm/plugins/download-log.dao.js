/* eslint-disable camelcase */
const fp = require('fastify-plugin');
const _ = require('lodash');

const logdownloaddb = async (fastify, opts) => {
    fastify.decorate('getLogDownloadCountByStatus', async (cli_id, fdatetime, tdatetime) => {
        const sql = 'select count(*) count, status from cm.download_req where cli_id=? and created_ts between ? and ?';
        const params = [cli_id, fdatetime, tdatetime];

        console.log('getLogDownloadCountByStatus() sql and params => ', sql, params);
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('getLogDownloads', async (cli_id, fdatetime, tdatetime) => {
        const sql = 'select * from cm.download_req where cli_id=? and created_ts between ? and ? order by created_ts desc';
        const params = [cli_id, fdatetime, tdatetime];

        console.log('getLogDownloads() sql and params => ', sql, params);
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('persistDownloadReq', async (id, cli_id, fdatetime, tdatetime, source, campaign_id, campaign_name, senderid, statusflag, user, user_type, fdate, tdate, tz, filters, req_id) => {
        const sql = 'insert into cm.download_req (id, cli_id, from_ts, to_ts, req_id, status, source, campaign_id,campaign_name, senderid, statusflag, loggedinuserid, loggedinusername, loggedinusertype, from_tz, to_tz, zone_name, filters) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)';
        const params = [id, cli_id, fdatetime, tdatetime, req_id, 'inprocess', source, campaign_id, campaign_name, senderid, statusflag, cli_id, user, user_type, fdate, tdate, tz, filters];

        console.log('persistDownloadReq() sql and params => ', sql, params);
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('getLogDownloadFileInfo', async (id) => {
        const sql = 'select * from cm.download_req where id=?';
        const params = [id];

        console.log('getLogDownloadFileInfo() sql and params => ', sql, params);
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('isFullMessageEnabled', async (cli_id) => {
        const sql = 'select count(*) count from cm.config_user_fullmessage where cli_id=?';
        const params = [cli_id];

        console.log('hasFullMessageEnabled() sql and params => ', sql, params);
        const result = await fastify.mariadb.query(sql, params);
        const counts = _.get(result[0], 'count', 0);

        if (+counts === 0) {
            return false;
        }
        return true;
    });
};

module.exports = fp(logdownloaddb);
