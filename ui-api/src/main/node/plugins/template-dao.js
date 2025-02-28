const fp = require('fastify-plugin');
const _ = require('lodash');
// TODO: Change req param to respective individual params. *** for method reusability ***

const templatedb = async (fastify, opts) => {
  fastify.decorate('createTemplate', async (reqObj) => {
    const { cli_id, t_name, t_type, t_mobile_column, dlt_entity_id, dlt_template_id, pattern_type, t_content, is_unicode, is_static } = reqObj;
    const langType = (is_unicode === 1) ? 'unicode' : 'english';

    const sql = `insert into cm.template_master (id, cli_id, t_name, t_type, t_mobile_column, dlt_entity_id, dlt_template_id, t_lang_type, t_lang, t_content, is_unicode, is_static)
                values (?,?,?,?,?,?,?,?,?,?,?,?)`;
    const params = [fastify.nanoid(), cli_id, t_name, t_type, t_mobile_column, dlt_entity_id, dlt_template_id, langType, '', t_content, is_unicode, is_static];

    console.log('createTemplate() sql params => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('getTemplateInfo', async (reqObj) => {
    const { id } = reqObj;

    const sql = 'select * from cm.template_master where id=?';
    const params = [id];

    console.log('getTemplateInfo() sql params => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('updateTemplate', async (reqObj) => {
    const { id, t_name } = reqObj;

    const sql = 'update cm.template_master set t_name=?, modified_ts=? where id=?';
    const params = [t_name, new Date(), id];

    console.log('updateTemplate() sql params => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('deleteTemplate', async (reqObj) => {
    const { ids } = reqObj;

    const sql = 'delete from cm.template_master where id in (?)';
    const params = [ids]; // NOTE: array (ids) gets converted to comma separated string 'id1','id2' automatically

    console.log('deleteTemplate() sql params => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('getTemplates', async (cli_id, entityid) => {
    let params = [];
    let sql = '';

    if ((entityid)) {
      // sql = 'select * from cm.template_master cmt  where cmt.dlt_entity_id=? and cmt.cli_id=? and cmt.dlt_template_id in(select dtm.template_id from accounts.accounts_view aview, accounts.dlt_template_master dtm where aview.cli_id = ? and aview.dlt_templ_grp_id=dtm.template_group_id)';
      sql = 'select * from cm.template_master cmt  where cmt.dlt_entity_id=? and cmt.cli_id=? and cmt.dlt_template_id in (select template_id from accounts.dlt_template_group_header_entity_map where template_group_id in(select dlt_templ_grp_id from accounts.accounts_view where cli_id=?)) order by created_ts desc';
      params = [entityid, cli_id, cli_id];
    } else {
      sql = 'select * from cm.template_master cmt  where cmt.cli_id=? and cmt.dlt_template_id in (select template_id from accounts.dlt_template_group_header_entity_map where template_group_id in(select dlt_templ_grp_id from accounts.accounts_view where cli_id=?)) order by created_ts desc';
      params = [cli_id, cli_id];
    }
    console.log('getTemplates() sql and params => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('getTemplatesForCampaign', async (cli_id, entityid, c_type) => {
    let params = [];
    let sql = '';
    let isStatic = 1;

    if (_.eq(c_type, 'template')) isStatic = 0;

    if ((entityid)) {
      // sql = 'select * from cm.template_master cmt  where cmt.dlt_entity_id=? and cmt.cli_id=? and cmt.dlt_template_id in(select dtm.template_id from accounts.accounts_view aview, accounts.dlt_template_master dtm where aview.cli_id = ? and aview.dlt_templ_grp_id=dtm.template_group_id)';
      sql = 'select * from cm.template_master cmt  where cmt.dlt_entity_id=? and cmt.cli_id=? and cmt.is_static=? and cmt.dlt_template_id in (select template_id from accounts.dlt_template_group_header_entity_map where template_group_id in(select dlt_templ_grp_id from accounts.accounts_view where cli_id=?)) order by created_ts desc';
      params = [entityid, cli_id, isStatic, cli_id];
    } else {
      sql = 'select * from cm.template_master cmt  where cmt.cli_id=? and cmt.is_static=? and cmt.dlt_template_id in (select template_id from accounts.dlt_template_group_header_entity_map where template_group_id in(select dlt_templ_grp_id from accounts.accounts_view where cli_id=?)) order by created_ts desc';
      params = [cli_id, isStatic, cli_id];
    }
    console.log('getTemplatesForCampaign() sql and params => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('isTemplateNameFound', async (cli_id, t_name) => {
    const sql = 'select count(1) as counts from cm.template_master where cli_id=? and lower(t_name)=?';
    console.log('isTemplateNameFound() sql => ', sql, [cli_id, _.toLower(t_name)]);

    const result = await fastify.mariadb.query(sql, [cli_id, _.toLower(t_name)]);

    return result;
  });
};

module.exports = fp(templatedb);
