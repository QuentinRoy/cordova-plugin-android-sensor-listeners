var createRegistry = require('./registry');

/**
 * Event emitted from sensors.
 * @typedef {Object} SensorEvent
 * @property {string} sensor - The type of sensor that is listened to.
 * @property {string} sampling - The sampling period the sensor is listened to
 * by the receiving event listener.
 * @property {int} timeStamp - The time the event was emitted.
 * @property {float[]} values - The sensor values.
 */

/**
 * This callback is used to get responses from async calls. It complies with
 * nodeJS callback style.
 * @callback errorFirstCallback
 * @param {*} [err] - the error or undefined if everything went fine
 * @param {*} data - the response or the called function
 */

/**
 * This listener is used to receive events from sensors.
 * @callback sensorEventListener
 * @param {SensorEvent} evt - the event emitted by one of the sensor
 */


// Small utility that enables to answer to a node-style callback in a Promise-
// like fashion. Make sure the error and result arguments are properly
// distributed.
function unpromisify(nodeStyleCallback, f) {
  function resolve(r) {
    nodeStyleCallback && nodeStyleCallback(null, r);
  }
  function reject(error) {
    // The error cannot be undefined.
    nodeStyleCallback && nodeStyleCallback(error || new Error());
  }
  try {
    f(resolve, reject);
  } catch (error) {
    reject(error);
  }
}

var listenerRegistry = createRegistry();

/**
 * Called when a subscribed sensor sent an event.
 * 
 * @ignore
 * @param {SensorEvent} event - the event
 * @return {undefined} 
 */
function onSensorEvent(event) {
  if (!event.sensor) {
    throw new Error('onSensorEvent called with wrong event format.');
  }
  if (
    !listenerRegistry.getListenersCaller(event.sensor, event.sampling)(event)
  ) {
    throw new Error(
      'Received unexpected event from non subscribed sensor: ' + event.sensor
    );
  }
}

// Register the event callback.
document.addEventListener('deviceready', function() {
  // This handler is called when Java push data to this plugin.
  // It needs to be registered as soon as possible.
  var registrationCallback = function handleConfirmation(resp) {
    if (resp === 'registered') {
      // Once the confirmation has been received, this directly handle events.
      registrationCallback = onSensorEvent;
    } else {
      throw new Error(
        'Expecting confirmation for the callback registration but received: ' +
          resp
      );
    }
  };
  cordova.exec(
    function() {
      registrationCallback.apply(this, arguments);
    },
    function(e) {
      console.error(e.stack || e);
      throw e;
    },
    'Sensors',
    'registerCallback',
    []
  );
});


/**
 * Add a sensor listener.
 * 
 * @param {string} sensorType - the sensor type's constant name (as defined by
 * {@link https://developer.android.com/guide/topics/sensors/sensors_overview.html}
 * but without the "TYPE_" prefix)
 * @param {string} samplingRate - the sampling period's constant name (as
 * accepted by [SensorManager#registerListener]{@link https://developer.android.com/reference/android/hardware/SensorManager.html#registerListener(android.hardware.SensorEventListener,%20android.hardware.Sensor,%20int)}
 * without the "SENSOR_DELAY_" prefix)
 * @param {sensorEventListener} listener - the listener to register
 * @param {errorFirstCallback} [callback] - a node-style callback to notify the success or
 * failure of the operation.
 * @return {undefined}
 */
function addSensorListener(sensorType, samplingRate, listener, callback) {
  unpromisify(callback, function(resolve, reject) {
    if (listenerRegistry.addListener(sensorType, samplingRate, listener)) {
      // `listenerRegistry.addListener` returns true if there was no listener
      //  of this type when the listener has been added. In this case, we
      // subscribe to the sensor notifications.
      cordova.exec(
        resolve,
        function() {
          listenerRegistry.removeListener(sensorType, samplingRate, listener);
          reject.apply(this, arguments);
        },
        'Sensors',
        'subscribe',
        [sensorType, samplingRate]
      );
    } else {
      // If there is other listeners registered for this type, we immediately
      // resolve;
      resolve();
    }
  });
}

/**
 * Remove a sensor listener.
 * 
 * @param {string} sensorType - the type of the sensor
 * (see @link{addSensorListener})
 * @param {string} samplingRate  the sampling period
 * (see @link{addSensorListener})
 * @param {sensorEventListener} listener - the listener to remove
 * @param {errorFirstCallback} [callback] - a node-style callback to notify the
 * success or failure of the operation.
 * @return {undefined}
 */
function removeSensorListener(sensorType, samplingRate, listener, callback) {
  unpromisify(callback, function(resolve, reject) {
    if (listenerRegistry.removeListener(sensorType, samplingRate, listener)) {
      // `listenerRegistry.removeListener` returns true if this was the last
      // listener of its type. In this case, we unsubscribe.
      cordova.exec(resolve, reject, 'Sensors', 'unsubscribe', [sensorType]);
    } else {
      resolve();
    }
  });
}

module.exports.addSensorListener = addSensorListener;
module.exports.removeSensorListener = removeSensorListener;
