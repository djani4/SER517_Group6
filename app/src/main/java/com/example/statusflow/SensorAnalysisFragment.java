package com.example.statusflow;


import android.app.NotificationManager;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.PowerManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.HashMap;
import java.util.Map;


public class SensorAnalysisFragment extends Fragment implements SensorEventListener {

    private static final String TAG = "SensorAnalysisFragment";
    private static final long UPDATE_INTERVAL = 500; // Update every 500 milliseconds (adjust as needed)
    private DataViewModel dataViewModel;
    private TextView sensorDataTextView;
    private Button sendToApiButton;
    private SensorManager sensorManager;
    private Sensor lightSensor;
    private Sensor accelerometerSensor;
    private Sensor rotationVectorSensor;
    private float lightValue;
    private float[] accelerometerValues = new float[3];
    private float[] rotationVectorValues = new float[4];
    private int ringerMode;
    private String userActivityType = "";
    private String screenValue = "";
    private float doNotDisturb;
    private String appValue = "";
    private String formattedResponse = "";
    private NotificationManager notificationManager;
    private AudioManager audioManager;
    private PowerManager powerManager;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataViewModel = new ViewModelProvider(requireActivity()).get(DataViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sensor_analysis, container, false);
        sensorDataTextView = view.findViewById(R.id.dataTextView);
        Log.d(TAG, "sensorDataTextView is null: " + (sensorDataTextView == null));
        sendToApiButton = view.findViewById(R.id.sendToApiButton);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Clear the TextView initially
        sensorDataTextView.setText("");
        if (dataViewModel.getIsListeningSensors().getValue()) {
            startListeningSensors();
        }
        sendToApiButton.setOnClickListener(v -> {
            dataViewModel.changeMode(AnalysisMode.SENSOR);
            dataViewModel.sendAnalyzedDataToApi(getContext());
        });
        // Observe and update the same TextView
        dataViewModel.getApiResponse().observe(getViewLifecycleOwner(), response -> {
            if (!response.isEmpty()) {
                String formattedResponse = extractAndFormatResponse(response);
                appendApiResponse(formattedResponse); // Append the response
            }
        });
    }

    private void appendApiResponse(String response) {
        Log.d(TAG, "appendApiResponse() called with response: " + response);
        sensorDataTextView.setText(response);
    }


    private String extractAndFormatResponse(String jsonString) {
        try {
            Gson gson = new Gson();
            JsonObject jsonObject = JsonParser.parseString(jsonString).getAsJsonObject();

            // Navigate to the content string
            JsonArray choices = jsonObject.getAsJsonArray("choices");
            JsonObject firstChoice = choices.get(0).getAsJsonObject();
            JsonObject message = firstChoice.getAsJsonObject("message");
            String content = message.get("content").getAsString();
            String formattedContent = content.replace("\\u003d", "=");
            // Add empty lines before "Prediction" and "Reason"
            formattedContent = formattedContent.replace("Prediction:", "\n\nPrediction:");
            formattedContent = formattedContent.replace("Reason:", "\n\nReason:");
            return formattedContent;
        } catch (Exception e) {
            Log.e(TAG, "Error parsing API response", e);
            return "Error parsing API response: " + e.getMessage();
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        Log.d(TAG, "onSensorChanged() called");
        if (!dataViewModel.getIsListeningSensors().getValue()) {
            Log.d(TAG, "Sensors are not being listened to, exiting onSensorChanged()");
            return;
        }
        Sensor sensor = event.sensor;
        if (sensor.getType() == Sensor.TYPE_LIGHT) {
            Log.d(TAG, "Light sensor value: " + event.values[0]);
            lightValue = event.values[0];
        } else if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            Log.d(TAG, "Accelerometer sensor values: " + event.values[0] + ", " + event.values[1] + ", " + event.values[2]);
            System.arraycopy(event.values, 0, accelerometerValues, 0, 3);
        } else if (sensor.getType() == Sensor.TYPE_ROTATION_VECTOR) {
            Log.d(TAG, "Rotation vector sensor values: " + event.values[0] + ", " + event.values[1] + ", " + event.values[2] + ", " + event.values[3]);
            System.arraycopy(event.values, 0, rotationVectorValues, 0, 4);
        }
        // Get Ringer Mode
        ringerMode = audioManager.getRingerMode();
        //Check DND status
        int filter = notificationManager.getCurrentInterruptionFilter();

        switch (filter) {
            case NotificationManager.INTERRUPTION_FILTER_ALL:
                doNotDisturb = 1.0f; // All notifications allowed
                break;
            case NotificationManager.INTERRUPTION_FILTER_PRIORITY:
                doNotDisturb = 2.0f; // Priority notifications only
                break;
            case NotificationManager.INTERRUPTION_FILTER_NONE:
                doNotDisturb = 3.0f; // Total silence (alarms only)
                break;
            case NotificationManager.INTERRUPTION_FILTER_ALARMS:
                doNotDisturb = 4.0f; // Alarms only
                break;
            case NotificationManager.INTERRUPTION_FILTER_UNKNOWN:
                doNotDisturb = 0.0f; // Unknown or error
                break;
        }

        //Get screen value
        if (powerManager.isInteractive()) {
            screenValue = "Screen ON";
        } else {
            screenValue = "Screen OFF";
        }

        Map<String, String> sensorDataMap = new HashMap<>();
        sensorDataMap.put("Light_Value", String.valueOf(lightValue));
        sensorDataMap.put("Ringer_Mode", String.valueOf(ringerMode));
        sensorDataMap.put("UserAct_Type", userActivityType);
        sensorDataMap.put("Screen_Value", screenValue);
        sensorDataMap.put("DoNot_Disturb", String.valueOf(doNotDisturb));
        sensorDataMap.put("App_Value", appValue);
        dataViewModel.setSensorsData(sensorDataMap);
        // If at least one sensor has a value, update the UI once and stop listening
        if (hasSensorData()) {
            Log.d(TAG, "Light Value (lightValue): " + lightValue);
            Log.d(TAG, "Ringer Mode (ringerMode): " + ringerMode);
            Log.d(TAG, "User Activity Type (userActivityType): " + userActivityType);
            Log.d(TAG, "Screen Value (screenValue): " + screenValue);
            Log.d(TAG, "Do Not Disturb (doNotDisturb): " + doNotDisturb);
            Log.d(TAG, "App Value (appValue): " + appValue);
            appendSensorData();
            stopListeningSensors();
        }

    }

    private boolean hasSensorData() {
        return lightValue != 0 || ringerMode != 0 || !userActivityType.isEmpty() || !screenValue.isEmpty() || doNotDisturb != 0 || !appValue.isEmpty();
    }

    // In SensorAnalysisFragment.java, modify appendSensorData():
    private void appendSensorData() {
        StringBuilder sb = new StringBuilder();
        sb.append("Light Value: ").append(lightValue).append("\n");
        sb.append("Ringer Mode: ").append(ringerMode).append("\n");
        sb.append("User Activity Type: ").append(userActivityType).append("\n");
        sb.append("Screen Value: ").append(screenValue).append("\n");
        sb.append("Do Not Disturb: ").append(doNotDisturb).append("\n");
        sb.append("App Value: ").append(appValue).append("\n");
        String sensorData = sb.toString();
        Log.d(TAG, "appendSensorData() called with response: " + sensorData);
        sensorDataTextView.setText(sensorData);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Remove all callbacks to prevent leaks.
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach() called");
        sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
        accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        rotationVectorSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach() called");
        stopListeningSensors();
    }

    private void startListeningSensors() {
        if (lightSensor != null) {
            sensorManager.registerListener(this, lightSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "Light sensor registered");
        }
        if (accelerometerSensor != null) {
            sensorManager.registerListener(this, accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "Accelerometer sensor registered");
        }
        if (rotationVectorSensor != null) {
            sensorManager.registerListener(this, rotationVectorSensor, SensorManager.SENSOR_DELAY_NORMAL);
            Log.d(TAG, "Rotation vector sensor registered");
        }
    }

    private void stopListeningSensors() {
        sensorManager.unregisterListener(this);
        Log.d(TAG, "All sensors unregistered");
    }
}