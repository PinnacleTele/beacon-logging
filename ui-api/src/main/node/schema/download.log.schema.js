const { response400, response500, response404 } = require('./generic-schema');

const dlogstatsSchema = {
    summary: 'Get log download statistics',
    tags: ['Download'],
    querystring: {
        type: 'object',
        required: [],
        properties: {},
    },
    response: {
        200: {
            type: 'object',
            properties: {
                total: { type: 'integer', example: 14, description: 'total download for today' },
                inprocess: { type: 'integer', example: 12, description: 'inprocess downloads' },
                completed: { type: 'integer', example: 2, description: 'completed downloads' },
                failed: {
                    type: 'integer',
                    example: 1,
                    description: 'failed downloads',
                },
            },
            description: 'Successful response',
        },
        400: response400,
        500: response500,
    },
};

const logdownloadsSchema = {
    summary: 'Get all the download req',
    tags: ['Download'],
    querystring: {
        type: 'object',
        required: ['dateselectiontype', 'fdate', 'tdate'],
        properties: {
            dateselectiontype: {
                type: 'string',
                enum: ['custom range', 'yesterday', 'today', 'last 7 days', 'last 15 days', 'last 30 days', 'this week', 'this month'],
                example: 'this week',
                description: 'date selection range',
            },
            fdate: { type: 'string', example: '2021-09-09', description: 'From date in yyyy-MM-dd format. in user tz' },
            tdate: { type: 'string', example: '2021-09-09', description: 'From date in yyyy-MM-dd format. in user tz' },
        },
    },
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    id: { type: 'string', example: 'sdfsf23wef454fr', description: 'unique id' },
                    from: { type: 'string', example: '12 Mar 2021', description: 'from date' },
                    to: { type: 'string', example: '12 Mar 2021', description: 'to date' },
                    total: { type: 'integer', example: 453 },
                    total_human: { type: 'string', example: '45K' },
                    filters: { type: 'string', example: '45K' },
                    status: { type: 'string', example: 'completed', description: 'status of the file upload' },
                    created_ts: { type: 'string', example: 'Created date & time e.g 30-06-2021 17:44:29 IST' },
                    created_ts_unix: { type: 'integer', example: 1318874398 },
                    time_from_now: { type: 'string', example: '3 hours ago', description: 'elapsed time from now' },
                },
            },
            description: 'Successful response',
        },
        400: response400,
        500: response500,
    },
};

const downloadlogSchema = {
    summary: 'API to make log download request',
    tags: ['Download'],
    body: {
        type: 'object',
        required: ['dateselectiontype', 'source', 'campaign_id', 'campaign_name', 'senderid', 'status', 'fdate', 'tdate'],
        properties: {
            dateselectiontype: {
                type: 'string',
                enum: ['custom range', 'yesterday', 'today', 'last 7 days', 'last 15 days', 'last 30 days', 'this week', 'this month'],
                example: 'last 7 days',
                description: 'the selected date type',
            },
            fdate: { type: 'string', example: '2021-09-09', description: 'From date in yyyy-MM-dd format' },
            tdate: { type: 'string', example: '2021-09-09', description: 'From date in yyyy-MM-dd format' },
            source: { type: 'string', example: 'gui', description: 'selected source' },
            campaign_id: {
                type: 'string',
                example: 'mGm9xpmtSm9Djeww97mQIMG3rHcSwH2Q7GFc',
                description: 'the campaign id',
            },
            campaign_name: {
                type: 'string',
                example: 'campaign name',
                description: 'the campaign name',
            },
            senderid: { type: 'string', example: 'HDFCBK', description: 'selected senderid' },
            status: {
                type: 'string',
                enum: ['Submitted', 'Rejected', 'Delivered', 'Failed', 'all', 'All'],
                example: 'Submitted',
                description: 'message status',
            },

        },
    },
    response: {

        400: response400,
        500: response500,
    },

};

const downloadlogfileSchema = {
    summary: 'download log file',
    tags: ['Download'],
    querystring: {
        type: 'object',
        required: ['id'],
        properties: { id: { type: 'string', example: 'tert34ewr235233ter34', description: 'reference to file' } },
    },
    response: {

        400: response400,
        404: response404,
        500: response500,
    },
};

module.exports = { dlogstatsSchema, logdownloadsSchema, downloadlogSchema, downloadlogfileSchema };
