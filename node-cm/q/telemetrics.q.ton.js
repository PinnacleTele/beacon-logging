const Denque = require('denque');

const q = new Denque();
module.exports = {
    getTelemetricQ() {
        return q;
    },

};
