/* eslint-disable global-require */
const _ = require('lodash');
const fs = require('fs');
const zlib = require('zlib');
const moment = require('moment');

const rclient = require('../factory/redis.telemetric.factory');

async function rootRoutes(fastify, opts) {
    fastify.get('/resptime', { preValidation: [] }, async (req, reply) => {
        try {
            const r1 = await rclient.zrange('cm:api:resp:delay', 0, 500, 'WITHSCORES', 'REV');

            let str = `${_.padEnd('route', 110)}time taken (millis)\n`;
            str += `${_.padEnd('---------', 110)}--------------------\n`;

            _.forEach(r1, (val, i) => {
                if (i % 2 === 0) {
                    str += `${_.padEnd(r1[i], 110)}`;
                } else {
                    str += `${r1[i]}\n`;
                }
            });
            return str;
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not update contact details. Please try again', { err });
            return e;
        }
    });
    // fastify.get('/downloadlogfilee', {}, async (req, reply) => {
    //     try {
    //         const { id } = req.query;
    //         // const { cli_id } = req.user;
    //
    //         console.log('/downloadlog incoming params => ', [id]);
    //
    //         // get file info
    //         const result = await fastify.getLogDownloadFileInfo(id);
    //         const obj = _.get(result, 0, {});
    //         console.log('/downloadlog resp from db =>', obj);
    //
    //         if (_.isEmpty(obj)) return fastify.httpErrors.badRequest('Could not find file information');
    //         const downloadpath = obj.download_path;
    //
    //         // check if the file exists
    //         try {
    //             if (!fs.existsSync(downloadpath)) {
    //                 return fastify.httpErrors.notFound('Could not download file');
    //             }
    //         } catch (err) {
    //             console.error(err);
    //         }
    //
    //         const filename = `log_${moment(obj.from_tz).format('YYYY-MM-DD')}-${moment(obj.to_tz).format('YYYY-MM-DD')}_${id}.csv`;
    //         // const readstream = fs.createReadStream('./package.json', 'utf8');
    //         const readstream = fs.createReadStream(obj.download_path, 'utf8');
    //
    //         reply.header('Content-Disposition', `attachment;filename=${filename}`);
    //         reply.send(readstream);
    //     } catch (err) {
    //         return fastify.httpErrors.createError(500, 'Could not download file. Please try again', { err });
    //     }
    // });

    // fastify.get('/', {}, async (req, reply) => {
    //     try {
    //         const con = await fastify.mariadb.getConnection();
    //         const result = await con.query('select count(1) from timezone');
    //         con.release();
    //         console.log('nanoid => ', fastify.nanoid());
    //
    //         return result;
    //     } catch (err) {
    //         req.log.error(`error =>${err}`);
    //         return err;
    //     }
    // });
    // fastify.get('/sscan', {}, async (req, reply) => {
    //     try {
    //         const result = [];
    //         const stream = await fastify.redis.GROUP.sscanStream('groups:contacts:o361lp84j667kw3vk6mb8okoeo7fj6it6xvi', {
    //             match: '*77*',
    //             count: 1000,
    //         });
    //
    //         stream.on('data', (resultKeys) => {
    //             // `resultKeys` is an array of strings representing key names.
    //             // Note that resultKeys may contain 0 keys, and that it will sometimes
    //             // contain duplicates due to SCAN's implementation in Redis.
    //             result.push(...resultKeys);
    //             if (result.length > 110) stream.close();
    //         });
    //         stream.on('end', async () => {
    //             console.log('all keys have been visited');
    //             const r1 = await fastify.redis.GROUP.hmget('groups:contactdetails:o361lp84j667kw3vk6mb8okoeo7fj6it6xvi', result);
    //             return reply.send(r1);
    //         });
    //         stream.on('error', (err) => {
    //             console.log('>>>>>>', err);
    //             return reply.send(err);
    //         });
    //     } catch (err) {
    //         req.log.error(`error =>${err}`);
    //         return err;
    //     }
    // });
}

module.exports = rootRoutes;
