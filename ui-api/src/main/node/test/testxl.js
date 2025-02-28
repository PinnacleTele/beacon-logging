const readXlsxFile = require('read-excel-file/node');

// const mariapool = require('../factory/mariadbpool.cmdb');

const xldata = [];

readXlsxFile('/Volumes/StorageSpace4/WebstormProjects/nunzioz-projects/cm-api-nodejs/test/SMS template dump_KAUVRY.xlsx').then((rows) => {
  // `rows` is an array of rows
  // each row being an array of cells.
  console.log(rows[85]);
  console.log(rows.length);
});
