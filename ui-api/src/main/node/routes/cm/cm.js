/* eslint-disable global-require */
const _ = require('lodash');

async function cm(fastify, opts) {
    fastify.get('/refreshinterval', { preValidation: [] }, async (req, reply) => {
        let intervalInSec = 90;
        try {
            intervalInSec = _.parseInt(process.env.REFRESH_TOKEN_INTERVAL_INSEC);
            if (_.isNaN(intervalInSec)) intervalInSec = 90;
            if (intervalInSec <= 0) intervalInSec = 90;
        } catch (err) {
            console.error('Could not get interval *** Ignoring ***. setting default value', { err });
        }
        return { interval_insec: intervalInSec };
    });
}

module.exports = cm;
