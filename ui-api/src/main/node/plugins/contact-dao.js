const fp = require('fastify-plugin');
const _ = require('lodash');
// TODO: Change req param to respective individual params. *** for method reusability ***

const contactdb = async (fastify, opts) => {
  fastify.decorate('addContactFiles', async (cli_id, g_id, g_type, mobile, name, email, valuesArr) => {
    const con = await fastify.mariadb.getConnection();
    let r1 = {};
    let r2 = {};

    try {
      let sql = 'update cm.group_master set status=\'queued\' where id=?';
      const params = [g_id];

      con.beginTransaction();
      console.log('addContacts() [group_master] sql params => ', sql, params);
      // update group master status
      r1 = await con.query(sql, params);

      // insert to group files
      sql = `insert into cm.group_files (id, g_id, filename, filename_ori, filetype, fileloc, total)
                values (?,?,?,?,?,?,?)`;

      console.log('createGroup() [group_files] sql params => ', sql, valuesArr);
      r2 = await con.batch(sql, valuesArr);
      con.commit();
    } catch (e) {
      con.rollback();
      console.error('con rollback', e);
      throw e;
    } finally {
      await con.release();
    }
    return { m: 'sucess' };
  });
};

module.exports = fp(contactdb);
