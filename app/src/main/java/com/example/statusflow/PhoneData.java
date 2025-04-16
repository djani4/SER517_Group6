package com.example.statusflow;

import java.util.List;

public class PhoneData {
    private Float lightLevel;
    private boolean isSilent;
    private boolean isScreenOn;
    private int batteryLevel;
    private List<String> recentApps;
    private boolean hasCalendarEvent;

    public PhoneData(Float lightLevel, boolean isSilent, boolean isScreenOn, int batteryLevel, List<String> recentApps, boolean hasCalendarEvent) {
        this.lightLevel = lightLevel;
        this.isSilent = isSilent;
        this.isScreenOn = isScreenOn;
        this.batteryLevel = batteryLevel;
        this.recentApps = recentApps;
        this.hasCalendarEvent = hasCalendarEvent;
    }

    public Float getLightLevel() { return lightLevel; }
    public boolean isSilent() { return isSilent; }
    public boolean isScreenOn() { return isScreenOn; }
    public int getBatteryLevel() { return batteryLevel; }
    public List<String> getRecentApps() { return recentApps; }
    public boolean hasCalendarEvent() { return hasCalendarEvent; }

    public String toFormattedString() {
        //String recentAppsStr = recentApps.isEmpty() ? "Not Available" : String.join(", ", recentApps);
        String recentAppsStr = recentApps.isEmpty() ? "Not Available" : recentApps.get(0);
        return String.format(
                "Light Level: %s\n" +
                        "Silent Mode: %s\n" +
                        "Screen On: %s\n" +
                        "Battery Level: %s\n" +
                        "Recent Apps: %s\n" +
                        "Calendar Event: %s",
                lightLevel != null ? lightLevel : "Not Available",
                isSilent,
                isScreenOn,
                batteryLevel != -1 ? batteryLevel + "%" : "Not Available",
                recentAppsStr,
                hasCalendarEvent ? "Yes" : "No"
        );
    }
}