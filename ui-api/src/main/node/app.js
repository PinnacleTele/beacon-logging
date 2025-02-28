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
        level: 'debug',
        file: '/logs/cm.log',
    },
    genReqId(req) {
        const reqid = fastify.nanoid();
        return reqid;
    },
    bodyLimit: 1048576 * 5, // 5mb
    trustProxy: true,
});


const fs = require('fs');
const util = require('util');

// Get log file path from environment variable
const logFilePath = process.env.LOG_FILE_PATH || 'app.log';

// Create a writable stream for logs
const logFile = fs.createWriteStream(logFilePath, { flags: 'a' });

// Redirect console.log and console.error to the file
console.log = (message) => {
  const logMessage = `${new Date().toISOString()} - LOG: ${util.format(message)}\n`;
  logFile.write(logMessage);
  process.stdout.write(logMessage); // Optionally, print to console as well
};

console.error = (message) => {
  const errorMessage = `${new Date().toISOString()} - ERROR: ${util.format(message)}\n`;
  logFile.write(errorMessage);
  process.stderr.write(errorMessage); // Optionally, print to console as well
};

// Example usage
console.log('This is a log message.');
console.error('This is an error message.');

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

console.log('process.env.ENV : '+process.env.ENV);

fastify.register(require('fastify-swagger'), swaggerOpts.options);
fastify.register(require('fastify-diagnostics-channel'), {});

console.log('process.env.MARIA_DB_CM_HOST : '+process.env.MARIA_DB_CM_HOST);
console.log('process.env.MARIA_DB_CM_PORT : '+process.env.MARIA_DB_CM_PORT);
console.log('process.env.MARIA_DB_CM_USER : '+process.env.MARIA_DB_CM_USER);
console.log('process.env.MARIA_DB_CM_PASSWORD : '+process.env.MARIA_DB_CM_PASSWORD);
console.log('process.env.MARIA_DB_CM_DATABASE : '+process.env.MARIA_DB_CM_DATABASE);
console.log('process.env.MARIA_DB_CM_CONNECTION_LIMIT : '+process.env.MARIA_DB_CM_CONNECTION_LIMIT);

fastify.register(require('fastify-mariadb'), {
    promise: true,
    host: process.env.MARIA_DB_CM_HOST,
    port: process.env.MARIA_DB_CM_PORT,
    user: process.env.MARIA_DB_CM_USER,
    password: process.env.MARIA_DB_CM_PASSWORD,
    database: process.env.MARIA_DB_CM_DATABASE,
    connectionLimit: process.env.MARIA_DB_CM_CONNECTION_LIMIT,
}	, (err) => {
	    if (err) {
	        console.log(`[MariaDB] Connection registration failed: ${err.message}`);
	    } else {
	        console.log('[MariaDB] Connection registration successful');
	    }
	});



console.log('process.env.PG_SUMMARY_NAMESPACE : '+process.env.PG_SUMMARY_NAMESPACE);
console.log('process.env.DB_URL_SUMMARY : '+process.env.DB_URL_SUMMARY);
console.log('process.env.PG_SUMMARY_POOLSIZE : '+process.env.PG_SUMMARY_POOLSIZE);

fastify.register(require('fastify-postgres'), {
    name: PG_SUMMARY_NAMESPACE,
    connectionString: DB_URL_SUMMARY,
    max: PG_SUMMARY_POOLSIZE,
}	, (err) => {
		    if (err) {
		        console.log(`[Posgres] Connection registration failed: ${err.message}`);
		    } else {
		        console.log('[Posgres] Connection registration successful');
		    }
		});

fastify.register(require('fastify-cookie'));

fastify.register(require('fastify-csrf'));

fastify.register(helmet, { contentSecurityPolicy: false });

console.log('process.env.REDIS_GEN_HOST : '+process.env.REDIS_GEN_HOST);
console.log('process.env.REDIS_GEN_PORT : '+process.env.REDIS_GEN_PORT);
console.log('process.env.REDIS_GEN_DB : '+process.env.REDIS_GEN_DB);
console.log('process.env.REDIS_GEN_NAMESPACE : '+process.env.REDIS_GEN_NAMESPACE);


fastify.register(require('fastify-redis'), {
    host: REDIS_GEN_HOST,
    port: REDIS_GEN_PORT,
    db: REDIS_GEN_DB,
    namespace: REDIS_GEN_NAMESPACE,
});

console.log('process.env.REDIS_GROUP_HOST : '+process.env.REDIS_GROUP_HOST);
console.log('process.env.REDIS_GROUP_PORT : '+process.env.REDIS_GROUP_PORT);
console.log('process.env.REDIS_GROUP_DB : '+process.env.REDIS_GROUP_DB);
console.log('process.env.REDIS_GROUP_NAMESPACE : '+process.env.REDIS_GROUP_NAMESPACE);


fastify.register(require('fastify-redis'), {
    host: REDIS_GROUP_HOST,
    port: REDIS_GROUP_PORT,
    db: REDIS_GROUP_DB,
    namespace: REDIS_GROUP_NAMESPACE,
});


console.log('process.env.REDIS_EGROUP_HOST : '+process.env.REDIS_EGROUP_HOST);
console.log('process.env.REDIS_EGROUP_PORT : '+process.env.REDIS_EGROUP_PORT);
console.log('process.env.REDIS_EGROUP_DB : '+process.env.REDIS_EGROUP_DB);
console.log('process.env.REDIS_EGROUP_NAMESPACE : '+process.env.REDIS_EGROUP_NAMESPACE);



fastify.register(require('fastify-redis'), {
    host: REDIS_EGROUP_HOST,
    port: REDIS_EGROUP_PORT,
    db: REDIS_EGROUP_DB,
    namespace: REDIS_EGROUP_NAMESPACE,
});

console.log('process.env.REDIS_WALLET_HOST : '+process.env.REDIS_WALLET_HOST);
console.log('process.env.REDIS_WALLET_PORT : '+process.env.REDIS_WALLET_PORT);
console.log('process.env.REDIS_WALLET_DB : '+process.env.REDIS_WALLET_DB);
console.log('process.env.REDIS_WALLET_NAMESPACE : '+process.env.REDIS_WALLET_NAMESPACE);


fastify.register(require('fastify-redis'), {
    host: REDIS_WALLET_HOST,
    port: REDIS_WALLET_PORT,
    db: REDIS_WALLET_DB,
    namespace: REDIS_WALLET_NAMESPACE,
});

console.log('process.env.ES_NODE_URL : '+process.env.ES_NODE_URL);


fastify.register(require('fastify-elasticsearch'), { node: process.env.ES_NODE_URL });

console.log('process.env.TELEMETRIC_Q_CONSUMER_INETRVAL_INSEC : '+process.env.TELEMETRIC_Q_CONSUMER_INETRVAL_INSEC);
console.log('process.env.ERROR_Q_CONSUMER_INETRVAL_INSEC : '+process.env.ERROR_Q_CONSUMER_INETRVAL_INSEC);

/** Run the schedulers ** */
const job1 = schedule.scheduleJob(`*/${process.env.TELEMETRIC_Q_CONSUMER_INETRVAL_INSEC} * * * * *`, telemetricTask);
const job2 = schedule.scheduleJob(`*/${process.env.ERROR_Q_CONSUMER_INETRVAL_INSEC} * * * * *`, logErrorTask);

/** ****************************************************************************************************** */

/** global prehandler to check if the session has not logged out. this hook is called after preValidation hook * */

console.log('addHook(\'preHandler\', async (req, reply) ');

fastify.addHook('preHandler', async (req, reply) => {
    try {
        if (req.user) {
            const { cli_id, sessionid } = req.user;
            console.log(`[preHandler] User cli_id: ${cli_id}, sessionid: ${sessionid}`);

            // Check if the user with this access token sessionid has logged out
            const r1 = await fastify.hasLoggedOut(cli_id, sessionid);
            console.log('[preHandler] Checking if this user has already logged out...');

            if (r1.length > 0) {
                console.log('[preHandler] User has already logged out...YES');
                return reply.send(fastify.httpErrors.unauthorized('Invalid Session'));
            }
            console.log('[preHandler] User has already logged out...NO');
        }
    } catch (error) {
        console.error('[preHandler] Error in preHandler hook:', error);
        return reply.send(fastify.httpErrors.internalServerError('Internal Server Error'));
    }
});


console.log('addHook(\'onError\', async (request, reply, error)');



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
        console.log(`[onError] Error queue size: ${q.size()}, max size: ${process.env.ERROR_Q_MAX_SIZE}`);
        request.log.error(errstack);

        // Push if in-memory threshold is not reached
        if (q.size() < process.env.ERROR_Q_MAX_SIZE) q.push(obj);
    } catch (e) {
        request.log.error(`[onError] Error in onError hook IGNORING: ${e}`);
    }
});

/** subscribe to diagnostic events * */

console.log('onResponse.subscribe((data)');

onResponse.subscribe((data) => {
    const cli_id = data.request.user?.cli_id;
    const { url } = data.request;
    const responseTimeInMillis = data.reply.getResponseTime();
    const obj = { cli_id, url, response_in_millis: responseTimeInMillis };
    const q = telemetricTon.getTelemetricQ();

    console.log(`[onResponse] Response time for ${url}: ${responseTimeInMillis}ms`);

    // Push if in-memory threshold is not reached
    if (q.size() < process.env.TELEMETRIC_Q_MAX_SIZE) q.push(obj);
});

/**  Do not change the following sections *********** */
/* This loads all plugins defined in plugins folder which that are reused
through your application */

console.log('AutoLoad plugins');

fastify.register(AutoLoad, {
    dir: path.join(__dirname, 'plugins'),
});

// This loads all plugins defined in routes
// define your routes in one of these

console.log('AutoLoad routes');

fastify.register(AutoLoad, {
    dir: path.join(__dirname, 'routes'),
});
/** ************************************************* */

console.log('process.env.HOST : '+process.env.HOST);
console.log('process.env.PORT : '+process.env.PORT);
const start = async () => {
    try {
        await fastify.listen(PORT, HOST);
        fastify.swagger();
        console.log('=>', fastify.printRoutes({ commonPrefix: false }));
        console.log(`Server listening on http://${HOST}:${PORT}`);
    } catch (err) {
        console.error(`[start] Error starting server: ${err}`);
        process.exit(1);
    }
};

// Run the server!
start();