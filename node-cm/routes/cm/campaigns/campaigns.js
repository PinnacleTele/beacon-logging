/* eslint-disable no-unused-expressions */
/* eslint-disable no-restricted-syntax */
const _ = require('lodash');
const jsunicode = require('jsunicode');
const requestIP = require('request-ip');
const mtz = require('moment-timezone');
const moment = require('moment');

const { senderidsSchema, entityidsSchema } = require('../../../schema/generic-schema');

const {
    msginfoSchema, cnameuniqueSchema, clistSchema, ctodaysstatsSchema, cdetailsSchema,
    cdetailsbyfileSchema, cschedstatsSchema, cslistSchema, csdetailsSchema, csdeleteSchema,
    csupdateSchema, cprocessedstatsSchema, intlsenderidsSchema,
} = require('../../../schema/campaigns-schema');

async function campaigns(fastify, opts) {
    fastify.addHook('preValidation', async (req, reply) => {
        console.log('preValidation hook called');
        return req.jwtVerify();
    });

    // preValidation: [fastify.verifyAccessToken]
    // fastify.addHook('onRequest', fastify.csrfProtection);

    async function getCountForFile(cli_id, cts, cname, fileid, tz) {
        console.log(fileid);

        // convert to user tz and check if the cam date falls today or prev date
        const tzcurrent = mtz().tz(tz).format('YYYY-MM-DD');
        const tzcts = mtz.tz(cts, tz).format('YYYY-MM-DD');

        let total = 0;
        let totalUnique = 0; // represents the distinct count of base_msg_id from es
        let mtsuccess = 0;
        let dnsuccess = 0;
        let dnfailed = 0;
        let mtrejected = 0;
        let dnpending = 0;
        let isTodaysCampaign = false;

        // today - get count from elastic
        if (_.eq(tzcts, tzcurrent)) {
            console.log(`/getCountForFile campaign ${cname} is [todays campaign]`);
            const [rTodayMTSuccess, rTodayMTRejected, rTodayDNSuccess, rTodayDNFailed, rTodayProcessed] = await Promise.all([fastify.getTodaysCountForFileId(cli_id, fileid, 'mtsuccess'), fastify.getTodaysCountForFileId(cli_id, fileid, 'mtrejected'), fastify.getTodaysCountForFileId(cli_id, fileid, 'dnsuccess'), fastify.getTodaysCountForFileId(cli_id, fileid, 'dnfailed'), fastify.getTodaysTotalProcessedForFileId(cli_id, fileid)]);
            const tmts = Number(_.get(rTodayMTSuccess, 'count', 0));
            const tmtr = Number(_.get(rTodayMTRejected, 'count', 0));
            const tdns = Number(_.get(rTodayDNSuccess, 'count', 0));
            const tdnf = Number(_.get(rTodayDNFailed, 'count', 0));
            const tprocessedUnique = Number(_.get(rTodayProcessed, 'count', 0));
            const ttotal = tmts + tmtr;
            const tdnpending = tmts - (tdns + tdnf);

            mtsuccess = tmts;
            dnsuccess = tdns;
            dnfailed = tdnf;
            mtrejected = tmtr;
            dnpending = tdnpending;
            total = ttotal;
            totalUnique = tprocessedUnique;
            isTodaysCampaign = true;
        } else {
            // previous day campaign - get count from postgres summary
            console.log(`/getCountForFile campaign ${cname} is [old campaign]`);

            const result = await fastify.getSummaryCountForFileId(cli_id, fileid);
            console.log('/getCountForFile resp from db => ', result);
            const r = _.get(result, 0, {});

            if (!_.isEmpty(r)) {
                total = Number(_.isNull(r.totalrecieved) ? 0 : r.totalrecieved);
                mtsuccess = Number(_.isNull(r.mtsuccess) ? 0 : r.mtsuccess);
                dnsuccess = Number(_.isNull(r.dnsuccess) ? 0 : r.dnsuccess);
                dnfailed = Number(_.isNull(r.dnfailed) ? 0 : r.dnfailed) + Number(_.isNull(r.expired) ? 0 : r.expired);
                mtrejected = Number(_.isNull(r.mtrejected) ? 0 : r.mtrejected);
                dnpending = Number(_.isNull(r.dnpending) ? 0 : r.dnpending);
                totalUnique = 0; // for T-1 the status will always be completed. so value does not matter
            }
        }
        return { total, mtsuccess, mtrejected, dnsuccess, dnfailed, dnpending, totalUnique, isTodaysCampaign };
    }

    fastify.post('/msginfo', { preValidation: [], schema: msginfoSchema }, async (req, reply) => {
        try {
            const { msg } = req.body;
            const { cli_id } = req.user;
            let msgconverted = '';

            const obj = await fastify.msginfo(cli_id, msg);

            console.log('/msginfo resp from remote service => ', obj);
            const isUnicode = _.get(obj, 'is_unicode', null);
            const msgraw = _.get(obj, 'msg', null);
            msgconverted = msgraw;
            if (isUnicode) {
                // convert the hex to text
                try {
                    // replace all the occurrence of {#var#} with the equivalent hex value
                    // convert the hex to unicode
                    msgconverted = jsunicode.decode(msgraw, { encoding: jsunicode.constants.encoding.utf16 });
                } catch (e) {
                    console.error(e);
                    // ignore
                }
            }
            return {
                chars: _.get(obj, 'msg_length', 0),
                parts: _.get(obj, 'parts', 0),
                isUnicode,
                msg: msgconverted,
            };
        } catch (err) {
            req.log.error(`${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not get message info. Please try again', { err });

            return e;
        }
    });

    fastify.get('/entityids', { preValidation: [], schema: entityidsSchema }, async (req, reply) => {
        try {
            const { cli_id, msg_type } = req.user;

            const result = await fastify.getEntityids(cli_id, msg_type);

            console.log('entityids => ', result);

            return result;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);

            return fastify.httpErrors.createError(500, 'Could not get entity ids. Please try again', { code });
        }
    });

    fastify.get('/senderids', { preValidation: [], schema: senderidsSchema }, async (req, reply) => {
        try {
            const { cli_id, msg_type } = req.user;

            const result1 = await fastify.findUserById(cli_id);
            const userObj = _.get(result1, '0', {});
            if (_.isEmpty(userObj)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            }
            const tmpl_group_id = _.get(userObj, 'dlt_templ_grp_id');

            const payload = [];
            const result = await fastify.getSenderids(msg_type, tmpl_group_id);

            console.log('/senderids resp from db => ', result);

            return result;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not get sender id. Please try again', { code });
            // const e = fastify.httpErrors.createError(500, err);

            return e;
        }
    });

    fastify.get('/intlsenderids', { preValidation: [], schema: intlsenderidsSchema }, async (req, reply) => {
        try {
            const { cli_id } = req.user;

            const result = await fastify.getIntlSenderids(cli_id);

            console.log('/intlsenderids resp from db => ', result);

            return result;
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get intl sender id. Please try again', { err });
            return e;
        }
    });

    fastify.get('/cnameunique', { preValidation: [], schema: cnameuniqueSchema }, async (req, reply) => {
        try {
            const result = await fastify.isCampaignNameFound(req);
            const { counts } = result[0];

            console.log('matching campaign name count => ', counts);
            const payload = { isUnique: (counts === 0) };
            return payload;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not get campaign name info. Please try again', { code });
            // const e = fastify.httpErrors.createError(500, err);

            return e;
        }
    });

    fastify.get('/ctodaysstats', { preValidation: [], schema: ctodaysstatsSchema }, async (req, reply) => {
        try {
            const { tz, cli_id } = req.user;
            let tot = 0;
            let completed = 0;
            let inprocess = 0;
            let failed = 0;

            const payload = {
                total: 0,
                completed: 0,
                running: 0,
                failed: 0,
            };

            // current date in users tz
            const momentInUserTZ = mtz().tz(tz);
            console.log('/ctodaysstats current date in users tz ', momentInUserTZ.format('YYYY-MM-DD'), tz);

            const { fromdatetimeStrInIST, todatetimeStrInIST } = await fastify.getFromAndToDate(tz, 'today', null, null, true);
            console.log('/ctodaysstats from and to date => ', fromdatetimeStrInIST, todatetimeStrInIST);

            const result1 = await fastify.getCampaigns(cli_id, fromdatetimeStrInIST, todatetimeStrInIST);
            console.log('/ctodaysstats total campaigns from db => ', result1.length);

            if (result1.length === 0) return payload;

            const idsAll = _.map(result1, 'id');
            const idsGroup = _.compact(_.map(result1, (obj) => {
                if ((obj.c_type === 'group')) return obj.id;
            }));

            const idsFile = _.pullAll(_.clone(idsAll), idsGroup);
            console.log(idsAll, idsGroup, idsFile);

            let groupBy2 = {};
            if (idsFile.length > 0) {
                const result2 = await fastify.getCampaignStatsForFileUpload(idsFile);
                groupBy2 = _.groupBy(result2, 'c_id');
            }

            let groupBy3 = {};
            if (idsGroup.length > 0) {
                const result3 = await fastify.getCampaignStatsForGroupUpload(idsGroup);
                groupBy3 = _.groupBy(result3, 'c_id');
            }

            for await (const obj of result1) {
                const id = _.get(obj, 'id');
                const cname = _.get(obj, 'c_name');
                const c_type = _.get(obj, 'c_type');
                const cts = _.get(obj, 'created_ts', null);
                const ctsMoment = mtz.tz(cts, tz);
                let total = 0;
                let status = _.get(obj, 'status', '');

                // not processing further as we are interested only in counts and not the camp details
                if (_.eq(_.toLower(status), 'failed')) {
                    _.set(obj, 'status', status);
                    continue;
                }

                if (_.eq(_.toLower(status), 'ginprocess')) status = 'inprocess';
                _.set(obj, 'status', status);

                // get the exclude count
                // TODO: re-visit logic. db call in iteration
                let excluded = 0;

                // get the count and total for this id
                if (!_.eq(c_type, 'group')) {
                    const o = _.get(groupBy2[id], 0, null);
                    total = _.get(o, 'total', 0);

                    _.set(obj, 'total', total);
                } else {
                    const o = _.get(groupBy3[id], 0, null);
                    const egrps = _.compact(_.split(_.get(o, 'exclude_group_ids', ''), ','));
                    // call only if it has exclude groups
                    if (egrps.length > 0) {
                        const r1 = await fastify.getCampaignStatForId(cli_id, id);
                        const resp1 = _.get(r1, 0, {});
                        if (!_.isEmpty(resp1)) {
                            excluded = _.get(resp1, 'excluded', 0);
                        }
                    }
                    total = _.get(o, 'total', 0);
                    _.set(obj, 'total', total);
                }
                total = _.get(obj, 'total', 0);

                // check if the total count matches with es total, if so mark it as completed else inprocess
                const campDate = ctsMoment.format('YYYY-MM-DD');
                const curDateInUserTz = momentInUserTZ.format('YYYY-MM-DD');

                if (_.eq(campDate, curDateInUserTz)) {
                    console.log('/ctodaysstats getting the total processed from es for campaign =>', cname);
                    // get the count from es
                    const r1 = await fastify.getTodaysTotalProcessedForCampaignId(cli_id, id);
                    const totalProcessed = Number(_.get(r1, 'count', 0));
                    console.log('>>>>>', cname, totalProcessed, total);

                    if (+totalProcessed >= (+total - +excluded)) {
                        _.set(obj, 'status', 'completed');
                    } else {
                        _.set(obj, 'status', 'inprocess');
                    }
                } else {
                    // T-1 campaigns - mark as completed
                    _.set(obj, 'status', 'completed');
                }
            }

            // find the stats counts
            for (const o of result1) {
                tot += 1;
                const s = _.toLower(_.get(o, 'status'));
                if (_.eq(s, 'inprocess')) inprocess += 1;
                if (_.eq(s, 'failed')) failed += 1;
                if (_.eq(s, 'completed')) completed += 1;
            }
            _.set(payload, 'total', tot);
            _.set(payload, 'completed', completed);
            _.set(payload, 'failed', failed);
            _.set(payload, 'running', inprocess);

            return payload;
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get campaign list. Please try again', { err });
            return e;
        }
    });

    fastify.post('/clist', { preValidation: [], schema: clistSchema }, async (req, reply) => {
        try {
            const { dateselectiontype, fdate, tdate } = req.body;
            const { tz, cli_id } = req.user;

            // current date in users tz
            const momentInUserTZ = mtz().tz(tz);
            console.log('/rsummary current date in users tz ', momentInUserTZ.format('YYYY-MM-DD'), tz);

            const { fromdatetimeStrInIST, todatetimeStrInIST } = await fastify.getFromAndToDate(tz, dateselectiontype, fdate, tdate, true);
            console.log('/clist from and to date => ', fromdatetimeStrInIST, todatetimeStrInIST);

            const result1 = await fastify.getCampaigns(cli_id, fromdatetimeStrInIST, todatetimeStrInIST);
            console.log('/clist total campaigns from db => ', result1.length);

            if (result1.length === 0) return [];

            const idsAll = _.map(result1, 'id');
            const idsGroup = _.compact(_.map(result1, (obj) => {
                if ((obj.c_type === 'group')) return obj.id;
            }));

            const idsFile = _.pullAll(_.clone(idsAll), idsGroup);
            console.log(idsAll, idsGroup, idsFile);

            let groupBy2 = {};
            if (idsFile.length > 0) {
                const result2 = await fastify.getCampaignStatsForFileUpload(idsFile);
                groupBy2 = _.groupBy(result2, 'c_id');
            }

            let groupBy3 = {};
            if (idsGroup.length > 0) {
                const result3 = await fastify.getCampaignStatsForGroupUpload(idsGroup);
                groupBy3 = _.groupBy(result3, 'c_id');
            }

            for await (const obj of result1) {
                const id = _.get(obj, 'id');
                const cname = _.get(obj, 'c_name');
                const c_type = _.get(obj, 'c_type');
                const cts = _.get(obj, 'created_ts', null);
                const ctsMoment = mtz.tz(cts, tz);
                const m = _.get(obj, 'msg', '');
                let total = 0;
                let status = _.get(obj, 'status', '');

                if (_.eq(_.toLower(status), 'ginprocess')) status = 'inprocess';
                _.set(obj, 'status', status);

                if (_.isEmpty(m)) _.set(obj, 'msg', '[Not Applicable]');

                if (!_.isNull(cts)) {
                    // convert to acc tz
                    const formattedDt = ctsMoment.format('MMM DD YYYY HH:mm z');

                    _.set(obj, 'created_ts', formattedDt);
                    _.set(obj, 'created_ts_unix', ctsMoment.unix());
                    _.set(obj, 'time_from_now', ctsMoment.fromNow());
                }

                // get the exclude count
                // TODO: re-visit logic. db call in iteration
                let excluded = 0;
                if (_.eq(c_type, 'group')) {
                    const o1 = _.get(groupBy3[id], 0, null);
                    const egrps = _.compact(_.split(_.get(o1, 'exclude_group_ids', ''), ','));
                    // call only if it has exclude groups
                    if (egrps.length > 0) {
                        const r1 = await fastify.getCampaignStatForId(cli_id, id);
                        const resp1 = _.get(r1, 0, {});
                        if (!_.isEmpty(resp1)) {
                            excluded = _.get(resp1, 'excluded', 0);
                        }
                    }
                    _.set(obj, 'excluded', excluded);
                    const excludedHuman = fastify.coolFormat(excluded, 0);
                    _.set(obj, 'excluded_human', excludedHuman);
                } else {
                    _.set(obj, 'excluded', 0);
                    _.set(obj, 'excluded_human', '0');
                }

                // get the count and total for this id
                if (!_.eq(c_type, 'group')) {
                    const o = _.get(groupBy2[id], 0, null);
                    total = _.get(o, 'total', 0);

                    _.set(obj, 'count', _.get(o, 'count', 0));
                    _.set(obj, 'total', total);
                    _.set(obj, 'exclude_group_count', 0);
                } else {
                    const o = _.get(groupBy3[id], 0, null);
                    total = _.get(o, 'total', 0);
                    _.set(obj, 'count', _.get(o, 'count', 0));
                    _.set(obj, 'total', total);
                    const egrps = _.compact(_.split(_.get(o, 'exclude_group_ids', ''), ','));
                    _.set(obj, 'exclude_group_count', egrps.length);
                }
                total = _.get(obj, 'total', 0);

                // check if the total count matches with es total, if so mark it as completed else inprocess
                const campDate = ctsMoment.format('YYYY-MM-DD');
                const curDateInUserTz = momentInUserTZ.format('YYYY-MM-DD');

                if (_.eq(campDate, curDateInUserTz)) {
                    console.log('/clist getting the total processed from es for campaign =>', cname);
                    // get the count from es
                    const r1 = await fastify.getTodaysTotalProcessedForCampaignId(cli_id, id);
                    const totalProcessed = Number(_.get(r1, 'count', 0));

                    if (!_.eq(c_type, 'group')) {
                        if (+totalProcessed >= (+total - +excluded)) {
                            _.set(obj, 'status', 'completed');
                        } else {
                            _.set(obj, 'status', 'inprocess');
                        }
                    } else {
                        // its group type. consider total only if status is completed in campaign_groups table and set final status accordingly
                        // get the status from campaign_groups
                        const rr = await fastify.getCampaignDetailByGroup(cli_id, id);
                        const resp1 = _.get(rr, 0, {});
                        const groupStatus = _.get(resp1, 'status', 'inprocess');

                        if (_.eq(groupStatus, 'completed')) {
                            if (+totalProcessed >= (+total - +excluded)) {
                                _.set(obj, 'status', 'completed');
                            } else {
                                _.set(obj, 'status', 'inprocess');
                            }
                        } else {
                            _.set(obj, 'status', 'inprocess');
                        }
                    }
                } else {
                    // T-1 campaigns - mark as completed
                    _.set(obj, 'status', 'completed');
                }

                // overide the status if the campaign is failed
                if (_.eq(_.toLower(status), 'failed')) {
                    _.set(obj, 'status', 'failed');
                }

                const totalHuman = fastify.coolFormat(total, 0);
                _.set(obj, 'total_human', totalHuman);
            }

            return result1;
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get campaign list. Please try again', { err });
            return e;
        }
    });

    fastify.get('/cdetails', { preValidation: [], schema: cdetailsSchema }, async (req, reply) => {
        try {
            const { c_id } = req.query;
            const { tz, cli_id } = req.user;

            // current date in users tz
            const momentInUserTZ = mtz().tz(tz);
            console.log('/rsummary current date in users tz ', momentInUserTZ.format('YYYY-MM-DD'), tz);

            const [result1, result2, result3] = await Promise.all([fastify.getCampaignDetailForId(cli_id, c_id), fastify.getCampaignStatForId(cli_id, c_id), fastify.getCampaignDetailFromGroupForId(cli_id, c_id)]);
            const resp1 = _.get(result1, 0, {});
            const resp2 = _.get(result2, 0, {});
            const resp3 = _.get(result3, 0, {});
            const payload = _.clone(resp1);
            console.log('/cdetails resp from from campaign_master db => ', payload);
            console.log('/cdetails resp from from campaign_files db => ', resp2);
            console.log('/cdetails resp from from campaign_groups db => ', resp3);

            if (_.isEmpty(payload)) return payload;

            const c_type = _.get(payload, 'c_type');
            const cLangType = _.get(payload, 'c_lang_type', null);
            const cts = _.get(payload, 'created_ts', null);
            const m = _.get(payload, 'msg', '');
            const cname = _.get(payload, 'c_name');

            if (_.isEmpty(m)) _.set(payload, 'msg', '[Not Applicable]');

            const ctsMoment = mtz.tz(cts, tz);

            if (!_.isNull(cts)) {
                // convert to acc tz
                const formattedDt = ctsMoment.format('MMM DD YYYY HH:mm z');
                _.set(payload, 'created_ts', formattedDt);
                _.set(payload, 'created_ts_unix', ctsMoment.unix());
                _.set(payload, 'time_from_now', ctsMoment.fromNow());
            }
            // get the count and total for this id

            if (_.eq(c_type, 'group')) {
                _.set(payload, 'count', _.get(resp3, 'count', 0));
                _.set(payload, 'total', _.get(resp3, 'total', 0));
                _.set(payload, 'status', 'inprocess');
                const egrps = _.compact(_.split(_.get(resp3, 'exclude_group_ids', ''), ','));
                _.set(payload, 'exclude_group_count', egrps.length);
            } else {
                _.set(payload, 'exclude_group_count', 0);
            }

            if (!_.isEmpty(resp2)) {
                _.set(payload, 'count', _.get(resp2, 'count', 0));
                _.set(payload, 'total', _.get(resp2, 'total', 0));
                const totalHuman = fastify.coolFormat(_.get(resp2, 'total', 0), 0);
                _.set(payload, 'total_human', totalHuman);
                const status = _.get(resp1, 'status', '');
                // _.eq(status, 'queued') ? _.set(payload, 'status', 'inprocess') : _.set(payload, 'status', status);
                _.eq(_.toLower(status), 'ginprocess') ? _.set(payload, 'status', 'inprocess') : _.set(payload, 'status', status);
            }

            return payload;
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get campaign details. Please try again', { err });
            return e;
        }
    });

    fastify.get('/cdetailsbyfile', { preValidation: [], schema: cdetailsbyfileSchema }, async (req, reply) => {
        try {
            const { c_id } = req.query;
            const { tz, cli_id } = req.user;
            const payload = { data: [] };

            // get the type of campaign
            const result1 = await fastify.getCampaignDetailForId(cli_id, c_id);
            const resp1 = _.get(result1, 0, {});

            if (_.isEmpty(resp1)) return payload;

            const [result2, result3] = await Promise.all([fastify.getCampaignDetailByFile(cli_id, c_id), fastify.getCampaignDetailByGroup(cli_id, c_id)]);

            // console.log('/cdetailsbyfile resp from from campaign_files db => ', payload);
            console.log('/cdetailsbyfile resp from from campaign_groups db => ', result3);

            const c_type = _.get(resp1, 'c_type');
            // group processing
            if (_.eq(c_type, 'group')) {
                const grpBy = _.groupBy(result2, 'group_id');

                for await (const obj of result3) {
                    const groupId = _.get(obj, 'group_id');
                    const o = _.get(grpBy, groupId); // data from campaign_files if there is one
                    const odata = _.get(o, 0, {});

                    if (!_.isEmpty(odata)) {
                        const cts = _.get(odata, 'cts'); // get the campaign date
                        const cname = _.get(odata, 'c_name'); // get the campaign name
                        const fileid = _.get(odata, 'id'); // get the campaign name

                        // structure - { total, mtsuccess, mtrejected, dnsuccess, dnfailed, dnpending, totalUnique, isTodaysCampaign }
                        const countObj = await getCountForFile(cli_id, cts, cname, fileid, tz);

                        console.log(`Count for group file id ${fileid} => ${JSON.stringify(countObj)}`);

                        const total = Number(_.get(odata, 'total', 0));
                        _.set(obj, 'total', total);
                        const totalHuman = fastify.coolFormat(total, 0);
                        _.set(obj, 'total_human', totalHuman);

                        const excluded = Number(_.get(odata, 'excluded', 0));
                        _.set(obj, 'excluded', excluded);
                        const excludedHuman = fastify.coolFormat(excluded, 0);
                        _.set(obj, 'excluded_human', excludedHuman);

                        _.set(obj, 'processed_count', countObj.total);
                        const totalProcessedHuman = fastify.coolFormat(countObj.total, 0);
                        _.set(obj, 'processed_count_human', totalProcessedHuman);
                        _.set(obj, 'valid', countObj.mtsuccess);
                        const validHuman = fastify.coolFormat(countObj.mtsuccess, 0);
                        _.set(obj, 'valid_human', validHuman);
                        _.set(obj, 'invalid', countObj.mtrejected);
                        const invalidHuman = fastify.coolFormat(countObj.mtrejected, 0);
                        _.set(obj, 'invalid_human', invalidHuman);
                        _.set(obj, 'failed', countObj.dnfailed);
                        const failedHuman = fastify.coolFormat(countObj.dnfailed, 0);
                        _.set(obj, 'failed_human', failedHuman);

                        if (countObj.isTodaysCampaign) {
                            if (+countObj.totalUnique >= (+total - +excluded)) {
                                _.set(obj, 'status', 'completed');
                            } else {
                                _.set(obj, 'status', 'inprocess');
                            }
                        } else {
                            // T-1 campaigns - mark as completed
                            _.set(obj, 'status', 'completed');
                        }
                    } else {
                        const totalHuman = fastify.coolFormat(_.get(obj, 'total', 0), 0);
                        _.set(obj, 'total_human', totalHuman);
                        _.set(obj, 'valid', 0);
                        _.set(obj, 'valid_human', '0');
                        _.set(obj, 'invalid', 0);
                        _.set(obj, 'invalid_human', '0');
                        _.set(obj, 'failed', 0);
                        _.set(obj, 'failed_human', '0');
                        _.set(obj, 'processed_count', 0);
                        _.set(obj, 'processed_count_human', '0');
                        _.set(obj, 'status', 'inprocess');
                    }
                    payload.data.push(obj);
                }
            } else {
                // other than group campaign type
                for await (const obj of result2) {
                    const cts = _.get(obj, 'cts'); // get the campaign date
                    const cname = _.get(obj, 'c_name'); // get the campaign name
                    const fileid = _.get(obj, 'id'); // get the campaign name

                    // structure - { total, mtsuccess, mtrejected, dnsuccess, dnfailed, dnpending }
                    const countObj = await getCountForFile(cli_id, cts, cname, fileid, tz);
                    console.log(`Count for file id ${fileid} => ${JSON.stringify(countObj)}`);

                    const total = Number(_.get(obj, 'total', 0));
                    _.set(obj, 'total', total);
                    const totalHuman = fastify.coolFormat(total, 0);
                    _.set(obj, 'total_human', totalHuman);

                    _.set(obj, 'excluded', 0);
                    _.set(obj, 'excluded_human', '0');

                    _.set(obj, 'processed_count', countObj.total);
                    const totalProcessedHuman = fastify.coolFormat(countObj.total, 0);
                    _.set(obj, 'processed_count_human', totalProcessedHuman);
                    _.set(obj, 'valid', countObj.mtsuccess);
                    const validHuman = fastify.coolFormat(countObj.mtsuccess, 0);
                    _.set(obj, 'valid_human', validHuman);
                    _.set(obj, 'invalid', countObj.mtrejected);
                    const invalidHuman = fastify.coolFormat(countObj.mtrejected, 0);
                    _.set(obj, 'invalid_human', invalidHuman);
                    _.set(obj, 'failed', countObj.dnfailed);
                    const failedHuman = fastify.coolFormat(countObj.dnfailed, 0);
                    _.set(obj, 'failed_human', failedHuman);

                    if (countObj.isTodaysCampaign) {
                        if (+countObj.totalUnique >= +total) {
                            _.set(obj, 'status', 'completed');
                        } else {
                            _.set(obj, 'status', 'inprocess');
                        }
                    } else {
                        // T-1 campaigns - mark as completed
                        _.set(obj, 'status', 'completed');
                    }

                    payload.data.push(obj);
                }
            }

            _.set(payload, 'asof_ts', mtz().tz(tz).format('MMM DD, YYYY HH:mm z'));
            _.set(payload, 'count', payload.data.length);

            let completedCount = 0;
            for (const obj of payload.data) {
                const s = _.get(obj, 'status', '');
                if (_.eq(s, 'completed')) completedCount += 1;
            }
            _.set(payload, 'completed_count', completedCount);
            _.set(payload, 'completion_percentage', 0);
            if (payload.data.length > 0) _.set(payload, 'completion_percentage', _.round((completedCount / payload.data.length) * 100));

            // find the overall processed count
            const processedCount = _.sumBy(payload.data, 'processed_count');
            const processedHuman = fastify.coolFormat(processedCount, 0);
            _.set(payload, 'processed_count', processedCount);
            _.set(payload, 'processed_count_human', processedHuman);

            return payload;
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get campaign details. Please try again', { err });
            // const e = fastify.httpErrors.createError(500, err);

            return e;
        }
    });

    fastify.get('/cprocessedstats', { preValidation: [], schema: cprocessedstatsSchema }, async (req, reply) => {
        try {
            const { tz, cli_id } = req.user;
            const { c_id } = req.query;
            const r = {};

            let total_processed = 0;
            let mtsuccess = 0;
            let dnsuccess = 0;
            let dnfailed = 0;
            let mtrejected = 0;
            let dnpending = 0;
            let status = 'inprocess';
            let totalUnique = 0; // represents the distinct count of base_msg_id from es

            // get the campaign cts
            const [result1, result2] = await Promise.all([fastify.getCampaignDetailForId(cli_id, c_id), fastify.getCampaignStatForId(cli_id, c_id)]);
            const resp1 = _.get(result1, 0, {});
            const resp2 = _.get(result2, 0, {});

            if (_.isEmpty(resp1)) {
                return fastify.httpErrors.badRequest('Could not find campaign information');
            }

            // convert to user tz and check if the cam date falls today or prev date
            const tzcurrent = mtz().tz(tz).format('YYYY-MM-DD');
            const tzcts = mtz.tz(resp1.created_ts, tz).format('YYYY-MM-DD');

            // today - get count from elastic
            if (_.eq(tzcts, tzcurrent)) {
                console.log(`/cprocessedstats Getting todays count for campaign ${resp1.c_name}`);
                const [rTodayMTSuccess, rTodayMTRejected, rTodayDNSuccess, rTodayDNFailed, rTodayProcessed] = await Promise.all([fastify.getTodaysCountForCampaignId(cli_id, c_id, 'mtsuccess'), fastify.getTodaysCountForCampaignId(cli_id, c_id, 'mtrejected'), fastify.getTodaysCountForCampaignId(cli_id, c_id, 'dnsuccess'), fastify.getTodaysCountForCampaignId(cli_id, c_id, 'dnfailed'), fastify.getTodaysTotalProcessedForCampaignId(cli_id, c_id)]);
                const tmts = Number(_.get(rTodayMTSuccess, 'count', 0));
                const tmtr = Number(_.get(rTodayMTRejected, 'count', 0));
                const tdns = Number(_.get(rTodayDNSuccess, 'count', 0));
                const tdnf = Number(_.get(rTodayDNFailed, 'count', 0));
                const tprocessedUnique = Number(_.get(rTodayProcessed, 'count', 0));
                const ttotal = tmts + tmtr;
                const tdnpending = tmts - (tdns + tdnf);

                mtsuccess = tmts;
                dnsuccess = tdns;
                dnfailed = tdnf;
                mtrejected = tmtr;
                dnpending = tdnpending;
                total_processed = ttotal;
                totalUnique = tprocessedUnique;

                console.log('Todays count => ', {
                    total: total_processed,
                    mtsuccess,
                    mtrejected,
                    dnsuccess,
                    dnfailed,
                    dnpending,
                    totalUnique,
                });
            } else {
                console.log(`/cprocessedstats Getting prev day count for campaign ${resp1.c_name}`);

                // summary form pg
                const result = await fastify.getSummaryCountForCampaignId(cli_id, c_id);
                console.log('/cprocessedstats resp from db => ', result);
                const r = _.get(result, 0, {});

                if (!_.isEmpty(r)) {
                    total_processed = Number(_.isNull(r.totalrecieved) ? 0 : r.totalrecieved);
                    mtsuccess = Number(_.isNull(r.mtsuccess) ? 0 : r.mtsuccess);
                    dnsuccess = Number(_.isNull(r.dnsuccess) ? 0 : r.dnsuccess);
                    dnfailed = Number(_.isNull(r.dnfailed) ? 0 : r.dnfailed) + Number(_.isNull(r.expired) ? 0 : r.expired);
                    mtrejected = Number(_.isNull(r.mtrejected) ? 0 : r.mtrejected);
                    dnpending = Number(_.isNull(r.dnpending) ? 0 : r.dnpending);
                }
                // for T-1 the status will always be completed
                status = 'completed';
            }
            let dnsuccesspercentage = _.round((dnsuccess / mtsuccess) * 100, 2);
            let mtsuccesspercentage = _.round((mtsuccess / total_processed) * 100, 2);
            let dnfailedpercentage = _.round((dnfailed / mtsuccess) * 100, 2);
            let mtrejectedpercentage = _.round((mtrejected / total_processed) * 100, 2);
            let dnpendingpercentage = _.round((dnpending / mtsuccess) * 100, 2);

            dnsuccesspercentage = (!dnsuccesspercentage) ? 0 : dnsuccesspercentage;
            mtsuccesspercentage = (!mtsuccesspercentage) ? 0 : mtsuccesspercentage;
            dnfailedpercentage = (!dnfailedpercentage) ? 0 : dnfailedpercentage;
            mtrejectedpercentage = (!mtrejectedpercentage) ? 0 : mtrejectedpercentage;
            dnpendingpercentage = (!dnpendingpercentage) ? 0 : dnpendingpercentage;

            // set the status
            if (!_.isEmpty(resp2)) {
                const tot = _.get(resp2, 'total', 0);
                const excluded = _.get(resp2, 'excluded', 0);
                if (total_processed >= (+tot - +excluded)) status = 'completed';
            }
            if (_.eq(tzcts, tzcurrent)) {
                if (!_.isEmpty(resp2)) {
                    const tot = _.get(resp2, 'total', 0);
                    const excluded = _.get(resp2, 'excluded', 0);
                    if (+totalUnique >= (+tot - +excluded)) status = 'completed';
                }
            }

            // overide the status if the campaign is failed
            if (_.eq(_.toLower(resp1.status), 'failed')) {
                status = 'failed';
            }

            const obj = {
                total_processed,
                total_processed_human: fastify.coolFormat(total_processed, 0),
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
                status,
            };

            return obj;
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get campaign stats. Please try again', { err });
            // const e = fastify.httpErrors.createError(500, err);

            return e;
        }
    });

    fastify.get('/cschedstats', { preValidation: [], schema: cschedstatsSchema }, async (req, reply) => {
        try {
            const { tz, cli_id } = req.user;
            const payload = {
                today: 0,
                thisweek: 0,
                thismonth: 0,
                nextmonth: 0,
            };

            const { istFromStr: todayfts, istToStr: todaytts } = await fastify.getFromAndToDateForSchedule(tz, 'today', null, null);
            const { istFromStr: thisweekfts, istToStr: thisweektts } = await fastify.getFromAndToDateForSchedule(tz, 'this week', null, null);
            const { istFromStr: thismonthfts, istToStr: thismonthtts } = await fastify.getFromAndToDateForSchedule(tz, 'this month', null, null);
            const { istFromStr: nextmonthfts, istToStr: nextmonthtts } = await fastify.getFromAndToDateForSchedule(tz, 'next month', null, null);
            console.log('/cschedstats from and to date => ', [todayfts, todaytts], [thisweekfts, thisweektts], [thismonthfts, thismonthtts], [nextmonthfts, nextmonthtts]);

            const [rToday, rThisweek, rThismonth, rNextmonth] = await Promise.all([fastify.getScheduledCampaignStats(cli_id, todayfts, todaytts), fastify.getScheduledCampaignStats(cli_id, thisweekfts, thisweektts), fastify.getScheduledCampaignStats(cli_id, thismonthfts, thismonthtts), fastify.getScheduledCampaignStats(cli_id, nextmonthfts, nextmonthtts)]);

            _.set(payload, 'today', _.get(rToday[0], 'count', 0));
            _.set(payload, 'thisweek', _.get(rThisweek[0], 'count', 0));
            _.set(payload, 'thismonth', _.get(rThismonth[0], 'count', 0));
            _.set(payload, 'nextmonth', _.get(rNextmonth[0], 'count', 0));

            return payload;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not get scheduled stats. Please try again', { code });
            // const e = fastify.httpErrors.createError(500, err);

            return e;
        }
    });

    fastify.post('/cslist', { preValidation: [], schema: cslistSchema }, async (req, reply) => {
        try {
            const { dateselectiontype, fdate, tdate } = req.body;
            const { tz, cli_id } = req.user;

            // TODO: validate custom range date. shud not be less than the cur date

            const { istFromStr, istToStr } = await fastify.getFromAndToDateForSchedule(tz, dateselectiontype, fdate, tdate);
            console.log('/cslist from and to date for  => ', istFromStr, istToStr, [dateselectiontype, fdate, tdate]);

            const result = await fastify.getScheduledCampaigns(cli_id, istFromStr, istToStr);

            console.log('/cslist total sched campaigns from db for => ', result.length, [dateselectiontype, fdate, tdate]);

            if (result.length === 0) return [];

            for await (const obj of result) {
                const id = _.get(obj, 'id');
                const c_type = _.get(obj, 'c_type');
                const cLangType = _.get(obj, 'c_lang_type', null);
                const cts = _.get(obj, 'created_ts', null);
                const sts = _.get(obj, 'scheduled_ts', null);
                const ctsMoment = mtz.tz(cts, tz);
                const m = _.get(obj, 'msg', '');
                const selectedts = _.get(obj, 'selected_dt', null);

                // selected sched dt by the user
                const selectedArr = _.split(selectedts, ' ');
                const selectedtsParsed = moment(`${selectedArr[3]}-${selectedArr[1]}-${selectedArr[2]} ${selectedArr[4]}`, 'YYYY-MMM-DD HH:mm:ss');
                _.set(obj, 'selected_dt', selectedtsParsed.format('YYYY-MM-DD HH:mm:ss'));

                if (_.isEmpty(m)) _.set(obj, 'msg', '[Not Applicable]');

                // convert hex to unicode string
                // if (_.eq(cLangType, 'unicode')) {
                //     try {
                //         const msg = jsunicode.decode(m, { encoding: jsunicode.constants.encoding.utf16 });
                //         _.set(obj, 'msg', msg);
                //     } catch (e) {
                //         // ignore the error. continue with original message
                //     }
                // }

                if (!_.isNull(cts)) {
                    // convert to acc tz
                    const formattedDt = ctsMoment.format('MMM DD, YYYY HH:mm z');
                    _.set(obj, 'created_ts', formattedDt);
                    _.set(obj, 'created_ts_unix', ctsMoment.unix());
                }

                // convert sched ts to users tz
                const stsMoment = mtz.tz(sts, tz);
                const formattedDt = stsMoment.format('MMM DD, YYYY HH:mm');
                _.set(obj, 'scheduled_ts', formattedDt);
                _.set(obj, 'scheduled_ts_unix', stsMoment.unix());

                // get the count and total for this id
                if (!_.eq(c_type, 'group')) {
                    const totals = _.get(obj, 'totals', 0);
                    const filenames = _.get(obj, 'filenames', 0);
                    const total = _.sum(_.map(_.split(totals, ','), (v) => Number(v)));

                    _.set(obj, 'count', _.split(filenames, ',').length);
                    _.set(obj, 'total', total);
                    _.set(obj, 'exclude_group_count', 0);
                } else {
                    const grps = _.compact(_.split(_.get(obj, 'group_ids', ''), ','));
                    const egrps = _.compact(_.split(_.get(obj, 'exclude_group_ids', ''), ','));
                    _.set(obj, 'exclude_group_count', egrps.length);
                    _.set(obj, 'count', grps.length);

                    // get the group counts from redis
                    let totalContacts = 0;

                    for await (const gid of grps) {
                        const total = await fastify.getTotalContacts(cli_id, gid, 'normal');
                        totalContacts += total;
                    }
                    _.set(obj, 'total', totalContacts);
                }

                const totalHuman = fastify.coolFormat(_.get(obj, 'total', 0), 0);
                _.set(obj, 'total_human', totalHuman);
            }

            return result;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not get campaign list. Please try again', { code });
            // const e = fastify.httpErrors.createError(500, err);

            return e;
        }
    });

    fastify.get('/csdetails', { preValidation: [], schema: csdetailsSchema }, async (req, reply) => {
        try {
            const { c_id, at_id } = req.query;
            const { tz, cli_id } = req.user;

            const result = await fastify.getScheduledCampaignDetailForId(cli_id, c_id, at_id);

            const obj = _.get(result, 0, {});
            console.log('/cdetails resp from from campaign_master db => ', obj);

            if (_.isEmpty(obj)) return obj;

            const c_type = _.get(obj, 'c_type');
            const cLangType = _.get(obj, 'c_lang_type', null);
            const cts = _.get(obj, 'created_ts', null);
            const sts = _.get(obj, 'scheduled_ts', null);
            const ctsMoment = mtz.tz(cts, tz);
            const m = _.get(obj, 'msg', '');
            const selectedts = _.get(obj, 'selected_dt', null);
            const selectedZone = _.get(obj, 'selected_zone', null);

            // selected sched dt by the user
            const selectedArr = _.split(selectedts, ' ');
            const selectedtsParsed = moment(`${selectedArr[3]}-${selectedArr[1]}-${selectedArr[2]} ${selectedArr[4]}`, 'YYYY-MMM-DD HH:mm:ss');
            _.set(obj, 'selected_dt', `${selectedtsParsed.format('YYYY-MM-DD HH:mm:ss')} (${moment.tz.zone(selectedZone).abbr(new Date())})`);

            _.set(obj, 'c_id', c_id);

            if (_.isEmpty(m)) _.set(obj, 'msg', '[Not Applicable]');

            // convert hex to unicode string
            // if (_.eq(cLangType, 'unicode')) {
            //     try {
            //         const msg = jsunicode.decode(m, { encoding: jsunicode.constants.encoding.utf16 });
            //         _.set(obj, 'msg', msg);
            //     } catch (e) {
            //         // ignore the error. continue with original message
            //     }
            // }

            if (!_.isNull(cts)) {
                // convert to acc tz
                const formattedDt = ctsMoment.format('MMM DD, YYYY HH:mm z');
                _.set(obj, 'created_ts', formattedDt);
                _.set(obj, 'created_ts_unix', ctsMoment.unix());
            }
            // convert sched ts to users tz
            const stsMoment = mtz.tz(sts, tz);
            _.set(obj, 'time_from_now', stsMoment.fromNow());
            const formattedDt = stsMoment.format('MMM DD, YYYY HH:mm (z)');
            _.set(obj, 'scheduled_ts', formattedDt);
            _.set(obj, 'scheduled_date', stsMoment.format('YYYY-MM-DD'));
            _.set(obj, 'scheduled_time', stsMoment.format('HH:mm'));
            _.set(obj, 'scheduled_ts_unix', stsMoment.unix());

            // get the count and total for this id
            if (!_.eq(c_type, 'group')) {
                const totals = _.get(obj, 'totals', 0);
                const totalsArr = _.split(totals, ',');
                const filenames = _.get(obj, 'filenames_ori', 0);

                const filenameArr = _.split(filenames, ',');
                const total = _.sum(_.map(totalsArr, (v) => Number(v)));
                const fArr = [];
                for (let i = 0; i < filenameArr.length; i++) {
                    const file = _.get(filenameArr, i, '');
                    const tot = _.get(totalsArr, i, '');
                    fArr.push({ r_filename: file, count: tot });
                }
                _.set(obj, 'files', fArr);
                _.set(obj, 'count', filenameArr.length);
                _.set(obj, 'total', total);
                _.set(obj, 'exclude_group_count', 0);
            } else {
                const groupIdsArr = _.split(_.get(obj, 'group_ids', ''), ',');
                const grps = _.compact(groupIdsArr);
                const egrps = _.compact(_.split(_.get(obj, 'exclude_group_ids', ''), ','));
                _.set(obj, 'exclude_group_count', egrps.length);
                _.set(obj, 'count', grps.length);

                // get group name
                const r1 = await fastify.getGroupInfoForIds(grps);
                const groupBy = _.groupBy(r1, 'id');
                const fArr = [];

                let totalContacts = 0;

                for await (const gid of grps) {
                    const g_name = _.get(_.get(groupBy, gid, [])[0], 'g_name', '');

                    // get the group counts from redis
                    const tot = await fastify.getTotalContacts(cli_id, gid, 'normal');
                    totalContacts += tot;

                    fArr.push({ r_filename: g_name, count: tot });
                }

                _.set(obj, 'files', fArr);
                _.set(obj, 'total', totalContacts);
            }

            const totalHuman = fastify.coolFormat(_.get(obj, 'total', 0), 0);
            _.set(obj, 'total_human', totalHuman);

            return obj;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not get campaign list. Please try again', { code });
            // const e = fastify.httpErrors.createError(500, err);

            return e;
        }
    });

    fastify.post('/csdelete', { preValidation: [], schema: csdeleteSchema }, async (req, reply) => {
        try {
            const { cli_id } = req.user;
            const { c_id, at_id } = req.body;

            // TODO: Check buffer period before deleting a campaign

            console.log('/csdelete incoming params => ', c_id, at_id);

            const result = await fastify.deleteSchedCampaign(cli_id, c_id, at_id);
            console.log('/csdelete resp from db for => ', result, [c_id, at_id]);

            // TODO: need to check the deleted status and reply accordingly { affectedRows: 1, insertId: 0, warningStatus: 0 }

            const resp = { statusCode: 200, message: 'Campaign has been deleted successfully', status: 'success' };

            return resp;
        } catch (err) {
            console.log(err);
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not delete the campaign. Please try again', { code });

            return e;
        }
    });

    fastify.post('/csupdate', { preValidation: [], schema: csupdateSchema }, async (req, reply) => {
        try {
            const { cli_id } = req.user;
            const { c_id, at_id, scheduled_date, scheduled_time, scheduled_zone } = req.body;

            // TODO: Check buffer period before updating a campaign

            console.log('/csupdate incoming params => ', [c_id, at_id, scheduled_date, scheduled_time, scheduled_zone]);

            // convert sched ts to IST
            const incomingTZ = mtz.tz(`${scheduled_date} ${scheduled_time}`, scheduled_zone);

            // convert to ist
            const istTZ = incomingTZ.clone().tz(process.env.IST_ZONE_NAME);
            const istFormatted = istTZ.format('YYYY-MM-DD HH:mm:ss');

            const result = await fastify.updateSchedCampaign(cli_id, c_id, at_id, istFormatted, incomingTZ.toString(), scheduled_zone);

            console.log('/csupdate resp from db for => ', result, [c_id, at_id, scheduled_date, scheduled_time, scheduled_zone]);

            // TODO: need to check the deleted status and reply accordingly { affectedRows: 1, insertId: 0, warningStatus: 0 }

            const resp = { statusCode: 200, message: 'Campaign has been updated successfully', status: 'success' };

            return resp;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not update campaign. Please try again', { code });

            return e;
        }
    });

    fastify.get('/getcookie', async (req, reply) => req.cookies.token);
    fastify.get('/dcookie', async (req, reply) => {
        reply.clearCookie('token');
        return 'cookie deleted';
    });
}

module.exports = campaigns;
