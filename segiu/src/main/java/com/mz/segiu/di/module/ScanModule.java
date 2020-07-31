package com.mz.segiu.di.module;


import androidx.fragment.app.FragmentActivity;

import com.jess.arms.di.scope.ActivityScope;
import com.mz.segiu.mvp.contract.ScanContract;
import com.mz.segiu.mvp.model.ScanModel;
import com.tbruyelle.rxpermissions2.RxPermissions;

import java.util.HashMap;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;


@Module
public abstract class ScanModule {

    @Binds
    abstract ScanContract.Model bindScanModel(ScanModel model);


    @ActivityScope
    @Provides
    static HashMap<String,Object> provideJsResMap() {
        return new HashMap<String,Object>();
    }

    @ActivityScope
    @Provides
    static RxPermissions provideRxPermissions( ScanContract.View view) {
        return new RxPermissions((FragmentActivity) view.getMzActivity());
    }
}