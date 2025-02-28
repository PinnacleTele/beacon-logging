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

  const response403 = {
    type: 'object',
    description: 'Forbidden',
    properties: {
      statusCode: { type: 'integer', example: 403 },
      error: { type: 'string', example: 'Forbidden Request' },
      message: { type: 'string', example: '<info on the error>' },
    },
};

  const timezonesSchema = {
      summary: 'Gets all the timezones ',
      tags: ['Generic'],

      response: {
        200: {
          type: 'array',
          items: {
            type: 'object',
            properties: {
              display_name: { type: 'string', example: '(UTC+05:30) Asia/Calcutta' },
              short_name: { type: 'string', example: 'IST' },
              long_name: { type: 'string', example: 'India Standard Time' },
              zone_name: { type: 'string', example: 'Asia/Calcutta' },
              offset: { type: 'string', example: '+05:30' },
            },
          },
          description: 'Successful response',
        },
        400: response400,
        500: response500,
      },
    };

  const countriesSchema = {
    summary: 'Get all the countries',
    tags: ['Generic'],
    querystring: {},
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    country_code_iso_3: { type: 'string', example: 'IND', description: '3 digit country code' },
                    country: { type: 'string', example: 'India', description: 'Country name' },
                },
                description: 'Successful response',
            },
        },
        400: response400,
        500: response500,
    },
  };

  const currenciesSchema = {
    summary: 'Get all the currencies',
    tags: ['Generic'],
    querystring: {},
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    currency_code: { type: 'string', example: 'IND', description: 'short form' },
                    desc: { type: 'string', example: 'Indian Rupee' },
                },
                description: 'Successful response',
            },
        },
        400: response400,
        500: response500,
    },
  };

  const conversionRatesSchema = {
    summary: 'Get conversion rates for the given quote currency',
    tags: ['Generic'],
    querystring: {
      type: 'object',
      required: ['conv_type'],
      properties: {
        conv_type: { type: 'integer', example: 2, description: 'conversion rates lookup type 1 - Monthly , 2 - Daily' },
        quote_currency: { type: 'string', enum: ['EUR', 'INR'], example: 'EUR', description: 'quote_currency code to find conversion rates. Allowed values EUR,INR' },
      },
  },
    response: {
        200: {
            type: 'array',
            items: {
                type: 'object',
                properties: {
                    base_currency: { type: 'string', example: 'USD', description: 'short form' },
                    quote_currency: { type: 'string', example: 'EUR', description: 'short form' },
                    rate: { type: 'number', example: 0.879395, description: 'conversion rate for given conv_type' },
                },
                description: 'Successful response',
            },
        },
        400: response400,
        500: response500,
    },
  };

  const smppSettingsSchema = {
    summary: 'SMPP default settings',
    tags: ['Generic'],
    querystring: {},
    response: {
        200: {
            type: 'object',
            properties: {
                bind_type: { type: 'string', example: 'TRx', description: 'accounts smpp bind type' },
                max_allowed_connections: { type: 'integer', example: 1, description: 'accounts smpp max allowed connections' },
                throttle_limit: { type: 'integer', example: 0, description: 'accounts smpp throttle limit (TPS)' },
                smpp_charset: { type: 'string', example: 'UTF-8', description: 'Charset for SMPP' },
                dlt_entityid_tag: { type: 'integer', example: 1400, description: 'DLT Entity id tag' },
                dlt_templateid_tag: { type: 'integer', example: 1401, description: 'DLT template id tag' },
                cli_mid_tag: { type: 'integer', example: 1408, description: 'cli_mid_tag' }
            },
            description: 'Successful response',
        },
        400: response400,
        500: response500,
    },
};

  module.exports = { currenciesSchema, countriesSchema, timezonesSchema, response500, response400, response403, conversionRatesSchema, smppSettingsSchema };
