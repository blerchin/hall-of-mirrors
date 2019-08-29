const Router = require('express-promise-router');

const PHONE_HEIGHT = 146.8;
const PHONE_WIDTH = 70.9;
const PHONE_DEPTH = 8.1;

function drawPhone({ translation, rotation }) {
  return `
    translate([${translation}],
      rotate([${rotation}],
        union(
          color('black', cube([${PHONE_WIDTH}, ${PHONE_HEIGHT}, 1])),
          cube([${PHONE_WIDTH}, ${PHONE_HEIGHT}, ${PHONE_DEPTH}])
        )
      )
    )
  `;
}

module.exports = (commands, db) => {
  const router = new Router();
  router.get('/layout/:id.jscad', async (req, res) => {
    const { id } = req.params;
    const { positions } = await db.getLayoutWithPositions(id);
    const phones = positions.map(drawPhone);
    res.send(`
      function main() {
        return union(${phones});
      }
    `)
  });

  return router
};
