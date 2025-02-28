const _ = require('lodash');

const { timezonesSchema, countriesSchema, currenciesSchema, conversionRatesSchema, smppSettingsSchema } = require('../../../schemas/generic');

async function generic(fastify, opts) {
    fastify.addHook('preValidation', async (req, reply) => req.jwtVerify());

    fastify.get('/timezones', { schema: timezonesSchema }, async (req, reply) => {
        try {
          const result = await fastify.getTimezones(req);
          return result;
        } catch (err) {
          const e = fastify.httpErrors.createError(500, 'Could not get timezones. Please try again', { err });
          return e;
        }
      });

    fastify.get('/countries', { schema: countriesSchema }, async (req, reply) => {
        try {
          const result = await fastify.getCountries(req);
          return result;
        } catch (err) {
          const e = fastify.httpErrors.createError(500, 'Could not get Countries. Please try again', { err });
          return e;
        }
      });

      fastify.get('/currencies', { schema: currenciesSchema }, async (req, reply) => {
        try {
          const result = await fastify.getAllCurrencies(req);

          let currencies = [];

          if (_.size(result) > 0) {
            _.forEach(result, (obj) => {
              currencies.push({
                currency_code: obj.currency_code,
                desc: `${obj.desc} (${obj.currency_code})`,
              });
            });
            currencies = _.sortBy(currencies, ['desc']);
          }
           return currencies;
        } catch (err) {
          const e = fastify.httpErrors.createError(500, 'Could not get Currencies. Please try again', { err });
          return e;
        }
      });

      fastify.get('/conversionrates', { schema: conversionRatesSchema }, async (req, reply) => {
        try {
          const { conv_type, quote_currency } = req.query;
          const result = await fastify.getConversionRates(quote_currency, conv_type);
          return result;
        } catch (err) {
          const e = fastify.httpErrors.createError(500, 'Could not get conversion rates. Please try again', { err });
          return e;
        }
      });

      fastify.get('/smppdefaultsettings', { schema: smppSettingsSchema }, async (req, reply) => {
        try {
          const smppSettings = {
            bind_type: process.env.BIND_TYPE,
            max_allowed_connections: process.env.MAX_ALLOWED_CONNECTIONS,
            throttle_limit: process.env.THROTTLE_LIMIT,
            smpp_charset: process.env.SMPP_CHARSET,
            dlt_entityid_tag: process.env.DLT_ENTITYID_TAG,
            dlt_templateid_tag: process.env.DLT_TEMPLATEID_TAG,
            cli_mid_tag: process.env.CLI_MID_TAG
          };

          reply.code(200);
          return reply.send(smppSettings);
        } catch (err) {
          const e = fastify.httpErrors.createError(500, 'Could not get smpp default settings. Please try again', { err });
          return e;
        }
      });
}

module.exports = generic;
