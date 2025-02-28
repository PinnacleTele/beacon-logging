const fp = require('fastify-plugin');
const fjwt = require('fastify-jwt');
const _ = require('lodash');
const Mustache = require('mustache');
const fs = require('fs');
const axios = require('axios');

const auth = async (fastify, opts) => {
  fastify.addHook('preHandler', async (req, reply) => {
    console.log('hook from authenticate.js called');

  });

  fastify.decorate('verifyAccessToken', async (req, reply) => {
    try {
      // const a = await req.jwtVerify();
      console.log('verifyAccessToken');
      return req.jwtVerify();
    } catch (err) {
      return err;
    }
  });

  fastify.decorate('verifyOtp', async (cli_id,sessionid) => {
    try {
      const params =  [cli_id,sessionid,"Pending"];
      const otlSql = `SELECT t2.otp, t2.expiry FROM cm.two_factor_authentication t2 WHERE t2.id = (SELECT max(id) FROM cm.two_factor_authentication t2 WHERE t2.cli_id = ? and t2.sessionid = ? and t2.status= ?)`;
      console.log(otlSql,params)
      const result = await fastify.mariadb.query(otlSql,params);
      
      return result;
    } catch (err) {
      return err;
    }
  });

  fastify.decorate('sendOtpEmail', async (req,otp,userObj) => {
    to_fname = userObj.firstname;
    to_lname = userObj.lastname;
    to_email = userObj.email;
    console.log("§§§",to_email)
    
    const subject = Mustache.render(process.env.OTP_MAIL_SUBJECT, { to_fname, to_lname });
    const emailTemplate = fs.readFileSync('./templates/otp_email_template.html', 'utf8');
    const from_name="System";
    const from_email=process.env.EMAIL_FROM;
    const expiry = process.env.OTP_EXPIRY;
    const dataObj = {from_name, to_fname, to_lname, from_email, to_email,  subject, otp, expiry};
    try {
        const respMail = await fastify.sendMailOTP(emailTemplate, dataObj);
        req.log.debug(respMail, otp);
        
    } catch (e) {
        req.log.error(respMail, otp);
        console.log('/sendOtpEmail ERROR while sending mail ', e);
    }
  });


  fastify.decorate('postPlatformToSMS', async (req,otp,mobile) => {
    version = process.env.VERSION;
    accesskey = process.env.ACCESSKEY;
    header = process.env.OTP_HEADER;
    type = process.env.OTP_TYPE;
    dlr_req = process.env.DLR_REQ;
    dlt_entity_id = process.env.DLT_ENTITY_ID ;
    dlt_template_id = process.env.DLT_TEMPLATE_ID;
    msg = process.env.OTP_MSG;
    msg = msg.replace('ACTUALOTP',otp);
        
    try {
      
      const resp = await axios.get(`${process.env.PLATFORMURL}?version=${version}&accesskey=${accesskey}&dest=${mobile}&header=${header}&type=${type}&dlr_req=${dlr_req}&dlt_entity_id=${dlt_entity_id}&dlt_template_id=${dlt_template_id}&msg=${msg}`);
      req.log.debug(resp.data)
      return resp.data
      
    } catch (err) {
      console.error("Error in sending OTP to Platform ", err);
    }
  });

  fastify.decorate('insert2FAforCli', async (sessionid,cli_id,user,otp,remarks) => {
    const params = [sessionid,cli_id,user,otp,remarks,'Pending'];
    try {
        const twoFASQL = `insert into cm.two_factor_authentication(sessionid,cli_id, username, otp, invalid_attempts, regen_attempts, remark, status, created , expiry  ) values (?,?,?,?,0,0,?,?,now(),DATE_ADD(now(), INTERVAL ${process.env.OTP_EXPIRY} MINUTE))`;
        
        console.log('otpquery() [twoFASQL] sql & params => ',params);
        const r2 = await fastify.mariadb.query(twoFASQL, params);
        
    } catch (e) {
        
        console.error('con rollback', e);
        throw e;
    } 
   
});

fastify.decorate('processRecordsOnResend', async (sessionid,cli_id,status,user, otp) => {
  const con = await fastify.mariadb.getConnection();
  
  try {
      con.beginTransaction();

      const select2FASQL = 'select max(regen_attempts) as regen_attempts  from cm.two_factor_authentication where sessionid = ? and cli_id = ? ';
      const selParams =[sessionid, cli_id];
    
      const update2FASQL = 'update cm.two_factor_authentication set  status = ? where sessionid = ? and cli_id = ? ';
      const updParams =[status, sessionid, cli_id];
    
      console.log(select2FASQL,selParams,"***")
      const r3 =await con.query(select2FASQL,selParams);
      
      let resendCnt = r3[0].regen_attempts ?? 0;
      const insparams = [sessionid,cli_id,user,otp,resendCnt+1,'OTP regenerated','Pending'];
    
        const insert2FASQL = `insert into cm.two_factor_authentication(sessionid,cli_id, username, otp, invalid_attempts, regen_attempts, remark, status, created , expiry  ) values (?,?,?,?,0,?,?,?,now(),DATE_ADD(now(), INTERVAL ${process.env.OTP_EXPIRY} MINUTE))`;
      
     
    
      console.log('update2FAforCli() [twoFASQL] sql & params => ',update2FASQL, updParams);
      const r1 = await con.query(update2FASQL,updParams);

      console.log('otpquery() [twoFASQL] sql & params => ',insert2FASQL, insparams);
      const r2 = await con.query(insert2FASQL, insparams);
      
      con.commit();
      return resendCnt+1;
      } catch (e) {
            con.rollback();
            console.error('con rollback', e);
            throw e;
        } finally {
            await con.release();
        }

});

fastify.decorate('update2FAOnVerify', async (sessionid,cli_id,status,otp) => {
  params =[ status, sessionid, cli_id, otp];
  let update2FASQL = "";
  try {

    if(status == 'Pending'){
        const select2FASQL = 'select max(invalid_attempts) as invalid_attempts  from cm.two_factor_authentication where sessionid = ? and cli_id = ? and otp =? ';
        const selParams =[sessionid, cli_id, otp];
        const r3 =await fastify.mariadb.query(select2FASQL,selParams);
          
        let invalidCnt = r3[0].invalid_attempts ?? 0;

        update2FAInvlidSQL = 'update cm.two_factor_authentication set invalid_attempts = ?, remark= ? where sessionid = ? and cli_id = ? and otp = ?';
        const invParams =[ invalidCnt+1, 'Invalid otp', sessionid, cli_id, otp];
        
        const r2 = await fastify.mariadb.query(update2FAInvlidSQL,invParams);
    }else{
      update2FASQL = 'update cm.two_factor_authentication set status = ? where sessionid = ? and cli_id = ? and otp = ?';
      
      console.log('otpquery() [twoFASQL] sql & params => ',update2FASQL,params);
      const r2 = await fastify.mariadb.query(update2FASQL,params);
    }
      
      
  } catch (e) {
      
      console.error('update2FAOnVerify  query failed', e);
      throw e;
  }

});

};

module.exports = fp(auth);
