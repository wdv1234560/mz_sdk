package com.mz.segiu.mvp.model.entity;

public class LocationEntity {
    double latitude = 0;
    double longitude = 0;
    String address = "";
    String city = "";
    String province = "";

    public LocationEntity(double latitude, double longitude, String address, String city, String province) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.address = address;
        this.city = city;
        this.province = province;
    }
}
