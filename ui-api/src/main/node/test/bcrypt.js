const bcryptjs = require('bcryptjs');
const _ = require('lodash');

const hash = bcryptjs.hashSync('welcome');

console.log(hash);

// $2a$10$uHVatIT2jWO0q6TDMv1GieIme1awVAtUGuPP/whx7RGdVICNCBMEm
// $2a$10$NH/t7E4IYpK8BQ2pR4ou.uO2bex8/mH70bWxqW9kSD2lzD9mwE/Ea

console.log(bcryptjs.compareSync('welcome', '$2a$10$7WXkm7BLZ2i9u13cPBSYs.inTafSva1rc7fWsct8dNjcNVzRnJd2K'));
// console.log(bcryptjs.compareSync('lnjb5DV8', '$2a$10$wJk8Kwaz8YcMdTj3uqCCfOp/TxeXothUr5Y0UwFsMN53Xsi1tACNG'));
// console.log(bcryptjs.compareSync('jshRuBo0', '$2a$10$urrZLHgfXmD2q2qMn0n4SOtaPD2OCryeu1ESCP7JcZUhsEdpnRq.u'));
// console.log(bcryptjs.compareSync('Qkm4XfGx', '$2a$10$O0HkrtRbDou3esHsZjZFzeYwhTjMre66fGfSNOoP0eSRVs4adeQD.'));
// console.log(bcryptjs.compareSync('AfYO3PjW', '$2a$10$5/iw7Q2PENxWE5HV0dvvp.IhBEuyD2D1FOkc0M/fIGO2k9iU5FjDS'));
// console.log(bcryptjs.compareSync('etySaeDW', '$2a$10$VAQRJhZ97RHqTDrC5aA4tOr5zRLhnt8Szg5Tp7h3XGd0NsNZypRCG'));
// console.log(bcryptjs.compareSync('2qFIN6wg', '$2a$10$1JzoRgmbsmVE32GjD.xVZeNV6fsLdCceim5Ayb/oscXG3N7/ERw0O'));
// console.log(bcryptjs.compareSync('do60JBwA', '$2a$10$IZzfsFHuU6wotRNhV41DiOp0U6n77u0TcemDp6cOq6.puadt85NUu'));
// console.log(bcryptjs.compareSync('OALIipYL', '$2a$10$hhecNaDccdZSrLsyoYpGfeIeNQklzy27fO5XS3rVhDxmd8DYURmoS'));
// console.log(bcryptjs.compareSync('yujWTNNe', '$2a$10$dy2h6zd9DrOm0UTVUBGGA.FvfCvJt1Zv8iLyR8BDU0YGQFBWpps6q'));
//
// console.log(bcryptjs.compareSync('nF34uXSg5Z', '$2a$10$pbxF4jj7smrkyO3oBVXz4OAXHkzXTySP3UHSB1U71TMlono0IQipa'));
// console.log(bcryptjs.compareSync('zyjIvTesE7', '$2a$10$TMlmD/H8qHw/z3TJBe7EneBCsKydrJUI5ZH8PL22otWLXqZOGvvEC'));
// console.log(bcryptjs.compareSync('n0FNlJQxRB', '$2a$10$fkfY4hnxcP1NHyiEwbkgYeFGOLZ1BHi.vKUVit4XXAijCOa2MGjAy'));
// console.log(bcryptjs.compareSync('JPTxGhjJuW', '$2a$10$IKr2eUcD6LjlIf1q/s7E4OgmmKwpnk/DpKrIBm6PKgmopsvC4ZU8u'));
// console.log(bcryptjs.compareSync('Fy40VQFcaH', '$2a$10$7XSzzw7Vw7AtnU/1A1k0I.GQKHlT7MApLxE4ERmsrQArsy//YKv0e'));
// console.log(bcryptjs.compareSync('372TCb9Zeo', '$2a$10$RI311p0nFDRBReqUJHvdJeTQ1ozz4D8Zw1O1p0o.YmiV1YLoGA1zq'));
// console.log(bcryptjs.compareSync('rJgHtW5sQL', '$2a$10$BPHPPSGL8SYb5/kHKIP6huNSNbBhOs9HZ4BOJUX6h6VcpxifpG57K'));
// console.log(bcryptjs.compareSync('0HkuO7c079', '$2a$10$8aX44DmKM.9YvPTenxc66.Z6B38KWWcQ9du/M64I9GWZN1lQ7QESy'));
// console.log(bcryptjs.compareSync('BV1r9fJYWN', '$2a$10$DI97ohqQiQeU2tGKlJquv.dSE590d.aJVGwQwM8zkas0aLOMJZJLS'));
// console.log(bcryptjs.compareSync('jrdaPB14o6', '$2a$10$sZpoo3y.JZteCEwFuIB9we6UGMISN2rjlKwnlf4oTJ40956aGP6zm'));
//
// const list = ['STPWsbSB',
//   'lnjb5DV8',
//   'jshRuBo0',
//   'Qkm4XfGx',
//   'AfYO3PjW',
//   'etySaeDW',
//   '2qFIN6wg',
//   'do60JBwA',
//   'OALIipYL',
//   'yujWTNNe',
//   'nF34uXSg5Z',
//   'zyjIvTesE7',
//   'n0FNlJQxRB',
//   'JPTxGhjJuW',
//   'Fy40VQFcaH',
//   '372TCb9Zeo',
//   'rJgHtW5sQL',
//   '0HkuO7c079',
//   'BV1r9fJYWN',
//   'jrdaPB14o6',
// ]
//
// _.forEach(list, (item) => {
//   console.log(`${item} ${bcryptjs.hashSync(item)}`);
// })
