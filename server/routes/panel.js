const Router = require('express-promise-router');
const router = new Router();

router.get('/', (req, res) => {
  res.render('index');
})

module.exports = router;
