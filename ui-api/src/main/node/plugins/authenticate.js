const fp = require('fastify-plugin');
const fjwt = require('fastify-jwt');
const _ = require('lodash');

const auth = async (fastify, opts) => {
  fastify.addHook('preHandler', async (req, reply) => {
    console.log('hook from authenticate.js called');

    // TODO: to be removed when auth is implemented
    // const userObj = {
    //   cli_id: '6000000200000000',
    //   user: 'wintest',
    //   tz: 'Asia/Calcutta',
    //   sessionid: fastify.nanoid(),
    // };
    //
    // req.user = userObj;
  });

  // fastify.register(fjwt, {
  //   secret: 'supersecre',
  //   cookie: {
  //     cookieName: 'token',
  //     signed: false,
  //   },
  // });

  fastify.decorate('verifyAccessToken', async (req, reply) => {
    try {
      // const a = await req.jwtVerify();
      console.log('verifyAccessToken');
      return req.jwtVerify();
    } catch (err) {
      return err;
    }
  });

};

module.exports = fp(auth);
