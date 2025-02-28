/* eslint-disable no-restricted-syntax */
const _ = require('lodash');

const rclient = require('../factory/redis.telemetric.factory');
const telemetricTon = require('../q/telemetrics.q.ton');

module.exports = async function (fireDate) {
    // console.log(`This job was supposed to run at ${fireDate}, but actually ran at ${new Date()}`);
    try {
        const memberArr = [];
        // get the data from q
        const q = telemetricTon.getTelemetricQ();

        if (!q.isEmpty()) {
            const itemObjArr = q.remove(0, process.env.TELEMETRIC_Q_CONSUMER_INETRVAL_INSEC);
            // console.log('total items poped from q =>', itemObjArr.length);

            for (const itemObj of itemObjArr) {
                if (itemObj) {
                    console.log('itemObj => ', itemObj);
                    const { cli_id, url, response_in_millis } = itemObj;

                    const member = `[${cli_id}] [${url}]`;
                    memberArr.push(_.ceil(response_in_millis));
                    memberArr.push(member);
                }
            }
            if (memberArr.length > 0) {
                const r = await rclient.zadd('cm:api:resp:delay', memberArr);
                // console.log('resp from redis ', r);
            }
        }
    } catch (e) {
        console.error('telemetric.task.js ERROR => ', e);
    }
};
