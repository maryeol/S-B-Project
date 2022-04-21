package com.example.myapplication;

import java.util.ArrayList;
import java.util.List;

public class BeaconModel {
    private String bleAddress;
    private String Url;
    private String Rssi;

    //Constructor
    public BeaconModel(){}

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
