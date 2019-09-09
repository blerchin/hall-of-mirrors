const { Router } = require('express');
const router = new Router();
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

  router.get('/:key', async (req, res) => {
    const { key } = req.params;
    try {
      const data = await readFile(path.join(UPLOADS_DIR, key));
      res.set('Content-Type', mime.contentType(path.extname(key)));
      res.send(data);
    } catch (e) {
      console.warn(e.message);
      res.sendStatus(403);
    }
  });

  return router;
};
