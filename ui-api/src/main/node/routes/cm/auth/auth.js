const _ = require('lodash');
const bcryptjs = require('bcryptjs');
const moment = require('moment');
const parser = require('ua-parser-js');
const fs = require('fs');
const Mustache = require('mustache');

const { loginSchema, logoutSchema, verifypasswordSchema, forgotpasswordSchema, tokenSchema } = require('../../../schema/auth-schema');

async function auth(fastify, opts) {
    async function constructTokenObj(userObj, sessionid, ssArr) {
        const zone_abbr = moment.tz.zone(userObj.time_zone).abbr(new Date());

        // get the customer logo based on the SA cli_id
        let logofile = process.env.LOGO_DEFAULT;
        let [result1, autogen_cname] = await Promise.all([fastify.getLogoPathForId(userObj.su_id), fastify.hasAutoGenCampName(userObj.cli_id)]);

        result1 = _.get(result1, '0', {});

        if (!_.isEmpty(result1)) {
            logofile = _.get(result1, 'logo_relative_path', '');
        }

        // check intl service is enabled
        let intl_enabled = 0;
        if (_.indexOf(ssArr, 'international') >= 0) intl_enabled = 1;

        return {
            cli_id: userObj.cli_id,
            user: userObj.user,
            user_type: userObj.user_type,
            bill_type: userObj.bill_type,
            cluster: userObj.platform_cluster,
            billing_currency: userObj.billing_currency,
            msg_type: userObj.msg_type,
            tz: userObj.time_zone,
            pu_id: userObj.pu_id,
            su_id: userObj.su_id,
            sessionid,
            id: fastify.nanoid(),
            intl_enabled_yn: intl_enabled,
            autogen_cname_yn: autogen_cname ? 1 : 0,
            logo_file: logofile,
            zone_abbr,
            vl_shortner: userObj.vl_shortner,
            firstname: userObj.firstname,
            lastname: userObj.lastname,
            email: userObj.email,
        };
    }

    fastify.post('/login', { schema: loginSchema }, async (req, reply) => {
        try {
            const { uname, pass } = req.body;
            const { ACCESS_TOKEN_EXPIRATION, REFRESH_TOKEN_EXPIRATION } = process.env;

            // authenticate the user
            const result = await fastify.findUser(uname);
            const userObj = _.get(result, '0', {});
            const code = fastify.nanoid();
            const sessionid = fastify.nanoid();

            if (_.isEmpty(userObj)) {
                const resp = {
                    statusCode: fastify.CONSTANTS.INVALID_AUTH,
                    message: 'Invalid username or password',
                };
                reply.code(200);
                return reply.send(resp);
            }

            // Check if the user is active
            const accStatus = _.get(userObj, 'acc_status');
            if (+accStatus != 0) {
                // send the error response
                const resp = {
                    statusCode: fastify.CONSTANTS.ACCOUNT_DEACTIVATED,
                    message: 'Your account is deactivated',
                };
                reply.code(200);
                return reply.send(resp);
            }

            // compare the passwords
            const isAuthenticated = bcryptjs.compareSync(pass, userObj.ui_pass);
            if (!isAuthenticated) {
                // send the error response
                const resp = {
                    statusCode: fastify.CONSTANTS.INVALID_AUTH,
                    message: 'Invalid username or password',
                };
                reply.code(200);
                return reply.send(resp);
            }

            // check if the user has ui service enabled
            const rSS = await fastify.getAssignedSubServices(userObj.cli_id);
            const ssArr = _.map(rSS, (o, i) => o.sub_service);
            console.log('/login Checking if cm service is enabled for this user...', ssArr);

            if (_.indexOf(ssArr, 'cm') < 0) {
                console.log('/login Checking if cm service is enabled for this user...DISABLED !');
                const resp = {
                    statusCode: fastify.CONSTANTS.INVALID_AUTH,
                    message: 'Access Denied',
                };
                reply.code(200);
                return reply.send(resp);
            }

            console.log('/login Checking if cm service is enabled for this user...ENABLED');

            // TODO: check for user blacklist

            // create the token payload
            console.log('User authenticated successfully...');
            // bill_type => 0-Postpaid, 1-Prepaid
            // user_type => 0-Super User,1-Admin User, 2-User
            // msg_type => 0-Promotional, 1-Tansactional

            const tokenObj = await constructTokenObj(userObj, sessionid, ssArr);

            // use the same session id
            const refreshtokenObj = {
                cli_id: userObj.cli_id,
                sessionid,
                id: fastify.nanoid(),
            };

            // const csrfToken = await reply.generateCsrf();
            // console.log('csrf token => ', csrfToken);
            console.log('access token obj => ', tokenObj);
            // const token = await reply.jwtSign(tokenObj);

            const refreshtoken = await reply.jwtSign(refreshtokenObj, { expiresIn: `${REFRESH_TOKEN_EXPIRATION}` });
            const token = await reply.jwtSign(tokenObj, { expiresIn: `${ACCESS_TOKEN_EXPIRATION}` });

            console.log('Access Token generated => ', token);
            console.log('Refresh Token generated => ', refreshtoken);

            console.log(fastify.jwt.decode(token));

            _.set(userObj, 'token', token);
            _.set(userObj, 'refresh_token', refreshtoken);
            _.set(userObj, 'smsrate', userObj.base_sms_rate);
            _.set(userObj, 'dltrate', userObj.base_add_fixed_rate);

            // log the session
            console.log('/login persisting user session...');
            const ua = _.get(req.headers, 'user-agent');
            const uastr = JSON.stringify(parser(ua));

            const r2 = await fastify.logSession(fastify.nanoid(), userObj.cli_id, userObj.user, 'login', sessionid, uastr, req.ip);

            console.log('/login persisting user session...DONE');
            console.log('/login ips ', req.ips);

            // use cookie for dev env
            if (_.eq(process.env.ENV, 'dev')) {
                return reply
                    .setCookie('token', token, {
                        path: '/',
                        secure: false, // send cookie over HTTPS only
                        httpOnly: true,
                        sameSite: false, // alternative CSRF protection
                    })
                    .code(200)
                    .send(userObj);
            }

            return reply.send(userObj);
        } catch (err) {
            req.log.error(`error =>${err}`);
            return err;
        }
    });

    fastify.post('/logout', {
        preValidation: [fastify.verifyAccessToken],
        schema: logoutSchema,
    }, async (req, reply) => {
        try {
            const { cli_id, user, sessionid } = req.user;
            const ua = _.get(req.headers, 'user-agent');
            const uastr = JSON.stringify(parser(ua));

            console.log('/logout invoked for [cli_id, user, sessionid] =>', [cli_id, user, sessionid]);

            // log the session
            const r = await fastify.logSession(fastify.nanoid(), cli_id, user, 'logout', sessionid, uastr, req.ip);

            console.log('/login persisting user session...DONE');

            const resp = {
                statusCode: 200,
                message: 'Logout Successful',
            };

            reply.clearCookie('token');
            reply.code(200);
            return reply.send(resp);
        } catch (err) {
            req.log.error(`error =>${err}`);
            return err;
        }
    });

    // api to get new access token
    fastify.post('/token', { preValidation: [], schema: tokenSchema }, async (req, reply) => {
        try {
            const { refresh_token } = req.body;
            const { ACCESS_TOKEN_EXPIRATION, REFRESH_TOKEN_EXPIRATION } = process.env;

            const ua = _.get(req.headers, 'user-agent');

            // TODO: check if the user is blacklisted

            // console.log(ua);
            // console.log(refresh_token);
            console.log('/token the agent =>', parser(ua));
            console.log('/token the ip =>', req.ip);

            // const decoded = await fastify.jwt.decode(refresh_token);
            // console.log(decoded);

            // verify if refresh token is still valid
            const decoded = fastify.jwt.decode(refresh_token);
            if (_.isEmpty(decoded)) {
                const e = fastify.httpErrors.createError(401, 'Invalid Session');
                return e;
            }

            console.log('/token checking if the refresh token is expired...');
            const iat = moment.unix(decoded.iat);
            const exp = moment.unix(decoded.exp);
            console.log('/ token the iat and exp of the token is ', [iat.format('YYYY-MM-DD HH:mm:ss'), exp.format('YYYY-MM-DD HH:mm:ss')]);
            const isExpired = moment().isAfter(exp);
            console.log('/token checking if the refresh token is expired...', isExpired ? ' YES' : ' NO');

            if (isExpired) {
                const e = fastify.httpErrors.createError(401, 'Invalid Session');
                return e;
            }

            const { cli_id, sessionid } = decoded;

            // get the user details
            const result = await fastify.findUserById(cli_id);
            const userObj = _.get(result, '0', {});

            if (_.isEmpty(userObj)) {
                const e = fastify.httpErrors.createError(401, 'Invalid User');
                return e;
            }

            const rSS = await fastify.getAssignedSubServices(userObj.cli_id);
            const ssArr = _.map(rSS, (o, i) => o.sub_service);

            const tokenObj = await constructTokenObj(userObj, sessionid, ssArr);

            // use the same session id
            const refreshtokenObj = {
                cli_id: userObj.cli_id,
                sessionid,
                id: fastify.nanoid(),
            };
            // const refreshtoken = await reply.jwtSign(refreshtokenObj, { expiresIn: `${REFRESH_TOKEN_EXPIRATION}` });
            const token = await reply.jwtSign(tokenObj, { expiresIn: `${ACCESS_TOKEN_EXPIRATION}` });

            // TODO: old refresh_token should be invalidated

            return { access_token: token };
        } catch (err) {
            const e = fastify.httpErrors.createError(500, 'Invalid Session', { err });
            return e;
        }
    });

    fastify.post('/verifypassword', {
        preValidation: [fastify.verifyAccessToken],
        schema: verifypasswordSchema,
    }, async (req, reply) => {
        try {
            const { pass } = req.body;
            const { cli_id } = req.user;

            const result = await fastify.findUserById(cli_id);
            const userObj = _.get(result, '0', {});

            if (_.isEmpty(userObj)) {
                return fastify.httpErrors.badRequest('Could not find user information');
            }

            // compare the passwords
            const isSame = bcryptjs.compareSync(pass, userObj.ui_pass);

            if (!isSame) {
                // send the error response
                const resp = {
                    statusCode: fastify.CONSTANTS.INVALID_AUTH,
                    message: 'Invalid password',
                };
                reply.code(200);
                return reply.send(resp);
            }

            console.log('/verifypassword old password matches..');

            reply
                .code(200)
                .send({ statusCode: 200, message: 'success' });
        } catch (err) {
            req.log.error(`error =>${err}`);
            return err;
        }
    });

    fastify.post('/forgotpassword', { preValidation: [], schema: forgotpasswordSchema }, async (req, reply) => {
        try {
            const { email, username } = req.body;
            console.log('/forgotpassword incoming params =>', email, username);

            // check if the username and email are valid acc email
            const r = await fastify.findUser(username);
            const userObj = _.get(r, '0', {});

            const cli_id = _.get(userObj, 'cli_id');
            const accEmail = _.toLower(_.get(userObj, 'email', null));

            if (_.isEmpty(userObj) || !_.eq(accEmail, _.toLower(email))) {
                // send the error response
                const resp = {
                    statusCode: fastify.CONSTANTS.INVALID_EMAIL,
                    message: 'Invalid User / Email Address',
                };
                reply.code(200);
                return reply.send(resp);
            }

            const password = fastify.nanoiduipass();
            console.log('/forgotpassword new reset password for the user => ', cli_id, ' is ', password);

            // hash the password
            const hashedPass = fastify.hash(password);
            console.log('/forgotpassword hashed password => ', hashedPass);

            const result = await fastify.updateAccountPassword(cli_id, hashedPass);

            // TODO: need to check the updated status and reply accordingly { affectedRows: 1, insertId: 0, warningStatus: 0 }
            // TODO: Send Email

            console.log('/forgotpassword resp from db => ', result);

            // update redis
            try {
                const a0 = await fastify.redis.GEN.hset('cli:gui:pass', cli_id, password);
            } catch (e) {
                console.error('/forgotpassword ERROR updating redis for pass ***IGNORING***', e);
            }

            // send email
            // send password thru mail
            const fname = _.get(userObj, 'firstname');
            const lname = _.get(userObj, 'lastname');
            const from_name = 'Support';

            try {
                /** ********************************************* Send Email  ******************************************** */
                console.log('sending mail...');

                const from_email = process.env.EMAIL_FROM;
                const to_fname = fname;
                const to_lname = lname;

                const subject = Mustache.render(process.env.FORGOT_PASSWORD_MAIL_SUBJECT, { to_fname, to_lname });

                const emailTemplate = fs.readFileSync('./templates/forgot_password_email_template.html', 'utf8');

                const dataObj = { showLogo: false, from_email, from_name, subject, to_email: email, to_fname, to_lname, reply_to: email, pass: password };

                // const dataObj = await fastify.constructEmailData(from_fname, from_lname, to_fname, to_lname, from_email, to_email, from_email, subject, ui_pass_cust, api_pass_cust, smpp_pass_cust, passwordRequiredList);

                try {
                    const respMail = await fastify.sendMail(emailTemplate, dataObj);
                    console.log(respMail);
                } catch (e) {
                    console.error('/anew ERROR while sending mail ***IGNORING*** ', e);
                }

                /** ********************************************* End of Send Email  ******************************************** */
                // const respMail = await fastify.sendMail(from_email, from_name, subject, email, to_name, from_email, message);
                // console.log(respMail);
            } catch (e) {
                console.error('/anew ERROR while sending mail ***IGNORING*** ', e);
            }

            const resp = {
                statusCode: 200,
                message: 'Password has been reset successfully. You will receive your new password in your mail.',
            };

            return resp;
        } catch (err) {
            const code = fastify.nanoid();
            req.log.error(`error ${code}  =>${err}`);
            const e = fastify.httpErrors.createError(500, 'Could not reset password. Please try again', { code });

            return e;
        }
    });
}

module.exports = auth;
