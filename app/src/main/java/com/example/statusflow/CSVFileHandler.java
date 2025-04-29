package com.example.statusflow;

import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CSVFileHandler {
    private static final String TAG = "CSVFileHandler";

    public List<Map<String, String>> readCSV(InputStream inputStream) {
        List<Map<String, String>> data = new ArrayList<>();
        BufferedReader reader = null;
        String[] headers = null;

        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));
            String line;
            int lineNumber = 0;

            while ((line = reader.readLine()) != null) {
                lineNumber++;
                if (line.trim().isEmpty()) {
                    Log.w(TAG, "Skipping empty line at line number: " + lineNumber);
                    continue;
                }

                List<String> row = parseLine(line);


                if (lineNumber == 1) {
                    headers = row.toArray(new String[0]);
                    Log.d(TAG, "Headers: " + Arrays.toString(headers));
                    continue;
                }

                if (headers == null) {
                    Log.e(TAG, "Headers not found in CSV");
                    return null;
                }

                if (row.isEmpty()) {
                    Log.w(TAG, "Skipping empty data line at line number: " + lineNumber);
                    continue;
                }

                if (row.size() != headers.length) {
                    Log.w(TAG, "Skipping line number: " + lineNumber + " with " + row.size() + " columns, expected: " + headers.length);
                    continue;
                }


                Map<String, String> rowData = new HashMap<>();
                for (int i = 0; i < headers.length; i++) {
                    rowData.put(headers[i], row.get(i));
                }

                data.add(rowData);
            }
        } catch (IOException e) {
            Log.e(TAG, "Error reading CSV file", e);
            return null;
        } finally {
            if (reader != null) {
                try {
                    reader.close();
                } catch (IOException e) {
                    Log.e(TAG, "Error closing reader", e);
                }
            }
        }

        return data;
    }

    private List<String> parseLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder currentField = new StringBuilder();
        boolean inQuotes = false;

        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
                if (!inQuotes) {
                    result.add(currentField.toString());
                    currentField.setLength(0);
                }
            } else if (c == ',' && !inQuotes) {
                result.add(currentField.toString());
                currentField.setLength(0);
            } else {
                currentField.append(c);
            }
        }

        result.add(currentField.toString());
        return result;
    }
}