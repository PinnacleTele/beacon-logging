const { response400, response500 } = require('./generic-schema');

const rlogsearchSchema = {
    summary: 'API to fetch detailed logs for mobile/ackid',
    tags: ['Report Search'],
    body: {
        type: 'object',
        required: ['dateselectiontype', 'input', 'search_for'],
        properties: {
            dateselectiontype: {
                type: 'string',
                enum: ['custom range', 'yesterday', 'today', 'last 7 days', 'last 15 days', 'last 30 days', 'this week', 'this month'],
                example: 'last 7 days',
                description: 'the selected date type',
            },
            fdate: { type: 'string', example: '2021-09-09', description: 'From date in yyyy-MM-dd format' },
            tdate: { type: 'string', example: '2021-09-09', description: 'From date in yyyy-MM-dd format' },
            input: { type: 'string', example: '997838349', description: 'mobile / ackid' },
            search_for: { type: 'string', example: 'mobile', enum: ['mobile', 'ackid'], description: 'search criteria' },
            cli_id: { type: 'string', example: '7000000200000000', description: 'the client id' },
        },
    },
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    recv_time: { type: 'string', example: '2021-09-26 18:24:20', description: 'time received from client' },
                    del_dly_time: { type: 'string', example: '2021-09-26 18:24:20', description: 'hanset delivery time' },
                    sub_carrier_sub_time: { type: 'string', example: '2021-09-26 18:24:20', description: 'carrier submitted time' },
                    sms_rate: { type: 'string', example: '1.023', description: 'sms rate' },
                    dlt_rate: { type: 'string', example: '0.23', description: 'dlt rate' },
                    cli_id: { type: 'string', example: '7000000200000000', description: 'the client id' },
                    username: { type: 'string', example: 'walletuser1', description: 'the client name' },
                    reason: { type: 'string', example: 'Duplicate Mobile', description: 'the reason for the failure' },
                    dest: { type: 'string', example: '98768227739', description: 'mobile no' },
                    sub_file_id: { type: 'string', example: '533452242344', description: 'ackid for api source' },
                    sub_intf_grp_type: { type: 'string', example: 'ui', description: 'source of the message' },
                    sub_cli_hdr: { type: 'string', example: 'HDFCGG', description: 'sender id' },
                    sub_msg: { type: 'string', example: 'ths is a test', description: 'the message content' },
                    status: { type: 'string', example: 'Rejected', description: 'status of the message delivery' },
                    sub_dlt_tmpl_id: { type: 'string', example: '1107160758270148231', description: 'dlt template id' },
                    sub_dlt_entity_id: { type: 'string', example: '110100001101', description: 'entity id' },
                    recv_unix: { type: 'integer', example: 1318874398, description: 'recieved dt in unix format' },
                    sub_unix: { type: 'integer', example: 1318874398, description: 'submitted to telco dt in unix format' },
                    del_unix: { type: 'integer', example: 1318874398, description: 'delivered dt in unix format' },
                },
            },

        },
        400: response400,
        500: response500,
    },
};

module.exports = { rlogsearchSchema };
