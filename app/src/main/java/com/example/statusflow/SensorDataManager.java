package com.example.statusflow;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class SensorDataManager implements SensorEventListener {

    private static final String TAG = "SensorDataManager";
    private final SensorManager sensorManager;
    private final Sensor accelerometer;
    private final AtomicReference<Triple<Float, Float, Float>> currentAccelerometerData = new AtomicReference<>();
    private final AtomicBoolean isListening = new AtomicBoolean(false);

    public SensorDataManager(Context context) {
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }

    public boolean isListening() {
        return isListening.get();
    }

    public Triple<Float, Float, Float> getCurrentAccelerometerData() {
        return currentAccelerometerData.get();
    }

    public void startListening() {
        if (!isListening()) {
            if (accelerometer != null) {
                sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                isListening.set(true);
                Log.d(TAG, "startListening");
            } else {
                Log.e(TAG, "Accelerometer not available!");
            }
        }
    }

    public void stopListening() {
        if (isListening()) {
            sensorManager.unregisterListener(this);
            isListening.set(false);
            Log.d(TAG, "stopListening");
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            currentAccelerometerData.set(new Triple<>(x, y, z));
            Log.d(TAG, "Accelerometer - X: " + x + ", Y: " + y + ", Z: " + z);
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "Accuracy changed for sensor: " + sensor.getName() + ", Accuracy: " + accuracy);
    }
}