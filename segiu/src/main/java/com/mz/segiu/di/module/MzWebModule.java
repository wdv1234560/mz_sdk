package com.mz.segiu.di.module;

import android.content.Context;

import androidx.fragment.app.FragmentActivity;

import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.example.sca100.ScaleDevice;
import com.jess.arms.di.scope.ActivityScope;
import com.mz.segiu.app.GlideEngine;
import com.mz.segiu.utils.PictureSelectorUtils;
import com.luck.picture.lib.PictureSelectionModel;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.engine.ImageEngine;
import com.mz.segiu.ble.BleProtocol;
import com.mz.segiu.mvp.contract.MzWebContract;
import com.mz.segiu.mvp.model.MzWebModel;
import com.tbruyelle.rxpermissions2.RxPermissions;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.inject.Named;

import dagger.Binds;
import dagger.Module;
import dagger.Provides;


@Module
public abstract class MzWebModule {

    @Binds
    abstract MzWebContract.Model bindMzWebModel(MzWebModel model);

    @ActivityScope
    @Provides
    static ScaleDevice provideScaleDevice() {
        return new ScaleDevice();
    }

    @ActivityScope
    @Provides
    static HashMap<String, Object> provideJsResMap() {
        return new HashMap<String, Object>();
    }

    @ActivityScope
    @Provides
    static PictureSelectionModel providePictureSelector(MzWebContract.View view) {
        return PictureSelector.create(view.getMzWebViewActivity()).openCamera(PictureMimeType.ofImage()).isCompress(true).isUseCustomCamera(PictureSelectorUtils.isUserCustomCamera()).loadImageEngine((ImageEngine) GlideEngine.createGlideEngine());

    }

    @ActivityScope
    @Provides
    static BleProtocol provideBleProtocol() {
        return new BleProtocol();
    }

    @ActivityScope
    @Provides
    @Named("listData")
    static ArrayList<Map<String, Object>> provideListData() {
        return new ArrayList<Map<String, Object>>();
    }

    @ActivityScope
    @Provides
    @Named("lastListData")
    static ArrayList<Map<String, Object>> provideLastListData() {
        return new ArrayList<Map<String, Object>>();
    }

    @ActivityScope
    @Provides
    @NotNull
    static RxPermissions provideRxPermissions(MzWebContract.View view) {
        return new RxPermissions((FragmentActivity) view.getMzWebViewActivity());
    }

    @ActivityScope
    @Provides
    static AMapLocationClient provideAMapLocationClient(MzWebContract.View view, @Nullable AMapLocationClientOption option) {
        AMapLocationClient client = new AMapLocationClient((Context) view.getMzWebViewActivity());
        client.setLocationOption(option);
        return client;
    }

    @ActivityScope
    @Provides
    static AMapLocationClientOption provideAMapLocationClientOption() {
        AMapLocationClientOption option = new AMapLocationClientOption();
        option.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        option.setInterval(2000L);
        return option;
    }
}