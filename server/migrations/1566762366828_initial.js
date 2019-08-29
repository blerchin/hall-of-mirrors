/* eslint-disable camelcase */

exports.shorthands = undefined;

exports.up = (pgm) => {
  pgm.createTable("layouts", {
    id: 'id',
    title: {
      type: 'varchar(1000)',
      notNull: true
    },
    createdAt: {
      type: 'timestamp',
      notNull: true,
      default: pgm.func('current_timestamp')
    }
  });

  pgm.createTable("positions", {
    id: 'id',
    layoutId: {
      type: 'integer',
      notNull: true,
      references: '"layouts"',
      onDelete: 'cascade'
    },
    translation: {
      type: 'numeric []',
      notNull: true
    },
    rotation: {
      type: 'numeric []',
      notNull: true
    }
  });

  pgm.createTable("cameras", {
    id: 'id',
    currentLayout: {
      type: 'integer',
      references: '"layouts"',
      onDelete: 'cascade'
    },
    currentPosition: {
      type: 'integer',
      references: '"positions"',
      onDelete: 'cascade'
    },
    uuid: {
      type: 'varchar(1000)',
      notNull: true,
      unique: true
    }
  });

  pgm.createTable("captures", {
    id: 'id',
    layoutId: {
      type: 'integer',
      notNull: true,
      references: '"layouts"',
      onDelete: 'cascade'
    }
  });

  pgm.createTable("frames", {
    id: 'id',
    captureId: {
      type: 'integer',
      notNull: true,
      references: '"captures"',
      onDelete: 'cascade'
    },
    layoutId: {
      type: 'integer',
      notNull: true,
      references: '"layouts"',
      onDelete: 'cascade'
    },
    positionId: {
      type: 'integer',
      notNull: true,
      references: '"positions"',
      onDelete: 'cascade'
    },
    s3Key: {
      type: 'varchar(1000)',
      notNull: true
    }
  });
  pgm.createIndex('frames', ['captureId', 'layoutId'], { method: 'btree' });
  pgm.createIndex('captures', 'layoutId', { method: 'btree' });
  pgm.createIndex('cameras', ['currentLayout', 'currentPosition'], { method: 'btree' });
  pgm.createIndex('positions', 'layoutId', { method: 'btree' });
};

exports.down = (pgm) => {

};
