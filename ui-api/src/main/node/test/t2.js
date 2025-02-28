const jsunicode = require('jsunicode');
const _ = require('lodash');

const s = _.toLower('0054004D004200200BB50B990BCD0B950BBF002D00200B890B990BCD0B950BB30BCD002000410054004D002C00200B9F0BC60BAA0BBF0B9F0BCD0020002600200B950BBF0BB00BBF0B9F0BBF0B9F0BCD00200B950BBE0BB00BCD0B9F0BC100200B8E0BA30BCD002C0BAA0BBF0BA90BCD0020002800500049004E0029002C00430056005600200B8E0BA30BCD002C002000450078007000690072007900200044006100740065002C0020004F0054005000200BAA0BCB0BA90BCD0BB10BB50BB10BCD0BB10BC800200B950BC70B9F0BCD0BAA0BA40BBF0BB20BCD0BB20BC8002E00200BAE0BC70BB10BCD0B950BC20BB10BBF0BAF00200BA40B950BB50BB20BCD0B950BB30BC800200B8E0BB50BB00BBF0B9F0BAE0BC10BAE0BCD00200BA40BC60BB00BBF0BB50BBF0B950BCD0B9500200BB50BC70BA30BCD0B9F0BBE0BAE0BCD0020002D0054004D0042');

const s16 = _.toLower('0054004d004200200bb50b990bcd0b950bbf002d00200b890b990bcd0b950bb30bcd002000410054004d002c00200b9f0bc60baa0bbf0b9f0bcd0020002600200b950bbf0bb00bbf0b9f0bbf0b9f0bcd00200b950bbe0bb00bcd0b9f0bc100200b8e0ba30bcd002c0baa0bbf0ba90bcd0020002800500049004e0029002c00430056005600200b8e0ba30bcd002c002000450078007000690072007900200044006100740065002c0020004f0054005000200baa0bcb0ba90bcd0bb10bb50bb10bcd0bb10bc800200b950bc70b9f0bcd0baa0ba40bbf0bb20bcd0bb20bc8002e00200bae0bc70bb10bcd0b950bc20bb10bbf0baf00200ba40b950bb50bb20bcd0b950bb30bc800200b8e0bb50bb00bbf0b9f0bae0bc10bae0bcd00200ba40bc60bb00bbf0bb50bbf0b950bcd0b9500200bb50bc70ba30bcd0b9f0bbe0bae0bcd0020002d0054004d0042');
const s162 = _.toLower('0054004D004200200BB50B990BCD0B950BBF002D00200B890B990BCD0B950BB30BCD002000410054004D002C00200B9F0BC60BAA0BBF0B9F0BCD0020002600200B950BBF0BB00BBF0B9F0BBF0B9F0BCD00200B950BBE0BB00BCD0B9F0BC100200B8E0BA30BCD002C0BAA0BBF0BA90BCD0020002800500049004E0029002C00430056005600200B8E0BA30BCD002C002000450078007000690072007900200044006100740065002C0020004F0054005000200BAA0BCB0BA90BCD0BB10BB50BB10BCD0BB10BC800200B950BC70B9F0BCD0BAA0BA40BBF0BB20BCD0BB20BC8002E00200BAE0BC70BB10BCD0B950BC20BB10BBF0BAF00200BA40B950BB50BB20BCD0B950BB30BC800200B8E0BB50BB00BBF0B9F0BAE0BC10BAE0BCD00200BA40BC60BB00BBF0BB50BBF0B950BCD0B9500200BB50BC70BA30BCD0B9F0BBE0BAE0BCD0020002D0054004D0042');

const s3 = '004800690020{#var#}002C002000770065FFFD0072006500200073006F007200720079002000740068006100740020007400680069006E006700730020006400690064006EFFFD007400200077006F0072006B0020006F0075007400200074006800690073002000740069006D0065002E0020005700650020007200650063006F006D006D0065006E00640020007400680061007400200079006F007500200075006E0069006E007300740061006C006C0020007400680065002000610070007000200061006E0064002000770065002000770069006C006C00200063006C006F0073006500200079006F007500720020006100630063006F0075006E0074002E00200059006F0075002000630061006E00200061006C007700610079007300200065006D00610069006C0020007500730020006F006E00200069006E0064006900610040006200720061006E00630068002E0063006F002000730068006F0075006C006400200079006F00750020007200650071007500690072006500200061007300730069007300740061006E00630065002E';
// console.log('>>>> ', jsunicode.decode(s3, { encoding: jsunicode.constants.encoding.utf16 }));
const s3arr = _.split(s3, '{#var#}');
const encodedVar = jsunicode.encode('{#var#}', { encoding: jsunicode.constants.encoding.utf16 }); // utf16
console.log(encodedVar);

const replaced = _.replace(s3, new RegExp('{#var#}', 'g'), '007b00230076006100720023007d');
console.log(replaced);
const tmsg = jsunicode.decode(replaced, { encoding: jsunicode.constants.encoding.utf16 });
console.log(tmsg)
// encoded with utf-8
// const s = '544d4220e0aeb5e0ae99e0af8de0ae95e0aebf2d20e0ae89e0ae99e0af8de0ae95e0aeb3e0af8d2041544d2c20e0ae9fe0af86e0aeaae0aebfe0ae9fe0af8d202620e0ae95e0aebfe0aeb0e0aebfe0ae9fe0aebfe0ae9fe0af8d20e0ae95e0aebee0aeb0e0af8de0ae9fe0af8120e0ae8ee0aea3e0af8d2ce0aeaae0aebfe0aea9e0af8d202850494e292c43565620e0ae8ee0aea3e0af8d2c2045787069727920446174652c204f545020e0aeaae0af8be0aea9e0af8de0aeb1e0aeb5e0aeb1e0af8de0aeb1e0af8820e0ae95e0af87e0ae9fe0af8de0aeaae0aea4e0aebfe0aeb2e0af8de0aeb2e0af882e20e0aeaee0af87e0aeb1e0af8de0ae95e0af82e0aeb1e0aebfe0aeaf20e0aea4e0ae95e0aeb5e0aeb2e0af8de0ae95e0aeb3e0af8820e0ae8ee0aeb5e0aeb0e0aebfe0ae9fe0aeaee0af81e0aeaee0af8d20e0aea4e0af86e0aeb0e0aebfe0aeb5e0aebfe0ae95e0af8de0ae9520e0aeb5e0af87e0aea3e0af8de0ae9fe0aebee0aeaee0af8d202d544d42';

const u = 'TMB வங்கி- உங்கள் ATM, டெபிட் & கிரிடிட் கார்டு எண்,பின் (PIN),CVV எண், Expiry Date, OTP போன்றவற்றை கேட்பதில்லை. மேற்கூறிய தகவல்களை எவரிடமும் தெரிவிக்க வேண்டாம் -TMB';
// const msg = jsunicode.decode(s, {  });
// const msg = jsunicode.decode(s, { encoding: jsunicode.constants.encoding.utf16be });
const msg = jsunicode.decode(s, { encoding: jsunicode.constants.encoding.utf16 });
const msg1 = jsunicode.decode(s162, { encoding: jsunicode.constants.encoding.utf16 });

console.log('total bytes => ', jsunicode.countEncodedBytes(s16, jsunicode.constants.encoding.utf16));
console.log('total bytes => ', jsunicode.countEncodedBytes(s16));
console.log('total bytes => ', jsunicode.countEncodedBytes(s162));
console.log('total bytes => ', jsunicode.countEncodedBytes('வ', jsunicode.constants.encoding.utf16));
console.log('total bytes => ', jsunicode.countEncodedBytes('வ', jsunicode.constants.encoding.utf8));
console.log('total bytes => ', jsunicode.countEncodedBytes('À')); // decimal val - 128
console.log('total bytes => ', jsunicode.countEncodedBytes(u));
console.log('total bytes => ', jsunicode.countEncodedBytes(u, jsunicode.constants.encoding.utf16));
console.log('total bytes => ', jsunicode.countEncodedBytes(u, jsunicode.constants.encoding.utf8));

const encodedHex = jsunicode.encode(u); // utf8
const encoded16Hex = jsunicode.encode(u, { encoding: jsunicode.constants.encoding.utf16 }); // utf16
// const encodedEngHex = jsunicode.encode('this is a english message', { encoding: jsunicode.constants.encoding.utf16 }); // utf16

// console.log(encodedHex);
console.log(encoded16Hex);
// console.log(encodedEngHex);
//
// const str = '\u2950';
// // const str = '\u0b86';
// console.log(str);
// console.log('ஆ'.charCodeAt());
// console.log('='.charCodeAt());
// console.log((2950).toString(16)); // convert decimal to hex
// // console.log(parseInt('0b86', 16))  // convert hex to decimal
// console.log(0x0b86); // convert hex to decimal
//
// function isDoubleByte(str) {
//   for (let i = 0, n = str.length; i < n; i += 1) {
//     console.log(str)
//     if (str.charCodeAt(i) > 127) { return true; }
//   }
//   return false;
// }
//
// console.log(msg);
// console.log(msg1);
// console.log(isDoubleByte('this Àis a english message'));
// const h = '0054004d004200200bb50b990bcd0b950bbf002d00200b890b990bcd0b950bb30bcd002000410054004d002c00200b9f0bc60baa0bbf0b9f0bcd0020002600200b950bbf0bb00bbf0b9f0bbf0b9f0bcd00200b950bbe0bb00bcd0b9f0bc100200b8e0ba30bcd002c0baa0bbf0ba90bcd0020002800500049004e0029002c00430056005600200b8e0ba30bcd002c002000450078007000690072007900200044006100740065002c0020004f0054005000200baa0bcb0ba90bcd0bb10bb50bb10bcd0bb10bc800200b950bc70b9f0bcd0baa0ba40bbf0bb20bcd0bb20bc8002e00200bae0bc70bb10bcd0b950bc20bb10bbf0baf00200ba40b950bb50bb20bcd0b950bb30bc800200b8e0bb50bb00bbf0b9f0bae0bc10bae0bcd00200ba40bc60bb00bbf0bb50bbf0b950bcd0b9500200bb50bc70ba30bcd0b9f0bbe0bae0bcd0020002d0054004d0042';
const h = '4400650061007200200050006100720074006E00650072002C00200079006F0075007200200063006F006D0070006C00610069006E007400200069007300200075006E0064006500720020007200650076006900650077002E0020005200650066002000690064003A0020007B00230BB50BA30B950BCD0B950BAE0BCD0023007D002E0020002D004B00750072006C006F006E';

const h1 = jsunicode.decode(h, { encoding: jsunicode.constants.encoding.utf16 });
console.log(h1);

const aa = '0B870BA40BC1007b00230076006100720023007d0B950BCD0B950BBE0BA900200BAE0BBE0BA40BBF0BB00BBF0B9A0BCD00200B9A0BC60BAF0BCD0BA40BBF002E';
const h2 = jsunicode.decode(aa, { encoding: jsunicode.constants.encoding.utf16 });
console.log(h2)

// convert hex to unicode string
// if (_.eq(cLangType, 'unicode')) {
//     const m = _.get(payload, 'msg', '');
//     try {
//         console.log(m);
//         const msg = jsunicode.decode(m, { encoding: jsunicode.constants.encoding.utf16 });
//         _.set(payload, 'msg', msg);
//     } catch (e) {
//         // ignore the error. continue with original message
//     }
// }