/* eslint-disable camelcase */
const fp = require('fastify-plugin');
const _ = require('lodash');

const brdb = async (fastify, opts) => {
  fastify.decorate('getROWForAllUsers', async (cli_id_arr) => {
    const sql = 'select * from configuration.client_intl_rates where cli_id in(?) and upper(country)=\'ROW\'';
    const result = await fastify.mariadb.query(sql, [cli_id_arr]);
    return result;
  });
  fastify.decorate('getLastModifiedBRForAllUsers', async (cli_id_arr) => {
    const sql = 'SELECT cli_id, max(modified_ts) as modified_ts from cm.billing_rate_changes where cli_id in (?) and modified_ts is not null GROUP by cli_id';
    const params = [cli_id_arr];

    console.log('getLastModifiedBRForAllUsers() sql params => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });
  fastify.decorate('getBillRatesForIntl', async (cli_id) => {
    const sql = 'select * from configuration.client_intl_rates where cli_id=?';
    const params = [cli_id];

    console.log('getBillRatesForIntl() sql params => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('getTotalCountriesWithBillRatesForIntl', async (cli_id) => {
    const sql = 'select count(1) as count from configuration.client_intl_rates cir where cli_id =? and lower(country) != \'row\'';
    const params = [cli_id];

    console.log('getTotalCountriesWithBillRatesForIntl() sql params => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });

  fastify.decorate('updateBillingRatesForOthers', async (cli_id, addValuesArr, updateValuesArr, updateChangesValuesArr, delete_arr, billing_currency) => {
    const con = await fastify.mariadb.getConnection();
    const sqladd = 'insert into configuration.client_intl_rates (cli_id, country, base_sms_rate) values (?,?,?)';
    const sqlupdate = 'update configuration.client_intl_rates set base_sms_rate=? where cli_id=? and lower(country)=?';
    const sqlupdate2 = 'insert into cm.billing_rate_changes (id, req_id, cli_id, username, country, new_sms_rate, old_sms_rate, new_addl_rate, old_addl_rate, done_by, billing_currency) values (?,?,?,?,?,?,?,?,?,?,?)';
    const sqldelete = 'delete from configuration.client_intl_rates where cli_id=? and country in (?)';

    let result1 = null;
    try {
      con.beginTransaction();

      if (addValuesArr.length > 0) {
        console.log('updateBillingRatesForOthers() [client_intl_rates (ADD)] sql params => ', sqladd, addValuesArr);
        result1 = await con.batch(sqladd, addValuesArr);
      }

      if (updateValuesArr.length > 0) {
        console.log('updateBillingRatesForOthers() [client_intl_rates (UPDATE)] sql params => ', sqlupdate, updateValuesArr);
        result1 = await con.batch(sqlupdate, updateValuesArr);
        console.log('updateBillingRatesForOthers() [billing_rate_changes (INSERT)] sql params => ', sqlupdate2, updateChangesValuesArr);
        result1 = await con.batch(sqlupdate2, updateChangesValuesArr);
      }
      if (delete_arr.length > 0) {
        console.log('updateAccountRates() [client_intl_rates (DELETE)] sql params => ', sqldelete, [cli_id, delete_arr]);
        result1 = await con.query(sqldelete, [cli_id, delete_arr]);
      }

      con.commit();
    } catch (e) {
      con.rollback();
      console.error('con rollback', e);
      throw e;
    } finally {
      await con.release();
    }
    return result1;
  });

  fastify.decorate('updateBillingRatesForROW', async (cli_id, username, converted_rate, sms_rate, sms_rate_old, loggedin_cli_id, req_id, billing_currency) => {
    const con = await fastify.mariadb.getConnection();
    const sql = 'select * from configuration.client_intl_rates where cli_id=? and lower(country)=\'row\'';
    const sqlupdate = 'update configuration.client_intl_rates set base_sms_rate=? where cli_id=? and lower(country)=\'row\'';
    const sqlinsert = 'insert into configuration.client_intl_rates (cli_id, country, base_sms_rate) values (?,?,?)';
    const sqlinsert2 = 'insert into cm.billing_rate_changes (id, req_id, cli_id, username, country, new_sms_rate, old_sms_rate, new_addl_rate, old_addl_rate, done_by, billing_currency) values (?,?,?,?,?,?,?,?,?,?,?)';
    const params2 = [fastify.nanoid(), req_id, cli_id, username, 'ROW', sms_rate, sms_rate_old, 0, 0, loggedin_cli_id, billing_currency];
    let result = null;
    try {
      con.beginTransaction();

      // check if row already exists
      result = await con.query(sql, [cli_id]);
      if (result.length === 0) {
        // insert
        console.log('updateAccountRates() [client_intl_rates (INSERT)] sql params => ', sqlinsert, [cli_id, 'ROW', converted_rate]);
        result = await con.query(sqlinsert, [cli_id, 'ROW', converted_rate]);
      } else {
        // update
        console.log('updateAccountRates() [client_intl_rates (UPDATE)] sql params => ', sqlupdate, [converted_rate, cli_id]);
        result = await con.query(sqlupdate, [converted_rate, cli_id]);
        console.log('updateAccountRates() [client_intl_rates (INSERT)] sql params => ', sqlinsert2, params2);
        result = await con.query(sqlinsert2, params2);
      }
      con.commit();
    } catch (e) {
      con.rollback();
      console.error('con rollback', e);
      throw e;
    } finally {
      await con.release();
    }

    return result;
  });

  fastify.decorate('getBillRateChanges', async (cli_id_arr, fromdatetime, todatetime) => {
    const sql = 'select * from cm.billing_rate_changes where cli_id in(?) and modified_ts between ? and ? order by modified_ts desc';
    const params = [cli_id_arr, fromdatetime, todatetime];

    console.log('getBillRateChanges() sql params => ', sql, params);
    const result = await fastify.mariadb.query(sql, params);
    return result;
  });
};

module.exports = fp(brdb);
