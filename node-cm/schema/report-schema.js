const { response400, response500 } = require('./generic-schema');

const rSourceSchema = {
    summary: 'API to populate source filter',
    tags: ['Report'],
    body: {
        type: 'object',
        required: ['dateselectiontype'],
        properties: {
            dateselectiontype: {
                type: 'string',
                enum: ['custom range', 'yesterday', 'today', 'last 7 days', 'last 15 days', 'last 30 days', 'this week', 'this month'],
                example: 'last 7 days',
                description: 'the selected date type',
            },
            fdate: { type: 'string', example: '2021-09-09', description: 'From date in yyyy-MM-dd format' },
            tdate: { type: 'string', example: '2021-09-09', description: 'From date in yyyy-MM-dd format' },

        },
    },
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    display_name: { type: 'string', example: 'UI', description: 'to be displayed on the filter' },
                    intf_grp_type: { type: 'string', example: 'ui', description: 'the value to be binded' },
                },
            },
            400: response400,
            500: response500,
        },
    },
};

const rCampaignsSchema = {
    summary: 'API to populate campaign filter',
    tags: ['Report'],
    body: {
        type: 'object',
        required: ['dateselectiontype', 'source'],
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

        },
    },
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    campaign_name: { type: 'string', example: 'Camp name', description: 'the campaign name' },
                    campaign_id: {
                        type: 'string',
                        example: 'mGm9xpmtSm9Djeww97mQIMG3rHcSwH2Q7GFc',
                        description: 'the campaign id',
                    },
                },
            },
            400: response400,
            500: response500,
        },
    },
};
const rSenderIdsSchema = {
    summary: 'API to populate senderid filter',
    tags: ['Report'],
    body: {
        type: 'object',
        required: ['dateselectiontype', 'source', 'campaign_id'],
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
        },
    },
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    senderid: { type: 'string', example: 'HDFCBK', description: 'senderid' },
                    id: { type: 'string', example: 'HDFCBK', description: 'senderid' }
                },
            },
            400: response400,
            500: response500,
        },
    },
};
module.exports = { rSourceSchema, rCampaignsSchema, rSenderIdsSchema };
