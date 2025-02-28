/* eslint-disable no-restricted-syntax */
const _ = require('lodash');
const mtz = require('moment-timezone');
const moment = require('moment');
const axios = require('axios');

const { rlogsearchSchema } = require('../../../../schema/report-search-schema');

/**
 * setup some routes
 * @param {import("fastify").FastifyInstance} fastify
 * @param {*} opts
 */
async function rsearch(fastify, opts) {
    fastify.addHook('preValidation', async (req, reply) => {
        console.log('preValidation hook called');
        return req.jwtVerify();
    });

    async function processSearch(req) {
        const { input, search_for, dateselectiontype, fdate, tdate, cli_id } = req.body;
        //const { tz, cli_id } = req.user;
        const { tz } = req.user;
        const { LOG_DATA_URL } = process.env;

        console.log('/rlogsearch incoming params => ', [cli_id, input, search_for, dateselectiontype, fdate, tdate]);

        const { fromdatetimeStrInIST, todatetimeStrInIST } = await fastify.getFromAndToDate(tz, dateselectiontype, fdate, tdate, true);

        // construct filters
        const filters = [];
        if (_.eq(search_for, 'mobile')) {
            filters.push({
                field: 's.dest',
                op: '=',
                val: input,
            });
        } else {
            filters.push({
                field: 's.file_id',
                op: '=',
                val: input,
            });
        }

        const cli_ids = [];
        const cli_id_wise_name = {};
        if(_.isUndefined(cli_id) || _.isNull(cli_id) || _.isEmpty(_.trim(cli_id))){
            cli_ids.push(req.user.cli_id);
            cli_id_wise_name[req.user.cli_id] = req.user.user;
        }else if(_.isEqual(_.toLower(_.trim(cli_id)), 'all')){
            // add login user
            cli_ids.push(req.user.cli_id);
            cli_id_wise_name[req.user.cli_id] = req.user.user;
            // add subusers under login user
            const result1 = await fastify.getAllUsersForId(req.user.cli_id);
            for (const row of result1) {
                cli_ids.push(row.cli_id);
                cli_id_wise_name[row.cli_id] = row.user;
            }
        }else{
            cli_ids.push(Number(cli_id));
            const result1 = await fastify.findUserById(cli_id);
            cli_id_wise_name[cli_id] = _.get(result1[0], 'user', '');
        }


        // construct req payload
        const reqPayload = {
            r_param: {
                recv_date_from: fromdatetimeStrInIST,
                recv_date_to: todatetimeStrInIST,
                zone_name: tz,
                cli_id: cli_ids,
                columns: 's.cli_id,s.recv_time,s.carrier_sub_time,d.dly_time,s.cli_hdr,s.dest,s.msg,s.sub_status,d.delivery_status,s.sub_cli_sts_desc,d.dn_cli_sts_desc,s.billing_sms_rate,s.billing_add_fixed_rate,d.billing_sms_rate,d.billing_add_fixed_rate, s.file_id, s.intf_grp_type, s.dlt_tmpl_id, s.dlt_entity_id',
                limit: process.env.MAX_LIMIT_VIEW_LOG,
                filters: { AND: filters.length === 0 ? undefined : filters },
            },
        };

        console.log('/rlogsearch req payload => ', JSON.stringify(reqPayload));

        const r = await axios.post(LOG_DATA_URL, reqPayload);
        console.log('/rlogsearch Resp from api total records => ', _.get(r.data, 'record-count'));

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

            _.set(obj, 'username', _.get(cli_id_wise_name, obj.cli_id, ''));
        }
        return _.reverse(_.sortBy(payload, 'recv_unix'));
    }

    fastify.post('/rlogsearch', { preValidation: [], schema: rlogsearchSchema }, async (req, reply) => {
        try {
            return await processSearch(req);
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Could not get search data. Please try again', { err });

            return e;
        }
    });

    fastify.post('/rlogsearchdownload', { preValidation: [], schema: rlogsearchSchema }, async (req, reply) => {
        try {
            const { input, dateselectiontype } = req.body;

            // construct file headers
            const header = 'Username, Accepted Date,Submitted Date,Delivered Date,Sender Id,Mobile,Status,Message,SMS Rate (INR), DLT Rate (INR), ACK ID, Reason';

            const payload = await processSearch(req);
            console.log(payload);

            reply.type('text/csv;charset=utf-8');
            reply.header('Content-Disposition', `attachment;filename=${input}-${dateselectiontype}.csv`); // prevent ie from opening in app and shows save as dialog instead
            const arr = [];

            arr.push(header);
            for (const o of payload) {
                arr.push(`${o.username},${o.recv_time},${o.sub_carrier_sub_time},${o.del_dly_time},${o.sub_cli_hdr},${o.dest},${o.status},"${_.replace(o.sub_msg, /"/g, '""')}",${o.sms_rate},${o.dlt_rate},${o.sub_file_id},${o.reason}`);
            }
            reply.send(_.join(arr, '\n'));
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not get data for source filter. Please try again', [req.id]);

            return e;
        }
    });
}

module.exports = rsearch;
