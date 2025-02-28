/* eslint-disable func-names */
const fp = require('fastify-plugin');
const { customAlphabet } = require('nanoid');
const axios = require('axios');
const bcryptjs = require('bcryptjs');
const https = require('https');
const Mustache = require('mustache');

const util_p = async function (fastify) {
  fastify.decorate('nanoid', () => {
    const nid = customAlphabet('1234567890abcdefghijklmnopqrstuvwxyz', 36);
    return nid();
  });

fastify.decorate('nanoiduipass', () => {
    const nid = customAlphabet('0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ_abcdefghijklmnopqrstuvwxyz-', 8);
    return nid();
});

fastify.decorate('apipass', async (cli_id) => {
    const url = process.env.PASSWORD_SERVICE_URL;
    // get the pass from remote service
    const resp = await axios.get(`${url}?cli_id=${cli_id}&reset_api=1`);

    return resp.data;
});

fastify.decorate('smpppass', async (cli_id) => {
    const url = process.env.PASSWORD_SERVICE_URL;
    // get the pass from remote service
    const resp = await axios.get(`${url}?cli_id=${cli_id}&reset_smpp=1`);

    return resp.data;
});

fastify.decorate('hash', (str) => {
  const hash = bcryptjs.hashSync(str);
  return hash;
});

fastify.decorate('sendMail', async (from_email, from_name, subject, to_email, to_name, reply_to, message) => {
  const { EMAIL_URL, EMAIL_OWNERID, EMAIL_TOKEN, EMAIL_SMTP_USERNAME } = process.env;

  const reqPayload = {
      owner_id: EMAIL_OWNERID,
      token: EMAIL_TOKEN,
      smtp_user_name: EMAIL_SMTP_USERNAME,
      message: {
          html: message,
          subject,
          from_email,
          from_name,
          to: [
              {
                  email: to_email,
                  name: to_name,
                  type: 'to',
              },
          ],
          headers: {
              'Reply-To': reply_to,
              'X-Unique-Id': fastify.nanoid(),
          },
          attachments: [

          ],
          images: [

          ],
      },
  };

  console.log(reqPayload);
  const resp = await axios.post(EMAIL_URL, reqPayload, { httpsAgent: new https.Agent({ rejectUnauthorized: false }) });

  return resp.data;
});

    fastify.decorate('CONSTANTS', {
      INVALID_AUTH: -603,
      USERNAME_NOT_UNIQUE_CODE: -600,
      INVALID_EMAIL: -605,
      NOT_ENOUGH_WALLET_BAL: -602,
      HOST: process.env.MARIA_DB_CM_HOST,
      PORT: process.env.MARIA_DB_CM_PORT,
      USER: process.env.MARIA_DB_CM_USER,
      PASSWORD: process.env.MARIA_DB_CM_PASSWORD,
      DATABASE: process.env.MARIA_DB_CM_DATABASE,
      CONNECTIONLIMIT: process.env.MARIA_DB_CM_CONNECTION_LIMIT,
      SERVICES: ['sms'],
      SUB_SERVICES: ['cm', 'international', 'smpp', 'api'],
    });

    fastify.decorate('logError', async (obj) => {
      const { cliid, username, httpcode, url, ip, errmsg, params, sessionid, reqid, errstack } = obj;
      const sql = 'insert into imp.error_log (user_id, username, httpcode, route, ip, err_msg, params, sessionid, req_id, err_stack, created_ts) values (?,?,?,?,?,?,?,?,?,?,now())';
      const paramsarr = [cliid, username, httpcode, url, ip, errmsg, params, sessionid, reqid, errstack];

      await fastify.mariadb.query(sql, paramsarr);
    });

    fastify.decorate('sendEmail', async (template, dataObj) => {
      const { EMAIL_URL, EMAIL_OWNERID, EMAIL_TOKEN, EMAIL_SMTP_USERNAME } = process.env;
      const { subject, from_email, from_name, to_email, to_name, reply_to } = dataObj;

      // render the template
      const renderedTemplate = Mustache.render(template, dataObj);

      const reqPayload = {
          owner_id: EMAIL_OWNERID,
          token: EMAIL_TOKEN,
          smtp_user_name: EMAIL_SMTP_USERNAME,
          message: {
              html: renderedTemplate,
              subject,
              from_email,
              from_name,
              to: [
                  {
                      email: to_email,
                      name: to_name,
                      type: 'to',
                  },
              ],
              headers: {
                  'Reply-To': reply_to,
                  'X-Unique-Id': fastify.nanoid(),
              },
              attachments: [

              ],
              images: [

              ],
          },
      };

      console.log(reqPayload);
      // process.env.NODE_TLS_REJECT_UNAUTHORIZED = 0;
      const resp = await axios.post(EMAIL_URL, reqPayload, { httpsAgent: new https.Agent({ rejectUnauthorized: false }) });

      return resp.data;
  });
};

module.exports = fp(util_p);
