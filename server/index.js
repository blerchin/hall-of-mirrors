require('dotenv').config();

const express = require('express');
const expressWs = require('express-ws');
const methodOverride = require('method-override');
const fs = require('fs');
const db = require('./db');
const pubSub = require('./pubSub');
const setRoutes = require('./routes');

const app = express();
app.use(methodOverride('_method'));
app.set('view engine', 'pug');
app.use(express.urlencoded({ extended: true }));
const websockets = expressWs(app);

const commands = pubSub();

setRoutes(app, commands, db);

const handleWsResult = (data, uuid = null) => {
  if (!data.result) { console.log('Tried to handle an event that was not a result!', data); }
  if (data.result == 'capture:success') {
    db.createFrame({
      ...data,
      uuid
    }).catch((e) => console.error(e.stack));
  } else {
    console.log("Don't know how to handle result " + data.result );
  }
}

app.ws('/ws/:uuid?', (ws, req) => {
  const socketId = commands.subscribe((command) => {
    ws.send(command);
  }, req.params.uuid);

  ws.on('message', (msg) => {
    try {
      const data = JSON.parse(msg);
      if (data.command) {
        commands.send(data.command);
      } else if (data.result) {
        handleWsResult(data, req.params.uuid);
      }
    } catch (SyntaxError) {
      console.log('Unable to parse message: ' + msg);
    }
  });

  ws.on('close', (info) => {
    if (socketId !== null) {
      commands.unsubscribe(socketId);
    }
  });

  ws.on('error', (evt) => {
    console.log(evt);
  });
});


app.use(function (err, req, res, next) {
  console.error(err.stack)
  res && res.status(500).send('Something broke!');
})

app.listen(8000);
