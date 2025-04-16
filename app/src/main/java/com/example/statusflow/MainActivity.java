package com.example.statusflow;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private TextView dataTextView;
    private DataViewModel viewModel;
    private ActivityResultLauncher<String[]> requestPermissionLauncher;
    private boolean usageStatsPermissionAlreadyRequested = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        dataTextView = findViewById(R.id.dataTextView);
        Button collectDataButton = findViewById(R.id.collectDataButton);

        viewModel = new ViewModelProvider(this).get(DataViewModel.class);
        viewModel.getPhoneData().observe(this, phoneData -> {
            if (phoneData != null) {
                dataTextView.setText(phoneData.toFormattedString());
            }
        });

        requestPermissionLauncher =
                registerForActivityResult(new ActivityResultContracts.RequestMultiplePermissions(),
                        permissions -> {
                            boolean allGranted = true;
                            for (Boolean granted : permissions.values()) {
                                if (!granted) {
                                    allGranted = false;
                                    break;
                                }
                            }
                            if (allGranted || hasUsageStatsPermission()) { // Check if usage stats was granted separately.
                                collectAndDisplayData();
                            } else {
                                dataTextView.setText("Permissions not granted. Some data will be unavailable.");
                                Log.w("Permissions", "Not all permissions granted.");
                            }
                        });


        collectDataButton.setOnClickListener(v -> checkAndRequestPermissions());
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
}