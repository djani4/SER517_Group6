package com.example.statusflow;

import android.Manifest;
import android.app.AppOpsManager;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.BatteryManager;
import android.os.PowerManager;
import android.provider.CalendarContract;
import android.util.Log;

import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class PhoneDataCollector implements SensorEventListener {
    private static final String TAG = "PhoneDataCollector";
    private final Context context;
    private final SensorManager sensorManager;
    private final Sensor lightSensor;
    private final Sensor accelerometer;
    private final AtomicReference<Float> currentLightLevel = new AtomicReference<>();
    private final AtomicReference<Triple<Float, Float, Float>> currentAccelerometerData = new AtomicReference<>();

    private final DeviceStateManager deviceStateManager;
    private final ActivityDataManager activityDataManager;

    private boolean isListening = false;

    public PhoneDataCollector(Context context) {
        this.context = context;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        this.accelerometer = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        this.deviceStateManager = new DeviceStateManager(context);
        this.activityDataManager = new ActivityDataManager(context);
        currentLightLevel.set(null);
        currentAccelerometerData.set(null);
    }

    public void startListening() {
        if (!isListening) {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.BODY_SENSORS) == PackageManager.PERMISSION_GRANTED) {
                if (lightSensor != null) {
                    sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
                    Log.d(TAG, "Light sensor listener registered");
                }
                if (accelerometer != null) {
                    sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
                    Log.d(TAG, "Accelerometer sensor listener registered");
                }
            } else {
                Log.e(TAG, "Missing permission BODY_SENSORS to register sensor listeners");
            }
            isListening = true;
        }
    }

    public void stopListening() {
        if (isListening) {
            sensorManager.unregisterListener(this);
            isListening = false;
            Log.d(TAG, "Sensor listener unregistered");
        }
    }

    public PhoneData collectData() {
        boolean isSilent = deviceStateManager.isSilentModeOn();
        boolean isScreenOn = deviceStateManager.isScreenOn();
        int batteryLevel = deviceStateManager.getBatteryLevel();

        List<String> recentApps = hasUsageStatsPermission() ? activityDataManager.getRecentAppUsage() : new ArrayList<>();

        boolean hasEvent = ContextCompat.checkSelfPermission(context, Manifest.permission.READ_CALENDAR) == PackageManager.PERMISSION_GRANTED && activityDataManager.hasCalendarEventNow();

        return new PhoneData(
                currentLightLevel.get(),
                isSilent,
                isScreenOn,
                batteryLevel,
                recentApps,
                hasEvent,
                currentAccelerometerData.get()
        );
    }

    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(
                AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), context.getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }

    public Triple<Float, Float, Float> getCurrentAccelerometerData() {
        return currentAccelerometerData.get();
    }

    public Float getCurrentLightLevel() {
        return currentLightLevel.get();
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            currentLightLevel.set(event.values[0]);
        } else if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            float x = event.values[0];
            float y = event.values[1];
            float z = event.values[2];
            currentAccelerometerData.set(new Triple<>(x, y, z));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        Log.d(TAG, "Accuracy changed for sensor: " + sensor.getName() + ", Accuracy: " + accuracy);
    }
}

class DeviceStateManager {
    private final Context context;
    private final AudioManager audioManager;
    private final PowerManager powerManager;
    private final BatteryManager batteryManager;

    public DeviceStateManager(Context context) {
        this.context = context;
        this.audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        this.powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
        this.batteryManager = (BatteryManager) context.getSystemService(Context.BATTERY_SERVICE);
    }

    public boolean isSilentModeOn() {
        return audioManager.getRingerMode() != AudioManager.RINGER_MODE_NORMAL;
    }

    public boolean isScreenOn() {
        return powerManager.isInteractive();
    }

    public int getBatteryLevel() {
        return batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY);
    }
}


class ActivityDataManager {
    private final Context context;
    private final UsageStatsManager usageStatsManager;

    public ActivityDataManager(Context context) {
        this.context = context;
        this.usageStatsManager = (UsageStatsManager) context.getSystemService(Context.USAGE_STATS_SERVICE);
    }

    public List<String> getRecentAppUsage() {
        long endTime = System.currentTimeMillis();
        long startTime = endTime - (60 * 1000);

        List<String> recentApps = new ArrayList<>();
        List<android.app.usage.UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, startTime, endTime);
        if (usageStatsList != null) {
            usageStatsList.sort((a, b) -> Long.compare(b.getLastTimeUsed(), a.getLastTimeUsed()));
            for (android.app.usage.UsageStats usageStats : usageStatsList) {
                recentApps.add(usageStats.getPackageName());
            }
        }
        return recentApps;
    }

    public boolean hasCalendarEventNow() {
        String[] projection = new String[]{CalendarContract.Events.TITLE};
        String selection = "(" + CalendarContract.Events.DTSTART + " <= ?) AND (" + CalendarContract.Events.DTEND + " >= ?)";
        long now = System.currentTimeMillis();
        String[] selectionArgs = new String[]{String.valueOf(now), String.valueOf(now)};

        try (android.database.Cursor cursor = context.getContentResolver().query(
                CalendarContract.Events.CONTENT_URI,
                projection,
                selection,
                selectionArgs,
                null
        )) {
            return cursor != null && cursor.getCount() > 0;
        }
    }
}