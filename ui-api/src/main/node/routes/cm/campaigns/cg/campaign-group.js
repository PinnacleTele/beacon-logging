const fs = require('fs');
const os = require('os');
const path = require('path');
const moment = require('moment');
const momenttz = require('moment-timezone');
const _ = require('lodash');
const jsunicode = require('jsunicode');
const { cgSendSchema, cgScheduleSchema } = require('../../../../schema/campaigns-schema');

async function campaignsGroup(fastify, opts) {
  fastify.addHook('preValidation', async (req, reply) => {
    console.log('preValidation hook called');
    return req.jwtVerify();
  });

  async function processCampaign(req) {
    const { c_name, remove_dupe_yn, msg, c_type, c_lang_type, c_lang, dlt_entity_id, header, group_ids, exclude_group_ids, shorten_url_yn, intl_header, traffic_to } = req.body;
    let { template_id, dlt_template_id } = req.body;

    const { cli_id, user, sessionid } = req.user;
    const id = fastify.nanoid();
    const valuesArr = [];
    const groupsValuesArr = [];
    const resp = '';
    const content = msg;
    let template_type = null;
    let template_mobile_column = null;

    if (_.isEmpty(template_id)) template_id = null;

    // get template_type template_mobile_column dlt_template_id for this template_id
    if (!_.isEmpty(template_id)) {
      let r = await fastify.getTemplateInfo({ id: template_id });
      r = _.get(r, '0', {});

      if (_.isEmpty(r)) {
        throw new Error(`could not find the template entry in template_master for id ${template_id}`);
      }

      dlt_template_id = _.get(r, 'dlt_template_id');
      template_type = _.get(r, 't_type');
      template_mobile_column = _.get(r, 't_mobile_column');
    }

    // comma seperate exclude group ids
    const egids = _.join(exclude_group_ids, ',');

    _.forEach(group_ids, (gid) => {
      // id, c_id, group_id, exclude_group_ids
      groupsValuesArr.push([fastify.nanoid(), id, gid, egids]);
    });

    // check if the message is unicode, if so convert it to hex
    // if (_.eq(c_lang_type, 'unicode')) {
    //   content = jsunicode.encode(msg, { encoding: jsunicode.constants.encoding.utf16 }); // utf16
    // }

    console.log(groupsValuesArr);

    const obj = {
      cli_id,
      user,
      ip: req.ip,
      sessionid,
      c_name,
      msg: content,
      remove_dupe_yn,
      c_type,
      c_lang_type,
      c_lang,
      dlt_entity_id,
      dlt_template_id,
      header,
      id,
      valuesArr,
      groupsValuesArr,
      template_id,
      template_type,
      template_mobile_column,
      shorten_url_yn,
      intl_header,
    };

    return obj;
  }

  async function processSchedule(req, id) {
    const { schedule_list } = req.body;
    const { user } = req.user;
    const scAtValuesArr = [];

    _.forEach(schedule_list, (schObj) => {
      const dt = _.get(schObj, 'dt'); // yyyy-mm-dd
      const time = _.get(schObj, 'time'); // hh24:mi:ss
      const zone = _.get(schObj, 'zone_name');

      const incomingTZ = momenttz.tz(`${dt} ${time}`, zone);
      // convert to ist
      const istTZ = incomingTZ.clone().tz(process.env.IST_ZONE_NAME);
      const istFormatted = istTZ.format('YYYY-MM-DD HH:mm:ss');
      // id, cs_id, username, scheduled_ts, selected_dt, selected_zone
      scAtValuesArr.push([fastify.nanoid(), id, user, istFormatted, incomingTZ.toString(), zone]);
    });

    return scAtValuesArr;
  }

  fastify.post('/send', { schema: cgSendSchema }, async (req, reply) => {
    try {
      const { cli_id, bill_type } = req.user;

      // check if the user has wallet balance
      if (+bill_type === 1) {
        const amount = await fastify.walletBalance(cli_id);
        if (+amount <= 0) {
          const resp = {
            statusCode: fastify.CONSTANTS.NOT_ENOUGH_WALLET_BAL,
            message: 'You do not have wallet balance to send any campaign. Please add amount.',
          };
          reply.code(200);
          return reply.send(resp);
        }
      }

      const obj = await processCampaign(req);
      const r1 = await fastify.sendCampaign(obj);

      // TODO: verify the inserted row
      resp = { statusCode: 200, message: 'Campaign sent for processing' };
      reply.code(200);

      return reply.send(resp);
    } catch (err) {
      const code = fastify.nanoid();
      req.log.error(`error ${code}  =>${err}`);
      const e = fastify.httpErrors.createError(500, 'Could not send the campaign. Please try again', { code });

      return e;
    }
  });

  fastify.post('/schedule', { schema: cgScheduleSchema }, async (req, reply) => {
    try {
      const { group_ids, exclude_group_ids } = req.body;

      const obj = await processCampaign(req);
      const scAtValuesArr = await processSchedule(req, obj.id);

      // set this in obj
      _.set(obj, 'scAtValuesArr', scAtValuesArr);
      _.set(obj, 'filenames', null);
      _.set(obj, 'filenames_ori', null);
      _.set(obj, 'filetypes', null);
      _.set(obj, 'filelocs', null);
      _.set(obj, 'totals', null);
      _.set(obj, 'group_ids', _.join(group_ids, ','));
      _.set(obj, 'exclude_group_ids', _.join(exclude_group_ids, ','));

      console.log(obj);

      const r1 = await fastify.scheduleCampaign(obj);

      // TODO: verify the inserted row
      const resp = { statusCode: 200, message: 'Campaign Scheduled Successfully' };
      reply.code(200);

      return reply.send(resp);
    } catch (err) {
      const code = fastify.nanoid();
      req.log.error(`error ${code}  =>${err}`);
      const e = fastify.httpErrors.createError(500, 'Could not schedule the campaign. Please try again', { code });

      return e;
    }
  });
}

module.exports = campaignsGroup;
