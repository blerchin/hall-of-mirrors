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

const updateCamera = async (id, { currentLayout, currentPosition }) => {
  await pool.query(
    'UPDATE cameras SET "currentLayout" = $1, "currentPosition" = $2 WHERE id = $3',
    [currentLayout, currentPosition, id]
  );
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
    'SELECT captures.id, captures."createdAt", captures."layoutId", recent_frames."s3Key" FROM captures JOIN (SELECT DISTINCT ON ("captureId") * FROM frames ORDER BY "captureId", "createdAt" DESC) AS recent_frames '
    + ' ON captures.id = recent_frames."captureId" WHERE captures."layoutId" = $1 ORDER BY captures."createdAt" DESC',
    [layoutId]
  );
  return rows;
};

const getLatestCapture = async (layoutId) => {
  const { rows: captures } = await pool.query('SELECT * FROM captures WHERE "layoutId" = $1 ORDER BY "createdAt" DESC LIMIT 1', [layoutId]);
  const { rows: frames } = await pool.query('SELECT * FROM frames WHERE "captureId" = $1', [captures[0].id]);
  return {
    ...captures[0],
    frames
  };
}

const getCaptureWithFrames = async (captureId) => {
  const { rows: captures } = await pool.query('SELECT * FROM captures WHERE id = $1', [captureId]);
  const { rows: frames } = await pool.query('SELECT * FROM frames WHERE "captureId" = $1', [captureId]);
  return {
    ...captures[0],
    frames
  };
}

const createLayout = async (title) => {
  const { rows } = await pool.query('INSERT INTO layouts(title) VALUES($1) RETURNING id', [title])
  return rows[0].id;
}

const getLayouts = async () => {
  const { rows } = await pool.query(
    'SELECT layouts.id, layouts.title, COUNT(cameras.id) AS count_cameras FROM layouts LEFT OUTER JOIN cameras ON layouts.id = "cameras"."currentLayout" GROUP BY layouts.id ORDER BY count_cameras DESC'
  );
  return rows;
};

const getLayoutWithPositions = async (id) => {
  const { rows: layoutRows } = await pool.query('SELECT * FROM layouts WHERE id = $1', [id]);
  const { rows: positionRows } = await pool.query(
    'SELECT positions.id, positions.translation, positions.rotation, cameras.id AS camera_id, cameras.uuid AS camera_uuid FROM positions LEFT JOIN cameras ON positions.id = "cameras"."currentPosition" WHERE "layoutId" = $1 ORDER BY positions."createdAt" ASC',
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
  createLayout,
  createPosition,
  updatePosition,
  deleteCamera,
  updateCamera,
  getCameras,
  getCaptures,
  getLatestCapture,
  getCaptureWithFrames,
  getLayouts,
  getLayoutWithPositions,
  getUUIDsByLayoutId,
  pool
};
