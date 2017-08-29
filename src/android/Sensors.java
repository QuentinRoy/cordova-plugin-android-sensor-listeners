package fr.quentinroy.plugin;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaInterface;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CordovaWebView;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Sensors extends CordovaPlugin {

    // Receives sensor updates.
    private CallbackContext eventCallback;
    // Register the listeners with a hash created with getHash.
    private Map<String, SensorEventListener> listeners = new HashMap<String, SensorEventListener>();
    // The activity's sensor manager.
    private SensorManager sensorManager;

    /**
     * Sets the context of the Command. This can then be used to do things like
     * get file paths associated with the Activity.
     *
     * @param cordova The context of the main Activity.
     * @param webView The CordovaWebView Cordova is running in.
     */
    public void initialize(CordovaInterface cordova, CordovaWebView webView) {
        super.initialize(cordova, webView);
        this.sensorManager = (SensorManager) cordova
                .getActivity()
                .getSystemService(Context.SENSOR_SERVICE);
    }

    /**
     * Executes the request and returns PluginResult.
     *
     * @param action          The action to execute.
     * @param args            JSONArry of arguments for the plugin.
     * @param callbackContext The callback id used when calling back into JavaScript.
     * @return True if the action was valid.
     * @throws JSONException
     */
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("registerCallback")) {
            registerCallback(args, callbackContext);
        } else if (action.equals("subscribe")) {
            subscribe(args, callbackContext);
        } else if (action.equals("unsubscribe")) {
            unsubscribe(args, callbackContext);
        } else {
            return false;
        }
        return true;
    }

    /**
     * This register the callback that should be called when a sensor event is received (any sensor
     * event).
     *
     * @param args            The arguments of the registration call. Currently ignored.
     * @param callbackContext The callback context to register as callback for every events.
     */
    private void registerCallback(JSONArray args, CallbackContext callbackContext)
            throws JSONException {
        if (this.eventCallback != null) {
            callbackContext.error("Callback already exist");
        }
        this.eventCallback = callbackContext;
        // Send the expected confirmation (JS is expecting for this).
        PluginResult result = new PluginResult(
                PluginResult.Status.OK,
                "registered"
        );
        result.setKeepCallback(true);
        callbackContext.sendPluginResult(result);
    }

    /**
     * Subscribed to a sensor event. Following this call, the registered callback will be called
     * each time a new value is received from the corresponding sensor.
     *
     * @param args            The subscription arguments. Must contains two strings:
     *                        - the sensor type's constant name (as defined by
     *                        https://developer.android.com/guide/topics/sensors/sensors_overview.html
     *                        but without the "TYPE_" prefix)
     *                        - the sampling period's constant name (as accepted by registerLister
     *                        https://developer.android.com/reference/android/hardware/SensorManager.html#registerListener(android.hardware.SensorEventListener,%20android.hardware.Sensor,%20int)
     *                        without the "SENSOR_DELAY_" prefix.
     * @param callbackContext The callback context to receive confirmation of the subscription.
     * @throws JSONException Thrown when the arguments cannot be properly parsed.
     */
    private void subscribe(JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (this.eventCallback == null) {
            callbackContext.error("Cannot subscribe to anything before registering a callback");
        }

        // Fetch and check the target sensor.
        final String sensorTypeName = args.getString(0);
        int sensorType;
        try {
            sensorType = Sensor.class
                    .getDeclaredField("TYPE_" + sensorTypeName)
                    .getInt(Sensor.class);
        } catch (NoSuchFieldException e) {
            callbackContext.error("Unavailable sensor type: " + sensorTypeName);
            return;
        } catch (IllegalAccessException e) {
            callbackContext.error("Unavailable sensor type: " + sensorTypeName);
            return;
        }
        List<Sensor> sensorList = this.sensorManager.getSensorList(sensorType);
        if (sensorList == null || sensorList.size() == 0) {
            callbackContext.error("Sensor " + sensorTypeName + " could not be retrieved");
            return;
        }

        // Fetch and check the target sampling rate.
        final String samplingPeriodName = args.getString(1);
        int samplingPeriodUs;
        try {
            samplingPeriodUs = SensorManager.class
                    .getDeclaredField("SENSOR_DELAY_" + samplingPeriodName)
                    .getInt(SensorManager.class);
        } catch (NoSuchFieldException e) {
            callbackContext.error("Unavailable sampling period: " + samplingPeriodName);
            return;
        } catch (IllegalAccessException e) {
            callbackContext.error("Unavailable sampling period: " + samplingPeriodName);
            return;
        }

        String hash = getListenerHash(sensorTypeName, samplingPeriodName);

        if (listeners.containsKey(hash)) {
            // If that sensor is already subscribed to, there is nothing to do here.
            callbackContext.error(
                    "Listener already registered for sensor " +
                            sensorTypeName +
                            " at sampling period " +
                            samplingPeriodName
            );
            return;
        }

        // Create the event listener.
        SensorEventListener listener = new SensorEventListener() {
            @Override
            public void onSensorChanged(SensorEvent event) {
                Sensors.this.onSensorChanged(
                        sensorTypeName,
                        samplingPeriodName,
                        event
                );
            }

            @Override
            public void onAccuracyChanged(Sensor sensor, int accuracy) {
                // Just in case we might want to do something with this one day.
                Sensors.this.onAccuracyChanged(
                        sensorTypeName,
                        samplingPeriodName,
                        sensor,
                        accuracy
                );
            }
        };
        listeners.put(hash, listener);
        sensorManager.registerListener(listener, sensorList.get(0), samplingPeriodUs);
        callbackContext.success();
    }

    /**
     * Unsubscribe from a sensor.
     *
     * @param args            The arguments. Must contains one string: the type of the sensor
     *                        and the listener sampling period as specified when calling
     *                        {@link Sensors#subscribe}.
     * @param callbackContext The callback context to receive confirmation of the un-subscription.
     * @throws JSONException Thrown when the arguments cannot be properly parsed.
     */
    private void unsubscribe(JSONArray args, CallbackContext callbackContext) throws JSONException {
        String hash = getListenerHash(args.getString(0), args.getString(1));
        SensorEventListener listener = listeners.remove(hash);
        sensorManager.unregisterListener(listener);
        if (listener != null) {
            callbackContext.success();
        } else {
            callbackContext.error(
                    "No listener found for sensor " +
                            args.getString(0) +
                            " at sampling period " +
                            args.getString(1)
            );
        }
        callbackContext.success(listener == null ? 0 : 1);
    }


    /**
     * @param sensor   The type of the sensor.
     * @param sampling The name of the sampling period.
     * @return a hash to store the corresponding listener in listeners.
     */
    private static String getListenerHash(String sensor, String sampling) {
        // This is safe, neither sensor nor sampling can contain ':'
        return sensor + ':' + sampling;
    }

    /**
     * Receive sensor events.
     *
     * @param sensorTypeName     The type of the sensor.
     * @param samplingPeriodName The name of the sampling period.
     * @param event              The event.
     */
    private void onSensorChanged(
            String sensorTypeName,
            String samplingPeriodName,
            SensorEvent event
    ) {
        try {
            // Convert the sensor event into JSON.
            JSONObject eventArg = new JSONObject();
            JSONArray values = new JSONArray();
            for (int i = 0; i < event.values.length; i++) {
                values.put(event.values[i]);
            }
            eventArg.put("values", values);
            eventArg.put("timeStamp", event.timestamp);
            eventArg.put("sensor", sensorTypeName);
            eventArg.put("sampling", samplingPeriodName);

            // Send the result.
            PluginResult result = new PluginResult(
                    PluginResult.Status.OK,
                    eventArg
            );
            result.setKeepCallback(true);
            eventCallback.sendPluginResult(result);
        } catch (Exception e) {
            // If anything bad happens we notify JS and print the stack.
            PluginResult result = new PluginResult(PluginResult.Status.ERROR, e.getMessage());
            result.setKeepCallback(true);
            eventCallback.sendPluginResult(result);
            e.printStackTrace();
        }
    }

    // Currently ignored.
    private void onAccuracyChanged(
            String sensorTypeName,
            String samplingPeriodName,
            Sensor sensor,
            int accuracy) {
    }

    /**
     * Called when listener is to be shut down and object is being destroyed.
     */
    public void onDestroy() {
        this.stop();
    }

    private void stop() {
        // Clear the listeners and the eventCallback.
        for (SensorEventListener listener : listeners.values()) {
            this.sensorManager.unregisterListener(listener);
        }
        listeners.clear();
        eventCallback = null;
    }

    /**
     * Called when app has navigated and JS listeners have been destroyed.
     */
    public void onReset() {
        this.stop();
    }
}
