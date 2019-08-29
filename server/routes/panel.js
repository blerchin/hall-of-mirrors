const Router = require('express-promise-router');
const router = new Router();

module.exports = (commands, db) => {
  router.get('/', async (req, res) => {
    const layouts = await db.getLayouts();
    res.render('index', { layouts });
  })

  router.get('/layout/:id', async (req, res) => {
    const { id } = req.params;
    const layout = await db.getLayoutWithPositions(id);
    res.render('layout', { layout });
  });

  router.put('/layout/:layoutId/position/:positionId', async (req, res) => {
    const { layoutId, positionId } = req.params;
    const { rotation, translation } = req.body;
    await db.updatePosition(positionId, { rotation, translation });
    res.redirect(`/layout/${layoutId}`);
  });

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