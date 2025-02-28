const _ = require('lodash');
const mtz = require('moment-timezone');
const jsunicode = require('jsunicode');
const path = require('path');

const {
    contactsSchema, cdeleteSchema, cupdateSchema, caddSchema,
} = require('../../../../schema/contact-schema');

/**
 * setup some routes
 * @param {import("fastify").FastifyInstance} fastify
 * @param {*} opts
 */
async function contacts(fastify, opts) {
    fastify.addHook('preValidation', async (req, reply) => {
        console.log('preValidation hook called');
        return req.jwtVerify();
    });

    fastify.get('/contacts', { schema: contactsSchema }, async (req, reply) => {
        try {
            const { cli_id } = req.user;
            const { g_id, match, g_type } = req.query;
            const { MAX_CONTACTS_LIMIT } = process.env;

            let result = [];
            const finalresult = [];
            let gRedis = '';
            let contactsKey = '';
            let contactDetailsKey = '';

            if (_.eq(g_type, 'normal')) {
                contactsKey = `groups:contacts:${g_id}`;
                contactDetailsKey = `groups:contactdetails:${g_id}`;
                gRedis = 'GROUP';
            } else {
                contactsKey = `excludegroups:contacts:${g_id}`;
                contactDetailsKey = `excludegroups:contactdetails:${g_id}`;
                gRedis = 'EGROUP';
            }

            // get the status of g_id. this will be used by GUI to enable/disable functionality
            const result1 = await fastify.getGroupInfo(g_id);
            const status = _.get(result1[0], 'status', '');

            // get the total conatcts in this group
            const total = await fastify.redis[gRedis].scard(contactsKey);
            const totalHuman = fastify.coolFormat(+total, 0);

            const stream = await fastify.redis[gRedis].sscanStream(contactsKey, {
                match: (match != '*' ? `*${match}*` : match),
                count: 1000,
            });

            stream.on('data', (resultKeys) => {
                // `resultKeys` is an array of strings representing key names.
                // Note that resultKeys may contain 0 keys, and that it will sometimes
                // contain duplicates due to SCAN's implementation in Redis.
                result.push(...resultKeys);
                if (result.length > MAX_CONTACTS_LIMIT) stream.close();
            });

            stream.on('end', async () => {
                console.log('all keys have been visited / reached max limit');
                console.log(result);
                if (result.length > 0) {
                    // trim the length to MAX_CONTACTS_LIMIT
                    result = _.slice(result, 0, MAX_CONTACTS_LIMIT);
                    const r1 = await fastify.redis[gRedis].hmget(contactDetailsKey, result);
                    _.forEach(result, (val, i) => {
                        const arr = _.split(r1[i], '~');
                        finalresult.push({ mobile: val, name: arr[0], email: arr[1] });
                    });
                }
                const finalObj = {
                    total,
                    total_human: totalHuman,
                    status,
                    data: finalresult,
                };
                return reply.send(finalObj);
            });

            stream.on('error', (err) => {
                const code = fastify.nanoid();
                req.log.error(`error ${code}  =>${err}`);

                const e = fastify.httpErrors.createError(500, `Could not get contacts for group ${g_id} Please try again`, { code });
                return reply.send(e);
            });
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);

            return fastify.httpErrors.createError(500, 'Could not get contacts. Please try again', { code });
        }
    });

    fastify.post('/cdelete', { preValidation: [], schema: cdeleteSchema }, async (req, reply) => {
        try {
            const { cli_id } = req.user;
            const { g_id, g_type, mobiles } = req.body;

            const result = await fastify.deleteContact(cli_id, g_id, g_type, mobiles);
            // TODO: need to check the deleted status and reply accordingly { affectedRows: 1, insertId: 0, warningStatus: 0 }

            console.log('/cdelete resp from redis => ', result);
            const resp = { statusCode: 200, message: 'Contacts has been deleted successfully' };

            return resp;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not delete the contacts. Please try again', { code });

            return e;
        }
    });

    fastify.post('/cupdate', { preValidation: [], schema: cupdateSchema }, async (req, reply) => {
        try {
            const { cli_id } = req.user;
            const {
                g_id, g_type, mobile, name, email,
            } = req.body;

            const result = await fastify.updateContactDetails(cli_id, g_id, g_type, mobile, name, email);

            const resp = { statusCode: 200, message: 'Contact has been modified successfully' };

            return resp;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not update contact details. Please try again', { code });

            return e;
        }
    });

    fastify.post('/cadd', { preValidation: [], schema: caddSchema }, async (req, reply) => {
        try {
            const { cli_id, user } = req.user;
            const {
                g_id, g_type, mobile, name, email, files,
            } = req.body;
            const id = fastify.nanoid();
            const valuesArr = [];

            if (files.length === 0) {
                console.log('/cadd its manual addition of contact');
                const result = await fastify.addContactDetails(cli_id, g_id, g_type, mobile, name, email);
            } else {
                console.log('/cadd its file upload');

                // get the base path from config params table
                let result = await fastify.getGroupBasePathFromConfigParams();
                result = _.get(result, '0', {});

                if (_.isEmpty(result)) {
                    throw new Error('group.file.store.path config not found in config_params table');
                }

                const basePath = _.get(result, 'value');

                _.forEach(files, (obj) => {
                    const rFilename = _.get(obj, 'r_filename');
                    const ext = path.extname(rFilename);
                    const fileloc = path.join(basePath, _.toLower(user), rFilename);
                    valuesArr.push([fastify.nanoid(), g_id, rFilename, _.get(obj, 'filename'), ext, fileloc, _.get(obj, 'count')]);
                });

                const r1 = await fastify.addContactFiles(cli_id, g_id, g_type, mobile, name, email, valuesArr);
            }
            // TODO: verify the inserted row
            const resp = { statusCode: 201, message: 'Contact has been created successfully' };
            reply.code(201);

            return reply.send(resp);
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);

            return fastify.httpErrors.createError(500, 'Could not create contact. Please try again', { code });
        }
    });
}

module.exports = contacts;
