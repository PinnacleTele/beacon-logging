const mariadb = require('mariadb');

const pool = mariadb.createPool({
    host: process.env.MARIA_DB_CM_HOST,
    port: process.env.MARIA_DB_CM_PORT,
    user: process.env.MARIA_DB_CM_USER,
    password: process.env.MARIA_DB_CM_PASSWORD,
    database: process.env.MARIA_DB_CM_DATABASE,
    connectionLimit: process.env.MARIA_DB_CM_CONNECTION_LIMIT_FOR_OTHERS,
});

module.exports = pool;
