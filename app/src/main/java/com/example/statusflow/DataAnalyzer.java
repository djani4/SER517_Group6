package com.example.statusflow;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataAnalyzer {

    private static final String TAG = "DataAnalyzer";
    private String outputDir;

    public DataAnalyzer() {

        outputDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS) + "/data";
        File dir = new File(outputDir);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }

    public void analyzeData(List<Map<String, String>> data) {
        if (data == null || data.isEmpty()) {
            Log.e(TAG, "No data to analyze");
            return;
        }


        List<String> numericCols = new ArrayList<>();
        List<String> categoricalCols = new ArrayList<>();
        identifyColumnTypes(data, numericCols, categoricalCols);


        try {
            generateSummaryStatistics(data, numericCols);
        } catch (IOException e) {
            Log.e(TAG, "Error generating summary statistics: " + e.getMessage());
        }
        try {
            generateCategoricalStatistics(data,categoricalCols);
        } catch (IOException e) {
            Log.e(TAG, "Error generating categorical statistics: " + e.getMessage());
        }
        try {
            generateRawData(data);
        } catch (IOException e) {
            Log.e(TAG, "Error generating raw data: " + e.getMessage());
        }

    }

    private void identifyColumnTypes(List<Map<String, String>> data, List<String> numericCols, List<String> categoricalCols) {
        Map<String, String> firstRow = data.get(0);
        for (String col : firstRow.keySet()) {
            String value = firstRow.get(col);
            if (isNumeric(value)) {
                numericCols.add(col);
            } else {
                categoricalCols.add(col);
            }
        }
    }

    private boolean isNumeric(String str) {
        if (str == null || str.trim().isEmpty()) {
            return false;
        }
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private void generateSummaryStatistics(List<Map<String, String>> data, List<String> numericCols) throws IOException {

        if (data.isEmpty()) {
            Log.w(TAG, "No data available for generating summary statistics.");
            return;
        }

        FileWriter writer = new FileWriter(outputDir + "/summary_statistics.csv");
        writer.write("Column,Count,Mean,Std,Min,25%,50%,75%,Max\n");

        if (numericCols.isEmpty()) {
            Log.w(TAG, "No numeric columns found to generate summary statistics.");
            writer.close();
            return;
        }
        for (String col : numericCols) {
            List<Double> values = new ArrayList<>();
            for (Map<String, String> row : data) {
                try {
                    values.add(Double.parseDouble(row.get(col)));
                } catch (NumberFormatException e) {
                    Log.d(TAG, "Non-numeric value found in column '" + col + "': " + row.get(col));

                    Log.w(TAG, "Non-numeric value found in column '" + col + "': " + row.get(col));
                }
            }

            if (values.isEmpty()) {
                Log.w(TAG, "No valid numeric data found for column: " + col);
                continue;
            }

            double count = values.size();
            double sum = 0;
            for (double value : values) {
                sum += value;
            }
            double mean = sum / count;

            double stdSum = 0;
            for (double value : values) {
                stdSum += Math.pow(value - mean, 2);
            }
            double std = Math.sqrt(stdSum / count);

            double min = values.get(0);
            double max = values.get(0);
            for (double value : values) {
                if (value < min) min = value;
                if (value > max) max = value;
            }

            values.sort(Double::compareTo);
            double q1 = values.get((int) Math.round(count * 0.25) - 1);
            double median = values.get((int) Math.round(count * 0.5) - 1);
            double q3 = values.get((int) Math.round(count * 0.75) - 1);

            writer.write(String.format("%s,%d,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f,%.2f\n", col, (int) count, mean, std, min, q1, median, q3, max));
        }
        writer.close();
    }
    private void generateCategoricalStatistics(List<Map<String, String>> data, List<String> categoricalCols) throws IOException {

        if (data.isEmpty()) {
            Log.w(TAG, "No data available for generating categorical statistics.");
            return;
        }

        FileWriter writer = new FileWriter(outputDir + "/categorical_statistics.csv");
        writer.write("Column,Value,Count\n");

        for (String col : categoricalCols) {
            Map<String, Integer> valueCounts = new HashMap<>();
            for (Map<String, String> row : data) {
                String value = row.get(col);
                valueCounts.put(value, valueCounts.getOrDefault(value, 0) + 1);
            }

            for (Map.Entry<String, Integer> entry : valueCounts.entrySet()) {
                writer.write(String.format("%s,%s,%d\n", col, entry.getKey(), entry.getValue()));
            }
        }
        writer.close();
    }
    private void generateRawData(List<Map<String, String>> data) throws IOException {
        FileWriter writer = new FileWriter(outputDir + "/raw_data.csv");

        if (!data.isEmpty()) {
            Map<String, String> firstRow = data.get(0);
            String headers = String.join(",", firstRow.keySet());
            writer.write(headers + "\n");


            for (Map<String, String> row : data) {
                List<String> values = new ArrayList<>(row.values());
                String rowData = String.join(",", values);
                writer.write(rowData + "\n");
            }
        }
        writer.close();
    }
}