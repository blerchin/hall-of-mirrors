const Router = require('express-promise-router');
const router = new Router();

module.exports = (commands, db) => {
  router.post('/layout/:id/capture', async (req, res) => {
    const captureId = await db.createCapture({ layoutId: req.params.id });
    const uuids = await db.getUuidsByLayoutId(req.params.id);
    commands.send('capture:now', { captureId }, { targetIds: uuids });
    res.sendStatus(200);
  });

  return router;
}
