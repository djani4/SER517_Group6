package com.example.statusflow;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private DataViewModel dataViewModel;
    private Button csvButton;
    private Button sensorButton;
    private FragmentManager fragmentManager;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private boolean isPermissionGranted = false;
    private static final String TAG = "MainActivity";
    private static final String API_URL = "https://api.groq.com/openai/v1/chat/completions"; // Replace with your actual URL
    private static final String API_KEY = "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"; // Replace with your actual API key

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Initialize DataViewModel
        dataViewModel = new ViewModelProvider(this, new ViewModelProvider.NewInstanceFactory() {
            @Override
            public <T extends androidx.lifecycle.ViewModel> T create(Class<T> modelClass) {
                return (T) new DataViewModel(API_URL, API_KEY);
            }
        }).get(DataViewModel.class);
        fragmentManager = getSupportFragmentManager();

        csvButton = findViewById(R.id.csvButton);
        sensorButton = findViewById(R.id.sensorButton);

        // Setup buttons
        csvButton.setOnClickListener(v -> {
            dataViewModel.changeMode(AnalysisMode.CSV);
            showFragment(CsvAnalysisFragment.class);
            dataViewModel.stopListeningSensors();
        });

        sensorButton.setOnClickListener(v -> {
            dataViewModel.changeMode(AnalysisMode.SENSOR);
            showFragment(SensorAnalysisFragment.class);
            dataViewModel.startListeningSensors(this);
        });

        showFragment(CsvAnalysisFragment.class); //set default

        //Dummy learning data to test
        List<Map<String, String>> dummyLearningData = createDummyLearningData();
        dataViewModel.setLearningData(dummyLearningData);
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    isPermissionGranted = isGranted;
                    if (isGranted) {
                        Log.i("Permission: ", "Granted");
                    } else {
                        Log.i("Permission: ", "Denied");
                    }
                });
        checkAndRequestSensorPermission();
    }

    private void showFragment(Class<? extends Fragment> fragmentClass) {
        try {
            Fragment fragment = fragmentClass.newInstance();
            FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fragmentContainer, fragment);
            transaction.commit();
        } catch (Exception e) {
            Log.e(TAG, "Error creating fragment", e);
        }
    }

    private List<Map<String, String>> createDummyLearningData() {
        List<Map<String, String>> learningData = new java.util.ArrayList<>();

        Map<String, String> row1 = new java.util.HashMap<>();
        row1.put("Light_Value", "LOW");
        row1.put("Ringer_Mode", "SILENT");
        row1.put("UserAct_Type", "SLEEPING");
        row1.put("Screen_Value", "OFF");
        row1.put("DoNot_Disturb", "1.0");
        row1.put("App_Value", "NONE");
        row1.put("Predicted_Availability", "0.0"); // Busy

        Map<String, String> row2 = new java.util.HashMap<>();
        row2.put("Light_Value", "HIGH");
        row2.put("Ringer_Mode", "NORMAL");
        row2.put("UserAct_Type", "ACTIVE");
        row2.put("Screen_Value", "ON");
        row2.put("DoNot_Disturb", "0.0");
        row2.put("App_Value", "WHATSAPP");
        row2.put("Predicted_Availability", "1.0"); // Not Busy

        learningData.add(row1);
        learningData.add(row2);
        return learningData;
    }
    private void checkAndRequestSensorPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
            requestSensorPermission();
        } else {
            isPermissionGranted = true;
        }
    }
    private void requestSensorPermission() {
        requestPermissionLauncher.launch(Manifest.permission.ACTIVITY_RECOGNITION);
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "onResume() called");
        if(dataViewModel.getAnalysisMode().getValue() == AnalysisMode.SENSOR){
            Log.d("MainActivity", "Starting to listen sensors in onResume");
            dataViewModel.startListeningSensors(this);
        } else {
            Log.d("MainActivity", "Stopping to listen sensors in onResume");
            dataViewModel.stopListeningSensors();
        }
    }
    @Override
    protected void onPause() {
        super.onPause();
        Log.d("MainActivity", "onPause() called");
        dataViewModel.stopListeningSensors();
    }
}