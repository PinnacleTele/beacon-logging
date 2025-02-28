const _ = require('lodash');

const { timezonesSchema, convrateSchema } = require('../../../schema/generic-schema');

// fastify.verifyAccessToken
async function utils(fastify, opts) {
  fastify.addHook('preValidation', async (req, reply) => {
    console.log('preValidation hook called');
    return req.jwtVerify();
  });

  fastify.get('/timezones', {
    preValidation: [],
    schema: timezonesSchema,
  }, async (req, reply) => {
    try {
      const result = await fastify.getTimezones(req);

      console.log('timezones => ', result.length);
      console.log('ip => ', req.ip);
      console.log('ips => ', req.ips);

      return result;
    } catch (err) {
      const e = fastify.httpErrors.createError(500, 'Could not get timezones. Please try again', { err });
      return e;
    }
  });

  /**
   *  to be called for rate conversion to EUR
   */

  fastify.get('/convrate', {
    preValidation: [],
    schema: convrateSchema,
  }, async (req, reply) => {
    try {
      const { cli_id } = req.user;
      const { rate } = req.query;

      const billtobaseRate = await fastify.getConversionRateForUser(cli_id, process.env.INTL_BASE_CURRENCY);

      const resp = { smsrate: _.floor(billtobaseRate * rate, 6) };
      return resp;
    } catch (err) {
      const e = fastify.httpErrors.createError(500, 'Something went wrong. Please try again', { err });
      return e;
    }
  });
}

module.exports = utils;
