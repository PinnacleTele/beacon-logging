const { response400, response500 } = require('./generic-schema');

const rLatencyTableSchema = {
    summary: 'API to fetch latency counts',
    tags: ['Report Latency'],
    body: {
        type: 'object',
        required: ['cli_id','dateselectiontype', 'fdate', 'tdate'],
        properties: {
            dateselectiontype: {
                type: 'string',
                enum: ['custom range', 'yesterday', 'today', 'last 7 days', 'last 15 days', 'last 30 days', 'this week', 'this month', 'this year'],
                example: 'last 7 days',
                description: 'the selected date type',
            },
            fdate: { type: 'string', example: '2021-09-09', description: 'From date in yyyy-MM-dd format' },
            tdate: { type: 'string', example: '2021-09-09', description: 'From date in yyyy-MM-dd format' },
            cli_id: { type: 'string', example: '6000000300000000', description: 'user id for whom report is required' }            
        },
    },
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    recv_date: { type: 'string', example: '10-Aug-2021', description: 'date' },
                    username: { type: 'string', example: 'Jon Doe', description: 'User Name of the cli_id' },
                    lat_0_5: { type: 'integer', example: 23, description: '0 - 5 sec latency count' },
                    lat_6_10: { type: 'integer', example: 23, description: '6 - 10 sec latency count' },
                    lat_11_15: { type: 'integer', example: 23, description: '11 - 15 sec latency count' },
                    lat_16_30: { type: 'integer', example: 23, description: '16 - 30 sec latency count' },
                    lat_31_45: { type: 'integer', example: 23, description: '31 - 45 sec latency count' },
                    lat_46_60: { type: 'integer', example: 23, description: '46 - 60 sec latency count' },
                    lat_61_120: { type: 'integer', example: 23, description: '61 - 120 sec latency count' },
                    lat_gt_120: { type: 'integer', example: 23, description: 'gt 120 sec latency count' },
                    total: { type: 'integer', example: 23, description: 'total received from client' }                    
                },
            },
        },
        400: response400,
        500: response500,
    },
};

const rLatencyPieChartSchema = {
    summary: 'API to fetch latency counts',
    tags: ['Report Latency'],
    body: {
        type: 'object',
        required: ['cli_id','dateselectiontype', 'fdate', 'tdate'],
        properties: {
            dateselectiontype: {
                type: 'string',
                enum: ['custom range', 'yesterday', 'today', 'last 7 days', 'last 15 days', 'last 30 days', 'this week', 'this month', 'this year'],
                example: 'last 7 days',
                description: 'the selected date type',
            },
            fdate: { type: 'string', example: '2021-09-09', description: 'From date in yyyy-MM-dd format' },
            tdate: { type: 'string', example: '2021-09-09', description: 'From date in yyyy-MM-dd format' },
            cli_id: { type: 'string', example: '6000000300000000', description: 'user id for whom report is required' }            
        },
    },
    response: {
        200: {
            type: 'object',
            properties: {
                table: {
                    type: 'array',
                    items: {
                        type: 'object',
                        properties: {
                            latency: { type: 'string', example: '0 - 5 sec' },
                            delivered: { type: 'integer', example: 23, description: '0 - 5 sec latency count' },
                            rate: { type: 'number', example: 0.04563, description: 'latency percentage' },
                            color: { type: 'string', example: '#f03e3e' },
                        },
                    },
                },
                data: {
                    type: 'array',
                    items: {
                        type: 'integer',
                        example: 14,
                        description: 'latency count'
                    },
                },
                colors: {
                    type: 'array',
                    items: {
                        type: 'string',
                        example: '#f03e3e',
                        description: 'legend color for latency count'
                    },
                },
                labels: {
                    type: 'array',
                    items: {
                        type: 'string',
                        example: '#f03e3e',
                        description: 'legend color for latency count'
                    },
                },
                delivered: { type: 'integer', example: 23, description: 'total delivered' }                    
            },
        },
        400: response400,
        500: response500,
    },
};

const rLatencyStackChartSchema = {
    summary: 'API to fetch latency counts',
    tags: ['Report Latency'],
    body: {
        type: 'object',
        required: ['cli_id','dateselectiontype', 'fdate', 'tdate'],
        properties: {
            dateselectiontype: {
                type: 'string',
                enum: ['custom range', 'yesterday', 'today', 'last 7 days', 'last 15 days', 'last 30 days', 'this week', 'this month', 'this year'],
                example: 'last 7 days',
                description: 'the selected date type',
            },
            fdate: { type: 'string', example: '2021-09-09', description: 'From date in yyyy-MM-dd format' },
            tdate: { type: 'string', example: '2021-09-09', description: 'From date in yyyy-MM-dd format' },
            cli_id: { type: 'string', example: '6000000300000000', description: 'user id for whom report is required' },
            latency: { type: 'string', example: 'all', description: 'latency for which report is required' }          
        },
    },
    response: {
        200: {
            type: 'object',
            properties: {
                series: {
                    type: 'array',
                    items: {
                        type: 'object',
                        properties: {
                            name: { type: 'string', example: '0 - 5 sec' },
                            data: { 
                                type: 'array', 
                                items: {
                                    type: 'integer',
                                    example: 23, 
                                    description: '0 - 5 sec latency counts' 
                                }                                
                            },
                            color: { type: 'string', example: '#f03e3e' }
                        }
                    }
                },
                label: {
                    type: 'array',
                    items: {
                        type: 'string',
                        example: '4 jan',
                        description: 'date'
                    }
                }                 
            },
        },
        400: response400,
        500: response500,
    },
};

module.exports = { rLatencyTableSchema, rLatencyPieChartSchema, rLatencyStackChartSchema };
