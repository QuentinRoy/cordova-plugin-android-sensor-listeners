# Cordova Android Sensor Listeners Plugin

This plugin gives easy access to every available [Android's sensors](https://developer.android.com/guide/topics/sensors/sensors_overview.html).

## Install

```sh
cordova plugin add https://github.com/QuentinRoy/cordova-plugin-android-sensor-listeners.git
```

<a name="sensors"></a>

## API: sensors

* [sensors](#sensors) : <code>object</code>
    * [.addSensorListener(sensorType, samplingPeriod, listener, [callback])](#sensors.addSensorListener) ⇒ <code>undefined</code>
    * [.removeSensorListener(sensorType, samplingPeriod, listener, [callback])](#sensors.removeSensorListener) ⇒ <code>undefined</code>

<a name="sensors.addSensorListener"></a>

### sensors.addSensorListener(sensorType, samplingPeriod, listener, [callback]) ⇒ <code>undefined</code>
Add a sensor listener.

**Kind**: static method of [<code>sensors</code>](#sensors)  

| Param | Type | Description |
| --- | --- | --- |
| sensorType | <code>string</code> | The sensor type's constant name (as defined by [Android's Sensor](https://developer.android.com/guide/topics/sensors/sensors_overview.html), but without the prefix `"TYPE_"`). |
| samplingPeriod | <code>string</code> | The sampling period's constant name (as accepted by [SensorManager#registerListener](https://developer.android.com/reference/android/hardware/SensorManager.html#registerListener(android.hardware.SensorEventListener,%20android.hardware.Sensor,%20int)), but without the prefix `"SENSOR_DELAY_"`). |
| listener | [<code>sensorEventListener</code>](#sensorEventListener) | The listener to register. |
| [callback] | [<code>errorFirstCallback</code>](#errorFirstCallback) | A node-style callback to be called upon success or failure of the operation. |

**Example**  
```js
function listener(event) {
  console.log("device's rotation is " + event.values.join(","));
}

sensors.addSensorListener("ROTATION_VECTOR", "GAME", listener, function(error) {
  if (error) console.error("Could not listen to sensor");
});
```
<a name="sensors.removeSensorListener"></a>

### sensors.removeSensorListener(sensorType, samplingPeriod, listener, [callback]) ⇒ <code>undefined</code>
Remove a sensor listener.

**Kind**: static method of [<code>sensors</code>](#sensors)  

| Param | Type | Description |
| --- | --- | --- |
| sensorType | <code>string</code> | The type of the sensor as registered when the listener was added (see [addSensorListener](#sensors.addSensorListener)). |
| samplingPeriod | <code>string</code> | The sampling period as registered when the listener was added (see [addSensorListener](#sensors.addSensorListener)). |
| listener | [<code>sensorEventListener</code>](#sensorEventListener) | The listener to remove. |
| [callback] | [<code>errorFirstCallback</code>](#errorFirstCallback) | A node-style callback to be called upon success or failure of the operation. |

**Example**  
```js
sensors.removeSensorListener("ROTATION_VECTOR", "GAME", listener, function(error) {
  if (error) console.error("Could not stop listening to sensor");
});
```
<a name="SensorEvent"></a>

## SensorEvent : <code>Object</code>
Event emitted from sensors.

**Kind**: global typedef  
**Properties**

| Name | Type | Description |
| --- | --- | --- |
| sensor | <code>string</code> | The type of sensor that is listened to. |
| sampling | <code>string</code> | The sampling period the sensor is listened to by the receiving event listener. |
| timeStamp | <code>int</code> | The time the event was emitted. |
| values | <code>Array.&lt;float&gt;</code> | The sensor values. |

<a name="errorFirstCallback"></a>

## errorFirstCallback : <code>function</code>
This callback is used to get responses from async calls. It complies with
nodeJS callback style.

**Kind**: global typedef  

| Param | Type | Description |
| --- | --- | --- |
| [err] | <code>\*</code> | the error or undefined if everything went fine |
| data | <code>\*</code> | the response or the called function |

<a name="sensorEventListener"></a>

## sensorEventListener : <code>function</code>
This listener is used to receive events from sensors.

**Kind**: global typedef  

| Param | Type | Description |
| --- | --- | --- |
| evt | [<code>SensorEvent</code>](#SensorEvent) | the event emitted by one of the sensor |
