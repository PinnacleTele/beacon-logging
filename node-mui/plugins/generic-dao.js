/* eslint-disable camelcase */
const fp = require('fastify-plugin');
const _ = require('lodash');

const genericDAO = async (fastify) => {
    fastify.decorate('getTimezones', async () => {
        const result = await fastify.mariadb.query('select * from cm.timezone order by zone_name');
        return result;
    });

    fastify.decorate('getCountries', async () => {
        const sql = 'select *  from configuration.country_info ci  where (TRIM(country) != \'\' or country != null) order by country';

        const result = await fastify.mariadb.query(sql);
        return result;
    });

    fastify.decorate('getAllCurrencies', async () => {
        const result = await fastify.mariadb.query('select * from configuration.billing_currency_master order by currency_code ');
        return result;
    });

    fastify.decorate('getConversionRate', async (base, quote, convType) => {
        let tablename = '';

        if (+convType === 1) tablename = 'configuration.currency_rates_monthly';
        else if (+convType === 2) tablename = 'configuration.currency_rates_daily';
        else throw new Error('Unsupported Conversion Type');

        const sql = `select rate from ${tablename} where base_currency=? and quote_currency=?`;
        const params = [base, quote];

        const result = await fastify.mariadb.query(sql, params);
        return result;
      });

      fastify.decorate('getConversionRates', async (quote, convType) => {
        let tablename = '';

        console.log('getConversionRates() incoming params => ', [quote, convType]);
        if (+convType === 1) tablename = 'configuration.currency_rates_monthly';
        else if (+convType === 2) tablename = 'configuration.currency_rates_daily';
        else throw new Error('Unsupported Conversion Type');

        let sql = `select base_currency, quote_currency, rate from ${tablename}`;
        const params = [];
        if (!_.isUndefined(quote) && !_.isNull(quote) && !_.isEmpty(quote)) {
            sql += ' where lower(quote_currency)=?';
            params.push(_.toLower(quote));
        }

        console.log('getConversionRates() sql & params => ', sql, params);

        const result = await fastify.mariadb.query(sql, params);
        return result;
      });

      fastify.decorate('getConversionRateForUser', async (cli_id, to_currency) => {
        const result = await fastify.findUserById(cli_id);
        // find the user conversion type to use
        const userObj = _.get(result, '0', {});
        const convType = _.get(userObj, 'billing_currency_conv_type');
        const billing_currency = _.get(userObj, 'billing_currency');

        // get the conversion rate for billing_currency to bill rates (eur)
        const result2 = await fastify.getConversionRate(billing_currency, to_currency, convType);
        const rateObj = _.get(result2, '0', {});

        if (_.isEmpty(rateObj)) {
            throw fastify.httpErrors.internalServerError('Error processing your request. Pleae try again');
        }

        const convRate = _.get(rateObj, 'rate');
        return convRate;
    });
    /*
    fastify.decorate('getWalletBalances', async () => {
        const walletBals = await fastify.redis.WALLET.hgetall('wallet:amount');
        return walletBals;
    });
    */
};

module.exports = fp(genericDAO);
