const Router = require('express-promise-router');
const router = new Router();

module.exports = (commands, db) => {
  router.get('/', (req, res) => {
    res.render('index');
  })

  router.get('/cameras', async (req, res) => {
    const cameras = await db.getCameras();
    res.render('cameras', { cameras });
  })

  router.post('/cameras/create', async (req, res) => {
    const { uuid } = req.body;
    await db.createCamera({ uuid });
    res.redirect('/cameras');
  });

  router.delete('/camera/:id', async (req, res) => {
    const { id } = req.params;
    await db.deleteCamera(id);
    res.redirect('/cameras');
  });

  return router;
}
