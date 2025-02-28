const fs = require('fs');
const os = require('os');
const path = require('path');
const moment = require('moment');
const momenttz = require('moment-timezone');
const _ = require('lodash');
const { ctSendSchema, ctScheduleSchema } = require('../../../../schema/campaigns-schema');

async function campaignsTemplate(fastify, opts) {
  fastify.addHook('preValidation', async (req, reply) => {
    console.log('preValidation hook called');
    return req.jwtVerify();
  });

  async function processCampaign(req) {
    const { c_name, remove_dupe_yn, c_type, c_lang_type, c_lang, dlt_entity_id, header, files, shorten_url_yn, intl_header, traffic_to, is_static, save_template_yn, t_name, is_unicode } = req.body;
    let { template_id, dlt_template_id, t_lang_type, t_type, t_mobile_column, msg } = req.body;

    const { cli_id, user, sessionid } = req.user;
    const id = fastify.nanoid();
    const valuesArr = [];
    const groupsValuesArr = [];
    const resp = '';

    // get template_type template_mobile_column dlt_template_id for this template_id
    if (_.isEmpty(template_id)) template_id = null;

    if (!_.isEmpty(template_id)) {
      let r = await fastify.getTemplateInfo({ id: template_id });
      r = _.get(r, '0', {});

      if (_.isEmpty(r)) {
        throw new Error(`could not find the template entry in template_master for id ${template_id}`);
      }

      dlt_template_id = _.get(r, 'dlt_template_id');
      t_type = _.get(r, 't_type');
      t_mobile_column = _.get(r, 't_mobile_column');
      t_lang_type = _.get(r, 't_lang_type');
      msg = _.get(r, 't_content');
    } else {
      t_lang_type = (is_unicode === 1) ? 'unicode' : 'english';
    }

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
      remove_dupe_yn,
      msg,
      c_type,
      c_lang_type: t_lang_type,
      c_lang,
      dlt_entity_id,
      dlt_template_id,
      header,
      id,
      valuesArr,
      groupsValuesArr,
      template_id,
      template_type: t_type,
      template_mobile_column: t_mobile_column,
      shorten_url_yn,
      intl_header,
    };

    // create a template if save template is opted by user
    if (save_template_yn === 1) {
      const result1 = await fastify.createTemplate({
        cli_id,
        t_name,
        t_type,
        t_mobile_column,
        dlt_entity_id,
        dlt_template_id,
        pattern_type: '',
        t_content: msg,
        is_unicode,
        is_static,
      });
    }
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

  fastify.post('/send', { schema: ctSendSchema }, async (req, reply) => {
    try {
      const { cli_id, bill_type } = req.user;

      // check if the user has wallet balance
      if (+bill_type === 1) {
        const amount = await fastify.walletBalance(cli_id);
        if (+amount <= 0) {
          const resp = {
            statusCode: fastify.CONSTANTS.NOT_ENOUGH_WALLET_BAL,
            message: 'You do not have wallet balance to send any campaign. Please add amount',
          };
          reply.code(200);
          return reply.send(resp);
        }
      }

      const obj = await processCampaign(req);
      console.log(obj);
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

  fastify.post('/schedule', { schema: ctScheduleSchema }, async (req, reply) => {
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

module.exports = campaignsTemplate;
