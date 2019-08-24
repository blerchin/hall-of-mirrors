const { Pool } = require('pg');

const pool = new Pool({ connectionString: ENV['DATABASE_URL']});

const createFrame = ({ captureId, positionId, s3Key }) => {
  pool.query(
    'INSERT INTO frames(captureId, positionId, s3Key) VALUES($1, $2, $3)',
    [data.captureId, data.locationId, data.s3Key],
    (err, res) { if (err) { console.log(err) } }
  );
}

module.exports = {
  createFrame
};
