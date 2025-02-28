const _ = require('lodash');
const axios = require('axios');
const util = require('util');
const path = require('path');

const sleep = util.promisify(setTimeout);

const mariapool = require('../factory/mariadbpool.cmdb');

module.exports = async function (fireDate) {
    // console.log(`This job was supposed to run at ${fireDate}, but actually ran at ${new Date()}`);
    try {
        // select all the inprocess data
        const r = await mariapool.query('select id,req_id from cm.download_req where lower(status)=\'inprocess\'');
        for await (const obj of r) {
            console.log('checking status for the request id', obj.req_id);

            try {
                const resp = await axios.post(process.env.LOG_DOWNLOAD_STATUS_CHECK_URL, { "queue_id": obj.req_id });

                if (!resp.data.queue_status) {
                    console.error('POSSIBLE ERROR! Resp from api => ' + JSON.stringify(resp.data));
                    continue;
                }

                const statusResp = _.get(resp.data, 'queue_status', {});
                console.log(statusResp);
                console.log(resp.data);

                if (!_.isEmpty(statusResp)) {
                    const status = _.toLower(_.get(statusResp, 'current_status'));
                    if (_.eq(status, 'completed')) {
                        // get the base path
                        const result = await mariapool.query('select * from cm.config_params where `key`=\'log.download.path\'');

                        const r3 = _.get(result,0,{});

                        if (_.isEmpty(r3)) throw new Error('ERROR property log.download.path is not configured in config_params');

                        const basePath = r3.value;
                        const filename = `${obj.req_id}.csv`;
                        const fileloc = path.join(basePath, filename);

                        const total = _.get(statusResp, 'record_count', 0);
                        // update table
                        console.log('updating table for the request id...', obj.req_id);
                        const r = await mariapool.query('update cm.download_req set status=\'csvcompleted\', download_path=?, total=?, modified_ts=?  where id=?', [fileloc, total, new Date(), obj.id]);
                        console.log(`updating table for the request id ${obj.req_id} ... SUCCESS `);

                    }
                    if (_.eq(status, 'error')) {
                        // update table
                        console.log('*** download has failed *** updating table for the request id...', obj.req_id);
                        const r = await mariapool.query('update cm.download_req set status=\'failed\', modified_ts=?  where id=?', [new Date(), obj.id]);
                        console.log(`updating table for the request id ${obj.req_id} ... SUCCESS `);
                    }
                }


            } catch (e1) {
                console.error('log.download.statuscheck.js ERROR ***Ignoring** => ', e1.response.data);
            }
        }
    } catch (e) {
        console.error('log.download.statuscheck.js ERROR => ', e);
    }
}
;
