# Cordova Android Sensors Plugin

This plugin gives access to android sensors.

## Functions

<dl>
<dt><a href="#addSensorListener">addSensorListener(sensorType, samplingRate, listener, [callback])</a> ⇒ <code>undefined</code></dt>
<dd><p>Add a sensor listener.</p>
</dd>
<dt><a href="#removeSensorListener">removeSensorListener(sensorType, samplingRate, listener, [callback])</a> ⇒ <code>undefined</code></dt>
<dd><p>Remove a sensor listener.</p>
</dd>
</dl>

## Typedefs

<dl>
<dt><a href="#SensorEvent">SensorEvent</a> : <code>Object</code></dt>
<dd><p>Event emitted from sensors.</p>
</dd>
<dt><a href="#errorFirstCallback">errorFirstCallback</a> : <code>function</code></dt>
<dd><p>This callback is used to get responses from async calls. It complies with
nodeJS callback style.</p>
</dd>
<dt><a href="#sensorEventListener">sensorEventListener</a> : <code>function</code></dt>
<dd><p>This listener is used to receive events from sensors.</p>
</dd>
</dl>

<a name="addSensorListener"></a>

## addSensorListener(sensorType, samplingRate, listener, [callback]) ⇒ <code>undefined</code>
Add a sensor listener.

**Kind**: global function  

| Param | Type | Description |
| --- | --- | --- |
| sensorType | <code>string</code> | the sensor type's constant name (as defined by [https://developer.android.com/guide/topics/sensors/sensors_overview.html](https://developer.android.com/guide/topics/sensors/sensors_overview.html) but without the "TYPE_" prefix) |
| samplingRate | <code>string</code> | the sampling period's constant name (as accepted by [SensorManager#registerListener](https://developer.android.com/reference/android/hardware/SensorManager.html#registerListener(android.hardware.SensorEventListener,%20android.hardware.Sensor,%20int)) without the "SENSOR_DELAY_" prefix) |
| listener | [<code>sensorEventListener</code>](#sensorEventListener) | the listener to register |
| [callback] | [<code>errorFirstCallback</code>](#errorFirstCallback) | a node-style callback to notify the success or failure of the operation. |

<a name="removeSensorListener"></a>

## removeSensorListener(sensorType, samplingRate, listener, [callback]) ⇒ <code>undefined</code>
Remove a sensor listener.

**Kind**: global function  

| Param | Type | Description |
| --- | --- | --- |
| sensorType | <code>string</code> | the type of the sensor (see @link{addSensorListener}) |
| samplingRate | <code>string</code> | the sampling period (see @link{addSensorListener}) |
| listener | [<code>sensorEventListener</code>](#sensorEventListener) | the listener to remove |
| [callback] | [<code>errorFirstCallback</code>](#errorFirstCallback) | a node-style callback to notify the success or failure of the operation. |

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

