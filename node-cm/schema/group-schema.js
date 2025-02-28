// holds the common/global definitions which will be used across the other schemas
const {
  response400, response500,
} = require('./generic-schema');

const groupsSchema = {
  summary: 'Get list of groups for the user',
  tags: ['Group'],
  querystring: {
    type: 'object',
    required: ['g_type', 'status'],
    properties: {
      g_type: {
        type: 'string',
        enum: ['all', 'normal', 'exclude'],
        example: 'normal',
        description: 'group type all/normal/exclude',
      },
      status: {
        type: 'string', enum: ['all', 'completed'], example: 'all', description: 'status of the group all/completed',
      },
    },
  },
  response: {
    200: {
      type: 'array',
      items: {
        type: 'object',
        properties: {
          id: { type: 'string', example: '<random unique id>' },
          g_name: { type: 'string', example: 'Group Name' },
          g_type: { type: 'string', example: 'Group Type <normal/exclude>' },
          g_visibility: { type: 'string', example: 'Group Visibility <private/shared>' },
          total: { type: 'integer', example: 3546 },
          total_human: { type: 'string', example: '3.5k' },
          is_owner: { type: 'boolean', example: true, description: 'true - if this user created this group' },
          created_ts: { type: 'string', example: 'Created date & time e.g 30-06-2021 17:44:29 IST' },
          modified_ts: { type: 'string', example: 'Last modified e.g 30-06-2021 17:44:29 IST' },
          created_ts_unix: { type: 'integer', example: 1318874398 },
          modified_ts_unix: { type: 'integer', example: 1318874398 },
          status: { type: 'string', example: 'inprocess', description: 'Status of group' },

        },
      },
      description: 'Successful response',
    },
    400: response400,
    500: response500,
  },
};

const groupsForCampaignSchema = {
  summary: 'Get list of groups along with assigned for the user',
  tags: ['Group'],
  querystring: {
    type: 'object',
    required: ['g_type'],
    properties: {
      g_type: {
        type: 'string',
        enum: ['normal', 'exclude'],
        example: 'normal',
        description: 'group type all/normal/exclude',
      },
    },
  },
  response: {
    200: {
      type: 'array',
      items: {
        type: 'object',
        properties: {
          id: { type: 'string', example: '<random unique id>' },
          g_name: { type: 'string', example: 'Group Name' },
          g_type: { type: 'string', example: 'Group Type <normal/exclude>' },
          g_visibility: { type: 'string', example: 'Group Visibility <private/shared>' },
          total: { type: 'integer', example: 3546 },
          total_human: { type: 'string', example: '3.5k' },
          is_owner: { type: 'boolean', example: true, description: 'true - if this user created this group' },
          created_ts: { type: 'string', example: 'Created date & time e.g 30-06-2021 17:44:29 IST' },
          modified_ts: { type: 'string', example: 'Last modified e.g 30-06-2021 17:44:29 IST' },
          created_ts_unix: { type: 'integer', example: 1318874398 },
          modified_ts_unix: { type: 'integer', example: 1318874398 },

        },
      },
      description: 'Successful response',
    },
    400: response400,
    500: response500,
  },
};

const sharedGroupsSchema = {
  summary: 'Get list of shared groups for the user',
  tags: ['Group', 'Account'],
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
          id: { type: 'string', example: '<random unique id>' },
          g_name: { type: 'string', example: 'Group Name' },
          g_type: { type: 'string', example: 'Group Type <normal/exclude>' },
          total: { type: 'integer', example: 3546 },
          total_human: { type: 'string', example: '3.5k' },
          created_ts: { type: 'string', example: 'Created date & time e.g 30-06-2021 17:44:29 IST' },
          modified_ts: { type: 'string', example: 'Last modified e.g 30-06-2021 17:44:29 IST' },
          created_ts_unix: { type: 'integer', example: 1318874398 },
          modified_ts_unix: { type: 'integer', example: 1318874398 },
        },
      },
      description: 'Successful response',
    },
    400: response400,
    500: response500,
  },
};

const ginfoSchema = {
  summary: 'Get group details by id',
  tags: ['Group'],
  querystring: {
    type: 'object',
    required: ['id'],
    properties: {
      id: {
        type: 'string',
        example: 'dnrq2c0th0x2ge5mvlfhbqcv17g1mclsmqj5',
        description: 'unique identifier for a group',
      },
    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        id: { type: 'string', example: '<random unique id>' },
        g_name: { type: 'string', example: 'Group Name' },
        g_id: { type: 'string', example: 'irb3tedg358hjr819zn900bxfc8lnqj2rs35', description: 'group id' },
        g_type: { type: 'string', example: 'normal', description: 'Group Type <normal/exclude>' },
        status: { type: 'string', example: 'Status of group files <queued,completed/failed/inprocess>' },
        g_visibility: { type: 'string', example: 'Group Visibility <private/shared>' },
        total: { type: 'integer', example: 3546 },
        total_human: { type: 'string', example: '3.5k' },
        is_owner: { type: 'boolean', example: true, description: 'true - if this user created this group' },
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

const gnewSchema = {
  summary: 'Create new Group',
  tags: ['Group'],
  body: {
    type: 'object',
    required: ['g_name', 'g_visibility', 'g_type', 'files'],
    properties: {
      g_name: { type: 'string', example: 'Group1', description: 'group name' },
      g_type: {
        type: 'string', enum: ['normal', 'exclude'], example: 'normal', description: 'group type',
      },
      g_visibility: {
        type: 'string', enum: ['private', 'shared'], example: 'private', description: 'group visibility',
      },
      files: {
        type: 'array',
        items: {
          type: 'object',
          required: ['filename', 'r_filename', 'count'],
          properties: {
            filename: { type: 'string', example: 'file1.xls', description: 'file uploaded by the user' },
            r_filename: { type: 'string', example: 'file1.xls', description: 'remote file name' },
            count: { type: 'integer', example: 3456, description: 'total count from the upload api' },
          },
        },
      },

    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        statusCode: { type: 'integer', example: -400, description: 'Group name not unique' },
        message: { type: 'string', example: 'Group name is not unique' },
        status: { type: 'string', example: 'failed', description: 'ok-for positive statusCode failed-for negative statusCode' },
      },
      description: 'Creation Un-Successful. Check for statusCode',
    },
    201: {
      type: 'object',
      properties: {
        statusCode: { type: 'integer', example: 201, description: 'Created Successfully' },
        message: { type: 'string', example: 'Group has been created successfully' },
      },
      description: 'Creation Successful',
    },
    400: response400,
    500: response500,

  },
};

const gnameuniqueSchema = {
  summary: 'Checks if the group name is unique for this user',
  tags: ['Group'],
  querystring: {
    type: 'object',
    required: ['g_name'],
    properties: {
      g_name: { type: 'string', description: 'group name' },
    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        isUnique: { type: 'boolean', example: true },
      },
      description: 'Successful response',
    },
    400: response400,
    500: response500,
  },
};

const gupdateSchema = {
  summary: 'Update group details',
  tags: ['Group'],
  body: {
    type: 'object',
    required: ['g_name', 'g_visibility', 'id'],
    properties: {
      g_name: { type: 'string', example: 'group 1', description: 'group name' },
      id: {
        type: 'string',
        example: '19qlry9xjzlpblxnhea54obd4aqctu0bqrdv',
        description: 'id of the template to be updated',
      },
      g_visibility: {
        type: 'string', enum: ['private', 'shared'], example: 'private', description: 'group visibility',
      },
    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        statusCode: { type: 'integer', example: 200, description: 'Successfully modified' },
        message: { type: 'string', example: 'Group has been successfully updated' },
      },
      description: 'Successful modification',
    },
    400: response400,
    500: response500,
  },
};

const gdeleteSchema = {
  summary: 'Delete group',
  tags: ['Group'],
  body: {
    type: 'object',
    required: ['ids'],
    properties: {
      ids: {
        type: 'array',
        minItems: 1,
        items: {
          type: 'string',
          example: '19qlry9xjzlpblxnhea54obd4aqctu0bqrdv',
          description: 'id of the group(s) to be deleted',
        },
      },
    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        statusCode: { type: 'integer', example: 200, description: 'Successfully deleted' },
        message: { type: 'string', example: 'Group has been successfully deleted' },
      },
      description: 'Successful deletion',
    },
    400: response400,
    500: response500,
  },
};
module.exports = {
  gnewSchema,
  gnameuniqueSchema,
  groupsSchema,
  ginfoSchema,
  gupdateSchema,
  gdeleteSchema,
  sharedGroupsSchema,
  groupsForCampaignSchema,
};
