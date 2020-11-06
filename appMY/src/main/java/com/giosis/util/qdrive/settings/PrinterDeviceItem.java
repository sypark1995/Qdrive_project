package com.giosis.util.qdrive.settings;

public class PrinterDeviceItem {

    private String deviceNm;
    private String deviceAddress;
    private boolean isFound;            // Pairing 발견
    private boolean isConnected;        // Connected

    PrinterDeviceItem(String deviceNm, String deviceAddress, boolean isFound, boolean isConnected) {

        this.deviceNm = deviceNm;
        this.deviceAddress = deviceAddress;
        this.isFound = isFound;
        this.isConnected = isConnected;
    }


    String getDeviceNm() {
        return deviceNm;
    }

    public void setDeviceNm(String deviceNm) {
        this.deviceNm = deviceNm;
    }

    String getDeviceAddress() {
        return deviceAddress;
    }

    public void setDeviceAddress(String deviceAddress) {
        this.deviceAddress = deviceAddress;
    }

    public boolean isFound() {
        return isFound;
    }

    public void setFound(boolean found) {
        isFound = found;
    }

    public boolean isConnected() {
        return isConnected;
    }

    public void setConnected(boolean connected) {
        isConnected = connected;
    }
}