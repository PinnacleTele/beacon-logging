const _ = require('lodash');
const bcryptjs = require('bcryptjs');
const moment = require('moment');

const { dstatsSchema, dhtrendSchema, dprocessedstatsSchema, dtrendSchema } = require('../../../schema/dashboard-schema');

async function auth(fastify, opts) {
    fastify.addHook('preValidation', async (req, reply) => {
        console.log('preValidation hook called');
        return req.jwtVerify();
    });

    fastify.get('/dstats', { preValidation: [], schema: dstatsSchema }, async (req, reply) => {
        try {
            const { tz, cli_id } = req.user;
            const payload = {
                today: 0,
                thisweek: 0,
                thismonth: 0,
                lastmonth: 0,
                today_human: 0,
                thisweek_human: 0,
                thismonth_human: 0,
                lastmonth_human: 0,
            };

            // TODO: Elasticsearch for Today
            const { fromdatetimeStrInIST: todayfts, todatetimeStrInIST: todaytts } = await fastify.getFromAndToDate(tz, 'today', null, null, true);
            // NOTE: summary count does not support tz, will be based on local tz
            const { fromdateStrInIST: thisweekfd, todateStrInIST: thisweektd } = await fastify.getFromAndToDate(process.env.IST_ZONE_NAME, 'this week', null, null, true);
            const { fromdateStrInIST: thismonthfd, todateStrInIST: thismonthtd } = await fastify.getFromAndToDate(process.env.IST_ZONE_NAME, 'this month', null, null, true);
            const { fromdateStrInIST: lastmonthfd, todateStrInIST: lastmonthtd } = await fastify.getFromAndToDate(process.env.IST_ZONE_NAME, 'last month', null, null, true);

            console.log('/clist from and to date => ', [todayfts, todaytts], [thisweekfd, thisweektd], [thismonthfd, thismonthtd], [lastmonthfd, lastmonthtd]);

            const [rToday, rThisweek, rThismonth, rLastmonth] = await Promise.all([fastify.getDashStatsForToday(cli_id, todayfts, todaytts), fastify.getDashStats(cli_id, thisweekfd, thisweektd), fastify.getDashStats(cli_id, thismonthfd, thismonthtd), fastify.getDashStats(cli_id, lastmonthfd, lastmonthtd)]);
            const todayCnt = _.get(rToday, 'count', 0);
            const weekCnt = _.get(rThisweek[0], 'total', 0);
            const monthCnt = _.get(rThismonth[0], 'total', 0);
            const lmonthCnt = _.get(rLastmonth[0], 'total', 0);

            _.set(payload, 'today', +todayCnt);
            _.set(payload, 'thisweek', +weekCnt + +todayCnt);
            _.set(payload, 'thismonth', +monthCnt + +todayCnt);
            _.set(payload, 'lastmonth', +lmonthCnt);
            _.set(payload, 'today_human', fastify.coolFormat(+todayCnt, 0));
            _.set(payload, 'thisweek_human', fastify.coolFormat(+weekCnt + +todayCnt, 0));
            _.set(payload, 'thismonth_human', fastify.coolFormat(+monthCnt + +todayCnt, 0));
            _.set(payload, 'lastmonth_human', fastify.coolFormat(+lmonthCnt, 0));

            return payload;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not get dashboard stats. Please try again', { code });
            // const e = fastify.httpErrors.createError(500, err);

            return e;
        }
    });

    fastify.get('/dhtrend', { preValidation: [], schema: dhtrendSchema }, async (req, reply) => {
        try {
            const { tz, cli_id } = req.user;
            const payload = {
                total_submitted: 0,
                total_delivered: 0,
                total_submitted_human: '0',
                total_delivered_human: '0',
                xaxis_label: [],
                dataset: [
                    {
                        name: 'Submitted',
                        displayName: '',
                        type: 'bar',
                        color: '#0049f7',
                        data: [],
                        // data: [10, 20, 30, 40, 45, 60, 60, 55, 45, 50, 13, 84, 35, 81, 77, 41],
                    },
                    {
                        name: 'Delivered',
                        displayName: '',
                        type: 'bar',
                        color: 'rgba(62,11,255,0.09)',
                        data: [],
                        // data: [64, 25, 41, 67, 41, 73, 46, 35, 81, 77, 41, 53, 26, 61, 21, 87],
                    },
                ],
            };

            // timezone is not applicable
            const { fromdatetimeStrInIST: todayfts, todatetimeStrInIST: todaytts } = await fastify.getFromAndToDate(process.env.IST_ZONE_NAME, 'today', null, null, true);

            console.log('/htrend from and to date => ', [todayfts, todaytts]);

            // get hourly count for submission and delivered
            const [r1, r2] = await Promise.all([fastify.getHourlyCount(cli_id, todayfts, todaytts, 'mtsuccess'), fastify.getHourlyCount(cli_id, todayfts, todaytts, 'dnsuccess')]);

            const sBucket = _.get(r1, 'aggregations.hour.buckets', []);
            const dBucket = _.get(r2, 'aggregations.hour.buckets', []);
            const sGroupby = _.groupBy(sBucket, 'key');
            const dGroupby = _.groupBy(dBucket, 'key');
            console.log(sGroupby, dGroupby);

            const curHour = moment().hour();
            for (let i = 0; i <= curHour; i++) {
                payload.xaxis_label.push(i);
                const sobj = _.get(sGroupby, i, {});
                const dobj = _.get(dGroupby, i, {});
                payload.dataset[0].data.push(_.get(sobj[0], 'doc_count', 0));
                payload.dataset[1].data.push(_.get(dobj[0], 'doc_count', 0));
            }
            const totalMTS = _.sum(payload.dataset[0].data);
            const totalDNS = _.sum(payload.dataset[1].data);

            payload.total_submitted = +totalMTS;
            payload.total_delivered = +totalDNS;
            _.set(payload, 'total_submitted_human', fastify.coolFormat(+totalMTS, 0));
            _.set(payload, 'total_delivered_human', fastify.coolFormat(+totalDNS, 0));

            return payload;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not get dashboard stats. Please try again', { code });
            // const e = fastify.httpErrors.createError(500, err);

            return e;
        }
    });

    fastify.get('/dprocessedstats', { preValidation: [], schema: dprocessedstatsSchema }, async (req, reply) => {
        try {
            const { tz, cli_id } = req.user;
            const { dateselectiontype } = req.query;
            let r = {};

            let total = 0;
            let mtsuccess = 0;
            let dnsuccess = 0;
            let dnfailed = 0;
            let mtrejected = 0;
            let dnpending = 0;

            const { fromdatetimeStrInIST: todayfts, todatetimeStrInIST: todaytts } = await fastify.getFromAndToDate(tz, 'today', null, null, true);
            // NOTE: summary count does not support tz, will be based on local tz
            const { fromdateStrInIST: thismonthfd, todateStrInIST: thismonthtd } = await fastify.getFromAndToDate(process.env.IST_ZONE_NAME, dateselectiontype, null, null, true);

            console.log('/dprocessedstats [today] from and to date => ', [todayfts, todaytts]);

            const [rTodayMTSuccess, rTodayMTRejected, rTodayDNSuccess, rTodayDNFailed] = await Promise.all([fastify.getTodaysCountByStatus(cli_id, todayfts, todaytts, 'mtsuccess'), fastify.getTodaysCountByStatus(cli_id, todayfts, todaytts, 'mtrejected'), fastify.getTodaysCountByStatus(cli_id, todayfts, todaytts, 'dnsuccess'), fastify.getTodaysCountByStatus(cli_id, todayfts, todaytts, 'dnfailed')]);
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

            console.log('Todays count => ', { mtsuccess, mtrejected, dnsuccess, dnfailed, dnpending });

            // get the intf_type for the source
            const result1 = await fastify.getInterfaceTypes('all');
            let interfaceArr = [];

            if (result1.length === 0) {
                console.log('/dprocessedstats *** Could not find mapping in interface_type_mapping table for source. setting source as all ');
            }

            interfaceArr = _.compact(_.map(result1, 'intf_type'));

            if (_.eq(dateselectiontype, 'this month')) {
                console.log('/dprocessedstats [this month] from and to date => ', [thismonthfd, thismonthtd]);
                cliAsArr = [];
                cliAsArr.push(cli_id)
                const [rThismonth] = await Promise.all([fastify.getRSOverall(thismonthfd, thismonthtd, interfaceArr, 'all', 'all', cliAsArr)]);
                r = _.get(rThismonth, 0, {});

                total = Number(_.isNull(r.totalrecieved) ? 0 : r.totalrecieved) + ttotal;
                mtsuccess = Number(_.isNull(r.mtsuccess) ? 0 : r.mtsuccess) + tmts;
                dnsuccess = Number(_.isNull(r.dnsuccess) ? 0 : r.dnsuccess) + tdns;
                dnfailed = Number(_.isNull(r.dnfailed) ? 0 : r.dnfailed) + Number(_.isNull(r.expired) ? 0 : r.expired) + tdnf;
                mtrejected = Number(_.isNull(r.mtrejected) ? 0 : r.mtrejected) + tmtr;
                dnpending = Number(_.isNull(r.dnpending) ? 0 : r.dnpending) + tdnpending;

                console.log('This month count =>---------- ', { total, mtsuccess, mtrejected, dnsuccess, dnfailed, dnpending });
            }

            const dnsuccesspercentage = _.round((dnsuccess / mtsuccess) * 100, 2);
            const mtsuccesspercentage = _.round((mtsuccess / total) * 100, 2);
            const dnfailedpercentage = _.round((dnfailed / mtsuccess) * 100, 2);
            const mtrejectedpercentage = _.round((mtrejected / total) * 100, 2);
            const dnpendingpercentage = _.round((dnpending / mtsuccess) * 100, 2);

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

            return obj;
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get dashboard stats. Please try again', { err });
            // const e = fastify.httpErrors.createError(500, err);

            return e;
        }
    });

    /*  api for Daywise Trend / Month-wise trend card */
    fastify.get('/dtrend', { preValidation: [], schema: dtrendSchema }, async (req, reply) => {
        try {
            const { tz, cli_id } = req.user;
            const { dateselectiontype } = req.query;

            console.log('/dtrend incoming params => ', dateselectiontype);

            const payload = {
                total_submitted: 0,
                total_delivered: 0,
                total_submitted_human: '0',
                total_delivered_human: '0',
                xaxis_label: [],
                dataset: [
                    {
                        name: 'Submitted',
                        displayName: '',
                        type: 'bar',
                        color: '#ffe381',
                        data: [],
                    },
                    {
                        name: 'Delivered',
                        displayName: '',
                        type: 'bar',
                        color: '#4338CA',
                        data: [],

                    },
                    {
                        name: 'Delivered %',
                        displayName: '',
                        type: 'line',
                        color: '#54545a',
                        data: [],
                    },
                ],
            };

            // tz not supported
            const { fromdatetimeStrInIST: todayfts, todatetimeStrInIST: todaytts } = await fastify.getFromAndToDate(process.env.IST_ZONE_NAME, 'today', null, null, true);
            const { fromdateStrInIST: thismonthfd, todateStrInIST: thismonthtd } = await fastify.getFromAndToDate(process.env.IST_ZONE_NAME, 'this month', null, null, true);
            const { fromdateStrInIST: thisyearfd, todateStrInIST: thisyeartd } = await fastify.getFromAndToDate(process.env.IST_ZONE_NAME, 'this year', null, null, true);

            // get the intf_type for the source
            const result1 = await fastify.getInterfaceTypes('all');
            const interfaceArr = [];

            if (result1.length === 0) {
                console.log('/dtrend  Could not find mapping in interface_type_mapping table for source. setting source as all ');
            }

            if (_.eq(dateselectiontype, 'this month')) {
                console.log('/dtrend ----[this month] from and to date => ',cli_id, [todayfts, todaytts], [thismonthfd, thismonthtd]);
                cliAsArr = [];
                cliAsArr.push(cli_id);

                const [rTodayMTSuccess, rTodayDNSuccess, rThismonth] = await Promise.all([fastify.getTodaysCountByStatus(cli_id, todayfts, todaytts, 'mtsuccess'), fastify.getTodaysCountByStatus(cli_id, todayfts, todaytts, 'dnsuccess'), fastify.getRSDatewise(thismonthfd, thismonthtd, interfaceArr, 'all', 'all', cliAsArr)]);

                const resultObj = {};
                const tmts = Number(_.get(rTodayMTSuccess, 'count', 0));
                const tdns = Number(_.get(rTodayDNSuccess, 'count', 0));

                // add the todays count
                _.set(resultObj, moment().format('D-MMM'), { mtsuccess: tmts, dnsuccess: tdns });

                _.forEach(rThismonth, (r, index) => {
                    const { recv_date } = r;
                    const dateStr = moment(recv_date).format('D-MMM');
                    resultObj[dateStr] = r;
                });

                const filledDates = await fastify.fillWithDates(thismonthfd, thismonthtd, 'D-MMM');

                _.forEach(filledDates, (date, index) => {
                    // check if the result has data for this date
                    const data = resultObj[date];
                    let dnsuccess = 0;
                    let mtsuccess = 0;
                    let percentage = 0;
                    const dateStr = date;
                    if (!_.isUndefined(data)) {
                        // fill with zero count
                        dnsuccess = Number(_.isNull(data.dnsuccess) ? 0 : data.dnsuccess);
                        mtsuccess = Number(_.isNull(data.mtsuccess) ? 0 : data.mtsuccess);
                        percentage = _.round((dnsuccess / mtsuccess) * 100);
                    }
                    payload.dataset[0].data.push(mtsuccess);
                    payload.dataset[1].data.push(dnsuccess);
                    payload.dataset[2].data.push(percentage);
                });
                payload.xaxis_label = filledDates;

                // set total count
                const totalMTS = _.sum(payload.dataset[0].data);
                const totalDNS = _.sum(payload.dataset[1].data);

                payload.total_submitted = +totalMTS;
                payload.total_delivered = +totalDNS;
                _.set(payload, 'total_submitted_human', fastify.coolFormat(+totalMTS, 0));
                _.set(payload, 'total_delivered_human', fastify.coolFormat(+totalDNS, 0));
            } else {
                console.log('/dtrend [this year] from and to date => ', [todayfts, todaytts], [thisyearfd, thisyeartd]);
                const [rTodayMTSuccess, rTodayDNSuccess, rThisyear] = await Promise.all([fastify.getTodaysCountByStatus(cli_id, todayfts, todaytts, 'mtsuccess'), fastify.getTodaysCountByStatus(cli_id, todayfts, todaytts, 'dnsuccess'), fastify.getRSMonthwise(thisyearfd, thisyeartd, interfaceArr, 'all', 'all', cli_id)]);

                const tmts = Number(_.get(rTodayMTSuccess, 'count', 0));
                const tdns = Number(_.get(rTodayDNSuccess, 'count', 0));
                const curMonthStr = moment().format('MMM');

                const resultObj = {};

                // add the todays count
                _.set(resultObj, curMonthStr, { mtsuccess: tmts, dnsuccess: tdns });

                _.forEach(rThisyear, (r, index) => {
                    const { recv_date_month } = r;
                    const dateStr = moment(recv_date_month).format('MMM');
                    resultObj[dateStr] = r;
                });

                const filledMonths = await fastify.fillWithMonths('MMM');
                console.log(resultObj, filledMonths);

                _.forEach(filledMonths, (month, index) => {
                    // check if the result has data for this month
                    const data = resultObj[month];
                    let dnsuccess = 0;
                    let mtsuccess = 0;
                    let percentage = 0;
                    if (!_.isUndefined(data)) {
                        // fill with count
                        if (_.eq(month, curMonthStr)) {
                            // add todays count as well
                            dnsuccess = Number(_.isNull(data.dnsuccess) ? 0 : data.dnsuccess) + tdns;
                            mtsuccess = Number(_.isNull(data.mtsuccess) ? 0 : data.mtsuccess) + tmts;
                            percentage = Number(_.round((dnsuccess / mtsuccess) * 100));
                        } else {
                            dnsuccess = Number(_.isNull(data.dnsuccess) ? 0 : data.dnsuccess);
                            mtsuccess = Number(_.isNull(data.mtsuccess) ? 0 : data.mtsuccess);
                            percentage = Number(_.round((dnsuccess / mtsuccess) * 100));
                        }
                    }
                    payload.dataset[0].data.push(mtsuccess);
                    payload.dataset[1].data.push(dnsuccess);
                    payload.dataset[2].data.push(percentage);
                });
                payload.xaxis_label = filledMonths;

                // set total count
                const totalMTS = _.sum(payload.dataset[0].data);
                const totalDNS = _.sum(payload.dataset[1].data);

                payload.total_submitted = +totalMTS;
                payload.total_delivered = +totalDNS;
                _.set(payload, 'total_submitted_human', fastify.coolFormat(+totalMTS, 0));
                _.set(payload, 'total_delivered_human', fastify.coolFormat(+totalDNS, 0));
            }

            return payload;
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get dashboard stats. Please try again', { err });
            // const e = fastify.httpErrors.createError(500, err);

            return e;
        }
    });
}

module.exports = auth;
