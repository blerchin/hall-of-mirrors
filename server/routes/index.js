const api = require('./api');
const panel = require('./panel');
const scad = require('./scad');

module.exports = (app, commands, db) => {
  app.use('/', panel(commands, db));
  app.use('/api', api(commands, db));
  app.use('/scad', scad(commands, db));
};
