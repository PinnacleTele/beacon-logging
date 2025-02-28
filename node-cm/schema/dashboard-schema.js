const { response400, response500 } = require('./generic-schema');

const dstatsSchema = {
    summary: 'Stats count',
    tags: ['Dashboard'],
    querystring: {
        type: 'object',
        required: [],
        properties: {},
    },
    response: {
        200: {
            type: 'object',
            properties: {
                today: { type: 'integer', example: 4, description: 'Total received today' },
                thisweek: { type: 'integer', example: 1, description: 'Total received this week' },
                thismonth: { type: 'integer', example: 2, description: 'Total received this month' },
                lastmonth: { type: 'integer', example: 1, description: 'Total received last month' },
                today_human: { type: 'string', example: '4', description: 'Total received today in human form' },
                thisweek_human: { type: 'string', example: '1', description: 'Total received this week in human form' },
                thismonth_human: { type: 'string', example: '2', description: 'Total received this month in human form' },
                lastmonth_human: { type: 'string', example: '1', description: 'Total received last month in human form' },

            },
            description: 'Successful response',
        },
        400: response400,
        500: response500,

    },
};

const dhtrendSchema = {
    summary: 'Hourly count',
    tags: ['Dashboard'],
    querystring: {
        type: 'object',
        required: [],
        properties: {},
    },
    response: {
        // 200: {
        //     type: 'object',
        //     properties: {
        //         today: { type: 'integer', example: 4, description: 'Total received today' },
        //         thisweek: { type: 'integer', example: 1, description: 'Total received this week' },
        //         thismonth: { type: 'integer', example: 2, description: 'Total received this month' },
        //         lastmonth: { type: 'integer', example: 1, description: 'Total received last month' },
        //         today_human: { type: 'string', example: '4', description: 'Total received today in human form' },
        //         thisweek_human: { type: 'string', example: '1', description: 'Total received this week in human form' },
        //         thismonth_human: { type: 'string', example: '2', description: 'Total received this month in human form' },
        //         lastmonth_human: { type: 'string', example: '1', description: 'Total received last month in human form' },
        //
        //     },
        //     description: 'Successful response',
        // },
        400: response400,
        500: response500,

    },
};

const dprocessedstatsSchema = {
    summary: 'Processed count',
    tags: ['Dashboard'],
    querystring: {
        type: 'object',
        required: ['dateselectiontype'],
        properties: {
            dateselectiontype: { type: 'string', enum: ['this month', 'today'], example: 'last 7 days', description: 'the selected date type' },
        },
    },
    response: {
        // 200: {
        //     type: 'object',
        //     properties: {
        //         today: { type: 'integer', example: 4, description: 'Total received today' },
        //         thisweek: { type: 'integer', example: 1, description: 'Total received this week' },
        //         thismonth: { type: 'integer', example: 2, description: 'Total received this month' },
        //         lastmonth: { type: 'integer', example: 1, description: 'Total received last month' },
        //         today_human: { type: 'string', example: '4', description: 'Total received today in human form' },
        //         thisweek_human: { type: 'string', example: '1', description: 'Total received this week in human form' },
        //         thismonth_human: { type: 'string', example: '2', description: 'Total received this month in human form' },
        //         lastmonth_human: { type: 'string', example: '1', description: 'Total received last month in human form' },
        //
        //     },
        //     description: 'Successful response',
        // },
        400: response400,
        500: response500,

    },
};

const dtrendSchema = {
    summary: 'Daywise / Monthwise Trend',
    tags: ['Dashboard'],
    querystring: {
        type: 'object',
        required: ['dateselectiontype'],
        properties: {
            dateselectiontype: { type: 'string', enum: ['this month', 'this year'], example: 'last 7 days', description: 'the selected date type' },
        },
    },
    response: {
        // 200: {
        //     type: 'object',
        //     properties: {
        //         today: { type: 'integer', example: 4, description: 'Total received today' },
        //         thisweek: { type: 'integer', example: 1, description: 'Total received this week' },
        //         thismonth: { type: 'integer', example: 2, description: 'Total received this month' },
        //         lastmonth: { type: 'integer', example: 1, description: 'Total received last month' },
        //         today_human: { type: 'string', example: '4', description: 'Total received today in human form' },
        //         thisweek_human: { type: 'string', example: '1', description: 'Total received this week in human form' },
        //         thismonth_human: { type: 'string', example: '2', description: 'Total received this month in human form' },
        //         lastmonth_human: { type: 'string', example: '1', description: 'Total received last month in human form' },
        //
        //     },
        //     description: 'Successful response',
        // },
        400: response400,
        500: response500,

    },
};

module.exports = { dstatsSchema, dhtrendSchema, dprocessedstatsSchema, dtrendSchema };
