const { response400, response500 } = require('./generic-schema');

const dlttelcosSchema = {
    summary: 'API to get dlt telcos',
    tags: ['DLT'],
    querystring: {},
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    telco: {
                        type: 'string',
                        example: 'Voda',
                        description: 'the value of the telco',
                    },
                    telco_display_name: {
                        type: 'string',
                        example: 'Voda',
                        description: 'to be displayed in UI',
                    },
                    filename: {
                        type: 'string',
                        example: 'BSNL.csv',
                        description: 'sample file',
                    },
                },
            },

            description: 'Successful response',
        },
        400: response400,
        500: response500,
    },
};

const dltaddSchema = {
    summary: 'Add dlt templates',
    tags: ['DLT'],
    body: {
        type: 'object',
        required: ['entityid', 'telco', 'files'],
        properties: {
            entityid: { type: 'string', example: '35234234', description: 'entity id' },
            telco: { type: 'string', example: 'voda', description: 'selected telco' },
            files: {
                type: 'array',
                minItems: 1,
                items: {
                    type: 'object',
                    required: ['filename', 'r_filename', 'count'],
                    properties: {
                        filename: { type: 'string', example: 'file1.xls', description: 'file uploaded by the user' },
                        r_filename: { type: 'string', example: 'file1.xls', description: 'remote file name' },
                        count: { type: 'string', example: '3456', description: 'total count from the upload api' },
                    },
                },
            },
        },
    },
    response: {
        200: {
            type: 'object',
            properties: {
                statusCode: { type: 'integer', example: 200, description: 'Added Successfully' },
                message: { type: 'string', example: 'Successfully added for processing' },
            },
            description: 'Successful',
        },
        400: response400,
        500: response500,
    },
};

const dltstatsSchema = {
    summary: 'Get DLT statistics',
    tags: ['DLT'],
    querystring: {
        type: 'object',
        required: [],
        properties: {},
    },
    response: {
        200: {
            type: 'object',
            properties: {
                total_templates: { type: 'integer', example: 14, description: 'total dlt templates under him' },
                total_senderid: { type: 'integer', example: 12, description: 'total senderid under him' },
                total_entityid: { type: 'integer', example: 2, description: 'total entity under him' },
                total_templates_human: {
                    type: 'integer',
                    example: 1,
                    description: 'total dlt templates in human readable form',
                },
            },
            description: 'Successful response',
        },
        400: response400,
        500: response500,
    },
};

const dlttemplatesSchema = {
    summary: 'Gets list of dlt templates for entityid/templateid .',
    tags: ['DLT'],
    querystring: {
        type: 'object',
        required: ['entity_id', 'dlt_template_id'],
        properties: {
            entity_id: { type: 'string', example: '110100001352', description: 'entity id' },
            dlt_template_id: { type: 'string', example: '467110100001352', description: 'dlt template id' },
        },
    },
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    header: { type: 'string', example: 'hdfcbk', description: 'sender id' },
                    dlt_entity_id: { type: 'string', example: '110100001352' },
                    dlt_template_id: { type: 'string', example: '1107158081212260410' },
                    dlt_template_name: { type: 'string', example: 'dlt template name' },
                    dlt_template_content: { type: 'string', example: 'template message' },
                    pattern_type: {
                        type: 'integer',
                        example: 1,
                        description: 'denotes whether its unicode or not 1-unicode, 0-english',
                    },
                    created_ts: { type: 'string', example: 'Created date & time e.g 30-06-2021 17:44:29 IST' },
                    created_ts_unix: { type: 'integer', example: 1318874398 },
                },
            },
            description: 'Successful response',
        },
        400: response400,
        500: response500,
    },
};

const dltuploadsSchema = {
    summary: 'Get list of dlt uploaded records',
    tags: ['DLT'],
    querystring: {
        type: 'object',
        required: ['dateselectiontype'],
        properties: {
            dateselectiontype: {
                type: 'string',
                enum: ['this week', 'this month'],
                example: 'this week',
                description: 'date selection range',
            },
        },
    },
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    telco: { type: 'string', example: 'Voda', description: 'file from which telco' },
                    dlt_entity_id: { type: 'string', example: '110100001352' },
                    total: { type: 'integer', example: 453 },
                    total_human: { type: 'string', example: '45K' },
                    status: { type: 'string', example: 'completed', description: 'status of the file upload' },
                    created_ts: { type: 'string', example: 'Created date & time e.g 30-06-2021 17:44:29 IST' },
                    created_ts_unix: { type: 'integer', example: 1318874398 },
                },
            },
            description: 'Successful response',
        },
        400: response400,
        500: response500,
    },
};

const dltsenderidsSchema = {
    summary: 'Get list of dlt senderids',
    tags: ['DLT'],
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
                properties: { header: { type: 'string', example: 'HDFCDS', description: 'sender id' } },
            },
            description: 'Successful response',
        },
        400: response400,
        500: response500,
    },
};

const dltentityidsforfilterSchema = {
    summary: 'Get list of dlt entityids for filter',
    tags: ['DLT'],
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
                    entity_id: { type: 'string', example: '4563453', description: 'entity id' },
                    entity_id_id: { type: 'string', example: '34534534', description: 'entity id' },
                },
            },
            description: 'Successful response',
        },
        400: response400,
        500: response500,
    },
};

const dlttemplateidsforfilterSchema = {
    summary: 'Get list of dlt templateids for filter',
    tags: ['DLT'],
    querystring: {
        type: 'object',
        required: ['entity_id'],
        properties: { entity_id: { type: 'string', example: '447463534', description: 'entity id' } },
    },
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    dlt_template_id: { type: 'string', example: '4474635345', description: 'template id' },
                    dlt_template_name: { type: 'string', example: 'templatename', description: 'template name' },
                },
            },
            description: 'Successful response',
        },
        400: response400,
        500: response500,
    },
};

const dltentityidstatsSchema = {
    summary: 'Get list of entityid and the corresponding dlt template count',
    tags: ['DLT'],
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
                    dlt_entity_id: { type: 'string', example: '4242342', description: 'entity id' },
                    total: { type: 'integer', example: 453, description: 'total dlt templates' },
                    total_human: { type: 'string', example: '45K' },
                },
            },
            description: 'Successful response',
        },
        400: response400,
        500: response500,
    },
};

module.exports = {
    dlttelcosSchema,
    dltaddSchema,
    dltstatsSchema,
    dlttemplatesSchema,
    dltuploadsSchema,
    dltsenderidsSchema,
    dltentityidstatsSchema,
    dltentityidsforfilterSchema,
    dlttemplateidsforfilterSchema,
};
