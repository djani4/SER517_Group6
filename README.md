
# StatusFlow - Android App for Contextual Busy Prediction

**StatusFlow** is an Android application designed to predict whether a user is currently busy based on various phone state and sensor data. It leverages a language model to analyze real-time and historical data, providing insights into user context. This project demonstrates the integration of mobile sensor data, CSV file processing, and language model interaction within an Android environment.

## Project Overview

The core objective of StatusFlow is to understand a user's current context and predict their availability (busy or not busy) by examining the state of their mobile device. The app accomplishes this by:

1.  **Collecting Real-Time Sensor Data:** It gathers information from various phone sensors, including the light sensor, ringer mode, activity recognition, screen state, do-not-disturb status, and the foreground app in use.
2.  **Analyzing CSV Data:** Users can import and analyze data from CSV files. This feature allows the application to process historical phone usage data for a more nuanced understanding of user behavior.
3.  **Leveraging a Language Model:** The app interacts with a language model to interpret both the real-time sensor data and the information from CSV files. The language model, which has been trained on past examples, makes a prediction about the user's current availability.
4.  **Learning Data:** The app includes a set of learning data to train the language model.
5.  **User-Friendly interface:** The app uses a simple interface to pick a CSV file or listen to the phone sensors. Also, the output from the language model is displayed in the main screen.

## Features

*   **Real-time Sensor Analysis:** Collects and analyzes real-time data from various phone sensors.
*   **CSV Data Analysis:** Allows users to select and analyze data from a CSV file.
*   **Language Model Integration:** Uses a language model to make predictions about user availability.
*   **Learning Data:** Uses a set of learning data to improve the accuracy of predictions.
*   **User-Friendly Interface:** The app provides an easy and intuitive way to interact with the app.
*   **Clear Output:** The response from the language model is displayed clearly in the app.

## Prerequisites

To run StatusFlow, you will need the following:

*   **Android Device or Emulator:** An Android device (with "Developer Options" and "USB Debugging" enabled) or an Android emulator is required to run the app.
*   **Android Studio (Optional):**  If you want to build the project from source, you will need Android Studio. The latest version is recommended and can be downloaded from [https://developer.android.com/studio](https://developer.android.com/studio).
*   **Android SDK (Optional):** If you are building from the source, ensure you have the required Android SDK components installed via Android Studio.
*   **Java Development Kit (JDK) (Optional):** If you are building from the source, a compatible version of the JDK is necessary for Android development.

## Installation

1.  **Clone the Repository:**
    *   Clone this repository to your local machine.
    *   If you want to build the project from the source code, follow the next steps. If you just want to use the app, follow the *Install the APK* section.
2.  **Open Project in Android Studio (Optional):**
    *   Launch Android Studio.
    *   Select "Open an Existing Project."
    *   Navigate to the project directory and select it.
3.  **Sync Project with Gradle (Optional):**
    *   Android Studio will prompt you to sync the project with Gradle files. Click "Sync Now."
4.  **Build Project (Optional):**
    *   In Android Studio, go to "Build" -> "Make Project."

## Running the App

### Option 1: Build from Source (Optional)

1.  **Connect Android Device or Start Emulator:**
    *   **Physical Device:** Enable "Developer Options" and "USB Debugging" on your device. Connect it to your computer via USB.
    *   **Emulator:** In Android Studio, go to "Tools" -> "Device Manager" to create or start an emulator.
2.  **Select Run Configuration:** In the toolbar, ensure your app module ("app") is selected in the run configuration dropdown.
3.  **Select Target Device:** Choose your connected device or emulator from the dropdown next to the run configuration.
4.  **Run the App:** Click the green "Run" button (play icon). Android Studio will build, install, and launch the app.

### Option 2: Install the APK (Recommended)

1.  **Download the APK:**
    *   Go to the `releases` folder in this repository and download the `app-release.apk` file.
2.  **Transfer to Device:**
    *   Transfer the `app-release.apk` file to your Android device using a USB cable, cloud storage, or any other file transfer method.
3.  **Install:**
    *   On your Android device, use a file manager app to locate the `app-release.apk` file.
    *   Tap the APK file.
    *   You might need to allow "Install apps from unknown sources" in your device settings (if prompted).
    *   Follow the on-screen instructions to install the app.
4.  **Launch:** After installation, you can find the app in your app drawer and launch it.
5.  **Permissions:** Allow the permissions that the app is requesting.

## Using the App

1.  **App Launch:** Upon launching, StatusFlow presents you with options for analyzing data.
2.  **Sensor Data Mode:** Select this mode to use real-time sensor data. The app will start collecting data from your phone's sensors and will send it to the language model to get a response. The response will be showed in the main screen.
3.  **CSV Data Mode:** Select this mode to analyze a CSV file.
    *   Tap the "Pick CSV File" button.
    *   Use the file picker to select a CSV file from your device's storage.
    *   Once selected, the app will process the first row of data from the CSV, send it to the language model, and display the prediction on the main screen.
4.  **View Results:** The language model's prediction (whether the user is busy or not) and the associated reasoning will be displayed on the app's main screen.

## Troubleshooting

*   **Gradle Sync Errors (If Building from Source):**
    *   Go to "File" -> "Sync Project with Gradle Files" in Android Studio.
    *   Check your internet connection.
*   **Device/Emulator Issues:**
    *   Ensure "USB Debugging" is enabled.
    *   Restart Android Studio and reconnect your device.
*   **Build Errors (If Building from Source):**
    *   Review the "Build" tab in Android Studio for specific error messages.
    *   Verify that you have the correct Android SDK versions.
*   **Installation Errors (If Installing from APK):**
    *   Ensure you have allowed "Install apps from unknown sources" in your device settings.
*   **Permissions:**
    *   Be sure to allow all the permissions that the app is requesting.
