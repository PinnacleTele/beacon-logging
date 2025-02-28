const _ = require('lodash');
const mtz = require('moment-timezone');
const jsunicode = require('jsunicode');
const path = require('path');

const { rSourceSchema, rCampaignsSchema, rSenderIdsSchema } = require('../../../schema/report-schema');

/**
 * setup some routes
 * @param {import("fastify").FastifyInstance} fastify
 * @param {*} opts
 */
async function report(fastify, opts) {
    fastify.addHook('preValidation', async (req, reply) => {
        console.log('preValidation hook called');
        return req.jwtVerify();
    });

    fastify.post('/rsources', { preValidation: [], schema: rSourceSchema }, async (req, reply) => {
        try {
            const { dateselectiontype, fdate, tdate,cli_id } = req.body;
            const { tz } = req.user;
            let cli_ids = [];
            cli_ids = await fastify.getCliIds(cli_id, req);     
            const payload = [{ display_name: 'All', intf_grp_type: 'all' }];

            // TODO: FIXIT diff tz does not work well

            const { fromdateStrInIST, todateStrInIST, fromdatetimeStrInIST, todatetimeStrInIST, typeofdate } = await fastify.getFromAndToDate(tz, dateselectiontype, fdate, tdate, true);
            console.log('/rsources from and to date => ', fromdateStrInIST, todateStrInIST, fromdatetimeStrInIST, todatetimeStrInIST, typeofdate);

            if (_.eq(typeofdate, 'onlytoday')) {
                console.log('/rsources [onlytoday] getting data... ');
                const r1 = await fastify.getRSourcesForToday(cli_ids, fromdatetimeStrInIST, todatetimeStrInIST);
                const b1 = _.get(r1, 'aggregations.sources.buckets', []);
                console.log('/rsources resp from es =>', b1);
                for (const v of b1) {
                    payload.push({ display_name: _.toUpper(v.key), intf_grp_type: v.key });
                }
            } else if (_.eq(typeofdate, 'onlyprev')) {
                console.log('/rsources [onlyprev] getting data... ');
                const result = await fastify.getRSources(cli_ids, fromdateStrInIST, todateStrInIST);
                console.log('/rsources resp from db => ', result);
                payload.push(...result);
            } else {
                // both
                console.log('/rsources [both] getting data...');
                const [r1, r2] = await Promise.all([fastify.getRSourcesForToday(cli_ids, fromdatetimeStrInIST, todatetimeStrInIST), fastify.getRSources(cli_ids, fromdateStrInIST, todateStrInIST)]);
                const b1 = _.get(r1, 'aggregations.sources.buckets', []);
                console.log('/rsources resp from es =>', b1);
                const arr = [];
                for (const v of b1) {
                    arr.push({ display_name: _.toUpper(v.key), intf_grp_type: v.key });
                }

                console.log('/rsources resp from db => ', r2);
                arr.push(...r2);

                const grpby = _.groupBy(arr, 'display_name');

                const a = _.map(grpby, (k, v) => ({ display_name: v, intf_grp_type: _.toLower(v) }));
                payload.push(...a);
            }

            return payload;
        } catch (err) {
            console.log(err)
            const e = fastify.httpErrors.createError(500, 'Could not get source data. Please try again', { err });
            return e;
        }
    });

    fastify.post('/rcampaigns', { preValidation: [], schema: rCampaignsSchema }, async (req, reply) => {
        try {
            const { dateselectiontype, fdate, tdate, source, cli_id } = req.body;
            const { tz  } = req.user;
            const campaigns = [{ campaign_name: 'All', campaign_id: 'all' }];

            let cli_ids = [];
            cli_ids = await fastify.getCliIds(cli_id, req);  

            console.log('/rcampaigns incoming params => ', [dateselectiontype, fdate, tdate, source]);

            if (!_.eq(source, 'ui') && !_.eq(source, 'all')) {
                return campaigns;
            }

            // TODO: validate custom range date. shud not be less than the cur date

            const { fromdatetimeStrInIST, todatetimeStrInIST, typeofdate } = await fastify.getFromAndToDate(tz, dateselectiontype, fdate, tdate, true);
            console.log('/rcampaigns from and to date => ', fromdatetimeStrInIST, todatetimeStrInIST, typeofdate);

            const r1 = await fastify.getRCampaigns(cli_ids, fromdatetimeStrInIST, todatetimeStrInIST);

            campaigns.push(...r1);

            return campaigns;
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get campaigns data. Please try again', { err });

            return e;
        }
    });

    fastify.post('/rsenderids', { preValidation: [], schema: rSenderIdsSchema }, async (req, reply) => {
        try {
            const { dateselectiontype, fdate, tdate, source, campaign_id, cli_id } = req.body;
            const { tz } = req.user;
            const payload = [{ senderid: 'All', id: 'all' }];
            let senderidArr = [];
            
            let cli_ids = [];
            cli_ids = await fastify.getCliIds(cli_id, req);  

            // TODO: validate custom range date. shud not be less than the cur date

            const { fromdateStrInIST, todateStrInIST, fromdatetimeStrInIST, todatetimeStrInIST, typeofdate } = await fastify.getFromAndToDate(tz, dateselectiontype, fdate, tdate, true);
            console.log('/rsenderids from and to date => ', fromdateStrInIST, todateStrInIST, fromdatetimeStrInIST, todatetimeStrInIST, typeofdate);

            // get the intf_type for the source
            const result1 = await fastify.getInterfaceTypes(source);
            let interfaceArr = [];

            if (result1.length === 0) {
                console.log('/rcampaigns *** Could not find mapping in interface_type_mapping table for source. setting source as all  => ', source);
            }

            interfaceArr = _.compact(_.map(result1, 'intf_type'));
            
            if (_.eq(typeofdate, 'onlytoday')) {
                console.log('/rsenderids [onlytoday] getting data... ');
                const r1 = await fastify.getRSenderIdsForToday(cli_ids, fromdatetimeStrInIST, todatetimeStrInIST, source, campaign_id);
                const b1 = _.get(r1, 'aggregations.senderid.buckets', []);
                console.log('/rsenderids resp from es =>', b1);
                for (const v of b1) {
                    // payload.push({ senderid: v.key, id: v.key });
                    senderidArr.push(v.key);
                }
            } else if (_.eq(typeofdate, 'onlyprev')) {
                console.log('/rsenderids [onlyprev] getting data... ');
                const result = await fastify.getRSenderIds(cli_ids, fromdateStrInIST, todateStrInIST, interfaceArr, campaign_id);
                console.log('/rsenderids resp from db => ', result);
                senderidArr.push(..._.map(result, 'cli_hdr'));
            } else {
                // both
                console.log('/rsenderids [both] getting data...');
                const [r1, r2] = await Promise.all([fastify.getRSenderIdsForToday(cli_ids, fromdatetimeStrInIST, todatetimeStrInIST, source, campaign_id), fastify.getRSenderIds(cli_ids, fromdateStrInIST, todateStrInIST, interfaceArr, campaign_id)]);
                const b1 = _.get(r1, 'aggregations.senderid.buckets', []);
                for (const v of b1) {
                    senderidArr.push(v.key);
                }
                senderidArr.push(..._.map(r2, 'cli_hdr'));
            }

            // remove duplicate senderids (possible from both dateselection type) & **** remove empty/null senderids *****
            senderidArr = _.uniq(_.compact(senderidArr));

            for (const v of senderidArr) {
                payload.push({ senderid: v, id: v });
            }
            return payload;
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get senderid data. Please try again', { err });

            return e;
        }
    });
}

module.exports = report;
