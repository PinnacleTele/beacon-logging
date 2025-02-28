const _ = require('lodash');
const bcryptjs = require('bcryptjs');
const moment = require('moment');
const mtz = require('moment-timezone');
const parser = require('ua-parser-js');
const fs = require('fs');
const Mustache = require('mustache');

const { loginSchema, logoutSchema, verifypasswordSchema, forgotpasswordSchema,verifyotpSchema, tokenSchema, resendotpSchema} = require('../../../schema/auth-schema');
const { now } = require('lodash');

async function auth(fastify, opts) {
    async function constructTokenObj(userObj, sessionid, ssArr) {

        const zone_abbr = moment.tz.zone(userObj.time_zone).abbr(new Date());

        // get the customer logo based on the SA cli_id
        let logofile = process.env.LOGO_DEFAULT;
        let [result1, user_configs] = await Promise.all([fastify.getLogoPathForId(userObj.su_id), fastify.getUserConfigs(userObj.cli_id)]);

        result1 = _.get(result1, '0', {});
        const two_level_auth = _.get(userObj, 'two_level_auth', 0);
        let expiry_date = _.get(userObj, 'expiry_date', null);
        if(!_.isNull(expiry_date)){
            const expiryMoment = moment.tz(expiry_date, userObj.time_zone);
            expiry_date = expiryMoment.format('YYYY-MM-DD HH:mm:ss');
        }
        

        if (!_.isEmpty(result1)) {
            logofile = _.get(result1, 'logo_relative_path', '');
        }

        // check intl service is enabled
        let intl_enabled = 0;
        if (_.indexOf(ssArr, 'international') >= 0) intl_enabled = 1;

        let auto_gen_cname_enabled = 0;
        let subusers_reports_enabled = 0;
        let fullmsg_enabled = 0;
        let inactive_login = 0;
        if (!_.isEmpty(user_configs)) {
            auto_gen_cname_enabled = _.get(user_configs, 'auto_gen_cname', 0);
            subusers_reports_enabled = _.get(user_configs, 'subusers_reports', 0);
            fullmsg_enabled = _.get(user_configs, 'full_message', 0);
            inactive_login = _.get(user_configs, 'inactive_account_login', 0);
        }        

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
            autogen_cname_yn: auto_gen_cname_enabled,
            logo_file: logofile,
            zone_abbr,
            vl_shortner: userObj.vl_shortner,
            firstname: userObj.firstname,
            lastname: userObj.lastname,
            email: userObj.email,
            subusers_reports_yn: subusers_reports_enabled,
            full_message_yn: fullmsg_enabled,
            inactive_login,
            two_level_auth,
            expiry_date,
            smsrate: userObj.base_sms_rate,
            dltrate : userObj.base_add_fixed_rate
        };
    }

    async function genericMethodToHandle2LevelAuth(userObj, ssArr,req, reply){
    
        const { ACCESS_TOKEN_EXPIRATION, REFRESH_TOKEN_EXPIRATION } = process.env;
        /* const rSS = await fastify.getAssignedSubServices(userObj.cli_id);
        const ssArr = _.map(rSS, (o, i) => o.sub_service); */
        
        const sessionid = fastify.nanoid();
        const tokenObj = await constructTokenObj(userObj, sessionid, ssArr);

        // use the same session id
        const refreshtokenObj = {
            cli_id: userObj.cli_id,
            sessionid,
            id: fastify.nanoid(),
        };

        const refreshtoken = await reply.jwtSign(refreshtokenObj, { expiresIn: `${REFRESH_TOKEN_EXPIRATION}` });
        const token = await reply.jwtSign(tokenObj, { expiresIn: `${ACCESS_TOKEN_EXPIRATION}` });

        _.set(userObj, 'token', token);
        _.set(userObj, 'refresh_token', refreshtoken);
        const ua = _.get(req.headers, 'user-agent');
        const uastr = JSON.stringify(parser(ua));

        const r2 = await fastify.logSession(fastify.nanoid(), userObj.cli_id, userObj.user, 'login', sessionid, uastr, req.ip);

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

        return userObj;
    }

    fastify.post('/login', { schema: loginSchema }, async (req, reply) => {
        
        try {
            const { uname, pass } = req.body;
            // authenticate the user
            const result = await fastify.findUser(uname);
            const userObj = _.get(result, '0', {});
            if (_.isEmpty(userObj)) {
                const resp = {
                    statusCode: fastify.CONSTANTS.INVALID_AUTH,
                    message: 'Invalid username or password',
                };
                reply.code(200);
                return reply.send(resp);
            }

            // Check if the user is active
            let accStatus = _.get(userObj, 'acc_status');
            if (+accStatus === +process.env.ACCOUNT_STATUS_SUSPENDED) {
                // send the error response
                const resp = {
                    statusCode: fastify.CONSTANTS.ACCOUNT_SUSPENDED,
                    message: process.env.ACCOUNT_STATUS_SUSPENDED_MSG,
                };
                reply.code(200);
                return reply.send(resp);
            }

            const expiry_date = _.get(userObj, 'expiry_date', null);
            if (!_.isNull(expiry_date)){
                const expMoment = mtz.tz(expiry_date, process.env.IST_ZONE_NAME);
                const istMoment = mtz.tz(process.env.IST_ZONE_NAME);
                if(istMoment.isAfter(expMoment)){
                    req.log.debug(`/login ${uname} expiry date reached (${expMoment.format('YYYY-MM-DD HH:mm:ss Z')}), updating acc_status to ${process.env.ACCOUNT_STATUS_EXPIRED}(expried) in response`);
                    accStatus = _.toNumber(process.env.ACCOUNT_STATUS_EXPIRED);
                    _.set(userObj, 'acc_status', accStatus);
                }
            }

            if (+accStatus === +process.env.ACCOUNT_STATUS_DEACTIVATED || +accStatus === +process.env.ACCOUNT_STATUS_EXPIRED){
                const user_configs = await fastify.getUserConfigs(userObj.cli_id);
                if (_.isEmpty(user_configs) || user_configs.inactive_account_login === 0) {
                    let msg = process.env.ACCOUNT_STATUS_DEACTIVATED_MSG;
                    let code = fastify.CONSTANTS.ACCOUNT_DEACTIVATED;
                    if(+accStatus === +process.env.ACCOUNT_STATUS_EXPIRED){
                        msg = process.env.ACCOUNT_STATUS_EXPIRED_MSG;
                        code = fastify.CONSTANTS.ACCOUNT_EXPIRED;
                    }
                    const resp = {
                        statusCode: code,
                        message: msg,
                    };
                    reply.code(200);
                    return reply.send(resp);
                }
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

            console.log('User Credentials verified successfully...');
           
            //otp check
           const otpCheckNeeded = _.get(userObj, 'two_level_auth');
           //const otpCheckNeeded = 1;
           const mobile = _.get(userObj, 'mobile');
           
           if (+otpCheckNeeded != 0) {
               const otp = Math.floor(100000 + Math.random() * 900000);
                
                try {
                    const anyOneSuccessfulResp = await Promise.all([fastify.sendOtpEmail(req, otp, userObj),fastify.postPlatformToSMS(req, otp, mobile)]);
                    const sessionId = fastify.nanoid();
                    if(anyOneSuccessfulResp){
                        
                        await fastify.insert2FAforCli(sessionId,userObj.cli_id, userObj.user, otp,"from Login");
                    }
                    reply.code(200);
                    return reply.send({'two_level_auth': '1', 'expiry':process.env.OTP_EXPIRY, 'resend_time':process.env.OTP_RESEND_APPEAR,'sessionid' : sessionId });
                   
               } catch (e) {
                   console.error('/login ERROR while sending OTP ***IGNORING*** ', e);
               }
            }else{
                const finalObj = await genericMethodToHandle2LevelAuth(userObj, ssArr,req, reply)
                return reply.send(finalObj);
            }            
            
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
            const decoded = fastify.jwt.decode(refresh_token);
            if (_.isEmpty(decoded)) {
                const e = fastify.httpErrors.createError(401, 'Invalid Session');
                return e;
            }

            const iat = moment.unix(decoded.iat);
            const exp = moment.unix(decoded.exp);
            const isExpired = moment().isAfter(exp);
            

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

            const fname = _.get(userObj, 'firstname');
            const lname = _.get(userObj, 'lastname');
            const from_name = 'Support';

            try {
                /** ********************************************* Send Email  ******************************************** */
                const from_email = process.env.EMAIL_FROM;
                const to_fname = fname;
                const to_lname = lname;

                const subject = Mustache.render(process.env.FORGOT_PASSWORD_MAIL_SUBJECT, { to_fname, to_lname });

                const emailTemplate = fs.readFileSync('./templates/forgot_password_email_template.html', 'utf8');

                const dataObj = { showLogo: false, from_email, from_name, subject, to_email: email, to_fname, to_lname, reply_to: email, pass: password };

                // const dataObj = await fastify.constructEmailData(from_fname, from_lname, to_name, from_email, to_email, from_email, subject, ui_pass_cust, api_pass_cust, smpp_pass_cust, passwordRequiredList);

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

    fastify.post('/verifyotp', { schema:verifyotpSchema }, async (req, reply) => {
        try {
            const { uname, otp,sessionid } = req.body;
            const result = await fastify.findUser(uname);
            const userObj = _.get(result, '0', {});
            const cli_id = userObj.cli_id;
            
            var m = new Date();
            var dateString = m.getUTCFullYear() +"-"+ ("0" + (m.getUTCMonth()+1)).slice(-2) + "-" +
            ("0" + m.getUTCDate()).slice(-2) + "T" +
            ("0" + m.getUTCHours()).slice(-2) + ":" +
            ("0" + m.getUTCMinutes()).slice(-2) + ":" +
            ("0" + m.getUTCSeconds()).slice(-2) + ".000Z" ;

            const r = await fastify.verifyOtp(cli_id, sessionid);
            const otpEntry = _.get(r, '0', {});
            const otpFromDb = _.get(otpEntry, 'otp');
            const expiryTime = _.get(otpEntry, 'expiry');
            //console.log('otp from db, now',otpFromDb, otp);
           // console.log('dates from db, now',expiryTime, dateString);
            if(otpFromDb == undefined){
                reply.code(200);
                return ( { statusCode: -603, message: 'No valid OTPs.', });
            }
            if(otp == otpFromDb){
                 
                var d1 = Date.parse(expiryTime);
                var d2Now = Date.parse(dateString);
                console.log( d1,d2Now)
                if(d1 < d2Now){
                    
                    await fastify.update2FAOnVerify(req.body.sessionid, cli_id, "Expired", otpFromDb);
                    reply.code(200);
                    return ( {
                        statusCode: -602,
                        message: 'OTP expired',
                    });
                }else{
                    await fastify.update2FAOnVerify(req.body.sessionid, cli_id, "Verified", otpFromDb);
                   
                    const rSS = await fastify.getAssignedSubServices(cli_id);
                    const ssArr = _.map(rSS, (o, i) => o.sub_service);
                    const loginResponseObj =  await genericMethodToHandle2LevelAuth(userObj, ssArr,req, reply);
                    reply.code(200);
                    return reply.send(loginResponseObj)

                }
                
            } else{ 
                await fastify.update2FAOnVerify(req.body.sessionid, cli_id, "Pending", otpFromDb);
                reply.code(200);
                return ( { statusCode: -601, message: 'Invalid OTP', });
            }
        } catch (err) {
            req.log.error(`error =>${err}`);
            return err;
        }
    });

    fastify.post('/resendotp', { schema: resendotpSchema }, async (req, reply) => {
        const {uname,sessionid } = req.body;
        const result = await fastify.findUser(uname);
        const userObj = _.get(result, '0', {});
        const cli_id = userObj.cli_id;

        const otp = Math.floor(100000 + Math.random() * 900000);
       
        try {
            const anyOneSuccessfulResp = await Promise.all([fastify.sendOtpEmail(req, otp, userObj),fastify.postPlatformToSMS(req, otp,userObj.mobile)]);
            if(anyOneSuccessfulResp){
              const resendCnt = await fastify.processRecordsOnResend(sessionid, cli_id, "Invalid", userObj.user, otp);
              
              reply.code(200);
             return reply.send(  {statusCode: 200, message: resendCnt });
            }
           
            
        } catch (e) {
            console.error('/login ERROR while resending OTP ***IGNORING*** ', e);
            return reply.send(  {statusCode: 500, message: "Something went wrong"});
        }
    });

}

module.exports = auth;
