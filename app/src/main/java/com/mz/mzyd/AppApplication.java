package com.mz.mzyd;

import android.app.Application;
import android.content.Context;


import androidx.multidex.MultiDex;

import com.jess.arms.base.MzSDK;


public class AppApplication extends Application {

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(base);

    }

    @Override
    public void onCreate() {
        super.onCreate();
        MzSDK.init(this);
//        MzSDK.onCreate(this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
//        MzSDK.onTerminate(this);
    }
}
