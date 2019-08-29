const { pool } = require('./index');

const layouts = require('./layouts.json');

const seedLayouts = () => Promise.all(Object.keys(layouts).map(async (title) => {
  const client = await pool.connect();
  const positions = layouts[title];
  try {
    await client.query('BEGIN');
    const { rows: layoutRows } = await client.query(
      'INSERT INTO layouts(title) VALUES($1) RETURNING id',
      [title]
    );

    await Promise.all(positions.map(async (position) => {
      const { rows: positionRows } = await client.query(
        'INSERT INTO positions("layoutId", translation, rotation) VALUES($1, $2, $3) RETURNING id',
        [layoutRows[0].id, position.translation, position.rotation]
      );
      if (position.uuid) {
        await client.query(
          'INSERT INTO cameras("currentLayout", "currentPosition", uuid) VALUES($1, $2, $3)',
          [layoutRows[0].id, positionRows[0].id, position.uuid]
        );
      }
    }));
    await client.query('COMMIT');
  } catch (e) {
    await client.query('ROLLBACK');
    throw e
  } finally {
    client.release();
  }
}));


seedLayouts().then(() => console.log('Seeding completed successfully'))
  .catch((e) => console.error(e.stack))
  .finally(() => process.exit());
