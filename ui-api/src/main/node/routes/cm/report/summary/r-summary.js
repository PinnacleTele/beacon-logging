/* eslint-disable no-restricted-syntax */
const _ = require('lodash');
const mtz = require('moment-timezone');
const moment = require('moment');

const { rsummarySchema, rsummarydownloadSchema } = require('../../../../schema/report-summary-schema');

/**
 * setup some routes
 * @param {import("fastify").FastifyInstance} fastify
 * @param {*} opts
 */
async function rsummary(fastify, opts) {
    fastify.addHook('preValidation', async (req, reply) => {
        console.log('preValidation hook called');
        return req.jwtVerify();
    });

    async function processTodaysCount(momentInUserTZ, rToday, rTodayMTSuccess, rTodayMTRejected, rTodayDNSuccess, rTodayDNFailed, reportfor) {
        const result = [];
        const tBucket = _.get(rToday, 'aggregations.groupby.buckets', []);
        const sBucket = _.get(rTodayMTSuccess, 'aggregations.groupby.buckets', []);
        const rBucket = _.get(rTodayMTRejected, 'aggregations.groupby.buckets', []);
        const dsBucket = _.get(rTodayDNSuccess, 'aggregations.groupby.buckets', []);
        const dfBucket = _.get(rTodayDNFailed, 'aggregations.groupby.buckets', []);

        const sGroupby = _.groupBy(sBucket, 'key');
        const rGroupby = _.groupBy(rBucket, 'key');
        const dsGroupby = _.groupBy(dsBucket, 'key');
        const dfGroupby = _.groupBy(dfBucket, 'key');

        for (const obj of tBucket) {
            let tmts = 0;
            let tmtr = 0;
            let tdns = 0;
            let tdnf = 0;

            const key = _.get(obj, 'key');

            // get the mt success counts for this campaign
            let o = _.get(sGroupby, key, []);
            if (o.length > 0) tmts = Number(_.get(o[0], 'doc_count', 0));

            // get the rejected counts for this campaign
            o = _.get(rGroupby, key, []);
            if (o.length > 0) tmtr = Number(_.get(o[0], 'doc_count', 0));

            // get the dn success counts for this campaign
            o = _.get(dsGroupby, key, {});
            if (o.length > 0) tdns = Number(_.get(o[0], 'doc_count', 0));

            // get the dn failed counts for this campaign
            o = _.get(dfGroupby, key, {});
            if (o.length > 0) tdnf = Number(_.get(o[0], 'doc_count', 0));

            const ttotal = tmts + tmtr;
            const tdnpending = tmts - (tdns + tdnf);

            const mtsuccess = tmts;
            const dnsuccess = tdns;
            const dnfailed = tdnf;
            const mtrejected = tmtr;
            const dnpending = tdnpending;
            const total = ttotal;
            const obj1 = {
                recv_date: momentInUserTZ.format('YYYY-MM-DD'),
                totalrecieved: total,
                mtsuccess,
                mtrejected,
                dnsuccess,
                dnfailed,
                dnpending,
            };
            _.set(obj1, reportfor, _.eq(reportfor, 'source') ? _.toUpper(key) : key);
            result.push(obj1);
        }
        return result;
    }
    async function processSummary(req) {
        const { dateselectiontype, fdate, tdate, source, campaign_id, senderid, reportby } = req.body;
        const { tz, cli_id } = req.user;
        const payload = [];
        let total = 0;
        let mtsuccess = 0;
        let dnsuccess = 0;
        let dnfailed = 0;
        let mtrejected = 0;
        let dnpending = 0;

        console.log('/rsummary incoming params => ', [dateselectiontype, fdate, tdate, source, campaign_id, senderid, reportby]);

        // TODO: validate custom range date. shud not be less than the cur date

        // current date in users tz
        const momentInUserTZ = mtz().tz(tz);
        console.log('/rsummary current date in users tz ', momentInUserTZ.format('YYYY-MM-DD'), tz);

        // timezone is not supported for T-1 data
        const { fromdateStrInIST, todateStrInIST, typeofdate } = await fastify.getFromAndToDate(process.env.IST_ZONE_NAME, dateselectiontype, fdate, tdate, true);
        // for es
        const { fromdatetimeStrInIST: todayfts, todatetimeStrInIST: todaytts } = await fastify.getFromAndToDate(tz, 'today', null, null, true);
        console.log('/rsummary from and to date => ', fromdateStrInIST, todateStrInIST, todayfts, todaytts, typeofdate);

        // get the intf_type for the source
        const result1 = await fastify.getInterfaceTypes(source);
        let interfaceArr = [];

        if (result1.length === 0) {
            console.log('/rsummary *** Could not find mapping in interface_type_mapping table for source. setting source as all  => ', source);
        }

        interfaceArr = _.compact(_.map(result1, 'intf_type'));

        // TODO: implementation based on typeofdate is pending
        let result = [];
        if (_.eq(reportby, 'date')) {
            if (_.eq(typeofdate, 'onlytoday') || _.eq(typeofdate, 'both')) {
                const [rTodayMTSuccess, rTodayMTRejected, rTodayDNSuccess, rTodayDNFailed] = await Promise.all([fastify.getTodaysCountOverall(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'mtsuccess'), fastify.getTodaysCountOverall(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'mtrejected'), fastify.getTodaysCountOverall(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'dnsuccess'), fastify.getTodaysCountOverall(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'dnfailed')]);
                const tmts = Number(_.get(rTodayMTSuccess, 'count', 0));
                const tmtr = Number(_.get(rTodayMTRejected, 'count', 0));
                const tdns = Number(_.get(rTodayDNSuccess, 'count', 0));
                const tdnf = Number(_.get(rTodayDNFailed, 'count', 0));
                const ttotal = tmts + tmtr;
                const tdnpending = tmts - (tdns + tdnf);

                mtsuccess = tmts;
                dnsuccess = tdns;
                dnfailed = tdnf;
                mtrejected = tmtr;
                dnpending = tdnpending;
                total = ttotal;

                result = [{
                    recv_date: momentInUserTZ.format('YYYY-MM-DD'),
                    totalrecieved: total,
                    mtsuccess,
                    mtrejected,
                    dnsuccess,
                    dnfailed,
                    dnpending,
                }];
                console.log(`/rsummary [${typeofdate}] Todays count => `, result);
            }
            if (_.eq(typeofdate, 'both')) {
                // get the date for prev days
                const r1 = await fastify.getRSDatewise(fromdateStrInIST, todateStrInIST, interfaceArr, campaign_id, senderid, cli_id);
                // todays is already added above
                result.push(...r1);
                console.log(`/rsummary [${typeofdate}] final summary count => `, result);
            }
            if (_.eq(typeofdate, 'onlyprev')) {
                // get the date for prev days
                const r1 = await fastify.getRSDatewise(fromdateStrInIST, todateStrInIST, interfaceArr, campaign_id, senderid, cli_id);
                result = r1;
            }
        } else if (_.eq(reportby, 'campaign')) {
            // get the interface types for this interface group (UI)
            const rtypes = await fastify.getAllInterfaceTypesForGroup('UI');
            interfaceArr = _.compact(_.map(rtypes, 'intf_type'));

            if (_.eq(typeofdate, 'onlytoday') || _.eq(typeofdate, 'both')) {
                const [rToday, rTodayMTSuccess, rTodayMTRejected, rTodayDNSuccess, rTodayDNFailed] = await Promise.all([fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'total', 'campaign'), fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'mtsuccess', 'campaign'), fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'mtrejected', 'campaign'), fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'dnsuccess', 'campaign'), fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'dnfailed', 'campaign')]);
                result = await processTodaysCount(momentInUserTZ, rToday, rTodayMTSuccess, rTodayMTRejected, rTodayDNSuccess, rTodayDNFailed, reportby);
                console.log(`/rsummary [${typeofdate}] Todays count => `, result);
                console.log(`/rsummary [${typeofdate}] Todays count 123 => `, rTodayMTRejected);
            }
            if (_.eq(typeofdate, 'both')) {
                // get the date for prev days
                const r1 = await fastify.getRSCampaignwise(fromdateStrInIST, todateStrInIST, interfaceArr, campaign_id, senderid, cli_id);
                // todays is already added above
                result.push(...r1);
                console.log(`/rsummary [${typeofdate}] final summary count => `, result);
            }
            if (_.eq(typeofdate, 'onlyprev')) {
                // get the date for prev days
                const r1 = await fastify.getRSCampaignwise(fromdateStrInIST, todateStrInIST, interfaceArr, campaign_id, senderid, cli_id);
                result = r1;
            }
        } else if (_.eq(reportby, 'source')) {
            if (_.eq(typeofdate, 'onlytoday') || _.eq(typeofdate, 'both')) {
                const [rToday, rTodayMTSuccess, rTodayMTRejected, rTodayDNSuccess, rTodayDNFailed] = await Promise.all([fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'total', 'source'), fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'mtsuccess', 'source'), fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'mtrejected', 'source'), fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'dnsuccess', 'source'), fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'dnfailed', 'source')]);
                result = await processTodaysCount(momentInUserTZ, rToday, rTodayMTSuccess, rTodayMTRejected, rTodayDNSuccess, rTodayDNFailed, reportby);
                console.log(`/rsummary [${typeofdate}] Todays count => `, result);
            }
            if (_.eq(typeofdate, 'both')) {
                // get the date for prev days
                const r1 = await fastify.getRSSourcewise(fromdateStrInIST, todateStrInIST, interfaceArr, campaign_id, senderid, cli_id);
                // todays is already added above
                console.log('>>>>>>> ', r1);

                result.push(...r1);
                console.log(`/rsummary [${typeofdate}] final summary count => `, result);
            }
            if (_.eq(typeofdate, 'onlyprev')) {
                // get the date for prev days
                const r1 = await fastify.getRSSourcewise(fromdateStrInIST, todateStrInIST, interfaceArr, campaign_id, senderid, cli_id);
                result = r1;
            }
        } else if (_.eq(reportby, 'senderid')) {
            if (_.eq(typeofdate, 'onlytoday') || _.eq(typeofdate, 'both')) {
                const [rToday, rTodayMTSuccess, rTodayMTRejected, rTodayDNSuccess, rTodayDNFailed] = await Promise.all([fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'total', 'senderid'), fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'mtsuccess', 'senderid'), fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'mtrejected', 'senderid'), fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'dnsuccess', 'senderid'), fastify.getTodaysCountBy(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'dnfailed', 'senderid')]);
                result = await processTodaysCount(momentInUserTZ, rToday, rTodayMTSuccess, rTodayMTRejected, rTodayDNSuccess, rTodayDNFailed, reportby);
                console.log(`/rsummary [${typeofdate}] Todays count => `, result);
            }
            if (_.eq(typeofdate, 'both')) {
                // get the date for prev days
                const r1 = await fastify.getRSSenderidwise(fromdateStrInIST, todateStrInIST, interfaceArr, campaign_id, senderid, cli_id);
                // todays is already added above
                result.push(...r1);
                console.log(`/rsummary [${typeofdate}] final summary count => `, result);
            }
            if (_.eq(typeofdate, 'onlyprev')) {
                // get the date for prev days
                const r1 = await fastify.getRSSenderidwise(fromdateStrInIST, todateStrInIST, interfaceArr, campaign_id, senderid, cli_id);
                console.log('>>> resp from db', r1);
                result = r1;
            }
        } else {
            if (_.eq(typeofdate, 'onlytoday') || _.eq(typeofdate, 'both')) {
                const [rTodayMTSuccess, rTodayMTRejected, rTodayDNSuccess, rTodayDNFailed] = await Promise.all([fastify.getTodaysCountOverall(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'mtsuccess'), fastify.getTodaysCountOverall(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'mtrejected'), fastify.getTodaysCountOverall(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'dnsuccess'), fastify.getTodaysCountOverall(cli_id, todayfts, todaytts, source, campaign_id, senderid, 'dnfailed')]);
                const tmts = Number(_.get(rTodayMTSuccess, 'count', 0));
                const tmtr = Number(_.get(rTodayMTRejected, 'count', 0));
                const tdns = Number(_.get(rTodayDNSuccess, 'count', 0));
                const tdnf = Number(_.get(rTodayDNFailed, 'count', 0));
                const ttotal = tmts + tmtr;
                const tdnpending = tmts - (tdns + tdnf);

                mtsuccess = tmts;
                dnsuccess = tdns;
                dnfailed = tdnf;
                mtrejected = tmtr;
                dnpending = tdnpending;
                total = ttotal;

                result = [{
                    totalrecieved: +total,
                    mtsuccess,
                    mtrejected,
                    dnsuccess,
                    dnfailed,
                    dnpending,
                }];
                console.log(`/rsummary [${typeofdate}] Todays count => `, result);
            }
            if (_.eq(typeofdate, 'both')) {
                // get the date for prev days
                const r1 = await fastify.getRSOverall(fromdateStrInIST, todateStrInIST, interfaceArr, campaign_id, senderid, cli_id);

                // todays is already added above
                result.push(...r1);

                if (result.length > 0) {
                    // sum the results
                    // total = _.sumBy(result, 'totalrecieved');
                    total = _.sumBy(result, (o) => Number(o.totalrecieved));
                    mtsuccess = _.sumBy(result, (o) => Number(o.mtsuccess));
                    mtrejected = _.sumBy(result, (o) => Number(o.mtrejected));
                    dnsuccess = _.sumBy(result, (o) => Number(o.dnsuccess));
                    dnfailed = _.sumBy(result, (o) => Number(o.dnfailed));
                    dnpending = _.sumBy(result, (o) => Number(o.dnpending));

                    result = [{
                        totalrecieved: total,
                        mtsuccess,
                        mtrejected,
                        dnsuccess,
                        dnfailed,
                        dnpending,
                    }];
                }
                console.log(`/rsummary [${typeofdate}] final summary count => `, result);
            }
            if (_.eq(typeofdate, 'onlyprev')) {
                // get the date for prev days
                const r1 = await fastify.getRSOverall(fromdateStrInIST, todateStrInIST, interfaceArr, campaign_id, senderid, cli_id);
                result = r1;
            }
        }

        console.log('/rsummary resp from datastore => ', result);

        for (const r of result) {
            // recv_date will not be present for Overall count.
            const recv_date = moment(r.recv_date).format('DD-MMM-YYYY');
            total = Number(_.isNull(r.totalrecieved) ? 0 : r.totalrecieved);

            // ignore if total is zero
            if (total === 0) continue;

            mtsuccess = Number(_.isNull(r.mtsuccess) ? 0 : r.mtsuccess);
            dnsuccess = Number(_.isNull(r.dnsuccess) ? 0 : r.dnsuccess);
            dnfailed = Number(_.isNull(r.dnfailed) ? 0 : r.dnfailed) + Number(_.isNull(_.get(r, 'expired', 0)) ? 0 : _.get(r, 'expired', 0));
            mtrejected = Number(_.isNull(r.mtrejected) ? 0 : r.mtrejected);
            dnpending = Number(_.isNull(r.dnpending) ? 0 : r.dnpending);
            dnpending = (dnpending < 0) ? 0 : dnpending;

            let dnsuccesspercentage = _.round((dnsuccess / mtsuccess) * 100, 2);
            let mtsuccesspercentage = _.round((mtsuccess / total) * 100, 2);
            let dnfailedpercentage = _.round((dnfailed / mtsuccess) * 100, 2);
            let mtrejectedpercentage = _.round((mtrejected / total) * 100, 2);
            let dnpendingpercentage = _.round((dnpending / mtsuccess) * 100, 2);
            dnfailedpercentage = _.isFinite(dnfailedpercentage) ? dnfailedpercentage : 0;
            mtsuccesspercentage = _.isFinite(mtsuccesspercentage) ? mtsuccesspercentage : 0;
            dnsuccesspercentage = _.isFinite(dnsuccesspercentage) ? dnsuccesspercentage : 0;
            mtrejectedpercentage = _.isFinite(mtrejectedpercentage) ? mtrejectedpercentage : 0;
            dnpendingpercentage = _.isFinite(dnpendingpercentage) ? dnpendingpercentage : 0;

            dnsuccesspercentage = _.isNaN(dnsuccesspercentage) ? '0' : dnsuccesspercentage;
            mtsuccesspercentage = _.isNaN(mtsuccesspercentage) ? '0' : mtsuccesspercentage;
            dnfailedpercentage = _.isNaN(dnfailedpercentage) ? '0' : dnfailedpercentage;
            mtrejectedpercentage = _.isNaN(mtrejectedpercentage) ? '0' : mtrejectedpercentage;
            dnpendingpercentage = _.isNaN(dnpendingpercentage) ? '0' : dnpendingpercentage;

            const obj = {
                total,
                total_human: fastify.coolFormat(total, 0),
                mtsuccess,
                mtsuccess_human: fastify.coolFormat(mtsuccess, 0),
                dnsuccess,
                dnsuccess_human: fastify.coolFormat(dnsuccess, 0),
                dnfailed,
                dnfailed_human: fastify.coolFormat(dnfailed, 0),
                mtrejected,
                mtrejected_human: fastify.coolFormat(mtrejected, 0),
                dnpending,
                dnpending_human: fastify.coolFormat(dnpending, 0),
                dnsuccesspercentage,
                mtsuccesspercentage,
                dnfailedpercentage,
                mtrejectedpercentage,
                dnpendingpercentage,
            };

            // set the date
            if (!_.eq(reportby, 'overall')) {
                _.set(obj, 'recv_date', recv_date);
            }
            // set campaign
            if (_.eq(reportby, 'campaign')) {
                _.set(obj, 'campaign_name', r.campaign);
                // _.set(obj, 'campaign_id', r.campaign_id);
            }
            // set source
            if (_.eq(reportby, 'source')) {
                _.set(obj, 'source', r.source);
            }
            // set senderid
            if (_.eq(reportby, 'senderid')) {
                _.set(obj, 'senderid', r.senderid);
            }

            // TODO: Sorting
            payload.push(obj);
        }

        console.log('payload => ', payload);
        return payload;
    }

    fastify.post('/rsummary', { preValidation: [], schema: rsummarySchema }, async (req, reply) => {
        try {
            return await processSummary(req);
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get summary data. Please try again', { err });

            return e;
        }
    });

    fastify.post('/rsummarydownload', { preValidation: [], schema: rsummarydownloadSchema }, async (req, reply) => {
        try {
            const { reportby, dateselectiontype } = req.body;
            let header = '';

            // construct file headers
            if (_.eq(reportby, 'date')) header = 'Date,Total,Submitted to Telco,Rejected,Delivered,Failed,Pending,Submitted %,Rejected %,Delivered %,Failed %,Pending %';
            if (_.eq(reportby, 'campaign')) header = 'Date,Campaign,Total,Submitted to Telco,Rejected,Delivered,Failed,Pending,Submitted %,Rejected %,Delivered %,Failed %,Pending %';
            if (_.eq(reportby, 'source')) header = 'Date,Source,Total,Submitted to Telco,Rejected,Delivered,Failed,Pending,Submitted %,Rejected %,Delivered %,Failed %,Pending %';
            if (_.eq(reportby, 'senderid')) header = 'Date,SenderId,Total,Submitted to Telco,Rejected,Delivered,Failed,Pending,Submitted %,Rejected %,Delivered %,Failed %,Pending %';
            if (_.eq(reportby, 'overall')) header = 'Total,Submitted to Telco,Rejected,Delivered,Failed,Pending,Submitted %,Rejected %,Delivered %,Failed %,Pending %';

            const payload = await processSummary(req);

            reply.type('text/csv');
            reply.header('Content-Disposition', `attachment;filename=${reportby}-${dateselectiontype}.csv`); // prevent ie from opening in app and shows save as dialog instead
            const arr = [];

            arr.push(header);
            for (const o of payload) {
                if (_.eq(reportby, 'date')) arr.push(`${o.recv_date},${o.total},${o.mtsuccess},${o.mtrejected},${o.dnsuccess},${o.dnfailed},${o.dnpending},${o.mtsuccesspercentage},${o.mtrejectedpercentage},${o.dnsuccesspercentage},${o.dnfailedpercentage},${o.dnpendingpercentage}`);
                if (_.eq(reportby, 'campaign')) arr.push(`${o.recv_date},${(o.campaign_name) ? o.campaign_name : ''},${o.total},${o.mtsuccess},${o.mtrejected},${o.dnsuccess},${o.dnfailed},${o.dnpending},${o.mtsuccesspercentage},${o.mtrejectedpercentage},${o.dnsuccesspercentage},${o.dnfailedpercentage},${o.dnpendingpercentage}`);
                if (_.eq(reportby, 'source')) arr.push(`${o.recv_date},${o.source},${o.total},${o.mtsuccess},${o.mtrejected},${o.dnsuccess},${o.dnfailed},${o.dnpending},${o.mtsuccesspercentage},${o.mtrejectedpercentage},${o.dnsuccesspercentage},${o.dnfailedpercentage},${o.dnpendingpercentage}`);
                if (_.eq(reportby, 'senderid')) arr.push(`${o.recv_date},${o.senderid},${o.total},${o.mtsuccess},${o.mtrejected},${o.dnsuccess},${o.dnfailed},${o.dnpending},${o.mtsuccesspercentage},${o.mtrejectedpercentage},${o.dnsuccesspercentage},${o.dnfailedpercentage},${o.dnpendingpercentage}`);
                if (_.eq(reportby, 'overall')) arr.push(`${o.total},${o.mtsuccess},${o.mtrejected},${o.dnsuccess},${o.dnfailed},${o.dnpending},${o.mtsuccesspercentage},${o.mtrejectedpercentage},${o.dnsuccesspercentage},${o.dnfailedpercentage},${o.dnpendingpercentage}`);
            }
            reply.send(_.join(arr, '\n'));
        } catch
        (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not get data for source filter. Please try again', [req.id]);

            return e;
        }
    });
}

module.exports = rsummary;
