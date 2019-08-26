require('dotenv').config();

const { Pool } = require('pg');

const pool = new Pool({ connectionString: process.env['DATABASE_URL']});

const createFrame = async ({ captureId, uuid, s3Key }) => {
  const { rows } = await pool.query(
    'SELECT currentPosition FROM cameras WHERE uuid = $1',
    [uuid]
  );
  await pool.query(
    'INSERT INTO frames(captureId, positionId, s3Key) VALUES($1, $2, $3)',
    [data.captureId, rows[0].currentPosition, data.s3Key]
  );
};

const createPosition = async ({ layoutId, rotation, translation}) => {
  const { rows } = await pool.query(
    'INSERT INTO positions("layoutId", rotation, translation) VALUES($1, $2, $3) RETURNING id',
    [layoutId, rotation, translation]
  );
  return rows[0].id;
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
  const { rows } = pool.query(
    'INSERT INTO captures(layoutId) VALUES($1) RETURNING id',
    [layoutId]
  );
  return rows[0].id;
};

const getUUIDsByLayoutId = async (layoutId) => {
  const { rows } = await pool.query(
    'SELECT uuid FROM cameras WHERE "cameras.layoutId" = $1',
    [layoutId]
  );
  return rows.map((r) => r.uuid);
}

module.exports = {
  createCamera,
  createCapture,
  createFrame,
  createPosition,
  deleteCamera,
  getCameras,
  getUUIDsByLayoutId,
  pool
};
