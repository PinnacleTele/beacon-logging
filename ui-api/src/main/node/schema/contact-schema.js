// holds the common/global definitions which will be used across the other schemas
const {
  response400, response500,
} = require('./generic-schema');

const contactsSchema = {
  summary: 'Get list of contacts for a group',
  tags: ['Contact'],
  querystring: {
    type: 'object',
    required: ['g_id', 'match', 'g_type'],
    properties: {
      g_id: { type: 'string', description: 'group id', example: 'o361lp84j667kw3vk6mb8okoeo7fj6it6xvi' },
      g_type: {
        type: 'string', enum: ['normal', 'exclude'], description: 'Group Type <normal/exclude>', example: 'normal',
      },
      match: {
        type: 'string',
        minLength: 1,
        description: 'filter text from search field. *-matches everything',
        example: '98407',
      },
    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        total: { type: 'integer', example: 91917, description: 'total contacts' },
        total_human: { type: 'string', example: '91.9K', description: 'total in human readable format' },
        status: { type: 'string', example: 'completed', description: 'status of group' },
        data: {
          type: 'array',
          items: {
            type: 'object',
            properties: {
              mobile: { type: 'string', example: '919177014692', description: 'mobile number' },
              email: { type: 'string', example: 'email@email.com', description: 'email' },
              name: { type: 'string', example: 'John Doe', description: 'Name' },
            },
          },
        },
      },
      description: 'Successful response',
    },
    400: response400,
    500: response500,
  },
};

const cdeleteSchema = {
  summary: 'Delete contact(s)',
  tags: ['Contact'],
  body: {
    type: 'object',
    required: ['mobiles', 'g_id', 'g_type'],
    properties: {
      g_id: { type: 'string', example: 'irb3tedg358hjr819zn900bxfc8lnqj2rs35', description: 'group id' },
      g_type: {
        type: 'string', enum: ['normal', 'exclude'], example: 'normal', description: 'group type',
      },
      mobiles: {
        type: 'array',
        items: {
          type: 'string',
          example: '9145646545',
          description: 'list of mobiles to be deleted',
        },
        minItems: 1,
      },
    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        statusCode: { type: 'integer', example: 200, description: 'Successfully deleted' },
        message: { type: 'string', example: 'Conatcts has been successfully deleted' },
      },
      description: 'Successful deletion',
    },
    400: response400,
    500: response500,
  },
};
const cupdateSchema = {
  summary: 'Update contact details',
  tags: ['Contact'],
  body: {
    type: 'object',
    required: ['mobile', 'email', 'name', 'g_id', 'g_type'],
    properties: {
      name: { type: 'string', example: 'John', description: 'contact name' },
      email: { type: 'string', example: 'john@email.com', description: 'contact email' },
      mobile: { type: 'string', example: '9175465256', description: 'mobile' },
      g_id: { type: 'string', example: 'irb3tedg358hjr819zn900bxfc8lnqj2rs35', description: 'group id' },
      g_type: {
        type: 'string', enum: ['normal', 'exclude'], example: 'normal', description: 'group type',
      },
    },
  },
  response: {
    200: {
      type: 'object',
      properties: {
        statusCode: { type: 'integer', example: 200, description: 'Successfully modified' },
        message: { type: 'string', example: 'Contact has been successfully updated' },
      },
      description: 'Successful modification',
    },
    400: response400,
    500: response500,
  },
};

const caddSchema = {
  summary: 'Add contact details',
  tags: ['Contact'],
  body: {
    type: 'object',
    required: ['mobile', 'email', 'name', 'g_id', 'g_type', 'files'],
    properties: {
      name: { type: 'string', example: 'John', description: 'contact name' },
      email: { type: 'string', example: 'john@email.com', description: 'contact email' },
      mobile: { type: 'string', example: '9175465256', description: 'mobile' },
      g_id: { type: 'string', example: 'irb3tedg358hjr819zn900bxfc8lnqj2rs35', description: 'group id' },
      g_type: {
        type: 'string', enum: ['normal', 'exclude'], example: 'normal', description: 'group type',
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

module.exports = {
  contactsSchema,
  cdeleteSchema,
  cupdateSchema,
  caddSchema,
};
