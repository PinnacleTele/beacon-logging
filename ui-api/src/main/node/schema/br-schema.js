const { response400, response500, response401 } = require('./generic-schema');

const brusersSchema = {
    summary: 'List users and their billing rate info',
    tags: ['Billing Rate'],
    querystring: {
        type: 'object',
        required: [],
        properties: {},
    },
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    cli_id: { type: 'integer', example: 6000000200000029, description: 'id for this user' },
                    user_type: { type: 'integer', example: '1', description: '0-superadmin 1-admin 2-user' },
                    user: { type: 'string', example: 'user1', description: 'login username' },
                    row_rate: { type: 'number', example: 1.0234, description: 'rest of world rate' },
                    billing_currency: { type: 'string', example: 'INR', description: 'currency assigned for this account' },
                    countries_customrate_total: { type: 'integer', example: 1, description: 'total countries with custom rates in intl + india' },
                    intl_enabled_yn: { type: 'integer', example: 0, description: 'International service. 1-Enabled 0-Disabled' },
                    modified_ts: { type: 'string', example: 'Last modified e.g 30-06-2021 17:44:29 IST' },
                    modified_ts_unix: { type: 'integer', example: 1318874398 },
                },
                description: 'Successful response',
            },
        },
        400: response400,
        500: response500,
    },
};

const brupdateOthersSchema = {
    summary: 'Update billing rates for others',
    tags: ['Billing Rate'],
    body: {
        type: 'object',
        required: ['cli_id', 'add_arr', 'update_arr', 'delete_arr'],
        properties: {
            cli_id: {
                type: 'integer',
                example: 6000000200000021,
                description: 'client id of the account',
            },
            add_arr: {
                type: 'array',
                items: {
                    type: 'object',
                    required: ['country', 'sms_rate'],
                    properties: {
                        country: { type: 'string', example: 'USA', description: 'country name' },
                        sms_rate: { type: 'number', example: 0.023, description: 'the rate' },
                    },
                },
            },
            update_arr: {
                type: 'array',
                items: {
                    type: 'object',
                    required: ['country', 'sms_rate', 'sms_rate_old'],
                    properties: {
                        country: { type: 'string', example: 'USA', description: 'country name' },
                        sms_rate: { type: 'number', example: 0.023, description: 'updated rate' },
                        sms_rate_old: { type: 'number', example: 0.023, description: 'old rate' },
                    },
                },
            },
            delete_arr: {
                type: 'array',
                items: {
                    type: 'string',
                    properties: { country: { type: 'string', example: 'USA', description: 'country name' } },
                },
            },
        },
    },
    response: {
        200: {
            type: 'object',
            properties: {
                statusCode: { type: 'integer', example: 200, description: 'Update Successful' },
                message: { type: 'string', example: 'Other rates has been updated successfully' },
            },
            description: 'Update Successful',
        },
        400: response400,
        500: response500,

    },
};

const brupdateROWSchema = {
    summary: 'Update billing rates for others',
    tags: ['Billing Rate'],
    body: {
        type: 'object',
        required: ['cli_id', 'sms_rate', 'sms_rate_old'],
        properties: {
            cli_id: {
                type: 'integer',
                example: 6000000200000021,
                description: 'client id of the account',
            },
            sms_rate: { type: 'number', example: 0.023, description: 'new rate' },
            sms_rate_old: { type: 'number', example: 0.023, description: 'old rate' },

        },
    },
    response: {
        200: {
            type: 'object',
            properties: {
                statusCode: { type: 'integer', example: 200, description: 'Update Successful' },
                message: { type: 'string', example: 'ROW Rates has been updated successfully' },
            },
            description: 'Update Successful',
        },
        400: response400,
        500: response500,

    },
};

const brusersforfilterSchema = {
    summary: 'List of users for the filter',
    tags: ['Billing Rate'],
    querystring: {
        type: 'object',
        required: [],
        properties: {},
    },
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    cli_id_str: { type: 'string', example: '6000000200000029', description: 'id for this user or all' },
                    username: { type: 'string', example: 'user1', description: 'login username' },
                },
                description: 'Successful response',
            },
        },
        400: response400,
        500: response500,
    },
};

const brchangesSchema = {
    summary: 'List of billing rate changes',
    tags: ['Billing Rate'],
    body: {
        type: 'object',
        required: ['dateselectiontype', 'cli_id_str'],
        properties: {
            dateselectiontype: {
                type: 'string',
                enum: ['custom range', 'yesterday', 'today', 'last 7 days', 'last 15 days', 'last 30 days', 'this week', 'this month'],
                example: 'last 7 days',
                description: 'the selected date type',
            },
            fdate: { type: 'string', example: '2021-09-09', description: 'From date in yyyy-MM-dd format' },
            tdate: { type: 'string', example: '2021-09-09', description: 'From date in yyyy-MM-dd format' },
            cli_id_str: { type: 'string', example: '6000000200000029', description: 'id for this user or all' },
        },
    },
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    username: { type: 'string', example: 'user1', description: 'login username' },
                    country: { type: 'string', example: 'user1', description: 'login username' },
                    new_sms_rate: { type: 'number', example: 0.023, description: 'new sms rate' },
                    old_sms_rate: { type: 'number', example: 0.023, description: 'old sms rate' },
                    new_addl_rate: { type: 'number', example: 0.023, description: 'new addl rate' },
                    old_addl_rate: { type: 'number', example: 0.023, description: 'old addl rate' },
                    is_smsrate_higher_yn: { type: 'integer', example: 1, description: '1- if new sms rate > old sms rate, 0- if new sms rate < old sms rate ' },
                    is_addlrate_higher_yn: { type: 'integer', example: 1, description: '1- if new additional rate > old additional rate, 0- if new additional rate < old additional rate ' },
                    user_type: { type: 'integer', example: '1', description: '0-superadmin 1-admin 2-user' },
                    billing_currency: { type: 'string', example: 'INR', description: 'currency assigned for this account' },
                    modified_ts: { type: 'string', example: 'Last modified e.g 30-06-2021 17:44:29 IST' },
                    modified_ts_unix: { type: 'integer', example: 1318874398 },
                },
                description: 'Successful response',
            },
        },
        400: response400,
        500: response500,
    },

};

module.exports = { brusersSchema, brupdateOthersSchema, brupdateROWSchema, brusersforfilterSchema, brchangesSchema };
