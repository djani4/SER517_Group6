package com.example.statusflow;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataViewModel extends ViewModel {
    private static final String TAG = "DataViewModel";
    private final MutableLiveData<PhoneData> phoneData = new MutableLiveData<>();
    private final MutableLiveData<String> apiResponse = new MutableLiveData<>();
    private final NetworkHelper networkHelper;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public DataViewModel(String apiUrl, String apiKey) {
        networkHelper = new NetworkHelper(apiUrl, apiKey);
    }

    public LiveData<PhoneData> getPhoneData() {
        return phoneData;
    }

    public LiveData<String> getApiResponse() {
        return apiResponse;
    }

    public void collectPhoneData(Context context) {
        PhoneDataCollector collector = new PhoneDataCollector(context);
        phoneData.setValue(collector.collectData());
    }

    public void sendAnalyzedDataToApi(List<Map<String, String>> analyzedData) {
        executorService.execute(() -> {
            if (analyzedData == null) {
                apiResponse.postValue("No data to analyze");
                return;
            }

            String learningData = buildLearningExamples(analyzedData);

            // Pick the number for row you want to analyze:
            Map<String, String> currentRow = analyzedData.get(analyzedData.size() - 1);

            String condition = "[light=" + currentRow.get("Light_Value") +
                    ", ringer=" + currentRow.get("Ringer_Mode") +
                    ", activity=" + currentRow.get("UserAct_Type") +
                    ", screen=" + currentRow.get("Screen_Value") +
                    ", dnd=" + (currentRow.get("DoNot_Disturb").equals("1.0") ? "ON" : "OFF") +
                    ", app=" + currentRow.get("App_Value") + "]";

            String query = "You are an expert assistant helping to understand mobile usage context.\n" +
                    "Here are some past examples of different phone states and whether the user was busy or not:\n\n" +
                    learningData +
                    "\nBased on that, evaluate the following condition:\n" +
                    condition +
                    "\n\nFormat your response like this:\n" +
                    "Condition: <repeat the above condition>\n" +
                    "Prediction: User is [busy/not busy]\n" +
                    "Reason: <give a short explanation, DO NOT mention 'training data' or 'examples'. Just explain based on the user's current phone usage context.>";


            String escapedQuery = escapeJson(query);
            String jsonPayload = "{\n" +
                    "  \"model\": \"llama-3.3-70b-versatile\",\n" +
                    "  \"messages\": [\n" +
                    "    {\"role\": \"user\", \"content\": \"" + escapedQuery + "\"}\n" +
                    "  ]\n" +
                    "}";

            Log.d(TAG, "Payload: " + jsonPayload);
            String response = networkHelper.sendDataToApi(jsonPayload);
            apiResponse.postValue(response);

        });
    }




    private String buildLearningExamples(List<Map<String, String>> rows) {
        StringBuilder sb = new StringBuilder();

        int count = 0;
        for (Map<String, String> row : rows) {
            if (count++ > 200) break;

            String light = row.getOrDefault("Light_Value", "?");
            String ringer = row.getOrDefault("Ringer_Mode", "?");
            String screen = row.getOrDefault("Screen_Value", "?");
            String activity = row.getOrDefault("UserAct_Type", "?");
            String dnd = row.getOrDefault("DoNot_Disturb", "0").equals("1.0") ? "ON" : "OFF";
            String app = row.getOrDefault("App_Value", "?");

            String label;
            String availability = row.getOrDefault("Predicted_Availability", "").trim();
            if (availability.equals("1.0")) {
                label = "not busy";
            } else if (availability.equals("0.0")) {
                label = "busy";
            } else {
                continue;
            }


            sb.append("[light=").append(light)
                    .append(", ringer=").append(ringer)
                    .append(", activity=").append(activity)
                    .append(", screen=").append(screen)
                    .append(", dnd=").append(dnd)
                    .append(", app=").append(app)
                    .append("] → ").append(label).append("\n");
        }

        return sb.toString();
    }

    private String escapeJson(String raw) {
        return raw
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r");
    }




    @Override
    protected void onCleared() {
        super.onCleared();
        executorService.shutdown();
    }
}