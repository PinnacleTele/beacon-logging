const gulp = require('gulp');
const zip = require('gulp-zip');

gulp.task('default', () => gulp.src(['**/*', '**/.env.qa', '**/.env.dev', '**/.env.prod', '!./node_modules{,/**}', '!./log{,/**}', '!./files{,/**}', '!./test{,/**}', '!.gitignore', '!./build{,/**}', '!./dist{,/**}'])
  .pipe(zip('cm.zip'))
  .pipe(gulp.dest('dist')));
