/* eslint-disable no-restricted-syntax */
const _ = require('lodash');
const mtz = require('moment-timezone');
const path = require('path');
const jsunicode = require('jsunicode');

const { dlttelcosSchema, dltaddSchema, dltstatsSchema, dlttemplatesSchema, dltuploadsSchema, dltsenderidsSchema, dltentityidstatsSchema, dltentityidsforfilterSchema, dlttemplateidsforfilterSchema } = require('../../../schema/dlt-schema');

/**
 * setup some routes
 * @param {import("fastify").FastifyInstance} fastify
 * @param {*} opts
 */
async function rsummary(fastify, opts) {
    fastify.addHook('preValidation', async (req, reply) => {
        console.log('preValidation hook called');
        return req.jwtVerify();
    });

    fastify.get('/dltstats', { preValidation: [], schema: dltstatsSchema }, async (req, reply) => {
        try {
            const { cli_id, msg_type } = req.user;
            const payload = {
                total_templates: 0,
                total_senderid: 0,
                total_entityid: 0,
                total_templates_human: 0,
            };

            console.log('/dltstats called...');

            const [r1, r2, r3] = await Promise.all([fastify.getDLTTemplates(cli_id, msg_type, null), fastify.getAllSenderids(cli_id, msg_type), fastify.getAllEntityids(cli_id, msg_type)]);

            // get the unique templateids for the total
            const uniqTemplateArr = _.uniqBy(r1, 'template_id');
            console.log('/dltstats resp from db ', uniqTemplateArr.length, r2.length, r3.length);

            _.set(payload, 'total_templates', uniqTemplateArr.length);
            _.set(payload, 'total_senderid', r2.length);
            _.set(payload, 'total_entityid', r3.length);
            _.set(payload, 'total_templates_human', fastify.coolFormat(r1.length, 0));

            return payload;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);

            return fastify.httpErrors.createError(500, 'Could not get dlt stats. Please try again', { code });
        }
    });

    fastify.get('/dlttelcos', { preValidation: [], schema: dlttelcosSchema }, async (req, reply) => {
        try {
            console.log('/dlttelcos called...');
            const result = await fastify.getDLTTelcos();
            console.log('/dlttelcos resp from db ', result);

            return result;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);

            return fastify.httpErrors.createError(500, 'Could not get dlt telcos. Please try again', { code });
        }
    });

    // for the senderid card
    fastify.get('/dltsenderids', { preValidation: [], schema: dltsenderidsSchema }, async (req, reply) => {
        try {
            const { cli_id, msg_type } = req.user;

            console.log('/dltsenderids called...');
            const result = await fastify.getAllSenderids(cli_id, msg_type);
            console.log('/dltsenderids resp from db. total senderids =>', result.length);

            return result;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);

            return fastify.httpErrors.createError(500, 'Could not get senderids. Please try again', { code });
        }
    });

    fastify.get('/dltentityidsforfilter', {
        preValidation: [],
        schema: dltentityidsforfilterSchema,
    }, async (req, reply) => {
        try {
            const { cli_id, msg_type } = req.user;
            const entityids = [{ entity_id: 'All Entity', entity_id_id: 'all' }];

            console.log('/dltentityidsforfilter called...');

            const result = await fastify.getAllEntityids(cli_id, msg_type);
            console.log('/dltentityidsforfilter resp from db. total entityids =>', result.length);

            for (const obj of result) {
                entityids.push({
                    entity_id: _.get(obj, 'entity_id'),
                    entity_id_id: _.get(obj, 'entity_id'),
                });
            }

            return entityids;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);

            return fastify.httpErrors.createError(500, 'Could not get entityids. Please try again', { code });
        }
    });

    fastify.get('/dlttemplateidsforfilter', {
        preValidation: [],
        schema: dlttemplateidsforfilterSchema,
    }, async (req, reply) => {
        try {
            const { cli_id, msg_type } = req.user;
            const { entity_id } = req.query;
            const templateids = [{ dlt_template_id: 'all', dlt_template_name: 'All Template' }];
            let eid = entity_id;

            console.log('/dlttemplateidsforfilter incoming params =>', entity_id);

            if (_.eq(_.toLower(eid), 'all')) eid = null;
            const result = await fastify.getDLTTemplates(cli_id, msg_type, eid);

            console.log('/dlttemplateidsforfilter resp from db. total dlt templates =>', result.length);

            for (const obj of result) {
                let tname = _.get(obj, 'template_name', null);
                const tid = _.get(obj, 'template_id', null);

                if (_.isEmpty(tname)) {
                    tname = tid;
                }
                templateids.push({
                    dlt_template_id: tid,
                    dlt_template_name: tname,
                });
            }

            return templateids;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);

            return fastify.httpErrors.createError(500, 'Could not get senderids. Please try again', { code });
        }
    });

    // for the entity card
    fastify.get('/dltentityidstats', { preValidation: [], schema: dltentityidstatsSchema }, async (req, reply) => {
        try {
            const { cli_id, msg_type } = req.user;
            const payload = [];

            console.log('/dltentityidstats called...');

            const result = await fastify.getAllEntityidsAndTemplateCount(cli_id, msg_type);

            console.log('/dltentityidstats resp from db. total entityids =>', result.length);

            _.forEach(result, (obj, i) => {
                const entityid = _.get(obj, 'entity_id', null);
                const total = _.get(obj, 'total', null);
                const totalHuman = fastify.coolFormat(+total, 0);

                payload.push({ dlt_entity_id: entityid, total, total_human: totalHuman });
            });

            return payload;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);

            return fastify.httpErrors.createError(500, 'Could not get entityid stats. Please try again', { code });
        }
    });

    fastify.get('/dlttemplates', { schema: dlttemplatesSchema }, async (req, reply) => {
        try {
            const { entity_id, dlt_template_id } = req.query;
            const { cli_id, tz, msg_type } = req.user;

            console.log('/dlttemplates incoming params => ', [entity_id, dlt_template_id]);

            const result = await fastify.getDLTTemplatesv2(cli_id, msg_type, entity_id, dlt_template_id);
            const respArr = [];

            console.log('/dlttemplates total templates => ', result.length);

            _.forEach(result, (obj, i) => {
                const header = _.get(obj, 'header', null);
                let tname = _.get(obj, 'template_name', null);
                const tid = _.get(obj, 'template_id', null);
                const tmsg = _.get(obj, 'template_content', null);
                const entityid = _.get(obj, 'entity_id', null);
                const patternType = _.get(obj, 'pattern_type', 0);
                const cts = _.get(obj, 'created_ts', null);
                const ctsMoment = mtz.tz(cts, tz);
                let formattedDt = null;
                let unixTs = null;

                if (!_.isNull(cts)) {
                    // convert to acc tz
                    formattedDt = ctsMoment.format('MMM DD YYYY HH:mm');
                    unixTs = ctsMoment.unix();
                }

                // if (patternType === 1) {
                //     try {
                //         // replace all the occurrence of {#var#} with the equivalent hex value
                //         const replacedMsgInHex = _.replace(tmsg, new RegExp('{#var#}', 'g'), process.env.DLT_TEMPLATE_VAR_HEX);
                //         // convert the hex to unicode
                //         tmsg = jsunicode.decode(replacedMsgInHex, { encoding: jsunicode.constants.encoding.utf16 });
                //     } catch (e) {
                //         console.error(e);
                //         // ignore
                //     }
                // }

                if (_.isEmpty(tname)) {
                    tname = tid;
                }
                const rowObj = {
                    header,
                    dlt_template_id: tid,
                    dlt_template_name: tname,
                    dlt_template_content: tmsg,
                    dlt_entity_id: entityid,
                    pattern_type: patternType,
                    created_ts: formattedDt,
                    created_ts_unix: unixTs,
                };
                respArr.push(rowObj);
            });

            return respArr;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);

            return fastify.httpErrors.createError(500, 'Could not get dlt templates. Please try again', { code });
        }
    });

    fastify.get('/dltuploads', { schema: dltuploadsSchema }, async (req, reply) => {
        try {
            const { dateselectiontype } = req.query;
            const { cli_id, tz } = req.user;
            const respArr = [];

            console.log('/dltuploads incoming params => ', [dateselectiontype]);

            const { fromdatetimeStrInIST, todatetimeStrInIST, typeofdate } = await fastify.getFromAndToDate(tz, dateselectiontype, null, null, true);
            console.log('/dltuploads from and to date => ', fromdatetimeStrInIST, todatetimeStrInIST, typeofdate);

            const result = await fastify.getDLTFileUploads(cli_id, fromdatetimeStrInIST, todatetimeStrInIST);

            console.log('/dlttemplates total templates => ', result.length);

            _.forEach(result, (obj, i) => {
                const cts = _.get(obj, 'created_ts', null);
                // TODO: Need to show display telco name
                const telco = _.get(obj, 'telco', 0);
                const total = _.get(obj, 'total', 0);
                const status = _.get(obj, 'status', 0);
                const dlt_entity_id = _.get(obj, 'entity_id', 0);
                const totalHuman = fastify.coolFormat(+total, 0);

                const ctsMoment = mtz.tz(cts, tz);
                let formattedDt = null;
                let unixTs = null;

                if (!_.isNull(cts)) {
                    // convert to acc tz
                    formattedDt = ctsMoment.format('MMM DD YYYY HH:mm');
                    unixTs = ctsMoment.unix();
                }

                const rowObj = {
                    total,
                    total_human: totalHuman,
                    telco,
                    dlt_entity_id,
                    status,
                    created_ts: formattedDt,
                    created_ts_unix: unixTs,
                };
                respArr.push(rowObj);
            });

            return respArr;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);

            return fastify.httpErrors.createError(500, 'Could not get dlt file uploads. Please try again', { code });
        }
    });

    fastify.post('/dltadd', { preValidation: [], schema: dltaddSchema }, async (req, reply) => {
        try {
            const { cli_id, user } = req.user;
            const { entityid, telco, files } = req.body;
            const id = fastify.nanoid();
            const valuesArr = [];

            console.log('/dltadd incoming params => ', [entityid, telco, files]);

            // get the base path from config params table
            let result = await fastify.getDLTBasePathFromConfigParams();
            result = _.get(result, '0', {});

            if (_.isEmpty(result)) {
                throw new Error('dlt.template.file.store.path config not found in config_params table');
            }

            const basePath = _.get(result, 'value');

            _.forEach(files, (obj) => {
                const rFilename = _.get(obj, 'r_filename');
                const fileloc = path.join(basePath, _.toLower(user), rFilename);
                // id, d_id, filename, filename_ori, fileloc
                // valuesArr.push([fastify.nanoid(), id, rFilename, _.get(obj, 'filename'), fileloc, _.get(obj, 'count')]);
                valuesArr.push([fastify.nanoid(), id, rFilename, _.get(obj, 'filename'), fileloc, 0]);
            });

            // find template group id for this user
            const result1 = await fastify.findUserById(cli_id);
            const userObj = _.get(result1, '0', {});

            if (_.isEmpty(userObj)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            }

            const r1 = await fastify.addDLTTemplates(id, cli_id, user, entityid, userObj.dlt_templ_grp_id, telco, valuesArr);

            // TODO: verify the inserted row
            const resp = { statusCode: 200, message: 'DLT template files were accepted for processing' };
            reply.code(200);

            return reply.send(resp);
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);

            return fastify.httpErrors.createError(500, 'Could not process the request. Please try again', { code });
        }
    });
}

module.exports = rsummary;
