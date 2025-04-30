package com.example.statusflow;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DataViewModel extends ViewModel {
    private static final String TAG = "DataViewModel";
    private final MutableLiveData<PhoneData> phoneData = new MutableLiveData<>();
    private final MutableLiveData<String> apiResponse = new MutableLiveData<>();
    private final MutableLiveData<AnalysisMode> analysisMode = new MutableLiveData<>(AnalysisMode.CSV);
    private final MutableLiveData<Boolean> isListeningSensors = new MutableLiveData<>(false);
    private final NetworkHelper networkHelper;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private PhoneDataCollector collector;
    private List<Map<String, String>> learningDataRows;
    private Map<String, String> sensorsData;

    public DataViewModel(String apiUrl, String apiKey) {
        networkHelper = new NetworkHelper(apiUrl, apiKey);
    }

    public LiveData<PhoneData> getPhoneData() {
        return phoneData;
    }

    public LiveData<String> getApiResponse() {
        return apiResponse;
    }

    public LiveData<AnalysisMode> getAnalysisMode() {
        return analysisMode;
    }
    public LiveData<Boolean> getIsListeningSensors() {
        return isListeningSensors;
    }

    public void collectPhoneData(Context context) {
        if (collector == null) {
            collector = new PhoneDataCollector(context);
        }
        phoneData.setValue(collector.collectData());
    }

    public void changeMode(AnalysisMode mode) {
        analysisMode.setValue(mode);
    }

    public void setLearningData(List<Map<String, String>> data) {
        learningDataRows = data;
    }
    public void setSensorsData(Map<String, String> data) {
        sensorsData = data;
    }

    public void startListeningSensors(Context context) {
        if (collector == null) {
            collector = new PhoneDataCollector(context);
        }
        collector.startListening();
        isListeningSensors.setValue(true);
    }

    public void stopListeningSensors() {
        if (collector != null) {
            collector.stopListening();
        }
        isListeningSensors.setValue(false);
    }



    public void sendAnalyzedDataToApi(Context context) {
        executorService.execute(() -> {
            String condition = "";
            if (learningDataRows == null) {
                apiResponse.postValue("No learning data available");
                return;
            }

            String learningData = buildLearningExamples(learningDataRows);

            if (analysisMode.getValue() == AnalysisMode.CSV) {
                // Use CSV data if avalible
                if (learningDataRows.size() > 0) {
                    Map<String, String> currentRow = learningDataRows.get(learningDataRows.size() - 1);
                    condition = "[light=" + currentRow.get("Light_Value") +
                            ", ringer=" + currentRow.get("Ringer_Mode") +
                            ", activity=" + currentRow.get("UserAct_Type") +
                            ", screen=" + currentRow.get("Screen_Value") +
                            ", dnd=" + (currentRow.get("DoNot_Disturb").equals("1.0") ? "ON" : "OFF") +
                            ", app=" + currentRow.get("App_Value") + "]";
                } else {
                    apiResponse.postValue("No data in CSV");
                    return;
                }

            } else if (analysisMode.getValue() == AnalysisMode.SENSOR) {
                // Use sensor data
                if (collector == null) {
                    collector = new PhoneDataCollector(context);
                }
                if(sensorsData==null) {
                    apiResponse.postValue("no data in sensors");
                    return;
                }
                condition = "[light=" + sensorsData.get("Light_Value") +
                        ", ringer=" + sensorsData.get("Ringer_Mode") +
                        ", activity=" + sensorsData.get("UserAct_Type") +
                        ", screen=" + sensorsData.get("Screen_Value") +
                        ", dnd=" + (sensorsData.get("DoNot_Disturb").equals("1.0") ? "ON" : "OFF") +
                        ", app=" + sensorsData.get("App_Value") + "]";//get first app

            }
            sendData(condition, learningData);
        });
    }

    private void sendData(String condition, String learningData) {

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
        if (collector != null) {
            collector.stopListening();
        }
    }
}