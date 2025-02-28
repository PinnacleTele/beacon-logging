const path = require('path');
const AutoLoad = require('fastify-autoload');
const dotenv = require('dotenv');
const Fastify = require('fastify');
const schedule = require('node-schedule');

const downloadStatusTask = require('./task/log.download.statuscheck.task');

dotenv.config();

const { HOST, PORT } = process.env;

const fastify = Fastify({
    logger: {
        prettyPrint: false,
        // prettyPrint: {colorize: true, translateTime: 'SYS:standard'},
        level: 'debug',
        file: './log/cm-scheduler.log',
    },
    genReqId(req) {
        const reqid = fastify.nanoid();
        return reqid;
    },
    bodyLimit: 1048576 * 2, // 2mb
});


/** Run the schedulers ** */
const job1 = schedule.scheduleJob(`*/${process.env.DOWNLOAD_STATUS_CHECK_INETRVAL_INSEC} * * * * *`, downloadStatusTask);

/** ****************************************************************************************************** */


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
        console.log('=>', fastify.printRoutes({ commonPrefix: false }));
        fastify.log.info(`listening on ${fastify.server.address().port}`);
    } catch (err) {
        fastify.log.error(err);
        process.exit(1);
    }
};

// Run the server!
start();
