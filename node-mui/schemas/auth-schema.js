const { response400, response500 } = require('./generic');

const loginSchema = {
    tags: ['Auth'],
    body: {
        type: 'object',
        required: ['uname', 'passwd'],
        properties: {
            uname: { type: 'string', minLength: 2, maxLength: 30, example: 'john123' },
            passwd: { type: 'string', example: 'b@r123!' },
        },
    },
};

const tokenSchema = {
    summary: 'Get new access token by sending refresh token',
    tags: ['Auth'],
    body: {
        type: 'object',
        required: ['refresh_token'],
        properties: { refresh_token: { type: 'string', example: 'sdf3r423rweer23', description: 'refresh token' } },
    },
    response: {
       400: response400,
       500: response500,
    },
};

const logoutSchema = {
    summary: 'Api to be called on signout',
    tags: ['Auth'],
    body: {
        type: 'object',
        required: [],
        properties: {},
    },
    response: {
        200: {

            type: 'object',
            properties: {
                statusCode: {
                    type: 'integer',
                    example: '200',
                    description: 'Logout Successful',
                },
                message: { type: 'string', example: 'successful' },
            },
            description: 'Successful response',
        },
        400: response400,
        500: response500,
    },
};

const forgotpasswordSchema = {
    summary: 'Reset password',
    tags: ['Auth'],
    body: {
        type: 'object',
        required: ['email', 'username'],
        properties: {
            email: { type: 'string', format: 'email', example: 'email@email.com', description: 'account email' },
            username: { type: 'string', example: 'username1', description: 'account username' },
        },
    },
    response: {
        200: {

            type: 'object',
            properties: {
                statusCode: {
                    type: 'integer',
                    example: '200/-605',
                    description: 'Successful/ Invalid Email address',
                },
                message: { type: 'string', example: 'successful' },
            },
            description: 'Successful response',
        },
        400: response400,
        500: response500,
    },
};

module.exports = { loginSchema, tokenSchema, logoutSchema, forgotpasswordSchema };
