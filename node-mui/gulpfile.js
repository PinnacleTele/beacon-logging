const gulp = require('gulp');
const chmod = require('gulp-chmod');
const zip = require('gulp-zip');
const glprename = require('gulp-rename');
const glpif = require('gulp-if');
const fs = require('fs-extra');

const permissions = { read: true, write: true, execute: true };
const allPermissions = { owner: permissions, group: permissions, others: permissions };
const globs = ['**/*', '**/.env', '!**/.env_qa', '!**/.env_prod',
              '**/.env.prod', '!./node_modules{,/**}', './log{,/**}',
              '!./files{,/**}', '!./test{,/**}', '!.gitignore', '!./build{,/**}',
              '!./dist{,/**}'];

const globs_qa = ['**/*', '**/.env_qa', '!**/.env_prod',
'!./node_modules{,/**}', './log{,/**}', '!./files{,/**}',
'!./test{,/**}', '!.gitignore', '!./build{,/**}',
'!./dist{,/**}'];

const globs_prod = ['**/*', '**/.env_prod', '!**/.env_qa',
'!./node_modules{,/**}', './log{,/**}', '!./files{,/**}',
'!./test{,/**}', '!.gitignore', '!./build{,/**}',
'!./dist{,/**}'];

gulp.task('dev', async () => new Promise((resolve) => {
    fs.emptyDirSync('./dist');
    fs.emptyDirSync('./log');
    gulp.src(globs, { allowEmpty: true })
    .pipe(chmod(allPermissions, true))
    .pipe(zip('mui-api.zip'))
    .pipe(chmod(allPermissions, true))
    .pipe(gulp.dest('dist', { mode: '777', dirMode: '777' }));
    resolve();
}));

gulp.task('qa', async () => new Promise((resolve) => {
  fs.emptyDirSync('./dist');
  fs.emptyDirSync('./log');
  gulp.src(globs_qa, { allowEmpty: true })
  .pipe(glpif('**/.env_qa', glprename({ basename: '.env' })))
  .pipe(chmod(allPermissions, true))
  .pipe(zip('mui-api.zip'))
  .pipe(chmod(allPermissions, true))
  .pipe(gulp.dest('dist', { mode: '777', dirMode: '777' }));
  resolve();
}));

gulp.task('prod', async () => new Promise((resolve) => {
  fs.emptyDirSync('./dist');
  fs.emptyDirSync('./log');
  gulp.src(globs_prod, { allowEmpty: true })
  .pipe(glpif('**/.env_prod', glprename({ basename: '.env' })))
  .pipe(chmod(allPermissions, true))
  .pipe(zip('mui-api.zip'))
  .pipe(chmod(allPermissions, true))
  .pipe(gulp.dest('dist', { mode: '777', dirMode: '777' }));
  resolve();
}));
