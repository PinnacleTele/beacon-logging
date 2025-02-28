const Denque = require('denque');

const q = new Denque();
module.exports = {
    getErrorsQ() {
        return q;
    },

};
