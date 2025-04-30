package com.example.statusflow;

import android.content.res.AssetManager;
import android.os.Bundle;
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
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CsvAnalysisFragment extends Fragment {

    private static final String TAG = "CsvAnalysisFragment";
    private DataViewModel dataViewModel;
    private TextView csvDataTextView;
    private Button sendToApiButton;
    private List<Map<String, String>> analyzedData = new ArrayList<>();

    private Button pickCsvButton;
    private static final int REQUEST_CODE_PICK_CSV_FILE = 1;
    private List<Map<String, String>> newCsvDataRows;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataViewModel = new ViewModelProvider(requireActivity()).get(DataViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_csv_analysis, container, false);
        csvDataTextView = view.findViewById(R.id.csvDataTextView);
        sendToApiButton = view.findViewById(R.id.sendToApiButton);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Clear the TextView initially
        csvDataTextView.setText("");

        // Setup button
        sendToApiButton.setOnClickListener(v -> {
            //Load and display csv data before sending to api
            loadCsvData();
            dataViewModel.changeMode(AnalysisMode.CSV);
            dataViewModel.sendAnalyzedDataToApi(getContext());
        });


        // Observe and update the same TextView
        dataViewModel.getApiResponse().observe(getViewLifecycleOwner(), response -> {
            if (!response.isEmpty()) {
                String formattedResponse = extractAndFormatResponse(response);
                appendResponseToCsvData(formattedResponse); // Append the response
            }
        });
    }

    private void loadCsvData() {
        try {
            File csvFile = getCsvFile(); // Method to get the CSV file
            List<Map<String, String>> csvData = readCsvFile(csvFile);
            if(csvData.size()>0) {
                analyzedData = csvData; // Store data
            } else{
                appendResponseToCsvData("no data in csv");
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading CSV file", e);
            appendResponseToCsvData("Error reading CSV file");
        }
    }

    private File getCsvFile() throws IOException {
        String fileName = "data.csv";
        AssetManager assetManager = requireContext().getAssets();
        InputStream inputStream = assetManager.open(fileName);

        // Create a temporary file to copy the CSV data
        File tempFile = File.createTempFile("temp_csv", ".csv", requireContext().getCacheDir());

        // Copy the content from the input stream to the temporary file
        try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
            byte[] buffer = new byte[1024];
            int length;
            while ((length = inputStream.read(buffer)) > 0) {
                fileOutputStream.write(buffer, 0, length);
            }
        }

        return tempFile;
    }

    private List<Map<String, String>> readCsvFile(File csvFile) throws IOException {
        List<Map<String, String>> data = new ArrayList<>();
        BufferedReader reader = new BufferedReader(new FileReader(csvFile));
        String line;
        String[] headers = null;

        while ((line = reader.readLine()) != null) {
            String[] row = line.split(",");
            if (headers == null) {
                headers = row;
            } else {
                Map<String, String> rowMap = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    if (i < row.length) { // Check if 'i' is a valid index for 'row'
                        rowMap.put(headers[i], row[i]);
                    } else {
                        rowMap.put(headers[i], ""); // Handle missing values (e.g., set to empty string)
                    }
                }
                data.add(rowMap);
            }
        }
        reader.close();
        return data;
    }
    //new method to append response to csvDataTextView
    private void appendResponseToCsvData(String response) {
        // Get the current text from the TextView
        CharSequence currentText = csvDataTextView.getText();

        // Append the new response with a separator (e.g., a newline)
        String newText = response;

        // Update the TextView with the combined text
        csvDataTextView.setText(newText);
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
}