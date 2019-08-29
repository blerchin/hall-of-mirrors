const Router = require('express-promise-router');

module.exports = (commands, db) => {
  const router = new Router();
  router.post('/layout/:id/capture', async (req, res) => {
    const captureId = await db.createCapture({ layoutId: req.params.id });
    const uuids = await db.getUUIDsByLayoutId(req.params.id);
    commands.send('capture:now', { captureId }, { targetIds: uuids });
    res.sendStatus(200);
  });

  return router;
}
