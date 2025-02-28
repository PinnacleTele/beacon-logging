const gulp = require('gulp');
const zip = require('gulp-zip');
const clean = require('gulp-clean');
const bump = require('gulp-bump');
const fs = require('fs');

const artifactName = 'cm';

function readversion() {
  const obj = JSON.parse(fs.readFileSync('./version.txt', 'utf8'));

  return obj.version;
}

gulp.task('clean', () => gulp.src('./build')
  .pipe(clean({ force: true })));

gulp.task('build', ['clean'], () => {
  const v = readversion();

  return gulp.src(['**/*', '!./node_modules{,/**}', '!./log{,/**}', '!./files{,/**}', '!./test{,/**}', '!.gitignore', '!./build{,/**}'])
    .pipe(gulp.dest(`./build/src/${artifactName}`));
});

gulp.task('zip', ['build'], () => {
  const v = readversion();

  return gulp.src('./build/src/**/*')
    .pipe(zip(`${artifactName}.zip`))
    .pipe(gulp.dest('./build/dist'));
});

/** ********************************************************************************* */

gulp.task('default', ['zip'], () => {
  console.log('this is the gulp default');
});

/** ********************************************************************************* */

gulp.task('release', ['zip'], () => gulp.src('./build/dist/*.zip')
  .pipe(gulp.dest(`./../minsight-releases/${artifactName}`)));

gulp.task('bump-pre', () => gulp.src('./version.txt')
  .pipe(bump({ type: 'prerelease' }))
  .pipe(gulp.dest('./')));

gulp.task('bump-major', () => gulp.src('./version.txt')
  .pipe(bump({ type: 'major' }))
  .pipe(gulp.dest('./')));

gulp.task('bump-minor', () => gulp.src('./version.txt')
  .pipe(bump({ type: 'minor' }))
  .pipe(gulp.dest('./')));

gulp.task('bump-patch', () => gulp.src('./version.txt')
  .pipe(bump({ type: 'patch' }))
  .pipe(gulp.dest('./')));

gulp.task('releaseqa', ['bump-pre', 'zip', 'release'], () => {
  console.log(`version - ${readversion()}`);
});

gulp.task('releasemajor', ['bump-major', 'zip', 'release'], () => {
  console.log(`version - ${readversion()}`);
});

gulp.task('releaseminor', ['bump-minor', 'zip', 'release'], () => {
  console.log(`version - ${readversion()}`);
});

gulp.task('releasepatch', ['bump-patch', 'zip', 'release'], () => {
  console.log(`version - ${readversion()}`);
});
