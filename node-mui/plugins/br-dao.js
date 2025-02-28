const fp = require('fastify-plugin');

const billRatesDAO = async (fastify) => {
    fastify.decorate('getBillRatesForIntl', async (cli_id) => {
        const sql = 'select * from configuration.client_intl_rates where cli_id=?';
        const params = [cli_id];

        const result = await fastify.mariadb.query(sql, params);

        return result;
    });
};
module.exports = fp(billRatesDAO);
