// Create a sensor listener registry.
module.exports = function createRegistry() {
  // Register the sensor listeners. The keys are the sensor type names, values
  // an array of listeners.
  var listeners = {};

  function getHash(sensor, sampling) {
    // This is a safe. Indeed both sensor or sampling are directly mapped to Java property
    // names and ':' is a forbidden character for JAVA property names.
    return sensor + ':' + sampling;
  }

  /**
   * Add a listener in the registry.
   *
   * @param {string} sensorType the sensor type
   * @param {string} samplingRate the sampling rate
   * @param {function} listener the listener to add
   * @return {boolean} true if there was not registered listener of this type before
   * the listener is added.
   */
  function addListener(sensorType, samplingRate, listener) {
    // Look for the corresponding listener list and create it if it does not
    // exists.
    var hash = getHash(sensorType, samplingRate);
    var sensorListeners = listeners[hash];
    if (!sensorListeners) {
      listeners[hash] = sensorListeners = [listener];
      return true;
    }
    sensorListeners.push(listener);
    return false;
  }

  /**
   * Remove a listener from the listeners registry.
   *
   * @param {string} sensorType the sensor type name the listener is listening
   * to
   * @param {string} samplingRate the sampling rate this listener is listening
   * at
   * @param {function} listener the listener
   * @return {boolean} true if the listener was the last of its type
   */
  function removeListener(sensorType, samplingRate, listener) {
    var hash = getHash(sensorType, samplingRate);
    // Get the corresponding listener list.
    var sensorListeners = listeners[hash];
    // If it is undefined, we are done and return false to notify that
    // the listener was not the last of his type (since it was not found).
    if (!sensorListeners) return false;
    // Look for the listener in the list and remove it if found.
    var i = sensorListeners.indexOf(listener);
    // Return false if it was not found.
    if (i < 0) return false;
    if (sensorListeners.length <= 1) {
      // If it was the only listener, we remove the corresponding list and
      // return true that it was the last of its type.
      listeners[hash] = undefined;
      return true;
    }
    sensorListeners.splice(i, 1);
    return false;
  }

  /**
   * Check if a listener has been registered.
   *
   * @param {string} sensorType the sensor type name the listener is listening
   * to
   * @param {string} samplingRate the sampling rate this listener is listening
   * at
   * @param {function} listener the listener
   * @return {boolean} true if the registry contains the listener, false
   * otherwise.
   */
  function containsListener(sensorType, samplingRate, listener) {
    var hash = getHash(sensorType, samplingRate);
    var sensorListeners = listeners[hash];
    return !!sensorListeners && sensorListeners.indexOf(listener) >= 0;
  }

  /**
   * @param {string} sensorType the sensor type name the listener is listening
   * to
   * @param {string} samplingRate the sampling rate this listener is listening
   * at
   * @param {*} context the context of the caller
   * @return {func} a function that calls every corresponding listeners
   * (with the arguments it is called with and using the provided context).
   * The function returns false if no listeners has been called, true otherwise.
   */
  function getListenersCaller(sensorType, samplingRate, context) {
    var hash = getHash(sensorType, samplingRate);
    return function listenersCaller() {
      var sensorListeners = listeners[hash];
      if (!sensorListeners || sensorListeners.length === 0) return false;
      for (var i = 0; i < sensorListeners.length; i++) {
        sensorListeners[i].apply(context, arguments);
      }
      return true;
    };
  }

  /**
   * @return {boolean} true if the registry is empty
   */
  function isEmpty() {
    var registeredSensorTypes = Object.getOwnPropertyNames(listeners);
    for (var i = 0; i < registeredSensorTypes.length; i++) {
      var sensorListeners = listeners[registeredSensorTypes[i]];
      if (sensorListeners && sensorListeners.length > 0) {
        return false;
      }
    }
    return true;
  }

  return {
    addListener: addListener,
    removeListener: removeListener,
    containsListener: containsListener,
    getListenersCaller: getListenersCaller,
    isEmpty: isEmpty
  };
};
