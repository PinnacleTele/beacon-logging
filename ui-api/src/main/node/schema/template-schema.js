// holds the common/global definitions which will be used across the other schemas
const { response400, response500 } = require('./generic-schema');

const templatesSchema = {
  summary: 'Gets list of templates for the user or user+entityid. entity id param is optional.',
  tags: ['Template'],
  querystring: {
    type: 'object',
    required: [],
    properties: { entity_id: { type: 'string', example: '110100001352', description: 'optional field' } },
  },
  response: {
    200: {
      type: 'array',
      items: {
        type: 'object',
        properties: {
          id: { type: 'string', example: '<random unique id>' },
          t_name: { type: 'string', example: 'Template Name' },
          t_type: { type: 'string', example: 'Template Type <column/index>' },
          t_mobile_column: { type: 'string', example: 'Mobile column. value may be a column name or index' },
          dlt_entity_id: { type: 'string', example: '110100001352' },
          dlt_template_id: { type: 'string', example: '1107158081212260410' },
          t_lang_type: { type: 'string', example: 'english/unicode' },
          t_lang: { type: 'string', example: 'Tamil if unicode, else empty' },
          t_content: { type: 'string', example: 'Template message' },
          is_unicode: { type: 'integer', example: '1' },
          created_ts: { type: 'string', example: 'Created date & time e.g 30-06-2021 17:44:29 IST' },
          modified_ts: { type: 'string', example: 'Last modified e.g 30-06-2021 17:44:29 IST' },
          created_ts_unix: { type: 'integer', example: 1318874398 },
          modified_ts_unix: { type: 'integer', example: 1318874398 },
          enabled: { type: 'boolean', example: true },
          info: { type: 'string', example: 'text to be displayed when enabled prop is false e.g. cannot be used' },
        },
      },
      description: 'Successful response',
    },
    400: response400,
    500: response500,
  },
};

const templatesForCampaignSchema = {
  summary: 'Gets list of templates for the user or user+entityid. entity id param is optional.',
  tags: ['Template'],
  querystring: {
    type: 'object',
    required: ['c_type', 'entity_id', 'header'],
    properties: {
      entity_id: { type: 'string', example: '110100001352', description: 'optional field' },
      header: { type: 'string', example: 'HDFCCC', description: 'sender id' },
      c_type: { type: 'string', enum: ['quick', 'otm', 'mtm', 'group', 'template'], example: 'quick', description: 'the type of the campaign' },
    },
  },
  response: {
    200: {
      type: 'array',
      items: {
        type: 'object',
        properties: {
          id: { type: 'string', example: '<random unique id>' },
          t_name: { type: 'string', example: 'Template Name' },
          t_type: { type: 'string', example: 'Template Type <column/index>' },
          t_mobile_column: { type: 'string', example: 'Mobile column. value may be a column name or index' },
          dlt_entity_id: { type: 'string', example: '110100001352' },
          dlt_template_id: { type: 'string', example: '1107158081212260410' },
          t_lang_type: { type: 'string', example: 'english/unicode' },
          t_lang: { type: 'string', example: 'Tamil if unicode, else empty' },
          t_content: { type: 'string', example: 'Template message' },
          is_unicode: { type: 'integer', example: '1' },
          created_ts: { type: 'string', example: 'Created date & time e.g 30-06-2021 17:44:29 IST' },
          modified_ts: { type: 'string', example: 'Last modified e.g 30-06-2021 17:44:29 IST' },
          created_ts_unix: { type: 'integer', example: 1318874398 },
          modified_ts_unix: { type: 'integer', example: 1318874398 },
          enabled: { type: 'boolean', example: true },
          info: { type: 'string', example: 'text to be displayed when enabled prop is false e.g. cannot be used' },
        },
      },
      description: 'Successful response',
    },
    400: response400,
    500: response500,
  },
};

const tinfoSchema = {
  summary: 'Gets template details by id',
  tags: ['Template'],
  querystring: {
    type: 'object',
    required: ['id'],
    properties: {
      id: {
        type: 'string',
        example: '19qlry9xjzlpblxnhea54obd4aqctu0bqrdv',
        description: 'unique identifier for a template',
      },
    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        id: { type: 'string', example: '<random unique id>' },
        t_name: { type: 'string', example: 'Template Name' },
        t_type: { type: 'string', example: 'Template Type <column/index>' },
        t_mobile_column: { type: 'string', example: 'Mobile column. value may be a column name or index' },
        dlt_entity_id: { type: 'string', example: '110100001352' },
        dlt_template_id: { type: 'string', example: '1107158081212260410' },
        t_lang_type: { type: 'string', example: 'english/unicode' },
        t_lang: { type: 'string', example: 'Tamil if unicode, else empty' },
        t_content: { type: 'string', example: 'Template message' },
        is_unicode: { type: 'integer', example: '1' },
        created_ts: { type: 'string', example: 'Created date & time e.g 30-06-2021 17:44:29 IST' },
        modified_ts: { type: 'string', example: 'Last modified e.g 30-06-2021 17:44:29 IST' },
        created_ts_unix: { type: 'integer', example: 1318874398 },
        modified_ts_unix: { type: 'integer', example: 1318874398 },
      },
      description: 'Successful response',
    },
    400: response400,
    500: response500,
  },
};

const dlttemplatesSchema = {
  summary: 'Gets list of dlt templates for the user or user+entityid. entity id param is optional.',
  tags: ['Template'],
  querystring: {
    type: 'object',
    required: [],
    properties: { entity_id: { type: 'string', example: '110100001352', description: 'optional field' } },
  },
  response: {
    200: {
      type: 'array',
      items: {
        type: 'object',
        properties: {
          dlt_entity_id: { type: 'string', example: '110100001352' },
          dlt_template_id: { type: 'string', example: '1107158081212260410' },
          dlt_template_name: { type: 'string', example: 'dlt template name' },
          dlt_template_content: { type: 'string', example: 'template message' },
          pattern_type: { type: 'integer', example: 1 },
          is_static: { type: 'integer', example: 1, description: 'whether the content is static/dynamic 0-dynamic 1-static' },
        },
      },
      description: 'Successful response',
    },
    400: response400,
    500: response500,
  },
};

const dlttemplatesForCampaignSchema = {
  summary: 'Gets list of dlt templates for the campaign.',
  tags: ['Template'],
  querystring: {
    type: 'object',
    required: ['c_type', 'header'],
    properties: {
      header: { type: 'string', example: 'HDFCCC', description: 'sender id' },
      c_type: { type: 'string', enum: ['quick', 'otm', 'mtm', 'group', 'template'], example: 'quick', description: 'the type of the campaign' },
    },
  },
  response: {
    200: {
      type: 'array',
      items: {
        type: 'object',
        properties: {
          dlt_entity_id: { type: 'string', example: '110100001352' },
          dlt_template_id: { type: 'string', example: '1107158081212260410' },
          dlt_template_name: { type: 'string', example: 'dlt template name' },
          dlt_template_content: { type: 'string', example: 'template message' },
          pattern_type: { type: 'integer', example: 1 },
        },
      },
      description: 'Successful response',
    },
    400: response400,
    500: response500,
  },
};

const unuseddlttemplatesSchema = {
  summary: 'Gets list of unused dlt templates for the user+entityid. to be called during create new template',
  tags: ['Template'],
  querystring: {
    type: 'object',
    required: ['entity_id'],
    properties: { entity_id: { type: 'string', example: '110100001352' } },
  },
  response: {
    200: {
      type: 'array',
      items: {
        type: 'object',
        properties: {

          dlt_entity_id: { type: 'string', example: '110100001352' },
          dlt_template_id: { type: 'string', example: '1107158081212260410' },
          dlt_template_name: { type: 'string', example: 'dlt template name' },
          dlt_template_content: { type: 'string', example: 'template message' },
          pattern_type: { type: 'integer', example: 1 },

        },
      },
      description: 'Successful response',
    },
    400: response400,
    500: response500,
  },
};

const tnameuniqueSchema = {
  summary: 'Checks if the template name is unique for this user',
  tags: ['Template'],
  querystring: {
    type: 'object',
    required: ['t_name'],
    properties: { t_name: { type: 'string', description: 'template name' } },
  },
  response: {
    200: {
      type: 'object',
      properties: { isUnique: { type: 'boolean', example: true } },
      description: 'Successful response',
    },
    400: response400,
    500: response500,
  },
};

const tnewSchema = {
  summary: 'Create new Template',
  tags: ['Template'],
  body: {
    type: 'object',
    required: ['t_name', 'is_static', 'dlt_entity_id', 'dlt_template_id', 'pattern_type', 't_content', 'is_unicode'],
    properties: {
      t_name: { type: 'string', example: 'Template1', description: 'template name' },
      t_type: { type: 'string', enum: ['column', 'index', ''], example: 'column', description: 'template type' },
      t_mobile_column: {
        type: 'string',
        example: 'phone or 1',
        description: 'column name/index pos which represents the mobile column in file ',
      },
      dlt_entity_id: { type: 'string', example: '110100001352' },
      dlt_template_id: { type: 'string', example: '1107158081212260410' },
      pattern_type: { type: 'integer', example: 1 },
      is_static: { type: 'integer', example: 1, description: 'denotes whether the content is static/dynamic 1-static, 0-dynamic' },
      t_content: { type: 'string', example: 'Template message from user' },
      is_unicode: { type: 'integer', example: '1', description: '1-unicode 0-plain' },

    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        statusCode: { type: 'integer', example: -410, description: 'Template name not unique' },
        message: { type: 'string', example: 'Template name is not unique' },
      },
      description: 'Creation Un-Successful. Check for statusCode',
    },
    201: {
      type: 'object',
      properties: {
        statusCode: { type: 'integer', example: 201, description: 'Created Successfully' },
        message: { type: 'string', example: 'Template has been created successfully' },
      },
      description: 'Creation Successful',
    },
    400: response400,
    500: response500,

  },
};

const tupdateSchema = {
  summary: 'Update templates details',
  tags: ['Template'],
  body: {
    type: 'object',
    required: ['t_name', 'id'],
    properties: {
      t_name: { type: 'string', example: 'template 1', description: 'template name' },
      id: {
        type: 'string',
        example: '19qlry9xjzlpblxnhea54obd4aqctu0bqrdv',
        description: 'id of the template to be updated',
      },
    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        statusCode: { type: 'integer', example: 200, description: 'Successfully modified' },
        message: { type: 'string', example: 'Template has been successfully updated' },
      },
      description: 'Successful modification',
    },
    400: response400,
    500: response500,
  },
};

const tdeleteSchema = {
  summary: 'Delete template',
  tags: ['Template'],
  body: {
    type: 'object',
    required: ['ids'],
    properties: {
      ids: {
        type: 'array',
        items: {
          type: 'string',
          example: '19qlry9xjzlpblxnhea54obd4aqctu0bqrdv',
          description: 'id of the template to be deleted',
        },
      },
    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        statusCode: { type: 'integer', example: 200, description: 'Successfully deleted' },
        message: { type: 'string', example: 'Template has been successfully deleted' },
      },
      description: 'Successful deletion',
    },
    400: response400,
    500: response500,
  },
};

module.exports = {
  templatesSchema,
  dlttemplatesSchema,
  unuseddlttemplatesSchema,
  tnewSchema,
  tnameuniqueSchema,
  tinfoSchema,
  tupdateSchema,
  tdeleteSchema,
  dlttemplatesForCampaignSchema,
  templatesForCampaignSchema,
};
