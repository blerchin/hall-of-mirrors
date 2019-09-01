require('dotenv').config();

const { Pool } = require('pg');

const pool = new Pool({ connectionString: process.env['DATABASE_URL']});

const createFrame = async ({ captureId, uuid, s3Key }) => {
  const { rows } = await pool.query(
    'SELECT "currentPosition", "currentLayout" FROM cameras WHERE uuid = $1',
    [uuid]
  );
  const { currentLayout, currentPosition } = rows[0];
  await pool.query(
    'INSERT INTO frames("captureId", "layoutId", "positionId", "s3Key") VALUES($1, $2, $3, $4)',
    [captureId, currentLayout, currentPosition, s3Key]
  );
};

const createPosition = async ({ layoutId, rotation, translation}) => {
  const { rows } = await pool.query(
    'INSERT INTO positions("layoutId", rotation, translation) VALUES($1, $2, $3) RETURNING id',
    [layoutId, rotation, translation]
  );
  return rows[0].id;
}

const updatePosition = async (id, attr) => {
  const { rotation, translation } = attr;
  await pool.query(
    'UPDATE positions SET rotation = $1, translation = $2 WHERE id = $3',
    [rotation, translation, id]
  );
}

const createCamera = async ({ uuid }) => {
  const { rows } = await pool.query(
    'INSERT INTO cameras(uuid) VALUES($1) RETURNING id',
    [uuid]
  );
  return rows[0].id
}

const deleteCamera = (id) => pool.query('DELETE FROM cameras WHERE id = $1', [id]);

const getCameras = async () => {
  const { rows } = await pool.query('SELECT * FROM cameras');
  return rows;
}

const createCapture = async ({ layoutId }) => {
  const { rows } = await pool.query(
    'INSERT INTO captures("layoutId") VALUES($1) RETURNING id',
    [layoutId]
  );
  return rows[0].id;
};

const getCaptures = async (layoutId) => {
  const { rows } = await pool.query(
    'SELECT captures.id, captures."createdAt", frames."positionId", frames."s3Key", frames."createdAt" FROM captures INNER JOIN frames ON captures.id = frames."captureId" WHERE captures."layoutId" = $1',
    [layoutId]
  );
  return rows;
};

const getLayouts = async () => {
  const { rows } = await pool.query(
    'SELECT layouts.id, layouts.title, COUNT(cameras.id) AS count_cameras FROM layouts LEFT OUTER JOIN cameras ON layouts.id = "cameras"."currentLayout" GROUP BY layouts.id ORDER BY count_cameras DESC'
  );
  return rows;
};

const getLayoutWithPositions = async (id) => {
  const { rows: layoutRows } = await pool.query('SELECT * FROM layouts WHERE id = $1', [id]);
  const { rows: positionRows } = await pool.query(
    'SELECT positions.id, positions.translation, positions.rotation, cameras.id AS camera_id, cameras.uuid AS camera_uuid FROM positions LEFT JOIN cameras ON positions.id = "cameras"."currentPosition" WHERE "layoutId" = $1',
    [id]
  );
  return {
    ...layoutRows[0],
    positions: positionRows
  }
}

const getUUIDsByLayoutId = async (layoutId) => {
  const { rows } = await pool.query(
    'SELECT uuid FROM cameras WHERE cameras."currentLayout" = $1',
    [layoutId]
  );
  return rows.map((r) => r.uuid);
}

module.exports = {
  createCamera,
  createCapture,
  createFrame,
  createPosition,
  updatePosition,
  deleteCamera,
  getCameras,
  getCaptures,
  getLayouts,
  getLayoutWithPositions,
  getUUIDsByLayoutId,
  pool
};
