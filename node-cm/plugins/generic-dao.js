/* eslint-disable camelcase */
const fp = require('fastify-plugin');
const _ = require('lodash');

const genericdb = async (fastify, opts) => {
  fastify.decorate('getSenderids', async (msg_type, dlt_templ_grp_id) => {
    // msg_type => 0-Promotional, 1-Tansactional
    let is_numeric = 0;
    if (msg_type === 0) is_numeric = 1;

    console.log('sql params => ', [dlt_templ_grp_id, msg_type]);

    const sql = 'select distinct header, entity_id from accounts.dlt_template_group_header_entity_map where is_numeric_header=? and template_group_id=?';

    const params = [is_numeric, dlt_templ_grp_id];

    console.log('getSenderids sql => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('getSenderidsForDLTTemplateIds', async (ids) => {
    console.log('sql params => ', [ids]);

    const sql = 'select distinct header, template_id from accounts.dlt_template_group_header_entity_map where template_id in (?)';
    const params = [ids];

    console.log('getSenderidsForDLTTemplateIds sql => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  // fastify.decorate('getSenderids', async (cli_id, msg_type, entity_id, dlt_template_id) => {
  //   // msg_type => 0-Promotional, 1-Tansactional
  //   let is_numeric = 0;
  //   if (msg_type === 0) is_numeric = 1;

  //   console.log('sql params => ', [cli_id, msg_type, entity_id, dlt_template_id]);

  //   let params = [];
  //   let sql = '';

  //   if (!_.isEmpty(dlt_template_id) && !_.isEmpty(entity_id)) {
  //     sql = 'select distinct header from accounts.dlt_template_group_header_entity_map where is_numeric_header=? and template_group_id in(select dlt_templ_grp_id from accounts.accounts_view where cli_id=?) and entity_id=? and template_id=? order by header';
  //     params = [is_numeric, cli_id, entity_id, dlt_template_id];
  //   } else {
  //     sql = 'select distinct header from accounts.dlt_template_group_header_entity_map where is_numeric_header=? and template_group_id in(select dlt_templ_grp_id from accounts.accounts_view where cli_id=?) and entity_id=? order by header';
  //     params = [is_numeric, cli_id, entity_id];
  //   }

  //   console.log('getSenderids sql => ', sql, params);
  //   const result = await fastify.mariadb.query(sql, params);
  //   return result;
  // });

  fastify.decorate('getIntlSenderids', async (cli_id) => {
    const params = [cli_id];
    const sql = 'select distinct header from accounts.user_headers_intl where cli_id=? order by header';

    console.log('getIntlSenderids sql => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('getEntityids', async (cli_id, msg_type) => {
    // msg_type => 0-Promotional, 1-Tansactional
    let is_numeric = 0;
    if (msg_type === 0) is_numeric = 1;

    // const sql = 'select distinct entity_id, telemarketer as telemarketer_name from accounts.dlt_template_group_header_entity_map t1, accounts.dlt_template_info t2 where t1.template_id=t2.template_id and template_group_id in(select dlt_templ_grp_id from accounts.accounts_view where cli_id=?)'
    const sql = 'select distinct entity_id from accounts.dlt_template_group_header_entity_map where is_numeric_header=? and template_group_id in(select dlt_templ_grp_id from accounts.accounts_view where cli_id=?)';
    const params = [is_numeric, cli_id];
    console.log('getEntityids sql => ', sql, params);

    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('getTimezones', async (req) => {
    const result = await fastify.mariadb.query('select * from cm.timezone');
    return result;
  });

  fastify.decorate('hasLoggedOut', async (cli_id, sessionid) => {
    console.log('hasLoggedOut() incoming params => ', [cli_id, sessionid]);

    const result = await fastify.mariadb.query('select * from cm.user_session_log where  cli_id=? and sessionid=? and action=\'logout\'', [cli_id, sessionid]);
    return result;
  });

  fastify.decorate('hasAutoGenCampName', async (cli_id) => {
    console.log('hasAutoGenCampName() incoming params => ', [cli_id]);

    let result = await fastify.mariadb.query('select * from cm.user_configs where  cli_id=? and auto_gen_cname=1', [cli_id]);
    result = _.get(result, '0', {});
    return !_.isEmpty(result);
  });

  fastify.decorate('logSession', async (id, cli_id, user, action, sessionid, uainfo, ip) => {
    const sql = 'insert into cm.user_session_log (id, cli_id, user, action, sessionid, ua_info, ip) values (?,?,?,?,?,?,?)';
    const params = [id, cli_id, user, action, sessionid, uainfo, ip];
    console.log('logSession() sql & params => ', sql, params);

    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('getConversionRate', async (base, quote, convType) => {
    let tablename = '';

    console.log('getConversionRate() incoming params => ', [base, quote, convType]);
    if (+convType === 1) tablename = 'configuration.currency_rates_monthly';
    else if (+convType === 2) tablename = 'configuration.currency_rates_daily';
    else throw new Error('Unsupported Conversion Type');

    const sql = `select rate from ${tablename} where base_currency=? and quote_currency=?`;
    const params = [base, quote];
    console.log('getConversionRate() sql & params => ', sql, params);

    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('getUserConfigs', async (cli_id) => {
    console.log('getUserConfigs() incoming params => ', [cli_id]);

    let result = await fastify.mariadb.query('select * from cm.user_configs where  cli_id=?', [cli_id]);
    result = _.get(result, '0', {});
    return result;
  });
  
  fastify.decorate('getCliIds',async(cli_id,req) =>{
    let cli_ids = [];
    if(_.isUndefined(cli_id) || _.isNull(cli_id) || _.isEmpty(_.trim(cli_id))){
      cli_ids.push(req.user.cli_id);
      
    }else if(_.isEqual(_.toLower(_.trim(cli_id)), 'all')){
        
        cli_ids.push(req.user.cli_id);
        // add subusers under login user
        const result1 = await fastify.getAllUsersForId(req.user.cli_id);
        for (const row of result1) {
            cli_ids.push(row.cli_id);
        }

    }else{
        cli_ids.push(cli_id);
    }
    return cli_ids
  })

};

module.exports = fp(genericdb);
