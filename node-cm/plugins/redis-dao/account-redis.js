/* eslint-disable camelcase */
const fp = require('fastify-plugin');
const _ = require('lodash');

const accountRedis = async (fastify, opts) => {
    fastify.decorate('walletBalance', async (cli_id) => {
        const amount = await fastify.redis.WALLET.hget('wallet:amount', cli_id);

        return amount;
    });

    fastify.decorate('getWalletBalancesForCliIds', async (cli_ids) => {
        const walletBals = await fastify.redis.WALLET.hmget('wallet:amount', cli_ids);
        const cliIdWiseBalance = {};
        _.forEach(cli_ids, (cli_id, index)=>{
            cliIdWiseBalance[cli_id] = walletBals[index];
        });
        return cliIdWiseBalance;
    });

    fastify.decorate('getWalletBalances', async () => {
        const walletBals = await fastify.redis.WALLET.hgetall('wallet:amount');
        return walletBals;
    });
	
	
};

module.exports = fp(accountRedis);
