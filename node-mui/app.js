/* eslint-disable global-require */
const path = require('path');
const AutoLoad = require('fastify-autoload');
const fjwt = require('fastify-jwt');
const dotenv = require('dotenv');
const Fastify = require('fastify');
const _ = require('lodash');

// const plugin = require('fastify-server-timeout');
const schedule = require('node-schedule');
const swaggerOpts = require('./config/swagger');

// const telemetricTask = require('./task/telemetric.task');
// const telemetricTon = require('./q/telemetrics.q.ton');
const logErrorTask = require('./task/log.error.task');
const errorQTon = require('./q/errors.q.ton');

dotenv.config();

const { HOST, PORT } = process.env;

const fastify = Fastify({
    logger: {
        prettyPrint: false,
        // prettyPrint: {colorize: true, translateTime: 'SYS:standard'},
        level: 'debug',
        file: './log/mui-api.log',
    },
    genReqId() {
        const reqid = fastify.nanoid();
        return reqid;
    },
    bodyLimit: 1048576 * 5, // 5mb
    trustProxy: true,
});

fastify.register(fjwt, {
    secret: 'supersecret',
    cookie: {
        cookieName: 'token',
        signed: false,
    },
});

if (_.eq(process.env.ENV, 'dev')) {
    fastify.register(require('fastify-cors'), { origin: '*', methods: ['GET', 'PUT', 'POST'] });
} else {
    fastify.register(require('fastify-cors'), { origin: true, methods: ['GET', 'PUT', 'POST'] });
}

fastify.register(require('fastify-swagger'), swaggerOpts.options);

fastify.register(require('fastify-mariadb'), {
    promise: true,
    host: process.env.MARIA_DB_CM_HOST,
    port: process.env.MARIA_DB_CM_PORT,
    user: process.env.MARIA_DB_CM_USER,
    password: process.env.MARIA_DB_CM_PASSWORD,
    database: process.env.MARIA_DB_CM_DATABASE,
    connectionLimit: process.env.MARIA_DB_CM_CONNECTION_LIMIT,
    multipleStatements: true
});

fastify.register(require('fastify-redis'), {
    host: process.env.REDIS_GEN_HOST,
    port: process.env.REDIS_GEN_PORT,
    db: process.env.REDIS_GEN_DB,
    namespace: process.env.REDIS_GEN_NAMESPACE,
});

fastify.register(require('fastify-redis'), {
    host: process.env.REDIS_WALLET_HOST,
    port: process.env.REDIS_WALLET_PORT,
    db: process.env.REDIS_WALLET_DB,
    namespace: process.env.REDIS_WALLET_NAMESPACE,
});

fastify.register(require('fastify-cookie'));

// fastify.register(plugin, { serverTimeout: 10000 });
fastify.register(require('fastify-graceful-shutdown'));

/** Run the schedulers ** */
// const job1 = schedule.scheduleJob(`*/${process.env.TELEMETRIC_Q_CONSUMER_INETRVAL_INSEC} * * * * *`, telemetricTask);

schedule.scheduleJob(`*/${process.env.ERROR_Q_CONSUMER_INETRVAL_INSEC} * * * * *`, logErrorTask);

// eslint-disable-next-line consistent-return
fastify.addHook('preHandler', async (req, reply) => {
    if (req.user) {
        const { userid, sessionid } = req.user;
        let r1 = '';
        if (userid) {
            r1 = await fastify.hasLoggedOut(userid, sessionid);
        } else {
            console.log(fastify.httpErrors.unauthorized('Invalid Session'));
            return reply.send(fastify.httpErrors.unauthorized('Invalid Session'));
        }

        if (r1.length > 0) {
            console.log('/preHandler Checking if this user has already logged out...YES');

            return reply.send(fastify.httpErrors.unauthorized('Invalid Session'));
        }
        console.log('/login Checking if this user has already logged out...NO');
        return null;
    }
});

fastify.addHook('onError', async (request, reply, error) => {
    try {
        let params = '';
        let { url } = request;
        let method = request?.routerMethod;
        let reqid = reply?.request?.id;
        let ip = request?.ip;
        let sessionid = reply.request?.user?.sessionid;
        let cliid = reply.request?.user?.userid;
        let username = reply?.request?.user?.user;
        let httpcode = reply.statusCode;
        let errstack = error;
        const errArr = _.split(error.message, '\n');
        let errmsg = errArr[0];

        if (_.eq(method, 'GET')) params = reply.request.query;
        else params = reply.request.body;
        params = (_.isEmpty(params)) ? '' : JSON.stringify(params);

        if (_.isUndefined(cliid)) cliid = 0;
        if (_.isUndefined(username)) username = '';
        if (_.isUndefined(sessionid)) sessionid = '';
        if (_.isUndefined(reqid)) reqid = '';
        if (_.isUndefined(method)) method = '';
        if (_.isUndefined(url)) url = '';
        if (_.isUndefined(httpcode)) httpcode = '';
        if (_.isUndefined(errstack)) errstack = '';
        if (_.isUndefined(errmsg)) errmsg = '';
        if (_.isUndefined(params)) params = '';
        if (_.isUndefined(ip)) ip = '';

        //
        const obj = { url, reqid, sessionid, cliid, username, params, httpcode, errmsg, errstack, ip };
        request.log.error(errstack);

        // push if in mem threshold is not reached
        const q = errorQTon.getErrorsQ();
        if (q.size() < process.env.ERROR_Q_MAX_SIZE) { q.push(obj); }

        // Write in table
        // fastify.logError(obj);
    } catch (e) {
        request.log.error(`error in onError hook IGNORING ${e}`);
    }
});

fastify.register(AutoLoad, {
    dir: path.join(__dirname, 'plugins'),
    // options: Object.assign({}, opts),
});

// This loads all plugins defined in routes
// define your routes in one of these
fastify.register(AutoLoad, {
    dir: path.join(__dirname, 'routes'),
    // options: Object.assign({}, opts),
});
/** ************************************************* */

const start = async () => {
    try {
        await fastify.listen(PORT, HOST);
        fastify.swagger();
        console.log('=>', fastify.printRoutes({ commonPrefix: false }));
        fastify.log.info(`listening on ${fastify.server.address().port}`);
    } catch (err) {
        fastify.log.error(err);
        process.exit(1);
    }
};

fastify.after(() => {
    fastify.gracefulShutdown((signal, next) => {
      next();
    });
});
// Run the server!
start();
