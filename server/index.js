require('dotenv').config();

const express = require('express');
const expressWs = require('express-ws');
const fs = require('fs');
const db = require('./db');

const app = express();
app.set('view engine', 'pug');
const websockets = expressWs(app);

const pubSub = () => {
  const subscribers = {};
  let lastId = 0;
  return {
    subscribe: (cb) => {
      subscribers[lastId] = cb;
      return lastId++;
    },
    send: (command) => {
      Object.keys(subscribers).forEach((k) => {
        subscribers[k](JSON.stringify({ command }));
      });
    },
    unsubscribe: (id) => {
      delete subscribers[id];
    }
  };
}

const commands = pubSub();

const handleResult = (data) => {
  if (!data.result) { console.log('Tried to handle an event that was not a result!', data); }
  if (data.result == 'capture:success') {
    db.createFrame(data);
  } else {
    console.log("Don't know how to handle result " + data.result );
  }
}

app.ws('/', (ws, req) => {
  const socketId = commands.subscribe((command) => {
    ws.send(command);
  });

  ws.on('message', (msg) => {
    try {
      const data = JSON.parse(msg);
      if (data.command) {
        commands.send(data.command);
      } else if (data.result) {
        handleResult(data);
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

app.get('/', (req, res) => {
  res.render('index');
})

app.post('/capture', (req, res) => {
  commands.send('capture:now');
  res.sendStatus(200);
});

app.use(function (err, req, res, next) {
  console.error(err.stack)
  res && res.status(500).send('Something broke!');
})

app.listen(8000);
