const files = require('./files');
const panel = require('./panel');
const scad = require('./scad');

module.exports = (app, commands, db) => {
  app.use('/', panel(commands, db));
  app.use('/files', files(commands, db));
  app.use('/scad', scad(commands, db));
};
