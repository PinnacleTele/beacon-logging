const _ = require('lodash');
const mtz = require('moment-timezone');
const jsunicode = require('jsunicode');

const { templatesSchema, dlttemplatesSchema, unuseddlttemplatesSchema, tnameuniqueSchema, tnewSchema, tinfoSchema, tupdateSchema, tdeleteSchema, dlttemplatesForCampaignSchema, templatesForCampaignSchema } = require('../../../schema/template-schema');

/**
 * setup some routes
 * @param {import("fastify").FastifyInstance} fastify
 * @param {*} opts
 */
async function template(fastify, opts) {
  fastify.addHook('preValidation', async (req, reply) => {
    console.log('preValidation hook called');
    return req.jwtVerify();
  });

  fastify.get('/templates', { schema: templatesSchema }, async (req, reply) => {
    try {
      const { tz, cli_id } = req.user; // Asia/Calcutta
      const entityid = _.trim(_.get(req.query, 'entity_id', null));

      const result = await fastify.getTemplates(cli_id, entityid);

      console.log('/templates total templates => ', result.length);

      _.forEach(result, (obj, i) => {
        // TODO: need to set enable/disable prop based on the compatibility of the uploaded file structure with the template list
        //  set enable to true only if the template matches with the uploaded file content
        _.set(obj, 'enabled', true);
        _.set(obj, 'info', ''); // will be set only if enabled is false. e.g. cannot be used

        // TODO: repetitive code. need to be centralized. same logic is used elsewhere
        const is_unicode = _.get(obj, 'is_unicode', null);
        const t_content = _.get(obj, 't_content', '');
        const cts = _.get(obj, 'created_ts', null);
        const mts = _.get(obj, 'modified_ts', null);
        const ctsMoment = mtz.tz(cts, tz);
        const mtsMoment = mtz.tz(mts, tz);

        // convert hex to unicode string
        // if (is_unicode === 1) {
        //   try {
        //     t_content = jsunicode.decode(t_content, { encoding: jsunicode.constants.encoding.utf16 });
        //     _.set(obj, 't_content', t_content);
        //   } catch (e) {
        //     // ignore the error. continue with original message
        //   }
        // }

        if (!_.isNull(cts)) {
          // convert to acc tz
          const formattedDt = ctsMoment.format('DD-MM-YYYY HH:mm:ss');
          _.set(obj, 'created_ts', formattedDt);
          _.set(obj, 'created_ts_unix', ctsMoment.unix());
        }
        if (!_.isNull(mts)) {
          console.log('inside');
          // convert to acc tz
          const formattedDt = mtsMoment.format('DD-MM-YYYY HH:mm:ss');
          _.set(obj, 'modified_ts', formattedDt);
          _.set(obj, 'modified_ts_unix', mtsMoment.unix());
        }
      });

      return result;
    } catch (err) {
      const code = fastify.nanoid();
      req.log.error(`error ${code}  =>${err}`);

      return fastify.httpErrors.createError(500, 'Could not get templates. Please try again', { code });
    }
  });

  fastify.get('/tinfo', { preValidation: [], schema: tinfoSchema }, async (req, reply) => {
    try {
      const { id } = req.query;
      const { tz } = req.user; // Asia/Calcutta

      const result = await fastify.getTemplateInfo({ id });

      console.log(`/tinfo resp from db for if ${id} is => ${JSON.stringify(result)}`);
      const resp = _.get(result, '0', {});

      // TODO: repetitive code. need to be centralized. same logic is used elsewhere
      if (!_.isEmpty(resp)) {
        const is_unicode = _.get(resp, 'is_unicode', null);
        const t_content = _.get(resp, 't_content', '');
        const cts = _.get(resp, 'created_ts', null);
        const mts = _.get(resp, 'modified_ts', null);
        const ctsMoment = mtz.tz(cts, tz);
        const mtsMoment = mtz.tz(mts, tz);

        // convert hex to unicode string
        // if (is_unicode === 1) {
        //   t_content = jsunicode.decode(t_content, { encoding: jsunicode.constants.encoding.utf16 });
        // }
        _.set(resp, 't_content', t_content);

        if (!_.isNull(cts)) {
          // convert to acc tz
          const formattedDt = ctsMoment.format('DD-MM-YYYY HH:mm:ss z');
          _.set(resp, 'created_ts', formattedDt);
          _.set(resp, 'created_ts_unix', ctsMoment.unix());
        }
        if (!_.isNull(mts)) {
          console.log('inside');
          // convert to acc tz
          const formattedDt = mtsMoment.format('DD-MM-YYYY HH:mm:ss z');
          _.set(resp, 'modified_ts', formattedDt);
          _.set(resp, 'modified_ts_unix', mtsMoment.unix());
        }
      }

      return resp;
    } catch (err) {
      const code = fastify.nanoid();
      req.log.error(`error ${code}  =>${err}`);
      const e = fastify.httpErrors.createError(500, 'Could not get template info. Please try again', { code });

      return e;
    }
  });

  fastify.get('/tnameunique', { preValidation: [], schema: tnameuniqueSchema }, async (req, reply) => {
    try {
      const { cli_id } = req.user;
      const { t_name } = req.query;

      const result = await fastify.isTemplateNameFound(cli_id, t_name);
      const { counts } = result[0];

      console.log('matching template name count => ', counts);
      const payload = { isUnique: (counts === 0) };
      return payload;
    } catch (err) {
      const code = fastify.nanoid();
      req.log.error(`error ${code}  =>${err}`);
      const e = fastify.httpErrors.createError(500, 'Could not check for uniqueness. Please try again', { code });
      // const e = fastify.httpErrors.createError(500, err);

      return e;
    }
  });

  fastify.post('/tnew', { schema: tnewSchema }, async (req, reply) => {
    try {
      const { cli_id } = req.user;
      let resp = '';
      const { t_name, t_type, t_mobile_column, dlt_entity_id, dlt_template_id, pattern_type, t_content, is_unicode, is_static } = req.body;

      const content = t_content;
      console.log('/tnew incoming params => ', [t_name, t_type, t_mobile_column, dlt_entity_id, dlt_template_id, pattern_type, t_content, is_unicode]);

      /* check if template name is unique */
      req.query.t_name = t_name;
      const result = await fastify.isTemplateNameFound(cli_id, t_name);
      const { counts } = result[0];

      console.log('matching template name count => ', counts);
      const isUnique = (counts === 0);

      if (!isUnique) {
        resp = { statusCode: fastify.CONSTANTS.TEMPLATE_NAME_NOT_UNIQUE_CODE, message: 'Template name is not unique' };
        reply.code(200);
      } else {
        /*  NOTE: **** t_content is not checked against the original dlt template content. any change in the template content structure
            will result in failed delivery (or) invalid content during table insert. e.g. if the t_content has any unicode char
            which was not present in the original dlt content will result in wrong message insert in the template master table. the entire
            message will be converted to HEX before insert if there is a presence of unicode char
         */

        // check if the message is unicode, if so convert it to hex
        // if (_.eq(is_unicode, 1)) {
        //   content = jsunicode.encode(t_content, { encoding: jsunicode.constants.encoding.utf16 }); // utf16
        // }
        const result1 = await fastify.createTemplate({
          cli_id,
          t_name,
          t_type,
          t_mobile_column,
          dlt_entity_id,
          dlt_template_id,
          pattern_type,
          t_content: content,
          is_unicode,
          is_static,
        });

        resp = { statusCode: 201, message: 'Template has been created successfully' };
        reply.code(201);
      }

      return reply.send(resp);
    } catch (err) {
      const code = fastify.nanoid();
      req.log.error(`error ${code}  =>${err}`);

      return fastify.httpErrors.createError(500, 'Could not create template. Please try again', { code });
    }
  });

  fastify.post('/tupdate', { preValidation: [], schema: tupdateSchema }, async (req, reply) => {
    try {
      const { cli_id } = req.user;
      const { t_name, id } = req.body;

      const result = await fastify.updateTemplate({ t_name, id });
      // TODO: need to check the updated status and reply accordingly { affectedRows: 1, insertId: 0, warningStatus: 0 }

      console.log('/tupdate resp from db => ', result);
      const resp = { statusCode: 200, message: 'Template has been modified successfully' };

      return resp;
    } catch (err) {
      const code = fastify.nanoid();
      req.log.error(`error ${code}  =>${err}`);
      const e = fastify.httpErrors.createError(500, 'Could not update template details. Please try again', { code });

      return e;
    }
  });

  fastify.post('/tdelete', { preValidation: [], schema: tdeleteSchema }, async (req, reply) => {
    try {
      const { cli_id } = req.user;
      const { ids } = req.body;

      const result = await fastify.deleteTemplate({ ids });
      // TODO: need to check the deleted status and reply accordingly { affectedRows: 1, insertId: 0, warningStatus: 0 }

      console.log('/tdelete resp from db => ', result);
      const resp = { statusCode: 200, message: 'Template has been deleted successfully' };

      return resp;
    } catch (err) {
      const e = fastify.httpErrors.createError(500, 'Could not delete the template. Please try again', { err });
      return e;
    }
  });

  fastify.get('/dlttemplates', { schema: dlttemplatesSchema }, async (req, reply) => {
    try {
      const { entity_id } = req.query;
      const { cli_id, msg_type } = req.user;

      const result = await fastify.getDLTTemplates(cli_id, msg_type, entity_id);
      const respArr = [];

      console.log('/dlttemplates total templates => ', result.length);

      _.forEach(result, (obj, i) => {
        let tname = _.get(obj, 'template_name', null);
        const tid = _.get(obj, 'template_id', null);
        const tmsg = _.get(obj, 'template_content', null);
        const entityid = _.get(obj, 'entity_id', null);
        const patternType = _.get(obj, 'pattern_type', 0);
        const is_static = _.get(obj, 'is_static', 0);

        // if (patternType === 1) {
        //   try {
        //     // replace all the occurrence of {#var#} with the equivalent hex value
        //     console.log(tmsg);
        //     const replacedMsgInHex = _.replace(tmsg, new RegExp('{#var#}', 'g'), process.env.DLT_TEMPLATE_VAR_HEX);
        //     console.log(replacedMsgInHex);
        //
        //     // convert the hex to unicode
        //     tmsg = jsunicode.decode(replacedMsgInHex, { encoding: jsunicode.constants.encoding.utf16 });
        //   } catch (e) {
        //     console.error(e);
        //     // ignore
        //   }
        // }

        if (_.isEmpty(tname)) {
          tname = tid;
        }
        const rowObj = {
          dlt_template_id: tid,
          dlt_template_name: tname,
          dlt_template_content: tmsg,
          dlt_entity_id: entityid,
          pattern_type: patternType,
          is_static,
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

  fastify.get('/dlttemplatesForCampaign', { schema: dlttemplatesForCampaignSchema }, async (req, reply) => {
    try {
      const { header, c_type } = req.query;
      const { cli_id, msg_type } = req.user;

      console.log('/dlttemplatesForCampaign incoming params => ', [header, c_type, cli_id]);

      const result = await fastify.getDLTTemplatesForCampaign(cli_id, msg_type, header, c_type);
      const respArr = [];

      console.log('/dlttemplatesForCampaign total templates => ', result.length);

      _.forEach(result, (obj, i) => {
        let tname = _.get(obj, 'template_name', null);
        const tid = _.get(obj, 'template_id', null);
        const tmsg = _.get(obj, 'template_content', null);
        const entityid = _.get(obj, 'entity_id', null);
        const patternType = _.get(obj, 'pattern_type', 0);

        if (_.isEmpty(tname)) {
          tname = tid;
        }
        const rowObj = {
          dlt_template_id: tid,
          dlt_template_name: tname,
          dlt_template_content: tmsg,
          dlt_entity_id: entityid,
          pattern_type: patternType,
        };
        respArr.push(rowObj);
      });

      return respArr;
    } catch (err) {
      const code = fastify.nanoid();
      req.log.error(`error ${code}  =>${err}`);

      return fastify.httpErrors.createError(500, 'Could not get dlt templates for campaign. Please try again', { code });
    }
  });

  fastify.get('/templatesForCampaign', { schema: templatesForCampaignSchema }, async (req, reply) => {
    try {
      const { tz, cli_id } = req.user; // Asia/Calcutta
      const entityid = _.trim(_.get(req.query, 'entity_id', null));
      const { c_type, header } = req.query;
      const payload = [];

      const result = await fastify.getTemplatesForCampaign(cli_id, entityid, c_type);

      console.log('/templatesForCampaign total templates => ', result.length);

      // get the dlt template ids to get the matching senderids
      const dlttids = _.map(result, 'dlt_template_id');

      console.log(dlttids);

      if (dlttids.length === 0) return payload;

      const result1 = await fastify.getSenderidsForDLTTemplateIds(dlttids);
      const grpby = _.groupBy(result1, 'header');

      // get the data for the user selected senderid
      const ids = _.map(_.get(grpby, header, []), 'template_id');
      console.log(ids);

      _.forEach(result, (obj, i) => {
        // TODO: need to set enable/disable prop based on the compatibility of the uploaded file structure with the template list
        // TODO:  set enable to true only if the template matches with the uploaded file content
        // process only if the dlt templateid is matching
        const tid = _.get(obj, 'dlt_template_id');
        if (ids.includes(tid)) {
          _.set(obj, 'enabled', true);
          _.set(obj, 'info', ''); // will be set only if enabled is false. e.g. cannot be used

          // TODO: repetitive code. need to be centralized. same logic is used elsewhere
          const cts = _.get(obj, 'created_ts', null);
          const mts = _.get(obj, 'modified_ts', null);
          const ctsMoment = mtz.tz(cts, tz);
          const mtsMoment = mtz.tz(mts, tz);

          if (!_.isNull(cts)) {
            // convert to acc tz
            const formattedDt = ctsMoment.format('DD-MM-YYYY HH:mm:ss');
            _.set(obj, 'created_ts', formattedDt);
            _.set(obj, 'created_ts_unix', ctsMoment.unix());
          }
          if (!_.isNull(mts)) {
            console.log('inside');
            // convert to acc tz
            const formattedDt = mtsMoment.format('DD-MM-YYYY HH:mm:ss');
            _.set(obj, 'modified_ts', formattedDt);
            _.set(obj, 'modified_ts_unix', mtsMoment.unix());
          }

          payload.push(obj);
        }
      });

      return payload;
    } catch (err) {
      return fastify.httpErrors.createError(500, 'Could not get templates for campaign. Please try again', { err });
    }
  });

  //   fastify.get('/unuseddlttemplates', { schema: unuseddlttemplatesSchema }, async (req, reply) => {
  //     try {
  //       const result = await fastify.getUnusedDLTTemplates(req);
  //       const respArr = [];

  //       console.log('/unuseddlttemplates total templates => ', result.length);

  //       _.forEach(result, (obj, i) => {
  //         let tname = _.get(obj, 'template_name', null);
  //         const tid = _.get(obj, 'template_id', null);
  //         const tmsg = _.get(obj, 'template_content', null);
  //         const entityid = _.get(obj, 'entity_id', null);
  //         const patternType = _.get(obj, 'pattern_type', 0);

  //         if (_.isEmpty(tname)) {
  //           tname = tid;
  //         }
  //         const rowObj = {
  //           dlt_template_id: tid,
  //           dlt_template_name: tname,
  //           dlt_template_content: tmsg,
  //           dlt_entity_id: entityid,
  //           pattern_type: patternType,
  //         };
  //         respArr.push(rowObj);
  //       });

  //       return respArr;
  //     } catch (err) {
  //       const code = fastify.nanoid();
  //       req.log.error(`error ${code}  =>${err}`);

  //       return fastify.httpErrors.createError(500, 'Could not get templates. Please try again', { code });
  //     }
  //   });
}

module.exports = template;
