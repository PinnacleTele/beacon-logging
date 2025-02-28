const path = require('path');
const AutoLoad = require('fastify-autoload');
const fjwt = require('fastify-jwt');
const dotenv = require('dotenv');
const Fastify = require('fastify');
const _ = require('lodash');
const dc = require('diagnostics_channel');
const schedule = require('node-schedule');
const helmet = require('fastify-helmet');

const onResponse = dc.channel('fastify.onResponse');
const onError = dc.channel('fastify.onError');

const swaggerOpts = require('./config/swagger');
const telemetricTask = require('./task/telemetric.task');
const logErrorTask = require('./task/log.error.task');
const telemetricTon = require('./q/telemetrics.q.ton');
const errorQTon = require('./q/errors.q.ton');

dotenv.config();

const {
    HOST, PORT, REDIS_GEN_HOST, REDIS_GEN_PORT, REDIS_GEN_NAMESPACE, REDIS_GEN_DB, REDIS_GROUP_HOST, REDIS_GROUP_PORT, REDIS_GROUP_NAMESPACE, REDIS_GROUP_DB,
    REDIS_WALLET_HOST, REDIS_WALLET_PORT, REDIS_WALLET_NAMESPACE, REDIS_WALLET_DB,
    REDIS_EGROUP_HOST, REDIS_EGROUP_PORT, REDIS_EGROUP_NAMESPACE, REDIS_EGROUP_DB,
    DB_URL_SUMMARY, PG_SUMMARY_NAMESPACE, PG_SUMMARY_POOLSIZE,
} = process.env;

const fastify = Fastify({
    logger: {
        prettyPrint: false,
        // prettyPrint: {colorize: true, translateTime: 'SYS:standard'},
        level: 'debug',
        file: './log/cm.log',
    },
    genReqId(req) {
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
fastify.register(require('fastify-diagnostics-channel'), {});

fastify.register(require('fastify-mariadb'), {
    promise: true,
    host: process.env.MARIA_DB_CM_HOST,
    port: process.env.MARIA_DB_CM_PORT,
    user: process.env.MARIA_DB_CM_USER,
    password: process.env.MARIA_DB_CM_PASSWORD,
    database: process.env.MARIA_DB_CM_DATABASE,
    connectionLimit: process.env.MARIA_DB_CM_CONNECTION_LIMIT,
});

fastify.register(require('fastify-postgres'), {
    name: PG_SUMMARY_NAMESPACE,
    connectionString: DB_URL_SUMMARY,
    max: PG_SUMMARY_POOLSIZE,
});

fastify.register(require('fastify-cookie'));

fastify.register(require('fastify-csrf'));

fastify.register(helmet, { contentSecurityPolicy: false });

fastify.register(require('fastify-redis'), {
    host: REDIS_GEN_HOST,
    port: REDIS_GEN_PORT,
    db: REDIS_GEN_DB,
    namespace: REDIS_GEN_NAMESPACE,
});
fastify.register(require('fastify-redis'), {
    host: REDIS_GROUP_HOST,
    port: REDIS_GROUP_PORT,
    db: REDIS_GROUP_DB,
    namespace: REDIS_GROUP_NAMESPACE,
});
fastify.register(require('fastify-redis'), {
    host: REDIS_EGROUP_HOST,
    port: REDIS_EGROUP_PORT,
    db: REDIS_EGROUP_DB,
    namespace: REDIS_EGROUP_NAMESPACE,
});
fastify.register(require('fastify-redis'), {
    host: REDIS_WALLET_HOST,
    port: REDIS_WALLET_PORT,
    db: REDIS_WALLET_DB,
    namespace: REDIS_WALLET_NAMESPACE,
});

// fastify.register(require('fastify-elasticsearch'), { node: 'http://192.168.1.137:9200' });
fastify.register(require('fastify-elasticsearch'), { node: process.env.ES_NODE_URL });

/** Run the schedulers ** */
const job1 = schedule.scheduleJob(`*/${process.env.TELEMETRIC_Q_CONSUMER_INETRVAL_INSEC} * * * * *`, telemetricTask);
const job2 = schedule.scheduleJob(`*/${process.env.ERROR_Q_CONSUMER_INETRVAL_INSEC} * * * * *`, logErrorTask);

/** ****************************************************************************************************** */

/** global prehandler to check if the session has not logged out. this hook is called after preValidation hook * */
// eslint-disable-next-line consistent-return
fastify.addHook('preHandler', async (req, reply) => {
    if (req.user) {
        const { cli_id, sessionid } = req.user;
        console.log(cli_id);
        // check if the user with this access token sessionid has logged out
        const r1 = await fastify.hasLoggedOut(cli_id, sessionid);
        console.log('/preHandler Checking if this user has already logged out...');

        if (r1.length > 0) {
            console.log('/preHandler Checking if this user has already logged out...YES');
            // const resp = {
            //     statusCode: fastify.CONSTANTS.INVALID_AUTH,
            //     message: 'Invalid Session',
            // };
            // reply.code(200);
            // return reply.send(resp);
            return reply.send(fastify.httpErrors.unauthorized('Invalid Session'));
        }
        console.log('/login Checking if this user has already logged out...NO');
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
        let cliid = reply.request?.user?.cli_id;
        let username = reply?.request?.user?.user;
        let httpcode = reply.statusCode;
        let errstack = error;
        const errArr = _.split(error.err, '\n');
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

        const q = errorQTon.getErrorsQ();
        const obj = { url, reqid, sessionid, cliid, username, params, httpcode, errmsg, errstack, ip };
        console.log(q.size(), process.env.ERROR_Q_MAX_SIZE);
        request.log.error(errstack);
        // push if in mem threshold is not reached
        if (q.size() < process.env.ERROR_Q_MAX_SIZE) q.push(obj);
    } catch (e) {
        request.log.error(`error in onError hook IGNORING ${e}`);
    }
});

/** subscribe to diagnostic events * */
onResponse.subscribe((data) => {
    // console.log('#### ', data.request.url);
    // console.log('#### ', data.request.routerMethod);
    // console.log('#### ', data.request.routerPath);
    // console.log('#### ', data.reply.request.id);
    // console.log('#### ', data.reply.getResponseTime());
    const cli_id = data.request.user?.cli_id;
    const { url } = data.request;
    const responseTimeInMillis = data.reply.getResponseTime();
    const obj = { cli_id, url, response_in_millis: responseTimeInMillis };
    const q = telemetricTon.getTelemetricQ();

    // push if in mem threshold is not reached
    if (q.size() < process.env.TELEMETRIC_Q_MAX_SIZE) q.push(obj);
});

/** subscribe to error events * */
// onError.subscribe((data) => {
//     // console.log(data);
//     // console.log('#### route', data.request.url);
//     // console.log('#### routerPath', data.request.routerPath);
//     // console.log('#### router method', data.request.routerMethod);
//     // console.log('#### ', data.request.routerMethod);
//     // console.log('#### reqid', data.reply.request.id);
//     // console.log('#### req user', data.reply.request.user);
//     // console.log('#### req body', data.reply.request.body);
//     // console.log('#### req query', data.reply.request.query);
//     // console.log('#### statuscode', data.reply.statusCode);
//     // console.log('#### error', data.error);
//     // console.log('#### error', data.error.err);
//
//     let params = '';
//     let { url } = data.request;
//     let method = data.request?.routerMethod;
//     let reqid = data.reply?.request?.id;
//     let sessionid = data.reply.request?.user?.sessionid;
//     let cliid = data.reply.request?.user?.cli_id;
//     let username = data.reply?.request?.user?.user;
//     let httpcode = data.reply.statusCode;
//     let errstack = data.error;
//     const errArr = _.split(data.error.err, '\n');
//     let errmsg = errArr[0];
//
//     if (_.eq(method, 'GET')) params = data.reply.request.query;
//     else params = data.reply.request.body;
//     params = (_.isEmpty(params)) ? '' : JSON.stringify(params);
//
//     if (_.isUndefined(cliid)) cliid = 0;
//     if (_.isUndefined(username)) username = '';
//     if (_.isUndefined(sessionid)) sessionid = '';
//     if (_.isUndefined(reqid)) reqid = '';
//     if (_.isUndefined(method)) method = '';
//     if (_.isUndefined(url)) url = '';
//     if (_.isUndefined(httpcode)) httpcode = '';
//     if (_.isUndefined(errstack)) errstack = '';
//     if (_.isUndefined(errmsg)) errmsg = '';
//     if (_.isUndefined(params)) params = '';
//
//     const q = errorQTon.getErrorsQ();
//     const obj = { url, reqid, sessionid, cliid, username, params, httpcode, errmsg, errstack };
//     console.log('>>>>>>>>>>>>> Pushing err object to q ', obj);
//     // push if in mem threshold is not reached
//     if (q.size() < process.env.ERROR_Q_MAX_SIZE) q.push(obj);
// });

/**  Do not change the following sections *********** */
/* This loads all plugins defined in plugins folder which that are reused
through your application */

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

// Run the server!
start();
