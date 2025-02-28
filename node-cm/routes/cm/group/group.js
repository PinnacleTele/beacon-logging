/* eslint-disable no-restricted-syntax */
const _ = require('lodash');
const mtz = require('moment-timezone');
const jsunicode = require('jsunicode');
const path = require('path');

const { gnewSchema, gnameuniqueSchema, groupsSchema, ginfoSchema, gupdateSchema, gdeleteSchema, sharedGroupsSchema, groupsForCampaignSchema } = require('../../../schema/group-schema');

/**
 * setup some routes
 * @param {import("fastify").FastifyInstance} fastify
 * @param {*} opts
 */
async function group(fastify, opts) {
    fastify.addHook('preValidation', async (req, reply) => {
        console.log('preValidation hook called');
        return req.jwtVerify();
    });

    fastify.get('/groups', { schema: groupsSchema }, async (req, reply) => {
        try {
            const { tz, cli_id } = req.user; // e.g Asia/Calcutta
            const { g_type, status } = req.query;

            const result = await fastify.getGroups(cli_id, g_type, status);

            console.log('/groups total groups => ', result.length);

            // TODO: re-visit for await loop for better implementation
            for await (const obj of result) {
                const cts = _.get(obj, 'created_ts', null);
                const mts = _.get(obj, 'modified_ts', null);
                const ctsMoment = mtz.tz(cts, tz);
                const mtsMoment = mtz.tz(mts, tz);

                // get total contacts from redis
                const g_id = _.get(obj, 'id');
                const g_type = _.get(obj, 'g_type');
                const totalContacts = await fastify.getTotalContacts(cli_id, g_id, g_type);
                // convert total to human format
                const totalHuman = fastify.coolFormat(totalContacts, 0);
                _.set(obj, 'total_human', totalHuman);
                _.set(obj, 'total', +totalContacts);

                if (!_.isNull(cts)) {
                    // convert to acc tz
                    const formattedDt = ctsMoment.format('MMM DD, YYYY HH:mm:ss');
                    _.set(obj, 'created_ts', formattedDt);
                    _.set(obj, 'created_ts_unix', ctsMoment.unix());
                }
                if (!_.isNull(mts)) {
                    // convert to acc tz
                    const formattedDt = mtsMoment.format('MMM DD, YYYY HH:mm:ss');
                    _.set(obj, 'modified_ts', formattedDt);
                    _.set(obj, 'modified_ts_unix', mtsMoment.unix());
                }

                // entries from group_master represents the groups owned/created by them
                _.set(obj, 'is_owner', true);
            }

            return result;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);

            return fastify.httpErrors.createError(500, 'Could not get groups. Please try again', { code });
        }
    });

    fastify.get('/groupsForCampaign', { schema: groupsForCampaignSchema }, async (req, reply) => {
        try {
            const { tz, cli_id } = req.user; // e.g Asia/Calcutta
            const { g_type } = req.query;
            const payload = [];

            // TODO: pass actual parameters instead of req object
            const result = await fastify.getGroupsForCampaign(cli_id, g_type);

            console.log('/groupsForCampaign total groups => ', result.length);

            // TODO: re-visit for await loop for better implementation
            for await (const obj of result) {
                const cts = _.get(obj, 'created_ts', null);
                const mts = _.get(obj, 'modified_ts', null);
                const ctsMoment = mtz.tz(cts, tz);
                const mtsMoment = mtz.tz(mts, tz);

                // get total contacts from redis
                const g_id = _.get(obj, 'id');
                const g_type = _.get(obj, 'g_type');

                const totalContacts = await fastify.getTotalContacts(cli_id, g_id, g_type);

                if (+totalContacts > 0) {
                    // convert total to human format
                    const totalHuman = fastify.coolFormat(totalContacts, 0);
                    _.set(obj, 'total_human', totalHuman);
                    _.set(obj, 'total', +totalContacts);

                    if (!_.isNull(cts)) {
                        // convert to acc tz
                        const formattedDt = ctsMoment.format('DD-MM-YYYY HH:mm:ss z');
                        _.set(obj, 'created_ts', formattedDt);
                        _.set(obj, 'created_ts_unix', ctsMoment.unix());
                    }
                    if (!_.isNull(mts)) {
                        // convert to acc tz
                        const formattedDt = mtsMoment.format('DD-MM-YYYY HH:mm:ss z');
                        _.set(obj, 'modified_ts', formattedDt);
                        _.set(obj, 'modified_ts_unix', mtsMoment.unix());
                    }

                    // entries from group_master represents the groups owned/created by them
                    // entries from group_user_mapping represents assigned groups, not the owner
                    const isOwner = _.get(obj, 'is_owner');
                    if (+isOwner === 0) _.set(obj, 'is_owner', false);
                    if (+isOwner === 1) _.set(obj, 'is_owner', true);

                    payload.push(obj);
                }
            }

            return payload;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);

            return fastify.httpErrors.createError(500, 'Could not get groups. Please try again', { code });
        }
    });

    fastify.get('/sharedgroups', { schema: sharedGroupsSchema }, async (req, reply) => {
        try {
            const { cli_id, tz } = req.user; // e.g Asia/Calcutta

            const result = await fastify.getSharedGroups(cli_id);

            console.log('/sharedgroups total  => ', result.length);

            // TODO: re-visit for await loop for better implementation
            for await (const obj of result) {
                const cts = _.get(obj, 'created_ts', null);
                const mts = _.get(obj, 'modified_ts', null);
                const ctsMoment = mtz.tz(cts, tz);
                const mtsMoment = mtz.tz(mts, tz);

                // get total contacts from redis
                const g_id = _.get(obj, 'id');
                const g_type = _.get(obj, 'g_type');
                const totalContacts = await fastify.getTotalContacts(cli_id, g_id, g_type);
                // convert total to human format
                const totalHuman = fastify.coolFormat(totalContacts, 0);
                _.set(obj, 'total_human', totalHuman);
                _.set(obj, 'total', +totalContacts);

                if (!_.isNull(cts)) {
                    // convert to acc tz
                    const formattedDt = ctsMoment.format('DD-MM-YYYY HH:mm:ss z');
                    _.set(obj, 'created_ts', formattedDt);
                    _.set(obj, 'created_ts_unix', ctsMoment.unix());
                }
                if (!_.isNull(mts)) {
                    // convert to acc tz
                    const formattedDt = mtsMoment.format('DD-MM-YYYY HH:mm:ss z');
                    _.set(obj, 'modified_ts', formattedDt);
                    _.set(obj, 'modified_ts_unix', mtsMoment.unix());
                }
            }

            return result;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);

            return fastify.httpErrors.createError(500, 'Could not get shared groups. Please try again', { code });
        }
    });

    fastify.post('/gnew', { schema: gnewSchema }, async (req, reply) => {
        try {
            const { cli_id, user } = req.user;
            let resp = '';
            const valuesArr = [];
            const { g_name, g_visibility, g_type, files } = req.body;

            const id = fastify.nanoid();

            console.log('/gnew incoming params => ', [g_name, g_visibility, g_type, files]);

            /* check if group name is unique */
            const result1 = await fastify.isGroupNameFound({ cli_id, g_name });
            const { counts } = result1[0];

            console.log('matching group name count => ', counts);
            const isUnique = (counts === 0);

            if (!isUnique) {
                resp = {
                    statusCode: fastify.CONSTANTS.GROUP_NAME_NOT_UNIQUE_CODE,
                    message: 'Group name is not unique',
                    status: 'failed',
                };
                reply.code(200);
            } else {
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
                    valuesArr.push([fastify.nanoid(), id, rFilename, _.get(obj, 'filename'), ext, fileloc, _.get(obj, 'count')]);
                });

                const r1 = await fastify.createGroup({
                    cli_id,
                    g_name,
                    g_type,
                    g_visibility,
                    id,
                    valuesArr,
                });

                // TODO: verify the inserted row

                resp = { statusCode: 201, message: 'Group has been created successfully' };
                reply.code(201);
            }

            return reply.send(resp);
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);

            return fastify.httpErrors.createError(500, 'Could not create group. Please try again', { code });
        }
    });

    fastify.get('/gnameunique', { preValidation: [], schema: gnameuniqueSchema }, async (req, reply) => {
        try {
            const { cli_id } = req.user;
            const { g_name } = req.query;

            const result = await fastify.isGroupNameFound({ cli_id, g_name });
            const { counts } = result[0];

            console.log('matching template name count => ', counts);
            const payload = { isUnique: (counts === 0) };

            return payload;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not check for uniqueness. Please try again', { code });

            return e;
        }
    });

    fastify.get('/ginfo', { preValidation: [], schema: ginfoSchema }, async (req, reply) => {
        try {
            const { id } = req.query;
            const { tz, cli_id } = req.user; // Asia/Calcutta

            const result = await fastify.getGroupInfo(id);

            console.log(`/ginfo resp from db for if ${id} is => ${JSON.stringify(result)}`);
            const resp = _.get(result, '0', {});

            if (!_.isEmpty(resp)) {
                const cts = _.get(resp, 'created_ts', null);
                const mts = _.get(resp, 'modified_ts', null);
                console.log('tz', tz);
                const ctsMoment = mtz.tz(cts, tz);
                const mtsMoment = mtz.tz(mts, tz);

                // get total contacts from redis
                const g_id = _.get(resp, 'id');
                const g_type = _.get(resp, 'g_type');
                const totalContacts = await fastify.getTotalContacts(cli_id, g_id, g_type);
                // convert total to human format
                const totalHuman = fastify.coolFormat(totalContacts, 0);
                _.set(resp, 'total_human', totalHuman);
                _.set(resp, 'total', +totalContacts);

                if (!_.isNull(cts)) {
                    // convert to acc tz
                    const formattedDt = ctsMoment.format('DD-MM-YYYY HH:mm:ss z');
                    _.set(resp, 'created_ts', formattedDt);
                    _.set(resp, 'created_ts_unix', ctsMoment.unix());
                }
                if (!_.isNull(mts)) {
                    // convert to acc tz
                    const formattedDt = mtsMoment.format('DD-MM-YYYY HH:mm:ss z');
                    _.set(resp, 'modified_ts', formattedDt);
                    _.set(resp, 'modified_ts_unix', mtsMoment.unix());
                }

                // entries from group_master represents the groups owned/created by them
                _.set(resp, 'is_owner', true);
            }

            return resp;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not get group info. Please try again', { code });

            return e;
        }
    });

    fastify.post('/gupdate', { preValidation: [], schema: gupdateSchema }, async (req, reply) => {
        try {
            const { cli_id } = req.user;
            const { g_name, id, g_visibility } = req.body;

            const result = await fastify.updateGroup({ g_name, id, cli_id, g_visibility });

            // TODO: need to check the updated status and reply accordingly { affectedRows: 1, insertId: 0, warningStatus: 0 }

            console.log('/gupdate [group_master] resp from db => ', result);

            // if g_visibility changes from shared to private, delete all the entries (all cli_id) from group_user_mapping which are already mapped
            if (_.eq(g_visibility, 'private')) {
              const r1 = await fastify.unshareGroup(cli_id, id);
              console.log('/gupdate [group_user_mapping] resp from db => ', r1);
            }
            const resp = { statusCode: 200, message: 'Group has been modified successfully' };

            return resp;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not update group details. Please try again', { code });

            return e;
        }
    });

    fastify.post('/gdelete', { preValidation: [], schema: gdeleteSchema }, async (req, reply) => {
        try {
            const { cli_id } = req.user;
            const { ids } = req.body;

            console.log('/gdelete incoming ids => ', ids);

            const result = await fastify.getGroupInfoForIds(ids);
            if (result.length === 0) return fastify.httpErrors.createError(400, 'Could not delete group(s). Selected groups are invalid');

            console.log(`/gdelete total group from db => ${result.length}`);
            // segregate by g_type
            const groupBy = _.groupBy(result, 'g_type');
            console.log(groupBy);
            const normalIds = _.map(_.get(groupBy, 'normal', []), 'id');
            const excludeIds = _.map(_.get(groupBy, 'exclude', []), 'id');
            console.log(normalIds, excludeIds);

            let normalInUse = false;
            let excludeInUse = false;
            if (normalIds.length > 0) {
                const [r1, r2, r3] = await Promise.all([fastify.isNormalGroupInUseInCG(normalIds), fastify.isNormalGroupInUseInCF(normalIds), fastify.isNormalGroupInUseInSched(normalIds)]);
                const r1Count = _.get(r1[0], 'count', 0);
                const r2Count = _.get(r2[0], 'count', 0);
                const r3Count = _.get(r3[0], 'count', 0);
                console.log('normal groups in use => ', r1Count, r2Count, r3Count);
                normalInUse = (r1Count + r2Count + r3Count) > 0;
            }

            if (excludeIds.length > 0) {
                const [r1, r2, r3] = await Promise.all([fastify.isExcludeGroupInUseInCG(excludeIds), fastify.isExcludeGroupInUseInCF(excludeIds), fastify.isExcludeGroupInUseInSched(excludeIds)]);
                const r1Count = _.get(r1[0], 'count', 0);
                const r2Count = _.get(r2[0], 'count', 0);
                const r3Count = _.get(r3[0], 'count', 0);
                console.log('exclude groups in use => ', r1Count, r2Count, r3Count);
                excludeInUse = (r1Count + r2Count + r3Count) > 0;
            }

            // throw err if groups/exclude are in use
            if (normalInUse || excludeInUse) {
                let message = 'Selected group is used in campaign. Deletion of group is not permitted while in use';
                if (ids.length > 1) {
                    message = 'Few of the selected group is used in campaign. Deletion of group is not permitted while in use';
                }

                const resp = {
                    statusCode: fastify.CONSTANTS.GROUP_IN_USE_CODE,
                    message,
                };
                reply.code(200);
                return reply.send(resp);
            }

            const result3 = await fastify.deleteGroup({ ids, cli_id });
            // TODO: need to check the deleted status and reply accordingly { affectedRows: 1, insertId: 0, warningStatus: 0 }
            try {
                // delete the corresponding contacts and details from redis
                if (normalIds.length > 0) {
                    const result4 = await fastify.deleteContactsAndDetails(normalIds, 'normal');
                }
                if (excludeIds.length > 0) {
                    const result5 = await fastify.deleteContactsAndDetails(excludeIds, 'exclude');
                }
            } catch (e) {
                req.log.error(`error deleting contacts and details from redis *** IGNORING ***  =>${e}`);
            }

            const resp = { statusCode: 200, message: 'Group has been deleted successfully', status: 'success' };

            return resp;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not delete the group. Please try again', { code });

            return e;
        }
    });
}

module.exports = group;
