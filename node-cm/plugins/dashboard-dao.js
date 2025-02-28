/* eslint-disable camelcase */
const fp = require('fastify-plugin');
const _ = require('lodash');

const dashdao = async (fastify, opts) => {
    fastify.decorate('getDashStats', async (cli_id, fdate, tdate) => {
        const sql = 'select (case when sum(tot_cnt) IS NOT NULL then sum(tot_cnt) else 0 end) as total from summary.ui_traffic_mix_report where cli_id=$1 and recv_date between $2 and $3';
        const params = [cli_id, fdate, tdate];
        console.log('getDashStats() sql params => ', sql, params);

        const result = await fastify.pg.summary.query(sql, params);
        return _.get(result, 'rows');
    });

    fastify.decorate('getDashStatsForToday', async (cli_id, fdatetime, tdatetime) => {
        console.log('getDashStatsForToday() fetcing data from es for => ', [cli_id, fdatetime, tdatetime]);

        const { body } = await fastify.elastic.count({
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
            },
        });

        return body;
    });

    fastify.decorate('getTodaysCountByStatus', async (cli_id, fdatetime, tdatetime, countfor) => {
        console.log('getTodaysCountByStatus() fetcing data from es for => ', [cli_id, fdatetime, tdatetime, countfor]);
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
                            { terms },
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
            },
        });

        return body;
    });

    fastify.decorate('getHourlyCount', async (cli_id, fdatetime, tdatetime, countfor) => {
        console.log('getHourlyCount() fetcing data from es for => ', [cli_id, fdatetime, tdatetime, countfor]);
        let terms = {};

        if (_.eq(countfor, 'mtsuccess')) terms = { sub_status: ['Success'] };
        if (_.eq(countfor, 'dnsuccess')) terms = { dn_delivery_status: ['Delivered'] };

        const { body } = await fastify.elastic.search({
            index: 'sub_del_t2',
            body: {
                query: {
                    bool: {
                        filter: [
                            { term: { cli_id } },
                            { exists: { field: 'sub_update_ts' } },
                            { terms },
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
                aggs: { hour: { terms: { field: 'recv_hour' } } },
                size: 0,
            },
        });

        return body;
    });
};

module.exports = fp(dashdao);
