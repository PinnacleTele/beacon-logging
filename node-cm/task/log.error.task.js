/* eslint-disable no-restricted-syntax */
const _ = require('lodash');

const mariapool = require('../factory/mariadbpool.cmdb');
const errorQTon = require('../q/errors.q.ton');

module.exports = async function (fireDate) {
    // console.log(`This job was supposed to run at ${fireDate}, but actually ran at ${new Date()}`);
    let itemObjArr = [];
    try {
        const valuesArr = [];

        // get the data from q
        const q = errorQTon.getErrorsQ();
        // console.log('error task - q size ', q.size());

        if (!q.isEmpty()) {
            itemObjArr = q.remove(0, process.env.ERROR_Q_CONSUMER_INETRVAL_INSEC);
            console.log('log.error.task.js total items poped from q =>', itemObjArr.length);
            itemObjArr = _.compact(itemObjArr);
            console.log(itemObjArr);

            for (const itemObj of itemObjArr) {
                if (itemObj) {
                    console.log('log.error.task.js error obj popped => ', itemObj);
                    const { url, reqid, sessionid, cliid, username, params, httpcode, errmsg, errstack, ip } = itemObj;
                    valuesArr.push([cliid, username, httpcode, url, errmsg, params, sessionid, reqid, errstack, ip]);
                }
            }

            if (_.flatten(valuesArr).length > 0) {
                // persist to table
                const con = await mariapool.getConnection();
                try {
                    con.beginTransaction();
                    console.log('log.error.task.js [error_log] persisting errors... ');
                    const sql = 'insert into cm.error_log (cli_id, user, httpcode, route, err_msg, params, sessionid, req_id, err_stack, ip) values (?,?,?,?,?,?,?,?,?,?)';
                    const r = await con.batch(sql, valuesArr);
                    console.log('log.error.task.js resp from db =>', r);

                    con.commit();
                } catch (e) {
                    con.rollback();
                    console.error('con rollback', e);
                    throw e;
                } finally {
                    await con.release();
                }
            }
        }
    } catch (e) {
        console.error('log.error.task.js ERROR payload => ', JSON.stringify(itemObjArr));
        console.error('log.error.task.js ERROR => ', e);
    }
};
