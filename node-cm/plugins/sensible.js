/* eslint-disable no-unused-vars */
const fp = require('fastify-plugin');
const fs = require('fastify-sensible');
/**
 * This plugins adds some utilities to handle http errors
 *
 * @see https://github.com/fastify/fastify-sensible
 */
module.exports = fp(async (fastify, opts) => {
  fastify.register(fs, { errorHandler: false });
});
