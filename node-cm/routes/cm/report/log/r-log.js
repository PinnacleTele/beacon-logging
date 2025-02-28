/* eslint-disable no-restricted-syntax */
const _ = require('lodash');
const mtz = require('moment-timezone');
const moment = require('moment');
const axios = require('axios');

const { rlogsSchema } = require('../../../../schema/report-log-schema');

/**
 * setup some routes
 * @param {import("fastify").FastifyInstance} fastify
 * @param {*} opts
 */
async function rlog(fastify, opts) {
    fastify.addHook('preValidation', async (req, reply) => {
        console.log('preValidation hook called');
        return req.jwtVerify();
    });

    fastify.post('/rlogs', { preValidation: [], schema: rlogsSchema }, async (req, reply) => {
        try {
            const { source, campaign_id, senderid, status, dateselectiontype, fdate, tdate } = req.body;
            //const { tz, cli_id } = req.user;
            const { tz } = req.user;
            const { LOG_DATA_URL } = process.env;
            let cli_id = req.body.cli_id;
            if(_.isUndefined(cli_id) || _.isNull(cli_id) || _.isEmpty(_.trim(cli_id))){
                cli_id = req.user.cli_id;
            }else{
                cli_id = Number(cli_id);
            }

            console.log('/rlogs incoming params => ', [source, campaign_id, senderid, status, dateselectiontype]);

            const { fromdatetimeStrInIST, todatetimeStrInIST } = await fastify.getFromAndToDate(tz, dateselectiontype, fdate, tdate, true);

            console.log('/rlogs ', fromdatetimeStrInIST, todatetimeStrInIST);

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
            // construct req payload
            const reqPayload = {
                r_param: {
                    recv_date_from: fromdatetimeStrInIST,
                    recv_date_to: todatetimeStrInIST,
                    zone_name: tz,
                    cli_id: [cli_id],
                    columns: 's.cli_id,s.recv_time,s.carrier_sub_time,d.dly_time,s.cli_hdr,s.dest,s.msg,s.sub_status,d.delivery_status,s.sub_cli_sts_desc,d.dn_cli_sts_desc,s.billing_sms_rate,s.billing_add_fixed_rate,d.billing_sms_rate,d.billing_add_fixed_rate, s.file_id, s.intf_grp_type, s.dlt_tmpl_id, s.dlt_entity_id',
                    limit: process.env.MAX_LIMIT_VIEW_LOG,
                    filters: { AND: filters.length === 0 ? undefined : filters },
                },
            };

            console.log('/rlogs req payload => ', JSON.stringify(reqPayload));

            const r = await axios.post(LOG_DATA_URL, reqPayload);
            console.log('/rlogs Resp from api total records => ', _.get(r.data, 'record-count'));

            // process the data
            const payload = [...r.data.records];
            for (const obj of payload) {
                const sub_status = _.get(obj, 'sub_sub_status');
                const dn_status = _.get(obj, 'del_delivery_status');
                const intfgrp = _.get(obj, 'sub_intf_grp_type');
                const ssmsrate = Number(_.get(obj, 'sub_billing_sms_rate', 0));
                const sdltrate = Number(_.get(obj, 'sub_billing_add_fixed_rate', 0));
                const dsmsrate = Number(_.get(obj, 'del_billing_sms_rate', 0));
                const ddltrate = Number(_.get(obj, 'del_billing_add_fixed_rate', 0));

                const smsrate = ssmsrate + dsmsrate;
                const dltrate = sdltrate + ddltrate;
                _.set(obj, 'sms_rate', _.floor(smsrate, 6));
                _.set(obj, 'dlt_rate', _.floor(dltrate, 6));

                if (!_.eq(intfgrp, 'api')) {
                    _.set(obj, 'sub_file_id', '');
                }
                if (_.eq(sub_status, 'Success')) {
                    _.set(obj, 'reason', _.get(obj, 'sub_sub_cli_sts_desc'));
                    _.set(obj, 'status', sub_status);
                    if (_.isEmpty(dn_status)) {
                        _.set(obj, 'status', sub_status);
                    } else if (!_.eq(dn_status, 'Delivered')) {
                        _.set(obj, 'del_dly_time', '');
                        _.set(obj, 'reason', _.get(obj, 'del_dn_cli_sts_desc'));
                        _.set(obj, 'status', dn_status);
                    } else {
                        // dn success
                        _.set(obj, 'reason', _.get(obj, 'del_dn_cli_sts_desc'));
                        _.set(obj, 'status', dn_status);
                    }
                } else {
                    // mt rejected
                    _.set(obj, 'reason', _.get(obj, 'sub_sub_cli_sts_desc'));
                    _.set(obj, 'del_dly_time', '');
                    _.set(obj, 'sub_carrier_sub_time', '');
                    _.set(obj, 'status', sub_status);
                }

                // format the ts
                const recvts = _.get(obj, 'recv_time');
                const subts = _.get(obj, 'sub_carrier_sub_time');
                const delts = _.get(obj, 'del_dly_time');
                if (!_.isEmpty(recvts)) {
                    const rmts = moment(recvts);
                    _.set(obj, 'recv_time', rmts.format('DD-MMM-YYYY HH:mm:ss'));
                    _.set(obj, 'recv_unix', rmts.unix());
                } else {
                    _.set(obj, 'recv_unix', 0);
                }
                if (!_.isEmpty(subts)) {
                    const smts = moment(subts);
                    _.set(obj, 'sub_carrier_sub_time', smts.format('DD-MMM-YYYY HH:mm:ss'));
                    _.set(obj, 'sub_unix', smts.unix());
                } else {
                    _.set(obj, 'sub_unix', 0);
                }
                if (!_.isEmpty(delts)) {
                    const dmts = moment(delts);
                    _.set(obj, 'del_dly_time', dmts.format('DD-MMM-YYYY HH:mm:ss'));
                    _.set(obj, 'del_unix', dmts.unix());
                } else {
                    _.set(obj, 'del_unix', 0);
                }
            }

            reply.send(_.reverse(_.sortBy(payload, 'recv_unix')));
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get detailed log data. Please try again', [err]);

            return e;
        }
    });
}

module.exports = rlog;
