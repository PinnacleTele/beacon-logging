/* eslint-disable camelcase */
const fp = require('fastify-plugin');
const _ = require('lodash');

const contactRedis = async (fastify, opts) => {
  fastify.decorate('deleteContact', async (cli_id, g_id, g_type, mobiles) => {
    let gRedis = '';
    let contactsKey = '';
    let contactDetailsKey = '';

    if (_.eq(g_type, 'normal')) {
      contactsKey = `groups:contacts:${g_id}`;
      contactDetailsKey = `groups:contactdetails:${g_id}`;
      gRedis = 'GROUP';
    } else {
      contactsKey = `excludegroups:contacts:${g_id}`;
      contactDetailsKey = `excludegroups:contactdetails:${g_id}`;
      gRedis = 'EGROUP';
    }
    console.log('deleteContact() params => ', cli_id, g_id, g_type, mobiles);
    const total = await fastify.redis[gRedis].srem(contactsKey, mobiles);
    console.log('deleteContact() total mobiles deleted => ', total);
    const totalDel = await fastify.redis[gRedis].hdel(contactDetailsKey, mobiles);
    console.log('deleteContact() total mobile details deleted => ', totalDel);
    return total;
  });

  fastify.decorate('getTotalContacts', async (cli_id, g_id, g_type) => {
    let gRedis = '';
    let contactsKey = '';

    if (_.eq(g_type, 'normal')) {
      contactsKey = `groups:contacts:${g_id}`;
      gRedis = 'GROUP';
    } else {
      contactsKey = `excludegroups:contacts:${g_id}`;
      gRedis = 'EGROUP';
    }
    console.log('getTotalContacts() params => ', cli_id, g_id, g_type);
    const total = await fastify.redis[gRedis].scard(contactsKey);
    console.log('getTotalContacts() total contacts => ', g_id, total);

    return total;
  });

  fastify.decorate('deleteContactsAndDetails', async (ids, g_type) => {
    let gRedis = '';
    const contactsKeyList = [];
    const contactDetailsKeyList = [];

    if (_.eq(g_type, 'normal')) {
      for (const v of ids) {
        contactsKeyList.push(`groups:contacts:${v}`);
        contactDetailsKeyList.push(`groups:contactdetails:${v}`);
      }
      gRedis = 'GROUP';
    } else {
      for (const v of ids) {
        contactsKeyList.push(`excludegroups:contacts:${v}`);
        contactDetailsKeyList.push(`excludegroups:contactdetails:${v}`);
      }
      gRedis = 'EGROUP';
    }
    console.log('deleteContactsAndDetails() contacts keys to be deleted from redis => ', contactsKeyList, g_type);
    console.log('deleteContactsAndDetails() contactdetails keys to be deleted from redis => ', contactDetailsKeyList, g_type);

    const total = await fastify.redis[gRedis].del(contactsKeyList);
    console.log('getTotalContacts() total contacts keys deleted => ', total);
    const total1 = await fastify.redis[gRedis].del(contactDetailsKeyList);
    console.log('getTotalContacts() total contactdetails keys deleted => ', total1);

    return total;
  });

  fastify.decorate('updateContactDetails', async (cli_id, g_id, g_type, mobile, name, email) => {
    let gRedis = '';
    let contactDetailsKey = '';

    if (_.eq(g_type, 'normal')) {
      contactDetailsKey = `groups:contactdetails:${g_id}`;
      gRedis = 'GROUP';
    } else {
      contactDetailsKey = `excludegroups:contactdetails:${g_id}`;
      gRedis = 'EGROUP';
    }

    console.log('updateContactDetails() params => ', cli_id, g_id, g_type, mobile, name, email);

    if (_.isEmpty(name) && _.isEmpty(email)) {
      // delete from redis
      const total = await fastify.redis[gRedis].hdel(contactDetailsKey, mobile);
      console.log(`updateContactDetails() contact details deleted for mobile ${mobile} => ${total}`);
    } else {
      const total = await fastify.redis[gRedis].hset(contactDetailsKey, mobile, `${name}~${email}`);
      console.log(`updateContactDetails() contact details updated for mobile ${mobile} => ${total}`);
      // // check if contact details exists. if not dont update
      // const exists = await fastify.redis[gRedis].hexists(contactDetailsKey, mobile);
      // console.log(`updateContactDetails() does contact details exists for mobile ${mobile} => ${exists}`);
      //
      // if (+exists === 1) {
      //   // update details
      //   const total = await fastify.redis[gRedis].hset(contactDetailsKey, mobile, `${name}~${email}`);
      //   console.log(`updateContactDetails() contact details updated for mobile ${mobile} => ${total}`);
      // } else {
      //   console.warn(`updateContactDetails() *** SKIPPING UPDATE *** No contact details found for mobile ${mobile}`);
      // }
    }

    return true;
  });

  fastify.decorate('addContactDetails', async (cli_id, g_id, g_type, mobile, name, email) => {
    let gRedis = '';
    let contactDetailsKey = '';
    let contactsKey = '';

    if (_.eq(g_type, 'normal')) {
      contactsKey = `groups:contacts:${g_id}`;
      contactDetailsKey = `groups:contactdetails:${g_id}`;
      gRedis = 'GROUP';
    } else {
      contactsKey = `excludegroups:contacts:${g_id}`;
      contactDetailsKey = `excludegroups:contactdetails:${g_id}`;
      gRedis = 'EGROUP';
    }

    console.log('addContactDetails() params => ', cli_id, g_id, g_type, mobile, name, email);
    const total1 = await fastify.redis[gRedis].sadd(contactsKey, mobile);
    console.log(`addContactDetails() contact added for mobile ${mobile} => ${total1}`);

    if (!_.isEmpty(name) || !_.isEmpty(email)) {
      const total = await fastify.redis[gRedis].hset(contactDetailsKey, mobile, `${name}~${email}`);
      console.log(`addContactDetails() contact details updated for mobile ${mobile} => ${total}`);
    }
    return total1;
  });

  fastify.decorate('contactDetailsExists', async (cli_id, g_id, g_type, mobile) => {
    let gRedis = '';
    let contactDetailsKey = '';

    if (_.eq(g_type, 'normal')) {
      contactDetailsKey = `groups:contactdetails:${g_id}`;
      gRedis = 'GROUP';
    } else {
      contactDetailsKey = `excludegroups:contactdetails:${g_id}`;
      gRedis = 'EGROUP';
    }

    console.log('contactDetailsExists() params => ', cli_id, g_id, g_type, mobile);

    const exists = await fastify.redis[gRedis].hexists(contactDetailsKey, mobile);
    console.log(`contactDetailsExists() does contact details exists for mobile ${mobile} => ${exists}`);

    return +exists;
  });
};

module.exports = fp(contactRedis);
