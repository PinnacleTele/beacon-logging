const Redis = require('ioredis');
const dotenv = require('dotenv');
dotenv.config();


const rclient = new Redis({
    port: process.env.REDIS_TELEMETRIC_PORT, // Redis port
    host: process.env.REDIS_TELEMETRIC_HOST, // Redis host
    db: process.env.REDIS_TELEMETRIC_DB,
    password: null,
});

module.exports = rclient;
