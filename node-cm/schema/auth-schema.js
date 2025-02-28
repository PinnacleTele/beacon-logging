const { response400, response500, response401 } = require('./generic-schema');

const loginSchema = {
    summary: 'API for username and password authentication',
    tags: ['Auth'],
    body: {
        type: 'object',
        required: ['uname', 'pass'],
        properties: {
            uname: { type: 'string', example: 'username' },
            pass: { type: 'string', example: 'password' },
        },
    }
   
};
/**
 *  response: {
        200: {

            type: 'object',
            properties: {
                statusCode: {
                    type: 'integer',
                    example: '200/-602',
                    description: 'Update Successful/ Not enough balance',
                },
                message: { type: 'string', example: 'Wallet amount has been updated successfully' },
                token: { type: 'string', example: '<randam char>', description: 'access token' },
                refresh_token: { type: 'string', example: '<randam char>', description: 'refresh token' },
                mobile: { type: 'string', example: '94466442245', description: 'Mobile' },
                company: { type: 'string', example: 'Inc Ltd', description: 'Company Name' },
                two_level_auth: { type: 'integer', example: 0, description: '0-disabled 1-enabled' },
                smsrate: { type: 'number', example: 1.0234, description: 'SMS Rate. Upto 4 decimal places' },
                dltrate: { type: 'number', example: 0.0025, description: 'DLT Rate. Upto 4 decimal places' },
                first_activation_date: {
                    type: 'string',
                    example: '2021-03-12 11:23:00',
                    description: 'First activation dt of the account. Filled after successful first login+mobile verification',
                },

            },
            description: 'Successful response',
        },
        400: response400,
        500: response500,
    },
 */

const verifypasswordSchema = {
    summary: 'API for username and password authentication',
    tags: ['Auth'],
    body: {
        type: 'object',
        required: ['pass'],
        properties: { pass: { type: 'string', example: 'password' } },
    },
    response: {
        200: {

            type: 'object',
            properties: {
                statusCode: {
                    type: 'integer',
                    example: '200/-603',
                    description: 'Successful/ Invalid Password',
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
            email: { type: 'string', example: 'email@email.com', description: 'account email' },
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

const tokenSchema = {
    summary: 'New access token',
    tags: ['Auth'],
    body: {
        type: 'object',
        required: ['refresh_token'],
        properties: { refresh_token: { type: 'string', example: 'sdf3r423rweer23', description: 'refresh token' } },
    },
    response: {
        // 200: {
        //
        //     type: 'object',
        //     properties: {
        //         statusCode: {
        //             type: 'integer',
        //             example: '200/-605',
        //             description: 'Successful/ Invalid Email address',
        //         },
        //         message: { type: 'string', example: 'successful' },
        //     },
        //     description: 'Successful response',
        // },
        400: response400,
        401: response401,
        500: response500,
    },
};

const logoutSchema = {
    summary: 'New access token',
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

const resendotpSchema = {
    summary: 'User requested to resend the OTP',
    tags: ['Auth'],
    body: {
        required: ['sessionid', 'uname'],
        properties: {
            sessionid: { type: 'string', example: '11jwahohbeu0a6wseaezp5xecoq0mqdy7io9', description: 'session Id' },
            uname: { type: 'string', example: 'username1', description: 'account username' },
        },
    },
    response: {
        200: {

            type: 'object',
            properties: {
                statusCode: {
                    type: 'integer',
                    example: '200',
                    description: 'OTP resent request successful ',
                },
                message: { type: 'string', example: 'successful' },
            },
            description: 'Successful response',
        },
        400: response400,
        500: response500,
    },
};

const verifyotpSchema = {
    summary: 'Verify the OTP for 2FA',
    tags: ['Auth'],
    body: {
        type: 'object',
        required: ['sessionid', 'uname','otp'],
        properties: {
            sessionid: { type: 'string', example: '11jwahohbeu0a6wseaezp5xecoq0mqdy7io9', description: 'session Id' },
            uname: { type: 'string', example: 'username1', description: 'account username' },
            otp: { type: 'number', example: '123456', description: 'A OTP' },
            
        },
    }
    
};

module.exports = { loginSchema, logoutSchema, verifypasswordSchema, forgotpasswordSchema, tokenSchema, verifyotpSchema, resendotpSchema };
