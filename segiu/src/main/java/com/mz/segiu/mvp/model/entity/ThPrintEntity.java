package com.mz.segiu.mvp.model.entity;

import android.text.TextUtils;



public class ThPrintEntity extends BaseFormJsEntity<ThPrintEntity> {
    public ThPrintEntity(String orgName, String deskWorkName, String medicalName, String weight, String userName, String heirName, String date, String medicalWasteCode) {
        this.orgName = orgName;
        this.deskWorkName = deskWorkName;
        this.medicalName = medicalName;
        this.weight = weight;
        this.userName = userName;
        this.heirName = heirName;
        this.date = date;
        this.medicalWasteCode = medicalWasteCode;
    }

    public String orgName;//项目名
    public String deskWorkName;//科室
    public String medicalName;//医废类型
    public String weight;//重量
    public String userName;//录入人
    public String heirName;//交接人
    public String date;//时间
    public String medicalWasteCode;//医废编码

    public String getOrgName() {
        return orgName;
    }

    public String getDeskWorkName() {
        return deskWorkName;
    }

    public String getMedicalName() {
        return medicalName;
    }

    public String getWeight() {
        return weight;
    }

    public String getUserName() {
        return userName;
    }

    public String getHeirName() {
        if(TextUtils.isEmpty(this.heirName)){
            return "无";
        }
        return heirName;
    }

    public String getDate() {
        return date;
    }

    public String getMedicalWasteCode() {
        return medicalWasteCode;
    }

    @Override
    public String toString() {
        return "ThPrintEntity{" +
                "orgName:'" + orgName + '\'' +
                ", deskWorkName:'" + deskWorkName + '\'' +
                ", medicalName:'" + medicalName + '\'' +
                ", weight:'" + weight + '\'' +
                ", userName:'" + userName + '\'' +
                ", heirName:'" + heirName + '\'' +
                ", date:'" + date + '\'' +
                ", medicalWasteCode:'" + medicalWasteCode + '\'' +
                '}';
    }
}
