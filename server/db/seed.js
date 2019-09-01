const { pool } = require('./index');

const layouts = require('./layouts.json');
const captures = require('./captures.json');

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

const seedCaptures = () => Promise.all(captures.map(async (capture) => {
  const client = await pool.connect();
  const { layoutId, frames } = capture;
  try {
    await client.query('BEGIN');
    const { rows: captures } = await client.query(
      'INSERT INTO captures("layoutId") VALUES($1) RETURNING id',
      [layoutId]
    );
    const captureId = captures[0].id;

    await Promise.all(frames.map(async (frame) => {
      await client.query(
        'INSERT INTO frames("layoutId", "positionId", "captureId", "s3Key") VALUES($1, $2, $3, $4)',
        [layoutId, frame.positionId, captureId, frame.s3Key]
      );
    }))

    await client.query('COMMIT');
  } catch (e) {
    await client.query('ROLLBACK');
    throw e
  } finally {
    client.release();
  }
}));


seedLayouts()
  .then(() => seedCaptures())
  .then(() => console.log('Seeding completed successfully'))
  .catch((e) => console.error(e.stack))
  .finally(() => process.exit());
