const express = require('express');
const router = new express.Router();
const fs = require('fs');
const path = require('path');
const mime = require('mime-types');
const { promisify } = require('util');
const multer = require('multer');

const UPLOADS_DIR = 'uploads/'
const upload = multer({ dest: UPLOADS_DIR });

const readFile = promisify(fs.readFile);
const writeFile = promisify(fs.writeFile);

module.exports = (commands, db) => {
  router.post('/upload', upload.single('file'), (req, res) => {
    if (req.file) {
      res.status(200).send(req.file.filename);
    } else {
      res.sendStatus(500);
    }
  });

  router.use('/', express.static(UPLOADS_DIR, { 
    immutable: true,
    maxAge: 1000 * 3600 * 24 * 30,
    setHeaders: (res, path, stat) => res.set('Content-Type', 'image/jpeg')
  }));

  return router;
};
