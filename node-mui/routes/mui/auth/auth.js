const _ = require('lodash');
const bcryptjs = require('bcryptjs');
const parser = require('ua-parser-js');
const { loginSchema, tokenSchema, logoutSchema, forgotpasswordSchema } = require('../../../schemas/auth-schema');

async function auth(fastify) {
    fastify.post('/login', { schema: loginSchema }, async (req, reply) => {
        const { ACCESS_TOKEN_EXPIRATION, REFRESH_TOKEN_EXPIRATION } = process.env;
        const { uname, passwd } = req.body;
        const result = await fastify.findAdmin(uname);
        const userObj = _.get(result, '0', {});

        if (_.isEmpty(userObj)) {
            const resp = {
                statusCode: fastify.CONSTANTS.INVALID_AUTH,
                message: 'Invalid username or password',
            };
            return reply.send(resp);
        }
        const sessionid = fastify.nanoid();

        // compare the passwords
        const isAuthenticated = bcryptjs.compareSync(passwd, userObj.password);
        if (!isAuthenticated) {
            // send the error response
            const resp = {
                statusCode: fastify.CONSTANTS.INVALID_AUTH,
                message: 'Invalid username or password',
            };
            reply.code(200);
            return reply.send(resp);
        }
        const tokenObj = {
            user: uname,
            userid: userObj.id,
            sessionid,
            id: fastify.nanoid,
        };
        const token = await reply.jwtSign(tokenObj, { expiresIn: `${ACCESS_TOKEN_EXPIRATION}` });

        const refreshTokObj = {
            user: uname,
            userid: userObj.id,
            sessionid,
            id: fastify.nanoid,
        };
        const refreshToken = await reply.jwtSign(refreshTokObj, { expiresIn: `${REFRESH_TOKEN_EXPIRATION}` });

        // set the token and refresh token to the userobj
        _.set(tokenObj, 'token', token);
        _.set(tokenObj, 'refresh_token', refreshToken);

        const ua = _.get(req.headers, 'user-agent');

        const uastr = JSON.stringify(parser(ua));

        await fastify.logSession(userObj.id, userObj.username, 'login', sessionid, uastr, req.ip);

        return reply
            .setCookie('token', token, {
            path: '/',
            secure: false, // send cookie over HTTPS only
            httpOnly: true,
            sameSite: false, // alternative CSRF protection
            })
        .code(200)
        .send(tokenObj);
    });

    fastify.post('/refresh_token', { schema: tokenSchema }, async (req, reply) => {
        const { ACCESS_TOKEN_EXPIRATION } = process.env;
        const { refresh_token } = req.body;

       const decoded = fastify.jwt.decode(refresh_token);
       const { user, sessionid } = decoded;

       if (_.isEmpty(decoded) || Date.now() >= decoded.exp * 1000) {
            const e = fastify.httpErrors.createError(401, 'Invalid Session');
            return e;
        }

        const result = await fastify.findAdmin(user);
        const userObj = _.get(result, '0', {});

        if (_.isEmpty(userObj)) {
            console.log('if');
            const e = fastify.httpErrors.createError(401, 'Invalid User');
            return e;
        }

        const refreshTokObj = {
            userid: userObj.id,
            user,
            sessionid,
            id: fastify.nanoid,

        };
        const refreshToken = await reply.jwtSign(refreshTokObj, { expiresIn: `${ACCESS_TOKEN_EXPIRATION}` });

        return reply.send({ token: refreshToken });
    });

    fastify.post('/logout', { schema: logoutSchema }, async (req, reply) => {
        try {
            req.jwtVerify();
            const { userid, user, sessionid } = req.user;
            const ua = _.get(req.headers, 'user-agent');
            const uastr = JSON.stringify(parser(ua));

            await fastify.logSession(userid, user, 'logout', sessionid, uastr, req.ip);

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

    fastify.post('/forgotpassword', { schema: forgotpasswordSchema }, async (req, reply) => {
        try {
            const { email, username } = req.body;

            // check if the username and email are valid acc email
            const r = await fastify.findAdmin(username);
            const userObj = _.get(r, '0', {});

            const cli_id = _.get(userObj, 'id');
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

            const hashedPass = fastify.hash(password);

            await fastify.updateAccountPassword(cli_id, hashedPass);

            const from_name = process.env.FORGOT_PASS_FROM_NAME;
            const from_email = process.env.FORGOT_PASS_FROM_EMAIL;

            const subject = 'Password Reset - MUI Admin Portal';

            const message = `Dear ${username}, <br><br>You recently requested to reset your password for MUI Admin Portal. Your new password for accessing the portal is <br><br> <div style='font-size: large;color: cadetblue'>${password}</div> <br><br> Regards <br>Support Team`;

            try {
                const respMail = await fastify.sendMail(from_email, from_name, subject, email, username, from_email, message);
                console.log(respMail);
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
