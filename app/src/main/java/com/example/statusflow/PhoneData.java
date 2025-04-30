package com.example.statusflow;

import java.util.List;

public class PhoneData {
    private final Float currentLightLevel;
    private final boolean isSilent;
    private final boolean isScreenOn;
    private final int batteryLevel;
    private final List<String> recentApps;
    private final boolean hasEvent;
    private final Triple<Float,Float,Float> accelerometerData;
    public PhoneData(Float currentLightLevel, boolean isSilent, boolean isScreenOn, int batteryLevel, List<String> recentApps, boolean hasEvent, Triple<Float, Float, Float> accelerometerData) {
        this.currentLightLevel = currentLightLevel;
        this.isSilent = isSilent;
        this.isScreenOn = isScreenOn;
        this.batteryLevel = batteryLevel;
        this.recentApps = recentApps;
        this.hasEvent = hasEvent;
        this.accelerometerData = accelerometerData;
    }

    public Float getCurrentLightLevel() {
        return currentLightLevel;
    }

    public boolean isSilent() {
        return isSilent;
    }

    public boolean isScreenOn() {
        return isScreenOn;
    }

    public int getBatteryLevel() {
        return batteryLevel;
    }

    public List<String> getRecentApps() {
        return recentApps;
    }

    public boolean hasEvent() {
        return hasEvent;
    }
    public Triple<Float, Float, Float> getAccelerometerData(){
        return accelerometerData;
    }
}