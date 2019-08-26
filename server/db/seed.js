const { pool } = require('./index');

const layouts = require('./layouts.json');
const cameras = require('./cameras.json');

const seedLayouts = () => Promise.all(Object.keys(layouts).map(async (title) => {
  const client = await pool.connect();
  const positions = layouts[title];
  try {
    await client.query('BEGIN');
    const { rows } = await client.query(
      'INSERT INTO layouts(title) VALUES($1) RETURNING id',
      [title]
    );

    await Promise.all(positions.map(async (position) => {
      client.query(
        'INSERT INTO positions("layoutId", translation, rotation) VALUES($1, $2, $3)',
        [rows[0].id, position.translation, position.rotation]
      );
    }));
    await client.query('COMMIT');
  } catch (e) {
    await client.query('ROLLBACK');
    throw e
  } finally {
    client.release();
  }
}));

const seedCameras = () => Promise.all(cameras.map( async ({ uuid }) => {
  await pool.query('INSERT INTO cameras(uuid) VALUES($1)', [uuid]);
}));

seedLayouts()
  .then(() => seedCameras())
  .then(() => console.log('Seeding completed successfully'))
  .catch((e) => console.error(e.stack))
  .finally(() => process.exit());
