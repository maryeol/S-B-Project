package com.example.myapplication;

public class BeaconModel {
    private String bleAddress;
    private String Url;
    private String Rssi;

    //Constructor
    public BeaconModel(){}

    //Getters and Setters
    public String getBleAddress() {
        return bleAddress;
    }

    public void setBleAddress(String bleAddress) {
        this.bleAddress = bleAddress;
    }

    public String getUrl() {
        return Url;
    }

    public void setUrl(String url) {
        Url = url;
    }

    public String getRssi() {
        return Rssi;
    }

    public void setRssi(String rssi) {
        Rssi = rssi;
    }

}
