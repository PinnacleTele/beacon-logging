/* eslint-disable camelcase */
const fp = require('fastify-plugin');
const _ = require('lodash');

const accountRedis = async (fastify, opts) => {
    fastify.decorate('walletBalance', async (cli_id) => {
        const amount = await fastify.redis.WALLET.hget('wallet:amount', cli_id);

        return amount;
    });
};

module.exports = fp(accountRedis);
