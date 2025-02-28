const fp = require('fastify-plugin');
const _ = require('lodash');
// TODO: Change req param to respective individual params. *** for method reusability ***

const campaigndao = async (fastify, opts) => {
  fastify.decorate('isCampaignNameFound', async (req) => {
    console.log('isCampaignNameFound');
    const cliid = req.user.cli_id;
    const { cname } = req.query;

    const result1 = await fastify.mariadb.query('select count(1) as counts from cm.campaign_master where cli_id=? and lower(c_name)=?', [cliid, _.toLower(cname)]);
    const counts1 = _.get(result1[0], 'counts', 0);

    if (+counts1 === 0) {
      // check sched table
      const result2 = await fastify.mariadb.query('select count(1) as counts from cm.campaign_schedule_master where cli_id=? and lower(c_name)=?', [cliid, _.toLower(cname)]);
      const counts2 = _.get(result2[0], 'counts', 0);
      return [{ counts: counts2 }];
    }
    return [{ counts: counts1 }];
  });

  fastify.decorate('getCampaignBasePathFromConfigParams', async () => {
    const result = await fastify.mariadb.query('select * from cm.config_params where `key`=\'campaigns.file.store.path\'');
    return result;
  });

  fastify.decorate('getTodaysCampaignsStats', async (cli_id, dtfrom, dtto) => {
    const sql = 'select count(*) count, status from cm.campaign_master where cli_id=? and created_ts between ? and ? group by status';
    const params = [cli_id, dtfrom, dtto];
    console.log('getTodaysCampaignsStats() sql and params => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('getScheduledCampaignStats', async (cli_id, fromdate, todate) => {
    const sql = 'select count(*) count from cm.campaign_schedule_at t1, cm.campaign_schedule_master t2 where t1.cs_id=t2.id and t2.cli_id=? and lower(t1.status)=\'queued\' and scheduled_ts between ? and ?';
    const params = [cli_id, fromdate, todate];
    console.log('getScheduledCampaignStats() sql and params => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('getCampaigns', async (cli_id, fromdate, todate) => {
    const sql = 'select * from cm.campaign_master where cli_id=? and created_ts between ? and ? order by created_ts desc';
    const params = [cli_id, fromdate, todate];
    console.log('getCampaigns() sql and params => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('getScheduledCampaigns', async (cli_id, fromdate, todate) => {
    const sql = 'select t2.*, scheduled_ts, t1.id at_id, t1.selected_zone, t1.selected_dt from cm.campaign_schedule_at t1, cm.campaign_schedule_master t2 where t1.cs_id=t2.id and t2.cli_id=? and lower(t1.status)=\'queued\' and scheduled_ts between ? and ? order by scheduled_ts';
    const params = [cli_id, fromdate, todate];
    console.log('getScheduledCampaigns() sql and params => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('getScheduledCampaignDetailForId', async (cli_id, id, at_id) => {
    const sql = 'select t2.*, t1.id at_id, scheduled_ts, selected_zone, t1.selected_dt from cm.campaign_schedule_at t1, cm.campaign_schedule_master t2 where t1.cs_id=t2.id and t2.cli_id=? and t2.id= ? and t1.id= ?';
    const params = [cli_id, id, at_id];
    console.log('getScheduledCampaignDetailForId() sql and params => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('getCampaignDetailForId', async (cli_id, id) => {
    const sql = 'select * from cm.campaign_master where cli_id=? and id=?';
    const params = [cli_id, id];
    console.log('getCampaignDetailForId() sql and params => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('getCampaignStatForId', async (cli_id, id) => {
    const sql = 'select count(*) count,sum(total) total, sum(valid) valid, sum(invalid) invalid, sum(duplicate) duplicate, sum(excluded) excluded,sum(failed) failed, c_id from cm.campaign_files cf, cm.campaign_master cmas where cf.c_id = cmas.id and cmas.cli_id=? and cmas.id=? group by c_id';
    const params = [cli_id, id];
    console.log('getCampaignStatForId() sql and params => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('getTodaysCountForFileId', async (cli_id, file_id, countfor) => {
    console.log('getTodaysCountForFileId() fetching data from es for => ', [cli_id, file_id, countfor]);
    let terms = {};

    if (_.eq(countfor, 'mtsuccess')) terms = { sub_status: ['Success'] };
    if (_.eq(countfor, 'dnsuccess')) terms = { dn_delivery_status: ['Delivered'] };
    if (_.eq(countfor, 'mtrejected')) terms = { sub_status: ['Rejected', 'Failed', 'Expired'] };
    if (_.eq(countfor, 'dnfailed')) terms = { delivery_status: ['Failed', 'Expired'] };

    const { body } = await fastify.elastic.count({
      index: 'sub_del_t2',
      body: {
        query: {
          bool: {
            filter: [
              { term: { cli_id } },
              { exists: { field: 'sub_update_ts' } },
              { term: { file_id } },
              { terms },
            ],
          },
        },
      },
    });

    return body;
  });

  /** method used to find if the campaign has completed at platform */
  fastify.decorate('getTodaysTotalProcessedForCampaignId', async (cli_id, campaign_id) => {
    console.log('getTodaysTotalProcessedForCampaignId() fetching data from es for => ', [cli_id, campaign_id]);

    const { body } = await fastify.elastic.count({
      index: 'sub_del_t2_fmsg_info',
      body: {
        query: {
          bool: {
            filter: [
              { term: { cli_id } },
              { term: { campaign_id } },
            ],
          },
        },
      },
    });

    return body;
  });

  /** method used to find if a file of campaign has completed at platform */
  fastify.decorate('getTodaysTotalProcessedForFileId', async (cli_id, file_id) => {
    console.log('getTodaysTotalProcessedForFileId() fetching data from es for => ', [cli_id, file_id]);

    const { body } = await fastify.elastic.count({
      index: 'sub_del_t2_fmsg_info',
      body: {
        query: {
          bool: {
            filter: [
              { term: { cli_id } },
              { term: { file_id } },
            ],
          },
        },
      },
    });

    return body;
  });

  fastify.decorate('getSummaryCountForFileId', async (cli_id, file_id) => {
    const params = [cli_id, file_id];

    const sql = `select sum(tot_cnt) as totalrecieved, sum(submitted_cnt) as mtsuccess, sum(delivered_cnt) as dnsuccess, sum(dn_failed_cnt) as dnfailed,sum(rejected_cnt) as mtrejected,sum(dn_expired_cnt) as expired,sum(dn_pending_cnt) as dnpending                
                from summary.ui_camp_report where cli_id=$1 and file_id=$2`;

    console.log('getSummaryCountForFileId() sql params => ', sql, params);

    const result = await fastify.pg.summary.query(sql, params);

    return _.get(result, 'rows');
  });

  fastify.decorate('getTodaysCountForCampaignId', async (cli_id, c_id, countfor) => {
    console.log('getTodaysCountForCampaignId() fetcing data from es for => ', [cli_id, c_id, countfor]);
    let terms = {};

    if (_.eq(countfor, 'mtsuccess')) terms = { sub_status: ['Success'] };
    if (_.eq(countfor, 'dnsuccess')) terms = { dn_delivery_status: ['Delivered'] };
    if (_.eq(countfor, 'mtrejected')) terms = { sub_status: ['Rejected', 'Failed', 'Expired'] };
    if (_.eq(countfor, 'dnfailed')) terms = { dn_delivery_status: ['Failed', 'Expired'] };

    const { body } = await fastify.elastic.count({
      index: 'sub_del_t2',
      body: {
        query: {
          bool: {
            filter: [
              { term: { cli_id } },
              { exists: { field: 'sub_update_ts' } },
              { term: { campaign_id: c_id } },
              { terms },
            ],
          },
        },
      },
    });

    return body;
  });

  fastify.decorate('getSummaryCountForCampaignId', async (cli_id, c_id) => {
    const params = [cli_id, c_id];

    const sql = `select sum(tot_cnt) as totalrecieved, sum(submitted_cnt) as mtsuccess, sum(delivered_cnt) as dnsuccess, sum(dn_failed_cnt) as dnfailed,sum(rejected_cnt) as mtrejected,sum(dn_expired_cnt) as expired,sum(dn_pending_cnt) as dnpending                
                from summary.ui_camp_report where cli_id=$1 and campaign_id=$2`;

    console.log('getSummaryCountForCampaignId() sql params => ', sql, params);

    const result = await fastify.pg.summary.query(sql, params);

    return _.get(result, 'rows');
  });

  fastify.decorate('getCampaignStatsForFileUpload', async (ids) => {
    const sql = 'select count(*) count,sum(total) total, sum(valid) valid, sum(invalid) invalid, sum(duplicate) duplicate, sum(excluded) excluded,sum(failed) failed, c_id from cm.campaign_files where c_id in (?) group by c_id';
    const params = [ids];
    console.log('getCampaignStatsForFileUpload() sql and params => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('getCampaignStatsForGroupUpload', async (ids) => {
    const sql = 'select count(*) count,sum(total) total, c_id, exclude_group_ids from cm.campaign_groups where c_id in (?) group by c_id, exclude_group_ids';
    const params = [ids];
    console.log('getCampaignStatsForGroupUpload() sql and params => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('getCampaignDetailFromGroupForId', async (cli_id, id) => {
    const sql = 'select count(*) count,sum(total) total, exclude_group_ids, c_id from cm.campaign_groups cg, cm.campaign_master cmas where cg.c_id = cmas.id and cmas.cli_id=? and cmas.id=? group by exclude_group_ids, c_id';
    const params = [cli_id, id];
    console.log('getCampaignDetailFromGroupForId() sql and params => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('getCampaignDetailByFile', async (cli_id, id) => {
    const sql = 'select cf.*, (valid + invalid + duplicate + excluded + failed) processed_count, cmas.c_type, cmas.created_ts as cts, cmas.c_name from cm.campaign_files cf, cm.campaign_master cmas where cf.c_id = cmas.id and cmas.cli_id=? and cmas.id=?;';
    const params = [cli_id, id];
    console.log('getCampaignDetailByFile() sql and params => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('getCampaignDetailByGroup', async (cli_id, id) => {
    const sql = 'select cg.*, gm.g_name from cm.campaign_groups cg, cm.group_master gm where cg.group_id=gm.id and cg.c_id=?';
    const params = [id];
    console.log('getCampaignDetailByFile() sql and params => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('updateSchedCampaign', async (cli_id, c_id, at_id, istts, incomingtsFormatted, zone) => {
    const sql = 'update campaign_schedule_at set scheduled_ts=?, selected_dt=?, selected_zone=? where id=? and cs_id=?';
    const params = [istts, incomingtsFormatted, zone, at_id, c_id];

    console.log('updateSchedCampaign() sql and params => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('deleteSchedCampaign', async (cli_id, c_id, at_id) => {
    // TODO: Need to add cli_id as part of the query for deletion
    const sql = 'delete from campaign_schedule_at where  id=? and cs_id=?';
    const params = [at_id, c_id];
    console.log('deleteSchedCampaign() sql and params => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('sendCampaign', async (reqObj) => {
    const con = await fastify.mariadb.getConnection();
    const {
      cli_id,
      user,
      ip,
      sessionid,
      c_name,
      remove_dupe_yn,
      msg,
      c_type,
      c_lang_type,
      c_lang,
      dlt_entity_id,
      dlt_template_id,
      header,
      id,
      valuesArr,
      groupsValuesArr,
      template_id, template_type, template_mobile_column, shorten_url_yn, intl_header,
    } = reqObj;
    let r1 = {};
    let r2 = {};

    try {
      const sqlCM = `insert into cm.campaign_master (id, cli_id, username, c_name, msg, header, dlt_entity_id, dlt_template_id, c_type, c_lang_type, c_lang, remove_dupe_yn, ipaddr, sessionid, template_id, template_type, template_mobile_column, shorten_url, intl_header)
                  values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?,?,?)`;
      const sqlCF = `insert into cm.campaign_files (id, c_id, filename, filename_ori, filetype, fileloc, total)
                      values (?, ?, ?, ?, ?, ?, ?)`;
      const sqlCG = `insert into cm.campaign_groups (id, c_id, group_id, exclude_group_ids)
                      values (?, ?, ?, ?)`;

      let shorten_yn = shorten_url_yn;
      if (_.isNull(shorten_url_yn) || _.isUndefined(shorten_url_yn)) {
        shorten_yn = 0;
      }

      const paramsCM = [id, cli_id, user, c_name, msg, header, dlt_entity_id, dlt_template_id, c_type, c_lang_type, c_lang, remove_dupe_yn, ip, sessionid, template_id, template_type, template_mobile_column, shorten_yn, intl_header];

      con.beginTransaction();

      console.log(`sendCampaign() [${c_type} campaign_master] sql params => `, sqlCM, paramsCM);
      // insert to campaign master
      r1 = await con.query(sqlCM, paramsCM);

      if (!_.eq(c_type, 'group')) {
        console.log(`sendCampaign() [${c_type} campaign_files] sql params => `, sqlCF, valuesArr);
        // insert to campaign files
        r2 = await con.batch(sqlCF, valuesArr);
      } else {
        console.log(`sendCampaign() [${c_type} campaign_groups] sql params => `, sqlCG, groupsValuesArr);
        // insert to campaign groups
        r2 = await con.batch(sqlCG, groupsValuesArr);
      }
      con.commit();
    } catch (e) {
      con.rollback();
      console.error('con rollback', e);
      throw e;
    } finally {
      await con.release();
    }
    return { m: 'sucess' };
  });


  fastify.decorate('scheduleCampaign', async (reqObj) => {
    const con = await fastify.mariadb.getConnection();
    const {
      cli_id,
      user,
      ip,
      sessionid,
      c_name,
      remove_dupe_yn,
      msg,
      c_type,
      c_lang_type,
      c_lang,
      dlt_entity_id,
      dlt_template_id,
      header,
      id,
      scAtValuesArr,
      template_id, template_type, template_mobile_column,
      group_ids, exclude_group_ids, filenames, filenames_ori, totals, filesizes, filetypes, filelocs, shorten_url_yn, intl_header,
    } = reqObj;
    let r1 = {};
    let r2 = {};

    try {
      const sqlCM = `insert into cm.campaign_schedule_master (id, cli_id, username, c_name, msg, header, dlt_entity_id, dlt_template_id, c_type, c_lang_type, c_lang, remove_dupe_yn, ipaddr, sessionid, template_id, template_type, template_mobile_column, group_ids, filenames, filenames_ori, totals, filetypes, filelocs, exclude_group_ids, shorten_url, intl_header)
                  values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)`;

      const sqlCSA = `insert into cm.campaign_schedule_at (id, cs_id, username, scheduled_ts, selected_dt, selected_zone)
                      values (?, ?, ?, ?, ?, ?)`;

      let shorten_yn = shorten_url_yn;
      if (_.isNull(shorten_url_yn) || _.isUndefined(shorten_url_yn)) {
        shorten_yn = 0;
      }

      const paramsCM = [id, cli_id, user, c_name, msg, header, dlt_entity_id, dlt_template_id, c_type, c_lang_type, c_lang, remove_dupe_yn, ip, sessionid, template_id, template_type, template_mobile_column, group_ids, filenames, filenames_ori, totals, filetypes, filelocs, exclude_group_ids, shorten_yn, intl_header];

      con.beginTransaction();

      console.log(`sendCampaign() [${c_type} campaign_schedule_master] sql params => `, sqlCM, paramsCM);
      // insert to campaign master
      r1 = await con.query(sqlCM, paramsCM);

      console.log(`sendCampaign() [${c_type} campaign_schedule_at] sql params => `, sqlCSA, scAtValuesArr);
      // insert to campaign files
      r2 = await con.batch(sqlCSA, scAtValuesArr);

      con.commit();
      await con.release();
    } catch (e) {
      con.rollback();
      console.error('con rollback', e);
      throw e;
    } finally {
      await con.release();
    }
    return { m: 'sucess' };
  });
};

module.exports = fp(campaigndao);
