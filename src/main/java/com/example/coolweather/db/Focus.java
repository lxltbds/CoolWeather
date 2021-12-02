package com.example.coolweather.db;

import org.litepal.crud.DataSupport;

public class Focus extends DataSupport {
    private String weatherId;
    private String address;

    public void setAddress(String address) {
        this.address = address;
    }

    public String getAddress() {
        return address;
    }

    public void setWeatherId(String weatherId) {
        this.weatherId = weatherId;
    }

    public String getWeatherId() {
        return weatherId;
    }
}
