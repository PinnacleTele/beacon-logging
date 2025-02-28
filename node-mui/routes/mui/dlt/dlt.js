const _ = require('lodash');
const mtz = require('moment-timezone');
const { unameuniqueSchema } = require('../../../schemas/account-schema');

async function dlt(fastify) {
    fastify.addHook('preValidation', async (req) => req.jwtVerify());

    fastify.get('/isDLTnameUnique', { schema: unameuniqueSchema }, async (req) => {
        try {
            const { tgrpname } = req.query.uname;
            const result = await fastify.checkDLTnameUniqueness(tgrpname);
            const { counts } = result[0];

            const payload = { isUnique: (counts === 0) };
            return payload;
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get username uniqueness. Please try again', { err });
            return e;
        }
    });

    fastify.post('/dltnew', {}, async (req, reply) => {
        try {
            const { action, dlt_grp_id, dlt_grp_name } = req.body;

            const result = await fastify.checkDLTnameUniqueness(dlt_grp_name);
            const { counts } = result[0];
            if (counts > 0) {
                const resp = { statusCode: -600, message: 'DLT Groupname exists already' };
                reply.code(200);
                return reply.send(resp);
                // const e = fastify.httpErrors.createError(500, 'DLT Groupname exists already');
                // return e
            }
            await fastify.insertNewDLT(action, dlt_grp_id, dlt_grp_name);

            const resp = { statusCode: 200, message: 'DLT has been created successfully' };
            reply.code(200);
            return reply.send(resp);
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not insert a new DLT templ group. Please try again', { code });
            return e;
        }
    });

    fastify.get('/dltlist', {}, async (req) => {
        try {
           const result = await fastify.getAllDLTGroups();
         //  const allocUsers = await fastify.getAllocUsersCount();
           const assignedUsers = await fastify.getAssignedUsersCount();
            _.forEach(result, (obj) => {
            const dltId = _.get(obj, 'template_group_id', null);

           // const getAllocUserCnt = _.filter(allocUsers, (io) => io.template_group_id == dltId);
            const getAssigndUserCnt = _.filter(assignedUsers, (io) => io.dlt_templ_grp_id == dltId);

          /*   if (getAllocUserCnt.length > 0) {
              _.set(obj, 'total_alloc_users', getAllocUserCnt[0].counts);
            }  else { _.set(obj, 'total_alloc_users', 0); }
            */

            const cts = _.get(obj, 'created_ts', null);
            const tz = process.env.IST_ZONE_NAME;
            const ctsMoment = mtz.tz(cts, tz);
            if (!_.isNull(cts)) {
                // convert to acc tz
                const formattedDt = ctsMoment.format('MMM DD, YYYY HH:mm:ss z');
                _.set(obj, 'created_ts', formattedDt);
                _.set(obj, 'created_ts_unix', ctsMoment.unix());
            }
            if (getAssigndUserCnt.length > 0) {
                _.set(obj, 'total_users', getAssigndUserCnt[0].counts);
              } else { _.set(obj, 'total_users', 0); }
           });
           return result;
        } catch (err) {
            const code = fastify.nanoid();
            console.log(err);
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not get DLT list. Please try again', { code });
            // const e = fastify.httpErrors.createError(500, err);
            return e;
        }
    });
}
module.exports = dlt;
