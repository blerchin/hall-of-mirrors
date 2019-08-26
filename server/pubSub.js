module.exports = () => {
  const subscribers = {};
  let lastId = 0;
  return {
    subscribe: (cb, uuid = null) => {
      const id = uuid || lastId++;
      subscribers[id] = cb;
      return id;
    },
    send: (command, data, { targetIds } = {}) => {
      const ids = targetIds || Object.keys(subscribers);
      ids.forEach((k) => {
        const message = JSON.stringify({ command, data });
        subscribers[k](message);
      });
    },
    unsubscribe: (id) => {
      delete subscribers[id];
    }
  };
}
