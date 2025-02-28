const gulp = require('gulp');
const zip = require('gulp-zip');
const chmod = require('gulp-chmod');
const glprename = require('gulp-rename');
const glpif = require('gulp-if');
const fs = require('fs-extra');

gulp.task('default', () => gulp.src(['**/*', '**/.env.qa', '**/.env.dev', '**/.env.prod', '!./node_modules{,/**}', '!./log{,/**}', '!./files{,/**}', '!./test{,/**}', '!.gitignore', '!./build{,/**}', '!./dist{,/**}'])
  .pipe(zip('cm.zip'))
  .pipe(gulp.dest('dist')));


const permissions = { read: true, write: true, execute: true };
const allPermissions = { owner: permissions, group: permissions, others: permissions };
const globs = ['**/*', '**/.env', '!**/.env_qa', '!**/.env_prod',
              '**/.env.prod', '!./node_modules{,/**}', './log{,/**}',
              '!./files{,/**}', '!./test{,/**}', '!.gitignore', '!./build{,/**}',
              '!./dist{,/**}'];

gulp.task('dev', async () => new Promise((resolve) => {
  fs.emptyDirSync('./dist');
  fs.emptyDirSync('./log');
  gulp.src(globs, { allowEmpty: true })
  .pipe(chmod(allPermissions, true))
  .pipe(zip('cm.zip'))
  .pipe(chmod(allPermissions, true))
  .pipe(gulp.dest('dist', { mode: '777', dirMode: '777' }));
  resolve();
}));

const globs_qa = ['**/*', '**/.env_qa', '!**/.env_prod',
'!./node_modules{,/**}', './log{,/**}', '!./files{,/**}',
'!./test{,/**}', '!.gitignore', '!./build{,/**}',
'!./dist{,/**}'];

gulp.task('qa', async () => new Promise((resolve) => {
  fs.emptyDirSync('./dist');
  fs.emptyDirSync('./log');
  gulp.src(globs_qa, { allowEmpty: true })
  .pipe(glpif('**/.env_qa', glprename({ basename: '.env' })))
  .pipe(chmod(allPermissions, true))
  .pipe(zip('cm.zip'))
  .pipe(chmod(allPermissions, true))
  .pipe(gulp.dest('dist', { mode: '777', dirMode: '777' }));
  resolve();
}));



