**Automatic Message Reply System: Data Analysis**

**Overview**
This project analyzes user situational data to automate message replies based on the user's context, such as activity, device state, and environmental factors. The goal is to identify patterns that help determine when a user is available to respond.

**Dataset**
The dataset includes features like:
Device Metrics: Battery, Wi-Fi, location, etc.
User Activities: Event timings, phone state (silent, on call), etc.
Environmental Factors: Temperature, light, etc.
The target variable is Predicted_Availability, indicating if the user is available to reply.

**Data Analysis**

*Preprocessing*
Data cleaning involved removing irrelevant features, handling missing values, and normalizing continuous variables.

*Correlation Insights*
Time-Based Features: Strong negative correlation with availability, especially event-related features.
Device State: Is_Silent, Wifi_Conn, and Batt_Value are important predictors of availability.
Environmental Factors: Moderate correlation with availability, indicating that conditions like temperature and light matter.

*Pattern Recognition*
Clustering identified two key user states:
High Availability: No ongoing events, good battery, not in silent mode.
Low Availability: Attending an event, low battery, or in a noisy environment.

*Decision Rules*
Automatic replies can be triggered based on the following rules:
Available: Is_Silent = False, Event_Start = None, Batt_Value > threshold.
Not Available: Event_Duration > threshold or Is_Silent = True.

**Conclusion**
The analysis identifies key features and patterns for automating message replies. Future work will focus on integrating machine learning models and real-time systems for better prediction accuracy.



