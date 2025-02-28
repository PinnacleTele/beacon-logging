/* eslint-disable no-restricted-syntax */
const _ = require('lodash');
const mtz = require('moment-timezone');

const { brusersSchema, brupdateOthersSchema, brupdateROWSchema, brusersforfilterSchema, brchangesSchema } = require('../../../schema/br-schema');

async function br(fastify, opts) {
  fastify.addHook('preValidation', async (req, reply) => {
    console.log('preValidation hook called');
    return req.jwtVerify();
  });

  fastify.get('/brusers', { preValidation: [], schema: brusersSchema }, async (req, reply) => {
    try {
      const { cli_id, tz } = req.user;
      const respArr = [];

      // get all the users under him
      const result1 = await fastify.getAllUsersForId(cli_id);
      console.log('/brusers total users => ', result1.length);
      const cli_ids_arr = _.map(result1, 'cli_id');

      if (cli_ids_arr.length === 0) reply.send([]);

      // get ROW rate and last modified for the users
      const [result2, result3] = await Promise.all([fastify.getROWForAllUsers(cli_ids_arr), fastify.getLastModifiedBRForAllUsers(cli_ids_arr)]);

      // group by
      const cliidsGroupby = _.groupBy(result1, 'cli_id');
      const rowGroupby = _.groupBy(result2, 'cli_id');
      const mtsGroupby = _.groupBy(result3, 'cli_id');

      for await (const id of cli_ids_arr) {
        const obj = {};
        const cliobj = _.get(cliidsGroupby[id], 0, {});
        const rowobj = _.get(rowGroupby[id], 0, {});
        const mtsobj = _.get(mtsGroupby[id], 0, {});

        if (!_.isEmpty(cliobj)) {
          const { user_type, user, billing_currency } = cliobj;
          _.set(obj, 'cli_id', id);
          _.set(obj, 'user', user);
          _.set(obj, 'user_type', user_type);
          _.set(obj, 'billing_currency', billing_currency);
        }

        // check if international is enabled
        // TODO: optimize. db call on a loop
        const [hasIntlService, result6] = await Promise.all([fastify.hasInternationalService(id), fastify.getTotalCountriesWithBillRatesForIntl(id)]);
        _.set(obj, 'intl_enabled_yn', hasIntlService ? 1 : 0);
        // addition of extra one represents india
        const totalCountriesWithCustomRates = Number(_.get(result6[0], 'count', 0)) + 1;
        _.set(obj, 'countries_customrate_total', totalCountriesWithCustomRates);

        // process ROW
        if (!_.isEmpty(rowobj)) {
          // find the user conversion type to use for this cliid
          const convType = _.get(cliobj, 'billing_currency_conv_type');
          const billing_currency = _.get(cliobj, 'billing_currency');
          const baserate = _.get(rowobj, 'base_sms_rate');

          const billtobaseRate = await fastify.getConversionRateForUserV2(id, billing_currency, process.env.INTL_BASE_CURRENCY, convType);
          const basetobillRate = 1 / billtobaseRate;
          const convered_rate = _.floor(baserate * basetobillRate, 6);
          _.set(obj, 'row_rate', convered_rate);
        } else {
          _.set(obj, 'row_rate', 0);
        }

        // process last modified
        if (!_.isEmpty(mtsobj)) {
          const mts = _.get(mtsobj, 'modified_ts', null);

          if (!_.isNull(mts)) {
            // convert to acc tz
            const mtsMoment = mtz.tz(mts, tz);

            const formattedDt = mtsMoment.format('MMM DD, YYYY HH:mm:ss');
            _.set(obj, 'modified_ts', formattedDt);
            _.set(obj, 'modified_ts_unix', mtsMoment.unix());
          } else {
            _.set(obj, 'modified_ts', '');
            _.set(obj, 'modified_ts_unix', 0);
          }
        } else {
          _.set(obj, 'modified_ts', '');
          _.set(obj, 'modified_ts_unix', 0);
        }

        respArr.push(obj);
      }

      return respArr;
    } catch (err) {
      const e = fastify.httpErrors.createError(500, 'Could not get account list. Please try again', { err });
      return e;
    }
  });

  fastify.post('/brupdateOthers', { preValidation: [], schema: brupdateOthersSchema }, async (req, reply) => {
    try {
      const { cli_id: loggedin_cli_id } = req.user;
      const { cli_id, add_arr, update_arr, delete_arr } = req.body;
      const req_id = req.id;
      const addValuesArr = [];
      const updateValuesArr = [];
      const updateChangesValuesArr = [];

      console.log('/brupdateOthers incoming params => ', [cli_id, add_arr, update_arr, delete_arr]);

      const result = await fastify.findUserById(cli_id);
      const userObj = _.get(result, '0', {});

      if (_.isEmpty(userObj)) {
        return fastify.httpErrors.badRequest('Could not find user information');
      }
      const user = _.get(userObj, 'user');
      const billing_currency = _.get(userObj, 'billing_currency');

      const billtobaseRate = await fastify.getConversionRateForUser(cli_id, process.env.INTL_BASE_CURRENCY);

      // for new
      if (add_arr.length > 0) {
        _.forEach(add_arr, (o) => {
          const converted_rate = _.ceil(billtobaseRate * o.sms_rate, 12);
          addValuesArr.push([cli_id, o.country, converted_rate]);
        });
      }
      // for update
      if (update_arr.length > 0) {
        _.forEach(update_arr, (o) => {
          const converted_rate = _.ceil(billtobaseRate * o.sms_rate, 12);
          updateValuesArr.push([converted_rate, cli_id, _.toLower(o.country)]);
          updateChangesValuesArr.push([fastify.nanoid(), req_id, cli_id, user, o.country, o.sms_rate, o.sms_rate_old, 0, 0, loggedin_cli_id, billing_currency]);
        });
      }
      const result1 = await fastify.updateBillingRatesForOthers(cli_id, addValuesArr, updateValuesArr, updateChangesValuesArr, delete_arr);
      console.log('/brupdateOthers resp from db => ', result1);

      const resp = { statusCode: 200, message: 'Billing Rates (Other Countries) has been modified successfully' };

      return resp;
    } catch (err) {
      const code = fastify.nanoid();
      req.log.error(`error ${code}  =>${err}`);
      const e = fastify.httpErrors.createError(500, 'Could not update account rates. Please try again', { code });

      return e;
    }
  });

  fastify.post('/brupdateROW', { preValidation: [], schema: brupdateROWSchema }, async (req, reply) => {
    try {
      const { cli_id: loggedin_cli_id } = req.user;
      const { cli_id, sms_rate, sms_rate_old } = req.body;
      const req_id = req.id;

      console.log('/brupdateROW incoming params => ', [cli_id, sms_rate, sms_rate_old]);

      const result = await fastify.findUserById(cli_id);
      const userObj = _.get(result, '0', {});

      if (_.isEmpty(userObj)) {
        return fastify.httpErrors.badRequest('Could not find user information');
      }

      const user = _.get(userObj, 'user');
      const billing_currency = _.get(userObj, 'billing_currency');

      const billtobaseRate = await fastify.getConversionRateForUser(cli_id, process.env.INTL_BASE_CURRENCY);

      const converted_rate = _.ceil(billtobaseRate * sms_rate, 12);

      const result1 = await fastify.updateBillingRatesForROW(cli_id, user, converted_rate, sms_rate, sms_rate_old, loggedin_cli_id, req_id, billing_currency);
      console.log('/brupdateROW resp from db => ', result1);

      const resp = { statusCode: 200, message: 'Billing Rates (ROW) has been modified successfully' };

      return resp;
    } catch (err) {
      const code = fastify.nanoid();
      req.log.error(`error ${code}  =>${err}`);
      const e = fastify.httpErrors.createError(500, 'Could not update ROW rates. Please try again', { code });

      return e;
    }
  });

  fastify.get('/brusersforfilter', { preValidation: [], schema: brusersforfilterSchema }, async (req, reply) => {
    try {
      const { cli_id, user } = req.user;
      const respArr = [];

      // add All Users and the logged in user as part of the filter
      respArr.push({ cli_id_str: 'all', username: 'All Users' });
      respArr.push({ cli_id_str: cli_id, username: `${user} (You)` });

      // get all the users under him
      const result1 = await fastify.getAllUsersForId(cli_id);
      for (const row of result1) {
        respArr.push({ cli_id_str: row.cli_id, username: row.user });
      }

      return respArr;
    } catch (err) {
      const e = fastify.httpErrors.createError(500, 'Could not get users list. Please try again', { err });
      return e;
    }
  });

  fastify.post('/brchanges', { preValidation: [], schema: brchangesSchema }, async (req, reply) => {
    try {
      const { cli_id, tz } = req.user;
      const { dateselectiontype, fdate, tdate, cli_id_str } = req.body;
      const cliidArr = [];

      console.log('/brchanges incoming params => ', [dateselectiontype, fdate, tdate, cli_id_str]);

      const { fromdatetimeStrInIST, todatetimeStrInIST } = await fastify.getFromAndToDate(tz, dateselectiontype, fdate, tdate, true);

      if (_.eq(cli_id_str, 'all')) {
        // get all the users list under him
        const result1 = await fastify.getAllUsersForId(cli_id);
        const arr = _.map(result1, 'cli_id');

        // add the loggedin cli id
        cliidArr.push(cli_id);
        // add the rest
        cliidArr.push(...arr);
      } else {
        cliidArr.push(cli_id_str);
      }

      const [result2, result3] = await Promise.all([fastify.getBillRateChanges(cliidArr, fromdatetimeStrInIST, todatetimeStrInIST), fastify.getAllUsersInfoForIds(cliidArr)]);
      // group by cli_id
      const groupbyCli = _.groupBy(result3, 'cli_id');

      for (const row of result2) {
        const mts = _.get(row, 'modified_ts', null);
        const country = _.get(row, 'country', null);

        console.log(row);
        const user_type = _.get(_.get(groupbyCli, row.cli_id)[0], 'user_type');

        _.set(row, 'user_type', user_type);
        // _.set(row, 'billing_currency', row.billing_currency);

        const newrate = _.get(row, 'new_sms_rate', 0);
        const oldrate = _.get(row, 'old_sms_rate', 0);
        _.set(row, 'is_smsrate_higher_yn', (+newrate > +oldrate) ? 1 : 0);

        if (_.eq(_.toLower(country), _.toLower(process.env.DOMESTIC_COUNTRY_NAME))) {
          // add additional rate
          const newaddlrate = _.get(row, 'new_addl_rate', 0);
          const oldaddlrate = _.get(row, 'old_addl_rate', 0);
          _.set(row, 'is_addlrate_higher_yn', (+newaddlrate > +oldaddlrate) ? 1 : 0);
        } else {
          // addl is not appplicable for intl countries other than india
          _.set(row, 'new_addl_rate', -1);
          _.set(row, 'old_addl_rate', -1);
        }

        if (!_.isNull(mts)) {
          const mtsMoment = mtz.tz(mts, tz);
          const formattedDt = mtsMoment.format('MMM DD, YYYY HH:mm:ss');
          _.set(row, 'modified_ts', formattedDt);
          _.set(row, 'modified_ts_unix', mtsMoment.unix());
        }
      }

      return result2;
    } catch (err) {
      const e = fastify.httpErrors.createError(500, 'Could not get billing rate changes. Please try again', { err });
      return e;
    }
  });
}

module.exports = br;
