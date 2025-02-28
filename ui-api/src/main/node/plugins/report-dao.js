/* eslint-disable camelcase */
const fp = require('fastify-plugin');
const _ = require('lodash');

const reportdb = async (fastify, opts) => {
    fastify.decorate('getRSources', async (cli_id, fdate, tdate) => {
        const sql = 'select distinct t2.display_name, lower(display_name) intf_grp_type from summary.ui_traffic_mix_report t1, summary.interface_type_mapping t2 where t1.intf_type=t2.intf_type and recv_date between $1 and $2 and t1.cli_id=$3 order by t2.display_name asc';
        const params = [fdate, tdate, cli_id];
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
                            { term: { cli_id } },
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

    fastify.decorate('getRCampaigns', async (cli_id, fdatetime, tdatetime) => {
        const sql = 'select distinct c_name as campaign_name, id as campaign_id from cm.campaign_master where cli_id=? and created_ts between ? and ? order by created_ts desc';
        const params = [cli_id, fdatetime, tdatetime];
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

        if (sources.length === 0 && _.eq(_.toLower(campaign_id), 'all')) {
            sql = 'select distinct cli_hdr from summary.ui_traffic_mix_report where recv_date between $1 and $2 and cli_id=$3 order by cli_hdr asc';
            params = [fdate, tdate, cli_id];
        } else if (sources.length > 0 && _.eq(_.toLower(campaign_id), 'all')) {
            sql = `select distinct cli_hdr from summary.ui_traffic_mix_report where intf_type in (${sourcesStr}) and recv_date between $1 and $2 and cli_id=$3 order by cli_hdr asc`;
            params = [fdate, tdate, cli_id];
        } else if (sources.length === 0 && !_.eq(_.toLower(campaign_id), 'all')) {
            sql = 'select distinct cli_hdr from summary.ui_traffic_mix_report where campaign_id=$1 and recv_date between $2 and $3 and cli_id=$4 order by cli_hdr asc';
            params = [campaign_id, fdate, tdate, cli_id];
        } else {
            sql = `select distinct cli_hdr from summary.ui_traffic_mix_report where intf_type in (${sourcesStr}) and recv_date between $1 and $2 and campaign_id=$3 and cli_id=$4 order by cli_hdr asc`;
            params = [fdate, tdate, campaign_id, cli_id];
        }
        console.log('getRSenderIds() sql params => ', sql, params);

        const result = await fastify.pg.summary.query(sql, params);

        return _.get(result, 'rows');
    });

    fastify.decorate('getRSenderIdsForToday', async (cli_id, fdatetime, tdatetime, source, campaign_id) => {
        const filters = [];
        filters.push({ term: { cli_id } });
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

    fastify.decorate('getRSDatewise', async (fdate, tdate, sources, campaign_id, senderid, cli_id) => {
        let sql = '';
        let sourceSnip = '';
        let campaignSnip = '';
        let senderSnip = '';
        let sourcesStr = '';

        if (sources.length > 0) {
            sourcesStr = _.join(_.map(sources, (v) => `'${v}'`));
        }
        const params = [cli_id, fdate, tdate];

        if (sources.length > 0) {
            sourceSnip = `lower(intf_type) in (${sourcesStr}) and `;
        }
        if (!_.eq(campaign_id, 'all')) {
            campaignSnip = `campaign_id='${campaign_id}' and `;
        }
        if (!_.eq(senderid, 'all')) {
            senderSnip = `cli_hdr='${senderid}' and `;
        }

        sql = `select recv_date, sum(tot_cnt) as totalrecieved, sum(submitted_cnt) as mtsuccess, sum(delivered_cnt) as dnsuccess, sum(dn_failed_cnt) as dnfailed,sum(rejected_cnt) as mtrejected,sum(dn_expired_cnt) as expired,sum(dn_pending_cnt) as dnpending         
                from summary.ui_traffic_mix_report where ${sourceSnip} ${campaignSnip} ${senderSnip}  cli_id=$1 and recv_date between $2 and $3  group by recv_date order by recv_date desc`;

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

    fastify.decorate('getRSCampaignwise', async (fdate, tdate, sources, campaign_id, senderid, cli_id) => {
        console.log('getRSCampaignwise() incoming params => ', [fdate, tdate, sources, campaign_id, senderid, cli_id]);

        let sql = '';
        let sourceSnip = '';
        let campaignSnip = '';
        let senderSnip = '';
        let sourcesStr = '';

        if (sources.length > 0) {
            sourcesStr = _.join(_.map(sources, (v) => `'${v}'`));
        }

        const params = [cli_id, fdate, tdate];

        if (sources.length > 0) {
            sourceSnip = `lower(intf_type) in (${sourcesStr}) and `;
        }
        if (!_.eq(campaign_id, 'all')) {
            campaignSnip = `campaign_id='${campaign_id}' and `;
        }
        if (!_.eq(senderid, 'all')) {
            senderSnip = `cli_hdr='${senderid}' and `;
        }

        sql = `select recv_date,campaign_name as campaign,campaign_id, sum(tot_cnt) as totalrecieved, sum(submitted_cnt) as mtsuccess, sum(delivered_cnt) as dnsuccess, sum(dn_failed_cnt) as dnfailed,sum(rejected_cnt) as mtrejected,sum(dn_expired_cnt) as expired,sum(dn_pending_cnt) as dnpending 
                from summary.ui_traffic_mix_report where ${sourceSnip} ${campaignSnip} ${senderSnip}  cli_id=$1 and recv_date between $2 and $3  group by recv_date, campaign_name,campaign_id order by recv_date desc,campaign_name`;

        console.log('getRSCampaignwise() sql params => ', sql, params);

        const result = await fastify.pg.summary.query(sql, params);

        return _.get(result, 'rows');
    });
    fastify.decorate('getRSSourcewise', async (fdate, tdate, sources, campaign_id, senderid, cli_id) => {
        let sql = '';
        let sourceSnip = '';
        let campaignSnip = '';
        let senderSnip = '';
        let sourcesStr = '';

        if (sources.length > 0) {
            sourcesStr = _.join(_.map(sources, (v) => `'${v}'`));
        }

        const params = [cli_id, fdate, tdate];

        if (sources.length > 0) {
            sourceSnip = `lower(t1.intf_type) in (${sourcesStr}) and `;
        }
        if (!_.eq(campaign_id, 'all')) {
            campaignSnip = `campaign_id='${campaign_id}' and `;
        }
        if (!_.eq(senderid, 'all')) {
            senderSnip = `cli_hdr='${senderid}' and `;
        }

        sql = `select recv_date,t2.display_name as source, sum(tot_cnt) as totalrecieved, sum(submitted_cnt) as mtsuccess, sum(delivered_cnt) as dnsuccess, sum(dn_failed_cnt) as dnfailed,sum(rejected_cnt) as mtrejected,sum(dn_expired_cnt) as expired,sum(dn_pending_cnt) as dnpending
                from summary.ui_traffic_mix_report t1, summary.interface_type_mapping t2 where ${sourceSnip} ${campaignSnip} ${senderSnip}  cli_id=$1 and recv_date between $2 and $3 and t1.intf_type=t2.intf_type  group by recv_date, display_name order by recv_date desc, display_name`;

        console.log('getRSSourcewise() sql params => ', sql, params);

        const result = await fastify.pg.summary.query(sql, params);

        return _.get(result, 'rows');
    });
    fastify.decorate('getRSSenderidwise', async (fdate, tdate, sources, campaign_id, senderid, cli_id) => {
        let sql = '';
        let sourceSnip = '';
        let campaignSnip = '';
        let senderSnip = '';
        let sourcesStr = '';

        if (sources.length > 0) {
            sourcesStr = _.join(_.map(sources, (v) => `'${v}'`));
        }

        const params = [cli_id, fdate, tdate];

        if (sources.length > 0) {
            sourceSnip = `lower(intf_type) in (${sourcesStr}) and `;
        }
        if (!_.eq(campaign_id, 'all')) {
            campaignSnip = `campaign_id='${campaign_id}' and `;
        }
        if (!_.eq(senderid, 'all')) {
            senderSnip = `cli_hdr='${senderid}' and `;
        }

        sql = `select recv_date, cli_hdr as senderid, sum(tot_cnt) as totalrecieved, sum(submitted_cnt) as mtsuccess, sum(delivered_cnt) as dnsuccess, sum(dn_failed_cnt) as dnfailed,sum(rejected_cnt) as mtrejected,sum(dn_expired_cnt) as expired,sum(dn_pending_cnt) as dnpending               
                from summary.ui_traffic_mix_report where ${sourceSnip} ${campaignSnip} ${senderSnip}  cli_id=$1 and recv_date between $2 and $3  group by recv_date, cli_hdr order by recv_date desc, cli_hdr`;

        console.log('getRSSenderidwise() sql params => ', sql, params);

        const result = await fastify.pg.summary.query(sql, params);

        return _.get(result, 'rows');
    });
    fastify.decorate('getRSOverall', async (fdate, tdate, sources, campaign_id, senderid, cli_id) => {
        let sql = '';
        let sourceSnip = '';
        let campaignSnip = '';
        let senderSnip = '';
        let sourcesStr = '';

        if (sources.length > 0) {
            sourcesStr = _.join(_.map(sources, (v) => `'${v}'`));
        }

        const params = [cli_id, fdate, tdate];

        if (sources.length > 0) {
            sourceSnip = `lower(intf_type) in (${sourcesStr}) and `;
        }
        if (!_.eq(campaign_id, 'all')) {
            campaignSnip = `campaign_id='${campaign_id}' and `;
        }
        if (!_.eq(senderid, 'all')) {
            senderSnip = `cli_hdr='${senderid}' and `;
        }

        sql = `select sum(tot_cnt) as totalrecieved, sum(submitted_cnt) as mtsuccess, sum(delivered_cnt) as dnsuccess, sum(dn_failed_cnt) as dnfailed,sum(rejected_cnt) as mtrejected,sum(dn_expired_cnt) as expired,sum(dn_pending_cnt) as dnpending       
                from summary.ui_traffic_mix_report where ${sourceSnip} ${campaignSnip} ${senderSnip}  cli_id=$1 and recv_date between $2 and $3 `;

        console.log('getRSOverall() sql params => ', sql, params);

        const result = await fastify.pg.summary.query(sql, params);
        console.log(result.rows);

        return _.get(result, 'rows');
    });

    fastify.decorate('getTodaysCountOverall', async (cli_id, fdatetime, tdatetime, source, campaign_id, senderid, countfor) => {
        console.log('getTodaysCountOverall() fetcing data from es for => ', [cli_id, fdatetime, tdatetime, countfor]);
        let terms = {};
        const filters = [];

        if (_.eq(countfor, 'mtsuccess')) terms = { sub_status: ['Success'] };
        if (_.eq(countfor, 'dnsuccess')) terms = { dn_delivery_status: ['Delivered'] };
        if (_.eq(countfor, 'mtrejected')) terms = { sub_status: ['Rejected', 'Failed', 'Expired'] };
        if (_.eq(countfor, 'dnfailed')) terms = { dn_delivery_status: ['Failed', 'Expired'] };

        filters.push({ term: { cli_id } });
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

        console.log(JSON.stringify(filters));

        const { body } = await fastify.elastic.count({
            index: 'sub_del_t2',
            body: { query: { bool: { filter: filters } } },
        });

        return body;
    });

    fastify.decorate('getTodaysCountBy', async (cli_id, fdatetime, tdatetime, source, campaign_id, senderid, countfor, groupby) => {
        console.log('getTodaysCountBy() fetcing data from es for => ', [cli_id, fdatetime, tdatetime, source, campaign_id, senderid, countfor, groupby]);
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

        filters.push({ term: { cli_id } });
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
                aggs: { groupby: { terms: { field: `${groupbyField}`, size: 1000 } } },
                size: 0,
            },
        };
        console.log(JSON.stringify(payload));

        const { body } = await fastify.elastic.search(payload);

        return body;
    });
};

module.exports = fp(reportdb);
