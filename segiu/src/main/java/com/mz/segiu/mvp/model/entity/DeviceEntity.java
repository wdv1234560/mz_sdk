package com.mz.segiu.mvp.model.entity;

public class DeviceEntity {
    public String deviceId;
    public int versionCode;
    public String versionName;

    public DeviceEntity(String deviceId, int versionCode, String versionName) {
        this.deviceId = deviceId;
        this.versionCode = versionCode;
        this.versionName = versionName;
    }
}
