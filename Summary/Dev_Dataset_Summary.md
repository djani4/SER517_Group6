Dataset Summary


Rows: 1,067

Columns: 77

Data Types:

  28 float columns

  18 integer columns

  31 object (text) columns

  
Time-related Columns: ts, ts_raw, DayOfWeek, HourOfDay


Key Features:

Device-related: Battery level (Batt_Value), charging status (Charging_Value), screen activity (Screen_Value), ringer mode (Ringer_Mode), DoNot_Disturb

Network-related: WiFi info (Wifi_SSID, Wifi_IP, Wifi_RSSI, CellTower_Type, CellTower_SignalStrength)

User behavior: UserAct_Type, timeSinceLastCallPickup, lastForegroundAppCategory

Contextual data: Weather, Temperature, Calendar_Event

User feedback: Appropriate, Useful, Comfortable, Better_Responses (most have limited responses)

Observations:

Several missing values in columns like Better_Responses, Reason, Granularity, and Event_Location
Some columns (e.g., Shap) contain no data
Contains both raw and processed time values (ts_raw, DayOfWeek, HourOfDay)
