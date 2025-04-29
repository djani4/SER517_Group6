package com.example.statusflow;

import android.util.Log;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class NetworkHelper {

    private static final String TAG = "NetworkHelper";
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client;
    private final String apiUrl;
    private final String apiKey;

    public NetworkHelper(String apiUrl, String apiKey) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build();
    }

    public String sendDataToApi(String data) {
        RequestBody body = RequestBody.create(data, JSON);
        Request request = new Request.Builder()
                .url(apiUrl)
                .addHeader("Authorization", "Bearer " + apiKey)
                .addHeader("Content-Type", "application/json")
                .post(body)
                .build();
        Log.d(TAG, "Sending data to API: " + request.toString());
        try (Response response = client.newCall(request).execute()) {
            if (response.isSuccessful()) {
                return response.body().string();
            } else {
                Log.e(TAG, "API request failed: " + response.code() + " - " + response.message());
                return "API request failed";
            }
        } catch (IOException e) {
            Log.e(TAG, "Error sending data to API: " + e.getMessage());
            return "Network error";
        }
    }
}