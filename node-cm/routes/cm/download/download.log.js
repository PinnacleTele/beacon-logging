const _ = require('lodash');
const mtz = require('moment-timezone');
const moment = require('moment');
const path = require('path');
const jsunicode = require('jsunicode');
const util = require('util');
const fs = require('fs');
const zlib = require('zlib');
const axios = require('axios');

const sleep = util.promisify(setTimeout);

const { dlogstatsSchema, logdownloadsSchema, downloadlogSchema, downloadlogfileSchema } = require('../../../schema/download.log.schema');

/**
 * setup some routes
 * @param {import("fastify").FastifyInstance} fastify
 * @param {*} opts
 */
async function downloadlog(fastify, opts) {
    fastify.addHook('preValidation', async (req, reply) => {
        console.log('preValidation hook called');
        return req.jwtVerify();
    });

    fastify.get('/dlogstats', { preValidation: [], schema: dlogstatsSchema }, async (req, reply) => {
        try {
            const { cli_id, tz } = req.user;
            const payload = {
                total: 0,
                inprocess: 0,
                completed: 0,
                failed: 0,
            };

            console.log('/dltstats called...');

            const { fromdatetimeStrInIST: todayfts, todatetimeStrInIST: todaytts } = await fastify.getFromAndToDate(tz, 'today', null, null, true);
            const r = await fastify.getLogDownloadCountByStatus(cli_id, todayfts, todaytts);

            console.log('/dltstats resp from db ', r);
            let total = 0;
            for (const obj of r) {
                const status = _.get(obj, 'status', null);
                if (!_.isEmpty(status)) {
                    const cnt = _.get(obj, 'count', 0);
                    total += +cnt;
                    if (_.eq(status, 'inprocess')) _.set(payload, 'inprocess', +cnt);
                    if (_.eq(status, 'completed')) _.set(payload, 'completed', +cnt);
                    if (_.eq(status, 'failed')) _.set(payload, 'failed', +cnt);
                }
            }
            _.set(payload, 'total', total);

            return payload;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);

            return fastify.httpErrors.createError(500, 'Could not get dlt stats. Please try again', { code });
        }
    });

    // get list of download req
    fastify.get('/logdownloads', { schema: logdownloadsSchema }, async (req, reply) => {
        try {
            const { dateselectiontype, fdate, tdate } = req.query;
            const { cli_id, tz } = req.user;
            const respArr = [];

            console.log('/logdownloads incoming params => ', [dateselectiontype, fdate, tdate]);

            const { fromdatetimeStrInIST, todatetimeStrInIST } = await fastify.getFromAndToDate(tz, dateselectiontype, null, null, true);
            console.log('/logdownloads from and to date => ', fromdatetimeStrInIST, todatetimeStrInIST);

            const result = await fastify.getLogDownloads(cli_id, fromdatetimeStrInIST, todatetimeStrInIST);

            console.log('/logdownloads total templates => ', result.length);

            _.forEach(result, (obj, i) => {
                const cts = _.get(obj, 'created_ts', null);
                // TODO: Need to show display telco name
                const from = _.get(obj, 'from_tz', 0);
                const to = _.get(obj, 'to_tz', 0);
                const total = _.get(obj, 'total', 0);
                const totalHuman = fastify.coolFormat(+total, 0);
                let status = _.get(obj, 'status', 0);
                const filters = _.get(obj, 'filters', 0);
                const id = _.get(obj, 'id', 0);
                let time_from_now = '';
                // format filters
                const filersObj = JSON.parse(filters);
                const filterArr = [];
                const fromfilter = _.get(filersObj, 'from');
                const tofilter = _.get(filersObj, 'to');
                const sourcefilter = _.get(filersObj, 'source');
                const cnamefilter = _.get(filersObj, 'campaign_name');
                const senderidfilter = _.get(filersObj, 'senderid');
                const statusfilter = _.get(filersObj, 'status');
                filterArr.push(`From Date: ${moment(fromfilter).format('DD MMM YYYY')}`);
                filterArr.push(` To Date: ${moment(tofilter).format('DD MMM YYYY')}`);
                filterArr.push(` Source: ${_.eq(sourcefilter, 'all') ? _.capitalize(sourcefilter) : sourcefilter}`);
                filterArr.push(` Campaign Name: ${_.eq(cnamefilter, 'all') ? _.capitalize(cnamefilter) : cnamefilter}`);
                filterArr.push(` Sender Id: ${_.eq(senderidfilter, 'all') ? _.capitalize(senderidfilter) : senderidfilter}`);
                filterArr.push(` Status: ${_.eq(statusfilter, 'all') ? _.capitalize(statusfilter) : statusfilter}`);

                const ctsMoment = mtz.tz(cts, tz);
                let formattedDt = null;
                let unixTs = null;

                if (!_.isNull(cts)) {
                    // convert to acc tz
                    formattedDt = ctsMoment.format('MMM DD YYYY HH:mm z');
                    unixTs = ctsMoment.unix();
                    time_from_now = ctsMoment.fromNow();
                }

                // override status if its other than completed/failed/inprocess
                if (!_.eq(status, 'inprocess') && !_.eq(status, 'failed') && !_.eq(status, 'completed')) {
                    status = 'inprocess';
                }

                const rowObj = {
                    id,
                    from: moment(from).format('DD MMM YYYY'),
                    to: moment(to).format('DD MMM YYYY'),
                    filters: _.join(filterArr),
                    total,
                    total_human: totalHuman,
                    status,
                    created_ts: formattedDt,
                    created_ts_unix: unixTs,
                    time_from_now,
                };
                respArr.push(rowObj);
            });

            return respArr;
        } catch (err) {
            return fastify.httpErrors.createError(500, 'Could not get dlt file uploads. Please try again', { err });
        }
    });

    fastify.post('/downloadlog', { schema: downloadlogSchema }, async (req, reply) => {
        try {
            const { dateselectiontype, fdate, tdate, source, campaign_id, campaign_name, senderid, status } = req.body;
            const { tz, user, user_type } = req.user;
            const { LOG_DOWNLOAD_URL } = process.env;
            const respArr = [];
            let cli_id = req.body.cli_id;
            if(_.isUndefined(cli_id) || _.isNull(cli_id) || _.isEmpty(_.trim(cli_id))){
                cli_id = req.user.cli_id;
            }else{
                cli_id = Number(cli_id);
            }

            req.log.debug(`/downloadlog incoming params => ${dateselectiontype}, ${fdate}, ${tdate}, ${source}, ${campaign_id}, ${campaign_name}, ${senderid}, ${status}`);

            const { fromdatetimeStrInIST, todatetimeStrInIST } = await fastify.getFromAndToDate(tz, dateselectiontype, fdate, tdate, true);
            req.log.debug(`/downloadlog from and to date => ${fromdatetimeStrInIST}, ${todatetimeStrInIST}`);

            // construct filters
            const filters = [];
            if (!_.eq(source, 'all')) {
                filters.push({
                    field: 's.intf_grp_type',
                    op: '=',
                    val: source,
                });
            }
            if (!_.eq(senderid, 'all')) {
                filters.push({
                    field: 's.cli_hdr',
                    op: '=',
                    val: senderid,
                });
            }
            if (!_.eq(campaign_id, 'all')) {
                filters.push({
                    field: 's.campaign_id',
                    op: '=',
                    val: campaign_id,
                });
            }
            if (!_.eq(status, 'all')) {
                // status possible values - 'Submitted', 'Delivered', 'Rejected', 'Failed', 'All'
                if (_.eq(status, 'Submitted')) {
                    filters.push({
                        field: 's.sub_status',
                        op: '=',
                        val: 'Success',
                    });
                } else if (_.eq(status, 'Delivered')) {
                    filters.push({
                        field: 'd.delivery_status',
                        op: '=',
                        val: 'Delivered',
                    });
                } else if (_.eq(status, 'Rejected')) {
                    filters.push({
                        field: 's.sub_status',
                        op: 'IN',
                        val: ['Rejected', 'Expired', 'Failed'],
                    });
                } else if (_.eq(status, 'Failed')) {
                    filters.push({
                        field: 'd.delivery_status',
                        op: 'IN',
                        val: ['Expired', 'Failed'],
                    });
                }
            }

            // check if the user has full message enabled
            //const isFullMsgEnabled = await fastify.isFullMessageEnabled(cli_id);
            let full_message = 0;
            let isFullMsgEnabled = false;
            const user_configs = await fastify.getUserConfigs(cli_id);
            req.log.debug(`/downloadlog UserConfigs for cli_id ${cli_id}... ${JSON.stringify(user_configs)}`);
            if (!_.isEmpty(user_configs)) {
                full_message = _.get(user_configs, 'full_message', 0);
                if(full_message === 1){
                    isFullMsgEnabled = true;
                }
            }
            req.log.debug(`/downloadlog is full msg enabled for cli_id ${cli_id}... ${isFullMsgEnabled ? 'YES' : 'NO'}`);

            // TODO: check max simultaneous downloads
            const reqPayload = {
                r_param: {
                    recv_date_from: fromdatetimeStrInIST,
                    recv_date_to: todatetimeStrInIST,
                    zone_name: tz,
                    cli_id: [cli_id],
                    full_message: isFullMsgEnabled,
                    filters: { AND: filters.length === 0 ? undefined : filters },
                },
                r_app_version: 'v1.1.1',
                r_username: 'guest',
                r_app: 'dashboard',
            };
            req.log.debug(`Request Payload =>  ${JSON.stringify(reqPayload)}`);

            const r = await axios.post(LOG_DOWNLOAD_URL, reqPayload);
            req.log.debug(`Resp from api =>  ${(!_.isUndefined(r.data) && !_.isNull(r.data))?JSON.stringify(r.data):r}`);
            const reqId = _.get(r.data, 'queue_id', 0);

            // construct filters
            const selectedfilters = { from: fdate, to: tdate, source, campaign_id, campaign_name, senderid, status };
            // insert the req to table
            const result = await fastify.persistDownloadReq(fastify.nanoid(), cli_id, fromdatetimeStrInIST, todatetimeStrInIST, source, campaign_id, campaign_name, senderid, status, user, user_type, fdate, tdate, tz, selectedfilters, reqId);

            const resp = { statusCode: 200, message: 'Download request is sent for processing. You can check the status of your request in download center' };
            return reply.send(resp);
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);

            return fastify.httpErrors.createError(500, 'Could not persist download req. Please try again', { err });
        }
    });

    fastify.get('/downloadlogfile', { schema: downloadlogfileSchema }, async (req, reply) => {
        try {
            const { id } = req.query;
            // const { cli_id } = req.user;

            console.log('/downloadlog incoming params => ', [id]);

            // get file info
            const result = await fastify.getLogDownloadFileInfo(id);
            const obj = _.get(result, 0, {});
            console.log('/downloadlog resp from db =>', obj);

            if (_.isEmpty(obj)) return fastify.httpErrors.badRequest('Could not find file information');

            if (!_.eq(obj.status, 'completed')) return fastify.httpErrors.badRequest('Invalid download request');

            const downloadpath = obj.download_xl_path;
            console.log(downloadpath);

            // check if the file exists
            try {
                if (!fs.existsSync(downloadpath)) {
                    return fastify.httpErrors.notFound('Could not download file');
                }
            } catch (err) {
                console.error(err);
            }

            const filename = `log_${moment(obj.from_tz).format('YYYY-MM-DD')}-${moment(obj.to_tz).format('YYYY-MM-DD')}_${id}.zip`;
            // const readstream = fs.createReadStream('./package.json', 'utf8');
            const readstream = fs.createReadStream(downloadpath);

            reply.type('application/zip');
            reply.header('Content-Disposition', `attachment;filename=${filename}`);
            reply.send(readstream);
        } catch (err) {
            return fastify.httpErrors.createError(500, 'Could not persist download req. Please try again', { err });
        }
    });
}

module.exports = downloadlog;
