/* eslint-disable camelcase */
const fp = require('fastify-plugin');
const _ = require('lodash');

const reportdb = async (fastify, opts) => {
    fastify.decorate('getRSources', async (cli_idIn, fdate, tdate) => {
        
        let cli_id = '';
        if(cli_idIn.length > 0){
            cli_id = _.join(_.map(cli_idIn, (v) => `'${v}'`));
        }
      
        //const sql = 'select distinct t2.display_name, lower(display_name) intf_grp_type from summary.ui_traffic_mix_report t1, summary.interface_type_mapping t2 where t1.intf_type=t2.intf_type and recv_date between $1 and $2 and t1.cli_id in ('+cli_id+') order by t2.display_name asc';
        const sql = `select distinct t2.display_name, lower(display_name) intf_grp_type from summary.ui_traffic_mix_report t1, summary.interface_type_mapping t2 where t1.intf_type=t2.intf_type and recv_date between $1 and $2 and t1.cli_id in (${cli_id}) order by t2.display_name asc`;
        const params = [fdate, tdate];
        console.log('getRSources() sql params => ', sql, params);

        const result = await fastify.pg.summary.query(sql, params);
        return _.get(result, 'rows');
    });

    fastify.decorate('getRSourcesForToday', async (cli_id, fdatetime, tdatetime) => {
        const { body } = await fastify.elastic.search({
            index: 'sub_del_t2',
            body: {
                query: {
                    bool: {
                        filter: [
                            { terms: { "cli_id":cli_id } },
                            { exists: { field: 'sub_update_ts' } },
                            {
                                range: {
                                    recv_time: {
                                        gte: fdatetime,
                                        lte: tdatetime,
                                    },
                                },
                            },
                        ],
                    },
                },
                aggs: { sources: { terms: { field: 'intf_grp_type' } } },
                size: 0,
            },
        });
        return body;
    });

    fastify.decorate('getInterfaceTypes', async (source) => {
        let sql = '';
        let result = '';
        if (_.eq(_.toLower(source), 'all')) {
            sql = 'select distinct intf_type from  summary.interface_type_mapping';
            console.log('getInterfaceTypes() sql params => ', sql);
            result = await fastify.pg.summary.query(sql);
        } else {
            sql = 'select distinct intf_type from  summary.interface_type_mapping where lower(display_name)=lower($1)';
            const params = [source];
            console.log('getInterfaceTypes() sql params => ', sql, params);
            result = await fastify.pg.summary.query(sql, params);
        }
        return _.get(result, 'rows');
    });

    fastify.decorate('getAllInterfaceTypesForGroup', async (group_type) => {
        const sql = 'select distinct intf_type from  summary.interface_type_mapping where lower(display_name)=$1';
        console.log('getAllInterfaceTypesForGroup() sql params => ', sql, [group_type]);
        const result = await fastify.pg.summary.query(sql, [_.toLower(group_type)]);

        return _.get(result, 'rows');
    });

    fastify.decorate('getRCampaigns', async (cli_ids, fdatetime, tdatetime) => {
        const sql = 'select distinct c_name as campaign_name, id as campaign_id from cm.campaign_master where cli_id in (?) and created_ts between ? and ? order by created_ts desc';
        const params = [cli_ids, fdatetime, tdatetime];
        console.log('getRCampaigns() sql and params => ', sql, params);
        const result = await fastify.mariadb.query(sql, params);
        return result;
    });

    fastify.decorate('getRSenderIds', async (cli_id, fdate, tdate, sources, campaign_id) => {
        let sql = '';
        let params = [];
        let sourcesStr = '';
        if (sources.length > 0) {
            sourcesStr = _.join(_.map(sources, (v) => `'${v}'`));
        }
        let cliIdArrSplit = '';
        if(cli_id.length > 0){
            cliIdArrSplit = _.join(_.map(cli_id, (v) => `'${v}'`));
        }
        
        if (sources.length === 0 && _.eq(_.toLower(campaign_id), 'all')) {
            sql = 'select distinct cli_hdr from summary.ui_traffic_mix_report where recv_date between $1 and $2 and cli_id in ('+cliIdArrSplit+') order by cli_hdr asc';
            params = [fdate, tdate];
        } else if (sources.length > 0 && _.eq(_.toLower(campaign_id), 'all')) {
            sql = `select distinct cli_hdr from summary.ui_traffic_mix_report where intf_type in (${sourcesStr}) and recv_date between $1 and $2 and cli_id in  (${cliIdArrSplit}) order by cli_hdr asc`;
            params = [fdate, tdate];
        } else if (sources.length === 0 && !_.eq(_.toLower(campaign_id), 'all')) {
            sql = `select distinct cli_hdr from summary.ui_traffic_mix_report where campaign_id=$1 and recv_date between $2 and $3 and cli_id in (${cliIdArrSplit}) order by cli_hdr asc`;
            params = [campaign_id, fdate, tdate];
        } else {
            sql = `select distinct cli_hdr from summary.ui_traffic_mix_report where intf_type in (${sourcesStr}) and recv_date between $1 and $2 and campaign_id=$3 and cli_id in (${cliIdArrSplit}) order by cli_hdr asc`;
            params = [fdate, tdate, campaign_id];
        }
        console.log('getRSenderIds() sql params => ', sql, params);

        const result = await fastify.pg.summary.query(sql, params);

        return _.get(result, 'rows');
    });

    fastify.decorate('getRSenderIdsForToday', async (cli_id, fdatetime, tdatetime, source, campaign_id) => {
        const filters = [];
        filters.push({ terms: { "cli_id" : cli_id } });
        filters.push({ exists: { field: 'sub_update_ts' } });
        filters.push({
            range: {
                recv_time: {
                    gte: fdatetime,
                    lte: tdatetime,
                },
            },
        });
        if (!_.eq('all', _.toLower(source))) {
            filters.push({ term: { intf_grp_type: _.toLower(source) } });
        }
        if (!_.eq('all', _.toLower(campaign_id))) {
            filters.push({ term: { campaign_id } });
        }

        const payload = {
            index: 'sub_del_t2',
            body: {
                query: { bool: { filter: filters } },
                aggs: { senderid: { terms: { field: 'cli_hdr' } } },
                size: 0,
            },
        };
        console.log('getRSenderIdsForToday() es payload =>', JSON.stringify(payload));
        const { body } = await fastify.elastic.search(payload);
        return body;
    });

    fastify.decorate('getRSDatewise', async (fdate, tdate, sources, campaign_id, senderid, cli_idIn) => {
        let sql = '';
        let sourceSnip = '';
        let campaignSnip = '';
        let senderSnip = '';
        let sourcesStr = '';
        let cli_id= "";

        if(cli_idIn.length >1){
            cli_id = cli_idIn.join(',');
        }else{
            cli_id = cli_idIn[0]
        }
        console.log("getrsdatewise",cli_id)
        if (sources.length > 0) {
            sourcesStr = _.join(_.map(sources, (v) => `'${v}'`));
        }
        const params = [ fdate, tdate];

        if (sources.length > 0) {
            sourceSnip = `lower(intf_type) in (${sourcesStr}) and `;
        }
        if (!_.eq(campaign_id, 'all')) {
            campaignSnip = `campaign_id='${campaign_id}' and `;
        }
        if (!_.eq(senderid, 'all')) {
            senderSnip = `cli_hdr='${senderid}' and `;
        }

        sql = `select recv_date, cli_id, sum(tot_cnt) as totalrecieved, sum(submitted_cnt) as mtsuccess, sum(delivered_cnt) as dnsuccess, sum(dn_failed_cnt) as dnfailed,sum(rejected_cnt) as mtrejected,sum(dn_expired_cnt) as expired,sum(dn_pending_cnt) as dnpending         
                from summary.ui_traffic_mix_report where ${sourceSnip} ${campaignSnip} ${senderSnip}  cli_id in (${cli_id}) and recv_date between $1 and $2  group by recv_date,cli_id order by recv_date desc`;

        console.log('getRSDatewise() sql params => ', sql, params);

        const result = await fastify.pg.summary.query(sql, params);

        return _.get(result, 'rows');
    });

    fastify.decorate('getRSMonthwise', async (fdate, tdate, sources, campaign_id, senderid, cli_id) => {
        let sql = '';
        let sourceSnip = '';
        let campaignSnip = '';
        let senderSnip = '';
        let sourcesStr = '';

        const params = [cli_id, fdate, tdate];

        if (sources.length > 0) {
            sourcesStr = _.join(_.map(sources, (v) => `'${v}'`));
        }
        if (sources.length > 0) {
            sourceSnip = `lower(intf_type) in (${sourcesStr}) and `;
        }
        if (!_.eq(campaign_id, 'all')) {
            campaignSnip = `campaign_id='${campaign_id}' and `;
        }
        if (!_.eq(senderid, 'all')) {
            senderSnip = `cli_hdr='${senderid}' and `;
        }

        sql = `select date_trunc('month',recv_date) recv_date_month, sum(tot_cnt) as totalrecieved, sum(submitted_cnt) as mtsuccess, sum(delivered_cnt) as dnsuccess, sum(dn_failed_cnt) as dnfailed,sum(rejected_cnt) as mtrejected,sum(dn_expired_cnt) as expired,sum(dn_pending_cnt) as dnpending 
                from summary.ui_traffic_mix_report where ${sourceSnip} ${campaignSnip} ${senderSnip}  cli_id=$1 and recv_date between $2 and $3  group by date_trunc('month',recv_date) order by date_trunc('month',recv_date) desc`;

        console.log('getRSMonthwise() sql params => ', sql, params);

        const result = await fastify.pg.summary.query(sql, params);

        return _.get(result, 'rows');
    });

    fastify.decorate('getRSCampaignwise', async (fdate, tdate, sources, campaign_id, senderid, cli_idIn) => {
        //console.log('getRSCampaignwise() incoming params => ', [fdate, tdate, sources, campaign_id, senderid, cli_idIn]);

        let sql = '';
        let sourceSnip = '';
        let campaignSnip = '';
        let senderSnip = '';
        let sourcesStr = '';
        let cli_id = cli_idIn.join(',');
        if (sources.length > 0) {
            sourcesStr = _.join(_.map(sources, (v) => `'${v}'`));
        }

        const params = [ fdate, tdate];

        if (sources.length > 0) {
            sourceSnip = `lower(intf_type) in (${sourcesStr}) and `;
        }
        if (!_.eq(campaign_id, 'all')) {
            campaignSnip = `campaign_id='${campaign_id}' and `;
        }
        if (!_.eq(senderid, 'all')) {
            senderSnip = `cli_hdr='${senderid}' and `;
        }

        sql = `select recv_date,campaign_name as campaign,campaign_id, cli_id, sum(tot_cnt) as totalrecieved, sum(submitted_cnt) as mtsuccess, sum(delivered_cnt) as dnsuccess, sum(dn_failed_cnt) as dnfailed,sum(rejected_cnt) as mtrejected,sum(dn_expired_cnt) as expired,sum(dn_pending_cnt) as dnpending 
                from summary.ui_traffic_mix_report 
                where ${sourceSnip} ${campaignSnip} ${senderSnip}  cli_id in (${cli_id}) and recv_date between $1 and $2  group by recv_date, campaign_name,campaign_id,cli_id order by recv_date desc,campaign_name`;

        //console.log('getRSCampaignwise() sql params => ', sql, params);

        const result = await fastify.pg.summary.query(sql, params);

        return _.get(result, 'rows');
    });
    fastify.decorate('getRSSourcewise', async (fdate, tdate, sources, campaign_id, senderid, cli_idIn) => {
        let sql = '';
        let sourceSnip = '';
        let campaignSnip = '';
        let senderSnip = '';
        let sourcesStr = '';
        
        if (sources.length > 0) {
            sourcesStr = _.join(_.map(sources, (v) => `'${v}'`));
        }
        let cli_id = "";
        if(cli_idIn.length > 0){
           cli_id = _.join(_.map(cli_idIn , (v) => `'${v}'`))
        }

        const params = [fdate, tdate];

        if (sources.length > 0) {
            sourceSnip = `lower(t1.intf_type) in (${sourcesStr}) and `;
        }
        if (!_.eq(campaign_id, 'all')) {
            campaignSnip = `campaign_id='${campaign_id}' and `;
        }
        if (!_.eq(senderid, 'all')) {
            senderSnip = `cli_hdr='${senderid}' and `;
        }

        sql = `select recv_date,cli_id,t2.display_name as source, sum(tot_cnt) as totalrecieved, sum(submitted_cnt) as mtsuccess, sum(delivered_cnt) as dnsuccess, sum(dn_failed_cnt) as dnfailed,sum(rejected_cnt) as mtrejected,sum(dn_expired_cnt) as expired,sum(dn_pending_cnt) as dnpending
                from summary.ui_traffic_mix_report t1, summary.interface_type_mapping t2 where ${sourceSnip} ${campaignSnip} ${senderSnip}  cli_id in (${cli_id}) and recv_date between $1 and $2 and t1.intf_type=t2.intf_type  group by recv_date, display_name,cli_id order by recv_date desc, display_name`;

        console.log('getRSSourcewise() sql params => ', sql, params);

        const result = await fastify.pg.summary.query(sql, params);

        return _.get(result, 'rows');
    });
    fastify.decorate('getRSSenderidwise', async (fdate, tdate, sources, campaign_id, senderid, cli_idIn) => {
        let sql = '';
        let sourceSnip = '';
        let campaignSnip = '';
        let senderSnip = '';
        let sourcesStr = '';

        let cli_id = "";
        if(cli_idIn.length > 0){
            cli_id = _.join(_.map(cli_idIn,(v) => `'${v}'`));
        }

        if (sources.length > 0) {
            sourcesStr = _.join(_.map(sources, (v) => `'${v}'`));
        }

        const params = [ fdate, tdate];

        if (sources.length > 0) {
            sourceSnip = `lower(intf_type) in (${sourcesStr}) and `;
        }
        if (!_.eq(campaign_id, 'all')) {
            campaignSnip = `campaign_id='${campaign_id}' and `;
        }
        if (!_.eq(senderid, 'all')) {
            senderSnip = `cli_hdr='${senderid}' and `;
        }

        sql = `select cli_id, recv_date, cli_hdr as senderid, sum(tot_cnt) as totalrecieved, sum(submitted_cnt) as mtsuccess, sum(delivered_cnt) as dnsuccess, sum(dn_failed_cnt) as dnfailed,sum(rejected_cnt) as mtrejected,sum(dn_expired_cnt) as expired,sum(dn_pending_cnt) as dnpending               
                from summary.ui_traffic_mix_report where ${sourceSnip} ${campaignSnip} ${senderSnip}  cli_id in (${cli_id}) and recv_date between $1 and $2  group by recv_date, cli_hdr, cli_id order by recv_date desc, cli_hdr`;

        console.log('getRSSenderidwise() sql params => ', sql, params);

        const result = await fastify.pg.summary.query(sql, params);

        return _.get(result, 'rows');
    });
    fastify.decorate('getRSOverall', async (fdate, tdate, sources, campaign_id, senderid, cli_idIn) => {
        let sql = '';
        let sourceSnip = '';
        let campaignSnip = '';
        let senderSnip = '';
        let sourcesStr = '';
        let cli_id = "";
        if(cli_idIn.length >1){
            cli_id = _.join(_.map(cli_idIn,(v) => `'${v}'`))
        }else{
            cli_id = cli_idIn[0]
        }
        
        if (sources.length > 0) {
            sourcesStr = _.join(_.map(sources, (v) => `'${v}'`));
        }

        const params = [fdate, tdate];

        if (sources.length > 0) {
            sourceSnip = `lower(intf_type) in (${sourcesStr}) and `;
        }
        if (!_.eq(campaign_id, 'all')) {
            campaignSnip = `campaign_id='${campaign_id}' and `;
        }
        if (!_.eq(senderid, 'all')) {
            senderSnip = `cli_hdr='${senderid}' and `;
        }

        sql = `select cli_id, sum(tot_cnt) as totalrecieved, sum(submitted_cnt) as mtsuccess, sum(delivered_cnt) as dnsuccess, sum(dn_failed_cnt) as dnfailed,sum(rejected_cnt) as mtrejected,sum(dn_expired_cnt) as expired,sum(dn_pending_cnt) as dnpending       
                from summary.ui_traffic_mix_report where ${sourceSnip} ${campaignSnip} ${senderSnip}  cli_id in (${cli_id}) and recv_date between $1 and $2 group by cli_id`;

      //  console.log('getRSOverall() this one sql params => ', sql, params);

        const result = await fastify.pg.summary.query(sql, params);
        //console.log(_.get(result, 'rows'))
        

        return _.get(result, 'rows');
    });

    fastify.decorate('getTodaysCountOverall', async (cli_id, fdatetime, tdatetime, source, campaign_id, senderid, countfor) => {
        console.log('getTodaysCountOverall() fetcing data from es for => ', [ fdatetime, tdatetime, countfor]);
        let terms = {};
        const filters = [];

        if (_.eq(countfor, 'mtsuccess')) terms = { sub_status: ['Success'] };
        if (_.eq(countfor, 'dnsuccess')) terms = { dn_delivery_status: ['Delivered'] };
        if (_.eq(countfor, 'mtrejected')) terms = { sub_status: ['Rejected', 'Failed', 'Expired'] };
        if (_.eq(countfor, 'dnfailed')) terms = { dn_delivery_status: ['Failed', 'Expired'] };

        filters.push({ terms: { "cli_id" : cli_id } });
        filters.push({ exists: { field: 'sub_update_ts' } });
        filters.push({ terms });
        filters.push({
            range: {
                recv_time: {
                    gte: fdatetime,
                    lte: tdatetime,
                },
            },
        });
        filters.push({ terms });
        if (!_.eq('all', _.toLower(source))) {
            filters.push({ term: { intf_grp_type: _.toLower(source) } });
        }
        if (!_.eq('all', _.toLower(campaign_id))) {
            filters.push({ term: { campaign_id } });
        }
        if (!_.eq('all', _.toLower(senderid))) {
            filters.push({ term: { cli_hdr: senderid } });
        }

        const payLoad = {
            index: 'sub_del_t2',
            body: { 
                query: { bool: { filter: filters } } ,
                aggs: { byCli: { terms: { field: "cli_id", size: 1000 } } },
                size: 0
            }
        }
	
	fastify.log.debug(`getTodaysCountOverall() countfor => ${countfor} payLoad => ${JSON.stringify(payLoad)}`);
        
        const { body } = await fastify.elastic.search(payLoad)
       
        return body;
    });

    fastify.decorate('getTodaysCountBy', async (cli_id, fdatetime, tdatetime, source, campaign_id, senderid, countfor, groupby) => {
       // console.log('getTodaysCountBy() fetcing data from es for => ', [cli_id, fdatetime, tdatetime, source, campaign_id, senderid, countfor, groupby]);
        let terms = {};
        const filters = [];
        let groupbyField = '';

        if (_.eq(groupby, 'campaign')) groupbyField = 'campaign_name';
        if (_.eq(groupby, 'senderid')) groupbyField = 'cli_hdr';
        if (_.eq(groupby, 'source')) groupbyField = 'intf_grp_type';

        if (_.eq(countfor, 'mtsuccess')) terms = { sub_status: ['Success'] };
        if (_.eq(countfor, 'dnsuccess')) terms = { dn_delivery_status: ['Delivered'] };
        if (_.eq(countfor, 'mtrejected')) terms = { sub_status: ['Rejected', 'Failed', 'Expired'] };
        if (_.eq(countfor, 'dnfailed')) terms = { dn_delivery_status: ['Failed', 'Expired'] };

        filters.push({ terms: { "cli_id": cli_id } });
        filters.push({ exists: { field: 'sub_update_ts' } });
        if (!_.eq(countfor, 'total')) {
            filters.push({ terms });
        }
        filters.push({
            range: {
                recv_time: {
                    gte: fdatetime,
                    lte: tdatetime,
                },
            },
        });

        if (_.eq('campaign', _.toLower(groupby))) {
            filters.push({ term: { intf_grp_type: 'ui' } });
        }
        if (!_.eq('all', _.toLower(source))) {
            filters.push({ term: { intf_grp_type: _.toLower(source) } });
        }
        if (!_.eq('all', _.toLower(campaign_id))) {
            filters.push({ term: { campaign_id } });
        }
        if (!_.eq('all', _.toLower(senderid))) {
            filters.push({ term: { cli_hdr: senderid } });
        }

        const payload = {
            index: 'sub_del_t2',
            body: {
                query: { bool: { filter: filters } },
                aggs: { "genric_and_cli_id": {
                    multi_terms: {
                      terms: [
                          { field: `${groupbyField}` },
                          { field: "cli_id" }
                        ]
                    }
                  } },
                size: 0,
            },
        };
        // aggs: { groupby: { terms: { field: `${groupbyField}`, size: 1000 } } },

      /*   aggs: { "genric_and_cli_id": {
            multi_terms: {
              terms: [
                  { field: `${groupbyField}` },
                  { field: "cli_id" }
                ]
            }
          } }, */
        
         //console.log(JSON.stringify(payload));

        const { body } = await fastify.elastic.search(payload);
        //console.log("§§§§   +++++§",JSON.stringify(body));
        return body;
    });

    fastify.decorate('getLatencyForTable', async (fdate, tdate, cli_idIn, table) => {
        let cli_id= "";

        if(cli_idIn.length > 1){
            cli_id = cli_idIn.join(',');
        }else{
            cli_id = cli_idIn[0]
        }
        const params = [fdate, tdate];

        const sql = `select recv_date, cli_id, sum(tot_cnt) as tot_cnt, sum(lat_0_5_sec_cnt) as lat_0_5, sum(lat_6_10_sec_cnt) as lat_6_10,
        sum(lat_11_15_sec_cnt) as lat_11_15, sum(lat_16_30_sec_cnt) as lat_16_30, sum(lat_31_45_sec_cnt) as lat_31_45, 
        sum(lat_46_60_sec_cnt) as lat_46_60, sum(lat_61_120_sec_cnt) as lat_61_120, sum(lat_gt_120_sec_cnt) as lat_gt_120
        from ${table} where cli_id in (${cli_id}) and recv_date >= $1 and recv_date <= $2 group by recv_date, cli_id order by recv_date desc`;

        fastify.log.debug(`getLatencyForTable() sql => ${sql}  params =>  ${params}`);

        const result = await fastify.pg.summary.query(sql, params);

        return _.get(result, 'rows');
    });

    fastify.decorate('getLatencyForPieChart', async (fdate, tdate, cli_idIn, table) => {
        let cli_id= "";

        if(cli_idIn.length > 1){
            cli_id = cli_idIn.join(',');
        }else{
            cli_id = cli_idIn[0]
        }
        const params = [fdate, tdate];

        const sql = `select sum(lat_0_5_sec_cnt) as lat_0_5, sum(lat_6_10_sec_cnt) as lat_6_10, sum(lat_11_15_sec_cnt) as lat_11_15, sum(lat_16_30_sec_cnt) as lat_16_30, sum(lat_31_45_sec_cnt) as lat_31_45, sum(lat_46_60_sec_cnt) as lat_46_60, sum(lat_61_120_sec_cnt) as lat_61_120, sum(lat_gt_120_sec_cnt) as lat_gt_120, sum(tot_cnt) as tot_cnt, (case when round(sum(lat_0_5_sec_cnt)/sum(tot_cnt)*100,2) IS NOT NULL then round(sum(lat_0_5_sec_cnt)/sum(tot_cnt)*100,2) else 0 end) as lat_0_5_percentage, (case when round(sum(lat_6_10_sec_cnt)/sum(tot_cnt)*100,2) IS NOT NULL then round(sum(lat_6_10_sec_cnt)/sum(tot_cnt)*100,2) else 0 end) as lat_6_10_percentage, (case when round(sum(lat_11_15_sec_cnt)/sum(tot_cnt)*100,2) IS NOT NULL then round(sum(lat_11_15_sec_cnt)/sum(tot_cnt)*100,2) else 0 end) as lat_11_15_percentage, (case when round(sum(lat_16_30_sec_cnt)/sum(tot_cnt)*100,2) IS NOT NULL then round(sum(lat_16_30_sec_cnt)/sum(tot_cnt)*100,2) else 0 end) as lat_16_30_percentage, (case when round(sum(lat_31_45_sec_cnt)/sum(tot_cnt)*100,2) IS NOT NULL then round(sum(lat_31_45_sec_cnt)/sum(tot_cnt)*100,2) else 0 end) as lat_31_45_percentage, (case when round(sum(lat_46_60_sec_cnt)/sum(tot_cnt)*100,2) IS NOT NULL then round(sum(lat_46_60_sec_cnt)/sum(tot_cnt)*100,2) else 0 end) as lat_46_60_percentage, (case when round(sum(lat_61_120_sec_cnt)/sum(tot_cnt)*100,2) IS NOT NULL then round(sum(lat_61_120_sec_cnt)/sum(tot_cnt)*100,2) else 0 end) as lat_61_120_percentage, (case when round(sum(lat_gt_120_sec_cnt)/sum(tot_cnt)*100,2) IS NOT NULL then round(sum(lat_gt_120_sec_cnt)/sum(tot_cnt)*100,2) else 0 end) as lat_gt_120_percentage  from ${table} where cli_id in(${cli_id}) and recv_date >= $1 and recv_date <= $2`;

        fastify.log.debug(`getLatencyForPieChart() sql => ${sql}  params =>  ${params}`);

        const result = await fastify.pg.summary.query(sql, params);

        return _.get(result, 'rows');
    });

    fastify.decorate('getLatencyForStackedChart', async (fdate, tdate, cli_idIn, table) => {
        let cli_id= "";

        if(cli_idIn.length > 1){
            cli_id = cli_idIn.join(',');
        }else{
            cli_id = cli_idIn[0]
        }
        const params = [fdate, tdate];

        const sql = `select recv_date, sum(tot_cnt) as tot_cnt, sum(lat_0_5_sec_cnt) as lat_0_5, sum(lat_6_10_sec_cnt) as lat_6_10,
        sum(lat_11_15_sec_cnt) as lat_11_15, sum(lat_16_30_sec_cnt) as lat_16_30, sum(lat_31_45_sec_cnt) as lat_31_45, 
        sum(lat_46_60_sec_cnt) as lat_46_60, sum(lat_61_120_sec_cnt) as lat_61_120, sum(lat_gt_120_sec_cnt) as lat_gt_120
        from ${table} where cli_id in (${cli_id}) and recv_date >= $1 and recv_date <= $2 group by recv_date order by recv_date desc`;

        fastify.log.debug(`getLatencyForStackedChart() sql => ${sql}  params =>  ${params}`);

        const result = await fastify.pg.summary.query(sql, params);

        return _.get(result, 'rows');
    });

    fastify.decorate('getTodaysLatencyForTable', async  (cli_id, fdatetime,tdatetime, table) => {
        console.log(fdatetime)
        let reportFor = "sub_lat_sla_in_millis";
        if(table.includes("telco")){
            reportFor = "delv_lat_sla_in_millis"
        }
        const filters = [];
        filters.push({ terms: { "cli_id" : cli_id } });
        filters.push( {
            range: {
                recv_time: {
                    gte: fdatetime,
                    lte: tdatetime,
                },
            },
        });

           

        const payLoad = {
            index: 'sub_del_t2',
            body: { 
                size: 0, 
                query: { bool: { filter: filters } } ,
                  aggs: {
                    "latency": {
                        terms: { field: "cli_id" } ,
                            "aggs": {                     
                                "tel_count": {
                                    range: {
                                        field: reportFor,
                                        ranges: [
                                        { to: 5500 },
                                        { from: 5500, to: 10500 },
                                        { from: 10500, to: 15500 },
                                        { from: 15500, to: 30500 },
                                        { from: 30500, to: 45500 },
                                        { from: 45500, to: 60500 },
                                        { from: 60500, to: 120500 },
                                        { from: 120500 }
                                        ]
                                    }
                                }
                            }
            
                        }
                    }
                }
        };
        console.log('getLatencyForTodayTable() es payload =>', JSON.stringify(payLoad));
        const { body } = await fastify.elastic.search(payLoad);
               
        return body;
    });

    fastify.decorate('getTodaysLatencyOnlyDateWise', async  (cli_id, fdatetime,tdatetime, table) => {
        //fdatetime = tdatetime = "2022-04-16";
        let reportFor = "sub_lat_sla_in_millis";
        if(table.includes("telco")){
            reportFor = "delv_lat_sla_in_millis"
        }
        const filters = [];
        filters.push({ terms: { "cli_id" : cli_id } });
        filters.push( {
            range: {
                recv_time: {
                    gte: fdatetime,
                    lte: tdatetime,
                },
            },
        });
       const payLoad = {
            index: 'sub_del_t2',
            body: { 
                size: 0, 
                query: { bool: { filter: filters } } ,
                  aggs: {
                                    
                        "sec_count": {
                            range: {
                                field: reportFor,
                                ranges: [
                                { to: 5500 },
                                { from: 5500, to: 10500 },
                                { from: 10500, to: 15500 },
                                { from: 15500, to: 30500 },
                                { from: 30500, to: 45500 },
                                { from: 45500, to: 60500 },
                                { from: 60500, to: 120500 },
                                { from: 120500 }
                                ]
                            }
                        }
                         
                    }
                }
        };
    //    console.log('gettodaysLatencyonlyDatewise() es payload =>', JSON.stringify(payLoad));
        const { body } = await fastify.elastic.search(payLoad);
    
        const aggCount = _.get(body, 'aggregations.sec_count.buckets', []);
        let result = {};
        
        perCli = {
            recv_date : fdatetime ,
            lat_0_5 : _.filter(aggCount,(x) => x.key == "*-5500.0")[0].doc_count,
            lat_6_10 : _.filter(aggCount,(x) => x.key == "5500.0-10500.0")[0].doc_count,
            lat_11_15 : _.filter(aggCount,(x) => x.key == "10500.0-15500.0")[0].doc_count,
            lat_16_30: _.filter(aggCount,(x) => x.key == "15500.0-30500.0")[0].doc_count,
            lat_31_45: _.filter(aggCount,(x) => x.key == "30500.0-45500.0")[0].doc_count,
            lat_46_60 : _.filter(aggCount,(x) => x.key == "45500.0-60500.0")[0].doc_count,
            lat_61_120 : _.filter(aggCount,(x) => x.key == "60500.0-120500.0")[0].doc_count,
            lat_gt_120 : _.filter(aggCount,(x) => x.key == "120500.0-*")[0].doc_count,
        
        };
        const total = perCli.lat_0_5 + perCli.lat_6_10 +perCli.lat_11_15 +perCli.lat_16_30 +perCli.lat_31_45 +perCli.lat_46_60 +perCli.lat_61_120 + perCli.lat_gt_120
        perCli.tot_cnt =total;
        
        return perCli
        
    });

};

module.exports = fp(reportdb);
