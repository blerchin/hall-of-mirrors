const api = require('./api');
const panel = require('./panel');

module.exports = (app, commands, db) => {
  app.use('/', panel);
  app.use('/api', api(commands, db));
};
