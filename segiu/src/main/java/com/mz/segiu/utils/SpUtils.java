package com.mz.segiu.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.webkit.WebStorage;

import com.jess.arms.integration.AppManager;
import com.jess.arms.utils.Convert;
import com.mz.segiu.mvp.model.entity.OrgEntity;
import com.mz.segiu.widget.webview.ComParams;

/**
 * @创建者 曹家旭
 * @创建时间 2016-4-5 上午11:12:16
 * @描述 对sp操作的封装
 * @版本 $Rev: 3 $
 * @更新者 $Author: admin $
 * @更新时间 $Date: 2016-04-05 11:28:11 +0800 (星期二, 05 四月 2016) $
 * @更新描述 TODO
 */
public class SpUtils {
    private static SpUtils mSpUtils;
    private SharedPreferences mSp;
    private Editor mEditor;

    private SpUtils(Context context) {
        mSp = context.getSharedPreferences("mz", Context.MODE_PRIVATE);
        mEditor = mSp.edit();
    }

    public static SpUtils getInstance(Context context) {
        if (mSpUtils == null) {
            mSpUtils = new SpUtils(context);
        }
        return mSpUtils;
    }

    /**
     * 存入string
     */
    public void putString(String key, String value) {
        mEditor.putString(key, value);
        mEditor.commit();
    }

    /**
     * 存入int
     */
    public void putInt(String key, int value) {
        mEditor.putInt(key, value);
        mEditor.commit();
    }

    /**
     * 存入boolean
     */
    public void putBoolean(String key, boolean value) {
        mEditor.putBoolean(key, value);
        mEditor.commit();
    }

    /**
     * 取出string
     */
    public String getString(String key, String defValue) {
        return mSp.getString(key, defValue);
    }

    /**
     * 取出int
     */
    public int getInt(String key, int defValue) {
        return mSp.getInt(key, defValue);
    }

    /**
     * 取出boolean
     */
    public boolean getBoolean(String key, boolean defValue) {
        return mSp.getBoolean(key, defValue);
    }

    public void setToken(String token) {
        mEditor.putString("token", token);
        mEditor.commit();
    }


    public OrgEntity getOrgInfo() {
        OrgEntity orgEntity = Convert.fromJson(getString(ComParams.ORG_RECORD, ""), OrgEntity.class);
        if (orgEntity == null) {
            orgEntity = new OrgEntity();
        }
        return orgEntity;
    }

    public String getToken() {
//        return mSp.getString("token", "");
        return mSp.getString("token", "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJ1c2VyX25hbWUiOiI0NzI1MTQzMTY5MDQxMzY3MDRfYXBwIiwiYXBwVHlwZSI6ImFwcCIsInNjb3BlIjpbIioiXSwibG9naW5OYW1lIjoiNDcyNTE0MzE2OTA0MTM2NzA0X2FwcCIsImV4cCI6MTU5NjIwMTk2NSwidXNlcklkIjoiNDcyNTE0MzE2OTA0MTM2NzA0IiwianRpIjoiNzliN2U4ZGMtNDQ2MC00YTY3LTlhOTktZjEyMDA2NTA2ZTUzIiwiY2xpZW50X2lkIjoicGFhc2Nsb3VkLWNsaWVudC11YWMiLCJ0aW1lc3RhbXAiOjE1OTYxNTg3NjUzNzd9.ORT8kbI3MIM1OGqEfh1NNtYfw0rel72cNtFNwKe02yY");
    }

    public void clearLogin() {
        //删除极光别名
        WebStorage.getInstance().deleteAllData();
        mEditor.putString("token", "")
                .putBoolean("isLogin", false)
//                .putString("client_pwd", "")
//                .putString("staff_pwd", "")
                .commit();
    }
}
