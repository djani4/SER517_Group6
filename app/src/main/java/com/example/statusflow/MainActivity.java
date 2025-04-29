package com.example.statusflow;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;


public class MainActivity extends AppCompatActivity {

    private TextView dataTextView;
    private DataViewModel viewModel;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private boolean usageStatsPermissionAlreadyRequested = false;

    private static final String TAG = "MainActivity";
    private Button selectFileButton;
    private Uri selectedFileUri;

    private ActivityResultLauncher<Intent> filePickerLauncher;

    private TextView responseTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        selectFileButton = findViewById(R.id.selectFileButton);
        responseTextView = findViewById(R.id.responseTextView);

        String apiUrl = "https://api.groq.com/openai/v1/chat/completions";
        String apiKey = "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";
        viewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory() {
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(Class<T> modelClass) {
                return (T) new DataViewModel(apiUrl, apiKey);
            }
        }).get(DataViewModel.class);


        filePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        selectedFileUri = result.getData().getData();
                        Log.d(TAG, "Selected file URI: " + selectedFileUri.toString());
                        try {
                            processFile(selectedFileUri);
                        } catch (IOException e) {
                            e.printStackTrace();
                            Log.e(TAG, "Error processing file: " + e.getMessage());
                            Toast.makeText(MainActivity.this, "Error processing file", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Log.e(TAG, "Error selecting file or no file selected");
                        Toast.makeText(MainActivity.this, "No file selected", Toast.LENGTH_SHORT).show();
                    }
                }
        );

        selectFileButton.setOnClickListener(v -> openFilePicker());
        viewModel.getApiResponse().observe(this, new Observer<String>() {
            @Override
            public void onChanged(String response) {
                Log.d("MainActivity", "Received response: " + response);
                try {
                    JSONObject jsonObject = new JSONObject(response);
                    String content = jsonObject
                            .getJSONArray("choices")
                            .getJSONObject(0)
                            .getJSONObject("message")
                            .getString("content");
                    responseTextView.setText(content.trim());
                } catch (Exception e) {
                    Log.e("MainActivity", "Error parsing response", e);
                    responseTextView.setText("Error parsing response");
                }
            }
        });

//        dataTextView = findViewById(R.id.dataTextView);
//        Button collectDataButton = findViewById(R.id.collectDataButton);
//
//        viewModel = new ViewModelProvider(this).get(DataViewModel.class);
//        viewModel.getPhoneData().observe(this, phoneData -> {
//            if (phoneData != null) {
//                dataTextView.setText(phoneData.toFormattedString());
//            }
//        });
//
//        requestPermissionLauncher =
//                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
//                        permissions -> {
//                            boolean allGranted = true;
//                            for (Boolean granted : permissions.values()) {
//                                if (!granted) {
//                                    allGranted = false;
//                                    break;
//                                }
//                            }
//                            if (allGranted || hasUsageStatsPermission()) { // Check if usage stats was granted separately.
//                                collectAndDisplayData();
//                            } else {
//                                dataTextView.setText("Permissions not granted. Some data will be unavailable.");
//                                Log.w("Permissions", "Not all permissions granted.");
//                            }
//                        });
//
//
//        collectDataButton.setOnClickListener(v -> checkAndRequestPermissions());
    }


    private void checkAndRequestPermissions() {
        List<String> permissionsToRequest = new ArrayList<>();

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.RECORD_AUDIO);
        }
        // We don't request PACKAGE_USAGE_STATS directly:
        if (!hasUsageStatsPermission() && !usageStatsPermissionAlreadyRequested) {
            // Instead, send the user to settings:
            Intent intent = new Intent(android.provider.Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
            usageStatsPermissionAlreadyRequested = true;
            // We'll check for the permission again in onResume().
            return;  // Exit the function to prevent data collection until permission is checked.
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_CALENDAR) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.READ_CALENDAR);
        }
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.BODY_SENSORS) != PackageManager.PERMISSION_GRANTED) {
            permissionsToRequest.add(Manifest.permission.BODY_SENSORS);
        }

        if (!permissionsToRequest.isEmpty()) {
            requestPermissionLauncher.launch(permissionsToRequest.toArray(new String[0]));
        } else {
            collectAndDisplayData();
        }
    }


    private boolean hasUsageStatsPermission() {
        AppOpsManager appOps = (AppOpsManager) getSystemService(APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        return mode == AppOpsManager.MODE_ALLOWED;
    }


    private void collectAndDisplayData() {
        viewModel.collectPhoneData(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasUsageStatsPermission()) {
            collectAndDisplayData();
        }
    }

    private void openFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        filePickerLauncher.launch(intent);
    }
    private void processFile(Uri fileUri) throws IOException {
        if (!Environment.isExternalStorageManager()) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_ALL_FILES_ACCESS_PERMISSION);
            startActivity(intent);
            return;
        }

        InputStream inputStream = getContentResolver().openInputStream(fileUri);
        CSVFileHandler csvFileHandler = new CSVFileHandler();
        List<Map<String, String>> csvData = csvFileHandler.readCSV(inputStream);

        if (csvData != null && !csvData.isEmpty()) {
            DataAnalyzer dataAnalyzer = new DataAnalyzer();
            dataAnalyzer.analyzeData(csvData);
            viewModel.sendAnalyzedDataToApi(csvData);

            Toast.makeText(this, "File analyzed and statistics generated in the documents folder/data", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "Failed to read or analyze CSV file", Toast.LENGTH_LONG).show();
        }
    }
}