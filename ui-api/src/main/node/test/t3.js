const bcryptjs = require('bcryptjs');
const _ = require('lodash')
const hash = bcryptjs.hashSync('HHsferY');

console.log(hash);

// const isSame = bcryptjs.compareSync('password', '$2a$10$0MB61m1ZiMRIsTnHsTLZoOmMeCTxH3QgpXKVvM3tDGA6UN70WvHKs');
// console.log(isSame)

// prpothyswarna - $2a$10$0MK8x5DGZqDFsAMp/jFIjutADIvswZDF2/W2wptHXirmdZ.p9wbUC
// prexzhilar - $2a$10$bK46hxpjRuMvQ1To9IQzHuT2erVv6b63CngYHDru5ezYmxyZUVJhm
const obj = {undefined, undefined }
const a = [[]];
console.log(_.flatten(a).length);