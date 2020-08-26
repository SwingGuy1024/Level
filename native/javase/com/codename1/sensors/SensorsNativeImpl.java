package com.codename1.sensors;

public class SensorsNativeImpl implements com.codename1.sensors.SensorsNative {
    public boolean initSensor(int param) {
        return false;
    }

    public float getResolution(int type) {
        return 0;
    }

    public String getStringType(int type) {
        return null;
    }

    public void deregisterListener(int param) {
    }

    public void registerListener(int param) {
    }

    public boolean isSupported() {
        return false;
    }

    public int getLongInterval(int type) {
        return 0;
    }

    public int getShortInterval(int type) {
        return 0;
    }

    public void setInterval(int type, int delayMicroSeconds) {
        // to be written
    }

    public boolean useEllipseWorkaround() { return false; }
}
