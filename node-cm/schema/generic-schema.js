// holds the common/global definitions which will be used across the other schemas

const response500 = {
  type: 'object',
  description: 'Happens when the server could not process the request.',
  properties: {
    statusCode: { type: 'integer', example: 500 },
    code: { type: 'string', example: '<some randam chars>' },
    error: { type: 'string', example: 'Internal Server Error' },
    message: { type: 'string', example: '<info on the error>' },
  },
};

const response400 = {
  type: 'object',
  description: 'Bad request. Happens if the expected request structure is not met. e.g. Invalid input / Missing parameters etc',
  properties: {
    statusCode: { type: 'integer', example: 400 },
    error: { type: 'string', example: 'Bad Request' },
    message: { type: 'string', example: '<info on the error>' },
  },
};

const response401 = {
  type: 'object',
  description: 'Unauthorized',
  properties: {
    statusCode: { type: 'integer', example: 400 },
    error: { type: 'string', example: 'Unauthorized' },
    message: { type: 'string', example: '<info on the error>' },
  },
};

const response404 = {
  type: 'object',
  description: 'Resource not found',
  properties: {
    statusCode: { type: 'integer', example: 404 },
    error: { type: 'string', example: 'Not Found' },
    message: { type: 'string', example: '<info on the error>' },
  },
};

const response403 = {
  type: 'object',
  description: 'Forbidden',
  properties: {
    statusCode: { type: 'integer', example: 403 },
    error: { type: 'string', example: 'Forbidden Request' },
    message: { type: 'string', example: '<info on the error>' },
  },
};

const entityidsSchema = {
  summary: 'Gets list of entityids for the user',
  tags: ['Quick SMS'],
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
        properties: { entity_id: { type: 'string', example: '<entity id>' } },
      },
      description: 'Successful response',
    },
    400: response400,
    500: response500,
  },
};

const senderidsSchema = {
  summary: 'Gets list of international senderids for the user+entityid  or user+entityid+dlttemplateid',
  tags: ['Campaign'],
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
          header: { type: 'string', example: '<sender id>' },
          entity_id: { type: 'string', example: '<entity id for the senderid>' },
        },
      },
      description: 'Successful response',
    },
    400: response400,
    500: response500,
  },
};

const intlsenderidsSchema = {
  summary: 'Gets list of international senderids for the user',
  tags: ['Campaign'],
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
        properties: { header: { type: 'string', example: '<sender id>' } },
      },
      description: 'Successful response',
    },
    400: response400,
    500: response500,
  },
};

const timezonesSchema = {
  summary: 'Gets all the timezones ',
  tags: ['Utils'],

  response: {
    200: {
      type: 'array',
      items: {
        type: 'object',
        properties: {
          display_name: { type: 'string', example: '(UTC+00:00) Atlantic/St_Helena' },
          short_name: { type: 'string', example: 'GMT' },
          long_name: { type: 'string', example: 'Greenwich Mean Time' },
          zone_name: { type: 'string', example: 'Atlantic/St_Helena' },
          offset: { type: 'string', example: '+01:00' },
        },
      },
      description: 'Successful response',
    },
    400: response400,
    500: response500,
  },
};

const convrateSchema = {
  summary: 'convert rate from one currency to another ',
  tags: ['Utils'],
  querystring: {
    type: 'object',
    required: ['rate'],
    properties: {
      rate: {
        type: 'number',
        example: 0.0034,
        description: 'the rate entered',
      },
    },
  },
  response: {
    200: {
      type: 'object',
      properties: { smsrate: { type: 'number', example: 0.045, description: 'converted rate' } },
    },
    400: response400,
    500: response500,
  },
};

module.exports = { response500, response400, response401, response404, response403, entityidsSchema, senderidsSchema, timezonesSchema, intlsenderidsSchema, convrateSchema };
