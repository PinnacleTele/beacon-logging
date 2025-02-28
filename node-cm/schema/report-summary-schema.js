const { response400, response500 } = require('./generic-schema');

const rsummarySchema = {
    summary: 'API to fetch summary counts',
    tags: ['Report Summary'],
    body: {
        type: 'object',
        required: ['cli_id','dateselectiontype', 'source', 'campaign_id', 'senderid', 'reportby'],
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
            senderid: { type: 'string', example: 'HDFCBK', description: 'selected senderid' },
            reportby: {
                type: 'string',
                enum: ['date', 'campaign', 'source', 'senderid', 'overall'],
                example: 'date',
                description: 'summary counts for',
            },
            cli_id: { type: 'string', example: '6000000300000000 or all', description: 'selected senderid' },
            

        },
    },
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    recv_date: { type: 'string', example: '10-Aug-2021', description: 'date' },
                    campaign_name: { type: 'string', example: 'camp name1', description: 'name of the campaign' },
                    campaign_id: {
                        type: 'string',
                        example: 'mGm9xpmtSm9Djeww97mQIMG3rHcSwH2Q7GFc',
                        description: 'the campaign id',
                    },
                    source: { type: 'string', example: 'UI', description: 'source of the traffic' },
                    senderid: { type: 'string', example: 'HDFCBK', description: 'selected senderid' },
                    total: { type: 'integer', example: 23, description: 'total received from client' },
                    mtsuccess: { type: 'integer', example: 23, description: 'total submitted to telco' },
                    dnsuccess: { type: 'integer', example: 23, description: 'total success dn from telco' },
                    dnfailed: { type: 'integer', example: 23, description: 'total failed dn from telco' },
                    mtrejected: { type: 'integer', example: 23, description: 'total rejected on platform' },
                    expired: { type: 'integer', example: 23, description: 'total expired' },
                    dnpending: { type: 'integer', example: 23, description: 'total dn pending from telco' },
                    total_human: {
                        type: 'string',
                        example: '23K',
                        description: 'total received from client in human readable form',
                    },
                    mtsuccess_human: {
                        type: 'string',
                        example: '23K',
                        description: 'total submitted to telco in human readable form',
                    },
                    dnsuccess_human: {
                        type: 'string',
                        example: '23K',
                        description: 'total success dn from telco in human readable form',
                    },
                    dnfailed_human: {
                        type: 'string',
                        example: '23K',
                        description: 'total failed dn from telco in human readable form',
                    },
                    mtrejected_human: {
                        type: 'string',
                        example: '23K',
                        description: 'total rejected on platform in human readable form',
                    },
                    expired_human: {
                        type: 'string',
                        example: '23K',
                        description: 'total expired in human readable form',
                    },
                    dnpending_human: {
                        type: 'string',
                        example: '23K',
                        description: 'total dn pending from telco in human readable form',
                    },
                    dnsuccesspercentage: { type: 'string', example: '23', description: 'in percentage' },
                    mtsuccesspercentage: { type: 'string', example: '23', description: 'in percentage' },
                    dnfailedpercentage: { type: 'string', example: '23', description: 'in percentage' },
                    mtrejectedpercentage: { type: 'string', example: '23', description: 'in percentage' },
                    expiredpercentage: { type: 'string', example: '23', description: 'in percentage' },
                    dnpendingpercentage: { type: 'string', example: '23', description: 'in percentage' },
                    username: { type: 'string', example: 'Jon Doe', description: 'User Name of the cli_id' },
                },
            },

        },
        400: response400,
        500: response500,
    },
};

const rsummarydownloadSchema = {
    summary: 'CSV download for summary counts',
    tags: ['Report Summary'],
    body: {
        type: 'object',
        required: ['cli_id','dateselectiontype', 'source', 'campaign_id', 'senderid', 'reportby'],
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
            senderid: { type: 'string', example: 'HDFCBK', description: 'selected senderid' },
            reportby: {
                type: 'string',
                enum: ['date', 'campaign', 'source', 'senderid', 'overall'],
                example: 'date',
                description: 'summary counts for',
            },
            cli_id: { type: 'string', example: '6000000300000000 or all', description: 'selected senderid' },

        },
    },
    response: {

        400: response400,
        500: response500,
    },

};

module.exports = { rsummarySchema, rsummarydownloadSchema };
