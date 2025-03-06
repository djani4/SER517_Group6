# Brief Dataset Summary

- **Time Period**: June 10–26, 2020 (weekdays, EDT).
- **Focus**: Phone usage and context, mainly WhatsApp and Samsung Messaging.
- **Number of rows**: 267.
- **Key Features**:
  - **Time**: `ts`, `DayOfWeek` (2–4), `HourOfDay` (7–23).
  - **Device**: `Battery` (45–100%), `Screen_Value` (Locked/Unlocked), `Ringer_Mode` (VIBRATE/SILENT).
  - **Activity**: Mostly `still`, some `on_foot`, `in_vehicle`.
  - **Environment**: `Loc_Type` (mostly Home), `Noise_Value` (36–96), `Light_Value` (0–955), `Temperature` (15–32°C).
  - **Apps**: WhatsApp, launcher, occasional Gmail, Audible.
  - **Interaction**: Timestamps like `timeSinceLastScreenUnlocked` (e.g., 1.8–72 minutes).
  - **Events**: Few meetings (e.g., CSCW).
  - **Predictions**: `Predicted_Availability` (0/1), auto-responses (e.g., "Bob may not be available").
- **Patterns**: Active mornings/evenings, inactive nights; frequent WhatsApp use.
- **Purpose**: Predict availability for calls/messages.
- **Limitations**: Single user, weekday-only sample, some missing data.
