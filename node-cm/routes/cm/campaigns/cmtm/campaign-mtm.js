const fs = require('fs');
const os = require('os');
const path = require('path');
const moment = require('moment');
const momenttz = require('moment-timezone');
const _ = require('lodash');
const { cmtmSendSchema, cmtmScheduleSchema } = require('../../../../schema/campaigns-schema');

async function campaignsMTM(fastify, opts) {
  fastify.addHook('preValidation', async (req, reply) => {
    console.log('preValidation hook called');
    return req.jwtVerify();
  });

  async function processCampaign(req) {
    const { c_name, remove_dupe_yn, c_type, c_lang_type, c_lang, dlt_entity_id, header, files, shorten_url_yn, intl_header, traffic_to } = req.body;

    const { cli_id, user, sessionid } = req.user;
    const id = fastify.nanoid();
    const valuesArr = [];
    const groupsValuesArr = [];
    const resp = '';
    const template_id = null;
    const dlt_template_id = null;
    const template_type = null;
    const template_mobile_column = null;

    // NOTE: Assumption is there is no selection on template for MTM, will let the platform to identify the dlt_template_id based on the message ***

    // get the base path from config params table
    let result = await fastify.getCampaignBasePathFromConfigParams();
    result = _.get(result, '0', {});

    if (_.isEmpty(result)) {
      throw new Error('campaigns.file.store.path config not found in config_params table');
    }

    const basePath = _.get(result, 'value');

    _.forEach(files, (obj) => {
      const rFilename = _.get(obj, 'r_filename');
      const ext = path.extname(rFilename);
      const fileloc = path.join(basePath, _.toLower(user), rFilename);
      valuesArr.push([fastify.nanoid22(), id, rFilename, _.get(obj, 'filename'), ext, fileloc, _.get(obj, 'count')]);
    });

    console.log(valuesArr);

    const obj = {
      cli_id,
      user,
      ip: req.ip,
      sessionid,
      c_name,
      msg: null,
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

  fastify.post('/send', { schema: cmtmSendSchema }, async (req, reply) => {
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
      const resp = { statusCode: 200, message: 'Campaign sent for processing' };
      reply.code(200);

      return reply.send(resp);
    } catch (err) {
      const code = fastify.nanoid();
      req.log.error(`error ${code}  =>${err}`);
      const e = fastify.httpErrors.createError(500, 'Could not send the campaign. Please try again', { code });

      return e;
    }
  });

  fastify.post('/schedule', { schema: cmtmScheduleSchema }, async (req, reply) => {
    try {
      const obj = await processCampaign(req);
      const scAtValuesArr = await processSchedule(req, obj.id);

      // get the vlauesArr which has file info
      const valuesArr = _.get(obj, 'valuesArr');
      // formatting for easier joining
      const unzippedArr = _.unzip(valuesArr);

      console.log('>>>> unzipped ', unzippedArr);

      // get the required items and join them
      // valuesArr format => [id, c_id, filename, filename_ori, filetype, fileloc, total]
      const filenames = _.join(_.get(unzippedArr, 2), ',');
      const filenames_ori = _.join(_.get(unzippedArr, 3), ',');
      const filetypes = _.join(_.get(unzippedArr, 4), ',');
      const filelocs = _.join(_.get(unzippedArr, 5), ',');
      const totals = _.join(_.get(unzippedArr, 6), ',');

      // set this in obj
      _.set(obj, 'scAtValuesArr', scAtValuesArr);
      _.set(obj, 'filenames', filenames);
      _.set(obj, 'filenames_ori', filenames_ori);
      _.set(obj, 'filetypes', filetypes);
      _.set(obj, 'filelocs', filelocs);
      _.set(obj, 'totals', totals);
      _.set(obj, 'group_ids', null);
      _.set(obj, 'exclude_group_ids', null);

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

module.exports = campaignsMTM;
